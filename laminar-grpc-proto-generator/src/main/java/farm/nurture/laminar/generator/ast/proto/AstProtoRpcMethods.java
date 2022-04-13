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

package farm.nurture.laminar.generator.ast.proto;

import static farm.nurture.laminar.generator.Constants.PROTO_TYPE_REPEATED;
import static farm.nurture.laminar.generator.Constants.REQ_NAME;
import static farm.nurture.laminar.generator.Constants.RESPONSE_NAME;
import static farm.nurture.laminar.generator.Constants.RES_BODY;

import farm.nurture.infra.util.Logger;
import farm.nurture.infra.util.LoggerFactory;
import farm.nurture.laminar.core.util.CaseUtils;
import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.Configuration;
import farm.nurture.laminar.generator.FieldDetail;
import farm.nurture.laminar.generator.TemplatesVO;
import farm.nurture.laminar.generator.ast.AstBase;
import farm.nurture.laminar.generator.ast.javalang.AstDao;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AstProtoRpcMethods extends AstBase {

    private StringBuilder protoCallableMethods;
    private AstBase.Flags flags;
    private static final Logger logger = LoggerFactory.getLogger(AstProtoRpcMethods.class);

    public AstProtoRpcMethods(StringBuilder protoCallableMethods) {
        this.protoCallableMethods = protoCallableMethods;
    }

    @Override
    public void onStart(Configuration conf, AstBase.Flags flags) {
        super.onStart(conf, flags);
        this.flags = flags;
    }

    @Override
    public void onEntry(
        AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields) {

        protoCallableMethods.append("\n\n/* ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ ");
        protoCallableMethods.append("\n\t\t\t\t").append(entry.getTitle()).append("\t\t\t\t\t\n");
        protoCallableMethods.append(" ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ */ \n");

        if (entry.isMutation()) {
            IUDRequest(entry, requestFields);
            IUDResponse(entry);
        } else {
            selectRequest(entry, requestFields);
            selectResponse(entry, responseFields);
        }
    }

    void IUDRequest(AppConfigVO entry, List<FieldDetail> paramDetails) {

        int rerOverrideT =
            (null == entry.getReqOverride()) ? 0 : entry.getReqOverride().trim().length();

        if (entry.isDeclReq()) {

            processIsContracts(entry);

            String reqBody = (rerOverrideT == 0) ? "" : entry.getReqOverride();

            if (rerOverrideT > 0) {
                protoCallableMethods.append("\n\t").append(reqBody);

            } else {
                int varPosition = 2;
                Set<String> uniqueParams = new HashSet<>();
                for (FieldDetail aParam : paramDetails) {
                    String paramName = aParam.getFldNameCamel();
                    if (uniqueParams.contains(paramName)) continue;
                    uniqueParams.add(paramName);
                    protoCallableMethods.append("\n\t")
                        .append(aParam.getProtoType())
                        .append(" ")
                        .append(paramName)
                        .append(" = ")
                        .append(varPosition++)
                        .append(';');
                }

                for (Map.Entry<String, String> aReplace : entry.getSqlReplaces().entrySet()) {
                    if (uniqueParams.contains(aReplace.getKey())) continue;
                    protoCallableMethods
                        .append("\n\t")
                        .append(aReplace.getValue())
                        .append(" ")
                        .append(aReplace.getKey())
                        .append(" = ")
                        .append(varPosition++)
                        .append(';');
                }
            }

            protoCallableMethods.append("\n}\n");
            // Create bulk request
            createBulkRequest(entry);
        }
    }

    private void processIsContracts(AppConfigVO entry) {
        if (flags.isContracts()) {
            protoCallableMethods.append(
                TemplatesVO.getProtoMutableContractRequest().replace(
                    REQ_NAME, entry.getReqName()));
        } else {
            protoCallableMethods.append(
                TemplatesVO.getProtoMutableRequest().replace(REQ_NAME, entry.getReqName()));
        }
    }

    private void createBulkRequest(AppConfigVO entry) {
        if (flags.isContracts()) {
            protoCallableMethods.append("\n\nmessage Bulk").append(entry.getReqName()).append(
                    " {\n  farm.nurture.core.contracts.common.RequestHeaders requestHeaders = 1;\n repeated ")
                .append(entry.getReqName()).append("  requests = 2;\n}\n\n\n");
        } else {
            protoCallableMethods.append("\n\nmessage Bulk").append(entry.getReqName())
                .append(" {\n  RequestHeaders requestHeaders = 1;\n repeated ")
                .append(entry.getReqName()).append("  requests = 2;\n}\n\n\n");
        }
    }

    void IUDResponse(AppConfigVO entry) {

        if (!entry.isDeclRes()) return;

        int resOverrideT =
            (null == entry.getResOverride()) ? 0 : entry.getResOverride().trim().length();
        String resBody = (resOverrideT == 0) ? "" : entry.getResOverride();
        String msgBody = "";
        if (flags.isContracts()) {
            msgBody =
                TemplatesVO.getProtoMutableContractResponse()
                    .replace(RESPONSE_NAME, entry.getResName())
                    .replace(RES_BODY, resBody);
        } else {
            msgBody =
                TemplatesVO.getProtoMutableResponse()
                    .replace(RESPONSE_NAME, entry.getResName())
                    .replace(RES_BODY, resBody);
        }

        protoCallableMethods.append(msgBody);

        if (flags.isContracts()) {
            protoCallableMethods.append("\n\nmessage ").append(entry.getBulkResName()).append(
                    " {\n  farm.nurture.core.contracts.common.RequestStatusResult status = 1; \n ")
                .append("int32 count = 2; \n ").append("repeated ").append(entry.getResName())
                .append("  responses = 3;\n}\n\n\n");
        } else {
            protoCallableMethods.append("\n\nmessage ").append(entry.getBulkResName())
                .append(" {\n  Status status = 1; \n ").append("int32 count = 2; \n ")
                .append("repeated ").append(entry.getResName()).append("  responses = 3;\n}\n\n\n");
        }
    }

    void selectRequest(AppConfigVO entry, List<FieldDetail> paramDetails) {

        int reqOverrideT =
            (null == entry.getReqOverride()) ? 0 : entry.getReqOverride().trim().length();

        if (entry.isDeclReq()) {

            if (flags.isContracts()) {
                protoCallableMethods.append(
                    TemplatesVO.getProtoImmutableContractRequest().replace(
                        REQ_NAME, entry.getReqName()));
            } else {
                protoCallableMethods.append(
                    TemplatesVO.getProtoImmutableRequest().replace(REQ_NAME, entry.getReqName()));
            }

            String reqBody = (reqOverrideT == 0) ? "" : entry.getReqOverride();
            processReqOverrideT(entry, paramDetails, reqOverrideT, reqBody);
            protoCallableMethods.append("\n}\n");
        }
    }

    private void processReqOverrideT(AppConfigVO entry, List<FieldDetail> paramDetails, int reqOverrideT,
        String reqBody) {
        if (reqOverrideT > 0) {

            protoCallableMethods.append("\n\t").append(reqBody);

        } else {

            int varPosition = 3;
            Set<String> uniqueParams = new HashSet<>();
            for (FieldDetail aParam : paramDetails) {
                String paramName = aParam.getFldNameCamel();
                if (uniqueParams.contains(paramName)) continue;
                uniqueParams.add(paramName);
                protoCallableMethods
                    .append("\n\t")
                    .append(aParam.getProtoType())
                    .append(" ")
                    .append(paramName)
                    .append(" = ")
                    .append(varPosition++)
                    .append(';');
            }

            for (Map.Entry<String, String> aReplace : entry.getSqlReplaces().entrySet()) {
                if (uniqueParams.contains(aReplace.getKey())) continue;
                protoCallableMethods
                    .append("\n\t")
                    .append(aReplace.getValue())
                    .append(" ")
                    .append(aReplace.getKey())
                    .append(" = ")
                    .append(varPosition++)
                    .append(';');
            }
        }
    }

    void selectResponse(AppConfigVO entry, List<FieldDetail> responseFields) {

        logger.info("Skipping response declaration - " + entry.isDeclRes());

        if (!entry.isDeclRes()) return;

        int recordIndex = 0;

        protoCallableMethods.append("message ").append(entry.getRecordName()).append(" {\n");
        /** Message Body Starts here */
        processMessageBody(responseFields, recordIndex);
        /** Message Body Ends here */
        protoCallableMethods.append("\n}\n");

        logger.info("\n\n\n\n\n\n\n\n\n\nentry.resOverride:" + entry.getResOverride());

        String repeated = (entry.isSqlUniquekey()) ? "" : PROTO_TYPE_REPEATED;
        int resOverrideT =
            (null == entry.getResOverride()) ? 0 : entry.getResOverride().trim().length();

        if (flags.isContracts()) {
            protoCallableMethods.append(
                TemplatesVO.getProtoImmutableContractResponse().
                    replace(RES_BODY,
                        (resOverrideT == 0) ? "@repeated@ @recordName@ records= 4;" : entry.getResOverride())
                    .replace("@repeated@", repeated)
                    .replace("@recordName@", entry.getRecordName())
                    .replace(RESPONSE_NAME, entry.getResName()));
        } else {
            protoCallableMethods.append(
                TemplatesVO.getProtoImmutableResponse()
                    .replace(
                        RES_BODY,
                        (resOverrideT == 0)
                            ? "@repeated@ @recordName@ records= 4;"
                            : entry.getResOverride())
                    .replace("@repeated@", repeated)
                    .replace("@recordName@", entry.getRecordName())
                    .replace(RESPONSE_NAME, entry.getResName()));
        }
    }

    private void processMessageBody(List<FieldDetail> responseFields, int recordIndex) {
        Set<String> msgnames = new HashSet<>();
        for (FieldDetail t : responseFields) {

            AstDao.FieldDetailDao fdd = new AstDao.FieldDetailDao(t);
            if (null != fdd.getMsgname()) {
                msgnames.add(fdd.getMsgname());
                continue;
            }

            if (recordIndex > 0) protoCallableMethods.append("\n");
            protoCallableMethods.append('\t');
            protoCallableMethods.append(t.getProtoType()).append(' ').append(t.getFldNameCamel())
                .append(" = ").append(++recordIndex).append(';');
        }

        for (String msgname : msgnames) {
            String msgNameCamel = CaseUtils.toCamelCase(msgname, false, '_');
            protoCallableMethods.append(msgname).append(' ').append(msgNameCamel).append(" = ")
                .append(++recordIndex).append(';');
        }
    }
}
