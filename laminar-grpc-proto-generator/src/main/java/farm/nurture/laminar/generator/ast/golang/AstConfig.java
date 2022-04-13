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
import static farm.nurture.laminar.generator.Constants.NAME_SPACE;
import static farm.nurture.laminar.generator.Constants.PROMETHUES_PORT;
import static farm.nurture.laminar.generator.Constants.SERVER_PORT;
import static farm.nurture.laminar.generator.Constants.SERVICE_NAME;
import static farm.nurture.laminar.generator.Constants.SERVICE_NAME_KEBAB;

import farm.nurture.infra.util.StringUtils;
import farm.nurture.laminar.core.util.CaseUtils;
import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.Configuration;
import farm.nurture.laminar.generator.FieldDetail;
import farm.nurture.laminar.generator.TemplatesVO;
import farm.nurture.laminar.generator.ast.AstBase;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AstConfig extends AstBase {

    private List<AstBase> parts = new ArrayList<>();
    private String goModContent;
    private String runLocalContent;
    private String configContent;
    private String dockerFileContent;
    private String buildFileContent;
    private String buildAndPublishFileContent;
    private StringBuilder insertConstantLines = new StringBuilder();
    private AstBase.Flags flags;

    public AstConfig() {
        goModContent = readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/go.mod.template");
        runLocalContent = readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/run.local.template");
        configContent = readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/config.json.template");
        dockerFileContent = readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/dockerfile.template");
        buildFileContent = readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/build.sh.template");
        buildAndPublishFileContent = readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/build_and_publish.sh.template");
        parts.add(new AstDeployments());
    }

    @Override
    public void onStart(Configuration conf, AstBase.Flags flags) {
        super.onStart(conf, flags);
        this.flags = flags;
        for (AstBase b : parts) b.onStart(conf, flags);
    }

    @Override
    public void onEntry(
        AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields) {

        for (AstBase b : parts) b.onEntry(entry, requestFields, responseFields);
    }

    @Override
    public void onEnd() {
        super.onEnd();
        for (AstBase b : parts) b.onEnd();

        String namespace = super.getNamespace();
        if (StringUtils.isNonEmpty(namespace)) {
            namespace = "/" + namespace;
        }

        processGoModContent(namespace);

        runLocalContent = runLocalContent.replace("@go_overridepath@", super.getGoOverridePath());
        writeToFile(getServiceName()+ "/run_local.sh", runLocalContent);

        File pj = new File(getServiceName() + "/config");
        if (!pj.exists()) pj.mkdirs();

        processConfigContent();
        processDockerFile(namespace);

        String buildFileContentL = buildFileContent;
        buildFileContentL =
            buildFileContentL.replace(SERVICE_NAME_KEBAB, CaseUtils.camelToKebab(getServiceName()));
        writeToFile(getServiceName() + "/build.sh", buildFileContentL);

        String buildAndPublishFileContentL = buildAndPublishFileContent;
        buildAndPublishFileContentL =
            buildAndPublishFileContentL.replace(SERVICE_NAME_KEBAB, CaseUtils.camelToKebab(getServiceName()));

        buildAndPublishFileContentL =
            buildAndPublishFileContentL.replace("@ecr_link@", getConf().getDeploymentConfig().getEcrLink());

//        serviceContentL = serviceContentL.replace(NAME_SPACE, namespace);
        buildAndPublishFileContentL =
            buildAndPublishFileContentL.replace("@namespace@", getNamespace());

        writeToFile(getServiceName() + "/build_and_publish.sh", buildAndPublishFileContentL);
    }

    private void processConfigContent() {
        String configContentL = configContent;
    if (Boolean.TRUE.equals(getTemporalWorkerEnabled())) {
            configContentL = configContentL.replace("@temporal_namespace@", getTemporalWorkerNamespace());
            configContentL = configContentL.replace("@temporal_host_port@", getTemporalWorkerAddress());
        } else {
            configContentL = configContentL.replace("@temporal_namespace@", "");
            configContentL = configContentL.replace("@temporal_host_port@", "");
        }
//        writeToFile(getGoOverridePath() + "/config/config.json", configContentL);
        writeToFile(getServiceName()+ "/config/config.json", configContentL);
    }

    private void processGoModContent(String namespace) {
        String replaceGoModContract = "";
        if (flags.isContracts()) {
            replaceGoModContract =
                readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/go.mod.replacecontract.template");
            //            replaceGoModContract = replaceGoModContract.replace("@contract_path@",
            // contractPath);
            goModContent =
                goModContent.replace(
                    "@import_contract@", "code.nurture.farm/Core/Contracts " + getContractTag());
        } else {
            goModContent = goModContent.replace("@import_contract@", "");
        }
        if (Boolean.TRUE.equals(getTemporalWorkerEnabled())) {
            goModContent =
                goModContent.replace(
                    "@import_temporal@", "go.temporal.io/sdk v1.2.0\n\tgo.temporal.io/api v1.2.0");
        } else {
            goModContent = goModContent.replace("@import_temporal@", "");
        }
        goModContent = goModContent.replace("@replace_contract@", replaceGoModContract);
        goModContent = goModContent.replace(CODE_URL, super.getCodeUrl());
        goModContent = goModContent.replace(NAME_SPACE, namespace);
        goModContent = goModContent.replace(SERVICE_NAME, super.getServiceName());
        writeToFile(getServiceName() + "/go.mod", getGoModContent());
    }

    private void processDockerFile(String namespace) {
        String dockerFileContentL = getDockerFileContent();
        dockerFileContentL = dockerFileContentL.replace(NAME_SPACE, namespace);
        dockerFileContentL =
            dockerFileContentL.replace(SERVICE_NAME, CaseUtils.camelToSnake(getServiceName()));
        dockerFileContentL =
            dockerFileContentL.replace(
                SERVER_PORT, Integer.toString(super.getConf().getServer().getPort()));
        dockerFileContentL =
            dockerFileContentL.replace(
                PROMETHUES_PORT, Integer.toString(super.getConf().getPrometheus().getPort()));
        writeToFile(getServiceName() + "/Dockerfile", dockerFileContentL);
    }
}
