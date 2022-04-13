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
package farm.nurture.laminar.generator;

import static farm.nurture.laminar.core.sql.dao.Constants.RS_NOT_INITIALIZED_MSG;
import static farm.nurture.laminar.generator.Constants.BIGINT;
import static farm.nurture.laminar.generator.Constants.BOOL;
import static farm.nurture.laminar.generator.Constants.BOOLEAN;
import static farm.nurture.laminar.generator.Constants.DATETIME;
import static farm.nurture.laminar.generator.Constants.ENUM;
import static farm.nurture.laminar.generator.Constants.FLOAT64;
import static farm.nurture.laminar.generator.Constants.INT;
import static farm.nurture.laminar.generator.Constants.INT32;
import static farm.nurture.laminar.generator.Constants.INT64;
import static farm.nurture.laminar.generator.Constants.JAVA_MATH_BIG_DECIMAL;
import static farm.nurture.laminar.generator.Constants.JAVA_SQL_DATE;
import static farm.nurture.laminar.generator.Constants.JAVA_SQL_TIME;
import static farm.nurture.laminar.generator.Constants.JAVA_SQL_TIMESTAMP;
import static farm.nurture.laminar.generator.Constants.JAVA_TYPE_DOUBLE;
import static farm.nurture.laminar.generator.Constants.JAVA_TYPE_INTEGER;
import static farm.nurture.laminar.generator.Constants.JAVA_TYPE_LONG;
import static farm.nurture.laminar.generator.Constants.JAVA_TYPE_STRING;
import static farm.nurture.laminar.generator.Constants.PROTO_TYPE_BYTES;
import static farm.nurture.laminar.generator.Constants.PROTO_TYPE_DOUBLE;
import static farm.nurture.laminar.generator.Constants.PROTO_TYPE_REPEATED;
import static farm.nurture.laminar.generator.Constants.PROTO_TYPE_TIMESTAMP;
import static farm.nurture.laminar.generator.Constants.STRING;
import static farm.nurture.laminar.generator.Constants.TIMESTAMP;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import farm.nurture.infra.util.StringUtils;
import farm.nurture.laminar.core.sql.dao.ReadBase;
import farm.nurture.laminar.core.util.CaseUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DatabaseReader extends ReadBase<FieldDetail> {

    private String docName = null;
    private PrintWriter out = null;
    private boolean notMetaAnalyzed = true;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseReader.class);

    @Override
    protected List<FieldDetail> populate() throws SQLException {
        if (this.rs == null) {
            throw new SQLException(RS_NOT_INITIALIZED_MSG);
        }

        List<FieldDetail> result = new ArrayList<>();
        if (notMetaAnalyzed) {
            notMetaAnalyzed = false;

            try {
                ResultSetMetaData md = rs.getMetaData();
                int totalCol = md.getColumnCount();
                String[] cols = new String[totalCol];
                String[] types = new String[totalCol];

                for (int i = 0; i < totalCol; i++) {
                    cols[i] = md.getColumnLabel(i + 1);
                    types[i] = md.getColumnTypeName(i + 1);
                    FieldDetail gRpcType = deriveProtoDataType(cols[i], types[i], md.getTableName(i + 1));
                    result.add(gRpcType);
                }

            } catch (IOException ex) {
                throw new SQLException(ex);
            }
        }
        return result;
    }

    @Override
    protected FieldDetail getFirstRow() throws SQLException {
        return null;
    }

    @Override
    protected int getRecordsCount() {
        return recordsCount;
    }

    public FieldDetail deriveProtoDataType(String variableName, String dataTypeCode, String tableName)
        throws IOException {

        logger.info("dataTypeCode:{}\t{}",variableName,dataTypeCode);
        FieldDetail variableType = null;

        if (dataTypeCode != null & dataTypeCode.toLowerCase().startsWith("enum(")) {
            return processEnum(variableName, dataTypeCode, tableName);
        }

        switch (dataTypeCode) {
            case "GEOMETRY":
            case "MEDIUMTEXT":
            case "LONGTEXT":
            case "TEXT":
            case "VARCHAR":
            case "LONGVARCHAR":
            case "CHAR":
            case "JSON":
                variableType = new FieldDetail(variableName, STRING, JAVA_TYPE_STRING, STRING);
                break;

            case "BIT":
            case "TINYINT(1)":
                variableType = new FieldDetail(variableName, BOOL, BOOLEAN, BOOL);
                break;

            case "SMALLINT":
            case "TINYINT":
            case "TINYINT(30)":
            case INT:
                variableType = new FieldDetail(variableName, INT32, JAVA_TYPE_INTEGER, INT32);
                break;

            //			case "FLOAT":
            //				variableType = new FieldDetail(variableName, "float", "Float", "float32" );
            //				break;

            case BIGINT:
                variableType = new FieldDetail(variableName, INT64, JAVA_TYPE_LONG, INT64);
                break;

            case "DECIMAL":
            case "NUMERIC":
                variableType = new FieldDetail(variableName, PROTO_TYPE_DOUBLE, JAVA_MATH_BIG_DECIMAL, FLOAT64);
                break;

            // todo: can float32 be implemented in a better way?
            case "FLOAT":
            case "DOUBLE":
                variableType = new FieldDetail(variableName, PROTO_TYPE_DOUBLE, JAVA_TYPE_DOUBLE, FLOAT64);
                break;

            case "DATE":
                variableType = new FieldDetail(variableName, INT64, JAVA_SQL_DATE, DATETIME);
                break;

            case "DATETIME":
                variableType = new FieldDetail(variableName, INT64, JAVA_SQL_DATE, DATETIME);
                break;

            case "TIMESTAMP":
                variableType = new FieldDetail(variableName, PROTO_TYPE_TIMESTAMP, JAVA_SQL_TIMESTAMP, TIMESTAMP);
                break;

            case "TIME":
                variableType = new FieldDetail(variableName, STRING, JAVA_SQL_TIME, "time.Time");
                break;

            case "LONGVARBINARY":
            case "VARBINARY":
            case "BINARY":
            case "BLOB":
            case "CLOB":
                variableType = new FieldDetail(variableName, PROTO_TYPE_BYTES, "byte[]", "[]byte");
                break;

            case "ARRAY":
                variableType = new FieldDetail(variableName, PROTO_TYPE_REPEATED, "[]", "[]");
                break;

            default:
                throw new IOException(
                    "Sql type is not understood = " + variableName + " type : " + dataTypeCode);
        }
        return variableType;
    }

    private FieldDetail processEnum(String variableName, String dataTypeCode, String tableName) {
        String enumName = CaseUtils.toCamelCase(variableName, true, '_');
        if (tableName.toLowerCase().startsWith("contract")) {
            return new FieldDetail(
                variableName, "farm.nurture.core.contracts.common." + variableName, JAVA_TYPE_STRING, ENUM);
        } else {
            StringBuilder protoEnum = new StringBuilder("enum " + enumName + "{");
            String enumElements = dataTypeCode;
            enumElements = enumElements.replace("ENUM(", "");
            enumElements = enumElements.replace(")", "");
            enumElements = enumElements.replace("'", "");

            int index = 0;
            for (String elem : StringUtils.fastSplit(enumElements, ',')) {
                elem = elem.replace(' ', '_');
                protoEnum.append(" ").append(elem).append(" = ").append(index).append(";");
                index++;
            }
            protoEnum.append("} \n");
            return new FieldDetail(variableName, protoEnum + enumName, JAVA_TYPE_STRING, ENUM);
        }
    }

    public List<FieldDetail> getFieldDetails(
        String dbName, String user, String password, String query, String dbUrl) throws SQLException {
        List<FieldDetail> result = new ArrayList<>();
        query = query.replace("?", "null");
        String connUrl = "jdbc:mysql://" + dbUrl + ":3306/" + dbName;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(connUrl, user, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            // Extract data from result set
            ResultSetMetaData md = rs.getMetaData();
            int totalCol = md.getColumnCount();
            String[] cols = new String[totalCol];
            String[] types = new String[totalCol];

            for (int i = 0; i < totalCol; i++) {
                cols[i] = md.getColumnLabel(i + 1);
                types[i] = md.getColumnTypeName(i + 1);
                FieldDetail gRpcType = deriveProtoDataType(cols[i], types[i], md.getTableName(i + 1));
                result.add(gRpcType);
            }
        } catch (SQLException | IOException e) {
            logger.error("error in getFieldDetails",e);
            throw new SQLException("SQLException raised",e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return result;
    }
}
