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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Properties;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Pool implements IPool {

    private static final Logger LOG = LoggerFactory.getLogger(Pool.class);
    private final Timer AGENT_TIMER = new Timer(true);
    private DbConfig config = null;
    private Stack<PoolConnection> availablePool = new Stack<>();
    private HealthCheckAgent hcAgent = null;
    private AtomicInteger createdConnection = new AtomicInteger(0);
    private AtomicInteger returnConnection = new AtomicInteger(0);
    private AtomicInteger destroyedConnection = new AtomicInteger(0);
    private String poolType;

    public Pool(final String poolType) {
        this.poolType = poolType;
    }

    private final Connection getNTestConnection() throws SQLException {

        /** Step # 1 Get a connection */
        PoolConnection poolCon = this.poolGetWrapper();
        boolean connectionAvailable = poolCon != null;

        if (null == poolCon) poolCon = this.createConnectionWrapper();

        /** Step # 2 Test for the null, closed and dirty connection */
        boolean goodConnection = true;
        goodConnection = processPoolConn(poolCon, goodConnection);

        /** Step # 3 Give a null connection and destroy the bad connections. */
        if (goodConnection) {
            return poolCon;
        } else {
            try {
                this.destroyPoolConnection(poolCon);
            } catch (Exception ex) {
                LOG.error("Potential connection leakage. check the root cause ..", ex);
            }
            return null;
        }
    }

    private boolean processPoolConn(PoolConnection poolCon, boolean goodConnection)
        throws SQLException {
        if (poolCon == null) {
            LOG.debug("Pool connection gave a null connection");
            goodConnection = false;
            throw new SQLException(
                "Could not make connection to database " + this.config.getConnectionUrl());
        }
        if (poolCon.isClosed()) {
            LOG.debug("Pool connection is already closed");
            goodConnection = false;
        }
        if (poolCon.isDirty()) {
            LOG.debug("Pool connection is dirty.");
            goodConnection = false;
        }
        return goodConnection;
    }

    private synchronized PoolConnection createConnectionWrapper() {

        if (this.getLiveConnection() >= this.config.getMaxConnections()) {
            // if ( null != jdbcCreateConnectionExceeded ) jdbcCreateConnectionExceeded.increment();
            return null;
        }

        PoolConnection pc = null;
        try {
            pc = createConnection();

            if (pc != null) {
                this.createdConnection.incrementAndGet();
                //	if(jdbcCreatedConnections != null) jdbcCreatedConnections.increment();
            } else {
                //	if ( null != jdbcCreateConnectionFailure ) jdbcCreateConnectionFailure.increment();
            }
        } catch (Exception ex) {
            // if ( null != jdbcCreateConnectionFailure ) jdbcCreateConnectionFailure.increment();
        }
        return pc;
    }

    private PoolConnection createConnection() {
        if (this.config == null)
            throw new RuntimeException("Unable to open the pool connection as configuration is absent.");

        try {
            Class.forName(this.config.getDriverClass()).newInstance();
        } catch (Exception ex) {
            LOG.error("Could not load the driver class, " + this.config.getDriverClass(), ex);
            throw new RuntimeException(
                "Unable to create pool connection. Unable to load the driver class.");
        }

        if (LOG.isDebugEnabled()) LOG.debug("Creating a new connection for {}", this.poolType);

        try {
            return processConnection();
        } catch (SQLException ex) {
            LOG.error(
                ("Error in accessing database, "
                    + "\tjdbcurl:"
                    + this.config.getConnectionUrl()
                    + "\tlogin:"
                    + this.config.getLogin()
                    + "\tpasswd:"
                    + this.config.getPassword()),
                ex);
            throw new RuntimeException("Error in accessing database using the config params.");
        } catch (Exception ex) {
            LOG.error("Error in creating connection", ex);
            throw new RuntimeException("Error in creating connection using the config params.");
        }
    }

    private PoolConnection processConnection() throws SQLException {
        PoolConnection poolCon = null;
        Connection con = null;
        if (this.config.isAllowMultiQueries()) {
            Properties props = new Properties();
            props.put("allowMultiQueries", "true");
            props.put("user", this.config.getLogin());
            props.put("password", this.config.getPassword());
            con = DriverManager.getConnection(this.config.getConnectionUrl(), props);
        } else {
            con =
                DriverManager.getConnection(
                    this.config.getConnectionUrl(), this.config.getLogin(), this.config.getPassword());
        }

        try {
            // presto throws exception here
            con.setTransactionIsolation(this.config.getIsolationLevel());
        } catch (Exception ex) {
            LOG.warn(ex.getLocalizedMessage());
        }

        poolCon = new PoolConnection(con, this.poolType);

        return poolCon;
    }

    private final int increment(final int count) {
        if (LOG.isInfoEnabled()) LOG.info("Incrementing Connections by {}", count);
        int numIncremented = 0;
        for (int i = 0; i < count; i++) {
            try {
                PoolConnection con = this.createConnectionWrapper();
                if (null != con) {
                    con.close(); // Returns to the stack
                    numIncremented++;
                }
                Thread.sleep(this.config.getTimeBetweenConnections());

            } catch (Exception ex) {
                LOG.error("Error in creating connection", ex);
            }
        }
        return numIncremented;
    }

    /**
     * This is explicitly called when people call Connection.close(). Make sure no other place it is
     * called.
     *
     * @param returnedConnection
     * @throws IllegalStateException
     */
    public final void returnConnection(final Connection returnedConnection) throws SQLException {

        PoolConnection poolConnection = null;
        if (returnedConnection instanceof PoolConnection) {
            poolConnection = (PoolConnection) returnedConnection;
        }

        if (null != poolConnection) {
            try {
                // presto throws exception here
                poolConnection.setTransactionIsolation(this.config.getIsolationLevel());
                poolConnection.setAutoCommit(true);
            } catch (Exception ex) {
                LOG.warn(ex.getLocalizedMessage());
            }

            boolean status = this.poolReturn(poolConnection);
            // Metric is out of synchronized block
            if (status) {
                // if ( null != jdbcReturnedConnections ) jdbcReturnedConnections.increment();
            } else {
                // if ( null != jdbcReturnedConnectionsFailure) jdbcReturnedConnectionsFailure.increment();
            }
        }
    }

    /**
     * This gives a connection from pool. If nothing is in the pool, it creates one and gives back.
     *
     * @return
     * @throws SQLException
     */
    public final Connection getConnection() throws SQLException {
        Connection con = this.getNTestConnection(); // First try
        if (null != con) return con;

        LOG.debug("Trying second time..");
        // if ( null != jdbcRetrySecondConnections) this.jdbcRetrySecondConnections.increment();
        con = this.getNTestConnection(); // Second try
        if (null != con) return con;

        LOG.debug("Trying third time..");
        // if ( null != jdbcRetryThirdConnections) this.jdbcRetryThirdConnections.increment();
        con = this.getNTestConnection(); // Third try
        if (null != con) return con;

        // if ( null != jdbcRetryNoConnections) this.jdbcRetryNoConnections.increment();
        LOG.error("Database is not available");
        throw new SQLException("NO_CONNECTION");
    }

    public final int getActiveConnection() {
        int active = getLiveConnection() - getAvailableConnection();
        if (active < 0) return 0;
        return active;
    }

    public final int getAvailableConnection() {
        return this.availablePool.size();
    }

    public final int getLiveConnection() {
        return this.createdConnection.get() - this.destroyedConnection.get();
    }

    private final synchronized boolean poolReturn(final PoolConnection poolConnection) {
        if (!this.availablePool.contains(poolConnection)) {
            this.availablePool.push(poolConnection);
            this.returnConnection.incrementAndGet();
            // this.jdbcAvailableConnections.increment();
            return true;

        } else {
            return false;
        }
    }

    private final PoolConnection poolGetWrapper() {
        PoolConnection pc = poolGet();
        if (null != pc) {
            // this.jdbcGetPoolConnections.increment();
        }
        return pc;
    }

    private final synchronized PoolConnection poolGet() {
        if (this.availablePool.empty()) return null;

        // this.jdbcAvailableConnections.decrement();
        return this.availablePool.pop();
    }

    public final synchronized void start(final DbConfig dbConfig) {
        if (null == dbConfig) {
            String errMsg = "Null db configuration file provided.";
            LOG.error("{}:{}", errMsg, dbConfig);
            throw new RuntimeException(errMsg);
        }
        this.config = dbConfig;

        /**
         * jdbcCreatedConnections = MetricFactory.getGauge("jdbc_created_connection_count_",
         * config.poolName); jdbcCreateConnectionFailure =
         * MetricFactory.getCounter("jdbc_create_connection_failure_count_", config.poolName);
         *
         * <p>jdbcTestConnection = MetricFactory.getCounter("jdbc_test_connection_count_",
         * config.poolName); jdbcTestConnectionFailure =
         * MetricFactory.getCounter("jdbc_test_connection_failure_count", config.poolName);
         *
         * <p>jdbcCreateConnectionExceeded = MetricFactory.getCounter
         * ("jdbc_create_connection_exceeded", config.poolName);
         *
         * <p>jdbcRetrySecondConnections = MetricFactory.getGauge("jdbc_connect_retry_second_count_",
         * config.poolName); jdbcRetryThirdConnections =
         * MetricFactory.getGauge("jdbc_connect_retry_third_count_", config.poolName);
         * jdbcRetryNoConnections = MetricFactory.getGauge("jdbc_connect_retry_nofound_count_",
         * config.poolName);
         *
         * <p>jdbcGetDirectConnections = MetricFactory.getGauge("jdbc_direct_connection_count",
         * config.poolName); jdbcGetPoolConnections =
         * MetricFactory.getGauge("jdbc_getpool_connection_count", config.poolName);
         *
         * <p>jdbcDestroyedConnections = MetricFactory.getGauge("jdbc_destroyed_connection_count",
         * config.poolName); jdbcDestroyConnectionFailure =
         * MetricFactory.getCounter("jdbc_destroy_connection_failure_count", config.poolName);
         *
         * <p>jdbcReturnedConnections = MetricFactory.getGauge("jdbc_returned_connection_count",
         * config.poolName); jdbcReturnedConnectionsFailure =
         * MetricFactory.getGauge("jdbc_returned_connection_failure_count", config.poolName);
         * jdbcAvailableConnections = MetricFactory.getGauge("jdbc_available_connection_count",
         * config.poolName);
         *
         * <p>jdbcHealthcheckAdd = MetricFactory.getGauge("jdbc_healthcheck_add_count",
         * config.poolName); jdbcHealthcheckRemove =
         * MetricFactory.getGauge("jdbc_healthcheck_remove_count", config.poolName);
         */
        LOG.debug("Starting the database service for {}",this.config.getPoolName());

        try {
            Class.forName(this.config.getDriverClass()).newInstance();
            LOG.debug("Driver instantiated with {}",this.config.getDriverClass());
            this.availablePool.ensureCapacity(this.config.getIdleConnections());
            /**
             * if(jdbcAvailableConnections != null) {
             * jdbcAvailableConnections.increment(availablePool.size()); }
             */
            this.healthCheck();
        } catch (Exception ex) {
            LOG.error("Pool creation issues.", ex);
        }

        this.hcAgent = new HealthCheckAgent();

        LOG.debug(
            "Health check Timer pause/duration in ms = {}/{}"
                ,this.config.getTimeBetweenConnections() ,
                this.config.getHealthCheckDurationMillis());

        AGENT_TIMER.schedule(
            this.hcAgent,
            this.config.getTimeBetweenConnections(),
            this.config.getHealthCheckDurationMillis());
    }

    public final synchronized void stop() {
        LOG.debug("Stoping the database service");
        if (null != this.hcAgent) {
            this.hcAgent.cancel();
            this.hcAgent = null;
        }
    }

    public final synchronized void stop(final boolean releaseConnection) {
        LOG.debug("Stoping the database service");
        if (null != this.hcAgent) {
            this.hcAgent.cancel();
            this.hcAgent = null;
        }

        if (releaseConnection && !this.availablePool.isEmpty()) {

            while (!this.availablePool.isEmpty()) {
                Objects.requireNonNull(this.poolGet()).destroySilently();
            }
        }
    }

    // --------------Health Check Module-------------------
    public final synchronized void healthCheck() {
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                "Connections Available/Total = {}/{}",
                this.getAvailableConnection(),
                this.getActiveConnection());
        }

        if (!this.healthAddConnections()) {
            this.healthRemoveConnections();
        }
        this.healthRefreshConnections();
    }

    private final boolean healthAddConnections() {

        int liveConnection = getLiveConnection();
        if (liveConnection > this.config.getMaxConnections()) return false; // 1

        int shortConnection = this.config.getIdleConnections() - liveConnection;
        if (shortConnection <= 0) return false;

        if (shortConnection > this.config.getIncrementBy())
            shortConnection = this.config.getIncrementBy();

        int numAdded = this.increment(shortConnection);
        // if(jdbcHealthcheckAdd!=null)  jdbcHealthcheckAdd.increment(numAdded);

        LOG.info(
            "Pool : healthAddConnections , after extracting connection ,poolName={}, return shortConnection={},"
                + "createdConnection={},destroyedConnection={},availablePool={}",
            config.getPoolName(),
            shortConnection,
            createdConnection,
            destroyedConnection,
            availablePool.size());

        return true;
    }

    private final void healthRemoveConnections() {

        int availableConnection = getAvailableConnection();
        if (availableConnection == 0) return;

        int liveConnection = getLiveConnection();

        int extraConnection = liveConnection - this.config.getIdleConnections(); // 5 - 3
        if (extraConnection > availableConnection)
            extraConnection = availableConnection; // 2 but available 1

        if (extraConnection <= 0) return; // minimum 1
        // jdbcHealthcheckRemove.increment(extraConnection);

        // Old destroy pool remove everything.
        int destroySize = extraConnection;
        for (int i = 0; i < destroySize; i++) {
            try {
                Connection con = this.getNTestConnection();
                if (con == null) break;
                this.destroyPoolConnection(con);
            } catch (Exception ex) {
                LOG.warn("Error while healthRemoveConnection ", ex);
            }
        }

        LOG.info(
            "Pool : healthRemoveConnections , after extracting connection ,poolName={}, return extraConnection={},"
                + "createdConnection={},destroyedConnection={},availablePool={}",
            config.getPoolName(),
            extraConnection,
            createdConnection,
            destroyedConnection,
            availablePool.size());
    }

    private final void healthRefreshConnections() {
        if (!this.config.isTestConnectionOnIdle()) return;

        int availablePoolT = this.getAvailableConnection();

        if (this.config.isRunTestSql()) {
            this.runTestSqlOnConnections(availablePoolT);
        }
    }

    private final void runTestSqlOnConnections(final int availablePoolT) {
        if (LOG.isDebugEnabled())
            LOG.debug(
                "Testing idle connections from pool : {} , Total connections {}  , using farm.laminar.core.io.sql > {}"
                    ,this.config.getPoolName(),availablePoolT,this.config.getTestSql() );

        int goodConnections = 0;

        for (int i = 0; i < availablePoolT; i++) {
            goodConnections = getGoodConnections(goodConnections);
        }
        if (LOG.isDebugEnabled())
            LOG.debug(
                "Pool : {} , Connection Status Good/Bad = {}/{}",
                this.config.getPoolName(),goodConnections,
                (availablePoolT - goodConnections));
    }

    private int getGoodConnections(int goodConnections) {
        PoolConnection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            connection = (PoolConnection) this.getConnection();
            if (connection != null) {
                stmt = connection.createStatement();
                rs = stmt.executeQuery(this.config.getTestSql());
                rs.close();
                rs = null;
                stmt.close();
                stmt = null;
                connection.close();
                connection = null;
                goodConnections++;
                // jdbcTestConnection.increment();
            }
        } catch (SQLException ex) {
            exceptionInTestingConnection(connection, stmt, rs, ex);
        }
        return goodConnections;
    }

    private void exceptionInTestingConnection(PoolConnection connection, Statement stmt, ResultSet rs,
        SQLException ex) {
        LOG.warn("Exception in testing connections.", ex);
        // if(jdbcTestConnectionFailure != null) { jdbcTestConnectionFailure.increment(); }
        if (null != rs)
            try {
                rs.close();
            } catch (Exception rex) {
            }
        if (null != stmt)
            try {
                stmt.close();
            } catch (Exception sex) {
            }
        if (null != connection) connection.destroySilently();
    }

    private final void destroyPoolConnection(Connection con) {
        try {
            if (con instanceof PoolConnection) {
                PoolConnection poolCon = (PoolConnection) con;
                sleepForDestroyAction();
                poolCon.destroy();
            } else {
                con.close();
            }
            con = null;
            this.destroyedConnection.incrementAndGet();

            // if(jdbcDestroyedConnections != null) { jdbcDestroyedConnections.increment(); }

        } catch (SQLException ex) {
            // jdbcDestroyConnectionFailure.increment();
            LOG.error("Error in cleaning up connection", ex);
        }
    }

    private void sleepForDestroyAction() {
        try {
            Thread.sleep(this.config.getTimeBetweenConnections());
        } catch (InterruptedException ex) {
            LOG.error("Error in sleeping for destroy action", ex);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Pool Connections : [");
        for (PoolConnection poolConnection : this.availablePool) {
            sb.append(poolConnection.hashCode()).append(", ");
        }
        sb.append(']');
        return sb.toString();
    }

    private class HealthCheckAgent extends TimerTask {
        public void run() {
            try {
                healthCheck();
            } catch (Exception e) {
                LOG.error("Error in running health check", e);
            }
        }
    }
}
