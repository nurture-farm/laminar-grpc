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

import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.FieldDetail;
import farm.nurture.laminar.generator.ast.AstBase;

import java.util.List;

public class AstProtoRpcServices extends AstBase {

    StringBuilder protoCallableServices;

    public AstProtoRpcServices(StringBuilder protoCallableServices) {
        this.protoCallableServices = protoCallableServices;
    }

    @Override
    public void onEntry(AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields){

        if (entry.isDeclGrpc()) {
            protoCallableServices.append("\n\trpc Execute").append(entry.getTitle()).append(" (")
                .append(entry.getReqName()).append(" ) returns (").append(entry.getResName())
                .append(") {}");

            if ( entry.isMutation()  )  protoCallableServices.append("\n\trpc Execute")
                .append(entry.getBulkTitle()).append(" ( ").append(entry.getBulkReqName())
                .append(" ) returns ( ").append(entry.getBulkResName()).append(" ) {}");
        }
    }
}
