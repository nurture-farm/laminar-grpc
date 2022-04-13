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

import static farm.nurture.laminar.generator.Constants.ENUM;
import static farm.nurture.laminar.generator.Constants.JAVA_TYPE_INTEGER;
import static farm.nurture.laminar.generator.Constants.JAVA_TYPE_LONG;
import static farm.nurture.laminar.generator.Constants.JAVA_TYPE_STRING;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import farm.nurture.infra.util.StringUtils;
import farm.nurture.laminar.core.util.JsonUtil;
import farm.nurture.laminar.generator.ast.AstBase;
import farm.nurture.laminar.generator.ast.AstBase.Flags;
import farm.nurture.laminar.generator.ast.AstTree;
import farm.nurture.laminar.generator.config.input.CodeGeneratorInputFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProtoGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ProtoGenerator.class);
    private String localBasePath;

    public void main(String[] args) throws Exception {
        generateCode(args);
    }

    public String generateCode(String[] args) throws IOException, SQLException {
        String configFilePath = args[1];
        String sqlDumpFile = args[2];
        String lamFilePath = args[3];
        String dbName = args[4];
        String dbUserName = args[5];
        String dbPwd = args[6];
        String dbUrl = args[7];
        this.localBasePath = args[8];
        startUpComments();

        if (StringUtils.isEmpty(configFilePath)) {
            throw new FileNotFoundException("config file is not given");
        }

        Configuration configuration = getConfiguration(configFilePath);
        String language;

        language = args[0];
        createDummyDb(sqlDumpFile, dbName, dbUserName, dbPwd, dbUrl);

        AstTree astTree = new AstTree();
        astTree.setConfigFilePath(configFilePath);
        astTree.setDumpFilePath(sqlDumpFile);
        astTree.setLamFilePath(lamFilePath);
        AstBase.Flags flags = astTree.setLang(language);
        astTree.onStart(configuration, flags);

        Map<String, FieldDetail> schemaColDataTypes;

        try{
            schemaColDataTypes =
                new ProtoGeneratorShowtables().showTables(dbName, dbUserName, dbPwd, dbUrl, flags);
            }
        catch (SQLException e){
            throw new SQLException("Problem in connection with the database.",e);
        }
        catch (IOException e){
            throw new IOException("SQL file is not correct.",e);
        }

        //        if(flags.contracts) {
        //            loadContractEnums(astTree.contractPath, schemaColDataTypes);
        //        }

        List<AppConfigVO> entries;

        try{
            entries = new CodeGeneratorInputFactory()
                    .getCodeGeneratorInputService(
                        configuration.getProto().getCodeGenerationInputConfigSource())
                    .getApplicationConfiguration(lamFilePath);
        }
        catch (Exception e){
            throw new IOException("LAM file is incorrect.",e);
        }

        if (entries == null) {throw new NullPointerException("LAM file is incorrect.");}

        for (AppConfigVO entry : entries) {
            processEntries(dbName, dbUserName, dbPwd, dbUrl, configuration, schemaColDataTypes, astTree,
                flags, entry);
        }
        try{
            astTree.onEnd();
        }
        catch (NullPointerException e){
            throw new NullPointerException("config file is incorrect.");
        }

        try{
            dropDummyDb(dbName, dbUserName, dbPwd, dbUrl);
            }
        catch (SQLException e){
            throw new SQLException("Problem in connection with database.");
        }
        return configuration.getProto().getServiceName();
    }

    private Configuration getConfiguration(String configFilePath) throws IOException {
        Configuration configuration =
            JsonUtil.deserialize(Files.readAllBytes(Paths.get(configFilePath)), Configuration.class);

        configuration.validateConfig();

        String language = "java,proto,graphql";

        if (!configuration.getProto().getCodeGenerationInputConfigSource().equals("LAM")) {
            throw new IllegalArgumentException("code_generation_input_config_source is incorrect in config file.");
        }
        return configuration;
    }

    private void processEntries(String dbName, String dbUserName, String dbPwd, String dbUrl,
        Configuration configuration, Map<String, FieldDetail> schemaColDataTypes, AstTree astTree,
        Flags flags, AppConfigVO entry) {
        String title = entry.getTitle();
        String body = entry.getSqlStmt();
        String parameterNameTypeStr = (null == entry.getSqlParams()) ? "" : entry.getSqlParams();

        debugAppConfig(title, body);
        List<FieldDetail> requestFields = analyzeParameters(title, parameterNameTypeStr, schemaColDataTypes, entry, flags);
        List<FieldDetail> responseFields = null;

        if (!entry.isMutation()) { // Run the sql to check the response
            try{
                responseFields = ifNotIsMutation(dbName, dbUserName, dbPwd, dbUrl, configuration, entry, requestFields);
                }
            catch (Exception e){
                throw new RuntimeException("LAM file is incorrect.");
            }
        }

        try{
            astTree.onEntry(entry, requestFields, responseFields);
        } catch (Exception e){
            throw new RuntimeException("LAM file or SQL file is incorrect.");
        }

    }

    private List<FieldDetail> ifNotIsMutation(
        String dbName,
        String dbUserName,
        String dbPwd,
        String dbUrl,
        Configuration configuration,
        AppConfigVO entry,
        List<FieldDetail> requestFields) {
        List<FieldDetail> responseFields;
        Set<String> sqlReplaceNames = new HashSet<>();
        if (!entry.isNullSqlReplace()) {
            List<String> replaceTokens = StringUtils.fastSplit(entry.getSqlReplace(), ',');
            for (String rToken : replaceTokens) {
                String StrTrimmed = rToken.trim();
                int cutPos = StrTrimmed.indexOf(':');
                String Name = StrTrimmed.substring(0, cutPos);
                sqlReplaceNames.add(Name);
            }
        }
        Object[] parameters = null;
        if (!requestFields.isEmpty()) {
            parameters = processRequestFields(requestFields, sqlReplaceNames);
        }

        String sqlStmt = entry.getSqlStmt();
        if (!entry.isNullSqlReplace()) {
            List<String> replaceTokens = StringUtils.fastSplit(entry.getSqlReplace(), ',');
            for (String rToken : replaceTokens) {
                String rTokenName = rToken.substring(0, rToken.indexOf(':'));
                sqlStmt = sqlStmt.replace("@" + rTokenName + "@", "''");
                logger.info("Replaced sql stmt = {}",sqlStmt);
            }
        }

        if (StringUtils.isEmpty(sqlStmt)) responseFields = new ArrayList<>();
        else
            responseFields =
                new CodeGeneratorInputFactory()
                    .getCodeGeneratorInputService(
                        configuration.getProto().getCodeGenerationInputConfigSource())
                    .getFieldDetailsForReqRes(sqlStmt, parameters, dbName, dbUrl, dbUserName, dbPwd);
        return responseFields;
    }

    private Object[] processRequestFields(
        List<FieldDetail> requestFields, Set<String> sqlReplaceNames) {
        Object[] parameters;
        int requestFieldSize = 0;
        for (Iterator<FieldDetail> iter = requestFields.listIterator(); iter.hasNext(); ) {
            FieldDetail fieldDetail = iter.next();
            if (!sqlReplaceNames.contains(fieldDetail.getFieldName())) {
                requestFieldSize++;
            }
        }
        parameters = new Object[requestFieldSize];
        int index = 0;
        for (FieldDetail fld : requestFields) {
            if (sqlReplaceNames.contains(fld.getFieldName())) {
                continue;
            }
            if (fld.getJavaType().equals(JAVA_TYPE_INTEGER) || fld.getJavaType().equals(JAVA_TYPE_LONG)) {
                parameters[index] = 0;
            }
            index++;
        }
        return parameters;
    }

    private void startUpComments() {
        logger.info("********************************************");
        logger.info("********************************************");
        logger.info("********************************************");
        logger.info("********************************************");
        logger.info(
            "Please don't add secrets to your code - api key, passwords or any other thing that can leak.");
        logger.info("Instead use env variables.");
        logger.info("********************************************");
        logger.info("********************************************");
        logger.info("********************************************");
        logger.info("********************************************");
    }

    private void createDummyDb(
        String sqlDumpFile, String dbName, String dbUserName, String dbPwd, String dbUrl)
        throws IOException {
        logger.info("################## Database Interaction ############");

        String mysqlCreateDatabaseCmd =
            "create database dbName; GRANT ALL PRIVILEGES ON dbName.* TO userName IDENTIFIED BY 'pwd'";
        mysqlCreateDatabaseCmd = mysqlCreateDatabaseCmd.replaceAll("dbName", dbName);
        mysqlCreateDatabaseCmd = mysqlCreateDatabaseCmd.replace("userName", dbUserName);
        mysqlCreateDatabaseCmd = mysqlCreateDatabaseCmd.replace("pwd", dbPwd);

        String mysqlImportCommand = "mysql -h " + dbUrl + " -u userName -ppwd dbName < sqlDumpFile;";
        mysqlImportCommand = mysqlImportCommand.replaceAll("dbName", dbName);
        mysqlImportCommand = mysqlImportCommand.replace("sqlDumpFile", sqlDumpFile);
        mysqlImportCommand = mysqlImportCommand.replace("userName", dbUserName);
        mysqlImportCommand = mysqlImportCommand.replace("pwd", dbPwd);

        String fileName = this.localBasePath + dbName + ".sh";

        FileWriter fileWriter = new FileWriter(fileName);
        fileWriter.write("mysql -h " + dbUrl + " -u " + dbUserName + " -p" + dbPwd + " -e \"");
        fileWriter.write(mysqlCreateDatabaseCmd);
        fileWriter.write("\"");
        fileWriter.write("\n");
        fileWriter.write(mysqlImportCommand);
        fileWriter.close();
        Runtime.getRuntime().exec(new String[] {"sh", fileName});
        sleep(5000);

        File file = new File(fileName);
        file.delete();
    }

    private void dropDummyDb(String dbName, String dbUserName, String dbPwd, String dbUrl)
        throws SQLException {
        // Deleting the temp database

        try {

            String mysqlDropDatabaseCmd = "drop database " + dbName + ";";

            logger.info("mysqlDropDatabaseCmd is {}",mysqlDropDatabaseCmd);

            String fileName = localBasePath + dbName + ".sh";

            FileWriter fileWriter = new FileWriter(fileName);
            fileWriter.write("mysql " + "-h " + dbUrl + " -u " + dbUserName + " -p" + dbPwd + " -e \"");
            fileWriter.write(mysqlDropDatabaseCmd);
            fileWriter.write("\"");
            fileWriter.close();
            Runtime.getRuntime().exec(new String[] {"sh", fileName});
            sleep(5000);

            File file = new File(fileName);
            file.delete();
        }
        catch (Exception e) {
            throw new SQLException("Problem in dropping the database, check connection!");
        }
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch ( InterruptedException e) {
            logger.error("exception inside sleep method",e);
        }
    }

    private List<FieldDetail> analyzeParameters(
        String title,
        String parameterNameTypeStr,
        Map<String, FieldDetail> schemaColDataTypes,
        AppConfigVO entry,
        AstBase.Flags flags) {

        List<FieldDetail> paramDetails = new ArrayList<>();
        if (parameterNameTypeStr.trim().length() > 0) {
            List<String> parameterNameTypeList = StringUtils.fastSplit(parameterNameTypeStr, ',');

            for (String aNameTypeStr : parameterNameTypeList) {
                processANameTypeStr(title, schemaColDataTypes, entry, flags, paramDetails, aNameTypeStr);
            }
        }
        return paramDetails;
    }

    private void processANameTypeStr(String title, Map<String, FieldDetail> schemaColDataTypes,
        AppConfigVO entry, Flags flags, List<FieldDetail> paramDetails, String aNameTypeStr) {
        String aNameTypeStrTrimmed = aNameTypeStr.trim();
        int cutPos = aNameTypeStrTrimmed.indexOf(':');
        if (cutPos <= 0) {
            logger.error(
                "Service :{} >> Missing {:} separator in [{}] , "
                    + "expecting something like {}:table.field or {}:string"
                ,title,aNameTypeStrTrimmed , aNameTypeStrTrimmed,aNameTypeStrTrimmed);
            System.exit(1);
        }
        String aName = aNameTypeStrTrimmed.substring(0, cutPos);
        String aType = aNameTypeStrTrimmed.substring(cutPos + 1);
        if (aType.startsWith("farm.nurture.core.contracts")) {
            AppConfigVO.setIsContractUsed(true);
            paramDetails.add(new FieldDetail(aName, aType, JAVA_TYPE_STRING, ENUM));
            flags.setContracts(true);
        } else if (schemaColDataTypes.containsKey(aType)) {
            FieldDetail fldDetail = schemaColDataTypes.get(aType);
            //
            // if(fldDetail.protoType.toLowerCase().startsWith("farm.nurture.core.contracts") &&
            // !AppConfigVO.isContractUsed) AppConfigVO.isContractUsed = true;
            //                    if(aType.toLowerCase().startsWith("farm.nurture.core.contracts") &&
            // !AppConfigVO.isContractUsed)
            paramDetails.add(fldDetail);
        } else {
            //                    System.err.println("Service :" + title + " >> Improper datatype in
            // [" + aNameTypeStrTrimmed + "] aName = " + aName + " , DataType=[" + aType + "]");
            //                    System.out.println("Missing in list : " +
            // schemaColDataTypes.toString());
            if (entry.isImplDao())
                paramDetails.add(new FieldDetail(aType, aType, JAVA_TYPE_STRING, ENUM));
        }
    }

    private void debugAppConfig(String title, String body) {
        logger.info(
            "\n\n******************************************************************************************************  ");
        logger.info("\t\t\t\t{}\t\t\t\t",title);
        logger.info("body = {}",body);
        logger.info(
            "***************************************************************************************************** */");
    }

    private void loadContractEnums(String contractPath, Map<String, FieldDetail> schemaColDataTypes)
        throws IOException {

        try {
            String contractCommonPath = contractPath + "/Common";
            File dir = new File(contractCommonPath);
            File[] files = dir.listFiles((dir1, name) -> name.endsWith(".proto"));

            for (File protofile : files) {
                List<String> lines;
                lines = Files.readAllLines(protofile.toPath());
                if (lines != null) {
                    for (String line : lines) {
                        if (line.trim().startsWith(ENUM)) {
                            String enumType = line.replace(ENUM, "").replace("{", "").trim();
                            FieldDetail gType =
                                new DatabaseReader().deriveProtoDataType(enumType, "enum()", "ContractEnum");
                            schemaColDataTypes.put(
                                "ContractEnum.farm.nurture.core.contracts.common." + enumType, gType);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException("Error in loading Contract Enums");
        }
    }
}
