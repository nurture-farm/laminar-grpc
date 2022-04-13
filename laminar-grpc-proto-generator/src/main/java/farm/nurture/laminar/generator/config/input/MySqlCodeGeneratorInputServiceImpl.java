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

import farm.nurture.infra.util.Logger;
import farm.nurture.infra.util.LoggerFactory;
import farm.nurture.laminar.generator.AppConfig;
import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.Constants;
import farm.nurture.laminar.generator.DatabaseReader;
import farm.nurture.laminar.generator.FieldDetail;
import farm.nurture.laminar.generator.Utilities;
import java.sql.SQLException;
import java.util.List;

public class MySqlCodeGeneratorInputServiceImpl implements ICodeGenratorInputService {
    private static final Logger logger = LoggerFactory.getLogger(MySqlCodeGeneratorInputServiceImpl.class);
    @Override
    public List<AppConfigVO> getApplicationConfiguration(String param) {
        try {
            return new AppConfig()
                .execute(
                    "select id,title,req_name,res_name,decl_req,decl_res,decl_grpc,decl_grapql,sql_stmt,sql_params,sql_replace,sql_uniquekey,sql_pool,impl_dao,impl_grpc,impl_reacrjs,req_override,res_override,oauth_public,oauth_claims,mutation,status from app_config where status = 1");
        } catch (Exception e) {
            String error = Utilities.getTopLinesFromStackTrace(e, Constants.DEFAULT_NUM_STACKTRACE);
            logger.error("Error: " + error);
        }
        return null;
    }

    @Override
    public List<FieldDetail> getFieldDetailsForReqRes(
        String query, Object[] params, String db, String dbUrl, String dbUserName, String dbPwd) {
        try {
            return new DatabaseReader().execute(query, params);
        } catch (SQLException throwables) {
            String error =
                Utilities.getTopLinesFromStackTrace(throwables, Constants.DEFAULT_NUM_STACKTRACE);
            logger.error("Error: " + error);
        }
        return null;
    }
}
