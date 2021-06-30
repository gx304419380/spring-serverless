package com.fly.serverless.jdbc;

import com.fly.serverless.util.ExpressionUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.fly.serverless.common.SqlConstant.*;
import static java.util.stream.Collectors.joining;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/6/29
 */

public interface ExpressionHandler {
    /**
     * 处理expression
     *
     * @param expression el表达式
     * @param context       上下文
     * @return          表达式替换字符串
     */
    String handleExpression(String expression, Object context);


    /**
     * 处理sql
     * @return  sql
     */
    static SqlHandler getSqlHandler() {
        return new SqlHandler();
    }

    /**
     * 处理for语句
     * @param item  item
     * @param list  list
     * @return      for sql片段
     */
    static ForHandler getForHandler(String item, String list) {
        return new ForHandler(item, list);
    }

    @Data
    class SqlHandler implements ExpressionHandler {

        private List<Object> params = new ArrayList<>();

        @Override
        public String handleExpression(String expression, Object context) {
            Object value = ExpressionUtils.parse(expression, context);

            //普通数据类型
            if (!(value instanceof Collection)) {
                params.add(value);
                return PLACEHOLDER;
            }

            //处理数组和集合
            Collection<?> collection = (Collection<?>) value;
            params.addAll(collection);
            return collection.stream().map(s -> PLACEHOLDER)
                    .collect(joining(",", "(", ")"));
        }
    }

    class ForHandler implements ExpressionHandler {

        Integer index;
        String item;
        String list;

        public ForHandler(String item, String list) {
            this.item = item;
            this.list = list;
        }

        public ForHandler setIndex(Integer index) {
            this.index = index;
            return this;
        }

        @Override
        public String handleExpression(String expression, Object context) {
            String key = expression.trim();

            if (key.equals(item)) {
                return OPEN_TOKEN + list + "[" + index + "]" + CLOSE_TOKEN;
            }

            if (key.startsWith(item + ".") || key.startsWith(item + "[")) {
                String replace = list + "[" + index + "]";
                String s = key.replaceFirst(item, replace);
                return OPEN_TOKEN + s + CLOSE_TOKEN;
            }

            return expression;
        }
    }

}
