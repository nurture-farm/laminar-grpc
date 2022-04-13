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

package farm.nurture.laminar.generator.ast.golang;

import static farm.nurture.laminar.generator.Constants.CODE_URL;
import static farm.nurture.laminar.generator.Constants.FORWARD_SLASH;
import static farm.nurture.laminar.generator.Constants.NAME_SPACE;
import static farm.nurture.laminar.generator.Constants.SERVICE_NAME;

import farm.nurture.infra.util.StringUtils;
import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.Configuration;
import farm.nurture.laminar.generator.FieldDetail;
import farm.nurture.laminar.generator.TemplatesVO;
import farm.nurture.laminar.generator.ast.AstBase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AstExecutor extends AstBase {

    List<AstBase> parts = new ArrayList<>();

    String executorContent;
    String commonContent;

    public AstExecutor() {
        executorContent = readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/database/executor/executor.go.template");
        commonContent = readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/database/common.go.template");
    }

    @Override
    public void onStart(Configuration conf, Flags flags ){
        super.onStart(conf, flags);
        for ( AstBase b: parts) b.onStart(conf,flags);
    }

    @Override
    public void onEntry(AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields){

        for ( AstBase b: parts) b.onEntry(entry, requestFields, responseFields);
    }

    @Override
    public void onEnd(){
        super.onEnd();
        for ( AstBase b: parts) b.onEnd();

        String namespace = super.getNamespace();
        if(StringUtils.isNonEmpty(namespace)) {
            namespace = FORWARD_SLASH+namespace;
        }

        File dj = new File(getGoGeneratePath() + "/database");
        if (!dj.exists()) dj.mkdirs();

        writeToFile( super.getGoGeneratePath() + "/database/common.go", commonContent);

        File ej = new File(getGoGeneratePath() + "/database/executor");
        if (!ej.exists()) ej.mkdirs();

        String executorContentL = executorContent.replace(CODE_URL, super.getCodeUrl());
        executorContentL = executorContentL.replace(SERVICE_NAME, super.getServiceName());
        executorContentL = executorContentL.replace(NAME_SPACE,namespace);

        writeToFile( super.getGoGeneratePath() + "/database/executor/executor.go", executorContentL);
    }
}
