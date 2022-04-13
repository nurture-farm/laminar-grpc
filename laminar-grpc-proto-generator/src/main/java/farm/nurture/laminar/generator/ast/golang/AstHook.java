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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AstHook extends AstBase {

  private List<AstBase>  parts = new ArrayList<>();
  private Set<String> ServiceRequestSet = new HashSet<>();
  private String helperContent;
  private String helperSelectContent;
  private String helperInsertContent;
  private String helperSelectInitContent;
  private String helperInsertInitContent;

  private StringBuilder helperInsertBuf = new StringBuilder();
  private StringBuilder helperInsertInitBuf = new StringBuilder();

  private List<StringBuilder> insertContents = new ArrayList<>();
  private List<String> fileNames = new ArrayList<>();

  AstBase.Flags flags;

  public AstHook() {
    helperContent = readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/hook.go.template");
    helperSelectContent =
        readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/hook.select.template");
    helperInsertContent =
        readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/hook.insert.template");
    helperSelectInitContent =
        readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/hook.select.init.template");
    helperInsertInitContent =
        readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/hook.insert.init.template");
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

    String namespace = super.getNamespace();
    if (StringUtils.isNonEmpty(namespace)) {
      namespace = FORWARD_SLASH + namespace;
    }

    String helperInsertContentL = null;
    if (!ServiceRequestSet.contains(entry.getTitle())) {
      helperInsertContentL = getString(entry, namespace);
    }

    if (helperInsertContentL != null) {

      //            helperInsertBuf.append(helperInsertContentL);
      StringBuilder contentBuilder = new StringBuilder();
      contentBuilder.append(helperInsertContentL);
      insertContents.add(contentBuilder);
      fileNames.add(entry.getTitle() + ".go");
    }
  }

  private String getString(AppConfigVO entry, String namespace) {
    String helperInsertContentL;
      helperInsertContentL = getStringHelperInsertContentL(entry);
      if (flags.isContracts()) {
      helperInsertContentL =
          helperInsertContentL.replace(
              GO_PROTO_PATH,
              "\"code.nurture.farm/Core/Contracts/"
                  + super.getServiceName()
                  + "/Gen/Go"
                  + super.getServiceName()
                  + "\"");
    } else {
      helperInsertContentL =
          helperInsertContentL.replace(
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
    ServiceRequestSet.add(entry.getReqName());
    return helperInsertContentL;
  }

    private String getStringHelperInsertContentL(AppConfigVO entry) {
        String helperInsertContentL;
        if (entry.isMutation()) {
          helperInsertInitBuf.append(helperInsertInitContent.replace(SERVICE_REQ, entry.getTitle()));
          helperInsertContentL = helperInsertContent.replace(SERVICE_NAME, getServiceName());
          helperInsertContentL = helperInsertContentL.replace(TITLE, entry.getTitle());
          helperInsertContentL = helperInsertContentL.replace(SERVICE_REQ, entry.getTitle());
          helperInsertContentL = helperInsertContentL.replace(CODE_URL, getCodeUrl());
          helperInsertContentL = helperInsertContentL.replace("@request_name@", entry.getReqName());
          helperInsertContentL = helperInsertContentL.replace(SERVICE_RES, entry.getResName());
        } else {
          helperInsertInitBuf.append(helperSelectInitContent.replace(SERVICE_REQ, entry.getTitle()));
          helperInsertContentL = helperSelectContent.replace(SERVICE_NAME, getServiceName());
          helperInsertContentL = helperInsertContentL.replace(TITLE, entry.getTitle());
          helperInsertContentL = helperInsertContentL.replace(SERVICE_REQ, entry.getTitle());
          helperInsertContentL = helperInsertContentL.replace(CODE_URL, getCodeUrl());
          helperInsertContentL = helperInsertContentL.replace("@request_name@", entry.getReqName());
          helperInsertContentL = helperInsertContentL.replace(SERVICE_RES, entry.getResName());
        }
        return helperInsertContentL;
    }

    @Override
  public void onEnd() {
    super.onEnd();
    for (AstBase b : parts) b.onEnd();

    File pj = new File(getGoOverridePath() + "/hook");
    if (!pj.exists()) pj.mkdirs();

    String helperContentL = helperContent.replace("@insert_init@", helperInsertInitBuf.toString());
    helperContentL = helperContentL.replace(CODE_URL, super.getCodeUrl());
    helperContentL = helperContentL.replace(SERVICE_NAME, super.getServiceName());
    helperContentL = helperContentL.replace(GO_PACKAGE, super.getGoPackage() + FORWARD_SLASH + super.getServiceName() + "\"");
    helperContentL = helperContentL.replace("@insert@", helperInsertBuf.toString());

    writeToFile(super.getGoOverridePath() + "/hook/hook.go", helperContentL);
    for (int i = 0; i < fileNames.size(); i++) {
      File f = new File(super.getGoOverridePath() + "/hook/" + fileNames.get(i));
      if (!f.exists()) {
        // Write file only if it doesn't exist otherwise dont override file
        writeToFile(
            super.getGoOverridePath() + "/hook/" + fileNames.get(i),
            insertContents.get(i).toString());
      }
    }
  }
}
