package com.fly.serverless.filter;

import com.fly.serverless.jdbc.Jdbc;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.sql.DataSource;
import java.io.IOException;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/6/30
 */
@WebFilter(urlPatterns = "/serverless/*", filterName = "JdbcFilter")
public class JdbcFilter implements Filter {

    @Resource
    private DataSource dataSource;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        DataSource source = getDataSourceByRequest(request);
        Jdbc.setDataSource(source);

        try {
            chain.doFilter(request, response);
        } finally {
            Jdbc.removeDataSource();
        }
    }

    /**
     * 获取用户的数据源
     *
     * @param request   请求
     * @return          数据源
     */
    private DataSource getDataSourceByRequest(ServletRequest request) {
        return dataSource;
    }
}
