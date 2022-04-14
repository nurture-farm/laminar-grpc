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

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import lombok.Getter;
import lombok.Setter;

/**
 * import java.farm.laminar.core.io.sql.Struct; import java.farm.laminar.core.io.sql.Clob; import
 * java.util.Properties; import java.farm.laminar.core.io.sql.NClob; import
 * java.farm.laminar.core.io.sql.SQLClientInfoException; import
 * java.farm.laminar.core.io.sql.SQLXML;
 */
@Getter
@Setter
public final class PoolConnection implements Connection {

    private Connection baseCon = null;
    private boolean isDirty = false;
    private String poolName;

    public PoolConnection(final Connection con, final String poolType) {
        this.setBaseCon(con);
        this.setPoolName(poolType);
        if (this.getBaseCon() == null) throw new IllegalStateException("NO_CONNECTION");
    }

    public final void clearWarnings() throws SQLException {
        this.getBaseCon().clearWarnings();
    }

    public final void close() throws SQLException {
        this.setDirty(false);
        PoolFactory.getInstance().returnConnection(this);
    }

    //	public final boolean isDirty() {
    //		return this.isDirty;
    //	}

    public final void destroy() throws SQLException {
        if (isDirty()) throw new SQLException("BUSY_CONNECTION");
        if (this.getBaseCon() == null) throw new SQLException("NULL_BASE_CONNECTION");
        if (!this.getBaseCon().isClosed()) this.getBaseCon().close();
        this.setBaseCon(null);
    }

    public final void destroySilently() {
        if (null == this.getBaseCon()) return;

        try {
            if (this.getBaseCon().isClosed()) return;
        } catch (SQLException e1) {
        }

        try {
            this.getBaseCon().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public final void commit() throws SQLException {
        this.getBaseCon().commit();
    }

    public final Statement createStatement() throws SQLException {
        this.setDirty(true);
        return this.getBaseCon().createStatement();
    }

    public final Statement createStatement(final int resultSetType, final int resultSetConcurrency)
        throws SQLException {

        this.setDirty(true);
        return this.getBaseCon().createStatement(resultSetType, resultSetConcurrency);
    }

    public final Statement createStatement(
        final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability)
        throws SQLException {

        this.setDirty(true);
        return this.getBaseCon()
            .createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public final boolean getAutoCommit() throws SQLException {
        return this.getBaseCon().getAutoCommit();
    }

    public final void setAutoCommit(boolean autoCommit) throws SQLException {
        this.getBaseCon().setAutoCommit(autoCommit);
    }

    public final String getCatalog() throws SQLException {
        return this.getBaseCon().getCatalog();
    }

    public final void setCatalog(String catalog) throws SQLException {
        this.getBaseCon().setCatalog(catalog);
    }

    public final int getHoldability() throws SQLException {
        return this.getBaseCon().getHoldability();
    }

    public final void setHoldability(int holdability) throws SQLException {
        this.getBaseCon().setHoldability(holdability);
    }

    public final DatabaseMetaData getMetaData() throws SQLException {
        return this.getBaseCon().getMetaData();
    }

    public final int getTransactionIsolation() throws SQLException {
        return this.getBaseCon().getTransactionIsolation();
    }

    public final void setTransactionIsolation(int level) throws SQLException {
        this.getBaseCon().setTransactionIsolation(level);
    }

    public final Map<String, Class<?>> getTypeMap() throws SQLException {
        return this.getBaseCon().getTypeMap();
    }

    public final void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        this.getBaseCon().setTypeMap(map);
    }

    public final SQLWarning getWarnings() throws SQLException {
        return this.getBaseCon().getWarnings();
    }

    public final boolean isClosed() throws SQLException {
        return this.getBaseCon().isClosed();
    }

    public final boolean isReadOnly() throws SQLException {
        return this.getBaseCon().isReadOnly();
    }

    public final void setReadOnly(boolean readOnly) throws SQLException {
        this.getBaseCon().setReadOnly(readOnly);
    }

    public final String nativeSQL(final String sql) throws SQLException {
        return this.getBaseCon().nativeSQL(sql);
    }

    public final CallableStatement prepareCall(final String sql) throws SQLException {
        return this.getBaseCon().prepareCall(sql);
    }

    public final CallableStatement prepareCall(
        final String sql, final int resultSetType, final int resultSetConcurrency)
        throws SQLException {

        this.setDirty(true);
        return this.getBaseCon().prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    public final CallableStatement prepareCall(
        final String sql,
        final int resultSetType,
        final int resultSetConcurrency,
        final int resultSetHoldability)
        throws SQLException {

        this.setDirty(true);
        return this.getBaseCon()
            .prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public final PreparedStatement prepareStatement(final String sql) throws SQLException {

        this.setDirty(true);
        return this.getBaseCon().prepareStatement(sql);
    }

    public final PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys)
        throws SQLException {

        this.setDirty(true);
        return this.getBaseCon().prepareStatement(sql, autoGeneratedKeys);
    }

    public final PreparedStatement prepareStatement(final String sql, final int[] columnIndexes)
        throws SQLException {

        this.setDirty(true);
        return this.getBaseCon().prepareStatement(sql, columnIndexes);
    }

    public final PreparedStatement prepareStatement(final String sql, final String[] columnNames)
        throws SQLException {

        this.setDirty(true);
        return this.getBaseCon().prepareStatement(sql, columnNames);
    }

    public final PreparedStatement prepareStatement(
        final String sql, final int resultSetType, final int resultSetConcurrency)
        throws SQLException {

        this.setDirty(true);
        return this.getBaseCon().prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    public final PreparedStatement prepareStatement(
        String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
        throws SQLException {

        this.setDirty(true);
        return this.getBaseCon()
            .prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public final void releaseSavepoint(Savepoint savepoint) throws SQLException {
        this.getBaseCon().releaseSavepoint(savepoint);
    }

    public final void rollback() throws SQLException {
        this.getBaseCon().rollback();
    }

    public final void rollback(Savepoint savepoint) throws SQLException {
        this.getBaseCon().rollback(savepoint);
    }

    public final Savepoint setSavepoint() throws SQLException {
        return this.getBaseCon().setSavepoint();
    }

    public final Savepoint setSavepoint(String name) throws SQLException {
        return this.getBaseCon().setSavepoint(name);
    }

    public final Array createArrayOf(String arg0, Object[] arg1) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    public final Blob createBlob() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    public final Clob createClob() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    public final NClob createNClob() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    public final SQLXML createSQLXML() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    public final Struct createStruct(String arg0, Object[] arg1) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    public final Properties getClientInfo() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    public final void setClientInfo(Properties arg0) throws SQLClientInfoException {
        // TODO Auto-generated method stub

    }

    public final String getClientInfo(String arg0) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    public final boolean isValid(int arg0) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    public final void setClientInfo(String arg0, String arg1) throws SQLClientInfoException {
        // TODO Auto-generated method stub

    }

    public final boolean isWrapperFor(Class<?> arg0) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    public final <T> T unwrap(Class<T> arg0) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final void abort(Executor arg0) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public final int getNetworkTimeout() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public final String getSchema() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final void setSchema(String arg0) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setNetworkTimeout(Executor arg0, int arg1) throws SQLException {
        // TODO Auto-generated method stub

    }

  /*
  public final Array createArrayOf(String arg0, Object[] arg1) throws SQLException {
  	return this.baseCon.createArrayOf(arg0, arg1);
  }

  public Blob createBlob() throws SQLException {
  	return this.baseCon.createBlob();
  }

  public Clob createClob() throws SQLException {
  	return this.baseCon.createClob();
  }

  public NClob createNClob() throws SQLException {
  	return this.baseCon.createNClob();
  }

  public SQLXML createSQLXML() throws SQLException {
  	return this.baseCon.createSQLXML();
  }

  public Struct createStruct(String arg0, Object[] arg1) throws SQLException {
  	return this.baseCon.createStruct(arg0,arg1);
  }

  public Properties getClientInfo() throws SQLException {
  	return this.baseCon.getClientInfo();
  }

  public String getClientInfo(String arg0) throws SQLException {
  	return this.baseCon.getClientInfo(arg0);
  }

  public boolean isValid(int arg0) throws SQLException {
  	return this.baseCon.isValid(arg0);
  }

  public void setClientInfo(Properties arg0) throws SQLClientInfoException {
  	this.baseCon.setClientInfo(arg0);
  }

  public void setClientInfo(String arg0, String arg1) throws SQLClientInfoException {
  	this.baseCon.setClientInfo(arg0, arg1);
  }

  public boolean isWrapperFor(Class<?> arg0) throws SQLException {
  	return this.baseCon.isWrapperFor(arg0);
  }

  public <T> T unwrap(Class<T> arg0) throws SQLException {
  	return this.baseCon.unwrap(arg0);
  }
  */
}
