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

import static farm.nurture.laminar.generator.Constants.JAVA_PACKAGE;

import farm.nurture.laminar.generator.*;
import farm.nurture.laminar.generator.ast.AstBase;

import java.util.ArrayList;
import java.util.List;

public class AstGrpcClient extends AstBase {

    StringBuilder clientSelectContent = new StringBuilder(1024 * 64);
    StringBuilder clientInsertContent = new StringBuilder(1024 * 64);

    List<AstBase> parts = new ArrayList<>();
    public AstGrpcClient() {
        parts.add(new AstGrpcClientInsert(clientInsertContent));
        parts.add(new AstGrpcClientSelect(clientSelectContent));
    }

    @Override
    public void onStart(Configuration conf, AstBase.Flags flags){
        super.onStart(conf, flags);
        for ( AstBase b: parts) b.onStart(conf, flags);
    }

    @Override
    public void onEntry(AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields){

        int reqOverrideT = (null == entry.getReqOverride() ) ? 0 : entry.getReqOverride().trim().length();
        int resOverrideT = (null == entry.getResOverride() ) ? 0 : entry.getResOverride().trim().length();
        if ( reqOverrideT > 0 || resOverrideT > 0 ) return;

        for ( AstBase b: parts) b.onEntry(entry, requestFields, responseFields);
    }

    @Override
    public void onEnd(){
        super.onEnd();
        for ( AstBase b: parts) b.onEnd();


        writeToFile(getJavaPackageDirectory() + "/GRpcClient.java", TemplatesVO.getServiceGrpcClient()
                .replace("@insertservice@", clientInsertContent.toString())
                .replace("@selectservice@", clientSelectContent.toString())
                .replace("@microservice_name@", getServiceName())
                .replace(JAVA_PACKAGE, getJavaPackage())
        );
    }
}
