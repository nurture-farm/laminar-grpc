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

package farm.nurture.laminar.generator.config.input;

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
import static farm.nurture.laminar.generator.Constants.JAVA_TYPE_FLOAT;
import static farm.nurture.laminar.generator.Constants.JAVA_TYPE_INTEGER;
import static farm.nurture.laminar.generator.Constants.JAVA_TYPE_LONG;
import static farm.nurture.laminar.generator.Constants.JAVA_TYPE_STRING;
import static farm.nurture.laminar.generator.Constants.PROTO_TYPE_BYTES;
import static farm.nurture.laminar.generator.Constants.PROTO_TYPE_DOUBLE;
import static farm.nurture.laminar.generator.Constants.PROTO_TYPE_FLOAT;
import static farm.nurture.laminar.generator.Constants.PROTO_TYPE_REPEATED;
import static farm.nurture.laminar.generator.Constants.STRING;
import static farm.nurture.laminar.generator.Constants.TIMESTAMP;

import farm.nurture.infra.util.StringUtils;
import farm.nurture.laminar.core.util.CaseUtils;
import farm.nurture.laminar.generator.FieldDetail;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test {
    static final String DB_URL = "jdbc:mysql://localhost/fcb08084_4d40_4b13_970e_51af2809444c";
    static final String USER = "root";
    static final String PASS = "welcome";
    static final String QUERY = "select value,created_at from key_value_cache where cache_key= null";
    private static final Logger logger = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) throws SQLException {
        // Open a connection
        getFieldDetails(DB_URL, USER, PASS, QUERY);
    }

    private static void getFieldDetails(String dbUrl, String user, String password, String query)
        throws SQLException {
        List<FieldDetail> result = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(dbUrl, user, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            // Extract data from result set
            ResultSetMetaData md = rs.getMetaData();
            int totalCol = md.getColumnCount();
            String[] cols = new String[totalCol];
            String[] types = new String[totalCol];

            for (int i = 0; i < totalCol; i++) {
                addGrpcType(result, md, cols, types, i);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    private static void addGrpcType(List<FieldDetail> result, ResultSetMetaData md, String[] cols,
        String[] types, int i) throws SQLException, IOException {
        cols[i] = md.getColumnLabel(i + 1);
        types[i] = md.getColumnTypeName(i + 1);
        FieldDetail gRpcType = deriveProtoDataType(cols[i], types[i], md.getTableName(i + 1));
        logger.info(
            "------- {} === {} +++  {}",
            gRpcType.getProtoType(),
            gRpcType.getGoType(),
            gRpcType.getJavaType());
        result.add(gRpcType);
    }

    public static FieldDetail deriveProtoDataType(
        String variableName, String dataTypeCode, String tableName) throws IOException {

        logger.info("dataTypeCode: {}\t {}",variableName,dataTypeCode);
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

            case "FLOAT":
                variableType = new FieldDetail(variableName, PROTO_TYPE_FLOAT, JAVA_TYPE_FLOAT, "float32");
                break;

            case BIGINT:
                variableType = new FieldDetail(variableName, INT64, JAVA_TYPE_LONG, INT64);
                break;

            case "DECIMAL":
            case "NUMERIC":
                variableType = new FieldDetail(variableName, PROTO_TYPE_DOUBLE, JAVA_MATH_BIG_DECIMAL, FLOAT64);
                break;

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
                variableType = new FieldDetail(variableName, INT64, JAVA_SQL_TIMESTAMP, TIMESTAMP);
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

    private static FieldDetail processEnum(
        String variableName, String dataTypeCode, String tableName) {
        String enumName = CaseUtils.toCamelCase(variableName, true, '_');
        if (tableName.toLowerCase().startsWith("contract")) {
            return new FieldDetail(
                variableName,
                "farm.nurture.core.contracts.common." + variableName,
                JAVA_TYPE_STRING,
                ENUM);
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
}
