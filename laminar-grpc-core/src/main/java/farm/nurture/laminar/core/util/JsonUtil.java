package farm.nurture.laminar.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import farm.nurture.infra.util.Logger;
import farm.nurture.infra.util.LoggerFactory;
import java.io.IOException;
import java.util.List;

public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    static {
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    private JsonUtil() {}

    public static String serialize(Object value) throws JsonProcessingException {
        return mapper.writeValueAsString(value);
    }

    public static <T> T deserialize(String value, Class<T> clazz) throws IOException {
        return mapper.readValue(value, clazz);
    }

    public static <T> T deserialize(byte[] value, Class<T> clazz) throws IOException {
        logger.info("Value = {}",new String(value));
        return mapper.readValue(value, clazz);
    }

    public static <T> List<T> deserialize(byte[] value, TypeReference<List<T>> clazz)
        throws IOException {
        return mapper.readValue(value, clazz);
    }
}
