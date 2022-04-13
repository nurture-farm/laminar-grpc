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

package farm.nurture.laminar.generator.ast.javalang;

import static farm.nurture.laminar.generator.Constants.JAVA_SQL_DATE;
import static farm.nurture.laminar.generator.Constants.JAVA_SQL_TIMESTAMP;
import static farm.nurture.laminar.generator.Constants.PARAM_VALUES;
import static farm.nurture.laminar.generator.Constants.PROTO_TYPE_BYTES;
import static farm.nurture.laminar.generator.Constants.PROTO_TYPE_DOUBLE;
import static farm.nurture.laminar.generator.Constants.PROTO_TYPE_FLOAT;
import static farm.nurture.laminar.generator.Constants.REQUEST_GET;
import static farm.nurture.laminar.generator.Constants.SERVICE_NAME;
import static farm.nurture.laminar.generator.Constants.SERVICE_REQ;
import static farm.nurture.laminar.generator.Constants.SERVICE_RES;
import static farm.nurture.laminar.generator.Constants.STRING;

import farm.nurture.infra.util.Logger;
import farm.nurture.infra.util.LoggerFactory;
import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.FieldDetail;
import farm.nurture.laminar.generator.TemplatesVO;
import farm.nurture.laminar.generator.ast.AstBase;
import java.util.List;

public class AstGrpcServerInsert extends AstBase {

    private StringBuilder serverInsertContent;
    private String ServiceGrpcServerInsertF =
        TemplatesVO.getTEMPLATE_DIR() + "/java/ServiceGrpcServerInsert.template";
    private String ServiceGrpcServerInsert = readTemplateFile(ServiceGrpcServerInsertF);

    private String ServiceGrpcServerInsertImplF =
        TemplatesVO.getTEMPLATE_DIR() + "/java/ServiceGrpcServerInsertImpl.template";
    private String ServiceGrpcServerInsertiImpl = readTemplateFile(ServiceGrpcServerInsertImplF);
    private static final Logger logger = LoggerFactory.getLogger(AstGrpcServerInsert.class);


    public AstGrpcServerInsert(StringBuilder serverInsertContent) {
        this.serverInsertContent = serverInsertContent;
    }

    @Override
    public void onEntry(
        AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields) {

        if (entry.isMutation()) {
            IUDResponse(entry, requestFields);
        }
    }

    void IUDResponse(AppConfigVO config, List<FieldDetail> paramDetails) {

        StringBuilder grpcServerParamValues = new StringBuilder(512);
        parameterHandling(config.getReqName(), paramDetails, grpcServerParamValues);

        if (config.isDeclGrpc()) {
            String commentImpl = (config.isImplGrpc()) ? "" : "//";

            serverInsertContent
                .append('\n')
                .append(
                    ServiceGrpcServerInsert.replace(SERVICE_NAME, config.getTitle())
                        .replace(SERVICE_REQ, config.getReqName())
                        .replace(SERVICE_RES, config.getResName())
                        .replace(PARAM_VALUES, grpcServerParamValues.toString())
                        .replace("@isInsert@", (config.isInsert()) ? "true" : "false")
                        .replace("@grpc_impl@", commentImpl))
                .append('\n');
        }
        if (config.isImplGrpc()) {

            StringBuilder sqlReplaceConent = new StringBuilder();
            replaceSql(config.getSqlReplaces(), sqlReplaceConent);

            serverInsertContent
                .append('\n')
                .append(
                    ServiceGrpcServerInsertiImpl.replace(SERVICE_NAME, config.getTitle())
                        .replace(SERVICE_REQ, config.getReqName())
                        .replace(SERVICE_RES, config.getResName())
                        .replace(PARAM_VALUES, grpcServerParamValues.toString())
                        .replace("@sqlreplace@", sqlReplaceConent.toString())
                        .replace("@isInsert@", (config.isInsert()) ? "true" : "false"))
                .append('\n');
        }
    }

    void parameterHandling(
        String requestName, List<FieldDetail> paramDetails, StringBuilder grpcServerParamValues) {

        logger.info("paramDetails = {}",paramDetails.toString());
        if (!paramDetails.isEmpty()) {

            grpcServerParamValues.append("paramValues = new Object[] {");
            boolean isFirst = true;
            for (FieldDetail aParam : paramDetails) {
                String fldName = aParam.getFldNameCamel();
                if (isFirst) isFirst = false;
                else grpcServerParamValues.append(',');

                String getSetSuffix = Character.toUpperCase(fldName.charAt(0)) + fldName.substring(1);
                if (aParam.getJavaType().equals(JAVA_SQL_DATE))
                    grpcServerParamValues.append(" new java.sql.Date(request.get" + getSetSuffix + "() )");
                else if (aParam.getJavaType().equals(JAVA_SQL_TIMESTAMP))
                    grpcServerParamValues.append(
                        " new java.sql.Timestamp(request.get" + getSetSuffix + "() )");
                else if (aParam.getProtoType().equals(PROTO_TYPE_BYTES))
                    grpcServerParamValues.append(" (request.get" + getSetSuffix + "().toByteArray() )");
                else if (aParam.getProtoType().startsWith("enum "))
                    grpcServerParamValues.append(REQUEST_GET + getSetSuffix + "().name() ");
                else if (aParam.getProtoType().equals(PROTO_TYPE_DOUBLE)
                    || aParam.getProtoType().equals(PROTO_TYPE_FLOAT)
                    || aParam.getProtoType().contains("int")
                    || aParam.getProtoType().contains("fixed"))
                    grpcServerParamValues.append(
                        REQUEST_GET + getSetSuffix + "() != 0 ? request.get" + getSetSuffix + "() : null");
                else if (aParam.getProtoType().equals(STRING))
                    grpcServerParamValues.append(
                        "StringUtils.isNonEmpty(request.get"
                            + getSetSuffix
                            + "()) ? request.get"
                            + getSetSuffix
                            + "() : null");
                else grpcServerParamValues.append(REQUEST_GET + getSetSuffix + "() ");
            }
            grpcServerParamValues.append("};");
        }
    }
}
