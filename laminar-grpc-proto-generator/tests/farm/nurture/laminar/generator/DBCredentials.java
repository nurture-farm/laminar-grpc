package farm.nurture.laminar.generator;

import java.util.UUID;

public class DBCredentials {
    private DBCredentials(){}

    public static final String DB_NAME = "testServiceDB";
    public static final String USER_NAME = "nurtureLocalUser";
    public static final String DB_PASSWORD = "password";
    public static final String DB_URL = "localhost";
    public static final  String PATH = "tests/resources/TestFiles/";
    public static final  String CONFIG_FILE_PATH = PATH + "config.json";
    public static final  String DUMP_FILE_PATH = PATH + "dump.sql";
    public static final  String LAM_FILE_PATH = PATH + "application.lam";
    public static final  String GENERATED_UUID = "lam_" + UUID.randomUUID().toString().substring(0, 8);
    public static final String NULL_PATH = "tests/resources/TestFiles/";
    public static final String NULL_LAM = NULL_PATH + "nullApplication.lam";

}
