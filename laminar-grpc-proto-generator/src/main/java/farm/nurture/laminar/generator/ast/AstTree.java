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

package farm.nurture.laminar.generator.ast;

import static farm.nurture.laminar.generator.Constants.JAVA_PACKAGE;

import farm.nurture.infra.util.Logger;
import farm.nurture.infra.util.LoggerFactory;
import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.Configuration;
import farm.nurture.laminar.generator.FieldDetail;
import farm.nurture.laminar.generator.TemplatesVO;
import farm.nurture.laminar.generator.ast.golang.AstClient;
import farm.nurture.laminar.generator.ast.golang.AstCommon;
import farm.nurture.laminar.generator.ast.golang.AstConfig;
import farm.nurture.laminar.generator.ast.golang.AstExecutor;
import farm.nurture.laminar.generator.ast.golang.AstHelper;
import farm.nurture.laminar.generator.ast.golang.AstHook;
import farm.nurture.laminar.generator.ast.golang.AstMain;
import farm.nurture.laminar.generator.ast.golang.AstMakeVO;
import farm.nurture.laminar.generator.ast.golang.AstMetrics;
import farm.nurture.laminar.generator.ast.golang.AstModel;
import farm.nurture.laminar.generator.ast.golang.AstServer;
import farm.nurture.laminar.generator.ast.golang.AstService;
import farm.nurture.laminar.generator.ast.golang.AstServiceExecutor;
import farm.nurture.laminar.generator.ast.golang.AstServiceExecutorTest;
import farm.nurture.laminar.generator.ast.golang.AstServiceTest;
import farm.nurture.laminar.generator.ast.graphql.AstGraphQL;
import farm.nurture.laminar.generator.ast.javalang.AstAbstractBackendController;
import farm.nurture.laminar.generator.ast.javalang.AstDao;
import farm.nurture.laminar.generator.ast.javalang.AstGrpcClient;
import farm.nurture.laminar.generator.ast.javalang.AstGrpcServer;
import farm.nurture.laminar.generator.ast.proto.AstProto;
import java.util.ArrayList;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
public class AstTree extends AstBase {

    private List<AstBase> parts = new ArrayList<>();
    private AstBase.Flags flags;
    private String configFilePath;
    private String lamFilePath;
    private String dumpFilePath;
    private static final Logger logger = LoggerFactory.getLogger(AstTree.class);

    @Override
    public void onStart(Configuration conf, AstBase.Flags flags) {
        this.flags = flags;
        if (flags.isProto()) {
            parts.add(new AstProto());
        }
        if (flags.isGraphql()) parts.add(new AstGraphQL());

        if (flags.isJavaLang()) {
            parts.add(new AstAbstractBackendController());
            parts.add(new AstGrpcServer());
            parts.add(new AstDao());
            parts.add(new AstGrpcClient());
        }

        if (flags.isGoLang()) {
            parts.add(new AstMain());
            parts.add(new AstServer());
            parts.add(new AstHook());
            parts.add(new AstService());
            parts.add(new AstMakeVO());
            parts.add(new AstModel());
            parts.add(new AstHelper());
            parts.add(new AstExecutor());
            parts.add(new AstServiceExecutor());
            parts.add(new AstServiceExecutorTest());
            parts.add(new AstServiceTest());
            parts.add(new AstMetrics());
            parts.add(new AstCommon());
            parts.add(new AstInputFiles(configFilePath, lamFilePath, dumpFilePath));

            if (conf.getGenerationInfo().isNewRepo()) {
                parts.add(new AstConfig());
                parts.add(new AstClient());
            }
        }

        super.onStart(conf, flags);
        for (AstBase b : parts) b.onStart(conf, flags);
    }


    public AstBase.Flags setLang(String language) {

        logger.info("Language is:" + language);
        AstBase.Flags currFlags = new AstBase.Flags();
        if (language != null && language.length() > 0) currFlags.signal(false);

        if (language != null) {
            if (language.contains("proto")) currFlags.setProto(true);
            if (language.contains("java")) currFlags.setJavaLang(true);
            if (language.contains("go")) currFlags.setGoLang(true);
            if (language.contains("graphql")) currFlags.setGraphql(true);
            if (language.contains("contract")) currFlags.setContracts(true);
        }
        return currFlags;
    }

    @Override
    public void onEntry(
        AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields) {
        for (AstBase b : parts) b.onEntry(entry, requestFields, responseFields);
    }

    @Override
    public void onEnd() {
        super.onEnd();
        for (AstBase b : parts) b.onEnd();

        if (flags.isJavaLang()) {
            writeToFile(
                getJavaPackageDirectory() + "/Configuration.java",
                TemplatesVO.getConfiguration().replace(JAVA_PACKAGE, getJavaPackage()));
        }

        if (flags.isGoLang()) {
            writeToFile(
                getGoGeneratePath() + "/common.go",
                readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/common.go.template"));
        }
    }
}
