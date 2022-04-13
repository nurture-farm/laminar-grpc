package farm.nurture.laminar.core.sql.dao;

import static farm.nurture.laminar.core.sql.dao.Constants.RS_NOT_INITIALIZED_MSG;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadTuple2 extends ReadBase<ReadTuple2.Tuple2> {

    private static final Logger LOG = LoggerFactory.getLogger(ReadTuple2.class);
    private List<Tuple2> records = null;

    public ReadTuple2() {}

    public ReadTuple2(final List<Tuple2> records) {
        this.records = records;
    }

    protected final List<Tuple2> populate() throws SQLException {
        if (null == this.rs) {
            LOG.warn(RS_NOT_INITIALIZED_MSG);
            throw new SQLException(RS_NOT_INITIALIZED_MSG);
        }

        if (null == records) records = new ArrayList<>();
        while (this.rs.next()) {
            recordsCount++;
            Tuple2 twin = new Tuple2(rs.getObject(1), rs.getObject(2));
            records.add(twin);
        }
        return records;
    }

    @Override
    protected final Tuple2 getFirstRow() throws SQLException {
        if (null == this.rs) {
            LOG.warn(RS_NOT_INITIALIZED_MSG);
            throw new SQLException(RS_NOT_INITIALIZED_MSG);
        }

        if (this.rs.next()) {
            return new Tuple2(rs.getObject(1), rs.getObject(2));
        }

        return null;
    }

    @Override
    protected int getRecordsCount() {
        return recordsCount;
    }

    @Getter
    @Setter
    public static final class Tuple2 {
        private Object f0;
        private Object f1;

        public Tuple2(final Object f0, final Object f1) {
            this.setF0(f0);
            this.setF1(f1);
        }
    }
}
