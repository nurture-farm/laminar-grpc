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
import static farm.nurture.laminar.generator.Constants.GO_PACKAGE;
import static farm.nurture.laminar.generator.Constants.GO_PROTO_PATH;
import static farm.nurture.laminar.generator.Constants.NAME_SPACE;
import static farm.nurture.laminar.generator.Constants.RESPONSE_STATUS_SUCCESS;
import static farm.nurture.laminar.generator.Constants.SERVICE_NAME;
import static farm.nurture.laminar.generator.Constants.SERVICE_REQ;
import static farm.nurture.laminar.generator.Constants.SERVICE_RES;
import static farm.nurture.laminar.generator.Constants.SQL_REPLACE;
import static farm.nurture.laminar.generator.Constants.TITLE;

import farm.nurture.infra.util.StringUtils;
import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.Configuration;
import farm.nurture.laminar.generator.FieldDetail;
import farm.nurture.laminar.generator.TemplatesVO;
import farm.nurture.laminar.generator.ast.AstBase;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AstServiceExecutor extends AstBase {

    private List<AstBase> parts = new ArrayList<>();

    private String queryContent;
    private String serviceExecutorContent;
    private String serviceExecutorSelectContent;
    private String serviceExecutorInsertContent;
    private String serviceExecutorBulkInsertContent;
    private String serviceExecutorMethodContent;
    private String serviceExecutorMethodBulkContent;
    private String responseStatusSuccessContent;
    private String responseContractStatusSuccessContent;

    private Boolean hasSelect = false;

    private StringBuilder queryContentBuf = new StringBuilder();
    private StringBuilder insertLines = new StringBuilder();
    private StringBuilder insertInterfaceLines = new StringBuilder();
    private StringBuilder insertMethodLines = new StringBuilder();

    AstBase.Flags flags;

    public AstServiceExecutor() {
        queryContent = readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/query.go.template");
        serviceExecutorContent =
            readTemplateFile(
                TemplatesVO.getTEMPLATE_DIR() + "/golang/database/executor/service_executor.go.template");
        serviceExecutorSelectContent =
            readTemplateFile(
                TemplatesVO.getTEMPLATE_DIR()
                    + "/golang/database/executor/service_executor.select.template");
        serviceExecutorInsertContent =
            readTemplateFile(
                TemplatesVO.getTEMPLATE_DIR()
                    + "/golang/database/executor/service_executor.insert.template");
        serviceExecutorMethodContent =
            readTemplateFile(
                TemplatesVO.getTEMPLATE_DIR()
                    + "/golang/database/executor/service_executor.method.template");
        serviceExecutorMethodBulkContent =
            readTemplateFile(
                TemplatesVO.getTEMPLATE_DIR()
                    + "/golang/database/executor/service_executor.methodBulk.template");
        serviceExecutorBulkInsertContent =
            readTemplateFile(
                TemplatesVO.getTEMPLATE_DIR()
                    + "/golang/database/executor/service_executor.bulkinsert.template");
        responseStatusSuccessContent =
            readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/common.status.success.template");
        responseContractStatusSuccessContent =
            readTemplateFile(
                TemplatesVO.getTEMPLATE_DIR() + "/golang/common.contract.status.success.template");
    }

    @Override
    public void onStart(Configuration conf, Flags flags) {
        super.onStart(conf, flags);
        this.flags = flags;
        for (AstBase b : parts) b.onStart(conf, flags);
        if (flags.isContracts()) {
            serviceExecutorSelectContent =
                serviceExecutorSelectContent.replace(
                    RESPONSE_STATUS_SUCCESS,
                    responseContractStatusSuccessContent.substring(
                        0, responseContractStatusSuccessContent.length() - 2));
            serviceExecutorInsertContent =
                serviceExecutorInsertContent.replace(
                    RESPONSE_STATUS_SUCCESS, responseContractStatusSuccessContent);
            serviceExecutorBulkInsertContent =
                serviceExecutorBulkInsertContent.replace(
                    RESPONSE_STATUS_SUCCESS, responseContractStatusSuccessContent);
        } else {
            serviceExecutorSelectContent =
                serviceExecutorSelectContent.replace(
                    RESPONSE_STATUS_SUCCESS,
                    responseStatusSuccessContent.substring(0, responseStatusSuccessContent.length() - 2));
            serviceExecutorInsertContent =
                serviceExecutorInsertContent.replace(
                    RESPONSE_STATUS_SUCCESS, responseStatusSuccessContent);
            serviceExecutorBulkInsertContent =
                serviceExecutorBulkInsertContent.replace(
                    RESPONSE_STATUS_SUCCESS, responseStatusSuccessContent);
        }
    }

    @Override
    public void onEntry(
        AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields) {

        for (AstBase b : parts) b.onEntry(entry, requestFields, responseFields);

        if (entry.getSqlStmt() != null) {
            String queryConstant = "QUERY_" + (entry.getTitle()).toUpperCase() + " = \"" + entry.getSqlStmt() + "\"\n\t";
            queryContentBuf.append(queryConstant);
        }

        if (entry.isImplDao()) {
            if (entry.isMutation()) {
                insertLines.append(
                        insertContent(serviceExecutorInsertContent, entry, responseFields))
                    .append("\n");
                if (entry.isInsert()) {
                    insertLines.append(
                            insertContent(serviceExecutorBulkInsertContent, entry, responseFields))
                        .append("\n");
                    insertInterfaceLines.append(insertInterfaceBulk(entry)).append("\n");
                    insertMethodLines.append(
                        insertMethodBulk(serviceExecutorMethodBulkContent, entry)).append("\n");
                }
            } else {
                hasSelect = true;
                insertLines.append(
                        insertContent(serviceExecutorSelectContent, entry, responseFields))
                    .append("\n");
            }
            insertInterfaceLines.append(insertInterface(entry)).append("\n");
            insertMethodLines.append(insertMethod(serviceExecutorMethodContent, entry))
                .append("\n");
        }
    }

    private String insertContent(
        String executorContent, AppConfigVO entry, List<FieldDetail> responseFields) {

        String insertContent = executorContent.replace(TITLE, entry.getTitle());
        insertContent = insertContent.replace("@title_query@", (entry.getTitle()).toUpperCase());
        insertContent = insertContent.replace(SERVICE_REQ, entry.getReqName());
        if (entry.isSqlUniquekey()) {
            insertContent = insertContent.replace("@begin_appendrecord@", "");
            insertContent = insertContent.replace("@end_appendrecord@", "");
        } else {
            insertContent = insertContent.replace("@begin_appendrecord@", "append(response.Records, ");
            insertContent = insertContent.replace("@end_appendrecord@", ")");
        }
        insertContent = insertContent.replace(SERVICE_RES, entry.getResName());
        if (entry.isInsert()) {
            insertContent = insertContent.replace(SQL_REPLACE, "");
            insertContent =
                insertContent.replace(
                    "@place_holders@",
                    entry.getSqlStmt().substring(entry.getSqlStmt().indexOf("(?")).trim());
        } else if (!entry.isMutation()) {
            // sql replace
            StringBuilder sqlReplaceConent = new StringBuilder();
            replaceSql(entry.getSqlReplaces(), sqlReplaceConent, entry.getSqlParams());
            insertContent = insertContent.replace(SQL_REPLACE, sqlReplaceConent.toString());

            // make model
            StringBuilder fields = new StringBuilder();
            boolean isFilled = false;
            for (FieldDetail fieldDetail : responseFields) {
                String fldName = fieldDetail.getFldNameCamel();
                if (isFilled) fields.append(",");
                else isFilled = true;
                String paramName = Character.toUpperCase(fldName.charAt(0)) + fldName.substring(1);
                fields.append("&model.").append(paramName);
            }
            insertContent = insertContent.replace("@response_fields@", fields.toString());
        } else {
            // UD operation
            // sql replace
            StringBuilder sqlReplaceConent = new StringBuilder();
            replaceSql(entry.getSqlReplaces(), sqlReplaceConent, entry.getSqlParams());
            insertContent = insertContent.replace(SQL_REPLACE, sqlReplaceConent.toString());
        }
        return insertContent;
    }

    private String insertMethod(String executorContent, AppConfigVO entry) {
        String insertMethod = executorContent.replace(TITLE, entry.getTitle());
        insertMethod = insertMethod.replace(SERVICE_REQ, entry.getReqName());
        insertMethod = insertMethod.replace(SERVICE_RES, entry.getResName());
        return insertMethod;
    }

    private String insertInterface(AppConfigVO entry) {
        String insertInterface =
            "    Execute@title@(ctx context.Context, request *fs.@service_request@) (*fs.@service_response@, error)";
        insertInterface = insertInterface.replace(SERVICE_REQ, entry.getReqName());
        insertInterface = insertInterface.replace(SERVICE_RES, entry.getResName());
        insertInterface = insertInterface.replace(TITLE, entry.getTitle());
        return insertInterface;
    }

    private String insertMethodBulk(String executorContent, AppConfigVO entry) {
        String insertMethod = executorContent.replace(TITLE, entry.getTitle());
        insertMethod = insertMethod.replace(SERVICE_REQ, entry.getReqName());
        insertMethod = insertMethod.replace(SERVICE_RES, entry.getResName());
        return insertMethod;
    }

    private String insertInterfaceBulk(AppConfigVO entry) {
        String insertInterface =
            "    Execute@title@Bulk(ctx context.Context, bulkrequest *fs.Bulk@service_request@) (*fs.Bulk@service_response@, error)";
        insertInterface = insertInterface.replace(SERVICE_REQ, entry.getReqName());
        insertInterface = insertInterface.replace(SERVICE_RES, entry.getResName());
        insertInterface = insertInterface.replace(TITLE, entry.getTitle());
        return insertInterface;
    }

    public void replaceSql(
        Map<String, String> sqlReplaces, StringBuilder sqlReplaceConent, String sqlParams) {

        Set<String> sqlParamFields = new HashSet<>();
        List<String> Params = StringUtils.fastSplit(sqlParams, ',');
        if (Params != null) {
            for (String Param : Params) {
                String StrTrimmed = Param.trim();
                int cutPos = StrTrimmed.indexOf(':');
                String Name = StrTrimmed.substring(0, cutPos);
                sqlParamFields.add(Name);
            }
        }
        processSqlReplaces(sqlReplaces, sqlReplaceConent, sqlParamFields);
    }

    private void processSqlReplaces(
        Map<String, String> sqlReplaces, StringBuilder sqlReplaceConent, Set<String> sqlParamFields) {
        for (Map.Entry<String, String> aReplace : sqlReplaces.entrySet()) {

            String fldName = aReplace.getKey();
            String getFldMethodSuffix = Character.toUpperCase(fldName.charAt(0)) + fldName.substring(1);

            String aReplaceValue = aReplace.getValue().trim();
            String replaceVal = "val" + getFldMethodSuffix;
            if (aReplaceValue.startsWith("repeated ")) {

                sqlReplaceConent.append(replaceVal).append(" := \"\"\n\t");
                if (sqlParamFields.contains(fldName)) {
                    sqlReplaceConent
                        .append("for _ , _  = range request.")
                        .append(getFldMethodSuffix)
                        .append(" {\n\t\t");
                    sqlReplaceConent.append("if ").append(replaceVal).append(" == \"\" {\n\t\t\t");
                    sqlReplaceConent.append(replaceVal).append(" = \"?\"\n\t\t} else {\n\t\t\t");
                    sqlReplaceConent.append(replaceVal).append("+= \",?\"\n\t\t}\n\t}\n\t");
                } else {
                    sqlReplaceConent
                        .append("for _ , requestId  := range request.")
                        .append(getFldMethodSuffix)
                        .append(" {\n\t\t");
                    sqlReplaceConent.append("if ").append(replaceVal).append(" == \"\" {\n\t\t\t");
                    sqlReplaceConent
                        .append(replaceVal)
                        .append(" = cast.ToString(requestId)\n\t\t} else {\n\t\t\t");
                    sqlReplaceConent
                        .append(replaceVal)
                        .append("+= \",\"+cast.ToString(requestId)\n\t\t}\n\t}\n\t");
                }
                sqlReplaceConent
                    .append("query = strings.ReplaceAll(query, \"").append("@").append(fldName)
                    .append("@").append("\", ").append(replaceVal).append(")");

            } else {

                // TO-DO:
            }
        }
    }

    @Override
    public void onEnd() {
        super.onEnd();
        for (AstBase b : parts) b.onEnd();

        String namespace = super.getNamespace();
        if (StringUtils.isNonEmpty(namespace)) {
            namespace = FORWARD_SLASH + namespace;
        }

        File pj = new File(getGoGeneratePath() + "/database");
        if (!pj.exists()) pj.mkdirs();

        String queryContentL = queryContent.replace("@insert_stmts@", queryContentBuf.toString());
        writeToFile(super.getGoGeneratePath() + "/database/query.go", queryContentL);

        String importModel = "";
        String importEntSql = "";
        if (Boolean.TRUE.equals(hasSelect)) {
            importModel =
                "\""
                    + super.getCodeUrl()
                    + namespace
                    + FORWARD_SLASH
                    + super.getServiceName()
                    + "/zerotouch/golang/database/models\"";
            importEntSql = "entsql \"github.com/facebook/ent/dialect/sql\"";
        }

        String serviceExecutorContentL = getStringServiceExecutorContentL(namespace, importModel, importEntSql);

        File ej = new File(getGoGeneratePath() + "/database/executor");
        if (!ej.exists()) ej.mkdirs();

        writeToFile(
            super.getGoGeneratePath() + "/database/executor/service_executor.go",
            serviceExecutorContentL);
    }

    private String getStringServiceExecutorContentL(String namespace, String importModel, String importEntSql) {
        String serviceExecutorContentL = serviceExecutorContent.replace(CODE_URL, getCodeUrl());
        serviceExecutorContentL = processIsContracts(namespace, serviceExecutorContentL);
        serviceExecutorContentL = serviceExecutorContentL.replace(NAME_SPACE, namespace);
        serviceExecutorContentL = serviceExecutorContentL.replace(SERVICE_NAME, getServiceName());
        serviceExecutorContentL = serviceExecutorContentL.replace(GO_PACKAGE, super.getGoPackage());
        serviceExecutorContentL =
            serviceExecutorContentL.replace("@execute_insert@", insertLines.toString());
        serviceExecutorContentL =
            serviceExecutorContentL.replace(
                "@execute_insert_interface@", insertInterfaceLines.toString());
        serviceExecutorContentL =
            serviceExecutorContentL.replace("@execute_insert_methods@", insertMethodLines.toString());
        serviceExecutorContentL = serviceExecutorContentL.replace("@import_model@", importModel);
        serviceExecutorContentL = serviceExecutorContentL.replace("@import_entsql@", importEntSql);
        return serviceExecutorContentL;
    }

    private String processIsContracts(String namespace, String serviceExecutorContentL) {
        if (flags.isContracts()) {
            serviceExecutorContentL =
                serviceExecutorContentL.replace(
                    GO_PROTO_PATH,
                    "\"code.nurture.farm/Core/Contracts/"
                        + super.getServiceName()
                        + "/Gen/Go"
                        + super.getServiceName()
                        + "\"");
            //                            + "\"\n\tcommon
            // \"code.nurture.farm/Core/Contracts/Common/Gen/GoCommon\"");
        } else {
            serviceExecutorContentL =
                serviceExecutorContentL.replace(
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
        return serviceExecutorContentL;
    }
}
