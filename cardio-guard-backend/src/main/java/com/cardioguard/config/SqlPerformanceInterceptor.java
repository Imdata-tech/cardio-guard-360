package com.cardioguard.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.Properties;

/**
 * SQL性能监控拦截器
 * 记录执行时间超过阈值的慢查询
 */
@Slf4j
@Component
@Intercepts({
    @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, org.apache.ibatis.session.ResultHandler.class}),
    @Signature(type = StatementHandler.class, method = "update", args = {Statement.class})
})
public class SqlPerformanceInterceptor implements Interceptor {
    
    /**
     * 慢查询阈值（毫秒）
     */
    private static final long SLOW_QUERY_THRESHOLD = 1000;
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            // 执行原始SQL
            return invocation.proceed();
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 如果执行时间超过阈值，记录警告日志
            if (executionTime > SLOW_QUERY_THRESHOLD) {
                String sql = getSql(invocation);
                log.warn("⚠️ 慢查询检测 | 执行时间: {}ms | SQL: {}", executionTime, sql);
            } else if (log.isDebugEnabled()) {
                // DEBUG级别记录所有SQL执行时间
                String sql = getSql(invocation);
                log.debug("SQL执行 | 耗时: {}ms | SQL: {}", executionTime, sql);
            }
        }
    }
    
    /**
     * 从Invocation中提取SQL语句
     */
    private String getSql(Invocation invocation) {
        try {
            StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
            MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
            
            // 获取BoundSql
            BoundSql boundSql = statementHandler.getBoundSql();
            String sql = boundSql.getSql();
            
            // 格式化SQL（去除多余空格和换行）
            return sql.replaceAll("\\s+", " ").trim();
            
        } catch (Exception e) {
            log.error("获取SQL失败", e);
            return "Unknown SQL";
        }
    }
    
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
    
    @Override
    public void setProperties(Properties properties) {
        // 可以从配置文件读取阈值
        String threshold = properties.getProperty("slowQueryThreshold");
        if (threshold != null) {
            try {
                long value = Long.parseLong(threshold);
                if (value > 0) {
                    log.info("慢查询阈值已设置为: {}ms", value);
                }
            } catch (NumberFormatException e) {
                log.warn("无效的慢查询阈值配置: {}", threshold);
            }
        }
    }
}
