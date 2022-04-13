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

import static farm.nurture.laminar.generator.Constants.BOOL;
import static farm.nurture.laminar.generator.Constants.CODE_URL;
import static farm.nurture.laminar.generator.Constants.DATETIME;
import static farm.nurture.laminar.generator.Constants.ENUM;
import static farm.nurture.laminar.generator.Constants.FLOAT64;
import static farm.nurture.laminar.generator.Constants.FORWARD_SLASH;
import static farm.nurture.laminar.generator.Constants.GO_PACKAGE;
import static farm.nurture.laminar.generator.Constants.GO_PROTO_PATH;
import static farm.nurture.laminar.generator.Constants.INT32;
import static farm.nurture.laminar.generator.Constants.INT64;
import static farm.nurture.laminar.generator.Constants.MODEL_DOT;
import static farm.nurture.laminar.generator.Constants.NAME_SPACE;
import static farm.nurture.laminar.generator.Constants.SERVICE_NAME;
import static farm.nurture.laminar.generator.Constants.STRING;
import static farm.nurture.laminar.generator.Constants.TIMESTAMP;

import farm.nurture.infra.util.Logger;
import farm.nurture.infra.util.LoggerFactory;
import farm.nurture.infra.util.StringUtils;
import farm.nurture.laminar.core.util.CaseUtils;
import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.Configuration;
import farm.nurture.laminar.generator.FieldDetail;
import farm.nurture.laminar.generator.TemplatesVO;
import farm.nurture.laminar.generator.ast.AstBase;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AstMakeVO extends AstBase {

    private List<AstBase> parts = new ArrayList<>();
    private Set<String> makeVOSet = new HashSet<>();
    private StringBuilder mapperBuf = new StringBuilder();
    private AstBase.Flags flags;
    private static final Logger logger = LoggerFactory.getLogger(AstMakeVO.class);

    public AstMakeVO() {
        /*Constructor*/
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
            if (entry.isMutation()) {
                if (makeVOSet.contains(entry.getReqName())) return;
                parameterHandling(
                    entry.isMutation(), entry.getReqName(), entry.getResName(), requestFields, mapperBuf);
                makeVOSet.add(entry.getReqName());
            } else {
                if (makeVOSet.contains(entry.getResName())) return;
                parameterHandling(
                    entry.isMutation(), entry.getReqName(), entry.getResName(), responseFields, mapperBuf);
                makeVOSet.add(entry.getResName());

                parameterHandling(
                    true, entry.getReqName(), entry.getResName(), requestFields, mapperBuf);
                makeVOSet.add(entry.getReqName());
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

        String mapperContent =
            readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/database/mappers/mappers.go.template");
        if (flags.isContracts()) {
            mapperContent =
                mapperContent.replace(
                    GO_PROTO_PATH,
                    "\"code.nurture.farm/Core/Contracts/" + super.getServiceName()
                        + "/Gen/Go" + super.getServiceName() + "\"");
        } else {
            mapperContent =
                mapperContent.replace(
                    GO_PROTO_PATH,
                    "\"" + super.getCodeUrl() + namespace + FORWARD_SLASH + super.getServiceName()
                        + "/zerotouch/golang/proto/" + super.getGoPackage() + FORWARD_SLASH + super.getServiceName()
                        + "\"");
        }
        mapperContent = mapperContent.replace(CODE_URL, getCodeUrl());
        mapperContent = mapperContent.replace(NAME_SPACE, namespace);
        mapperContent = mapperContent.replace(SERVICE_NAME, getServiceName());
        mapperContent = mapperContent.replace(GO_PACKAGE, super.getGoPackage());
        mapperContent = mapperContent.replace("@insert_maps@", mapperBuf.toString());

        File pj = new File(getGoGeneratePath() + "/database/mappers");
        if (!pj.exists()) pj.mkdirs();

        writeToFile(super.getGoGeneratePath() + "/database/mappers/mappers.go", mapperContent);
    }

    void parameterHandling(
        Boolean isMutation,
        String requestName,
        String responseName,
        List<FieldDetail> paramDetails,
        StringBuilder mapperBuf) {

        processIsMutation(isMutation, requestName, responseName, mapperBuf);

        if (!paramDetails.isEmpty()) {
            if (Boolean.TRUE.equals(isMutation)) {
                mapperBuf.append("\n\treturn &models." + requestName + "VO { \n\t\t");
            } else {
                mapperBuf.append("\n\treturn &fs." + responseName + "Record { \n\t\t");
            }
            boolean isFirst = true;
            Map<String, List<FieldDetail>> msgNames = new HashMap<>();
            processParamDetails(isMutation, paramDetails, mapperBuf, isFirst, msgNames);
            processMsgNames(isMutation, mapperBuf, isFirst, msgNames);
            mapperBuf.append(",\n\t}\n\n}\n");
        } else {
            mapperBuf.append("\n\treturn nil  \n}\n");
        }
    }

    private void processMsgNames(
        Boolean isMutation,
        StringBuilder mapperBuf,
        boolean isFirst,
        Map<String, List<FieldDetail>> msgNames) {
        for (Map.Entry<String, List<FieldDetail>> entry : msgNames.entrySet()) {

            if (isFirst) isFirst = false;
            else mapperBuf.append(",\n\t\t");

            String msgname = entry.getKey().toLowerCase();
            msgname = Character.toUpperCase(msgname.charAt(0)) + msgname.substring(1);

            mapperBuf.append(msgname).append(": &fs.").append(entry.getKey()).append("{\n\t\t\t");
            for (FieldDetail aParam : entry.getValue()) {

                int dotPos = aParam.getFieldName().indexOf('.');
                String fldName = aParam.getFieldName().substring(dotPos + 1);
                mapperBuf.append(CaseUtils.toCamelCase(fldName, true, '_')).append(":");
                String paramName = aParam.getFldNameCamel();
                paramName = Character.toUpperCase(paramName.charAt(0)) + paramName.substring(1);
                setVOType(aParam.getGoType(), paramName, isMutation, mapperBuf);
                mapperBuf.append(",\n\t\t\t");
            }
            mapperBuf.append("\n\t\t}");
        }
    }

    private void processParamDetails(
        Boolean isMutation,
        List<FieldDetail> paramDetails,
        StringBuilder mapperBuf,
        boolean isFirst,
        Map<String, List<FieldDetail>> msgNames) {
        for (FieldDetail aParam : paramDetails) {

            int dotPos = aParam.getFieldName().indexOf('.');
            if (dotPos >= 0) {
                String msgname = aParam.getFieldName().substring(0, dotPos);
                if (msgNames.containsKey(msgname)) {
                    msgNames.get(msgname).add(aParam);
                } else {
                    List<FieldDetail> fldNames = new ArrayList<>();
                    fldNames.add(aParam);
                    msgNames.put(msgname, fldNames);
                }
            } else {
                String fldName = aParam.getFldNameCamel();
                if (isFirst) isFirst = false;
                else mapperBuf.append(",\n\t\t");

                String paramName = Character.toUpperCase(fldName.charAt(0)) + fldName.substring(1);

                mapperBuf.append(paramName).append(": ");

                setVOType(aParam.getGoType(), paramName, isMutation, mapperBuf);
            }
        }
    }

    private void processIsMutation(
        Boolean isMutation, String requestName, String responseName, StringBuilder mapperBuf) {
        if (Boolean.TRUE.equals(isMutation)) {
            mapperBuf
                .append("func Make")
                .append(requestName)
                .append("VO(request *fs.")
                .append(requestName)
                .append(") *models.")
                .append(requestName)
                .append("VO {\n");
        } else {
            mapperBuf
                .append("func Make")
                .append(responseName)
                .append("VO(model *models.")
                .append(responseName)
                .append("VO) *fs.")
                .append(responseName)
                .append("Record {\n");
        }
    }

    void setVOType(String type, String paramName, boolean isMutation, StringBuilder mapperBuf) {

        if (type.equals(STRING)) {
            if (isMutation) {
                mapperBuf.append("getNullableString(request.").append(paramName).append(")");
            } else {
                mapperBuf.append(MODEL_DOT).append(paramName).append(".String");
            }
        } else if (type.startsWith(ENUM)) {
            if (isMutation) {
                mapperBuf.append("getNullableString(request.").append(paramName).append(".String())");
            } else {
                mapperBuf.append(MODEL_DOT).append(paramName).append(".String");
            }
        } else if (type.equals(BOOL)) {
            if (isMutation) {
                mapperBuf.append("getNullableBool(request.").append(paramName).append(")");
            } else {
                mapperBuf.append(MODEL_DOT).append(paramName).append(".Bool");
            }
            //    } else if (type.equals("time.Time")) {
            //      if (isMutation) {
            //        mapperBuf.append("getNullableTimestamp(request.").append(paramName).append(")");
            //      } else {
            //        mapperBuf.append(MODEL_DOT).append(paramName).append(".Time.Unix()");
            //      }
        } else if (type.equals(DATETIME)) {
            if (isMutation) {
                mapperBuf.append("getNullableDateTime(request.").append(paramName).append(")");
            } else {
                mapperBuf.append(MODEL_DOT).append(paramName).append(".Time.Unix()");
            }
        } else if (type.equals(TIMESTAMP)) {
            if (isMutation) {
                mapperBuf.append("getNullableTimestamp(request.").append(paramName).append(")");
            } else {
                mapperBuf.append("getProtoTime(model.").append(paramName).append(".String)");
            }
        } else if (type.equals(INT32)) {
            if (isMutation) {
                mapperBuf.append("getNullableInt32(request.").append(paramName).append(")");
            } else {
                mapperBuf.append(MODEL_DOT).append(paramName).append(".Int32");
            }
        } else if (type.equals(INT64)) {
            if (isMutation) {
                mapperBuf.append("getNullableInt64(request.").append(paramName).append(")");
            } else {
                mapperBuf.append(MODEL_DOT).append(paramName).append(".Int64");
            }
        } else if (type.equals(FLOAT64)) {
            if (isMutation) {
                mapperBuf.append("getNullableFloat64(request.").append(paramName).append(")");
            } else {
                mapperBuf.append(MODEL_DOT).append(paramName).append(".Float64");
            }
        } else if (type.equals("[]string")) {
            if (isMutation) {
                mapperBuf.append("getNullableStrings(request.").append(paramName).append(")");
            }
        } else {
            logger.error("TODO1 data type" + type);
            System.exit(1);
        }
    }
}
