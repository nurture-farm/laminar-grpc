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

import static farm.nurture.laminar.core.sql.dao.Constants.RS_NOT_INITIALIZED_MSG;

import farm.nurture.laminar.core.sql.dao.ReadBase;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AppConfig extends ReadBase<AppConfigVO> {

    private static final Logger LOG = LoggerFactory.getLogger(AppConfig.class);

    public AppConfig() {}

    @Override
    protected final List<AppConfigVO> populate() throws SQLException {

        if (null == this.rs) {
            LOG.warn(RS_NOT_INITIALIZED_MSG);
            throw new SQLException(RS_NOT_INITIALIZED_MSG);
        }

        List<AppConfigVO> records = new ArrayList<>();

        try {
            while (this.rs.next()) {
                recordsCount++;

                Object id = rs.getObject(1);
                Object title = rs.getObject(2);
                Object reqName = rs.getObject(3);
                Object resName = rs.getObject(4);
                Object declReq = rs.getObject(5);
                Object declRes = rs.getObject(6);
                Object declGrpc = rs.getObject(7);
                Object declGrapql = rs.getObject(8);
                Object sqlStmt = rs.getObject(9);
                Object sqlParams = rs.getObject(10);
                Object sqlReplace = rs.getObject(11);
                Object sqlUniquekey = rs.getObject(12);
                Object sqlPool = rs.getObject(13);
                Object implDao = rs.getObject(14);
                Object implGrpc = rs.getObject(15);
                Object implReacrjs = rs.getObject(16);
                Object reqOverride = rs.getObject(17);
                Object resOverride = rs.getObject(18);
                Object oauthPublic = rs.getObject(19);
                Object oauthClaims = rs.getObject(20);
                Object mutation = rs.getObject(21);
                Object status = rs.getObject(22);

                AppConfigVO currRecord =
                    new AppConfigVO(
                        (id == null), (id == null) ? 0 : (Long) id,
                        (title == null), (title == null) ? "" : (String) title,
                        (reqName == null), (reqName == null) ? "" : (String) reqName,
                        (resName == null), (resName == null) ? "" : (String) resName,
                        (declReq == null), (declReq == null) ? false : (Boolean) declReq,
                        (declRes == null), (declRes == null) ? false : (Boolean) declRes,
                        (declGrpc == null), (declGrpc == null) ? false : (Boolean) declGrpc,
                        (declGrapql == null), (declGrapql == null) ? false : (Boolean) declGrapql,
                        (sqlStmt == null), (sqlStmt == null) ? "" : (String) sqlStmt,
                        (sqlParams == null), (sqlParams == null) ? "" : (String) sqlParams,
                        (sqlReplace == null), (sqlReplace == null) ? "" : (String) sqlReplace,
                        (sqlUniquekey == null), (sqlUniquekey == null) ? false : (Boolean) sqlUniquekey,
                        (sqlPool == null), (sqlPool == null) ? "" : (String) sqlPool,
                        (implDao == null), (implDao == null) ? false : (Boolean) implDao,
                        (implGrpc == null), (implGrpc == null) ? false : (Boolean) implGrpc,
                        (implReacrjs == null), (implReacrjs == null) ? false : (Boolean) implReacrjs,
                        (reqOverride == null), (reqOverride == null) ? "" : (String) reqOverride,
                        (resOverride == null), (resOverride == null) ? "" : (String) resOverride,
                        (oauthPublic == null), (oauthPublic == null) ? false : (Boolean) oauthPublic,
                        (oauthClaims == null), (oauthClaims == null) ? "" : (String) oauthClaims,
                        (mutation == null), (mutation == null) ? "" : (String) mutation,
                        (status == null), (status == null) ? false : (Boolean) status);

                records.add(currRecord);
            }

        } catch (SQLException ex) {
            throw new SQLException(ex);
        }
        return records;
    }

    @Override
    protected final AppConfigVO getFirstRow() throws SQLException {
        return null;
    }

    @Override
    protected int getRecordsCount() {
        return recordsCount;
    }
}
