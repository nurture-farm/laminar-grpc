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
import static farm.nurture.laminar.generator.Constants.RESPONSE;
import static farm.nurture.laminar.generator.Constants.RESPONSE_STATUS_FAILURE;
import static farm.nurture.laminar.generator.Constants.RETURN_RESPONSE;
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

public class AstService extends AstBase {

  private List<AstBase> parts = new ArrayList<>();
  private String serviceContent;
  private String serviceSelectContent;
  private String serviceInsertContent;
  private String serviceBulkInsertContent;
  private String serviceGrpcSelectContent;
  private String serviceGrpcInsertContent;
  private String serviceGrpcBulkInsertContent;

  private String responseStatusFailureContent;
  private String responseContractStatusFailureContent;

  private StringBuilder insertConstantLines = new StringBuilder();
  private StringBuilder insertLines = new StringBuilder();

  private AstBase.Flags flags;

  public AstService() {
    serviceContent = readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/service.go.template");
    serviceSelectContent =
        readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/service.select.template");
    serviceInsertContent =
        readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/service.insert.template");
    serviceBulkInsertContent =
        readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/service.bulkinsert.template");
    serviceGrpcSelectContent =
        readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/serviceGrpc.select.template");
    serviceGrpcInsertContent =
        readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/serviceGrpc.insert.template");
    serviceGrpcBulkInsertContent =
        readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/serviceGrpc.bulkinsert.template");
    responseStatusFailureContent =
        readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/common.status.failure.template");
    responseContractStatusFailureContent =
        readTemplateFile(
            TemplatesVO.getTEMPLATE_DIR() + "/golang/common.contract.status.failure.template");
  }

  @Override
  public void onStart(Configuration conf, Flags flags) {
    super.onStart(conf, flags);
    this.flags = flags;
    for (AstBase b : parts) b.onStart(conf, flags);
    if (flags.isContracts()) {
      serviceGrpcSelectContent =
          serviceGrpcSelectContent.replace(
              RESPONSE_STATUS_FAILURE, responseContractStatusFailureContent);
      serviceGrpcInsertContent =
          serviceGrpcInsertContent.replace(
              RESPONSE_STATUS_FAILURE, responseContractStatusFailureContent);
      serviceGrpcBulkInsertContent =
          serviceGrpcBulkInsertContent.replace(
              RESPONSE_STATUS_FAILURE, responseContractStatusFailureContent);
    } else {
      serviceGrpcSelectContent =
          serviceGrpcSelectContent.replace(RESPONSE_STATUS_FAILURE, responseStatusFailureContent);
      serviceGrpcInsertContent =
          serviceGrpcInsertContent.replace(RESPONSE_STATUS_FAILURE, responseStatusFailureContent);
      serviceGrpcBulkInsertContent =
          serviceGrpcBulkInsertContent.replace(
              RESPONSE_STATUS_FAILURE, responseStatusFailureContent);
    }
  }

  @Override
  public void onEntry(
      AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields) {

    for (AstBase b : parts) b.onEntry(entry, requestFields, responseFields);

    if (entry.isDeclGrpc()) {
      processIsMutation(entry);
      if (entry.isInsert()) {
        String grpcBulkInsertContent = "";
        if (entry.isImplGrpc()) {
          grpcBulkInsertContent = insertContent(serviceGrpcBulkInsertContent, entry);
        }
        String insertBulkContent = insertContent(serviceBulkInsertContent, entry) + "\n";
        insertBulkContent =
            insertBulkContent.replace("@servicegrpc_bulkinsert@", grpcBulkInsertContent);
        if (entry.isImplGrpc()) {
          insertBulkContent = insertBulkContent.replace(RETURN_RESPONSE, RESPONSE);
        } else {
          insertBulkContent = insertBulkContent.replace(RETURN_RESPONSE, "nil");
        }
        insertLines.append(insertBulkContent);
      }
    }
  }

  private void processIsMutation(AppConfigVO entry) {
    if (entry.isMutation()) {
      String grpcInsertContent = "";
      if (entry.isImplGrpc()) {
        grpcInsertContent = insertContent(serviceGrpcInsertContent, entry);
      }
      String insertContent = insertContent(serviceInsertContent, entry) + "\n";
      insertContent = insertContent.replace("@servicegrpc_insert@", grpcInsertContent);
      if (entry.isImplGrpc()) {
        insertContent = insertContent.replace(RETURN_RESPONSE, RESPONSE);
      } else {
        insertContent = insertContent.replace(RETURN_RESPONSE, "nil");
      }
      insertLines.append(insertContent);
    } else {
      String grpcSelectContent = "";
      if (entry.isImplGrpc()) {
        grpcSelectContent = insertContent(serviceGrpcSelectContent, entry);
      }
      String selectContent = insertContent(serviceSelectContent, entry) + "\n";
      selectContent = selectContent.replace("@servicegrpc_select@", grpcSelectContent);
      if (entry.isImplGrpc()) {
        selectContent = selectContent.replace(RETURN_RESPONSE, RESPONSE);
      } else {
        selectContent = selectContent.replace(RETURN_RESPONSE, "nil");
      }
      insertLines.append(selectContent);
    }
  }

  private String insertContent(String executorContent, AppConfigVO entry) {

    String insertContent = executorContent.replace(TITLE, entry.getTitle());
    insertContent = insertContent.replace(SERVICE_REQ, entry.getReqName());
    insertContent = insertContent.replace(SERVICE_RES, entry.getResName());
    insertContent = insertContent.replace("@onrequest_hook@", entry.getTitle());
    insertContent = insertContent.replace("@onerror_hook@", entry.getTitle());
    insertContent = insertContent.replace("@ondata_hook@", entry.getTitle());
    return insertContent;
  }

  @Override
  public void onEnd() {
    super.onEnd();
    for (AstBase b : parts) b.onEnd();

    File pj = new File(getGoGeneratePath() + "/service");
    if (!pj.exists()) pj.mkdirs();

    String namespace = super.getNamespace();
    if (StringUtils.isNonEmpty(namespace)) {
      namespace = "/" + namespace;
    }

    if (flags.isContracts()) {
        ifFlagsIsContracts();
        // \"code.nurture.farm/Core/Contracts/Common/Gen/GoCommon\"");
    } else {
      serviceContent =
          serviceContent.replace(
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
      writeStringServiceContent(namespace);
  }

    private void writeStringServiceContent(String namespace) {
        serviceContent = serviceContent.replace(CODE_URL, super.getCodeUrl());
        serviceContent = serviceContent.replace(NAME_SPACE, namespace);
        serviceContent = serviceContent.replace(SERVICE_NAME, super.getServiceName());
        serviceContent = serviceContent.replace("@insert_impl@", insertLines.toString());
        serviceContent = serviceContent.replace(GO_PACKAGE, super.getGoPackage());
        serviceContent = serviceContent.replace("@insert_constants@", insertConstantLines.toString());
        writeToFile(super.getGoGeneratePath() + "/service/service.go", serviceContent);
    }

    private void ifFlagsIsContracts() {
        serviceContent =
            serviceContent.replace(
                GO_PROTO_PATH,
                "\"code.nurture.farm/Core/Contracts/"
                    + super.getServiceName()
                    + "/Gen/Go"
                    + super.getServiceName()
                    + "\"");
        //                      + "\"\n\tcommon
    }


}
