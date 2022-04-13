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

import static farm.nurture.laminar.generator.Constants.BOOLEAN;
import static farm.nurture.laminar.generator.Constants.BYTE;
import static farm.nurture.laminar.generator.Constants.FORWARD_SLASH;
import static farm.nurture.laminar.generator.Constants.JAVA_MATH_BIG_DECIMAL;
import static farm.nurture.laminar.generator.Constants.JAVA_PACKAGE;
import static farm.nurture.laminar.generator.Constants.JAVA_SQL_DATE;
import static farm.nurture.laminar.generator.Constants.JAVA_SQL_TIME;
import static farm.nurture.laminar.generator.Constants.JAVA_SQL_TIMESTAMP;
import static farm.nurture.laminar.generator.Constants.JAVA_TYPE_DOUBLE;
import static farm.nurture.laminar.generator.Constants.JAVA_TYPE_FLOAT;
import static farm.nurture.laminar.generator.Constants.JAVA_TYPE_INTEGER;
import static farm.nurture.laminar.generator.Constants.JAVA_TYPE_LONG;
import static farm.nurture.laminar.generator.Constants.JAVA_TYPE_STRING;
import static farm.nurture.laminar.generator.Constants.PROTO_TYPE_BYTES;
import static farm.nurture.laminar.generator.Constants.PROTO_TYPE_DOUBLE;
import static farm.nurture.laminar.generator.Constants.PROTO_TYPE_FLOAT;
import static farm.nurture.laminar.generator.Constants.SERVICE_NAME;
import static farm.nurture.laminar.generator.Constants.SERVICE_RES;

import farm.nurture.infra.util.Logger;
import farm.nurture.infra.util.LoggerFactory;
import farm.nurture.laminar.core.util.CaseUtils;
import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.Configuration;
import farm.nurture.laminar.generator.FieldDetail;
import farm.nurture.laminar.generator.TemplatesVO;
import farm.nurture.laminar.generator.ast.AstBase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

public class AstDao extends AstBase {

    private StringBuilder sqlstmts = new StringBuilder(16 * 1024);
    private static final Logger logger = LoggerFactory.getLogger(AstDao.class);

    @Override
    public void onEntry(
        AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields) {

        if (null != entry.getSqlStmt())
            sqlstmts
                .append("public static String ")
                .append(entry.getTitle())
                .append(" = \"")
                .append(entry.getSqlStmt())
                .append("\";\n");

        if (entry.isImplDao() && !entry.isMutation()) {
            createDao(entry, responseFields);
        }
    }

    @Override
    public void onEnd() {
        super.onEnd();

        writeToFile(
            getJavaDaoDirectory() + "/SqlStmts.java",
            TemplatesVO.getSqlStmts().replace(JAVA_PACKAGE, getJavaPackage())
                .replace("@sql_stmts@", sqlstmts.toString()));
    }

    private void createDao(AppConfigVO entry, List<FieldDetail> responseFields) {

        Chunk chunk = new Chunk();
        int objPos = 1;
        Map<String, FieldDetailDao> msgnames = new HashMap<>();
        List<FieldDetailDao> fddList = new ArrayList<>();
        for (FieldDetail t : responseFields) {
            FieldDetailDao fdd = new FieldDetailDao(t);
            fddList.add(fdd);
            if (null != fdd.getMsgNameCamel() && !msgnames.containsKey(fdd.getMsgNameCamel())) {
                msgnames.put(fdd.getMsgname(), fdd);
            }
        }


        for (FieldDetailDao fdo : msgnames.values()) {
            processFdo(chunk, fdo);
        }

        for (FieldDetailDao t : fddList) {

            objPos = processT(chunk, objPos, t);
        }

    for (FieldDetailDao fdo : msgnames.values()) {
      chunk
          .voToProtoRecord
          .append("recordBuilder.set" + CaseUtils.toCamelCase(fdo.getMsgname(), true, '_'))
          .append('(')
          .append(fdo.getMsgNameCamel())
          .append("Builder.build());\n");
        }
        logger.info("super conf:{}", super.getConf());
        writeSqlReader(entry, super.getConf(), chunk);
        writeSqlVO(entry, entry.isDeclGrpc(), super.getConf(), chunk);
    }

    private int processT(Chunk chunk, int objPos, FieldDetailDao t) {
        chunk.readerFldDefinitionContent.append(
            "Object " + t.getFd().getFldNameCamel() + " = rs.getObject(" + objPos + ");\n");

        if (objPos > 1) {
            chunk.voInitializationContent.append(",\n");
            chunk.voConstructorVariables.append(",\n");
        }

        objPos++;
        generate(chunk, t);
        return objPos;
    }

    private void processFdo(Chunk chunk, FieldDetailDao fdo) {
        chunk
            .builderVariables
            .append(fdo.getMsgname())
            .append(".Builder ")
            .append(fdo.getMsgNameCamel())
            .append("Builder = ");
        chunk.builderVariables.append(fdo.getMsgname()).append(".newBuilder(); \n");
        chunk.hasBuilder = true;
    }

    void writeSqlVO(AppConfigVO entry, boolean isRPC, Configuration configuration, Chunk chunk) {
        String voTemplateContent = TemplatesVO.getServiceVO();
        voTemplateContent = voTemplateContent.replace(SERVICE_NAME, entry.getTitle());
        voTemplateContent = voTemplateContent.replace("@voVariables@", chunk.voVariables.toString());
        voTemplateContent =
            voTemplateContent.replace("@voNullVariables@", chunk.voNullVariables.toString());
        voTemplateContent =
            voTemplateContent.replace(
                "@voConstructorVariables@", chunk.voConstructorVariables.toString());
        voTemplateContent =
            voTemplateContent.replace("@voThisVarAssignment@", chunk.voThisVarAssignment.toString());
        voTemplateContent =
            voTemplateContent.replace("@voToProtoRecord@", chunk.voToProtoRecord.toString());
        voTemplateContent = voTemplateContent.replace("@voDisableProtoS@", (isRPC) ? "" : "/**");
        voTemplateContent = voTemplateContent.replace("@voDisableProtoE@", (isRPC) ? "" : "*/");
        voTemplateContent = voTemplateContent.replace("@voDisableProtoE@", (isRPC) ? "" : "*/");
        voTemplateContent = voTemplateContent.replace(JAVA_PACKAGE, getJavaPackage());
        voTemplateContent = voTemplateContent.replace(SERVICE_RES, entry.getResName());
        voTemplateContent =
            voTemplateContent.replace("@builders@", (chunk.hasBuilder) ? chunk.builderVariables : "");
        writeToFile(getJavaDaoDirectory() + FORWARD_SLASH + entry.getResName() + "VO.java", voTemplateContent);
    }

    void writeSqlReader(AppConfigVO entry, Configuration configuration, Chunk chunk) {
        String tempContentT = TemplatesVO.getServiceSqlReader();
        tempContentT = tempContentT.replace(SERVICE_NAME, entry.getTitle());
        tempContentT =
            tempContentT.replace(
                "@readerFldDefinitionContent@", chunk.readerFldDefinitionContent.toString());
        tempContentT =
            tempContentT.replace("@voInitializationContent@", chunk.voInitializationContent.toString());
        tempContentT = tempContentT.replace(JAVA_PACKAGE, getJavaPackage());
        tempContentT = tempContentT.replace(SERVICE_RES, entry.getResName());
        String daoFile = getJavaDaoDirectory() + FORWARD_SLASH + entry.getResName() + "Dao.java";
        writeToFile(daoFile, tempContentT);
    }

    void generate(Chunk chunk, FieldDetailDao t) {

        String setMethod = CaseUtils.toCamelCase("set_" + t.getFldName(), false, '_');

        chunk.voNullVariables.append("public boolean ").append(t.getNullFldNameCamel()).append(";\n");
        chunk.voConstructorVariables.append("boolean ").append(t.getNullFldNameCamel()).append(" , ");
        chunk
            .voThisVarAssignment
            .append("this.")
            .append(t.getFd().getFldNameCamel())
            .append(" = ")
            .append(t.getFd().getFldNameCamel())
            .append(";\n");
        chunk
            .voThisVarAssignment
            .append("this.")
            .append(t.getNullFldNameCamel())
            .append(" = ")
            .append(t.getNullFldNameCamel())
            .append(";\n");
        processProtoType(chunk, t, setMethod);
        processJavaDataType(chunk, t);
    }

    private void processProtoType(Chunk chunk, FieldDetailDao t, String setMethod) {
        if (PROTO_TYPE_BYTES.equals(t.getFd().getProtoType())) {
            String setMethodBraced = t.getRecordName() + "." + setMethod + "(";
            chunk
                .voToProtoRecord
                .append("if ( ")
                .append(t.getNullFldNameCamel())
                .append(" ) ")
                .append(setMethodBraced)
                .append("ByteString.EMPTY) ;");
            chunk
                .voToProtoRecord
                .append("\nelse ")
                .append(setMethodBraced)
                .append("ByteString.copyFrom(")
                .append(t.getFd().getFldNameCamel())
                .append(")) ;\n");
        } else {
            chunk
                .voToProtoRecord
                .append("if ( ! ")
                .append(t.getNullFldNameCamel())
                .append(" ) ")
                .append(t.getRecordName())
                .append(".")
                .append(setMethod)
                .append('(')
                .append(t.getFd().getFldNameCamel())
                .append(");\n");
        }
    }

    private void processJavaDataType(Chunk chunk, FieldDetailDao t) {
        switch (t.getFd().getJavaType()) {
            case JAVA_TYPE_STRING:
                voGeneratorCommonVariables(JAVA_TYPE_STRING, JAVA_TYPE_STRING, "\"\"",
                    t.getFd().getFldNameCamel(), chunk);
                break;
            case JAVA_TYPE_INTEGER:
                voGeneratorCommonVariables(JAVA_TYPE_INTEGER, "int", "0", t.getFd().getFldNameCamel(), chunk);
                break;
            case JAVA_TYPE_DOUBLE:
                voGeneratorCommonVariables(JAVA_TYPE_DOUBLE, PROTO_TYPE_DOUBLE, "0", t.getFd().getFldNameCamel(), chunk);
                break;
            case JAVA_TYPE_FLOAT:
                voGeneratorCommonVariables(JAVA_TYPE_FLOAT, PROTO_TYPE_FLOAT, "0", t.getFd().getFldNameCamel(), chunk);
                break;
            case JAVA_TYPE_LONG:
                voGeneratorCommonVariables(JAVA_TYPE_LONG, "long", "0", t.getFd().getFldNameCamel(), chunk);
                break;
            case BYTE:
                voGeneratorCommonVariables(BYTE, BYTE, "null", t.getFd().getFldNameCamel(), chunk);
                break;
            case BOOLEAN:
                voGeneratorCommonVariables(BOOLEAN, "boolean", "false",
                    t.getFd().getFldNameCamel(), chunk);
                break;
            case JAVA_SQL_DATE:
                voGeneratorTypecastVariables(
                    JAVA_SQL_DATE, "long", "0", "getTime", chunk, t.getFd().getFldNameCamel());
                break;
            case JAVA_SQL_TIME:
                voGeneratorTypecastVariables(
                    JAVA_SQL_TIME, "String", "\"0\"", "toString", chunk,
                    t.getFd().getFldNameCamel());
                break;
            case JAVA_SQL_TIMESTAMP:
                voGeneratorTypecastVariables(
                    JAVA_SQL_TIMESTAMP, "long", "0", "getTime", chunk,
                    t.getFd().getFldNameCamel());
                break;
            case JAVA_MATH_BIG_DECIMAL:
                voGeneratorTypecastVariables(
                    JAVA_MATH_BIG_DECIMAL, PROTO_TYPE_DOUBLE, "0", "doubleValue", chunk,
                    t.getFd().getFldNameCamel());
                break;
            default:
                throw new RuntimeException("Unknown dataype : " + t.getFd().getJavaType());
        }
    }

    void voGeneratorCommonVariables(
        String javaType, String nativeType, String defaultVal, String fldNameCamel, Chunk chunk) {
        chunk
            .voVariables
            .append("public ")
            .append(nativeType)
            .append(" ")
            .append(fldNameCamel)
            .append(";\n");
        chunk
            .voInitializationContent
            .append("(")
            .append(fldNameCamel)
            .append(" == null), (")
            .append(fldNameCamel)
            .append(" == null) ? ")
            .append(defaultVal)
            .append("  : (")
            .append(javaType)
            .append(") ")
            .append(fldNameCamel);
        chunk.voConstructorVariables.append(nativeType).append(' ').append(fldNameCamel);
    }

    void voGeneratorTypecastVariables(
        String javaType,
        String nativeType,
        String defaultVal,
        String methodName,
        Chunk chunk,
        String fldNameCamel) {

        chunk
            .voVariables
            .append("public ")
            .append(nativeType)
            .append(" ")
            .append(fldNameCamel)
            .append(";\n");
        chunk
            .voInitializationContent
            .append('(')
            .append(fldNameCamel)
            .append(" == null), (")
            .append(fldNameCamel)
            .append(" == null) ? ")
            .append(defaultVal)
            .append("  : ((")
            .append(javaType)
            .append(") ")
            .append(fldNameCamel)
            .append(").")
            .append(methodName)
            .append("()");
        chunk.voConstructorVariables.append(nativeType).append(' ').append(fldNameCamel);
    }

    public static class Chunk {
        StringBuilder voInitializationContent = new StringBuilder(1024 * 10);
        StringBuilder readerFldDefinitionContent = new StringBuilder(1024 * 10);
        StringBuilder voVariables = new StringBuilder(1024 * 2);
        StringBuilder voNullVariables = new StringBuilder(1024 * 2);
        StringBuilder voConstructorVariables = new StringBuilder(1024 * 2);
        StringBuilder voThisVarAssignment = new StringBuilder(1024 * 2);
        StringBuilder voToProtoRecord = new StringBuilder(1024 * 2);
        StringBuilder voToProtoExtRecord = new StringBuilder(1024 * 2);
        StringBuilder builderVariables = new StringBuilder();
        boolean hasBuilder = false;
    }

    @Getter
    @Setter
    public static class FieldDetailDao {

        private FieldDetail fd;
        private String msgname;
        private String msgNameCamel;
        private String fldName;
        private String fldNameCamel;
        private String nullFldName;
        private String nullFldNameCamel;

        public FieldDetailDao(FieldDetail fd) {

            this.setFd(fd);

            int dotPos = fd.getFieldName().indexOf('.');
            if (dotPos >= 0) {
                this.setMsgname(fd.getFieldName().substring(0, dotPos));
                setMsgNameCamel(CaseUtils.toCamelCase(this.getMsgname(), false, '_'));
                this.setFldName(fd.getFieldName().substring(dotPos + 1));
            } else {
                this.setMsgname(null);
                this.setFldName(fd.getFieldName());
            }
            this.setFldNameCamel(CaseUtils.toCamelCase(this.getFldName(), false, '_'));

            setNullFldName("null_" + fd.getFieldName());
            setNullFldNameCamel(CaseUtils.toCamelCase(getNullFldName(), false, '_', '.'));
        }

        public String getRecordName() {
            if (null == this.getMsgNameCamel()) return "recordBuilder";
            return this.getMsgNameCamel() + "Builder";
        }
    }
}
