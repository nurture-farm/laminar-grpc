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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ReadTuple6 extends ReadBase<ReadTuple6.Tuple6> {

    private static final Logger LOG = LoggerFactory.getLogger(ReadTuple6.class);
    List<Tuple6> records = null;

    public ReadTuple6() {}

    public ReadTuple6(final List<Tuple6> records) {
        this.records = records;
    }

    protected final List<Tuple6> populate() throws SQLException {
        if (null == this.rs) {
            LOG.warn(RS_NOT_INITIALIZED_MSG);
            throw new SQLException(RS_NOT_INITIALIZED_MSG);
        }

        if (null == records) records = new ArrayList<>();
        this.rs.setFetchSize(50);
        while (this.rs.next()) {
            recordsCount++;
            Tuple6 currRecord =
                new Tuple6(
                    rs.getObject(1),
                    rs.getObject(2),
                    rs.getObject(3),
                    rs.getObject(4),
                    rs.getObject(5),
                    rs.getObject(6));
            records.add(currRecord);
        }
        return records;
    }

    @Override
    protected final Tuple6 getFirstRow() throws SQLException {
        if (null == this.rs) {
            LOG.warn(RS_NOT_INITIALIZED_MSG);
            throw new SQLException(RS_NOT_INITIALIZED_MSG);
        }

        if (this.rs.next()) {
            return new Tuple6(
                rs.getObject(1),
                rs.getObject(2),
                rs.getObject(3),
                rs.getObject(4),
                rs.getObject(5),
                rs.getObject(6));
        }

        return null;
    }

    @Override
    protected int getRecordsCount() {
        return recordsCount;
    }

    @Getter
    @Setter
    public static final class Tuple6 {

        private Object f0;
        private Object f1;
        private Object f2;
        private Object f3;
        private Object f4;
        private Object f5;

        public Tuple6(
            final Object f0,
            final Object f1,
            final Object f2,
            final Object f3,
            final Object f4,
            final Object f5) {

            this.setF0(f0);
            this.setF1(f1);
            this.setF2(f2);
            this.setF3(f3);
            this.setF4(f4);
            this.setF5(f5);
        }
    }
}
