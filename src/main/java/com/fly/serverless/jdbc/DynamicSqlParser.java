package com.fly.serverless.jdbc;

import com.fly.serverless.common.BaseException;
import com.fly.serverless.entity.SqlAndParams;
import com.fly.serverless.util.Assert;
import com.fly.serverless.util.ExpressionUtils;
import org.dom4j.*;
import org.dom4j.tree.DefaultCDATA;
import org.dom4j.tree.DefaultText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.fly.serverless.common.SqlConstant.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.joining;

/**
 * 海尔再也没人能写出这么漂亮的代码了
 *
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/6/22
 */
public class DynamicSqlParser {

    private static final Logger log = LoggerFactory.getLogger(DynamicSqlParser.class);

    private DynamicSqlParser() {
        //这是一个工具类
    }

    /**
     * 将数据库中的sql解析为可执行的sql语句
     *
     * @param sql    数据库中的sql
     * @param params 参数
     * @return sql
     */
    public static String generateSql(String sql, Object params) {
        //如果不是xml格式，先封装成xml，用于适配以往版本
        if (!XML_PATTERN.matcher(sql).matches()) {
            sql = DEFAULT_XML_TAG + sql + DEFAULT_XML_TAG_END;
        }

        Document document;
        try {
            document = DocumentHelper.parseText(sql);
        } catch (DocumentException e) {
            throw new BaseException(e);
        }

        return parseXml(document.getRootElement(), params);
    }

    public static SqlAndParams parseSql(String sql, Object params) {
        ExpressionHandler.SqlHandler sqlHandler = ExpressionHandler.getSqlHandler();
        String s = parseSql(sql, params, sqlHandler);
        return new SqlAndParams(s, sqlHandler.getParams());
    }


    /**
     * 处理节点
     *
     * @param node    node
     * @param context 参数上下文
     * @return sql
     */
    private static String parseXml(Node node, Object context) {
        //如果是普通sql || 如果是CDATA
        if (node instanceof DefaultText || node instanceof DefaultCDATA) {
            return node.getText();
        }

        //如果不认识，则返回空串
        if (!(node instanceof Element)) {
            return EMPTY_STRING;
        }

        Element element = (Element) node;
        String name = element.getName();

        //处理各种标签
        switch (name) {
            case WHERE:
                return handleWhere(element, context);
            case IF:
            case WHEN:
                return handleIf(element, context);
            case FOR:
                return handleFor(element, context);
            case SQL:
            case OTHERWISE:
                return handleRoot(element, context);
            case CHOOSE:
                return handleChoose(element, context);
            default:
                return EMPTY_STRING;
        }
    }

    /**
     * 处理根节点
     *
     * @param element 元素
     * @param context 上下文
     * @return sql
     */
    private static String handleRoot(Element element, Object context) {
        return element.content()
                .stream()
                .map(n -> parseXml(n, context))
                .collect(joining());
    }


    /**
     * 处理<if>标签
     *
     * @param element 元素
     * @param context 参数
     * @return sql
     */
    private static String handleIf(Element element, Object context) {
        Attribute test = element.attribute(TEST);
        Object value = ExpressionUtils.parse(test.getValue(), context);

        if (FALSE.equals(value)) {
            return EMPTY_STRING;
        }

        return handleRoot(element, context);
    }

    /**
     * 处理where标签
     *
     * @param element 元素
     * @param context 参数
     * @return sql
     */
    private static String handleWhere(Element element, Object context) {

        String sql = handleRoot(element, context);

        if (ObjectUtils.isEmpty(sql)) {
            return EMPTY_STRING;
        }

        sql = sql.replaceAll(LINE, SPACE).trim();

        if (ObjectUtils.isEmpty(sql)) {
            return EMPTY_STRING;
        }

        //替换掉开头的and或者or
        return WHERE + SPACE + sql.replaceFirst(AND_OR, EMPTY_STRING);
    }

    /**
     * 处理for标签 语法兼容mybatis
     * 原理：将for标签解析成多个sql片段，将其中的#xxx#标签转换为对应的#list[i].xxx#
     * 在执行sql时，使用ognl表达式取值
     *
     * @param element 元素
     * @param context 上下文
     * @return sql
     */
    private static String handleFor(Element element, Object context) {
        String listName = getAttribute(element, LIST, COLLECTION);
        String itemName = getAttribute(element, ITEM);
        String start = getAttribute(element, START, OPEN);
        String end = getAttribute(element, END, CLOSE);
        String join = getAttribute(element, JOIN, SEPARATOR);

        Assert.notEmpty(listName, SQL_ERROR_CODE, SQL_ERROR);

        String sqlFragment = element.getText().replace(LINE, SPACE);

        Collection<Object> list = ExpressionUtils.parse(listName, context);

        if (ObjectUtils.isEmpty(list)) {
            log.warn("- list: {} is empty!", listName);
            return start + end;
        }

        //生成sql循环
        ExpressionHandler.ForHandler handler = new ExpressionHandler.ForHandler(itemName, listName);

        return IntStream.range(0, list.size())
                .boxed()
                .map(i -> parseSql(sqlFragment, context, handler.setIndex(i)))
                .collect(joining(join, start, end));
    }

    private static String getAttribute(Element element, String... names) {
        return Stream.of(names)
                .map(element::attribute)
                .filter(Objects::nonNull)
                .findAny()
                .map(Attribute::getValue)
                .orElse(EMPTY_STRING);
    }

    /**
     * 处理choose标签
     *
     * @param element   element
     * @param context   context
     * @return          sql
     */
    private static String handleChoose(Element element, Object context) {
        List<Node> content = element.content();

        Optional<Node> whenNode = content.stream()
                .filter(node -> WHEN.equals(node.getName()))
                .filter(node -> testWhen(node, context))
                .findFirst();

        //如果存在满足条件地when，则删除其他的when和otherwise
        if (whenNode.isPresent()) {
            Node when = whenNode.get();
            content.removeIf(node -> WHEN.equals(node.getName()) && !node.equals(when));
            content.removeIf(node -> OTHERWISE.equals(node.getName()));
        }
        //否则删除所有的when，保留otherwise
        else {
            content.removeIf(node -> WHEN.equals(node.getName()));
        }

        //校验最多只有一个when或者other
        long count = content.stream()
                .filter(node -> WHEN.equals(node.getName()) || OTHERWISE.equals(node.getName()))
                .count();

        if (count > 1) {
            log.error("when tag or otherwise tag is more than one: {}", element);
            throw new BaseException(SQL_ERROR_CODE, SQL_ERROR);
        }

        return content.stream()
                .map(n -> parseXml(n, context))
                .collect(joining());
    }

    private static boolean testWhen(Node node, Object context) {
        Element element = (Element) node;
        Attribute test = element.attribute(TEST);
        Boolean result = ExpressionUtils.parse(test.getValue(), context);

        return TRUE.equals(result);
    }


    /**
     * 解析sql语句为可执行的sql和相应的参数
     * 这部分为了避免使用正则表达式因而手动解析
     * 逻辑极其复杂普通人别tm乱动
     *
     * @param sql sql
     * @return 可执行sql： select * from tt where id = ?
     */
    private static String parseSql(String sql, Object context, ExpressionHandler handler) {
        // search open token
        int start = sql.indexOf(OPEN_TOKEN);
        if (start == -1) {
            return sql;
        }
        char[] src = sql.toCharArray();
        int offset = 0;
        final StringBuilder builder = new StringBuilder();
        StringBuilder expression = null;
        do {
            if (start > 0 && src[start - 1] == '\\') {
                builder.append(src, offset, start - offset - 1).append(OPEN_TOKEN);
                offset = start + OPEN_TOKEN.length();
            } else {
                if (expression == null) {
                    expression = new StringBuilder();
                } else {
                    expression.setLength(0);
                }
                builder.append(src, offset, start - offset);
                offset = start + OPEN_TOKEN.length();
                int end = sql.indexOf(CLOSE_TOKEN, offset);
                while (end > -1) {
                    if (end > offset && src[end - 1] == '\\') {
                        expression.append(src, offset, end - offset - 1).append(CLOSE_TOKEN);
                        offset = end + CLOSE_TOKEN.length();
                        end = sql.indexOf(CLOSE_TOKEN, offset);
                    } else {
                        expression.append(src, offset, end - offset);
                        break;
                    }
                }
                if (end == -1) {
                    builder.append(src, start, src.length - start);
                    offset = src.length;
                } else {
                    String replace = handler.handleExpression(expression.toString(), context);
                    builder.append(replace);
                    offset = end + CLOSE_TOKEN.length();
                }
            }
            start = sql.indexOf(OPEN_TOKEN, offset);
        } while (start > -1);
        if (offset < src.length) {
            builder.append(src, offset, src.length - offset);
        }

        return builder.toString();
    }

}
