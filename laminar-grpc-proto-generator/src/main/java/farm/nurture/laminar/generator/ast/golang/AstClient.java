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

import static farm.nurture.laminar.generator.Constants.FORWARD_SLASH;
import static farm.nurture.laminar.generator.Constants.GO_PROTO_PATH;
import static farm.nurture.laminar.generator.Constants.SERVER_PORT;
import static farm.nurture.laminar.generator.Constants.SERVICE_NAME;
import static farm.nurture.laminar.generator.Constants.SERVICE_REQ;
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

public class AstClient extends AstBase {

    List<AstBase> parts = new ArrayList<>();
    String grpcClientContent;
    String clientContent;
    String clientBulkContent;

    StringBuilder insertTestClients = new StringBuilder();

    AstBase.Flags flags;

    public AstClient() {
        grpcClientContent = readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/grpcClient.go.template");
        clientContent = readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/client.go.template");
        clientBulkContent = readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/clientBulk.go.template");
    }

    @Override
    public void onStart(Configuration conf, AstBase.Flags flags ){
        super.onStart(conf, flags);
        this.flags = flags;
        for ( AstBase b: parts) b.onStart(conf,flags);
    }

    @Override
    public void onEntry(AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields){

        for ( AstBase b: parts) b.onEntry(entry, requestFields, responseFields);

        String insertContent = clientContent.replace(TITLE, entry.getTitle());
        insertContent = insertContent.replace(SERVICE_NAME, super.getServiceName());
        insertContent = insertContent.replace(SERVICE_REQ, entry.getReqName());
        insertTestClients.append(insertContent+"\n");
        if (entry.getMutation().equals("I")) {
            String insertBulkContent = clientBulkContent.replace(TITLE, entry.getTitle());
            insertBulkContent = insertBulkContent.replace(SERVICE_NAME, super.getServiceName());
            insertBulkContent = insertBulkContent.replace(SERVICE_REQ, entry.getReqName());
            insertTestClients.append(insertBulkContent+"\n");
        }
    }

    @Override
    public void onEnd(){
        super.onEnd();
        for ( AstBase b: parts) b.onEnd();

        String namespace = super.getNamespace();
        if(StringUtils.isNonEmpty(namespace)) {
            namespace = FORWARD_SLASH+namespace;
        }

        if(flags.isContracts()) {
            grpcClientContent = grpcClientContent.replace(GO_PROTO_PATH,"\"code.nurture.farm/Core/Contracts/"+ super.getServiceName()
                +"/Gen/Go"+ super.getServiceName() +"\"");
        } else {
            grpcClientContent = grpcClientContent.replace(GO_PROTO_PATH,"\""+ super.getCodeUrl()
                +namespace+FORWARD_SLASH+ super.getServiceName()
                +"/zerotouch/golang/proto/"+ super.getGoPackage() +FORWARD_SLASH+ super.getServiceName() +"\"");
        }
        grpcClientContent = grpcClientContent.replace(SERVER_PORT, Integer.toString(super.getConf().getServer().getPort()));
        grpcClientContent = grpcClientContent.replace("@insert_test_clients@", insertTestClients.toString());
        File pj = new File(getServiceName() + "/grpcClient");
        if (!pj.exists()) pj.mkdirs();
        writeToFile(getServiceName() + "/grpcClient/grpcClient.go", grpcClientContent);
    }
}
