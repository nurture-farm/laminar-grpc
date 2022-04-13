package farm.nurture.laminar.core.sql.dao;

import static farm.nurture.laminar.core.sql.dao.Constants.RS_NOT_INITIALIZED_MSG;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by Hansraj (hansraj.choudhary@) on 29/05/17. */
public class WriteBaseWithLock {

    private static final Logger LOG = LoggerFactory.getLogger(WriteBaseWithLock.class);
    static volatile int transactions = 0;
    static Exception lastBegin = null;
    protected Connection con = null;
    protected PreparedStatement prepareStmt = null;
    protected ResultSet rs = null;
    protected int recordsTouched = -1;
    protected boolean isInTransaction = false;
    protected int recordsCount = 0;
    private IPool conPool = null;
    private String poolName = null;

    public WriteBaseWithLock() {
        conPool = PoolFactory.getDefaultPool();
    }

    public WriteBaseWithLock(String poolName) {
        this.poolName = poolName;
        conPool = PoolFactory.getInstance().getPool(poolName, false);
    }

    // Only time to create a connection if not there.
    public void beginTransaction() throws SQLException {

        if (LOG.isDebugEnabled()) {
            try {
                throw new IOException("Transaction Count: " + transactions);
            } catch (Exception ex) {
                if (transactions > 0 && lastBegin != null)
                    LOG.warn("Transaction leaked, possible table locking", lastBegin);
                lastBegin = ex;
            }
            transactions++;
        }

        /**
         * If already an open temporary transaction then set auto commit false. else create a connection
         * with auto commit false
         */
        if (this.isInTransaction) {
            this.con.setAutoCommit(false);
        } else {
            this.createConnection(false);
            this.isInTransaction = true;
            if (LOG.isDebugEnabled())
                LOG.debug("Beginning Transaction {} inTransaction = {}", this.hashCode(), transactions);
        }
    }

    // End the transaction now.
    public void commitTransaction(boolean isOpenTempTransaction) throws SQLException {
        if (LOG.isDebugEnabled()) transactions--;

        /**
         * If already an open temporary transaction then isOpenTempTransaction = true. else
         * isOpenTempTransaction = false
         */
        if (this.isInTransaction) {
            this.isInTransaction = isOpenTempTransaction;
            this.con.commit();
            this.releaseResources();
            if (LOG.isDebugEnabled())
                LOG.debug("Committed Transaction {} intransaction = {}", this.hashCode(), transactions);
        }
    }

    public void commitTransaction() throws SQLException {
        commitTransaction(false);
    }

    // Rollback the transaction now.
    public void rollbackTransaction() {
        if (LOG.isDebugEnabled() && transactions > 0) transactions--;
        if (this.isInTransaction) {
            this.isInTransaction = false;
            if (LOG.isDebugEnabled()) LOG.debug("Rolling back the connection {}", this.hashCode());
            try {
                this.con.rollback();

            } catch (SQLException ex) {
                LOG.error("Rollback failed", ex);

            } finally {
                this.releaseResources();
            }
        }
    }

    // If the transaction is not there, create it.
    protected void createConnection(boolean autoCommit) throws SQLException {
        if (!this.isInTransaction) {
            this.con = conPool.getConnection();
            this.con.setAutoCommit(autoCommit);
        }
    }

    // If the transaction is not there, release connection.
    protected void releaseResources() {
        if (this.isInTransaction) {
            if (LOG.isDebugEnabled())
                LOG.debug("Releasing statement and resultset only {}", this.hashCode());
            this.release(true, true, false);
        } else {
            if (LOG.isDebugEnabled())
                LOG.debug("Releasing connection, statement and resultset {}", this.hashCode());
            this.release(true, true, true);
        }
    }

    // The insert statement
    public Integer insert(String query, Object[] columns) throws SQLException {

        try {
            this.createConnection(true);
            this.prepareStmt = this.con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            for (int i = 1; i <= columns.length; i++) {
                if (LOG.isDebugEnabled()) LOG.debug("{}-{}", i, columns[i - 1]);
                this.prepareStmt.setObject(i, columns[i - 1]);
            }
            this.recordsTouched = prepareStmt.executeUpdate();
            if (LOG.isDebugEnabled())
                LOG.info("Records Touched= {}-{}", this.recordsTouched, this.hashCode());

            Integer autoIncKey = new Integer(-1);
            rs = prepareStmt.getGeneratedKeys();
            if (rs.next()) {
                autoIncKey = rs.getInt(1);
            } else {
                throw new SQLException("NO_KEY_GENERATED");
            }
            return autoIncKey;
        } catch (SQLException ex) {
            logException(query, columns, ex);
            throw ex;
        } finally {
            this.releaseResources();
        }
    }

    // The update statement
    public int execute(String query, Object[] columns) throws SQLException {
        try {
            this.createConnection(true);
            this.prepareStmt = this.con.prepareStatement(query);

            for (int i = 1; i <= columns.length; i++) {
                this.prepareStmt.setObject(i, columns[i - 1]);
            }
            this.recordsTouched = prepareStmt.executeUpdate();
            return this.recordsTouched;

        } catch (SQLException ex) {

            logException(query, columns, ex);
            throw ex;

        } finally {
            this.releaseResources();
        }
    }

    private void logException(String query, Object[] columns, SQLException ex) {
        StringBuilder sb = new StringBuilder(256);
        if (null != columns) {
            for (Object column : columns) {
                sb.append('[').append(column).append(']');
            }
        }
        LOG.error("Query = " + query.replace('\n', ' ') + "\nColums = " + sb.toString(), ex);
        ex.printStackTrace();
    }

    protected void release(boolean closeRs, boolean closeStmt, boolean closeCon) {
        if (this.rs != null && closeRs) {
            try {
                this.rs.close();
                this.rs = null;
            } catch (SQLException ex) {
                LOG.error("Release Failed, Possible memory leak", ex);
            }
        }
        if (this.prepareStmt != null && closeStmt) {
            try {
                this.prepareStmt.close();
                this.prepareStmt = null;
            } catch (SQLException ex) {
                LOG.error("Unable to release prepared statement", ex);
            }
        }

        if (this.con != null && closeCon) {
            try {
                this.con.close();
                this.con = null;
            } catch (SQLException ex) {
                LOG.error("Unable to release prepared connection", ex);
            }
        }
    }

    /**
     * To execute a batch of updates or inserts onto a single table.
     *
     * @param query - For all records to execute with
     * @param records - List of column values for each record. List of Object[] objects
     * @return int[] - rows updated per record
     * @throws SQLException
     */
    public int[] executeBatch(String query, List<Object[]> records) throws SQLException {
        boolean transactionByBatch = !(this.isInTransaction);

        if (null == records) {
            if (transactionByBatch) this.commitTransaction();
            return new int[] {};
        } else {
            if (LOG.isDebugEnabled()) LOG.debug("Number of records to executeBatch : {}", records.size());
        }

        this.startBatch(query, transactionByBatch);
        for (Object[] currRecord : records) {
            this.addToBatch(currRecord);
        }
        return this.executeBatch(transactionByBatch);
    }

    private void startBatch(String query, boolean transactionByBatch) throws SQLException {
        try {
            if (transactionByBatch) this.beginTransaction();
            this.createConnection(true);
            this.prepareStmt = this.con.prepareStatement(query);
        } catch (SQLException ex) {
            LOG.error("Unable to start batch", ex);
            throw ex;
        }
    }

    private void addToBatch(Object[] columns) throws SQLException {
        if (this.prepareStmt == null) {
            throw new SQLException("Illegal call. startBatch has to be done before addToBatch");
        }

        int columnLength = columns.length;
        for (int i = 1; i <= columnLength; i++) {
            this.prepareStmt.setObject(i, columns[i - 1]);
        }
        this.prepareStmt.addBatch();
    }

    private int[] executeBatch(boolean transactionByBatch) throws SQLException {
        if (this.prepareStmt == null) {
            throw new SQLException("Illegal call. startBatch has to be done before addToBatch");
        }
        try {
            int[] results = this.prepareStmt.executeBatch();
            if (transactionByBatch) this.commitTransaction();
            return results;
        } catch (SQLException ex) {
            LOG.error("Unable to execute batch", ex);
            if (transactionByBatch) this.rollbackTransaction();
            throw ex;
        } finally {
            this.releaseResources();
        }
    }

    public final List<Object> executeSelectForUpdate(final String sqlStmt, final Object[] columns)
        throws SQLException {
        try {
            if (null == this.con) {
                this.con =
                    (null == poolName)
                        ? PoolFactory.getDefaultPool().getConnection()
                        : PoolFactory.getInstance().getPool(poolName, false).getConnection();
            }

            this.prepare(sqlStmt, columns);
            return this.populate();
        } catch (SQLException ex) {
            LOG.error("Unable to execute:" + sqlStmt, ex);
            throw (ex);
        } finally {
            // Should not release resources otherwise transaction might be in bad state
            this.release(true, true, false);
        }
    }

    protected final void prepare(final String sqlStmt, final Object[] columns) throws SQLException {
        this.prepareStmt = this.con.prepareStatement(sqlStmt);
        for (int i = 1; i <= columns.length; i++) {
            this.prepareStmt.setObject(i, columns[i - 1]);
        }
        this.rs = this.prepareStmt.executeQuery();
    }

    protected Object getFirstRow() throws SQLException {
        if (null == this.rs) {
            LOG.warn(RS_NOT_INITIALIZED_MSG);
            throw new SQLException(RS_NOT_INITIALIZED_MSG);
        }

        if (this.rs.next()) {
            return rs.getObject(1);
        }

        return null;
    }

    protected List<Object> populate() throws SQLException {
        if (null == this.rs) {
            LOG.warn(RS_NOT_INITIALIZED_MSG);
            throw new SQLException(RS_NOT_INITIALIZED_MSG);
        }

        List<Object> records = new ArrayList<>();
        while (this.rs.next()) {
            recordsCount++;
            records.add(rs.getObject(1));
        }
        return records;
    }
}
