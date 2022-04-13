package farm.nurture.laminar.core.sql;

import farm.nurture.infra.util.FileReaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import farm.nurture.infra.util.StringUtils;
import farm.nurture.laminar.core.sql.dao.PoolFactory;
import farm.nurture.laminar.core.util.CoreUtil;
import java.io.File;
import java.util.UUID;

public class SqlProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(SqlProcessor.class);
    private static SqlProcessor singleton = null;

    private SqlProcessor() {

        LOG.info("Initializing DB Pool ...");
        String configXml = null;
        for (String fileName : new String[] {"/etc/laminar/jdbc.conf", "etc/jdbc.conf", "jdbc.conf"}) {
            LOG.info("Scanning jdbc.conf file @ {}",fileName);
            File file = new File(fileName);
            if (file.exists() && file.canRead()) {
                configXml = FileReaderUtil.toString(file.getAbsolutePath());
                break;
            }
        }

        // read from classpath
        if (null == configXml) {
            configXml = CoreUtil.readAsString("jdbc.conf");
        }

        if (StringUtils.isEmpty(configXml)) {
            LOG.error("Unable to load jdbc config");
            System.exit(1);
        }

        try {
            PoolFactory.getInstance().setup(configXml);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static SqlProcessor getInstance() {
        if (null != singleton) return singleton;
        synchronized (SqlProcessor.class.getName()) {
            if (null != singleton) return singleton;
            singleton = new SqlProcessor();
        }
        return singleton;
    }

    /**
     * Generates an UUID
     *
     * @param query_id
     * @return
     */
    public static String generateUUID(String query_id) {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public void init() {}
}
