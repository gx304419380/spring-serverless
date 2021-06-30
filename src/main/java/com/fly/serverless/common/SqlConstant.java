package com.fly.serverless.common;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/6/22
 */
@UtilityClass
public class SqlConstant {
    public static final Integer SQL_ERROR_CODE = 1;
    public static final String SQL_ERROR = "SQL has error";

    public static final String SQL = "sql";
    public static final String DYNAMIC_SQL = "DynamicSql";
    public static final String ONE_SQL = "OneSql";
    public static final String MORE_SQL = "MoreSql";
    public static final String EMPTY_STRING = "";
    public static final Pattern XML_PATTERN = Pattern.compile("^<[^/]\\S+?>.*</\\S+>$");
    public static final String DEFAULT_XML_TAG = "<sql>";
    public static final String DEFAULT_XML_TAG_END = "</sql>";
    public static final String LINE = "\n";
    public static final String SPACE = " ";
    public static final String SQL_DELIMITER = "; ";

    public static final String OPEN_TOKEN = "#";
    public static final String CLOSE_TOKEN = "#";
    public static final String PLACEHOLDER = "?";


    public static final String WHERE = "where";
    public static final String WHERE_DEFAULT = "where 1=1";
    public static final String AND = "and";
    public static final String OR = "or";
    public static final String AND_OR = "^(and |AND |or |OR )";


    public static final String CHOOSE = "choose";
    public static final String IF = "if";
    public static final String WHEN = "when";
    public static final String OTHERWISE = "otherwise";
    public static final String TEST = "test";

    public static final String FOR = "for";
    public static final String LIST = "list";
    public static final String COLLECTION = "collection";
    public static final String ITEM = "item";
    public static final String START = "start";
    public static final String OPEN = "open";
    public static final String END = "end";
    public static final String CLOSE = "close";
    public static final String JOIN = "join";
    public static final String SEPARATOR = "separator";

    public static final String SELECT = "select";
    public static final String UPDATE = "update";
}
