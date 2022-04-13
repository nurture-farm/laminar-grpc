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

package farm.nurture.laminar.generator.ast;

import static farm.nurture.laminar.generator.Constants.FORWARD_SLASH;
import static farm.nurture.laminar.generator.Constants.STRING;

import farm.nurture.infra.util.Logger;
import farm.nurture.infra.util.LoggerFactory;
import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.Configuration;
import farm.nurture.laminar.generator.FieldDetail;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AstBase {

    private Configuration conf;
    private String protoGeneratePath;
    private String contractPath;
    private String contractTag;
    private String javaGeneratePath;
    private String javaOverridePath;
    private String goGeneratePath;
    private String goOverridePath;
    private String graphQLGeneratePath;
    private String serviceName;
    private String namespace;
    private String javaPackage;
    private String protoPackage;
    private String goPackage;
    private String codeUrl;
    private String graphQLUri;
    private String javaPackageDirectory;
    private String javaDaoDirectory;
    private String javaOverrideDirectory;
    private String graphQLPackage;
    private String graphQLPackageDirectory;
    private String graphQLResourceDirectory;
    private Boolean temporalWorkerEnabled;
    private String temporalWorkerNamespace;
    private String temporalWorkerAddress;
    private String temporalWorkerTaskQueue;
    private String codeGenerationInputConfigSource;
    public static final Logger LOG = LoggerFactory.getLogger(AstBase.class);


    public void onStart(Configuration conf, AstBase.Flags flags) {
        setConfig(conf);

        try {
            if (flags.isProto()) {
                ifIsProto(flags);
            }

            if (flags.isJavaLang()) {
                ifIsJavaLang();
            }

            if (flags.isGraphql()) {
                ifIsGraphql();
            }

            if (flags.isGoLang()) {
                ifIsGoLang();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void ifIsGoLang() {
        File g = new File(getGoGeneratePath());
        if (!g.exists()) g.mkdirs();

        File og = new File(getGoOverridePath());
        if (!og.exists()) og.mkdirs();
    }

    private void ifIsGraphql() {
        File qj = new File(getGraphQLPackageDirectory());
        if (!qj.exists()) qj.mkdirs();

        File qr = new File(getGraphQLResourceDirectory());
        if (!qr.exists()) qr.mkdirs();
    }

    private void ifIsJavaLang() {
        File oj = new File(getJavaOverrideDirectory());
        if (!oj.exists()) oj.mkdirs();

        File jj = new File(getJavaPackageDirectory());
        if (!jj.exists()) jj.mkdirs();

        File jd = new File(getJavaDaoDirectory());
        if (!jd.exists()) jd.mkdirs();
    }

    private void ifIsProto(Flags flags) {
        File pj = new File(getJavaGeneratePath() + FORWARD_SLASH + getProtoGeneratePath());
        if (!pj.exists()) pj.mkdirs();

        File pg = new File(getGoGeneratePath() + FORWARD_SLASH + getProtoGeneratePath());
        if (!pg.exists()) pg.mkdirs();

        if (flags.isContracts()) {
            File cg = new File(getContractPath() + FORWARD_SLASH + getServiceName());
            if (!cg.exists()) cg.mkdirs();
        }
    }

    private void setConfig(Configuration conf) {
        this.setConf(conf);

        setServiceName(conf.getProto().getServiceName());
        setNamespace(conf.getProto().getNamespace());

        setProtoGeneratePath(conf.getProto().getProtoGeneratePath());
        if (conf.getContract() != null) setContractPath(conf.getContract().getContractPath());
        if (conf.getContract() != null) setContractTag(conf.getContract().getContractTag());
        setJavaOverridePath(getServiceName() + conf.getProto().getJavaOverridePath());
        setJavaGeneratePath(getServiceName() + conf.getProto().getJavaGeneratePath());
        setGraphQLGeneratePath(getServiceName() + conf.getProto().getGraphqlGeneratePath());
        setGoGeneratePath(getServiceName() + conf.getProto().getGoGeneratePath());
        setGoOverridePath(getServiceName() + conf.getProto().getGoOverridePath());

        setProtoPackage(conf.getProto().getProtoPackage());
        setJavaPackage(conf.getProto().getJavaPackage());
        setGoPackage(conf.getProto().getGoPackage());
        setCodeUrl(conf.getProto().getCodeUrl());
        setGraphQLUri(getServiceName() + conf.getProto().getGraphqlUri());
        setCodeGenerationInputConfigSource(conf.getProto().getCodeGenerationInputConfigSource());
        final String javaDir = "/java/";
        String javaPackageSlash = getJavaPackage().replaceAll("\\.", "\\/");
        setJavaPackageDirectory(getJavaGeneratePath() + javaDir + javaPackageSlash);
        setJavaDaoDirectory(getJavaPackageDirectory() + "/dao");
        setJavaOverrideDirectory(getJavaOverridePath() + javaDir + javaPackageSlash);

        setGraphQLPackage(getJavaPackage() + ".graphql");
        setGraphQLPackageDirectory(getGraphQLGeneratePath() + javaDir + javaPackageSlash + "/graphql");
        setGraphQLResourceDirectory(getGraphQLGeneratePath() + "/resources");

        setTemporalWorkerEnabled(conf.getTemporalWorker().getEnabled());
        setTemporalWorkerNamespace(conf.getTemporalWorker().getNamespace());
        setTemporalWorkerAddress(conf.getTemporalWorker().getAddress());
        setTemporalWorkerTaskQueue(conf.getTemporalWorker().getTaskQueue());
    }

    public void onEntry(
        AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields) {
        // write your code here
    }

    public void onEnd() {
        // write your code here
    }

    public void writeToFile(String fileName, String contentHolder) {
        try {
            LOG.info("Writting file: " + fileName);
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileName));
            fileWriter.write(contentHolder);
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public String readTemplateFile(String fileName) {
        StringBuilder dataBuilder = new StringBuilder();
        try (InputStream inputStream = AstBase.class.getResourceAsStream(fileName);
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

    public void replaceSql(Map<String, String> sqlReplaces, StringBuilder grpcServerSqlReplace) {

        for (Map.Entry<String, String> aReplace : sqlReplaces.entrySet()) {

            String fldName = aReplace.getKey();
            String getFldMethodSuffix = Character.toUpperCase(fldName.charAt(0)) + fldName.substring(1);

            String aReplaceValue = aReplace.getValue().trim();
            String replaceVal = "val" + getFldMethodSuffix;
            if (aReplaceValue.startsWith("repeated ")) {
                startingWithRepeated(grpcServerSqlReplace, getFldMethodSuffix, aReplaceValue, replaceVal);

                grpcServerSqlReplace.append("\n\t\t\t}\n");
            } else {

                boolean isString = aReplaceValue.startsWith(STRING);

                startingWithString(grpcServerSqlReplace, getFldMethodSuffix, replaceVal, isString);
            }

            grpcServerSqlReplace
                .append("\t\t\tsqlStmt = sqlStmt.replace(\"@")
                .append(aReplace.getKey())
                .append("@\" , ")
                .append(replaceVal)
                .append(");\n");
        }
    }

    private void startingWithString(
        StringBuilder grpcServerSqlReplace,
        String getFldMethodSuffix,
        String replaceVal,
        boolean isString) {
        if (isString) {
            grpcServerSqlReplace
                .append("String ")
                .append(replaceVal)
                .append(" = request.get")
                .append(getFldMethodSuffix)
                .append("();\n");
        } else {
            grpcServerSqlReplace
                .append("String ")
                .append(replaceVal)
                .append(" = String.valueOf(request.get")
                .append(getFldMethodSuffix)
                .append("());\n");
        }
    }

    private void startingWithRepeated(
        StringBuilder grpcServerSqlReplace,
        String getFldMethodSuffix,
        String aReplaceValue,
        String replaceVal) {
        boolean isString = aReplaceValue.startsWith("repeated string");

        String totalVariable = "total" + getFldMethodSuffix;
        grpcServerSqlReplace
            .append("int ")
            .append(totalVariable)
            .append(" = request.get" + getFldMethodSuffix + "Count();\n");
        grpcServerSqlReplace
            .append("\t\t\tString ")
            .append(replaceVal)
            .append(" = (")
            .append(totalVariable)
            .append(" == 0) ? \"''\" : null;;\n");

        grpcServerSqlReplace
            .append("\t\t\tfor ( int i=0; i<total")
            .append(getFldMethodSuffix)
            .append("; i++) {");

        grpcServerSqlReplace.append("\n\t\t\t\tif ( null == ").append(replaceVal).append(") ");
        grpcServerSqlReplace.append("\t").append(replaceVal).append(" = ");
        if (isString) grpcServerSqlReplace.append("\"'\" + ");
        else grpcServerSqlReplace.append(" String.valueOf ( ");
        grpcServerSqlReplace.append(" request.get").append(getFldMethodSuffix).append("(i) ");
        if (isString) grpcServerSqlReplace.append("+ \"'\" ;");
        else grpcServerSqlReplace.append(");");

        grpcServerSqlReplace.append("\n\t\t\t\telse ").append(replaceVal).append(" += \",\" + ");
        if (isString) grpcServerSqlReplace.append("\"'\" + ");
        grpcServerSqlReplace.append(" request.get").append(getFldMethodSuffix).append("(i) ");
        if (isString) grpcServerSqlReplace.append("+ \"'\" ;");
        else grpcServerSqlReplace.append(";");
    }

    @Getter
    @Setter
    public class Flags {

        private boolean proto = true;
        private boolean javaLang = true;
        private boolean goLang = true;
        private boolean graphql = true;
        private boolean contracts = true;
        private boolean protoTimestampUsed = false;

        public void signal(boolean onOff) {
            setProto(onOff);
            setJavaLang(onOff);
            setGoLang(onOff);
            setGraphql(onOff);
            setContracts(onOff);
        }
    }
}
