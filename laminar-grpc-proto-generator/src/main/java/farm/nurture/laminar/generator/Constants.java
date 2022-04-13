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

public class Constants {

    private Constants() {
        // restrict instantiation
    }

    public static final Integer DEFAULT_NUM_STACKTRACE = 3;
    public static final String DEFAULT_CONFIG_SOURCE = "MySQL";
    public static final String BACKEND_CONTROLLER_FACTORY = "BackendControllerFactory.java";
    public static final String BACKEND_CONTROLLER = "AbstractBackendController.java";
    public static final String SERVICE_NAME = "@service_name@";
    public static final String SERVICE_REQ = "@service_request@";
    public static final String SERVICE_RES = "@service_response@";
    public static final String ABSTRACT_BACKEND_CONTROLLER_TEMPLATE =
        "/java/AbstractBackendController.template";
    public static final String BACKEND_CONTROLLER_FACTORY_TEMPLATE =
        "/java/BackendControllerFactory.template";
    public static final String BACKEND_CONTROLLER_INSERT_TEMPLATE =
        "/java/BackendControllerInsert.template";
    public static final String BACKEND_CONTROLLER_INSERT_REQ_RES =
        "/java/BackendControllerInsertReqRes.template";
    public static final String BACKEND_CONTROLLER_SELECT_TEMPLATE =
        "/java/BackendControllerSelect.template";
    public static final String BACKEND_CONTROLLER_SELECT_ON_DATA =
        "/java/BackendControllerSelectOnData.template";
    public static final String BACKEND_CONTROLLER_SELECT_REQ_RES =
        "/java/BackendControllerSelectReqRes.template";
    public static final String ACTIVITIES_GO_TEMPLATE = "/golang/activities.go.template";
    public static final String ACTIVITIES_INSERT_GO_TEMPLATE =
        "/golang/activities.insert.go.template";
    public static final String ACTIVITIES_BULKINSERT_TEMPLATE =
        "/golang/activities.bulkinsert.go.template";
    public static final String FORWARD_SLASH = "/";
    public static final String SERVICE_NAME_KEBAB = "@service_name_kebab@";
    public static final String JAVA_PACKAGE = "@java_package@";
    public static final String BYTE = "byte[]";
    public static final String NAME_SPACE = "@namespace@";
    public static final String SERVER_PORT = "@server_port@";
    public static final String PROMETHUES_PORT = "@prometheus_port@";
    public static final String MUTATION = "@mutation@";
    public static final String EXECUTE = "execute";
    public static final String BASE_PATH = "@base_path@";
    public static final String THREE_CLOSING_BRACKETS_DOT_SPACE = "))). ";
    public static final String REQUEST_GET = "request.get";
    public static final String CODE_URL = "@code_url@";
    public static final String MODEL_DOT = "model.";
    public static final String SERVICE_ACRONYM = "@service_acronym@";
    public static final String TITLE = "@title@";
    public static final String UNDERSCORE_ERROR = "_Error";
    public static final String METRIC_NAME = "@metric_name@";
    public static final String DOT_PROTO = ".proto";
    public static final String RESPONSE_NAME = "@responseName@";
    public static final String RES_BODY = "@res_body@";
    public static final String REQ_NAME = "@requestName@";
    public static final String RESPONSE_STATUS_FAILURE = "@response_status_failure@";
    public static final String RETURN_RESPONSE = "@return_response@";
    public static final String RESPONSE = "response";
    public static final String RESPONSE_STATUS_SUCCESS = "@response_status_success@";
    public static final String SQL_REPLACE = "@sql_replace@";
    public static final String EXECUTOR = "@executor@";
    public static final String ON_DATA = "@ondata@";
    public static final String EXECUTOR_CONST  ="Executor";
    public static final String PARAM_VALUES = "@paramValues@";
    public static final String GO_PACKAGE = "@go_package@";
    public static final String INT64 = "int64";
    public static final String STRING  = "string";
    public static final String JAVA_TYPE_STRING = "String";
    public static final String DATETIME = "datetime";
    public static final String ENUM = "enum";
    public static final String TIMESTAMP = "timestamp";
    public static final String INT32 = "int32";
    public static final String FLOAT64 = "float64";
    public static final String BOOL = "bool";
    public static final String BOOLEAN = "Boolean";
    public static final String INT = "INT";
    public static final String JAVA_TYPE_INTEGER = "Integer";
    public static final String PROTO_TYPE_FLOAT = "float";
    public static final String JAVA_TYPE_FLOAT = "Float";
    public static final String JAVA_TYPE_LONG = "Long";
    public static final String PROTO_TYPE_DOUBLE = "double";
    public static final String JAVA_TYPE_DOUBLE = "Double";
    public static final String JAVA_MATH_BIG_DECIMAL =  "java.math.BigDecimal";
    public static final String JAVA_SQL_DATE = "java.sql.Date";
    public static final String JAVA_SQL_TIMESTAMP = "java.sql.Timestamp";
    public static final String JAVA_SQL_TIME = "java.sql.Time";
    public static final String BIGINT = "BIGINT";
    public static final String PROTO_TYPE_BYTES = "bytes";
    public static final String PROTO_TYPE_REPEATED = "repeated";
    public static final String GO_PROTO_PATH = "@go_proto_path@";
    public static final String PROTO_TYPE_TIMESTAMP =  "google.protobuf.Timestamp";
}
