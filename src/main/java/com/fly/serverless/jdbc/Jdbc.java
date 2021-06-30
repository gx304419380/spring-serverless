package com.fly.serverless.jdbc;

import com.fly.serverless.entity.SqlAndParams;
import lombok.experimental.UtilityClass;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.util.ObjectUtils;

import javax.sql.DataSource;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Map;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/6/30
 */
@UtilityClass
public class Jdbc {

    private static final ThreadLocal<DataSource> DATA_SOURCE_HOLDER = new ThreadLocal<>();

    private static JdbcTemplate getTemplate() {
        DataSource dataSource = DATA_SOURCE_HOLDER.get();
        return new JdbcTemplate(dataSource);
    }

    public static void setDataSource(DataSource dataSource) {
        DATA_SOURCE_HOLDER.set(dataSource);
    }

    public static void removeDataSource() {
        DATA_SOURCE_HOLDER.remove();
    }

    public static <T> T selectOne(String dynamicSql, Class<T> type, Object param) {
        List<T> list = selectList(dynamicSql, type, param);

        if (ObjectUtils.isEmpty(list)) {
            return null;
        }

        return list.get(0);
    }

    /**
     * 查询list
     *
     * @param dynamicSql   动态sql
     * @param type  返回值类型
     * @param <T>   泛型
     * @return      list
     */
    public static <T> List<T> selectList(String dynamicSql, Class<T> type, Object param) {
        JdbcTemplate template = getTemplate();

        //解析动态sql语句
        String sql = DynamicSqlParser.generateSql(dynamicSql, param);
        SqlAndParams sqlAndParams = DynamicSqlParser.parseSql(sql, param);
        RowMapper<T> rowMapper;

        //基本数据类型
        if (Number.class.isAssignableFrom(type) || type.equals(String.class) || Temporal.class.isAssignableFrom(type)) {
            rowMapper = new SingleColumnRowMapper<>(type);
        }
        //其他数据类型
        else {
            rowMapper = new BeanPropertyRowMapper<>(type);
        }

        return template.query(sqlAndParams.getSql(), rowMapper, sqlAndParams.getParams());
    }


    /**
     * 查询list
     *
     * @param dynamicSql   动态sql
     * @return      list
     */
    public static List<Map<String, Object>> selectMapList(String dynamicSql, Object param) {
        JdbcTemplate template = getTemplate();

        //解析动态sql语句
        String sql = DynamicSqlParser.generateSql(dynamicSql, param);
        SqlAndParams sqlAndParams = DynamicSqlParser.parseSql(sql, param);

        return template.queryForList(sqlAndParams.getSql(), sqlAndParams.getParams());
    }

}
