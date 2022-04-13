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

import java.util.ArrayList;
import java.util.List;

public class AstProtoOneOf extends AstBase {

    List<AppConfigVO> entries = new ArrayList<>();

    StringBuilder protoOneofRequests;
    StringBuilder protoOneofResponses;

    public AstProtoOneOf( StringBuilder protoOneofRequests, StringBuilder protoOneofResponses) {
        this.protoOneofRequests = protoOneofRequests;
        this.protoOneofResponses = protoOneofResponses;
    }

    @Override
    public void onEntry(AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields){

        if (entry.isDeclGrpc()) {
            entries.add(entry);
        }
    }

    @Override
    public void onEnd(){
        int index = 1;
        for (AppConfigVO entry : entries) {
            protoOneofRequests.append("\t\t").append(
                    entry.getReqName()).append(" req").append(entry.getTitle()).append(" = ").
                    append(++index).append(";\n");

            if ( entry.isMutation()) {
                protoOneofRequests.append("\t\t").append(entry.getBulkReqName()).
                        append(" req").append(entry.getBulkTitle()).append(" = ").append(++index).append(";\n");
            }
        }

        index = 2;
        for (AppConfigVO entry : entries) {
            protoOneofResponses.append("\t\t").append(entry.getResName()).
                    append(" res").append(entry.getTitle()).append(" = ").append(++index).append(";\n");
            if ( entry.isMutation()) {
                protoOneofResponses.append("\t\t").append(entry.getBulkResName()).
                    append(" res").append(entry.getBulkTitle()).append(" = ").append(++index).append(";\n");
            }
        }
    }


}
