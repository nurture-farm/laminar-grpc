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

import farm.nurture.infra.util.StringUtils;
import farm.nurture.laminar.core.util.CaseUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AppConfigVO {
    private static boolean isContractUsed;
    private long id;
    private String title;
    private String reqName;
    private String resName;
    private boolean declReq;
    private boolean declRes;
    private boolean declGrpc;
    private boolean declGrapql;
    private String sqlStmt;
    private String sqlParams;
    private String sqlReplace;
    private boolean sqlUniquekey;
    private String sqlPool;
    private boolean implDao;
    private boolean implGrpc;
    private boolean implReacrjs;
    private String reqOverride;
    private String resOverride;
    private boolean oauthPublic;
    private String oauthClaims;
    private String mutation;
    private boolean status;
    private boolean nullId;
    private boolean nullTitle;
    private boolean nullReqName;
    private boolean nullResName;
    private boolean nullDeclReq;
    private boolean nullDeclRes;
    private boolean nullDeclGrpc;
    private boolean nullDeclGrapql;
    private boolean nullSqlStmt;
    private boolean nullSqlParams;
    private boolean nullSqlReplace;
    private boolean nullSqlUniquekey;
    private boolean nullSqlPool;
    private boolean nullImplDao;
    private boolean nullImplGrpc;
    private boolean nullImplReacrjs;
    private boolean nullReqOverride;
    private boolean nullResOverride;
    private boolean nullOauthPublic;
    private boolean nullOauthClaims;
    private boolean nullMutation;
    private boolean nullStatus;

    public AppConfigVO(
        boolean nullId,
        long id,
        boolean nullTitle,
        String title,
        boolean nullReqName,
        String reqName,
        boolean nullResName,
        String resName,
        boolean nullDeclReq,
        boolean declReq,
        boolean nullDeclRes,
        boolean declRes,
        boolean nullDeclGrpc,
        boolean declGrpc,
        boolean nullDeclGrapql,
        boolean declGrapql,
        boolean nullSqlStmt,
        String sqlStmt,
        boolean nullSqlParams,
        String sqlParams,
        boolean nullSqlReplace,
        String sqlReplace,
        boolean nullSqlUniquekey,
        boolean sqlUniquekey,
        boolean nullSqlPool,
        String sqlPool,
        boolean nullImplDao,
        boolean implDao,
        boolean nullImplGrpc,
        boolean implGrpc,
        boolean nullImplReacrjs,
        boolean implReacrjs,
        boolean nullReqOverride,
        String reqOverride,
        boolean nullResOverride,
        String resOverride,
        boolean nullOauthPublic,
        boolean oauthPublic,
        boolean nullOauthClaims,
        String oauthClaims,
        boolean nullMutation,
        String mutation,
        boolean nullStatus,
        boolean status) {

        setNullId(nullId);
        setId(id);
        setNullTitle(nullTitle);
        setTitle(title);
        setNullReqName(nullReqName);
        setReqName(reqName);
        setNullResName(nullResName);
        setResName(resName);
        setNullDeclReq(nullDeclReq);
        setDeclReq(declReq);
        setNullDeclRes(nullDeclRes);
        setDeclRes(declRes);
        setNullDeclGrpc(nullDeclGrpc);
        setDeclGrpc(declGrpc);
        setNullDeclGrapql(nullDeclGrapql);
        setDeclGrapql(declGrapql);
        setNullSqlStmt(nullSqlStmt);
        setSqlStmt(sqlStmt);
        setNullSqlParams(nullSqlParams);
        setSqlParams(sqlParams);
        setNullSqlReplace(nullSqlReplace);
        setSqlReplace(sqlReplace);
        setNullSqlUniquekey(nullSqlUniquekey);
        setSqlUniquekey(sqlUniquekey);
        setNullSqlPool(nullSqlPool);
        setSqlPool(sqlPool);
        setNullImplDao(nullImplDao);
        setImplDao(implDao);
        setNullImplGrpc(nullImplGrpc);
        setImplGrpc(implGrpc);
        setNullImplReacrjs(nullImplReacrjs);
        setImplReacrjs(implReacrjs);
        setNullReqOverride(nullReqOverride);
        setReqOverride(reqOverride);
        setNullResOverride(nullResOverride);
        setResOverride(resOverride);
        setNullOauthPublic(nullOauthPublic);
        setOauthPublic(oauthPublic);
        setNullOauthClaims(nullOauthClaims);
        setOauthClaims(oauthClaims);
        setNullMutation(nullMutation);
        setMutation(mutation);
        setNullStatus(nullStatus);
        setStatus(status);
    }

    public static boolean isIsContractUsed() {
        return isContractUsed;
    }

    public static void setIsContractUsed(boolean isContractUsed) {
        AppConfigVO.isContractUsed = isContractUsed;
    }

    public void setOauthClaims(String oauthClaimsInput) {
        if (oauthClaimsInput == null){
            oauthClaimsInput = "";
        }
        this.oauthClaims = oauthClaimsInput;
    }

    public void setReqOverride(String reqOverrideInput) {
        if (reqOverrideInput == null){
            reqOverrideInput = "";
        }
        this.reqOverride = reqOverrideInput;
    }

    public void setResOverride(String resOverrideInput) {
        if (resOverrideInput == null){
            resOverrideInput = "";
        }
        this.resOverride = resOverrideInput;
    }

    public void setReqName(String reqNameInput) {
        if (reqNameInput == null){
            reqNameInput = "";
        }
        this.reqName = reqNameInput;
    }

    public void setResName(String resNameInput) {
        if (resNameInput == null){
            resNameInput = "";
        }
        this.resName = resNameInput;
    }

    public void setSqlReplace(String sqlReplaceInput) {
        if (sqlReplaceInput == null){
            sqlReplaceInput = "";
        }
        this.sqlReplace = sqlReplaceInput;
    }

    public void setSqlPool(String sqlPoolInput) {
        if (sqlPoolInput == null){
            sqlPoolInput = "";
        }
        this.sqlPool = sqlPoolInput;
    }

    public String getReqName() {
        if (nullReqName || reqName.trim().length() == 0) return getTitle() + "Request";
        else return reqName;
    }

    public String getResName() {
        if (nullResName || resName.trim().length() == 0) return getTitle() + "Response";
        else return resName;
    }

    public String getBulkTitle() {
        return getTitle() + "Bulk";
    }

    public String getBulkReqName() {
        return "Bulk" + getReqName();
    }

    public String getBulkResName() {
        return "Bulk" + getResName();
    }

    public boolean isMutation() {
        return ("I".equals(mutation) || "U".equals(mutation) || "D".equals(mutation));
    }

    public void setMutation(String mutation) {
        this.mutation = mutation;
    }

    public boolean isInsert() {
        return ("I".equals(mutation));
    }

    public String getRecordName() {
        return getResName() + "Record";
    }

    // abc:repeated int32
    public Map<String, String> getSqlReplaces() {
        Map<String, String> uniqueReplaces = new HashMap<>();
        if (!nullSqlReplace) {
            List<String> replaceTokens = StringUtils.fastSplit(sqlReplace, ',');
            for (String rToken : replaceTokens) {
                String rTokenName = rToken.substring(0, rToken.indexOf(':'));
                String rTokenType = rToken.substring(rToken.indexOf(':') + 1);
                String rTokenNameCamel = CaseUtils.toCamelCase(rTokenName, false, '_');
                uniqueReplaces.put(rTokenNameCamel, rTokenType);
            }
        }
        return uniqueReplaces;
    }
}
