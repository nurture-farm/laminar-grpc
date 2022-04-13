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

import farm.nurture.laminar.generator.ProtoGenerator;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AstInputFiles extends AstBase {

    private String configFilePath;
    private String lamFilePath;
    private String dumpFilePath;
    private static final Logger logger = LoggerFactory.getLogger(AstInputFiles.class);

    public AstInputFiles(String configFilePath, String lamFilePath, String dumpFilePath){
        this.configFilePath = configFilePath;
        this.lamFilePath = lamFilePath;
        this.dumpFilePath = dumpFilePath;
    }

    public void copyFile(String filePath,String fileName){

        File source = new File(filePath);
        File dest = new File(getGoGeneratePath() + "/inputFiles/"+ fileName);
        try {
            FileUtils.copyFile(source, dest);
        } catch (IOException e) {
            logger.error("Error while copying the input files",e);
        }
    }

    @Override
    public void onEnd() {

        File pj = new File(getGoGeneratePath() + "/inputFiles");
        if (!pj.exists()) pj.mkdirs();

        copyFile(lamFilePath,"application.lam");
        copyFile(configFilePath,"config.json");
        copyFile(dumpFilePath,"dump.sql");

    }
}
