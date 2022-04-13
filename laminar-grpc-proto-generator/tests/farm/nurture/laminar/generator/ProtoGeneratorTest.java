package farm.nurture.laminar.generator;


import static farm.nurture.laminar.generator.DBCredentials.CONFIG_FILE_PATH;
import static farm.nurture.laminar.generator.DBCredentials.PATH;
import static farm.nurture.laminar.generator.DBCredentials.DB_URL;
import static farm.nurture.laminar.generator.DBCredentials.DUMP_FILE_PATH;
import static farm.nurture.laminar.generator.DBCredentials.LAM_FILE_PATH;
import static farm.nurture.laminar.generator.DBCredentials.DB_PASSWORD;
import static farm.nurture.laminar.generator.DBCredentials.USER_NAME;
import static farm.nurture.laminar.generator.DBCredentials.GENERATED_UUID;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

class ProtoGeneratorTest {

    private final ProtoGenerator protoGenerator = new ProtoGenerator();

    @Test
    void generateCodeTest1() {
        try{
            protoGenerator.generateCode(new String [] {"go,proto,graphql", CONFIG_FILE_PATH, DUMP_FILE_PATH, LAM_FILE_PATH, GENERATED_UUID, USER_NAME,
                DB_PASSWORD, DB_URL, PATH});
            }
        catch (SQLException | IOException e){
            fail("exception was thrown");
        }
    }

    @Test
    void generateCodeTest2(){
        Exception exp = null;
        try{
            protoGenerator.generateCode(new String [] {"go,proto,graphql", "", DUMP_FILE_PATH, LAM_FILE_PATH, GENERATED_UUID, USER_NAME,
                DB_PASSWORD, DB_URL, PATH});
        }
        catch (SQLException | IOException e){
            exp = e;
        }
        assertNotEquals(null,exp);
    }

}