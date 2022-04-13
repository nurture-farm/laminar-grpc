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
import static farm.nurture.laminar.generator.Constants.DATETIME;
import static farm.nurture.laminar.generator.Constants.ENUM;
import static farm.nurture.laminar.generator.Constants.FLOAT64;
import static farm.nurture.laminar.generator.Constants.INT32;
import static farm.nurture.laminar.generator.Constants.INT64;
import static farm.nurture.laminar.generator.Constants.STRING;
import static farm.nurture.laminar.generator.Constants.TIMESTAMP;

import farm.nurture.infra.util.Logger;
import farm.nurture.infra.util.LoggerFactory;
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
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AstModel extends AstBase {

    private List<AstBase> parts = new ArrayList<>();
    private Set<String> modelSet = new HashSet<>();
    private StringBuilder modelBuf = new StringBuilder();
    private static final Logger logger = LoggerFactory.getLogger(AstModel.class);

    @Override
    public void onStart(Configuration conf, Flags flags) {
        super.onStart(conf, flags);
        for (AstBase b : parts) b.onStart(conf, flags);
    }

    @Override
    public void onEntry(
        AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields) {

        for (AstBase b : parts) b.onEntry(entry, requestFields, responseFields);
        if (entry.isImplDao()) {
            Boolean isMutation = entry.isMutation();
            if (Boolean.TRUE.equals(isMutation)) {
                parameterHandling(
                    isMutation, entry.getReqName(), entry.getResName(), requestFields, modelBuf);
            } else {
                parameterHandling(
                    isMutation, entry.getReqName(), entry.getResName(), responseFields, modelBuf);

                parameterHandling(
                    true, entry.getReqName(), entry.getResName(), requestFields, modelBuf);
            }
        }
    }

    @Override
    public void onEnd() {
        super.onEnd();
        for (AstBase b : parts) b.onEnd();

        String mapperContent =
            readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/database/models/models.go.template");
        mapperContent = mapperContent.replace("@insert_models@", modelBuf.toString());

        File pj = new File(getGoGeneratePath() + "/database/models");
        if (!pj.exists()) pj.mkdirs();

        writeToFile(super.getGoGeneratePath() + "/database/models/models.go", mapperContent);
    }

    void parameterHandling(
        Boolean isMutation,
        String requestName,
        String responseName,
        List<FieldDetail> paramDetails,
        StringBuilder modelBuf) {

        if (Boolean.TRUE.equals(isMutation)) {
            if (modelSet.contains(requestName)) return;
            modelBuf.append("type ").append(requestName).append("VO struct {\n\t\t");
            modelSet.add(requestName);
        } else {
            if (modelSet.contains(responseName)) return;
            modelBuf.append("type ").append(responseName).append("VO struct {\n\t\t");
            modelSet.add(responseName);
        }

        ifParamDetailsAreNonEmpty(isMutation, paramDetails, modelBuf);
        modelBuf.append("\n}\n");
    }

    private void ifParamDetailsAreNonEmpty(
        Boolean isMutation, List<FieldDetail> paramDetails, StringBuilder modelBuf) {
        if (!paramDetails.isEmpty()) {
            boolean isFirst = true;
            for (FieldDetail aParam : paramDetails) {
                String fldName = aParam.getFldNameCamel();
                if (isFirst) isFirst = false;
                else modelBuf.append("\n\t\t");

                String paramName = Character.toUpperCase(fldName.charAt(0)) + fldName.substring(1);
                modelBuf.append(paramName);
                processParamGoType(isMutation, modelBuf, aParam);
            }
        }
    }

    private void processParamGoType(Boolean isMutation, StringBuilder modelBuf, FieldDetail aParam) {
        if (aParam.getGoType().equals(STRING) || aParam.getGoType().startsWith(ENUM) || aParam.getGoType().equals(TIMESTAMP)) {
            modelBuf.append("\t\t\tsql.NullString");
        }  else if (aParam.getGoType().equals(BOOL)) {
            modelBuf.append("\t\t\tsql.NullBool");
        } else if (aParam.getGoType().equals(DATETIME)) {
            modelBuf.append("\t\t\tsql.NullTime");
        } else if (aParam.getGoType().equals(INT32)) {
            modelBuf.append("\t\t\tsql.NullInt32");
        } else if (aParam.getGoType().equals(INT64)) {
            modelBuf.append("\t\t\tsql.NullInt64");
        } else if (aParam.getGoType().equals(FLOAT64)) {
            modelBuf.append("\t\t\tsql.NullFloat64");
        } else if (aParam.getGoType().equals("[]string")) {
            if (Boolean.TRUE.equals(isMutation)) {
                modelBuf.append("\t\t\t[]sql.NullString");
            }
        } else {
            logger.error("TODO2 data type" + aParam.getGoType());
            System.exit(1);
        }
    }
}
