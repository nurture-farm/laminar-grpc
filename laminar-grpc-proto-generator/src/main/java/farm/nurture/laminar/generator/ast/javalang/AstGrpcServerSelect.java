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

import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.FieldDetail;
import farm.nurture.laminar.generator.TemplatesVO;
import farm.nurture.laminar.generator.ast.AstBase;
import java.util.List;

public class AstGrpcServerSelect extends AstBase {

    private String ServiceGrpcServerSelectF =
        TemplatesVO.getTEMPLATE_DIR() + "/java/ServiceGrpcServerSelect.template";
    private String ServiceGrpcServerSelect = readTemplateFile(ServiceGrpcServerSelectF);
    private String ServiceGrpcServerSelectImplF =
        TemplatesVO.getTEMPLATE_DIR() + "/java/ServiceGrpcServerSelectImpl.template";
    private String ServiceGrpcServerSelectImpl = readTemplateFile(ServiceGrpcServerSelectImplF);
    StringBuilder serverSelectContent;

    public AstGrpcServerSelect(StringBuilder serverSelectContent) {
        this.serverSelectContent = serverSelectContent;
    }

    @Override
    public void onEntry(
        AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields) {

        if (!entry.isMutation()) {
            selectResponse(entry, requestFields);
        }
    }

    void selectResponse(AppConfigVO entry, List<FieldDetail> requestFields) {

        StringBuilder grpcServerParamValues = new StringBuilder(512);
        parameterHandling(entry.getReqName(), requestFields, grpcServerParamValues);

        if (entry.isDeclGrpc()) {
            String commentImpl = (entry.isImplGrpc()) ? "" : "//";
            serverSelectContent
                .append('\n')
                .append(
                    ServiceGrpcServerSelect.replace(SERVICE_NAME, entry.getTitle())
                        .replace(SERVICE_REQ, entry.getReqName())
                        .replace(SERVICE_RES, entry.getResName())
                            .
                        //                            replace("@paramValues@",
                        // grpcServerParamValues.toString()).
                            replace("@grpc_impl@", commentImpl))
                .append('\n');
        }

        if (entry.isImplGrpc()) {
            String addRecords =
                (entry.isSqlUniquekey())
                    ? "for ( @service_response@VO vo : recordList)  { responseBuilder.setRecords(vo.buildProto()); break; }"
                    : "for ( @service_response@VO vo : recordList)  responseBuilder.addRecords(vo.buildProto());";

            StringBuilder sqlReplaceConent = new StringBuilder();
            replaceSql(entry.getSqlReplaces(), sqlReplaceConent);
            serverSelectContent
                .append('\n')
                .append(
                    ServiceGrpcServerSelectImpl.replace(SERVICE_NAME, entry.getTitle())
                        .replace("@add_records@", addRecords)
                        .replace(SERVICE_REQ, entry.getReqName())
                        .replace(SERVICE_RES, entry.getResName())
                        .replace("@sqlreplace@", sqlReplaceConent.toString())
                        .replace(PARAM_VALUES, grpcServerParamValues.toString()))
                .append('\n');
        }
    }

    void parameterHandling(
        String requestName, List<FieldDetail> paramDetails, StringBuilder grpcServerParamValues) {

        if (!paramDetails.isEmpty()) {

            grpcServerParamValues.append("paramValues = new Object[] {");
            boolean isFirst = true;
            for (FieldDetail aParam : paramDetails) {

                String fldName = aParam.getFldNameCamel();
                if (isFirst) isFirst = false;
                else grpcServerParamValues.append(',');
                String getSetSuffix = Character.toUpperCase(fldName.charAt(0)) + fldName.substring(1);
                if (aParam.getJavaType().equals(JAVA_SQL_DATE))
                    grpcServerParamValues.append(" new java.sql.Date(request.get")
                        .append(getSetSuffix).append("() )");
                else if (aParam.getJavaType().equals(JAVA_SQL_TIMESTAMP))
                    grpcServerParamValues.append(" new java.sql.Timestamp(request.get")
                        .append(getSetSuffix).append("() )");
                else if (aParam.getProtoType().equals(PROTO_TYPE_BYTES))
                    grpcServerParamValues.append(" (request.get").append(getSetSuffix)
                        .append("().toByteArray() )");
                else if (aParam.getProtoType().startsWith("enum "))
                    grpcServerParamValues.append(REQUEST_GET).append(getSetSuffix)
                        .append("().name() ");
                else if (aParam.getProtoType().contains("int") && "offset".equalsIgnoreCase(getSetSuffix))
                    grpcServerParamValues.append(REQUEST_GET).append(getSetSuffix).append("() ");
                else if (aParam.getProtoType().equals(PROTO_TYPE_DOUBLE)
                    || aParam.getProtoType().equals(PROTO_TYPE_FLOAT)
                    || aParam.getProtoType().contains("int")
                    || aParam.getProtoType().contains("fixed"))
                    grpcServerParamValues.append(REQUEST_GET).append(getSetSuffix)
                        .append("() != 0 ? request.get").append(getSetSuffix).append("() : null");
                else if (aParam.getProtoType().equals(STRING))
                    grpcServerParamValues.append("StringUtils.isNonEmpty(request.get")
                        .append(getSetSuffix).append("()) ? request.get").append(getSetSuffix)
                        .append("() : null");
                else grpcServerParamValues.append(REQUEST_GET + getSetSuffix + "() ");
            }
            grpcServerParamValues.append("};");
        }
    }
}
