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

import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.FieldDetail;

import java.io.IOException;
import java.util.List;

public interface ICodeGenratorInputService {
    /**
     * @return List of AppConfigs, which contains metadata about API to be created.
     * The source of app config is selected using environment variable "code_genration_input_config_source" in config.json
     * code_genration_input_config_source=MYSQL uses mysql "app_config" table as the configuration source.
     * code_genration_input_config_source = LAM uses application.lam file for code generation and act as config source.
     *
     */
    List<AppConfigVO> getApplicationConfiguration(String param) throws IOException;

    List<FieldDetail> getFieldDetailsForReqRes(final String query, final Object[] params, String db, String dbUrl, String dbUserName,String dbPwd);


}
