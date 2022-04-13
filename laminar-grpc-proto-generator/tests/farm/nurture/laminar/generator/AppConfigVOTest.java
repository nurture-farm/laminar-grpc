package farm.nurture.laminar.generator;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AppConfigVOTest {
    private AppConfigVO appConfigVO = new AppConfigVO();

    AppConfigVOTest(){
        appConfigVO.setId(0);
        appConfigVO.setTitle("UpdateAge");
        appConfigVO.setReqName("");
        appConfigVO.setResName("");
        appConfigVO.setDeclReq(true);
        appConfigVO.setDeclRes(true);
        appConfigVO.setDeclGrpc(true);
        appConfigVO.setDeclGrapql(false);
        appConfigVO.setSqlStmt("UPDATE User SET age = ? WHERE id = ?;");
        appConfigVO.setSqlParams("age:User.age,id:User.id");
        appConfigVO.setSqlReplace("");
        appConfigVO.setSqlUniquekey(false);
        appConfigVO.setSqlPool(null);
        appConfigVO.setImplDao(true);
        appConfigVO.setImplGrpc(true);
        appConfigVO.setImplReacrjs(false);
        appConfigVO.setReqOverride("");
        appConfigVO.setResOverride("");
        appConfigVO.setOauthPublic(false);
        appConfigVO.setOauthClaims("");
        appConfigVO.setMutation("U");
        appConfigVO.setStatus(true);
        appConfigVO.setNullId(false);
        appConfigVO.setNullTitle(false);
        appConfigVO.setNullReqName(false);
        appConfigVO.setNullResName(false);
        appConfigVO.setNullDeclReq(false);
        appConfigVO.setNullDeclRes(false);
        appConfigVO.setNullDeclGrpc(false);
        appConfigVO.setNullDeclGrapql(false);
        appConfigVO.setNullSqlStmt(false);
        appConfigVO.setNullSqlParams(false);
        appConfigVO.setNullSqlReplace(true);
        appConfigVO.setNullSqlUniquekey(false);
        appConfigVO.setNullSqlPool(false);
        appConfigVO.setNullImplDao(false);
        appConfigVO.setNullImplGrpc(false);
        appConfigVO.setNullImplReacrjs(false);
        appConfigVO.setNullReqOverride(false);
        appConfigVO.setNullResOverride(false);
        appConfigVO.setNullOauthPublic(false);
        appConfigVO.setNullOauthClaims(false);
        appConfigVO.setNullMutation(false);
        appConfigVO.setNullStatus(false);

    }

    @Test
    void getReqName() {
        assertEquals("UpdateAgeRequest",appConfigVO.getReqName());
    }

    @Test
    void getResName() {
        assertEquals("UpdateAgeResponse",appConfigVO.getResName());
    }

    @Test
    void getBulkTitle() {
        assertEquals("UpdateAgeBulk",appConfigVO.getBulkTitle());
    }

    @Test
    void getBulkReqName() {
        assertEquals("BulkUpdateAgeRequest",appConfigVO.getBulkReqName());
    }

    @Test
    void getBulkResName() {
        assertEquals("BulkUpdateAgeResponse",appConfigVO.getBulkResName());
    }

    @Test
    void isMutation() {
        assertTrue(appConfigVO.isMutation());
    }
}