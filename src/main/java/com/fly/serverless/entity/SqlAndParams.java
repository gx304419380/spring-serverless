package com.fly.serverless.entity;

import java.util.Arrays;
import java.util.List;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/6/25
 */
public class SqlAndParams {
    private String sql;
    private Object[] params;

    public SqlAndParams() {
    }

    public SqlAndParams(String sql) {
        this.sql = sql;
    }

    public SqlAndParams(String sql, List<Object> paramList) {
        this.sql = sql;
        this.params = paramList.toArray();
    }

    /**
     * 获取当前sql类型，select insert delete update
     * @return type
     */
    public String getType() {
        if (sql.length() < 6) {
            return sql;
        }
        return sql.replace("\r\n\t", "").trim().substring(0, 6).toLowerCase();
    }

    public String getSql() {
        return sql;
    }

    public SqlAndParams setSql(String sql) {
        this.sql = sql;
        return this;
    }

    public Object[] getParams() {
        return params;
    }

    public SqlAndParams setParams(Object[] params) {
        this.params = params;
        return this;
    }

    @Override
    public String toString() {
        return "SqlAndParams{" +
                "sql='" + sql + '\'' +
                ", params=" + Arrays.toString(params) +
                '}';
    }
}
