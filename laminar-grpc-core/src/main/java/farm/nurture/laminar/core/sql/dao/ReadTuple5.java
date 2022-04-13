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

public final class ReadTuple5 extends ReadBase<ReadTuple5.Tuple5> {

    private static final Logger LOG = LoggerFactory.getLogger(ReadTuple5.class);
    List<Tuple5> records = null;

    public ReadTuple5() {}

    public ReadTuple5(final List<Tuple5> records) {
        this.records = records;
    }

    protected final List<Tuple5> populate() throws SQLException {
        if (null == this.rs) {
            LOG.warn(RS_NOT_INITIALIZED_MSG);
            throw new SQLException(RS_NOT_INITIALIZED_MSG);
        }

        if (null == records) records = new ArrayList<>();
        this.rs.setFetchSize(50);
        while (this.rs.next()) {
            recordsCount++;
            Tuple5 quint =
                new Tuple5(
                    rs.getObject(1), rs.getObject(2), rs.getObject(3), rs.getObject(4), rs.getObject(5));
            records.add(quint);
        }
        return records;
    }

    @Override
    protected final Tuple5 getFirstRow() throws SQLException {
        if (null == this.rs) {
            LOG.warn(RS_NOT_INITIALIZED_MSG);
            throw new SQLException(RS_NOT_INITIALIZED_MSG);
        }

        if (this.rs.next()) {
            return new Tuple5(
                rs.getObject(1), rs.getObject(2), rs.getObject(3), rs.getObject(4), rs.getObject(5));
        }

        return null;
    }

    @Override
    protected int getRecordsCount() {
        return recordsCount;
    }

    @Setter
    @Getter
    public static final class Tuple5 {

        private Object f0;
        private Object f1;
        private Object f2;
        private Object f3;
        private Object f4;

        public Tuple5(
            final Object f0, final Object f1, final Object f2, final Object f3, final Object f4) {

            this.setF0(f0);
            this.setF1(f1);
            this.setF2(f2);
            this.setF3(f3);
            this.setF4(f4);
        }
    }
}
