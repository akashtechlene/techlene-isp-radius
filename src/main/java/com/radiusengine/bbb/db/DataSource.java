/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.bbb.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DataSource {
    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    public static void setupDataSource(String dbURL, String userName, String password, String cachePrepStmts, String prepStmtCacheSize, String prepStmtCacheSqlLimit, Integer minIdlePool, int maxPoolSize) {
        config.setJdbcUrl(dbURL);
        config.setUsername(userName);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", cachePrepStmts);
        config.addDataSourceProperty("prepStmtCacheSize", prepStmtCacheSize);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", prepStmtCacheSqlLimit);
        config.setMinimumIdle(minIdlePool);
        config.setMaximumPoolSize(maxPoolSize);
        config.setMaxLifetime(300000L);
        config.setConnectionTimeout(5000L);
        config.setValidationTimeout(3000L);
        ds = new HikariDataSource(config);
    }

    private DataSource() {
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
