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

import static farm.nurture.laminar.generator.Constants.NAME_SPACE;
import static farm.nurture.laminar.generator.Constants.PROMETHUES_PORT;
import static farm.nurture.laminar.generator.Constants.SERVER_PORT;
import static farm.nurture.laminar.generator.Constants.SERVICE_NAME_KEBAB;

import farm.nurture.laminar.core.util.CaseUtils;
import farm.nurture.laminar.generator.TemplatesVO;
import farm.nurture.laminar.generator.ast.AstBase;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AstDeployments extends AstBase {

    List<AstBase> parts = new ArrayList<>();

    String serviceContent;
    String virtualServiceContent;
    String devDeploymentContent;
    String stageDeploymentContent;
    String prodDeploymentContent;

    public AstDeployments() {
        serviceContent =
            readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/deployments/service.yaml.template");
        virtualServiceContent =
            readTemplateFile(
                TemplatesVO.getTEMPLATE_DIR() + "/golang/deployments/virtualService.yaml.template");
        devDeploymentContent =
            readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/deployments/deployment.yaml.template");
        stageDeploymentContent =
            readTemplateFile(
                TemplatesVO.getTEMPLATE_DIR() + "/golang/deployments/deploymentStage.yaml.template");
        prodDeploymentContent =
            readTemplateFile(
                TemplatesVO.getTEMPLATE_DIR() + "/golang/deployments/deploymentProd.yaml.template");
    }

    @Override
    public void onEnd() {

        super.onEnd();
        for (AstBase b : parts) b.onEnd();

        File pj = new File(getServiceName() + "/deployments");
        if (!pj.exists()) pj.mkdirs();

        String namespace = super.getNamespace();

        String serviceContentL = serviceContent.replace(SERVICE_NAME_KEBAB, CaseUtils.camelToKebab(getServiceName()));
        serviceContentL = serviceContentL.replace(NAME_SPACE, namespace);
        serviceContentL = serviceContentL.replace(SERVER_PORT, Integer.toString(super.getConf().getServer().getPort()));
        writeToFile(getServiceName() + "/deployments/service.yaml", serviceContentL);

        String virtualServiceContentL = virtualServiceContent.replace(SERVICE_NAME_KEBAB, CaseUtils.camelToKebab(getServiceName()));
        virtualServiceContentL = virtualServiceContentL.replace(NAME_SPACE, namespace);
        virtualServiceContentL = virtualServiceContentL.replace(SERVER_PORT, Integer.toString(super.getConf().getServer().getPort()));
        virtualServiceContentL = virtualServiceContentL.replace("@proto_package@", getProtoPackage());
        writeToFile(getServiceName() + "/deployments/virtualService.yaml", virtualServiceContentL);

        String devDeploymentContentL = devDeploymentContent.replace(SERVICE_NAME_KEBAB, CaseUtils.camelToKebab(getServiceName()));
        devDeploymentContentL = devDeploymentContentL.replace(NAME_SPACE, namespace);
        devDeploymentContentL = devDeploymentContentL.replace(PROMETHUES_PORT, Integer.toString(super.getConf().getPrometheus().getPort()));
        devDeploymentContentL = devDeploymentContentL.replace(SERVER_PORT, Integer.toString(super.getConf().getPrometheus().getPort()));
        devDeploymentContentL = devDeploymentContentL.replace("@ecr_link@", getConf().getDeploymentConfig().getEcrLink());
        writeToFile(getServiceName() + "/deployments/deployment.yaml", devDeploymentContentL);

        String devDeploymentStageContentL = stageDeploymentContent.replace(SERVICE_NAME_KEBAB, CaseUtils.camelToKebab(getServiceName()));
        devDeploymentStageContentL = devDeploymentStageContentL.replace(NAME_SPACE, namespace);
        devDeploymentStageContentL = devDeploymentStageContentL.replace(PROMETHUES_PORT, Integer.toString(super.getConf().getPrometheus().getPort()));
        devDeploymentStageContentL = devDeploymentStageContentL.replace(SERVER_PORT, Integer.toString(super.getConf().getPrometheus().getPort()));
        devDeploymentStageContentL = devDeploymentStageContentL.replace("@ecr_link@", getConf().getDeploymentConfig().getEcrLink());
        writeToFile(getServiceName() + "/deployments/deploymentStage.yaml", devDeploymentStageContentL);

        String deployProdContentL = prodDeploymentContent.replace(SERVICE_NAME_KEBAB, CaseUtils.camelToKebab(getServiceName()));
        deployProdContentL = deployProdContentL.replace(NAME_SPACE, namespace);
        deployProdContentL = deployProdContentL.replace(PROMETHUES_PORT, Integer.toString(super.getConf().getPrometheus().getPort()));
        deployProdContentL = deployProdContentL.replace(SERVER_PORT, Integer.toString(super.getConf().getPrometheus().getPort()));
        deployProdContentL = deployProdContentL.replace("@ecr_link@", getConf().getDeploymentConfig().getEcrLink());
        writeToFile(getServiceName() + "/deployments/deploymentProd.yaml", deployProdContentL);
    }

    //    private String getAcroNymSmall(String serviceName) {
    //
    //        String acronym = "";
    //        for(int i = 0; i < serviceName.length(); i++){
    //            if(Character.isUpperCase(serviceName.charAt(i))){
    //                char w = serviceName.charAt(i);
    //                w = Character.toLowerCase(w);
    //                acronym+=w;
    //            }
    //        }
    //        return acronym;
    //    }
}
