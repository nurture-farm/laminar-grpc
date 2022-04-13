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
import static farm.nurture.laminar.generator.Constants.EXECUTOR;
import static farm.nurture.laminar.generator.Constants.EXECUTOR_CONST;
import static farm.nurture.laminar.generator.Constants.FORWARD_SLASH;
import static farm.nurture.laminar.generator.Constants.GO_PACKAGE;
import static farm.nurture.laminar.generator.Constants.GO_PROTO_PATH;
import static farm.nurture.laminar.generator.Constants.NAME_SPACE;
import static farm.nurture.laminar.generator.Constants.ON_DATA;
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

public class AstServiceTest extends AstBase {

    private List<AstBase> parts = new ArrayList<>();
    private String serviceTestContent;
    private String serviceTestMockContent;
    private String serviceTestFuncContent;
    private String serviceTestMockBulkContent;
    private String serviceTestMockStructContent;
    private String serviceTestMockHookInsertContent;
    private String serviceTestMockHookSelectContent;

    private StringBuilder insertMockFunction = new StringBuilder();
    private StringBuilder insertTestFunction = new StringBuilder();
    private StringBuilder insertMockStruct = new StringBuilder();
    private StringBuilder insertMockHook = new StringBuilder();

    AstBase.Flags flags;

    public AstServiceTest() {
        serviceTestContent =
            readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/service.test.go.template");
        serviceTestMockContent =
            readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/service.test.mockfunction.template");
        serviceTestMockBulkContent =
            readTemplateFile(
                TemplatesVO.getTEMPLATE_DIR() + "/golang/service.test.mockfunctionbulk.template");
        serviceTestFuncContent =
            readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/service.test.function.template");
        serviceTestMockStructContent =
            readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/service.test.hookmockstruct.template");
        serviceTestMockHookInsertContent =
            readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/service.test.hookmockinsert.template");
        serviceTestMockHookSelectContent =
            readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/service.test.hookmockselect.template");
    }

    @Override
    public void onStart(Configuration conf, Flags flags) {
        super.onStart(conf, flags);
        this.flags = flags;
        for (AstBase b : parts) b.onStart(conf, flags);
    }

    @Override
    public void onEntry(
        AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields) {

        for (AstBase b : parts) b.onEntry(entry, requestFields, responseFields);
        if (entry.isDeclGrpc()) {
            ifIsDeclGrpc(entry);
            String insertLine;

            if (entry.isInsert()) {
                insertLine = insertMockFunction(serviceTestMockBulkContent, entry);
                insertMockFunction.append(insertLine);
                insertLine = serviceTestMockStructContent;
                insertLine = insertLine.replace(TITLE, entry.getTitle() + "Bulk");
                insertMockStruct.append(insertLine);
            }

            if (entry.isImplGrpc()) {
                ifIsImplGrpc(entry);
            }

            if (entry.isMutation()) {
                ifIsMutation(entry);
            } else {
                insertLine = serviceTestMockHookSelectContent;
                insertLine = insertLine.replace(TITLE, entry.getTitle());
                insertLine = insertLine.replace(SERVICE_REQ, entry.getReqName());
                insertLine = insertLine.replace(SERVICE_RES, entry.getResName());
                insertMockHook.append(insertLine);
            }
        }
    }

    private void ifIsImplGrpc(AppConfigVO entry) {
        String insertLine;
        if (entry.isInsert()) {
            insertLine = insertTestBulkFunction(serviceTestFuncContent, entry);
            insertTestFunction.append(insertLine);
        }
        insertLine = insertTestFunction(serviceTestFuncContent, entry);
        insertTestFunction.append(insertLine);
    }

    private void ifIsMutation(AppConfigVO entry) {
        String insertLine;
        insertLine = serviceTestMockHookInsertContent;
        insertLine = insertLine.replace(TITLE, entry.getTitle());
        insertLine = insertLine.replace(SERVICE_REQ, entry.getReqName());
        insertLine = insertLine.replace(EXECUTOR, entry.getTitle() + EXECUTOR_CONST);
        insertLine = insertLine.replace(SERVICE_RES, entry.getResName());
        insertMockHook.append(insertLine);
        if (entry.isInsert()) {
            insertLine = serviceTestMockHookInsertContent;
            insertLine = insertLine.replace(TITLE, entry.getTitle() + "Bulk");
            insertLine = insertLine.replace(EXECUTOR, entry.getTitle() + "BulkExecutor");
            insertLine = insertLine.replace(SERVICE_REQ, "Bulk" + entry.getReqName());
            insertLine = insertLine.replace(SERVICE_RES, "Bulk" + entry.getResName());
            insertMockHook.append(insertLine);
        }
    }

    private void ifIsDeclGrpc(AppConfigVO entry) {
        String insertLine = "";
        insertLine = insertMockFunction(serviceTestMockContent, entry);
        insertMockFunction.append(insertLine);
        insertLine = serviceTestMockStructContent;
        insertLine = insertLine.replace(TITLE, entry.getTitle());
        insertMockStruct.append(insertLine);
    }

    private String insertMockFunction(String executorContent, AppConfigVO entry) {

        String insertContent = executorContent.replace(TITLE, entry.getTitle());
        insertContent = insertContent.replace(SERVICE_REQ, entry.getReqName());
        insertContent = insertContent.replace(SERVICE_RES, entry.getResName());
        return insertContent;
    }

    private String insertTestFunction(String executorContent, AppConfigVO entry) {

        String insertContent = executorContent.replace(TITLE, entry.getTitle());
        if (entry.isMutation()) {
            insertContent = insertContent.replace(ON_DATA, "");
        } else {
            insertContent =
                insertContent.replace(
                    ON_DATA, "hookMock.On(\"OnData\", ctx, request, mockedResponse).Return(nil)");
        }

        insertContent = insertContent.replace(SERVICE_REQ, entry.getReqName());
        insertContent = insertContent.replace(SERVICE_RES, entry.getResName());
        insertContent = insertContent.replace(EXECUTOR, entry.getTitle() + EXECUTOR_CONST);
        insertContent = insertContent.replace("@executor_struct@", entry.getTitle() + EXECUTOR_CONST);

        return insertContent;
    }

    private String insertTestBulkFunction(String executorContent, AppConfigVO entry) {

        String insertContent = executorContent.replace(TITLE, entry.getTitle() + "Bulk");
        insertContent = insertContent.replace(ON_DATA, "");
        insertContent = insertContent.replace(SERVICE_REQ, "Bulk" + entry.getReqName());
        insertContent = insertContent.replace(SERVICE_RES, "Bulk" + entry.getResName());
        insertContent = insertContent.replace("@executor_struct@", entry.getTitle() + "ExecutorBulk");
        insertContent = insertContent.replace(EXECUTOR, "Bulk" + entry.getTitle() + EXECUTOR_CONST);
        return insertContent;
    }

    @Override
    public void onEnd() {
        super.onEnd();
        for (AstBase b : parts) b.onEnd();

        String namespace = super.getNamespace();
        if (StringUtils.isNonEmpty(namespace)) {
            namespace = FORWARD_SLASH + namespace;
        }
        File pj = new File(getGoGeneratePath() + "/service");
        if (!pj.exists()) pj.mkdirs();

        if (flags.isContracts()) {
            serviceTestContent =
                serviceTestContent.replace(
                    GO_PROTO_PATH,
                    "\"code.nurture.farm/Core/Contracts/"
                        + super.getServiceName()
                        + "/Gen/Go"
                        + super.getServiceName()
                        + "\"");
        } else {
            serviceTestContent =
                serviceTestContent.replace(
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
        getServiceTestContent(namespace);
        writeToFile(super.getGoGeneratePath() + "/service/service_test.go", serviceTestContent);
    }

    private void getServiceTestContent(String namespace) {
        serviceTestContent = serviceTestContent.replace(CODE_URL, super.getCodeUrl());
        serviceTestContent = serviceTestContent.replace(NAME_SPACE, namespace);
        serviceTestContent = serviceTestContent.replace(SERVICE_NAME, super.getServiceName());
        serviceTestContent = serviceTestContent.replace(GO_PACKAGE, super.getGoPackage());
        serviceTestContent =
            serviceTestContent.replace("@insert_mock_functions@", insertMockFunction.toString());
        serviceTestContent =
            serviceTestContent.replace("@insert_test_functions@", insertTestFunction.toString());
        serviceTestContent =
            serviceTestContent.replace("@insert_mock_struct@", insertMockStruct.toString());
        serviceTestContent =
            serviceTestContent.replace("@insert_mock_hooks@", insertMockHook.toString());
    }
}
