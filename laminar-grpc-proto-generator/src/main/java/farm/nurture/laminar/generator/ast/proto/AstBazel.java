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

package farm.nurture.laminar.generator.ast.proto;

import static farm.nurture.laminar.generator.Constants.SERVICE_NAME;

import farm.nurture.laminar.core.util.CaseUtils;
import farm.nurture.laminar.generator.TemplatesVO;
import farm.nurture.laminar.generator.ast.AstBase;

public class AstBazel extends AstBase {

    StringBuilder bazelBuildResource;

    public AstBazel(StringBuilder bazelBuildResource) {
        this.bazelBuildResource = bazelBuildResource;
    }

    @Override
    public void onEnd(){

        String bazelBuildContent = readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/proto/bazel.build.template");

        bazelBuildContent = bazelBuildContent.replace("@service_name_snake@", CaseUtils.camelToSnake(
            getServiceName()));
        bazelBuildContent = bazelBuildContent.replace(SERVICE_NAME, getServiceName());

        bazelBuildResource.append(bazelBuildContent);
    }
}
