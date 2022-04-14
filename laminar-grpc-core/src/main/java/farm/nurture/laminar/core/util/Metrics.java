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

package farm.nurture.laminar.core.util;

import farm.nurture.infra.metrics.IMetricSummary;
import farm.nurture.infra.metrics.MetricFactory;

public class Metrics {

    private static Metrics singleton;
    private static String LBL_QUERYID = "nmethod";
    private static String LBL_CODE = "ncode";

    private IMetricSummary mTraffic;
    private IMetricSummary mBytesSent;
    private IMetricSummary mBytesReceived;

    private Metrics() {
        mTraffic = MetricFactory.getSummary("", "traffic", Metrics.LBL_QUERYID, Metrics.LBL_CODE);
        mBytesSent = MetricFactory.getSummary("", "bytes_sent", Metrics.LBL_QUERYID, Metrics.LBL_CODE);
        mBytesReceived = MetricFactory.getSummary("", "bytes_received", Metrics.LBL_QUERYID);
    }

    public static Metrics getInstance() {
        if (null != singleton) return singleton;
        synchronized (Metrics.class.getName()) {
            if (null != singleton) return singleton;
            singleton = new Metrics();
        }
        return singleton;
    }
}
