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
