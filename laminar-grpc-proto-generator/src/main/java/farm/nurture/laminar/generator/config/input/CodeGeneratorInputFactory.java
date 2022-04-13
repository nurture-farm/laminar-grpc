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

import farm.nurture.laminar.generator.Constants;

public class CodeGeneratorInputFactory {

    public ICodeGenratorInputService getCodeGeneratorInputService(String configSource) {
        if (null == configSource) configSource = Constants.DEFAULT_CONFIG_SOURCE;

        if (configSource.equals("LAM")) {
            return new LamFileCodeGeneratorInputServiceImpl();
        } else {
            return new MySqlCodeGeneratorInputServiceImpl();
        }
    }
}
