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

package farm.nurture.laminar.generator.ast.graphql;

import static farm.nurture.laminar.generator.Constants.BASE_PATH;
import static farm.nurture.laminar.generator.Constants.EXECUTE;
import static farm.nurture.laminar.generator.Constants.JAVA_PACKAGE;
import static farm.nurture.laminar.generator.Constants.MUTATION;

import farm.nurture.infra.util.StringUtils;
import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.Configuration;
import farm.nurture.laminar.generator.FieldDetail;
import farm.nurture.laminar.generator.TemplatesVO;
import farm.nurture.laminar.generator.ast.AstBase;
import java.util.List;

public class AstGraphQL extends AstBase {

    StringBuilder queryList = new StringBuilder(1024);
    StringBuilder mutationList = new StringBuilder(1024);
    StringBuilder overrideList = new StringBuilder(1024);

    @Override
    public void onEntry(
        AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields) {

        if (entry.isDeclGrpc()) {

            String mutation = "@mutation@";

            if (entry.isMutation()) {
                mutationList.append("\"").append(EXECUTE).append(entry.getTitle()).append("\", ");
                mutationList.append("\"").append(EXECUTE).append(entry.getBulkTitle()).append("\", ");
                mutation = "@Mutation";
            } else {
                queryList.append("\"").append(EXECUTE).append(entry.getTitle()).append("\", ");
                mutation = "@Query";
            }

            String oauthPrivate =
                (entry.isNullOauthPublic()) ? "false" : entry.isOauthPublic() ? "false" : "true";

            StringBuilder oauthClaims = new StringBuilder();
            if (!StringUtils.isEmpty(entry.getOauthClaims())) {
                List<String> oauthClaimsL = StringUtils.fastSplit(entry.getOauthClaims(), ',');
                boolean isFirst = true;
                for (String oauthClaimsStr : oauthClaimsL) {
                    int oauthClaimIndex = oauthClaimsStr.indexOf('=');
                    if (isFirst) isFirst = false;
                    else oauthClaims.append(",\n\t\t\t\t");

                    oauthClaims.append("new OAuthValidator.Claim(\"");
                    oauthClaims.append(oauthClaimsStr.substring(0, oauthClaimIndex).trim()).append("\", \"");
                    oauthClaims.append(oauthClaimsStr.substring(oauthClaimIndex + 1).trim()).append("\")");
                }
            }
            appendOverrideList(entry, mutation, oauthPrivate, oauthClaims);
        }
    }

    private void appendOverrideList(
        AppConfigVO entry, String mutation, String oauthPrivate, StringBuilder oauthClaims) {
        overrideList.append(
            TemplatesVO.getGraphqlSchemaModuleOverride().replace(MUTATION, mutation)
                .replace("@grpc_rpcname@", entry.getTitle())
                .replace("@oauth_private@", oauthPrivate)
                .replace("@oauth_claims@", oauthClaims.toString())
                .replace("@grpc_reqname@", entry.getReqName())
                .replace("@grpc_resname@", entry.getResName()));

        if (entry.isMutation()) {
            overrideList.append(
                TemplatesVO.getGraphqlSchemaModuleOverride().replace(MUTATION, mutation)
                    .replace("@grpc_rpcname@", entry.getBulkTitle())
                    .replace("@oauth_private@", oauthPrivate)
                    .replace("@oauth_claims@", oauthClaims.toString())
                    .replace("@grpc_reqname@", entry.getBulkReqName())
                    .replace("@grpc_resname@", entry.getBulkResName()));
        }
    }

    @Override
    public void onEnd() {
        mutationList.append("\"").append(EXECUTE).append("\", ");
        writeGraphQLSchemaModule(getConf());

        writeToFile(
            getGraphQLPackageDirectory() + "/GRpcClientModule.java",
            TemplatesVO.getGRpcClientModule().replace("@microservice_name@", getServiceName())
                .replace(JAVA_PACKAGE, getJavaPackage()));

        writeToFile(
            getGraphQLResourceDirectory() + "/index.html",
            TemplatesVO.getIndexHtml().replace(BASE_PATH, getGraphQLUri()));

        writeToFile(
            getGraphQLPackageDirectory() + "/GraphqlResource.java",
            TemplatesVO.getGraphqlResource().replace(BASE_PATH, getGraphQLUri())
                .replace(JAVA_PACKAGE, getJavaPackage()));

        writeToFile(
            getGraphQLPackageDirectory() + "/GraphqlWebResource.java",
            TemplatesVO.getGraphqlWebResource().replace(BASE_PATH, getGraphQLUri())
                .replace(JAVA_PACKAGE, getJavaPackage()));

        writeToFile(
            getGraphQLPackageDirectory() + "/GraphqlResourceModule.java",
            TemplatesVO.getGraphqlResourceModule().replace(JAVA_PACKAGE, getJavaPackage()));

        writeToFile(
            getGraphQLPackageDirectory() + "/GraphqlServer.java",
            TemplatesVO.getGraphqlServer().replace(JAVA_PACKAGE, getJavaPackage()));

        writeToFile(
            getGraphQLPackageDirectory() + "/GraphqlRequest.java",
            TemplatesVO.getGraphqlRequest().replace(JAVA_PACKAGE, getJavaPackage()));

        writeToFile(
            getGraphQLPackageDirectory() + "/CORSFilter.java",
            TemplatesVO.getGraphqlCORSFilter().replace(JAVA_PACKAGE, getJavaPackage()));

        writeToFile(
            getGraphQLPackageDirectory() + "/OAuthValidator.java",
            TemplatesVO.getGraphqlOAuthValidator().replace(JAVA_PACKAGE, getJavaPackage()));
    }

    void writeGraphQLSchemaModule(Configuration configuration) {

        String seperator = ", ";
        String queryListStr = queryList.toString();
        String mutationListStr = mutationList.toString();

        int mutationListPos = mutationListStr.length() - seperator.length();
        if (mutationListPos < 0) mutationListPos = 0;
        int queryListPos = queryListStr.length() - seperator.length();
        if (queryListPos < 0) queryListPos = 0;

        writeToFile(
            getGraphQLPackageDirectory() + "/GraphqlSchemaModule.java",
            TemplatesVO.getGraphqlSchemaModule().replace(
                    "@queryList@", queryListStr.substring(0, queryListPos))
                .replace("@overrideList@", overrideList.toString())
                .replace("@mutationList@", mutationListStr.substring(0, mutationListPos))
                .replace("@microservice_name@", getServiceName())
                .replace(JAVA_PACKAGE, getJavaPackage()));
    }
}
