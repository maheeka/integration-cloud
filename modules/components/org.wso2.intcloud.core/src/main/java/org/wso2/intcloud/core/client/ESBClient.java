/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.intcloud.core.client;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.task.stub.TaskManagementException;
import org.wso2.intcloud.common.IntCloudException;
import org.wso2.intcloud.common.util.IntCloudUtil;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class ESBClient {

    private static Log log = LogFactory.getLog(ESBClient.class);

    private static ESBClient esbClient = new ESBClient();

    private ESBClient() {
        log.debug("Initializing ESBClient");
        System.setProperty("javax.net.ssl.trustStore", IntCloudUtil.getPropertyValue("ESBKeyStorePath"));
        System.setProperty("javax.net.ssl.trustStorePassword", IntCloudUtil.getPropertyValue("ESBTrustStorePassword"));
    }

    public static ESBClient getInstance() {
        return esbClient;
    }

    public void deployCarbonApp(String carbonApplicationName, String carbonApplicationPath) throws IntCloudException {
        CarbonApplicationClient.getInstance().deployCarbonApp(carbonApplicationName, carbonApplicationPath);
    }

    public void deployCarbonApp(int tenantId, String carbonApplicationName, String carbonApplicationPath)
            throws IntCloudException {
        CarbonApplicationClient.getInstance().deployCarbonApp(tenantId, carbonApplicationName, carbonApplicationPath);
    }

    public void unDeployCarbonApp(String carbonApplicationName) throws IntCloudException {
        CarbonApplicationClient.getInstance().unDeployCarbonApp(carbonApplicationName);
    }

    public void unDeployCarbonApp(int tenantId, String carbonApplicationName) throws IntCloudException {
        CarbonApplicationClient.getInstance().unDeployCarbonApp(tenantId, carbonApplicationName);
    }

    public String getIntegrationParamConfiguration(String carbonApplicationName) throws IntCloudException {
        return SequenceTemplateClient.getInstance().getSequenceTemplate(carbonApplicationName);
    }

    public String getIntegrationParamConfiguration(int tenantId, String carbonApplicationName)
            throws IntCloudException {
        return SequenceTemplateClient.getInstance().getSequenceTemplate(tenantId, carbonApplicationName);
    }

    public String deployScheduleTask(String applicationName, String paramConfiguration) throws IntCloudException {
        OMElement taskConfiguration =
                ScheduledTaskClient.getInstance().addTask(applicationName, new JSONObject(paramConfiguration));
        return taskConfiguration.toString();
    }

    public void deployTestScheduleTask(String applicationName, String paramConfiguration) throws IntCloudException {
        ScheduledTaskClient.getInstance().addTestTask(applicationName, new JSONObject(paramConfiguration));
    }

    public String deployScheduleTask(int tenantId, String applicationName, String paramConfiguration)
            throws IntCloudException {
        OMElement taskConfiguration = ScheduledTaskClient.getInstance().addTask(tenantId, applicationName,
                                                                                new JSONObject(paramConfiguration));
        return taskConfiguration.toString();
    }

    public void deployTestScheduleTask(int tenantId, String applicationName, String paramConfiguration)
            throws IntCloudException {
        ScheduledTaskClient.getInstance().addTestTask(tenantId, applicationName, new JSONObject(paramConfiguration));
    }

    public void stopScheduleTask(int tenantId, String applicationName, String taskConfiguration)
            throws IntCloudException, TaskManagementException, XMLStreamException, IOException, InterruptedException {
        ScheduledTaskClient.getInstance().stopTask(tenantId, applicationName, taskConfiguration);
    }
}
