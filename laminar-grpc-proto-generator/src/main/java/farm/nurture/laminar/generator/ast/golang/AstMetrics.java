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

package farm.nurture.laminar.generator.ast.golang;

import static farm.nurture.laminar.generator.Constants.METRIC_NAME;
import static farm.nurture.laminar.generator.Constants.SERVICE_ACRONYM;
import static farm.nurture.laminar.generator.Constants.SERVICE_NAME;
import static farm.nurture.laminar.generator.Constants.TITLE;
import static farm.nurture.laminar.generator.Constants.UNDERSCORE_ERROR;

import farm.nurture.laminar.generator.AppConfigVO;
import farm.nurture.laminar.generator.Configuration;
import farm.nurture.laminar.generator.FieldDetail;
import farm.nurture.laminar.generator.TemplatesVO;
import farm.nurture.laminar.generator.ast.AstBase;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AstMetrics extends AstBase {

    private List<AstBase> parts = new ArrayList<>();

    private String metricsContent;
    private String summaryMetricsDeclareContent;
    private String counterMetricsDeclareContent;
    private String metricsInitContent;

    private StringBuilder metricsDeclareContentBuf = new StringBuilder();
    private StringBuilder metricsInitContentBuf = new StringBuilder();

    public AstMetrics() {
        metricsContent = readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/metrics.go.template");
        summaryMetricsDeclareContent =
            readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/summarymetrics.declare.go.template");
        counterMetricsDeclareContent =
            readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/countermetrics.declare.go.template");
        metricsInitContent =
            readTemplateFile(TemplatesVO.getTEMPLATE_DIR() + "/golang/metrics.init.go.template");
    }

    @Override
    public void onStart(Configuration conf, Flags flags) {
        super.onStart(conf, flags);
        for (AstBase b : parts) b.onStart(conf, flags);
    }

    @Override
    public void onEntry(
        AppConfigVO entry, List<FieldDetail> requestFields, List<FieldDetail> responseFields) {

        for (AstBase b : parts) b.onEntry(entry, requestFields, responseFields);

        String metricsDeclareContentL =
            summaryMetricsDeclareContent.replace(SERVICE_ACRONYM, getAcroNym(super.getServiceName()));
        metricsDeclareContentL = metricsDeclareContentL.replace(TITLE, entry.getTitle());
        metricsDeclareContentL =
            metricsDeclareContentL.replace(METRIC_NAME, entry.getTitle().toUpperCase());

        metricsDeclareContentBuf.append(metricsDeclareContentL).append("\n");
        metricsInitContentBuf.append(metricsInitContent.replace(TITLE, entry.getTitle()));

        String errorMetricsDeclareContentL =
            counterMetricsDeclareContent.replace(SERVICE_ACRONYM, getAcroNym(super.getServiceName()));
        errorMetricsDeclareContentL =
            errorMetricsDeclareContentL.replace(TITLE, entry.getTitle() + UNDERSCORE_ERROR);
        errorMetricsDeclareContentL =
            errorMetricsDeclareContentL.replace(METRIC_NAME, entry.getTitle().toUpperCase() + UNDERSCORE_ERROR);

        metricsDeclareContentBuf.append(errorMetricsDeclareContentL).append("\n");
        metricsInitContentBuf.append(
            metricsInitContent.replace(TITLE, entry.getTitle() + UNDERSCORE_ERROR));

        if (entry.isInsert()) {
            String bulkmetricsDeclareContentL =
                summaryMetricsDeclareContent.replace(SERVICE_ACRONYM, getAcroNym(super.getServiceName()));
            bulkmetricsDeclareContentL =
                bulkmetricsDeclareContentL.replace(TITLE, "Bulk" + entry.getTitle());
            bulkmetricsDeclareContentL =
                bulkmetricsDeclareContentL.replace(METRIC_NAME, "BULK" + entry.getTitle().toUpperCase());

            metricsDeclareContentBuf.append(bulkmetricsDeclareContentL).append("\n");
            metricsInitContentBuf.append(metricsInitContent.replace(TITLE, "Bulk" + entry.getTitle()));

            String errorbulkmetricsDeclareContentL =
                counterMetricsDeclareContent.replace(SERVICE_ACRONYM, getAcroNym(super.getServiceName()));
            errorbulkmetricsDeclareContentL =
                errorbulkmetricsDeclareContentL.replace(
                    TITLE, "Bulk" + entry.getTitle() + UNDERSCORE_ERROR);
            errorbulkmetricsDeclareContentL =
                errorbulkmetricsDeclareContentL.replace(
                    METRIC_NAME, "BULK" + entry.getTitle().toUpperCase() + UNDERSCORE_ERROR);

            metricsDeclareContentBuf.append(errorbulkmetricsDeclareContentL).append("\n");
            metricsInitContentBuf.append(
                metricsInitContent.replace(TITLE, "Bulk" + entry.getTitle() + UNDERSCORE_ERROR));
        }
    }

    private String getAcroNym(String serviceName) {

        StringBuilder acronym = new StringBuilder();
        for (int i = 0; i < serviceName.length(); i++) {
            if (Character.isUpperCase(serviceName.charAt(i))) {
                char w = serviceName.charAt(i);
                acronym.append(w);
            }
        }
        return acronym.toString();
    }

    @Override
    public void onEnd() {
        super.onEnd();
        for (AstBase b : parts) b.onEnd();

        File pj = new File(getGoGeneratePath() + "/metrics");
        if (!pj.exists()) pj.mkdirs();

        String metricsContentL =
            metricsContent.replace("@declare_metrics@", metricsDeclareContentBuf.toString());
        metricsContentL = metricsContentL.replace("@init_metrics@", metricsInitContentBuf.toString());
        metricsContentL = metricsContentL.replace(SERVICE_NAME, getServiceName());
        metricsContentL = metricsContentL.replace("@code_url@", super.getCodeUrl());

        writeToFile(super.getGoGeneratePath() + "/metrics/metrics.go", metricsContentL);
    }
}
