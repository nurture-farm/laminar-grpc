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

import farm.nurture.laminar.generator.ast.AstBase;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TemplatesVO {

    private TemplatesVO(){}

    public static final String TEMPLATE_DIR = "/templates";

    private static String CONFIGURATION_F = TEMPLATE_DIR + "/Configuration.template";

    private static String ABSTRACT_STARTUP_HOOK_F = TEMPLATE_DIR + "/java/AbstractStartupHook.template";
    private static String SERVICE_CONTEXT_F = TEMPLATE_DIR + "/java/ServiceContext.template";
    private static String SERVICE_GRPC_SERVER_F = TEMPLATE_DIR + "/java/ServiceGrpcServer.template";
    private static String SERVICE_GRPC_SERVER_MULTI_F = TEMPLATE_DIR + "/java/ServiceGrpcServerMulti.template";
    private static String SERVICE_GRPC_SERVER_MULTI_BULK_F = TEMPLATE_DIR + "/java/ServiceGrpcServerMultiBulk.template";
    private static String SERVICE_METHOD_F = TEMPLATE_DIR + "/java/ServiceMethods.template";

    private static String SQL_STMTS_F = TEMPLATE_DIR + "/java/SqlStmts.template";
    private static String SERVICE_SQL_READER_F = TEMPLATE_DIR + "/java/ServiceSqlReader.template";
    private static String SERVICE_VOF = TEMPLATE_DIR + "/java/ServiceVO.template";

    private static String SERVICE_GRPC_CLIENT_F = TEMPLATE_DIR + "/java/ServiceGrpcClient.template";
    private static String SERVICE_GRPC_CLIENT_INSERT_F = TEMPLATE_DIR + "/java/ServiceGrpcClientInsert.template";
    private static String SERVICE_GRPC_CLIENT_SELECT_F = TEMPLATE_DIR + "/java/ServiceGrpcClientSelect.template";

    private static String PROTO_IMMUTABLE_REQUEST_F = TEMPLATE_DIR + "/proto/proto.immutable.request.template";
    private static String PROTO_IMMUTABLE_CONTRACT_REQUEST_F = TEMPLATE_DIR + "/proto/proto.immutable.contract.request.template";
    private static String LAMINAR_PROTO_F = TEMPLATE_DIR + "/proto/laminar.proto.template";
    private static String COMMON_PROTO_F = TEMPLATE_DIR + "/proto/common.proto.template";
    private static String PROTO_MUTABLE_REQUEST_F = TEMPLATE_DIR + "/proto/proto.mutable.request.template";
    private static String PROTO_MUTABLE_CONTRACT_REQUEST_F = TEMPLATE_DIR + "/proto/proto.mutable.contract.request.template";
    private static String PROTO_MUTABLE_RESPONSE_F = TEMPLATE_DIR + "/proto/proto.mutable.response.template";
    private static String PROTO_MUTABLE_CONTRACT_RESPONSE_F = TEMPLATE_DIR + "/proto/proto.mutable.contract.response.template";
    private static String PROTO_IMMUTABLE_RESPONSE_F = TEMPLATE_DIR + "/proto/proto.immutable.response.template";
    private static String PROTO_IMMUTABLE_CONTRACT_RESPONSE_F = TEMPLATE_DIR + "/proto/proto.immutable.contract.response.template";

    private static String GRPC_CLIENT_MODULE_F = TEMPLATE_DIR + "/graphql/GRpcClientModule.template";
    private static String GRAPHQL_SCHEMA_MODULE_F = TEMPLATE_DIR + "/graphql/GraphqlSchemaModule.template";
    private static String GRAPHQL_SCHEMA_MODULE_BULK_F = TEMPLATE_DIR + "/graphql/GraphqlSchemaModuleBulk.template";
    private static String GRAPHQL_SCHEMA_MODULE_OVERRIDE_F = TEMPLATE_DIR + "/graphql/GraphqlSchemaModuleOverride.template";
    private static String GRAPHQL_RESOURCE_F = TEMPLATE_DIR + "/graphql/GraphqlResource.template";
    private static String GRAPHQL_WEB_RESOURCE_F = TEMPLATE_DIR + "/graphql/GraphqlWebResource.template";
    private static String GRAPHQL_RESOURCE_MODULE_F = TEMPLATE_DIR + "/graphql/GraphqlResourceModule.template";
    private static String GRAPHQL_SERVER_F = TEMPLATE_DIR + "/graphql/GraphqlServer.template";
    private static String GRAPHQL_REQUEST_F = TEMPLATE_DIR + "/graphql/GraphqlRequest.template";
    private static String GRAPHQL_CORS_FILTER_F = TEMPLATE_DIR + "/graphql/CORSFilter.template";
    private static String GRAPHQL_OAUTH_VALIDATOR_F = TEMPLATE_DIR + "/graphql/OAuthValidator.templates";
    private static String INDEX_HTML_F = TEMPLATE_DIR + "/graphql/index.html.template";

    private static String abstractStartupHook = readTemplateFile(ABSTRACT_STARTUP_HOOK_F);
    private static String serviceContext = readTemplateFile(SERVICE_CONTEXT_F);

    private static String configuration = readTemplateFile(CONFIGURATION_F);
    private static String serviceGrpcServer = readTemplateFile(SERVICE_GRPC_SERVER_F);
    private static String serviceGrpcServerMulti = readTemplateFile(SERVICE_GRPC_SERVER_MULTI_F);
    private static String protoImmutableRequest = readTemplateFile(PROTO_IMMUTABLE_REQUEST_F);
    private static String protoImmutableContractRequest = readTemplateFile(PROTO_IMMUTABLE_CONTRACT_REQUEST_F);
    private static String serviceGrpcServerMultiBulk = readTemplateFile(SERVICE_GRPC_SERVER_MULTI_BULK_F);
    private static String serviceMethods = readTemplateFile(SERVICE_METHOD_F);

    private static String sqlStmts = readTemplateFile(SQL_STMTS_F);
    private static String serviceSqlReader = readTemplateFile(SERVICE_SQL_READER_F);
    private static String serviceVO = readTemplateFile(SERVICE_VOF);

    private static String serviceGrpcClient = readTemplateFile(SERVICE_GRPC_CLIENT_F);
    private static String serviceGrpcClientInsert = readTemplateFile(SERVICE_GRPC_CLIENT_INSERT_F);
    private static String serviceGrpcClientSelect = readTemplateFile(SERVICE_GRPC_CLIENT_SELECT_F);

    private static String laminarProto = readTemplateFile(LAMINAR_PROTO_F);
    private static String commonProto = readTemplateFile(COMMON_PROTO_F);
    private static String protoMutableRequest = readTemplateFile(PROTO_MUTABLE_REQUEST_F);
    private static String protoMutableContractRequest = readTemplateFile(PROTO_MUTABLE_CONTRACT_REQUEST_F);
    private static String protoMutableResponse = readTemplateFile(PROTO_MUTABLE_RESPONSE_F);
    private static String protoMutableContractResponse = readTemplateFile(PROTO_MUTABLE_CONTRACT_RESPONSE_F);
    private static String protoImmutableResponse = readTemplateFile(PROTO_IMMUTABLE_RESPONSE_F);
    private static String protoImmutableContractResponse = readTemplateFile(PROTO_IMMUTABLE_CONTRACT_RESPONSE_F);

    private static String grpcClientModule = readTemplateFile(GRPC_CLIENT_MODULE_F);
    private static String graphqlSchemaModule = readTemplateFile(GRAPHQL_SCHEMA_MODULE_F);
    private static String graphqlSchemaModuleOverride = readTemplateFile(GRAPHQL_SCHEMA_MODULE_OVERRIDE_F);
    private static String graphqlSchemaModuleBulk = readTemplateFile(GRAPHQL_SCHEMA_MODULE_BULK_F);
    private static String graphqlResource = readTemplateFile(GRAPHQL_RESOURCE_F);
    private static String graphqlWebResource = readTemplateFile(GRAPHQL_WEB_RESOURCE_F);
    private static String graphqlResourceModule = readTemplateFile(GRAPHQL_RESOURCE_MODULE_F);
    private static String graphqlServer = readTemplateFile(GRAPHQL_SERVER_F);
    private static String graphqlRequest = readTemplateFile(GRAPHQL_REQUEST_F);
    private static String graphqlCORSFilter = readTemplateFile(GRAPHQL_CORS_FILTER_F);
    private static String graphqlOAuthValidator = readTemplateFile(GRAPHQL_OAUTH_VALIDATOR_F);
    private static String indexHtml = readTemplateFile(INDEX_HTML_F);

    public static String readTemplateFile(String fileName) {
        StringBuilder dataBuilder = new StringBuilder();
        try (
                InputStream inputStream = AstBase.class.getResourceAsStream(fileName);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                dataBuilder.append(line).append("\n");
            }
            return dataBuilder.toString();

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    public static String getTEMPLATE_DIR() {
        return TEMPLATE_DIR;
    }

    public static String getAbstractStartupHook() {
        return abstractStartupHook;
    }

    public static void setAbstractStartupHook(String abstractStartupHook_) {
        abstractStartupHook = abstractStartupHook_;
    }

    public static String getServiceContext() {
        return serviceContext;
    }

    public void setServiceContext(String serviceContext_) {
        serviceContext = serviceContext_;
    }

    public static String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration_) {
        configuration = configuration_;
    }

    public static String getServiceGrpcServer() {
        return serviceGrpcServer;
    }

    public static void setServiceGrpcServer(String serviceGrpcServer_) {
        serviceGrpcServer = serviceGrpcServer_;
    }

    public static String getServiceGrpcServerMulti() {
        return serviceGrpcServerMulti;
    }

    public static void setServiceGrpcServerMulti(String serviceGrpcServerMulti_) {
        serviceGrpcServerMulti = serviceGrpcServerMulti_;
    }

    public static String getProtoImmutableRequest() {
        return protoImmutableRequest;
    }

    public void setProtoImmutableRequest(String proto_immutable_request) {
        this.protoImmutableRequest = proto_immutable_request;
    }

    public static String getProtoImmutableContractRequest() {
        return protoImmutableContractRequest;
    }

    public void setProtoImmutableContractRequest(String protoImmutableContractRequest) {
        this.protoImmutableContractRequest = protoImmutableContractRequest;
    }

    public static String getServiceGrpcServerMultiBulk() {
        return serviceGrpcServerMultiBulk;
    }

    public static void setServiceGrpcServerMultiBulk(String serviceGrpcServerMultiBulk_) {
        serviceGrpcServerMultiBulk = serviceGrpcServerMultiBulk_;
    }

    public static String getServiceMethods() {
        return serviceMethods;
    }

    public static void setServiceMethods(String serviceMethods_) {
        serviceMethods = serviceMethods_;
    }

    public static String getSqlStmts() {
        return sqlStmts;
    }

    public static void setSqlStmts(String sqlStmts_) {
        sqlStmts = sqlStmts_;
    }

    public static String getServiceSqlReader() {
        return serviceSqlReader;
    }

    public static void setServiceSqlReader(String serviceSqlReader_) {
        serviceSqlReader = serviceSqlReader_;
    }

    public static String getServiceVO() {
        return serviceVO;
    }

    public static void setServiceVO(String serviceVO_) {
        serviceVO = serviceVO_;
    }

    public static String getServiceGrpcClient() {
        return serviceGrpcClient;
    }

    public static void setServiceGrpcClient(String serviceGrpcClient_) {
        serviceGrpcClient = serviceGrpcClient_;
    }

    public static String getServiceGrpcClientInsert() {
        return serviceGrpcClientInsert;
    }

    public static void setServiceGrpcClientInsert(String serviceGrpcClientInsert_) {
        serviceGrpcClientInsert = serviceGrpcClientInsert_;
    }

    public static String getServiceGrpcClientSelect() {
        return serviceGrpcClientSelect;
    }

    public static void setServiceGrpcClientSelect(String serviceGrpcClientSelect_) {
        serviceGrpcClientSelect = serviceGrpcClientSelect_;
    }

    public static String getLaminarProto() {
        return laminarProto;
    }

    public void setLaminarProto(String laminarProto) {
        this.laminarProto = laminarProto;
    }

    public static String getCommonProto() {
        return commonProto;
    }

    public void setCommonProto(String commonProto) {
        this.commonProto = commonProto;
    }

    public static String getProtoMutableRequest() {
        return protoMutableRequest;
    }

    public void setProtoMutableRequest(String protoMutableRequest) {
        this.protoMutableRequest = protoMutableRequest;
    }

    public static String getProtoMutableContractRequest() {
        return protoMutableContractRequest;
    }

    public void setProtoMutableContractRequest(String protoMutableContractRequest) {
        this.protoMutableContractRequest = protoMutableContractRequest;
    }

    public static String getProtoMutableResponse() {
        return protoMutableResponse;
    }

    public void setProtoMutableResponse(String protoMutableResponse) {
        this.protoMutableResponse = protoMutableResponse;
    }

    public static String getProtoMutableContractResponse() {
        return protoMutableContractResponse;
    }

    public void setProtoMutableContractResponse(String protoMutableContractResponse) {
        this.protoMutableContractResponse = protoMutableContractResponse;
    }

    public static String getProtoImmutableResponse() {
        return protoImmutableResponse;
    }

    public void setProtoImmutableResponse(String protoImmutableResponse) {
        this.protoImmutableResponse = protoImmutableResponse;
    }

    public static String getProtoImmutableContractResponse() {
        return protoImmutableContractResponse;
    }

    public void setProtoImmutableContractResponse(String protoImmutableContractResponse) {
        TemplatesVO.protoImmutableContractResponse = protoImmutableContractResponse;
    }

    public static String getGRpcClientModule() {
        return grpcClientModule;
    }

    public void setGRpcClientModule(String GRpcClientModule) {
        this.grpcClientModule = GRpcClientModule;
    }

    public static String getGraphqlSchemaModule() {
        return graphqlSchemaModule;
    }

    public void setGraphqlSchemaModule(String graphqlSchemaModule_) {
        graphqlSchemaModule = graphqlSchemaModule_;
    }

    public static String getGraphqlSchemaModuleOverride() {
        return graphqlSchemaModuleOverride;
    }

    public void setGraphqlSchemaModuleOverride(String graphqlSchemaModuleOverride_) {
        graphqlSchemaModuleOverride = graphqlSchemaModuleOverride_;
    }

    public String getGraphqlSchemaModuleBulk() {
        return graphqlSchemaModuleBulk;
    }

    public void setGraphqlSchemaModuleBulk(String graphqlSchemaModuleBulk_) {
        graphqlSchemaModuleBulk = graphqlSchemaModuleBulk_;
    }

    public static String getGraphqlResource() {
        return graphqlResource;
    }

    public void setGraphqlResource(String graphqlResource_) {
        graphqlResource = graphqlResource_;
    }

    public static String getGraphqlWebResource() {
        return graphqlWebResource;
    }

    public void setGraphqlWebResource(String graphqlWebResource_) {
        graphqlWebResource = graphqlWebResource_;
    }

    public static String getGraphqlResourceModule() {
        return graphqlResourceModule;
    }

    public void setGraphqlResourceModule(String graphqlResourceModule_) {
        graphqlResourceModule = graphqlResourceModule_;
    }

    public static String getGraphqlServer() {
        return graphqlServer;
    }

    public void setGraphqlServer(String graphqlServer_) {
        graphqlServer = graphqlServer_;
    }

    public static String getGraphqlRequest() {
        return graphqlRequest;
    }

    public void setGraphqlRequest(String graphqlRequest_) {
        graphqlRequest = graphqlRequest_;
    }

    public static String getGraphqlCORSFilter() {
        return graphqlCORSFilter;
    }

    public void setGraphqlCORSFilter(String graphqlCORSFilter_) {
        graphqlCORSFilter = graphqlCORSFilter_;
    }

    public static String getGraphqlOAuthValidator() {
        return graphqlOAuthValidator;
    }

    public void setGraphqlOAuthValidator(String graphqlOAuthValidator_) {
        graphqlOAuthValidator = graphqlOAuthValidator_;
    }

    public static String getIndexHtml() {
        return indexHtml;
    }

    public void setIndexHtml(String indexHtml) {
        this.indexHtml = indexHtml;
    }
}
