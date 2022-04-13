package farm.nurture.laminar.generator;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;

class DatabaseReaderTest {
    private final DatabaseReader reader = new DatabaseReader();
    private FieldDetail v1 = null;
    private FieldDetail v2 = null;
    private FieldDetail v3 = null;
    private FieldDetail v4 = null;
    private FieldDetail v5 = null;
    private FieldDetail v6 = null;
    private FieldDetail v7 = null;
    private FieldDetail v8 = null;

    @Test
    void deriveProtoDataTypeTest1() {
        try{
            v1 = reader.deriveProtoDataType("fakeVariable1", "GEOMETRY", "User");
            v2 = reader.deriveProtoDataType("fakeVariable2","MEDIUMTEXT","User");
            v3 = reader.deriveProtoDataType("fakeVariable3","LONGTEXT","User");
            v4 = reader.deriveProtoDataType("fakeVariable4","TEXT","User");
            v5 = reader.deriveProtoDataType("fakeVariable5","VARCHAR","User");
            v6 = reader.deriveProtoDataType("fakeVariable6","LONGVARCHAR","User");
            v7 = reader.deriveProtoDataType("fakeVariable7","CHAR","User");
            v8 = reader.deriveProtoDataType("fakeVariable8","JSON","User");
        }
        catch (IOException e){
            fail("IOException was raised");
        }
        assertEquals("string",v1.getProtoType());
        assertEquals("string",v1.getGoType());
        assertEquals("string",v2.getProtoType());
        assertEquals("string",v2.getGoType());
        assertEquals("string",v3.getProtoType());
        assertEquals("string",v3.getGoType());
        assertEquals("string",v4.getProtoType());
        assertEquals("string",v5.getGoType());
        assertEquals("string",v5.getProtoType());
        assertEquals("string",v5.getGoType());
        assertEquals("string",v6.getProtoType());
        assertEquals("string",v6.getGoType());
        assertEquals("string",v7.getProtoType());
        assertEquals("string",v7.getGoType());
        assertEquals("string",v8.getProtoType());
        assertEquals("string",v8.getGoType());
    }

    @Test
    void deriveProtoDataTypeTest2() {
        try{
            v1 = reader.deriveProtoDataType("fakeVariable9", "BIT", "User");
            v2 = reader.deriveProtoDataType("fakeVariable10","TINYINT(1)","User");
        }
        catch (IOException e){
            fail("IOException was raised");
        }
        assertEquals("bool",v1.getProtoType());
        assertEquals("bool",v1.getGoType());
        assertEquals("bool",v2.getProtoType());
        assertEquals("bool",v2.getGoType());
    }

    @Test
    void deriveProtoDataTypeTest3() {
        try{
        v1 = reader.deriveProtoDataType("fakeVariable11","SMALLINT","User");
        v2 = reader.deriveProtoDataType("fakeVariable12","TINYINT","User");
        v3 = reader.deriveProtoDataType("fakeVariable13","TINYINT(30)","User");
        }
        catch (IOException e){
            fail("IOException was raised");
        }
        assertEquals("int32",v1.getProtoType());
        assertEquals("int32",v1.getGoType());
        assertEquals("int32",v2.getProtoType());
        assertEquals("int32",v2.getGoType());
        assertEquals("int32",v3.getProtoType());
        assertEquals("int32",v3.getGoType());
    }

    @Test
    void deriveProtoDataTypeTest4() {
        try{
            v1 = reader.deriveProtoDataType("fakeVariable14","BIGINT","User");
        }
        catch (IOException e){
            fail("IOException was raised");
        }
        assertEquals("int64",v1.getProtoType());
        assertEquals("int64",v1.getGoType());
    }

    @Test
    void deriveProtoDataTypeTest5() {
        try{
            v1 = reader.deriveProtoDataType("fakeVariable15","DECIMAL","User");
            v2 = reader.deriveProtoDataType("fakeVariable16","NUMERIC","User");
        }
        catch (IOException e){
            fail("IOException was raised");
        }
        assertEquals("double",v1.getProtoType());
        assertEquals("float64",v1.getGoType());
        assertEquals("double",v2.getProtoType());
        assertEquals("float64",v2.getGoType());
    }

    @Test
    void deriveProtoDataTypeTest6() {
        try{
            v1 = reader.deriveProtoDataType("fakeVariable17","FLOAT","User");
            v2 = reader.deriveProtoDataType("fakeVariable18","DOUBLE","User");
        }
        catch (IOException e){
            fail("IOException was raised");
        }
        assertEquals("double",v1.getProtoType());
        assertEquals("float64",v1.getGoType());
        assertEquals("double",v2.getProtoType());
        assertEquals("float64",v2.getGoType());
    }

    @Test
    void deriveProtoDataTypeTest7() {
        try{
            v1 = reader.deriveProtoDataType("fakeVariable19","DATE","User");
        }
        catch (IOException e){
            fail("IOException was raised");
        }
        assertEquals("int64",v1.getProtoType());
        assertEquals("datetime",v1.getGoType());
    }

    @Test
    void deriveProtoDataTypeTest8() {
        try{
            v1 = reader.deriveProtoDataType("fakeVariable20","DATETIME","User");
        }
        catch (IOException e){
            fail("IOException was raised");
        }
        assertEquals("int64",v1.getProtoType());
        assertEquals("datetime",v1.getGoType());
    }

    @Test
    void deriveProtoDataTypeTest9() {
        try{
            v1 = reader.deriveProtoDataType("fakeVariable21","TIMESTAMP","User");
        }
        catch (IOException e){
            fail("IOException was raised");
        }
        assertEquals("google.protobuf.Timestamp",v1.getProtoType());
        assertEquals("timestamp",v1.getGoType());
    }

    @Test
    void deriveProtoDataTypeTest10() {
        try{
            v1 = reader.deriveProtoDataType("fakeVariable22","TIME","User");
        }
        catch (IOException e){
            fail("IOException was raised");
        }
        assertEquals("string",v1.getProtoType());
        assertEquals("time.Time",v1.getGoType());
    }

    @Test
    void deriveProtoDataTypeTest11() {
        try{
            v1 = reader.deriveProtoDataType("fakeVariable23","LONGVARBINARY","User");
            v2 = reader.deriveProtoDataType("fakeVariable24","VARBINARY","User");
            v3 = reader.deriveProtoDataType("fakeVariable25","BINARY","User");
            v4 = reader.deriveProtoDataType("fakeVariable26","BLOB","User");
            v5 = reader.deriveProtoDataType("fakeVariable27","CLOB","User");
        }
        catch (IOException e){
            fail("IOException was raised");
        }
        assertEquals("bytes",v1.getProtoType());
        assertEquals("[]byte",v1.getGoType());
        assertEquals("bytes",v2.getProtoType());
        assertEquals("[]byte",v2.getGoType());
        assertEquals("bytes",v3.getProtoType());
        assertEquals("[]byte",v3.getGoType());
        assertEquals("bytes",v4.getProtoType());
        assertEquals("[]byte",v4.getGoType());
        assertEquals("bytes",v5.getProtoType());
        assertEquals("[]byte",v5.getGoType());
    }


    @Test
    void deriveProtoDataTypeTest12() {
        try{
            v1 = reader.deriveProtoDataType("fakeVariable28","ARRAY","User");
        }
        catch (IOException e){
            fail("IOException was raised");
        }
        assertEquals("repeated",v1.getProtoType());
        assertEquals("[]",v1.getGoType());
    }

    @Test
    void deriveProtoDataTypeTest13() {
        IOException exp = null;
        try{
            reader.deriveProtoDataType("fakeVariable29","RANDOM_DATA_TYPE","User");
        }
        catch (IOException e){
            exp = e;
        }
        assertNotEquals(exp,null);
    }


    @Test
    void getFieldDetails() {
        List<FieldDetail> res = null;
        try{
            res = reader.getFieldDetails("testServiceDB", "nurtureLocalUser", "password",
                "SELECT * FROM User", "LOCALHOST");
        }
        catch (SQLException e){
            fail();
        }
        assertEquals("google.protobuf.Timestamp",res.get(0).getProtoType());
        assertEquals("timestamp",res.get(0).getGoType());
        assertEquals("int32",res.get(1).getProtoType());
        assertEquals("int32",res.get(1).getGoType());
        assertNotEquals(null,res);
        assertNotEquals(0,res.size()); // because a table cannot be column-less
    }
}