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

import java.io.PrintWriter;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadAsDML extends ReadBase<String> {
    public static final String READ_INSERT = "READ_INSERT";
    public static final String READ_UPDATE = "READ_UPDATE";
    private static final Logger LOG = LoggerFactory.getLogger(ReadAsDML.class);
    private static final HashMap<String, String> sqlTokens;
    private static Pattern sqlTokenPattern;

    static {
        // MySQL escape sequences: http://dev.mysql.com/doc/refman/5.1/en/string-syntax.html
        String[][] search_regex_replacement = new String[][]
            {
                //search string     search regex        farm.laminar.core.io.sql replacement regex
                {   "\u0000"    ,       "\\x00"     ,       "\\\\0"     },
                {   "'"         ,       "'"         ,       "\\\\'"     },
                {   "\""        ,       "\""        ,       "\\\\\""    },
                {   "\b"        ,       "\\x08"     ,       "\\\\b"     },
                {   "\n"        ,       "\\n"       ,       "\\\\n"     },
                {   "\r"        ,       "\\r"       ,       "\\\\r"     },
                {   "\t"        ,       "\\t"       ,       "\\\\t"     },
                {   "\u001A"    ,       "\\x1A"     ,       "\\\\Z"     },
                {   "\\"        ,       "\\\\"      ,       "\\\\\\\\"  }
            };

        sqlTokens = new HashMap<>();
        StringBuilder patternStr = new StringBuilder();
        for (String[] srr : search_regex_replacement) {
            sqlTokens.put(srr[0], srr[2]);
            patternStr.append((patternStr.length() == 0) ? "" : "|").append(srr[1]);
        }
        sqlTokenPattern = Pattern.compile('(' + patternStr.toString() + ')');
    }

    private String tableName = null;
    private PrintWriter out = null;
    private String readType = null;

    public ReadAsDML(PrintWriter out, String readType, String tableName) {
        this.out = out;
        this.readType = readType;
        this.tableName = tableName;
    }

    public static String escape(String s) {
        Matcher matcher = sqlTokenPattern.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, sqlTokens.get(matcher.group(1)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @Override
    protected List<String> populate() throws SQLException {
        if (this.rs == null) {
            throw new SQLException("Rs is not initialized.");
        }

        ResultSetMetaData md = rs.getMetaData();
        int totalCol = md.getColumnCount();
        String[] cols = new String[totalCol];
        int[] types = new int[totalCol];

        for (int i = 0; i < totalCol; i++) {
            cols[i] = md.getColumnLabel(i + 1);
            types[i] = md.getColumnType(i + 1);
        }

        List<String> records = null;
        StringBuilder strBuf = new StringBuilder();
        String className = null;

        int rowNo = 0;
        while (this.rs.next()) {
            rowNo = processRow(totalCol, cols, strBuf, className, rowNo);
        }
        this.out.append(';');
        return records;
    }

    private int processRow(int totalCol, String[] cols, StringBuilder strBuf, String className,
        int rowNo) throws SQLException {
        recordsCount++;
        rowNo++;

        if (this.readType == READ_INSERT) {
            this.recordAsInsertQuery(rowNo, totalCol, cols, strBuf, className);
        } else if (this.readType == READ_UPDATE) {
            this.recordAsUpdateQuery(totalCol, cols, strBuf, className);
        } else {
            throw new SQLException(
                "Unknown Read Format : "
                    + READ_INSERT
                    + " For insert and "
                    + READ_UPDATE
                    + " For update are supported.");
        }

        if (LOG.isDebugEnabled()) LOG.debug(strBuf.toString());

        this.out.println(strBuf.toString());
        strBuf.delete(0, strBuf.length());
        return rowNo;
    }

    @Override
    protected String getFirstRow() throws SQLException {
        if (this.rs == null) {
            throw new SQLException("Rs is not initialized.");
        }

        ResultSetMetaData md = rs.getMetaData();
        int totalCol = md.getColumnCount();
        String[] cols = new String[totalCol];
        int[] types = new int[totalCol];
        for (int i = 0; i < totalCol; i++) {
            cols[i] = md.getColumnLabel(i + 1);
            types[i] = md.getColumnType(i + 1);
        }

        StringBuilder strBuf = new StringBuilder();
        if (!this.rs.next()) return null;

        if (LOG.isDebugEnabled()) LOG.debug(strBuf.toString());
        String xmlRec = strBuf.toString();
        if (this.out != null) this.out.println(xmlRec);
        return xmlRec;
    }

    private void recordAsInsertQuery(
        int rowNo, int totalCol, String[] cols, StringBuilder strBuf, String className)
        throws SQLException {
        if (rowNo == 1) {
            strBuf.append("INSERT INTO " + tableName + " (");
            for (int i = 0; i < totalCol; i++) {
                strBuf.append(cols[i]);
                if (i == (totalCol - 1)) strBuf.append(") VALUES \n(");
                else strBuf.append(',');
            }
        } else {
            strBuf.append(",(");
        }

        for (int i = 0; i < totalCol; i++) {
            Object obj = rs.getObject(i + 1);
            if (null == obj) continue;

            strBuf.append("\"").append(escape(rs.getObject(i + 1).toString())).append("\"");
            if (i < (totalCol - 1)) strBuf.append(", ");
        }

        strBuf.append(')');
    }

    private void recordAsUpdateQuery(
        int totalCol, String[] cols, StringBuilder strBuf, String className) throws SQLException {
        strBuf.append("UPDATE ").append(tableName).append(" SET ");
        int totalColMinus1 = totalCol - 1;

        for (int i = 1; i < totalColMinus1; i++) {
            Object obj = rs.getObject(i + 1);
            if (null == obj) continue;

            strBuf.append(cols[i]).append('=');
            strBuf.append('"').append(escape(rs.getObject(i + 1).toString())).append('"');
            if (i < (totalCol - 2)) strBuf.append(", ");
        }
        strBuf.append(" WHERE ");
        strBuf.append(cols[0]).append('=');
        strBuf.append('"').append(escape(rs.getObject(1).toString())).append('"');
        strBuf.append(';');
    }

    @Override
    protected int getRecordsCount() {
        return recordsCount;
    }
}
