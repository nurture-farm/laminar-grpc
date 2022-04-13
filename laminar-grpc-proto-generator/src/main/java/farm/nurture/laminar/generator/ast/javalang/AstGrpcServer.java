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
import static farm.nurture.laminar.generator.Constants.SERVICE_NAME;
import static farm.nurture.laminar.generator.Constants.SERVICE_REQ;
import static farm.nurture.laminar.generator.Constants.SERVICE_RES;

import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.Configuration;
import farm.nurture.laminar.generator.FieldDetail;
import farm.nurture.laminar.generator.TemplatesVO;
import farm.nurture.laminar.generator.ast.AstBase;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AstGrpcServer extends AstBase {

    private StringBuilder serverSelectContent = new StringBuilder(1024 * 64);
    private StringBuilder serverInsertContent = new StringBuilder(1024 * 64);
    private StringBuilder multiExecuteSb = new StringBuilder(1024);
    private StringBuilder serviceMethodsSb = new StringBuilder(1024);
    private List<AstBase> parts = new ArrayList<>();

    public AstGrpcServer() {
        parts.add(new AstGrpcServerInsert(serverInsertContent));
        parts.add(new AstGrpcServerSelect(serverSelectContent));
    }

    @Override
    public void onStart(Configuration conf, AstBase.Flags flags) {
        super.onStart(conf, flags);
        for (AstBase b : parts) b.onStart(conf, flags);
    }

    @Override
    public void onEntry(
        AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields) {

        for (AstBase b : parts) b.onEntry(entry, requestFields, responseFields);

        if (entry.isDeclGrpc()) {
            ifIsDeclGrpc(entry);
        }
        if (entry.isImplGrpc()) {
            ifIsImplGrpc(entry);
        }
    }

    private void ifIsImplGrpc(AppConfigVO entry) {
        String serviceMethod =
            "\nvoid @service_name@(WriteBase writer, AbstractBackendController.ReqRes<@service_request@, @service_response@> reqres,\n"
                + "    List<AbstractBackendController.ReqRes<Request,Response>> prevSteps, ServiceContext context);\n";
        serviceMethod =
            serviceMethod
                .replace(SERVICE_NAME, entry.getTitle())
                .replace(SERVICE_REQ, entry.getReqName())
                .replace(SERVICE_RES, entry.getResName());
        serviceMethodsSb.append(serviceMethod);

        if (entry.isMutation()) {
            String bulkServiceMethod =
                "\n            void @service_name@Bulk(WriteBase writer, AbstractBackendController.ReqRes<Bulk@service_request@, Bulk@service_response@> reqres,\n"
                    + "                    List<AbstractBackendController.ReqRes<Request,Response>> prevSteps, ServiceContext context);\n";
            bulkServiceMethod =
                bulkServiceMethod
                    .replace(SERVICE_NAME, entry.getTitle())
                    .replace(SERVICE_REQ, entry.getReqName())
                    .replace(SERVICE_RES, entry.getResName());
            serviceMethodsSb.append(bulkServiceMethod);
        }
    }

    private void ifIsDeclGrpc(AppConfigVO entry) {
        String commentImpl = (entry.isImplGrpc()) ? "" : "//";
        if (entry.isMutation()) {
            multiExecuteSb.append(
                TemplatesVO.getServiceGrpcServerMultiBulk().replace(SERVICE_NAME, entry.getTitle())
                    .replace(SERVICE_REQ, entry.getReqName())
                    .replace("@grpc_impl@", commentImpl)
                    .replace(SERVICE_RES, entry.getResName()));
        }

        multiExecuteSb.append(
            TemplatesVO.getServiceGrpcServerMulti().replace(SERVICE_NAME, entry.getTitle())
                .replace(SERVICE_REQ, entry.getReqName())
                .replace("@grpc_impl@", commentImpl)
                .replace(SERVICE_RES, entry.getResName()));
    }

    @Override
    public void onEnd() {
        super.onEnd();
        for (AstBase b : parts) b.onEnd();
        writeGrpcServer();

        writeToFile(
            getJavaPackageDirectory() + "/AbstractStartupHook.java",
            TemplatesVO.getAbstractStartupHook().replace(JAVA_PACKAGE, getJavaPackage()));

        writeToFile(
            getJavaPackageDirectory() + "/ServiceMethods.java",
            TemplatesVO.getServiceMethods().replace(JAVA_PACKAGE, getJavaPackage())
                .replace("@grpc_methods@", serviceMethodsSb.toString()));

        writeToFile(
            getJavaPackageDirectory() + "/ServiceContext.java",
            TemplatesVO.getServiceContext().replace(JAVA_PACKAGE, getJavaPackage()));
    }

    void writeGrpcServer() {
        writeToFile(
            getJavaPackageDirectory() + "/GRpcServer.java",
            TemplatesVO.getServiceGrpcServer().replace("@multiservice@", multiExecuteSb.toString())
                .replace("@insertservice@", serverSelectContent.toString())
                .replace("@selectservice@", serverInsertContent.toString())
                .replace("@microservice_name@", getServiceName())
                .replace(JAVA_PACKAGE, getJavaPackage()));
    }
}
