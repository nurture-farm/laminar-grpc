package farm.nurture.laminar.generator.config.input;


import static farm.nurture.laminar.generator.DBCredentials.CONFIG_FILE_PATH;
import static farm.nurture.laminar.generator.DBCredentials.DB_URL;
import static farm.nurture.laminar.generator.DBCredentials.LAM_FILE_PATH;
import static farm.nurture.laminar.generator.DBCredentials.DB_PASSWORD;
import static farm.nurture.laminar.generator.DBCredentials.USER_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import farm.nurture.laminar.core.util.JsonUtil;
import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.Configuration;
import farm.nurture.laminar.generator.FieldDetail;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

class MySqlCodeGeneratorInputServiceImplTest {

    // this also tests ReadBase execute
    @Test
    void getApplicationConfiguration() {
        
        Configuration configuration = new Configuration();

        try{
            configuration = JsonUtil.deserialize(Files.readAllBytes(Paths.get(CONFIG_FILE_PATH)), Configuration.class);
            configuration.validateConfig();
            }
        catch (IOException e){
            fail("Exception was raised!");
        }

            if (!configuration.getProto().getCodeGenerationInputConfigSource().equals("LAM")) {
                fail("Wrong codeGenerationInputConfigSource");
            }
            List<AppConfigVO> entries = null;
            try{
                entries= new CodeGeneratorInputFactory().getCodeGeneratorInputService(
                    configuration.getProto().getCodeGenerationInputConfigSource()).getApplicationConfiguration(LAM_FILE_PATH);
            }
            catch (IOException e){
                fail();
            }
            assertEquals("FindAge",entries.get(0).getTitle());
            assertEquals("SELECT age FROM User WHERE id = ?;",entries.get(0).getSqlStmt());
            assertEquals("id:User.id",entries.get(0).getSqlParams());
            assertEquals("UpdateAge",entries.get(1).getTitle());
            assertEquals("UPDATE User SET age = ? WHERE id = ?;",entries.get(1).getSqlStmt());
            assertEquals("age:User.age,id:User.id",entries.get(1).getSqlParams());
            assertEquals("DeleteById",entries.get(2).getTitle());
            assertEquals("DELETE FROM User WHERE id = ?;",entries.get(2).getSqlStmt());
            assertEquals("id:User.id",entries.get(2).getSqlParams());
            assertEquals("InsertValues",entries.get(3).getTitle());
            assertEquals("INSERT INTO User(id,age) VALUES(?,?);",entries.get(3).getSqlStmt());
            assertEquals("id:User.id,age:User.age",entries.get(3).getSqlParams());
        }

        @Test
        void getFieldDetailsForReqRes() {
            Configuration configuration = new Configuration();

            try{
                configuration = JsonUtil.deserialize(Files.readAllBytes(Paths.get(CONFIG_FILE_PATH)), Configuration.class);
                configuration.validateConfig();
            }
            catch (IOException e){
                fail();
            }

            List<FieldDetail> responseFields = new CodeGeneratorInputFactory().getCodeGeneratorInputService(
                    configuration.getProto().getCodeGenerationInputConfigSource())
                .getFieldDetailsForReqRes("Select * From User;", null, "testServiceDB", DB_URL, USER_NAME, DB_PASSWORD);

            assertEquals("currTime",responseFields.get(0).getFieldName());
            assertEquals("timestamp",responseFields.get(0).getGoType());
            assertEquals("google.protobuf.Timestamp",responseFields.get(0).getProtoType());
            assertEquals("id",responseFields.get(1).getFieldName());
            assertEquals("int32",responseFields.get(1).getGoType());
            assertEquals("int32",responseFields.get(1).getProtoType());

        }
    }