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

package farm.nurture.laminar.generator.ast.javalang;

import static farm.nurture.laminar.generator.Constants.ABSTRACT_BACKEND_CONTROLLER_TEMPLATE;
import static farm.nurture.laminar.generator.Constants.BACKEND_CONTROLLER;
import static farm.nurture.laminar.generator.Constants.BACKEND_CONTROLLER_FACTORY;
import static farm.nurture.laminar.generator.Constants.BACKEND_CONTROLLER_FACTORY_TEMPLATE;
import static farm.nurture.laminar.generator.Constants.BACKEND_CONTROLLER_INSERT_REQ_RES;
import static farm.nurture.laminar.generator.Constants.BACKEND_CONTROLLER_INSERT_TEMPLATE;
import static farm.nurture.laminar.generator.Constants.BACKEND_CONTROLLER_SELECT_ON_DATA;
import static farm.nurture.laminar.generator.Constants.BACKEND_CONTROLLER_SELECT_REQ_RES;
import static farm.nurture.laminar.generator.Constants.BACKEND_CONTROLLER_SELECT_TEMPLATE;
import static farm.nurture.laminar.generator.Constants.FORWARD_SLASH;
import static farm.nurture.laminar.generator.Constants.JAVA_PACKAGE;
import static farm.nurture.laminar.generator.Constants.SERVICE_NAME;
import static farm.nurture.laminar.generator.Constants.SERVICE_REQ;
import static farm.nurture.laminar.generator.Constants.SERVICE_RES;

import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.Configuration;
import farm.nurture.laminar.generator.FieldDetail;
import farm.nurture.laminar.generator.TemplatesVO;
import farm.nurture.laminar.generator.ast.AstBase;
import java.util.List;

public class AstAbstractBackendController extends AstBase {

    private static String AbstractBackendControllerF =
        TemplatesVO.getTEMPLATE_DIR() + ABSTRACT_BACKEND_CONTROLLER_TEMPLATE;
    private static String BackendControllerFactoryF =
        TemplatesVO.getTEMPLATE_DIR() + BACKEND_CONTROLLER_FACTORY_TEMPLATE;
    private static String BackendControllerInsertF =
        TemplatesVO.getTEMPLATE_DIR() + BACKEND_CONTROLLER_INSERT_TEMPLATE;
    private static String BackendControllerInsertReqResF =
        TemplatesVO.getTEMPLATE_DIR() + BACKEND_CONTROLLER_INSERT_REQ_RES;
    private static String BackendControllerSelectF =
        TemplatesVO.getTEMPLATE_DIR() + BACKEND_CONTROLLER_SELECT_TEMPLATE;
    private static String BackendControllerSelectOnDataF =
        TemplatesVO.getTEMPLATE_DIR() + BACKEND_CONTROLLER_SELECT_ON_DATA;
    private static String BackendControllerSelectReqResF =
        TemplatesVO.getTEMPLATE_DIR() + BACKEND_CONTROLLER_SELECT_REQ_RES;
    private final String AbstractBackendController;
    private final String BackendControllerFactory;
    private final String BackendControllerInsert;
    private final String BackendControllerInsertReqRes;
    private final String BackendControllerSelect;
    private final String BackendControllerSelectOnData;
    private final String BackendControllerSelectReqRes;
    private StringBuilder backendControllerSb = new StringBuilder(4096);

    public AstAbstractBackendController() {
        AbstractBackendController = readTemplateFile(AbstractBackendControllerF);
        BackendControllerFactory = readTemplateFile(BackendControllerFactoryF);
        BackendControllerInsert = readTemplateFile(BackendControllerInsertF);
        BackendControllerInsertReqRes = readTemplateFile(BackendControllerInsertReqResF);
        BackendControllerSelect = readTemplateFile(BackendControllerSelectF);
        BackendControllerSelectOnData = readTemplateFile(BackendControllerSelectOnDataF);
        BackendControllerSelectReqRes = readTemplateFile(BackendControllerSelectReqResF);
    }

    @Override
    public void onStart(Configuration conf, AstBase.Flags flags) {
        super.onStart(conf, flags);
    }

    @Override
    public void onEntry(
        AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields) {

        if (entry.isDeclGrpc()) {
            ifDeclGrpc(entry);
        }

        if (entry.isImplGrpc()) {
            ifImplGrpc(entry);
        }

        if (entry.isImplGrpc() || entry.isDeclGrpc()) {
            ifImplOrDecl(entry);
        }
    }

    private void ifImplOrDecl(AppConfigVO entry) {
        if (entry.isMutation()) {
            backendControllerSb.append(
                BackendControllerInsert.replace(SERVICE_NAME, entry.getTitle())
                    .replace(SERVICE_REQ, entry.getReqName())
                    .replace(SERVICE_RES, entry.getResName()));

            // Bulk onError
            backendControllerSb.append(
                BackendControllerInsert.replace(SERVICE_NAME, "Bulk" + entry.getTitle())
                    .replace(SERVICE_REQ, entry.getBulkReqName())
                    .replace(SERVICE_RES, entry.getBulkResName()));

        } else {
            backendControllerSb.append(
                BackendControllerSelect.replace(SERVICE_NAME, entry.getTitle())
                    .replace(SERVICE_REQ, entry.getReqName())
                    .replace(SERVICE_RES, entry.getResName()));
        }
    }

    private void ifImplGrpc(AppConfigVO entry) {
        if (!entry.isMutation()) {
            backendControllerSb.append(
                BackendControllerSelectOnData.replace(SERVICE_NAME, entry.getReqName())
                    .replace(SERVICE_RES, entry.getResName()));
        }
    }

    private void ifDeclGrpc(AppConfigVO entry) {
        if (entry.isMutation()) {

            // Regular insert
            backendControllerSb.append(
                BackendControllerInsertReqRes.replace(SERVICE_NAME, entry.getTitle())
                    .replace(SERVICE_REQ, entry.getReqName())
                    .replace(SERVICE_RES, entry.getResName()));

            // Bulk insert
            backendControllerSb.append(
                BackendControllerInsertReqRes.replace(SERVICE_NAME, "Bulk" + entry.getTitle())
                    .replace(SERVICE_REQ, entry.getBulkReqName())
                    .replace(SERVICE_RES, entry.getBulkResName()));

        } else {
            backendControllerSb.append(
                BackendControllerSelectReqRes.replace(SERVICE_NAME, entry.getTitle())
                    .replace(SERVICE_REQ, entry.getReqName())
                    .replace(SERVICE_RES, entry.getResName()));
        }
    }

    @Override
    public void onEnd() {
        super.onEnd();

        writeToFile(
            getJavaPackageDirectory() + FORWARD_SLASH + BACKEND_CONTROLLER_FACTORY,
            BackendControllerFactory.replace(JAVA_PACKAGE, getJavaPackage()));

        writeToFile(
            getJavaPackageDirectory() + FORWARD_SLASH + BACKEND_CONTROLLER,
            AbstractBackendController.replace("@hooks@", backendControllerSb.toString())
                .replace(JAVA_PACKAGE, getJavaPackage()));
    }
}
