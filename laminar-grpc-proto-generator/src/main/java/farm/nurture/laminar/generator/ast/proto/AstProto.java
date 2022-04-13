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

import static farm.nurture.laminar.generator.Constants.DOT_PROTO;
import static farm.nurture.laminar.generator.Constants.FORWARD_SLASH;
import static farm.nurture.laminar.generator.Constants.GO_PACKAGE;
import static farm.nurture.laminar.generator.Constants.JAVA_PACKAGE;

import farm.nurture.laminar.core.util.CaseUtils;
import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.Configuration;
import farm.nurture.laminar.generator.FieldDetail;
import farm.nurture.laminar.generator.ProtoGeneratorShowtables;
import farm.nurture.laminar.generator.TemplatesVO;
import farm.nurture.laminar.generator.ast.AstBase;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AstProto extends AstBase {

  private StringBuilder protoCallableMethods = new StringBuilder(1024 * 64);
  private StringBuilder protoOneofRequests = new StringBuilder(1024 * 4);
  private StringBuilder protoOneofResponses = new StringBuilder(1024 * 4);
  private StringBuilder protoCallableServices = new StringBuilder(1024);
  private StringBuilder bazelBuildResource = new StringBuilder(1024 * 4);
  private AstBase.Flags flags;
  private List<AstBase> parts = new ArrayList<>();
  private static final Logger logger = LoggerFactory.getLogger(AstProto.class);

  public AstProto() {
    parts.add(new AstProtoRpcServices(protoCallableServices));
    parts.add(new AstProtoRpcMethods(protoCallableMethods));
    parts.add(new AstProtoOneOf(protoOneofRequests, protoOneofResponses));
    parts.add(new AstBazel(bazelBuildResource));
  }

  @Override
  public void onStart(Configuration conf, AstBase.Flags flags) {
    super.onStart(conf, flags);
    this.flags = flags;
    for (AstBase b : parts) b.onStart(conf, flags);
  }

  @Override
  public void onEntry(
      AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields) {
    for (AstBase b : parts) b.onEntry(entry, requestFields, responseFields);
  }

  @Override
  public void onEnd() {
    super.onEnd();
    for (AstBase b : parts) b.onEnd();

    String importContract = getImportContract();

    String importGoPackage = getGoPackage() + FORWARD_SLASH + getServiceName();
    String protoStatus = "";
    String commonProto = "";
    if (flags.isContracts()) {
      importGoPackage = "code.nurture.farm/Core/Contracts/" + getServiceName();
      protoStatus = "farm.nurture.core.contracts.common.RequestStatusResult";
    } else {
      commonProto = TemplatesVO.getCommonProto();
      protoStatus = "Status";
    }

    String protoFileContent =
        getProtoFileContent(importContract, importGoPackage, protoStatus, commonProto);

    if (flags.isGoLang()) {
      ifIsGOLang(protoFileContent);
    }

    if (flags.isJavaLang()) {
      isIsJavaLang(protoFileContent);
    }
  }

  private void isIsJavaLang(String protoFileContent) {
    writeToFile(
        getJavaGeneratePath()
            + FORWARD_SLASH
            + getProtoGeneratePath()
            + FORWARD_SLASH
            + CaseUtils.camelToSnake(getServiceName())
            + DOT_PROTO,
        protoFileContent);
  }

  private void ifIsGOLang(String protoFileContent) {
    if (flags.isContracts()) {
      writeToFile(
          getGoGeneratePath()
              + FORWARD_SLASH
              + getProtoGeneratePath()
              + FORWARD_SLASH
              + CaseUtils.camelToSnake(getServiceName())
              + DOT_PROTO,
          protoFileContent);
      writeToFile(
          getGoGeneratePath() + FORWARD_SLASH + getProtoGeneratePath() + "/BUILD.bazel",
          bazelBuildResource.toString());
      // fire bazel command
    } else {
      writeToFile(
          getGoGeneratePath()
              + FORWARD_SLASH
              + getProtoGeneratePath()
              + FORWARD_SLASH
              + CaseUtils.camelToSnake(getServiceName())
              + DOT_PROTO,
          protoFileContent);
    }
  }

  private String getImportContract() {
    String importContract = "";
    //        if(AppConfigVO.isContractUsed) importContract = "import \"Common/enums.proto\";\n";
    if (flags.isContracts()) {
      importContract = "import \"Common/enums.proto\";\n";
      importContract += "import \"Common/utils.proto\";\n";
      importContract += "import \"Common/headers.proto\";\n";
      importContract += "import \"Common/entities.proto\";\n";
      importContract += "import \"Common/event_reference.proto\";\n";
      importContract += "import \"Common/time_slot.proto\";\n";
      importContract += "import \"Common/tags.proto\";";
    }
    return importContract;
  }

  private String getProtoFileContent(
      String importContract, String importGoPackage, String protoStatus, String commonProto) {

    String protoFileContent = TemplatesVO.getLaminarProto()
        .replace("@callable_services@", protoCallableServices)
        .replace("@callable_methods@", protoCallableMethods)
        .replace("@oneof_requests@", protoOneofRequests)
        .replace("@oneof_responses@", protoOneofResponses)
        .replace("@microservice_name@", getServiceName())
        .replace("@proto_package@", getProtoPackage())
        .replace(JAVA_PACKAGE, getJavaPackage())
        .replace(GO_PACKAGE, importGoPackage)
        .replace("@import_contract@", importContract)
        .replace("@common_proto@", commonProto)
        .replace("@proto_status@", protoStatus);

    if (flags.isProtoTimestampUsed()){
      protoFileContent = protoFileContent.replace("@import_proto_timestamp@", "import \"google/protobuf/timestamp.proto\";");
    }
    else{
      protoFileContent = protoFileContent.replace("@import_proto_timestamp@","");
    }
    return protoFileContent;
  }
}
