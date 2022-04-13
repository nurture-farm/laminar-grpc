package farm.nurture.laminar.generator.config.input;

import static farm.nurture.laminar.generator.DBCredentials.NULL_LAM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import farm.nurture.laminar.generator.AppConfigVO;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class LamFileCodeGeneratorInputServiceImplTest {

    @Test
    void getApplicationConfiguration() {
        // This test checks if null is converted to "" while reading creating the objects from .lam file
        LamFileCodeGeneratorInputServiceImpl obj;
        List<AppConfigVO> returned_val = null;
        try{
            obj = new LamFileCodeGeneratorInputServiceImpl();
            returned_val = obj.getApplicationConfiguration(NULL_LAM);
            }
        catch (IOException e){
            fail();
        }

        for (AppConfigVO val : returned_val){
            assertEquals("FindAgeRequest",val.getReqName());
            assertEquals("FindAgeResponse",val.getResName());
            assertEquals("",val.getSqlReplace());
            assertEquals("",val.getReqOverride());
            assertEquals("",val.getResOverride());
            assertEquals("",val.getOauthClaims());
        }
    }
}