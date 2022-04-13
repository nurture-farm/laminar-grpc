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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import farm.nurture.infra.util.StringUtils;
import lombok.Data;

@Data
public class Configuration {

    private Server server;
    private Prometheus prometheus;
    private TemporalWorker temporalWorker;
    private Proto proto;
    private DeploymentConfig deploymentConfig;
    private Contract contract;
    private GenerationInfo generationInfo;
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    public void validateConfig() {
        if (this.getProto() == null) {
            logger.error("Proto config is not given");
            throw new NullPointerException("Proto config is not given");
        }

        Configuration.Proto protoConfig = this.getProto();
        if (StringUtils.isEmpty(protoConfig.getServiceName())) {
            logger.error("Server configuration is not valid");
            throw new NullPointerException("Server configuration is not valid");
        }
    }

    @Data
    public static final class Server {
        private Integer port;

        private String apiHook;

        private String startupHook;
    }

    @Data
    public static final class Prometheus {
        private Boolean enabled;

        private Integer port;
    }

    @Data
    public static final class TemporalWorker {
        private Boolean enabled;

        private String namespace;

        private String address;

        private String taskQueue;
    }

    @Data
    public static final class Proto {
        private String serviceName;

        private String namespace;

        private String graphqlUri;

        private String protoPackage;

        private String javaPackage;

        private String goPackage;

        private String codeUrl;

        private String javaGeneratePath;

        private String javaOverridePath;

        private String goGeneratePath;

        private String goOverridePath;

        private String graphqlGeneratePath;

        private String protoGeneratePath;

        private String codeGenerationInputConfigSource;

        private String projectPath;
    }

    @Data
    public static final class DeploymentConfig{
        private String ecrLink;
    }

    @Data
    public static final class GenerationInfo{
        private boolean newRepo;
    }

    @Data
    public static final class Contract {

        private String contractPath;

        private String contractTag;
    }
}
