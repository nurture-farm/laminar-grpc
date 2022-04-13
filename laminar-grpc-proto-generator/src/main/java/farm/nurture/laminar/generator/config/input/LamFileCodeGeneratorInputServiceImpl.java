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

package farm.nurture.laminar.generator.config.input;

import com.fasterxml.jackson.core.type.TypeReference;
import farm.nurture.infra.util.Logger;
import farm.nurture.infra.util.LoggerFactory;
import farm.nurture.laminar.core.util.JsonUtil;
import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.Constants;
import farm.nurture.laminar.generator.DatabaseReader;
import farm.nurture.laminar.generator.FieldDetail;
import farm.nurture.laminar.generator.Utilities;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class LamFileCodeGeneratorInputServiceImpl implements ICodeGenratorInputService {

    private static final Logger logger = LoggerFactory.getLogger(LamFileCodeGeneratorInputServiceImpl.class);

    @Override
    public List<AppConfigVO> getApplicationConfiguration(String param) throws IOException {
        try {
            return JsonUtil.deserialize(
                Files.readAllBytes(Paths.get(param)), new TypeReference<List<AppConfigVO>>() {});
        } catch (IOException e) {
            String error = Utilities.getTopLinesFromStackTrace(e, Constants.DEFAULT_NUM_STACKTRACE);
            logger.error("Error: " + error);
            throw new IOException("IOException inside getApplicationConfiguration",e);
        }
    }

    @Override
    public List<FieldDetail> getFieldDetailsForReqRes(
        String query, Object[] params, String db, String dbUrl, String dbUserName, String dbPwd) {
        try {
            logger.info("Query :: " + query);

            if (params != null) {
                for (Object param : params) {
                    logger.info("params :: " + param);
                }
            }

            else{
                logger.info("params :: " + null);
            }


            return new DatabaseReader().getFieldDetails(db, dbUserName, dbPwd, query, dbUrl);
        } catch (Exception e) {
            String error = Utilities.getTopLinesFromStackTrace(e, Constants.DEFAULT_NUM_STACKTRACE);
            logger.error("Error: " + error);
        }
        return null;
    }
}
