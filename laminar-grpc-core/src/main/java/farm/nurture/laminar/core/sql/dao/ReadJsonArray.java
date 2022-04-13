/*
 * Copyright 2010 Bizosys Technologies Limited
 *
 * Licensed to the Bizosys Technologies Limited (Bizosys) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Bizosys licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package farm.nurture.laminar.core.sql.dao;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ReadJsonArray extends ReadBase<String> {

    private static final String EMPTY_STRING = "";
    private static final Logger LOG = Logger.getLogger(String.valueOf(ReadJsonArray.class));
    private static Pattern pattern = Pattern.compile("\"");
    private PrintWriterOptional out = null;
    private Collection<String> fieldsWithArrayDataType = null;
    private Collection<String> allowedFields = null;

    public ReadJsonArray(
        PrintWriterOptional out,
        Collection<String> allowedFields,
        Collection<String> fieldsWithArrayDataType) {
        this.out = out;
        this.allowedFields = allowedFields;
        this.fieldsWithArrayDataType = fieldsWithArrayDataType;
    }

    @Override
    protected List<String> populate() throws SQLException {

        checkCondition();
        ResultSetMetaData md = rs.getMetaData();
        int totalCol = md.getColumnCount();
        String[] cols = createLabels(md, totalCol);

        List<String> records = null;
        boolean isStreaming = (null != out);
        if (isStreaming) out.println("{ \"values\" : [");
        else records = new ArrayList<>(totalCol);

        StringBuilder aRecord = new StringBuilder(256);
        boolean isFirstRow = true;
        while (this.rs.next()) {
            isFirstRow = processRecord(totalCol, cols, records, isStreaming, aRecord, isFirstRow);
        }
        if (isStreaming) {
            if (!isFirstRow) out.println("}]}");
            else out.println("]}");
        }
        return records;
    }

    private boolean processRecord(
        int totalCol,
        String[] cols,
        List<String> records,
        boolean isStreaming,
        StringBuilder aRecord,
        boolean isFirstRow)
        throws SQLException {
        if (isFirstRow) isFirstRow = false;
        else out.println("},\n");
        assert out != null;
        out.println("{ ");
        createRecord(totalCol, cols, aRecord);
        if (isStreaming) {
            out.print(aRecord.toString());
        } else {
            records.add(aRecord.toString());
        }
        aRecord.delete(0, aRecord.capacity());
        return isFirstRow;
    }

    @Override
    protected String getFirstRow() throws SQLException {
        checkCondition();
        ResultSetMetaData md = rs.getMetaData();
        int totalCol = md.getColumnCount();
        String[] cols = createLabels(md, totalCol);

        StringBuilder aRecord = new StringBuilder(256);
        if (this.rs.next()) {
            createRecord(totalCol, cols, aRecord);
        }
        return aRecord.toString();
    }

    @Override
    protected int getRecordsCount() {
        return recordsCount;
    }

    private void checkCondition() throws SQLException {
        if (null == this.rs) {
            LOG.warning("Rs is not initialized.");
            throw new SQLException("Rs is not initialized.");
        }
    }

    private String[] createLabels(ResultSetMetaData md, int totalCol) throws SQLException {
        String[] cols = new String[totalCol];
        for (int i = 0; i < totalCol; i++) {
            cols[i] = md.getColumnLabel(i + 1);
        }
        return cols;
    }

    private void createRecord(int colsT, String[] cols, StringBuilder recordsSb) throws SQLException {

        boolean inBetweenRows = false;
        boolean isAllowed = false;
        boolean isArrayDatatype = false;

        Object colObj = null;
        String colStr = null;

        for (int colI = 0; colI < colsT; colI++) {
            isAllowed = null == allowedFields || this.allowedFields.contains(cols[colI]);
            if (!isAllowed) continue;

            inBetweenRows = (colI < (colsT - 1));

            colObj = rs.getObject(colI + 1);
            colStr = (null == colObj) ? EMPTY_STRING : colObj.toString().trim();
            colStr = pattern.matcher(colStr).replaceAll("\\\\\""); // Escape all dobule quotes
            isArrayDatatype =
                null != fieldsWithArrayDataType && this.fieldsWithArrayDataType.contains(
                    cols[colI]);
            processArrayDataType(cols, recordsSb, isArrayDatatype, colStr, colI);

            if (inBetweenRows) {
                recordsSb.append(',');
            }
            recordsSb.append('\n');
        }
    }

    private void processArrayDataType(
        String[] cols, StringBuilder recordsSb, boolean isArrayDatatype, String colStr, int colI) {
        if (isArrayDatatype) {
            if (!colStr.isEmpty()) {
                recordsSb.append('"').append(cols[colI]).append("\":").append(colStr);
            } else {
                recordsSb.append('"').append(cols[colI]).append("\":\"").append(colStr).append("\"");
            }
        } else {
            recordsSb.append('"').append(cols[colI]).append("\":\"").append(colStr).append('\"');
        }
    }
}
