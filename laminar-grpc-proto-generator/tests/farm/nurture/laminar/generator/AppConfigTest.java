package farm.nurture.laminar.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

import farm.nurture.laminar.core.sql.dao.ReadBase;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.reflection.FieldSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AppConfigTest {
    AppConfig appConfigMock = mock(AppConfig.class);
    private static final Logger logger = LoggerFactory.getLogger(DatabaseReader.class);

    @Mock
    private ResultSet resultSet = Mockito.mock(ResultSet.class);

    @Test
    void populate(){

        try {
            Mockito.when(resultSet.getObject(1)).thenReturn(1L);
            Mockito.when(resultSet.getObject(2)).thenReturn("FindAge");
            Mockito.when(resultSet.getObject(3)).thenReturn("FindAgeRequest");
            Mockito.when(resultSet.getObject(4)).thenReturn("FindAgeResponse");
            Mockito.when(resultSet.getObject(5)).thenReturn(null);
            Mockito.when(resultSet.getObject(6)).thenReturn(null);
            Mockito.when(resultSet.getObject(7)).thenReturn(null);
            Mockito.when(resultSet.getObject(8)).thenReturn(null);
            Mockito.when(resultSet.getObject(9)).thenReturn("SELECT age FROM User WHERE id = ?;");
            Mockito.when(resultSet.getObject(10)).thenReturn("id:User.id");
            Mockito.when(resultSet.getObject(11)).thenReturn(null);
            Mockito.when(resultSet.getObject(12)).thenReturn(null);
            Mockito.when(resultSet.getObject(13)).thenReturn(null);
            Mockito.when(resultSet.getObject(14)).thenReturn(null);
            Mockito.when(resultSet.getObject(15)).thenReturn(null);
            Mockito.when(resultSet.getObject(16)).thenReturn(null);
            Mockito.when(resultSet.getObject(17)).thenReturn(null);
            Mockito.when(resultSet.getObject(18)).thenReturn(null);
            Mockito.when(resultSet.getObject(19)).thenReturn(null);
            Mockito.when(resultSet.getObject(20)).thenReturn(null);
            Mockito.when(resultSet.getObject(21)).thenReturn("U");
            Mockito.when(resultSet.getObject(22)).thenReturn(null);
            Mockito.when(resultSet.next()).thenReturn(true).thenReturn(false);

            Field rs = ReadBase.class.getDeclaredField("rs");
            rs.setAccessible(true);
            FieldSetter.setField(appConfigMock, rs, resultSet);
            List<AppConfigVO> returnedVal = appConfigMock.populate();

            assertEquals("FindAge",returnedVal.get(0).getTitle());
            assertEquals("FindAgeRequest",returnedVal.get(0).getReqName());
            assertEquals("FindAgeResponse",returnedVal.get(0).getResName());
            assertEquals("SELECT age FROM User WHERE id = ?;",returnedVal.get(0).getSqlStmt());
            assertEquals("id:User.id",returnedVal.get(0).getSqlParams());
            assertEquals("U",returnedVal.get(0).getMutation());
            assertFalse(returnedVal.get(0).isDeclReq());
            assertFalse(returnedVal.get(0).isDeclRes());
            assertFalse(returnedVal.get(0).isDeclGrpc());
            assertFalse(returnedVal.get(0).isDeclGrapql());
            assertEquals("",returnedVal.get(0).getSqlReplace());
            assertFalse(returnedVal.get(0).isSqlUniquekey());
            assertEquals("",returnedVal.get(0).getSqlPool());
            assertFalse(returnedVal.get(0).isImplDao());
            assertFalse(returnedVal.get(0).isImplGrpc());
            assertFalse(returnedVal.get(0).isImplReacrjs());
            assertEquals("",returnedVal.get(0).getReqOverride());
            assertEquals("",returnedVal.get(0).getResOverride());
            assertFalse(returnedVal.get(0).isOauthPublic());
            assertEquals("",returnedVal.get(0).getOauthClaims());
            assertFalse(returnedVal.get(0).isStatus());

        } catch (NoSuchFieldException | SQLException e) {
            logger.error("error in populate",e);
            fail();
        }
    }

    @Test
    void getFirstRow() {
    }

    @Test
    void getRecordsCount() {
        }
}