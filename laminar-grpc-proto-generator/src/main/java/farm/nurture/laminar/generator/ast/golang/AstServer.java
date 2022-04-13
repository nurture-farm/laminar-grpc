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
import static farm.nurture.laminar.generator.Constants.GO_PROTO_PATH;
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

public class AstServer extends AstBase {

    private List<AstBase> parts = new ArrayList<>();
    private String serverContent;
    private String serverTemporalContent;
    private String serverInsertContent;
    private String serverBulkInsertContent;
    private StringBuilder insertLinesBuf = new StringBuilder();
    private AstBase.Flags flags;

    public AstServer() {
        serverContent = readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/server.go.template");
        serverTemporalContent =
            readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/server.temporal.go.template");
        serverInsertContent =
            readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/server.insert.template");
        serverBulkInsertContent =
            readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/server.bulkinsert.template");
        parts.add(new AstActivities());
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
            insertLinesBuf.append(insertContent(serverInsertContent, entry)).append("\n");
            if (entry.getMutation().equals("I")) {
                insertLinesBuf.append(insertContent(serverBulkInsertContent, entry)).append("\n");
            }
        }
    }

    private String insertContent(String executorContent, AppConfigVO entry) {

        String insertContent = executorContent.replace(SERVICE_NAME, getServiceName());
        insertContent = insertContent.replace(TITLE, entry.getTitle());
        insertContent = insertContent.replace(SERVICE_REQ, entry.getReqName());
        insertContent = insertContent.replace(CODE_URL, getCodeUrl());
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
            namespace = namespace + FORWARD_SLASH;
        }

        String serverContentL = serverContent;
        serverContentL = processIsContracts(namespace, serverContentL);
        serverContentL = getString(namespace, serverContentL);

        writeToFile(super.getGoGeneratePath() + "/setup/server.go", serverContentL);
    }

    private String getString(String namespace, String serverContentL) {
        if (Boolean.TRUE.equals(getTemporalWorkerEnabled())) {
            serverContentL =
                serverContentL.replace(
                    "@service_import@",
                    "\"github.com/spf13/viper\"\n\t\"code.nurture.farm/"
                        + namespace
                        + getServiceName()
                        + "/zerotouch/golang/database\"\n\t\"go.temporal.io/sdk/client\"\n\t\"go.uber.org/zap\"");
            serverContentL =
                serverContentL.replace("@activities_name@", "s" + super.getServiceName() + "Activities");
            serverContentL = serverContentL.replace("@workflow_client@", "wfClient client.Client");
            serverContentL = serverContentL.replace("@workflow_client_set@", "wfClient: WorkflowClient,");
            serverContentL = serverContentL.replace("@insert_temporalsetup@", serverTemporalContent);
        } else {
            serverContentL =
                serverContentL.replace(
                    "@service_import@",
                    "\""
                        + super.getCodeUrl()
                        + FORWARD_SLASH
                        + namespace
                        + super.getServiceName()
                        + "/zerotouch/golang/service\"");
            serverContentL = serverContentL.replace("@activities_name@", "");
            serverContentL = serverContentL.replace("@workflow_client@", "");
            serverContentL = serverContentL.replace("@workflow_client_set@", "");
            serverContentL = serverContentL.replace("@insert_temporalsetup@", "");
        }
        serverContentL = serverContentL.replace(SERVICE_NAME, super.getServiceName());
        serverContentL = serverContentL.replace("@insertservice@", insertLinesBuf.toString());
        serverContentL = getStringReturnPackage(serverContentL);
        return serverContentL;
    }

    private String getStringReturnPackage(String serverContentL) {
        if (Boolean.TRUE.equals(getTemporalWorkerEnabled())) {
            serverContentL =
                serverContentL.replace(
                    "@return_package@", "fs.s" + super.getServiceName() + "Activities");
            serverContentL = serverContentL.replace("@return_nil@", "");
        } else {
            serverContentL = serverContentL.replace("@return_package@", "service");
            serverContentL = serverContentL.replace("@return_nil@", ", nil");
        }
        return serverContentL;
    }

    private String processIsContracts(String namespace, String serverContentL) {
        if (flags.isContracts()) {
            serverContentL =
                serverContentL.replace(
                    GO_PROTO_PATH,
                    "\"code.nurture.farm/Core/Contracts/"
                        + super.getServiceName()
                        + "/Gen/Go"
                        + super.getServiceName()
                        + "\"");

        } else {
            serverContentL =
                serverContentL.replace(
                    GO_PROTO_PATH,
                    "\""
                        + super.getCodeUrl()
                        + FORWARD_SLASH
                        + namespace
                        + super.getServiceName()
                        + "/zerotouch/golang/proto/"
                        + super.getGoPackage()
                        + FORWARD_SLASH
                        + super.getServiceName()
                        + "\"");
        }
        return serverContentL;
    }
}
