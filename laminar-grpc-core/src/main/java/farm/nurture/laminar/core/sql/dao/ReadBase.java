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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public abstract class ReadBase<T> {

    private static final Logger LOG = LoggerFactory.getLogger(ReadBase.class);
    protected Connection con = null;
    protected boolean isTxn = false;
    protected Statement stmt = null;
    protected PreparedStatement prepareStmt = null;
    protected ResultSet rs = null;
    protected int recordsCount = 0;
    protected CallableStatement callableStmt = null;
    protected boolean callableResponse = false;
    String poolName = null;

    public final ReadBase<T> setPoolName(final String poolName) {
        this.poolName = poolName;
        return this;
    }

    /**
     * Returns the appropriate results for the given farm.laminar.core.io.sql statement. The data is
     * stored in generic Object.class instance
     *
     * @param sqlStmt
     * @return List of records
     * @throws SQLException
     */
    public final List<T> execute(final String sqlStmt) throws SQLException {

        boolean sucess = false;
        int size = -1;
        try {
            if (null == this.con) {
                this.con =
                    (null == poolName)
                        ? PoolFactory.getDefaultPool().getConnection()
                        : PoolFactory.getInstance().getPool(poolName, false).getConnection();
            }

            this.prepare(sqlStmt);
            List<T> result = this.populate();
            if (null != result) size = result.size();
            sucess = true;
            return result;

        } catch (SQLException ex) {
            LOG.error("Unable to execute:" + sqlStmt, ex);
            throw (ex);

        } finally {
            this.release();
            if (size != -1) {}
        }
    }

    /**
     * This is with prepare statement.
     *
     * @param sqlStmt
     * @return
     * @throws SQLException
     */
    public final List<T> execute(final String sqlStmt, final Object[] columns) throws SQLException {

        if (null == columns) return execute(sqlStmt);

        boolean sucess = false;
        int size = -1;
        try {
            if (null == this.con) {
                this.con =
                    (null == poolName)
                        ? PoolFactory.getDefaultPool().getConnection()
                        : PoolFactory.getInstance().getPool(poolName, false).getConnection();
            }
            this.prepare(sqlStmt, columns);
            List<T> result = this.populate();
            if (null != result) {
                size = result.size();
                sucess = true;
            }
            return result;
        } catch (SQLException ex) {
            LOG.error("Unable to execute:" + sqlStmt, ex);
            throw (ex);
        } finally {
            this.release();
            if (size != -1) {}
        }
    }

    /**
     * This is with prepare statement.
     *
     * @param sqlStmt
     * @return
     * @throws SQLException
     */
    public final List<T> execute(final String sqlStmt, final List<Object> columns)
        throws SQLException {

        boolean sucess = false;
        int size = -1;

        try {
            if (null == this.con) {
                this.con =
                    (null == poolName)
                        ? PoolFactory.getDefaultPool().getConnection()
                        : PoolFactory.getInstance().getPool(poolName, false).getConnection();
            }
            this.prepare(sqlStmt, columns);
            List<T> result = this.populate();
            if (null != result) size = result.size();
            sucess = true;
            return result;

        } catch (SQLException ex) {
            LOG.error("Unable to execute :" + sqlStmt, ex);
            throw (ex);
        } finally {
            this.release();
            if (size != -1) {}
        }
    }

    /**
     * This is with prepare statement to get a record by unique key.
     *
     * @param sqlStmt
     * @return
     * @throws SQLException
     */
    public final T selectByPrimaryKey(final String sqlStmt, final Object id) throws SQLException {
        boolean sucess = false;

        try {
            if (null == this.con) {
                this.con =
                    (null == poolName)
                        ? PoolFactory.getDefaultPool().getConnection()
                        : PoolFactory.getInstance().getPool(poolName, false).getConnection();
            }
            this.prepareForPrimaryKey(sqlStmt, id);
            T result = this.getFirstRow();
            sucess = true;
            return result;

        } catch (SQLException ex) {
            String key = (id == null) ? "null" : id.toString();
            LOG.error("Unable to select by PK(" + key + ")" + sqlStmt, ex);
            throw (ex);
        } finally {
            this.release();
        }
    }

    public final T selectByUniqueKey(final String sqlStmt, final Object[] columns)
        throws SQLException {
        boolean sucess = false;

        try {
            if (null == this.con) {
                this.con =
                    (null == poolName)
                        ? PoolFactory.getDefaultPool().getConnection()
                        : PoolFactory.getInstance().getPool(poolName, false).getConnection();
            }
            this.prepare(sqlStmt, columns);
            T result = this.getFirstRow();
            sucess = true;
            return result;
        } catch (SQLException ex) {
            LOG.error("Unable to select by unique key:" + sqlStmt, ex);
            throw (ex);
        } finally {
            this.release();
        }
    }

    protected final void prepare(final String sqlStmt) throws SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sql={}",sqlStmt);
        }
        this.stmt = this.con.createStatement();
        this.rs = this.stmt.executeQuery(sqlStmt);
    }

    protected final void prepareForPrimaryKey(final String sqlStmt, final Object id)
        throws SQLException {
        this.prepareStmt = this.con.prepareStatement(sqlStmt);
        this.prepareStmt.setObject(1, id);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Sql statement\n {}\n id={}",sqlStmt,id);
        }
        this.rs = this.prepareStmt.executeQuery();
    }

    protected final void prepare(final String sqlStmt, final Object[] columns) throws SQLException {
        this.prepareStmt = this.con.prepareStatement(sqlStmt);
        if (null != columns) {
            for (int i = 1; i <= columns.length; i++) {
                this.prepareStmt.setObject(i, columns[i - 1]);
            }
        }

        this.rs = this.prepareStmt.executeQuery();
    }

    protected final void prepare(String sqlStmt, final List<Object> columns) throws SQLException {

        if (DbConfig.isDefaultPrepareStmtSupport()) {

            this.prepareStmt = this.con.prepareStatement(sqlStmt);
            int colsT = columns.size();
            for (int i = 1; i <= colsT; i++) {
                this.prepareStmt.setObject(i, columns.get(i - 1));
            }
            this.rs = this.prepareStmt.executeQuery();

        } else {

            StringBuilder queryBuilder = new StringBuilder();
            for (Object object : columns) {
                queryBuilder.append(sqlStmt.substring(0, sqlStmt.indexOf('?')));
                queryBuilder.append(object.toString());
                sqlStmt = sqlStmt.substring(sqlStmt.indexOf('?') + 1);
            }
            queryBuilder.append(sqlStmt);
            this.rs = this.con.createStatement().executeQuery(queryBuilder.toString());
        }
    }

    /** Close everything... */
    protected final void release() {

        AutoCloseable[] resources =
            (isTxn)
                ? new AutoCloseable[] {this.rs, this.prepareStmt, this.stmt}
                : new AutoCloseable[] {this.rs, this.prepareStmt, this.stmt, this.con};

        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                    resource = null;
                } catch (Exception ex) {
                    LOG.warn("Error while closing ", ex);
                }
            }
        }
    }

    protected abstract List<T> populate() throws SQLException;

    protected abstract T getFirstRow() throws SQLException;

    protected abstract int getRecordsCount();
}
