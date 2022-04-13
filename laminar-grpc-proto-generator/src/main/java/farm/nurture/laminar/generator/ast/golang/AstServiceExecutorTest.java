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
import java.util.Objects;

public class AstServiceExecutorTest extends AstBase {

    private List<AstBase> parts = new ArrayList<>();
    private String serviceExecutorTestContent;
    private String serviceExecutorTestInsertContent;
    private String serviceExecutorTestInsertBulkContent;
    private String serviceExecutorTestSelectContent;
    private String serviceExecutorTestUpdateContent;
    private String serviceExecutorTestDeleteContent;

    private StringBuilder insertTestFunction = new StringBuilder();

    private AstBase.Flags flags;

    public AstServiceExecutorTest() {
        serviceExecutorTestContent =
            readTemplateFile(
                TemplatesVO.getTEMPLATE_DIR()
                    + "/golang/database/executor/service_executor.test.go.template");
        serviceExecutorTestInsertContent =
            readTemplateFile(
                TemplatesVO.getTEMPLATE_DIR()
                    + "/golang/database/executor/service_executor.test.insert.template");
        serviceExecutorTestInsertBulkContent =
            readTemplateFile(
                TemplatesVO.getTEMPLATE_DIR()
                    + "/golang/database/executor/service_executor.test.insertbulk.template");
        serviceExecutorTestSelectContent =
            readTemplateFile(
                TemplatesVO.getTEMPLATE_DIR()
                    + "/golang/database/executor/service_executor.test.select.template");
        serviceExecutorTestUpdateContent =
            readTemplateFile(
                TemplatesVO.getTEMPLATE_DIR()
                    + "/golang/database/executor/service_executor.test.update.template");

        serviceExecutorTestDeleteContent =
            readTemplateFile(
                TemplatesVO.getTEMPLATE_DIR()
                    + "/golang/database/executor/service_executor.test.delete.template");
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
        if (entry.isDeclGrpc() && entry.isImplDao()) {

            if (entry.isMutation()) {

                if (Objects.equals(entry.getMutation(), "I")) {
                    insertTestFunction
                        .append(
                            insertContent(serviceExecutorTestInsertContent, entry, responseFields))
                        .append("\n");

                    insertTestFunction
                        .append(insertBulkContent(serviceExecutorTestInsertBulkContent, entry, responseFields))
                        .append("\n");

                } else if (Objects.equals(entry.getMutation(), "U")) {
                    insertTestFunction
                        .append(
                            insertContent(serviceExecutorTestUpdateContent, entry, responseFields))
                        .append("\n");
                } else if (Objects.equals(entry.getMutation(), "D")) {
                    insertTestFunction
                        .append(
                            insertContent(serviceExecutorTestDeleteContent, entry, responseFields))
                        .append("\n");
                } else if (entry.isMutation() && (entry.isInsert())) {
                    //
                    insertTestFunction
                        .append(insertBulkContent(serviceExecutorTestInsertBulkContent, entry,
                            responseFields))
                        .append("\n");
                }
            }

            else {
                insertTestFunction.append(
                        insertSelectContent(serviceExecutorTestSelectContent, entry, responseFields))
                    .append("\n");
            }
        }
    }

    private String insertContent(
        String executorContent, AppConfigVO entry, List<FieldDetail> responseFields) {

        String insertContent = executorContent.replace(TITLE, entry.getTitle());
        insertContent = insertContent.replace(SERVICE_REQ, entry.getReqName());
        StringBuilder args = new StringBuilder();
        int count = 0;
        for (int j = 0; j < entry.getSqlStmt().length(); j++) {
            if (entry.getSqlStmt().charAt(j) == '?') {
                count++;
            }
        }
        for (int i = 0; i < count; i++) {
            if (i == 0) {

                args.append("args[").append(i).append("]");
            } else {
                args.append(",args[").append(i).append("]");
            }
        }
        insertContent = insertContent.replace("@args@", args);
        return insertContent;
    }

    private String insertBulkContent(
        String executorContent, AppConfigVO entry, List<FieldDetail> responseFields) {

        String insertContent = executorContent.replace(TITLE, entry.getTitle());
        insertContent = insertContent.replace(SERVICE_REQ, entry.getReqName());
        StringBuilder args = new StringBuilder();
        int count = 0;
        for (int j = 0; j < entry.getSqlStmt().length(); j++) {
            if (entry.getSqlStmt().charAt(j) == '?') {
                count++;
            }
        }
        for (int i = 0; i < 2 * count; i = i + 2) {
            if (i == 0) {

                args.append("args[" + String.valueOf(i) + "]");
            } else {
                args.append(",args[" + String.valueOf(i) + "]");
            }
            args.append(",args[" + String.valueOf(i + 1) + "]");
        }
        insertContent = insertContent.replace("@args@", args);
        return insertContent;
    }

    private String insertSelectContent(
        String executorContent, AppConfigVO entry, List<FieldDetail> responseFields) {

        String insertContent = executorContent.replace(TITLE, entry.getTitle());
        insertContent = insertContent.replace(SERVICE_REQ, entry.getReqName());
        insertContent = insertContent.replace(SERVICE_RES, entry.getResName());
        StringBuilder colvalues = new StringBuilder();
        StringBuilder colnames = new StringBuilder();
        int i = 0;
        for (FieldDetail fieldDetail : responseFields) {
            if (i == 0) {
                colnames.append("\" \"");
                colvalues.append("nil");
            } else {
                colnames.append(",\" \"");
                colvalues.append(",nil");
            }
            i = i + 1;
        }
        insertContent = insertContent.replace("@colvalues@", colvalues);
        insertContent = insertContent.replace("@column_names@", colnames);
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

//        File pj = new File(getGoOverridePath() + "/database");
//        if (!pj.exists()) pj.mkdirs();

        String serviceExecutorTestContentL = getStringServiceExecutorTestContentL(namespace);

        File ej = new File(getGoGeneratePath() + "/database/executor");
        if (!ej.exists()) ej.mkdirs();

        writeToFile(
            super.getGoGeneratePath() + "/database/executor/service_executor_test.go",
            serviceExecutorTestContentL);
    }

    private String getStringServiceExecutorTestContentL(String namespace) {
        String serviceExecutorTestContentL =
            serviceExecutorTestContent.replace(CODE_URL, getCodeUrl());
        if (flags.isContracts()) {
            serviceExecutorTestContentL =
                serviceExecutorTestContentL.replace(
                    GO_PROTO_PATH,
                    "\"code.nurture.farm/Core/Contracts/"
                        + super.getServiceName()
                        + "/Gen/Go"
                        + super.getServiceName()
                        + "\"");
        } else {
            serviceExecutorTestContentL =
                serviceExecutorTestContentL.replace(
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
        serviceExecutorTestContentL = serviceExecutorTestContentL.replace(NAME_SPACE, namespace);
        serviceExecutorTestContentL =
            serviceExecutorTestContentL.replace(SERVICE_NAME, getServiceName());
        serviceExecutorTestContentL =
            serviceExecutorTestContentL.replace(
                "@insert_test_functions@", insertTestFunction.toString());
        return serviceExecutorTestContentL;
    }
}
