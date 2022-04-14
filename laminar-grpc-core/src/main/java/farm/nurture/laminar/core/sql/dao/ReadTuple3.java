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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ReadTuple3 extends ReadBase<ReadTuple3.Tuple3> {

    private static final Logger LOG = LoggerFactory.getLogger(ReadTuple3.class);
    List<Tuple3> records = null;

    public ReadTuple3(final List<Tuple3> records) {
        this.records = records;
    }

    public ReadTuple3() {}

    protected final List<Tuple3> populate() throws SQLException {
        if (null == this.rs) {
            LOG.warn(RS_NOT_INITIALIZED_MSG);
            throw new SQLException(RS_NOT_INITIALIZED_MSG);
        }

        if (null == records) records = new ArrayList<>();

        while (this.rs.next()) {
            recordsCount++;
            Tuple3 twin = new Tuple3(rs.getObject(1), rs.getObject(2), rs.getObject(3));
            records.add(twin);
        }
        return records;
    }

    @Override
    protected final Tuple3 getFirstRow() throws SQLException {
        if (null == this.rs) {
            LOG.warn(RS_NOT_INITIALIZED_MSG);
            throw new SQLException(RS_NOT_INITIALIZED_MSG);
        }

        if (this.rs.next()) {
            return new Tuple3(rs.getObject(1), rs.getObject(2), rs.getObject(3));
        }

        return null;
    }

    @Override
    protected int getRecordsCount() {
        return recordsCount;
    }

    @Getter
    @Setter
    public static final class Tuple3 {
        private Object f0;
        private Object f1;
        private Object f2;

        public Tuple3(final Object f0, final Object f1, final Object f2) {
            this.setF0(f0);
            this.setF0(f1);
            this.setF2(f2);
        }
    }
}
