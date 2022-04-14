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

import static farm.nurture.laminar.core.sql.dao.Constants.RS_NOT_INITIALIZED_MSG;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ReadJsonOptimised extends ReadBase<String> {

    private static final String EMPTY_STRING = "";
    private static final Logger LOG = Logger.getLogger(String.valueOf(ReadJsonOptimised.class));
    private static Pattern pattern = Pattern.compile("\"");
    private PrintWriterOptional out = null;
    private Collection<String> fieldsWithArrayDataType = null;

    public ReadJsonOptimised(PrintWriterOptional out, Collection<String> fieldsWithArrayDataType) {
        this.out = out;
        this.fieldsWithArrayDataType = fieldsWithArrayDataType;
    }

    @Override
    protected List<String> populate() throws SQLException {

        checkCondition();
        ResultSetMetaData md = rs.getMetaData();
        int totalCol = md.getColumnCount();
        String[] cols = createLabels(md, totalCol);

        List<String> records = null;
        out.print("{ \"values\" : [".intern());

        boolean isFirstRow = true;
        while (this.rs.next()) {
            if (isFirstRow) isFirstRow = false;
            else out.print("},\n".intern());
            out.print("{ ".intern());
            createRecord(totalCol, cols);
        }

        if (!isFirstRow) out.print("}]}".intern());
        else out.print("]}".intern());

        return records;
    }

    @Override
    protected String getFirstRow() throws SQLException {
        checkCondition();
        ResultSetMetaData md = rs.getMetaData();
        int totalCol = md.getColumnCount();
        String[] cols = createLabels(md, totalCol);

        if (this.rs.next()) {
            createRecord(totalCol, cols);
        }
        return null;
    }

    @Override
    protected int getRecordsCount() {
        return recordsCount;
    }

    private void checkCondition() throws SQLException {
        if (null == this.rs) {
            LOG.warning(RS_NOT_INITIALIZED_MSG);
            throw new SQLException(RS_NOT_INITIALIZED_MSG);
        }
    }

    private String[] createLabels(ResultSetMetaData md, int totalCol) throws SQLException {
        String[] cols = new String[totalCol];
        for (int i = 0; i < totalCol; i++) {
            cols[i] = md.getColumnLabel(i + 1);
        }
        return cols;
    }

    private void createRecord(int colsT, String[] cols) throws SQLException {

        boolean inBetweenRows = false;
        boolean isAllowed = false;
        boolean isArrayDatatype = false;

        Object colObj = null;
        String colStr = null;

        for (int colI = 0; colI < colsT; colI++) {

            inBetweenRows = (colI < (colsT - 1));

            colObj = rs.getObject(colI + 1);
            if (null == colObj) {
                colStr = EMPTY_STRING;
            } else {
                colStr = colObj.toString();
                colStr = pattern.matcher(colStr).replaceAll("\\\\\""); // Escape all dobule quotes
            }

            isArrayDatatype =
                null != fieldsWithArrayDataType && this.fieldsWithArrayDataType.contains(cols[colI]);
            processIsArrayDataType(cols, isArrayDatatype, colStr, colI);

            if (inBetweenRows) {
                out.print(",".intern());
            }
            out.print("\n".intern());
        }
    }

    private void processIsArrayDataType(
        String[] cols, boolean isArrayDatatype, String colStr, int colI) {
        if (isArrayDatatype) {
            if (!colStr.isEmpty()) {
                out.print("\"".intern()).print(cols[colI].intern()).print("\":".intern()).print(colStr);
            } else {
                out.print("\"".intern())
                    .print(cols[colI].intern())
                    .print("\":\"".intern())
                    .print(colStr)
                    .print("\"".intern());
            }
        } else {
            out.print("\"".intern())
                .print(cols[colI].intern())
                .print("\":\"".intern())
                .print(colStr)
                .print("\"".intern());
        }
    }
}
