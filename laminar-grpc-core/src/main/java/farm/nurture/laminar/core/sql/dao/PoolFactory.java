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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PoolFactory {
    private static final Logger LOG = LoggerFactory.getLogger(PoolFactory.class);
    private static PoolFactory instance = null;
    private Map<String, IPool> poolMap;
    private IPool defaultPool;
    private static final farm.nurture.infra.util.Logger logger = farm.nurture.infra.util.LoggerFactory.getLogger(PoolFactory.class);


    private PoolFactory() {
        this.poolMap = new HashMap<>();
    }

    public static PoolFactory getInstance() {
        if (null != instance) return instance;
        synchronized (PoolFactory.class.getName()) {
            if (null != instance) return instance;
            instance = new PoolFactory();
        }
        return instance;
    }

    public static IPool getDefaultPool() {
        return PoolFactory.getInstance().defaultPool;
    }

    public IPool getPool(String poolName, boolean isGCS) {
        if (this.poolMap.containsKey(poolName)) {
            LOG.debug("Pool name  found :{}", poolName);
            return this.poolMap.get(poolName);
        } else {
            LOG.warn("Pool name not found :{} . Switching to default pool", poolName);
            return PoolFactory.getInstance().defaultPool;
        }
    }

    public void returnConnection(Connection conn) {
        if (conn instanceof PoolConnection) this.returnConnection((PoolConnection) conn);
    }

    public void returnConnection(PoolConnection poolConn) {
        if (null == poolConn) return;
        try {

            if (this.poolMap.containsKey(poolConn.getPoolName())) {
                if (LOG.isDebugEnabled())
                    LOG.debug(
                        "Returning connection {} to pool {}",poolConn.hashCode(),poolConn.getPoolName());
                this.poolMap.get(poolConn.getPoolName()).returnConnection(poolConn);
            } else {
                LOG.warn("Joombie connection to pool {}",poolConn.getPoolName());
            }
        } catch (SQLException e) {
            LOG.error("Unable to return Pool Connection for pool type: " + poolConn.getPoolName(), e);
        }
    }

    public boolean setup(String configXml) {
        String copyConfigXml = "Config XML = \n" + configXml;
        String copyConfigXmlSecure =
            copyConfigXml.replaceAll("\\<password\\>.*\\<\\/password\\>", "<password>-</password>");
        logger.info("copyConfigXmlSecure = {}",copyConfigXmlSecure);
        try {
            List<DbConfig> dbcL = new JdbcConfigLoader().getConfiguration(configXml);

            if (dbcL != null && !dbcL.isEmpty()) {
                for (DbConfig config : dbcL) {
                    // System.err.println("DBCONFIG=" + config.toString());
                    IPool pool = this.startHikariPool(config);
                    // Check the userid and password are all good.
                    Connection conn = pool.getConnection();
                    if (null != conn) pool.returnConnection(conn);
                    logger.info("config.defaultPool = {}",config.isPreparedStmt());
                    if (config.isDefaultPool())
                        DbConfig.setDefaultPrepareStmtSupport(config.isPreparedStmt());
                }
                return true;
            }
        } catch (Exception e) {
            LOG.error("Error in starting database service with config: " + configXml, e);
        }
        return false;
    }

    public boolean setup(DbConfig config) {
        try {
            this.startHikariPool(config);
        } catch (Exception e) {
            LOG.error("Error in starting database service with config: " + config, e);
            return false;
        }
        return true;
    }

    public boolean stop() {
        if (this.poolMap == null || this.poolMap.isEmpty()) return true;
        for (IPool pool : this.poolMap.values()) {
            pool.stop();
        }
        this.poolMap.clear();
        this.defaultPool = null;
        return true;
    }

    public final boolean stop(final boolean releaseConnection, final String poolName) {
        if (LOG.isDebugEnabled())
            LOG.debug(
                "PoolFactory stopping pool for [ {} ] and releaseConnection is [ {} ]",
                poolName,
                releaseConnection);

        if (this.poolMap == null || this.poolMap.isEmpty()) return true;
        IPool pool = this.poolMap.get(poolName);
        if (null == pool) return false;

        pool.stop(releaseConnection);
        this.poolMap.remove(poolName);

        return true;
    }

    public final boolean contains(final String poolName) {
        if (this.poolMap == null || this.poolMap.isEmpty()) return false;
        return this.poolMap.containsKey(poolName);
    }

    private synchronized IPool startPool(DbConfig config) {
        LOG.debug("Initializing DB Pool - {}",config.getPoolName());
        if (this.poolMap.containsKey(config.getPoolName())) {
            LOG.debug("Pool is already started Ignoring - {}",config.getPoolName());
            return this.poolMap.get(config.getPoolName());
        }

        IPool pool = new Pool(config.getPoolName());
        this.poolMap.put(config.getPoolName(), pool);
        pool.start(config);
        if (this.defaultPool == null && config.isDefaultPool()) this.defaultPool = pool;
        LOG.debug("Created Pools are - {}", this);
        return pool;
    }

    private synchronized IPool startHikariPool(DbConfig config) {
        LOG.debug("Initializing DB Pool - {}",config.getPoolName());
        if (this.poolMap.containsKey(config.getPoolName())) {
            LOG.debug("Pool is already started Ignoring - {}",config.getPoolName());
            return this.poolMap.get(config.getPoolName());
        }

        IPool pool = new HikariPool(config.getPoolName());
        this.poolMap.put(config.getPoolName(), pool);
        pool.start(config);
        if (this.defaultPool == null && config.isDefaultPool()) this.defaultPool = pool;
        LOG.debug("Created Pools are - {}", this);
        return pool;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, IPool> poolE : this.poolMap.entrySet()) {
            String poolName = poolE.getKey();
            IPool pool = poolE.getValue();
            sb.append(poolName)
                .append("=> Pool:")
                .append(pool.hashCode())
                .append("=")
                .append(pool.toString())
                .append(", ");
        }
        return sb.toString();
    }
}
