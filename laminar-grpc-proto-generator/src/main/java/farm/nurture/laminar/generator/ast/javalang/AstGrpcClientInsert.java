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

import static farm.nurture.laminar.generator.Constants.BOOL;
import static farm.nurture.laminar.generator.Constants.INT32;
import static farm.nurture.laminar.generator.Constants.INT64;
import static farm.nurture.laminar.generator.Constants.PROTO_TYPE_BYTES;
import static farm.nurture.laminar.generator.Constants.PROTO_TYPE_DOUBLE;
import static farm.nurture.laminar.generator.Constants.PROTO_TYPE_FLOAT;
import static farm.nurture.laminar.generator.Constants.SERVICE_NAME;
import static farm.nurture.laminar.generator.Constants.SERVICE_REQ;
import static farm.nurture.laminar.generator.Constants.SERVICE_RES;
import static farm.nurture.laminar.generator.Constants.THREE_CLOSING_BRACKETS_DOT_SPACE;

import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.FieldDetail;
import farm.nurture.laminar.generator.TemplatesVO;
import farm.nurture.laminar.generator.ast.AstBase;
import java.util.List;

public class AstGrpcClientInsert extends AstBase {

    StringBuilder clientInsertContent = new StringBuilder(1024 * 64);

    public AstGrpcClientInsert(StringBuilder clientInsertContent) {
        this.clientInsertContent = clientInsertContent;
    }

    @Override
    public void onEntry(
        AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields) {

        int reqOverrideT =
            (null == entry.getReqOverride()) ? 0 : entry.getReqOverride().trim().length();
        int resOverrideT =
            (null == entry.getResOverride()) ? 0 : entry.getResOverride().trim().length();

        if (reqOverrideT > 0 || resOverrideT > 0) return;

        if (entry.isDeclGrpc() && entry.isMutation()) {
            IUDResponse(entry, requestFields);
        }
    }

    void IUDResponse(AppConfigVO config, List<FieldDetail> paramDetails) {

        if (config.isDeclGrpc()) {
            StringBuilder grpcClientParamSettings = new StringBuilder(512);
            StringBuilder grpcClientParamInputs = new StringBuilder(512);
            parameterHandling(config, paramDetails, grpcClientParamSettings, grpcClientParamInputs);

            clientInsertContent
                .append('\n')
                .append(
                    TemplatesVO.getServiceGrpcClientInsert().replace(SERVICE_NAME, config.getTitle())
                        .replace(SERVICE_REQ, config.getReqName())
                        .replace(SERVICE_RES, config.getResName())
                        .replace("@paramInputs@", grpcClientParamInputs.toString())
                        .replace("@paramSettings@", grpcClientParamSettings.toString()))
                .append('\n');
        }
    }

    void parameterHandling(
        AppConfigVO config,
        List<FieldDetail> paramDetails,
        StringBuilder grpcClientParamSettings,
        StringBuilder grpcClientParamInputs) {

        if (!paramDetails.isEmpty()) {
            int index = -1;
            for (FieldDetail aParam : paramDetails) {
                String fldName = aParam.getFldNameCamel();
                String getSetSuffix = Character.toUpperCase(fldName.charAt(0)) + fldName.substring(1);

                index =
                    processProtoType(
                        config,
                        grpcClientParamSettings,
                        grpcClientParamInputs,
                        index,
                        aParam,
                        getSetSuffix);
            }
        }
    }

    private int processProtoType(
        AppConfigVO config,
        StringBuilder grpcClientParamSettings,
        StringBuilder grpcClientParamInputs,
        int index,
        FieldDetail aParam,
        String getSetSuffix) {
        if (aParam.getProtoType().equals(BOOL))
            grpcClientParamSettings.append("set").append(getSetSuffix)
                .append("(Boolean.valueOf(params.get(")
                .append(++index)
                .append(THREE_CLOSING_BRACKETS_DOT_SPACE);
        else if (aParam.getProtoType().equals(INT64))
            grpcClientParamSettings.append("set").append(getSetSuffix)
                .append("(Long.valueOf(params.get(")
                .append(++index)
                .append(THREE_CLOSING_BRACKETS_DOT_SPACE);
        else if (aParam.getProtoType().equals(INT32))
            grpcClientParamSettings.append("set").append(getSetSuffix)
                .append("(Integer.valueOf(params.get(")
                .append(++index)
                .append(THREE_CLOSING_BRACKETS_DOT_SPACE);
        else if (aParam.getProtoType().equals(PROTO_TYPE_FLOAT))
            grpcClientParamSettings.append("set").append(getSetSuffix)
                .append("(Float.valueOf(params.get(")
                .append(++index)
                .append(THREE_CLOSING_BRACKETS_DOT_SPACE);
        else if (aParam.getProtoType().equals(PROTO_TYPE_DOUBLE))
            grpcClientParamSettings.append("set").append(getSetSuffix)
                .append("(Double.valueOf(params.get(")
                .append(++index)
                .append(THREE_CLOSING_BRACKETS_DOT_SPACE);
        else if (aParam.getProtoType().equals(PROTO_TYPE_BYTES))
            grpcClientParamSettings.append("set").append(getSetSuffix)
                .append("(ByteString.copyFrom(params.get(")
                .append(++index)
                .append(").getBytes())    ). ");
        else if (aParam.getProtoType().startsWith("enum ")) {
            grpcClientParamSettings.append("set").append(getSetSuffix).append("( ")
                .append(config.getReqName()).append(".").append(getSetSuffix)
                .append(".valueOf(params.get(")
                .append(++index)
                .append(THREE_CLOSING_BRACKETS_DOT_SPACE);
        } else
            grpcClientParamSettings.append("set").append(getSetSuffix).append("(params.get(")
                .append(++index)
                .append(")). ");

        grpcClientParamInputs
            .append("System.out.print(\"Enter ")
            .append(getSetSuffix)
            .append(": \"); params.add(reader.readLine());");
        return index;
    }
}
