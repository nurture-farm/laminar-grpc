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
import static farm.nurture.laminar.generator.Constants.ENUM;
import static farm.nurture.laminar.generator.Constants.FORWARD_SLASH;
import static farm.nurture.laminar.generator.Constants.GO_PACKAGE;
import static farm.nurture.laminar.generator.Constants.NAME_SPACE;
import static farm.nurture.laminar.generator.Constants.PROTO_TYPE_REPEATED;
import static farm.nurture.laminar.generator.Constants.SERVICE_NAME;
import static farm.nurture.laminar.generator.Constants.SERVICE_REQ;
import static farm.nurture.laminar.generator.Constants.TITLE;

import farm.nurture.infra.util.Logger;
import farm.nurture.infra.util.LoggerFactory;
import farm.nurture.infra.util.StringUtils;
import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.Configuration;
import farm.nurture.laminar.generator.FieldDetail;
import farm.nurture.laminar.generator.TemplatesVO;
import farm.nurture.laminar.generator.ast.AstBase;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AstHelper extends AstBase {

    private List<AstBase> parts = new ArrayList<>();
    private String helperContent;
    private String helperSelectContent;
    private String helperInsertContent;
    private Boolean importProto = false;
    private StringBuilder insertLinesBuf = new StringBuilder();
    private AstBase.Flags flags;
    private static final Logger logger = LoggerFactory.getLogger(AstHelper.class);

    public AstHelper() {
        helperContent =
            readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/database/executor/helper.go.template");
        helperSelectContent =
            readTemplateFile(
                TemplatesVO.getTEMPLATE_DIR() + "/golang/database/executor/helper.select.template");
        helperInsertContent =
            readTemplateFile(
                TemplatesVO.getTEMPLATE_DIR() + "/golang/database/executor/helper.insert.template");
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

        if (entry.isImplDao()) {
            StringBuilder voVariableBuf = new StringBuilder();
            Boolean isMutation = entry.isMutation();
            parameterHandling(
                isMutation, entry.getReqName(), entry.getResName(), requestFields, voVariableBuf);

            String helperInsertContentL;
            if (Boolean.TRUE.equals(isMutation)) {
                helperInsertContentL = helperInsertContent.replace(TITLE, entry.getTitle());
                helperInsertContentL = helperInsertContentL.replace(SERVICE_REQ, entry.getReqName());
                helperInsertContentL = helperInsertContentL.replace("@voVariablesAppend@", voVariableBuf);
            } else {
                importProto = true;
                helperInsertContentL = helperSelectContent.replace(TITLE, entry.getTitle());
                helperInsertContentL = helperInsertContentL.replace(SERVICE_REQ, entry.getReqName());
                helperInsertContentL =
                    helperInsertContentL.replace("@requestVariablesAppend@", voVariableBuf);

                StringBuilder voVariableBufReq = new StringBuilder();
                parameterHandling(
                    true, entry.getReqName(), entry.getResName(), requestFields, voVariableBufReq);
                helperInsertContentL = helperInsertContentL.replace("@voVariablesAppend@", voVariableBufReq);


            }

            insertLinesBuf.append(helperInsertContentL);
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

        File pj = new File(getGoGeneratePath() + "/database/executor");
        if (!pj.exists()) pj.mkdirs();

        String importProtoPath = "";
        if (Boolean.TRUE.equals(importProto)) {
            if (flags.isContracts()) {
                importProtoPath =
                    "fs \"code.nurture.farm/Core/Contracts/"
                        + super.getServiceName()
                        + "/Gen/Go"
                        + super.getServiceName()
                        + "\"";
            } else {
                importProtoPath =
                    "fs \""
                        + super.getCodeUrl()
                        + namespace
                        + FORWARD_SLASH
                        + super.getServiceName()
                        + "/zerotouch/golang/proto/"
                        + super.getGoPackage()
                        + FORWARD_SLASH
                        + super.getServiceName()
                        + "\"";
            }
        }
        String helperContentL = getStringHelperContentL(namespace, importProtoPath);
        writeToFile(super.getGoGeneratePath() + "/database/executor/helper.go", helperContentL);
    }

    private String getStringHelperContentL(String namespace, String importProtoPath) {
        String helperContentL = helperContent.replace(CODE_URL, super.getCodeUrl());
        helperContentL = helperContentL.replace(NAME_SPACE, namespace);
        helperContentL = helperContentL.replace(SERVICE_NAME, super.getServiceName());
        helperContentL = helperContentL.replace(GO_PACKAGE, super.getGoPackage());
        helperContentL = helperContentL.replace("@inserthelper@", insertLinesBuf.toString());
        helperContentL = helperContentL.replace("@import_proto@", importProtoPath);
        return helperContentL;
    }

    void parameterHandling(
        Boolean isMutation,
        String requestName,
        String responseName,
        List<FieldDetail> paramDetails,
        StringBuilder voVariableBuf) {

        logger.info("paramDetails = {}",paramDetails.toString());
        if (!paramDetails.isEmpty()) {

            boolean isFirst = true;
            for (FieldDetail aParam : paramDetails) {
                String fldName = aParam.getFldNameCamel();
                if (isFirst) isFirst = false;
                else voVariableBuf.append("\t");

                String paramName = Character.toUpperCase(fldName.charAt(0)) + fldName.substring(1);

                if (Boolean.TRUE.equals(isMutation)) {
                    if (aParam.getProtoType().startsWith(PROTO_TYPE_REPEATED)) {
                        voVariableBuf
                            .append("for _, id := range model.")
                            .append(paramName)
                            .append(" {\n\t\targs = append(args, id)")
                            .append("\n\t}");
                        continue;
                    } else {
                        voVariableBuf.append("args = append(args, model.");
                    }
                } else {
                    if (aParam.getProtoType().startsWith(PROTO_TYPE_REPEATED)) {
                        voVariableBuf
                            .append("for _, id := range request.")
                            .append(paramName)
                            .append(" {\n\t\targs = append(args, id)")
                            .append("\n\t}");
                        continue;
                    } else {
                        voVariableBuf.append("args = append(args, request.");
                    }
                }
                voVariableBuf.append(paramName);
                if (aParam.getGoType().equals(ENUM) && Boolean.FALSE.equals(isMutation)) {
                    voVariableBuf.append(".String()");
                }
                voVariableBuf.append(")\n");
            }
        }
    }
}
