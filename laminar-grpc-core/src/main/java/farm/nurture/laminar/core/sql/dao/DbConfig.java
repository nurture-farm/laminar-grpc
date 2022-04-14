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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DbConfig {

    private static boolean DEFAULT_PREPARE_STMT_SUPPORT = true;

    private int idleConnections = 5;
    private int maxConnections = 100;
    private int incrementBy = 5;
    private boolean testConnectionOnBorrow = false;
    private boolean testConnectionOnIdle = true;
    private boolean testConnectionOnReturn = false;
    private long healthCheckDurationMillis = 29 * 60 * 1000;
    private String testSql = "select 1";
    private boolean runTestSql = true;
    private int timeBetweenConnections = 50;
    private int isolationLevel = Connection.TRANSACTION_READ_COMMITTED;
    private boolean allowMultiQueries = false;
    private String poolName;
    private boolean defaultPool = true;
    private boolean isGCS = false;
    private boolean preparedStmt = true;

    private long connectionTimeoutMillis = 30000l;
    private long validationTimeoutMillis = 5000l;

    private String driverClass;
    private String connectionUrl;
    private String login;
    private String password;

    public static boolean isDefaultPrepareStmtSupport() {
        return DEFAULT_PREPARE_STMT_SUPPORT;
    }

    public static void setDefaultPrepareStmtSupport(boolean defaultPrepareStmtSupport) {
        DEFAULT_PREPARE_STMT_SUPPORT = defaultPrepareStmtSupport;
    }

    @Override
    public String toString() {
        return "DbConfig{"
            + "idleConnections="
            + getIdleConnections()
            + ", maxConnections="
            + getMaxConnections()
            + ", incrementBy="
            + getIncrementBy()
            + ", testConnectionOnBorrow="
            + isTestConnectionOnBorrow()
            + ", testConnectionOnIdle="
            + isTestConnectionOnIdle()
            + ", testConnectionOnReturn="
            + isTestConnectionOnReturn()
            + ", healthCheckDurationMillis="
            + getHealthCheckDurationMillis()
            + ", testSql='"
            + getTestSql()
            + '\''
            + ", runTestSql="
            + isRunTestSql()
            + ", timeBetweenConnections="
            + getTimeBetweenConnections()
            + ", isolationLevel="
            + getIsolationLevel()
            + ", allowMultiQueries="
            + isAllowMultiQueries()
            + ", poolName='"
            + getPoolName()
            + '\''
            + ", defaultPool="
            + isDefaultPool()
            + ", isGCS="
            + isGCS()
            + ", preparedStmt="
            + isPreparedStmt()
            + ", connectionTimeoutMillis="
            + getConnectionTimeoutMillis()
            + ", validationTimeoutMillis="
            + getValidationTimeoutMillis()
            + ", driverClass='"
            + getDriverClass()
            + '\''
            + ", connectionUrl='"
            + getConnectionUrl()
            + '\''
            + ", login='"
            + getLogin()
            + '\''
            + '}';
    }

  /*
  public void encrypt() throws Exception
  {
  	this.password = PasswordEncryptor.encrypt(this.password);
  }

  public void decrypt() throws Exception
  {
  	this.password = PasswordEncryptor.decrypt(this.password);
  }

  */

}
