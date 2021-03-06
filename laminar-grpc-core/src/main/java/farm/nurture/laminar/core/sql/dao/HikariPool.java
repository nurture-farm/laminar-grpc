/*
 *  Copyright 2022 Nurture.Farm
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package farm.nurture.laminar.core.sql.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HikariPool implements IPool {

    private static final Logger LOG =
        LoggerFactory.getLogger(farm.nurture.laminar.core.sql.dao.HikariPool.class);
    private String poolName;
    private HikariDataSource hikariDataSource;

    public HikariPool(String poolName) {
        this.poolName = poolName;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (hikariDataSource == null) {
            LOG.error("Hikari data source is not initialized for poolName : {}", poolName);
            throw new SQLException("Data source is not initialized");
        }
        return hikariDataSource.getConnection();
    }

    @Override
    public void returnConnection(Connection returnedConnection) throws SQLException {}

    @Override
    public void start(DbConfig dbConfig) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbConfig.getConnectionUrl());
        config.setUsername(dbConfig.getLogin());
        config.setPassword(dbConfig.getPassword());
        config.setMaximumPoolSize(dbConfig.getMaxConnections());
        config.setMinimumIdle(dbConfig.getIdleConnections());
        config.setConnectionTestQuery(dbConfig.getTestSql());
        config.setConnectionInitSql(dbConfig.getTestSql());
        config.setDriverClassName(dbConfig.getDriverClass());
        config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        config.setPoolName(dbConfig.getPoolName());
        config.setConnectionTimeout(dbConfig.getConnectionTimeoutMillis());
        config.setValidationTimeout(dbConfig.getValidationTimeoutMillis());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.hikariDataSource = new HikariDataSource(config);
    }

    @Override
    public void stop() {
        if (hikariDataSource != null) {
            hikariDataSource.close();
        }
    }

    @Override
    public void stop(boolean releaseConnection) {
        if (hikariDataSource != null) {
            hikariDataSource.close();
        }
    }
}
