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

import static farm.nurture.laminar.generator.Constants.ACTIVITIES_BULKINSERT_TEMPLATE;
import static farm.nurture.laminar.generator.Constants.ACTIVITIES_GO_TEMPLATE;
import static farm.nurture.laminar.generator.Constants.ACTIVITIES_INSERT_GO_TEMPLATE;
import static farm.nurture.laminar.generator.Constants.FORWARD_SLASH;
import static farm.nurture.laminar.generator.Constants.GO_PROTO_PATH;
import static farm.nurture.laminar.generator.Constants.NAME_SPACE;
import static farm.nurture.laminar.generator.Constants.SERVICE_NAME;
import static farm.nurture.laminar.generator.Constants.SERVICE_REQ;
import static farm.nurture.laminar.generator.Constants.SERVICE_RES;
import static farm.nurture.laminar.generator.Constants.TITLE;

import farm.nurture.infra.util.StringUtils;
import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.Configuration;
import farm.nurture.laminar.generator.FieldDetail;
import farm.nurture.laminar.generator.TemplatesVO;
import farm.nurture.laminar.generator.ast.AstBase;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AstActivities extends AstBase {

    private List<AstBase> parts = new ArrayList<>();

    private String activityContent;
    private String activiitiesInsertActivity;
    private String activitiesBulkInsertActivity;
    private StringBuilder activityContentBuf = new StringBuilder();

    AstBase.Flags flags;

    public AstActivities() {
        activityContent = readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + ACTIVITIES_GO_TEMPLATE);
        activiitiesInsertActivity =
            readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + ACTIVITIES_INSERT_GO_TEMPLATE);
        activitiesBulkInsertActivity =
            readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + ACTIVITIES_BULKINSERT_TEMPLATE);
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

        if (entry.isDeclGrpc()) {
            activityContentBuf.append(insertContent(activiitiesInsertActivity, entry) + "\n");
            if (entry.getMutation().equals("I")) {
                activityContentBuf.append(insertContent(activitiesBulkInsertActivity, entry) + "\n");
            }
        }
    }

    private String insertContent(String executorContent, AppConfigVO entry) {

        String insertContent = executorContent.replace(SERVICE_NAME, getServiceName());
        insertContent = insertContent.replace(TITLE, entry.getTitle());
        insertContent = insertContent.replace(SERVICE_REQ, entry.getReqName());
        insertContent = insertContent.replace(SERVICE_RES, entry.getResName());
        return insertContent;
    }

    @Override
    public void onEnd() {

        super.onEnd();
        for (AstBase b : parts) b.onEnd();

        File pj = new File(getGoGeneratePath() + "/setup");
        if (!pj.exists()) pj.mkdirs();

        String namespace = super.getNamespace();
        if (StringUtils.isNonEmpty(namespace)) {
            namespace = FORWARD_SLASH + namespace;
        }

        String activityContentL = activityContent.replace(SERVICE_NAME, super.getServiceName());
        activityContentL = processIsContracts(namespace, activityContentL);

        if (Boolean.TRUE.equals(getTemporalWorkerEnabled())) {
            activityContentL =
                activityContentL.replace("@insert_activtiesp@", activityContentBuf.toString());
            activityContentL = activityContentL.replace(NAME_SPACE, namespace);
            writeToFile(super.getGoGeneratePath() + "/setup/activities.go", activityContentL);
        }
    }

    private String processIsContracts(String namespace, String activityContentL) {
        if (flags.isContracts()) {
            activityContentL =
                activityContentL.replace(
                    GO_PROTO_PATH,
                    "\"code.nurture.farm/Core/Contracts/"
                        + super.getServiceName()
                        + "/Gen/Go"
                        + super.getServiceName()
                        + "\"");
        } else {
            activityContentL =
                activityContentL.replace(
                    GO_PROTO_PATH,
                    "\""
                        + super.getCodeUrl()
                        + namespace
                        + FORWARD_SLASH
                        + super.getServiceName()
                        + "/zerotouch/golang/proto/"
                        + super.getGoPackage()
                        + FORWARD_SLASH
                        + super.getServiceName()
                        + "\"");
        }
        return activityContentL;
    }
}
