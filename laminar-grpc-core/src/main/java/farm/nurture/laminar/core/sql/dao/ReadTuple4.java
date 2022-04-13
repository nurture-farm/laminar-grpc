package farm.nurture.laminar.core.sql.dao;

import static farm.nurture.laminar.core.sql.dao.Constants.RS_NOT_INITIALIZED_MSG;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ReadTuple4 extends ReadBase<ReadTuple4.Tuple4> {

    private static final Logger LOG = LoggerFactory.getLogger(ReadTuple4.class);
    List<Tuple4> records = null;
    public ReadTuple4() {}

    public ReadTuple4(final List<Tuple4> records) {
        this.records = records;
    }

    protected final List<Tuple4> populate() throws SQLException {

        if (null == this.rs) {
            LOG.warn(RS_NOT_INITIALIZED_MSG);
            throw new SQLException(RS_NOT_INITIALIZED_MSG);
        }

        if (null == this.records) this.records = new ArrayList<>();

        while (this.rs.next()) {
            recordsCount++;
            Tuple4 twin = new Tuple4(rs.getObject(1), rs.getObject(2), rs.getObject(3), rs.getObject(4));
            records.add(twin);
        }
        return records;
    }

    @Override
    protected final Tuple4 getFirstRow() throws SQLException {
        if (null == this.rs) {
            LOG.warn(RS_NOT_INITIALIZED_MSG);
            throw new SQLException(RS_NOT_INITIALIZED_MSG);
        }

        if (this.rs.next()) {
            return new Tuple4(rs.getObject(1), rs.getObject(2), rs.getObject(3), rs.getObject(4));
        }

        return null;
    }

    @Override
    protected int getRecordsCount() {
        return recordsCount;
    }

    @Getter
    @Setter
    public static final class Tuple4 {

        private Object f0;
        private Object f1;
        private Object f2;
        private Object f3;

        public Tuple4(final Object f0, final Object f1, final Object f2, final Object f3) {
            this.setF0(f0);
            this.setF1(f1);
            this.setF2(f2);
            this.setF3(f3);
        }
    }
}
