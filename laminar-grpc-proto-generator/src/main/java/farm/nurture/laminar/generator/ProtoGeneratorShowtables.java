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

import static farm.nurture.laminar.generator.Constants.BIGINT;
import static farm.nurture.laminar.generator.Constants.INT;

import farm.nurture.laminar.generator.ast.AstBase;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtoGeneratorShowtables {
    private static final Logger logger = LoggerFactory.getLogger(ProtoGeneratorShowtables.class);
    public Map<String, FieldDetail> showTables(
        String dbName, String user, String password, String dbUrl, AstBase.Flags flags)
        throws SQLException, IOException {

        Map<String, FieldDetail> schemaColDataTypes = new HashMap<>();
        String connUrl = "jdbc:mysql://" + dbUrl + ":3306/" + dbName;
        Connection conn = null;
        executeAndExtract(user, password, schemaColDataTypes, connUrl, conn,flags);

        // Add grpc data types as is
        for (String gType :
            new String[] {
                "bool|Boolean|bool",
                "int32|Integer|int32",
                "int64|Long|int64",
                "uint32|Integer|uint32",
                "uint64|Long|uint64",
                "sint32|Integer|sint32",
                "sint64|Long|sint64",
                "float|Float|float32",
                "double|Double|float64",
                "fixed32|Integer|fixed32",
                "fixed64|Long|fixed64",
                "sfixed32|Integer|sfixed32",
                "sfixed64|Long|sfixed64",
                "string|String|string",
                "bytes|byte[]|[] byte",
                "repeated int32|Integer|[]int32",
                "repeated string|String|[]string"
            }) {

            int javaIndex = gType.indexOf('|');
            int goIndex = gType.indexOf('|', javaIndex + 1);
            String protoType = gType.substring(0, javaIndex);
            String javaType = gType.substring(javaIndex + 1, goIndex);
            String goType = gType.substring(goIndex + 1);
            schemaColDataTypes.put(protoType, new FieldDetail(protoType, protoType, javaType, goType));
        }
        return schemaColDataTypes;
    }

    private void executeAndExtract(String user, String password,
        Map<String, FieldDetail> schemaColDataTypes, String connUrl, Connection conn, AstBase.Flags flags)
        throws SQLException, IOException {
        try {
            conn = DriverManager.getConnection(connUrl, user, password);
            Statement stmt = conn.createStatement();
            // Extract data from result set
            String query = "show tables";
            ResultSet tableData = stmt.executeQuery(query);
            List<String> tableNameList = new ArrayList<>();

            while (tableData.next()) {
                tableNameList.add(tableData.getString(1));
            }
            processTableNameList(schemaColDataTypes, stmt, tableNameList, flags);
        } catch (SQLException e) {
            throw new SQLException("SQLException raised inside executeAndExtract",e);
        } catch (IOException e){
            throw new IOException("IOException raised inside executeAndExtract",e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    private void processTableNameList(Map<String, FieldDetail> schemaColDataTypes, Statement stmt,
        List<String> tableNameList, AstBase.Flags flags) throws SQLException, IOException {
        String query;
        for (String tableName : tableNameList) {
            logger.info("table name is : {}",tableName);
            query = "SHOW COLUMNS FROM " + tableName;
            ResultSet columnData = stmt.executeQuery(query);

            while (columnData.next()) {
                String columnName;
                String dataType;
                try{
                    columnName = columnData.getString(1);
                    dataType = columnData.getString(2).toUpperCase();
                    }
                catch (SQLException e){
                    throw new SQLException("Exception raised while getting the column data",e);
                }

                if (dataType.startsWith("VARCHAR")) dataType = "VARCHAR";
                else if (dataType.startsWith("CHAR")) dataType = "CHAR";
                else if (dataType.startsWith("DECIMAL")) dataType = "DECIMAL";
                else if (dataType.startsWith(INT)) dataType = INT;
                else if (dataType.startsWith("SMALLINT")) dataType = "SMALLINT";
                else if (dataType.startsWith(BIGINT)) dataType = BIGINT;
                else if (dataType.startsWith("TIMESTAMP")) flags.setProtoTimestampUsed(true);

                FieldDetail gRpcType;
                try{
                    gRpcType = new DatabaseReader().deriveProtoDataType(columnName, dataType, tableName);
                    schemaColDataTypes.put(tableName + "." + columnName, gRpcType);
                    }
                catch (IOException e){
                    throw new IOException("Exception raised while reading the data type",e);
                }
            }
        }
    }
}
