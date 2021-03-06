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

package farm.nurture.laminar.core.sql.dao;

import java.io.File;
import java.sql.SQLException;

public class DbLoader {

    private static String lineSeparator = System.getProperty("line.separator");

    private DbLoader() {}

    public static void loadDataFromCsvFile(String fileName, String tableName) throws SQLException {
        File dataFile = new File(fileName);
        // This is for windows only. Just leave it as it is.
        String absolutePath = dataFile.getAbsolutePath().replace('\\', '/');
        StringBuilder strBuild = new StringBuilder(100);
        strBuild.append("LOAD DATA INFILE '");
        strBuild.append(absolutePath);
        strBuild.append("' INTO TABLE ");
        strBuild.append(tableName);
        strBuild.append(" FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '");
        strBuild.append(lineSeparator);
        strBuild.append("'");
        new WriteBase().execute(strBuild.toString(), new Object[] {});
    }
}
