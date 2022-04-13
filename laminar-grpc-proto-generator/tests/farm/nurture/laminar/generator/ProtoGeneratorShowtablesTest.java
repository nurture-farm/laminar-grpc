package farm.nurture.laminar.generator;


import static farm.nurture.laminar.generator.DBCredentials.DB_NAME;
import static farm.nurture.laminar.generator.DBCredentials.DB_URL;
import static farm.nurture.laminar.generator.DBCredentials.DB_PASSWORD;
import static farm.nurture.laminar.generator.DBCredentials.USER_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

import farm.nurture.laminar.generator.ast.AstBase;
import farm.nurture.laminar.generator.ast.AstTree;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;


class ProtoGeneratorShowtablesTest {

    private final ProtoGeneratorShowtables obj = new ProtoGeneratorShowtables();

    @Test
    void showTables() {
        AstTree astTree = new AstTree();
        AstBase.Flags flags = astTree.setLang("go,proto,graphql");
        Map<String, FieldDetail> res = null;
        try{
            res = obj.showTables(DB_NAME, USER_NAME, DB_PASSWORD, DB_URL, flags);
            }

        catch (Exception e){
            fail("exception was raised");
        }
        assertNotEquals(null,res);
        assertNotEquals(0,res.size());

        HashMap<String, String[]> dataTypeMap = new HashMap<>();
        // protoType --> {fieldName, protoType, javaType, goType}
        dataTypeMap.put("fixed64", new String[]{"fixed64", "fixed64", "Long", "fixed64"});
        dataTypeMap.put("app_config.res_name", new String[]{"res_name", "string", "String", "string"});
        dataTypeMap.put("app_config.sql_replace", new String[]{"sql_replace", "string", "String", "string"});
        dataTypeMap.put("sint32", new String[]{"sint32", "sint32", "Integer", "sint32"});
        dataTypeMap.put("app_config.res_override", new String[]{"res_override", "string", "String", "string"});
        dataTypeMap.put("bool", new String[]{"bool", "bool", "Boolean", "bool"});
        dataTypeMap.put("string", new String[]{"string", "string", "String", "string"});
        dataTypeMap.put("sfixed64", new String[]{"sfixed64", "sfixed64", "Long", "sfixed64"});
        dataTypeMap.put("app_config.sql_params", new String[]{"sql_params", "string", "String", "string"});
        dataTypeMap.put("app_config.impl_reacrjs", new String[]{"impl_reacrjs", "bool", "Boolean", "bool"});
        dataTypeMap.put("app_config.status", new String[]{"status", "bool", "Boolean", "bool"});
        dataTypeMap.put("User.id", new String[]{"id", "int32", "Integer", "int32"});
        dataTypeMap.put("float", new String[]{"float", "float", "Float", "float32"});
        dataTypeMap.put("app_config.decl_grapql", new String[]{"decl_grapql", "bool", "Boolean", "bool"});
        dataTypeMap.put("User.currTime", new String[]{"currTime", "google.protobuf.Timestamp", "java.sql.Timestamp", "timestamp"});
        dataTypeMap.put("app_config.id", new String[]{"id", "int64", "Long", "int64"});
        dataTypeMap.put("app_config.req_override", new String[]{"req_override", "string", "String", "string"});
        dataTypeMap.put("int64", new String[]{"int64", "int64", "Long", "int64"});
        dataTypeMap.put("repeated string", new String[]{"repeated string", "repeated string", "String", "[]string"});
        dataTypeMap.put("sfixed32", new String[]{"sfixed32", "sfixed32", "Integer", "sfixed32"});
        dataTypeMap.put("uint32", new String[]{"uint32", "uint32", "Integer", "uint32"});
        dataTypeMap.put("app_config.mutation", new String[] {"mutation", "enum Mutation{ I = 0; U = 1; D = 2; S = 3; - = 4;} \n" + "Mutation", "String", "enum"});
        dataTypeMap.put("app_config.title", new String[]{"title", "string", "String", "string"});
        dataTypeMap.put("fixed32", new String[]{"fixed32", "fixed32", "Integer", "fixed32"});
        dataTypeMap.put("sint64", new String[]{"sint64", "sint64", "Long", "sint64"});
        dataTypeMap.put("app_config.oauth_claims", new String[]{"oauth_claims", "string", "String", "string"});
        dataTypeMap.put("double", new String[]{"double", "double", "Double", "float64"});
        dataTypeMap.put("app_config.oauth_public", new String[]{"oauth_public", "bool", "Boolean", "bool"});
        dataTypeMap.put("app_config.req_name", new String[]{"req_name", "string", "String", "string"});
        dataTypeMap.put("app_config.decl_grpc", new String[]{"decl_grpc", "bool", "Boolean", "bool"});
        dataTypeMap.put("app_config.sql_pool", new String[]{"sql_pool", "string", "String", "string"});
        dataTypeMap.put("app_config.sql_stmt", new String[]{"sql_stmt", "string", "String", "string"});
        dataTypeMap.put("app_config.impl_dao", new String[]{"impl_dao", "bool", "Boolean", "bool"});
        dataTypeMap.put("repeated int32", new String[]{"repeated int32", "repeated int32", "Integer", "[]int32"});
        dataTypeMap.put("app_config.impl_grpc", new String[]{"impl_grpc", "bool", "Boolean", "bool"});
        dataTypeMap.put("app_config.decl_res", new String[]{"decl_res", "bool", "Boolean", "bool"});
        dataTypeMap.put("int32", new String[]{"int32", "int32", "Integer", "int32"});
        dataTypeMap.put("bytes", new String[]{"bytes", "bytes", "byte[]", "[] byte"});
        dataTypeMap.put("uint64", new String[]{"uint64", "uint64", "Long", "uint64"});
        dataTypeMap.put("app_config.decl_req", new String[]{"decl_req", "bool", "Boolean", "bool"});
        dataTypeMap.put("app_config.sql_uniquekey", new String[]{"sql_uniquekey", "bool", "Boolean", "bool"});

        // protoType --> {fieldName, protoType, javaType, goType}
        for (String key : res.keySet()) {
            assertEquals(dataTypeMap.get(key)[0],res.get(key).getFieldName());
            assertEquals(dataTypeMap.get(key)[1],res.get(key).getProtoType());
            assertEquals(dataTypeMap.get(key)[2],res.get(key).getJavaType());
            assertEquals(dataTypeMap.get(key)[3],res.get(key).getGoType());
            }
    }
}