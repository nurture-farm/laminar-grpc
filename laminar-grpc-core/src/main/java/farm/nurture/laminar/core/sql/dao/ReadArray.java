/*
 * Copyright 2015 Bizosys Technologies Limited
 *
 * Licensed to the Bizosys Technologies Limited (Bizosys) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The Bizosys licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package farm.nurture.laminar.core.sql.dao;

import static farm.nurture.laminar.core.sql.dao.Constants.RS_NOT_INITIALIZED_MSG;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadArray extends ReadBase<Object[]> {

    private static final Logger LOG = LoggerFactory.getLogger(ReadArray.class);

    protected List<Object[]> populate() throws SQLException {
        if (null == this.rs) {
            LOG.warn(RS_NOT_INITIALIZED_MSG);
            throw new SQLException(RS_NOT_INITIALIZED_MSG);
        }
        ResultSetMetaData md = rs.getMetaData();
        int totalCol = md.getColumnCount();

        List<Object[]> records = new ArrayList<>();
        while (this.rs.next()) {
            recordsCount++;
            Object[] cols = new Object[totalCol];
            for (int i = 0; i < totalCol; i++) {
                cols[i] = rs.getObject(i + 1);
            }
            records.add(cols);
        }
        return records;
    }

    @Override
    protected Object[] getFirstRow() throws SQLException {
        if (null == this.rs) {
            LOG.warn(RS_NOT_INITIALIZED_MSG);
            throw new SQLException(RS_NOT_INITIALIZED_MSG);
        }

        ResultSetMetaData md = rs.getMetaData();
        int totalCol = md.getColumnCount();

        if (this.rs.next()) {
            Object[] cols = new Object[totalCol];
            for (int i = 0; i < totalCol; i++) {
                cols[i] = rs.getObject(i + 1);
            }
            return cols;
        }

        return null;
    }

    @Override
    protected int getRecordsCount() {
        return recordsCount;
    }
}
