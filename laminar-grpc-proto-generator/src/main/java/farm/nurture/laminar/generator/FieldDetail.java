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

package farm.nurture.laminar.generator;

import farm.nurture.infra.util.Logger;
import farm.nurture.infra.util.LoggerFactory;
import farm.nurture.laminar.core.util.CaseUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldDetail {

    private String fieldName;
    private String javaType;
    private String goType;
    private String protoType;
    private String messageType;
    private String fldNameCamel;
    private boolean repeated = false;
    private static final Logger logger = LoggerFactory.getLogger(FieldDetail.class);

    public FieldDetail(String fieldName, String protoType, String javaType, String goType) {
        this.setFieldName(fieldName);
        this.setJavaType(javaType);
        this.setProtoType(protoType);
        this.setGoType(goType);
        this.setFldNameCamel(CaseUtils.toCamelCase(this.getFieldName(), false, '_', '.'));
        logger.info("*****:" + this.getFieldName() + "\t\t" + this.getFldNameCamel());
    }
}
