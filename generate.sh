echo "export LANG=go,proto,graphql      Default : java,proto,graphql"
echo "Usage generate.sh config.json"
echo "-----------------------------"

export TARGET_ROOT=/Users/kishannigam/IdeaProjects/laminar-grpc/laminar-grpc-proto-generator/target
export CORE_ROOT=/Users/kishannigam/IdeaProjects/laminar-grpc/laminar-grpc-core/target
export TARGET_LIB=$TARGET_ROOT/libs
export M2=/Users/kishannigam/.m2/repository

if [ -z "${LANG}" ]; then
    export LANG=go
fi

java -Dlog4j.configurationFile=log4j2.xml -classpath .:$TARGET_ROOT/laminar-grpc-proto-generator-0.0.18-SNAPSHOT.jar:$CORE_ROOT/laminar-grpc-core-0.0.18-SNAPSHOT.jar:$M2/farm/nurture/infra/0.0.1/infra-0.0.1.jar::$TARGET_LIB//HikariCP-3.4.5.jar:$TARGET_LIB//archaius-core-0.7.7.jar:$TARGET_LIB//checker-qual-2.11.1.jar:$TARGET_LIB//commons-configuration-1.8.jar:$TARGET_LIB//commons-lang-2.6.jar:$TARGET_LIB//commons-logging-1.1.1.jar:$TARGET_LIB//disruptor-3.4.2.jar:$TARGET_LIB//error_prone_annotations-2.3.4.jar:$TARGET_LIB//failureaccess-1.0.1.jar:$TARGET_LIB//guava-29.0-jre.jar:$TARGET_LIB//hamcrest-core-1.3.jar:$TARGET_LIB//infra-0.0.1.jar:$TARGET_LIB//j2objc-annotations-1.3.jar:$TARGET_LIB//jackson-annotations-2.9.0.jar:$TARGET_LIB//jackson-core-2.9.6.jar:$TARGET_LIB//jackson-databind-2.9.6.jar:$TARGET_LIB//javax.servlet-api-3.1.0.jar:$TARGET_LIB//jsr305-3.0.1.jar:$TARGET_LIB//junit-4.12.jar:$TARGET_LIB//laminar-grpc-core-0.0.18-SNAPSHOT.jar:$TARGET_LIB//listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar:$TARGET_LIB//log4j-api-2.9.0.jar:$TARGET_LIB//log4j-core-2.9.0.jar:$TARGET_LIB//log4j-slf4j-impl-2.9.0.jar:$TARGET_LIB//lombok-1.18.12.jar:$TARGET_LIB//mysql-connector-java-8.0.19.jar:$TARGET_LIB//netty-buffer-4.1.47.Final.jar:$TARGET_LIB//netty-codec-4.1.47.Final.jar:$TARGET_LIB//netty-codec-http-4.1.47.Final.jar:$TARGET_LIB//netty-common-4.1.47.Final.jar:$TARGET_LIB//netty-handler-4.1.47.Final.jar:$TARGET_LIB//netty-resolver-4.1.47.Final.jar:$TARGET_LIB//netty-transport-4.1.47.Final.jar:$TARGET_LIB//protobuf-java-3.6.1.jar:$TARGET_LIB//simpleclient-0.2.0.jar:$TARGET_LIB//simpleclient_common-0.2.0.jar:$TARGET_LIB//slf4j-api-1.7.25.jar farm.nurture.laminar.generator.ProtoGenerator config.json $LANG dump.sql


echo "----------- FOR GOLANG GENERATION ------------------"
echo export GO111MODULE=on  # Enable module mode
echo go get google.golang.org/protobuf/cmd/protoc-gen-go \
          google.golang.org/grpc/cmd/protoc-gen-go-grpc
echo export PATH=\"$PATH:$(go env GOPATH)/bin\"
echo "protoc --go_out=zerotouch/golang/proto --go-grpc_out=zerotouch/golang/proto zerotouch/golang/proto/*.proto"

echo "----------- FOR JAVA GENERATION ------------------"
echo "export PATH=\$PATH:~/.gradle/caches/modules-2/metadata-2.95/descriptors/io.grpc/"
echo "protoc --java_out=zerotouch/src/main/java --grpc-java_out=zerotouch/src/main/java  zerotouch/src/main/proto/farm_service.proto"
