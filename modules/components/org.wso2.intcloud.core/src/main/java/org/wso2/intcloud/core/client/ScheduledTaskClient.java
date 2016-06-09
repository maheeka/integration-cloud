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
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.task.stub.TaskAdminStub;
import org.wso2.carbon.task.stub.TaskManagementException;
import org.wso2.intcloud.common.IntCloudException;
import org.wso2.intcloud.common.util.IntCloudUtil;
import org.wso2.intcloud.services.tenant.tasks.stub.types.TenantTaskAdminServiceStub;
import org.wso2.intcloud.services.tenant.tasks.stub.types.TenantTaskAdminServiceTaskManagementExceptionException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;
import java.util.UUID;

public class ScheduledTaskClient {

    private static Log log = LogFactory.getLog(ScheduledTaskClient.class);

    private static ScheduledTaskClient scheduledTaskClient = null;

    TaskAdminStub taskAdminStub = null;

    TenantTaskAdminServiceStub tenantTaskAdminServiceStub = null;

    private ScheduledTaskClient() throws IntCloudException {

        try {
            taskAdminStub = new TaskAdminStub(IntCloudUtil.getPropertyValue("TaskAdminService"));
        } catch (AxisFault axisFault) {
            throw new IntCloudException(axisFault.getMessage(), axisFault);
        }
        ServiceClient client = taskAdminStub._getServiceClient();
        Options client_options = client.getOptions();
        HttpTransportProperties.Authenticator authenticator = new HttpTransportProperties.Authenticator();
        authenticator.setUsername(IntCloudUtil.getPropertyValue("ESBServerUserName"));
        authenticator.setPassword(IntCloudUtil.getPropertyValue("ESBServerPassword"));
        authenticator.setPreemptiveAuthentication(true);
        client_options.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, authenticator);
        client.setOptions(client_options);

        try {
            tenantTaskAdminServiceStub =
                    new TenantTaskAdminServiceStub(IntCloudUtil.getPropertyValue("TenantTaskAdminService"));
        } catch (AxisFault axisFault) {
            throw new IntCloudException(axisFault.getMessage(), axisFault);
        }
        ServiceClient ten_client = tenantTaskAdminServiceStub._getServiceClient();
        Options ten_client_options = ten_client.getOptions();
        HttpTransportProperties.Authenticator ten_authenticator = new HttpTransportProperties.Authenticator();
        ten_authenticator.setUsername(IntCloudUtil.getPropertyValue("ESBServerUserName"));
        ten_authenticator.setPassword(IntCloudUtil.getPropertyValue("ESBServerPassword"));
        ten_authenticator.setPreemptiveAuthentication(true);
        ten_client_options.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, ten_authenticator);
        ten_client.setOptions(ten_client_options);
    }

    public static ScheduledTaskClient getInstance() throws IntCloudException {
        if (scheduledTaskClient == null) {
            synchronized (ScheduledTaskClient.class) {
                if (scheduledTaskClient == null) {
                    scheduledTaskClient = new ScheduledTaskClient();
                }
            }
        }
        return scheduledTaskClient;
    }

    public OMElement addTask(int tenantId, String applicationName, JSONObject paramConfigurationJSON)
            throws IntCloudException {
        OMElement taskOM = null;
        try {
            String taskConfiguration = getTaskConfiguration(applicationName, paramConfigurationJSON);
            taskOM = AXIOMUtil.stringToOM(taskConfiguration);
            log.info("Deploying task configuration : " + taskConfiguration);
            tenantTaskAdminServiceStub.addTaskDescriptionInTenant(tenantId, taskConfiguration);
        } catch (XMLStreamException | RemoteException | TenantTaskAdminServiceTaskManagementExceptionException e) {
            throw new IntCloudException(e.getMessage(), e);
        }
        return taskOM;
    }

    public OMElement addTask(String applicationName, JSONObject paramConfigurationJSON) throws IntCloudException {
        OMElement taskOM = null;
        try {
            String taskConfiguration = getTaskConfiguration(applicationName, paramConfigurationJSON);
            taskOM = AXIOMUtil.stringToOM(taskConfiguration);
            log.info("Deploying task configuration : " + taskConfiguration);
            taskAdminStub.addTaskDescription(taskOM);
        } catch (XMLStreamException | RemoteException | TaskManagementException e) {
            throw new IntCloudException(e.getMessage(), e);
        }
        return taskOM;
    }

    private String getTaskConfiguration(String applicationName, JSONObject paramConfigurationJSON)
            throws XMLStreamException {
        Object template_name = paramConfigurationJSON.get("template_name");

        StringBuilder taskConfiguration = new StringBuilder();
        taskConfiguration.append("<task:task xmlns=\"http://ws.apache.org/ns/synapse\" xmlns:task=\"http://www.wso2" +
                                 ".org/products/wso2commons/tasks\" name=\"");
        taskConfiguration.append(applicationName);
        taskConfiguration.append("-");
        taskConfiguration.append(template_name);
        taskConfiguration.append("-Task\"");
        taskConfiguration.append(" class=\"org.apache.synapse.startup.tasks.TemplateMessageExecutor\"\n" +
                                 "        group=\"synapse.simple.quartz\">");

        taskConfiguration.append("<task:trigger count=\"");
        taskConfiguration.append(paramConfigurationJSON.getJSONObject("schedule").get("count"));
        taskConfiguration.append("\" interval=\"");
        taskConfiguration.append(paramConfigurationJSON.getJSONObject("schedule").get("interval"));
        taskConfiguration.append("\" />");

        taskConfiguration.append("<task:property xmlns:task=\"http://www.wso2.org/products/wso2commons/tasks\"\n" +
                                 "        name=\"templateParams\"><params>");

        JSONArray paramsArray = paramConfigurationJSON.getJSONArray("params");

        for (int i = 0; i < paramsArray.length(); i++) {
            JSONObject paramJSON = paramsArray.getJSONObject(i);
            taskConfiguration.append("<");
            taskConfiguration.append(paramJSON.get("param"));
            taskConfiguration.append(">");
            taskConfiguration.append(paramJSON.get("value"));
            taskConfiguration.append("</");
            taskConfiguration.append(paramJSON.get("param"));
            taskConfiguration.append(">");
        }

        taskConfiguration.append("</params>");
        taskConfiguration.append("</task:property>");

        taskConfiguration.append("<task:property xmlns:task=\"http://www.wso2.org/products/wso2commons/tasks\" " +
                                 "name=\"templateKey\" value=\"");
        taskConfiguration.append(template_name);
        taskConfiguration.append("\"/></task:task>");

        return taskConfiguration.toString();
    }

    public void addTestTask(int tenantId, String applicationName, JSONObject paramConfigurationJSON)
            throws IntCloudException {

        String randomApplicationName = applicationName + UUID.randomUUID();

        paramConfigurationJSON.put("count", "1");
        paramConfigurationJSON.put("interval", "1");

        OMElement task = addTask(tenantId, randomApplicationName, paramConfigurationJSON);

        String taskName = task.getAttributeValue(new QName("name"));
        String taskGroup = task.getAttributeValue(new QName("group"));

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            throw new IntCloudException(e.getMessage(), e);
        }

        deleteTask(tenantId, taskName, taskGroup);

    }

    public void addTestTask(String applicationName, JSONObject paramConfigurationJSON) throws IntCloudException {

        String randomApplicationName = applicationName + UUID.randomUUID();

        paramConfigurationJSON.put("count", "1");
        paramConfigurationJSON.put("interval", "1");

        OMElement task = addTask(randomApplicationName, paramConfigurationJSON);

        String taskName = task.getAttributeValue(new QName("name"));
        String taskGroup = task.getAttributeValue(new QName("group"));

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            throw new IntCloudException(e.getMessage(), e);
        }

        deleteTask(taskName, taskGroup);

    }

    public void deleteTask(int tenantId, String name, String group) throws IntCloudException {
        log.info("Deleting task " + name);
        try {
            tenantTaskAdminServiceStub.deleteTaskDescriptionInTenant(tenantId, name, group);
        } catch (RemoteException | TenantTaskAdminServiceTaskManagementExceptionException e) {
            throw new IntCloudException("Error deleting integration task " + name, e);
        }
    }

    public void deleteTask(String name, String group) throws IntCloudException {
        log.info("Deleting task " + name);
        try {
            taskAdminStub.deleteTaskDescription(name, group);
        } catch (RemoteException | TaskManagementException e) {
            throw new IntCloudException("Error deleting integration task " + name, e);
        }
    }

    public void stopTask(int tenantId, String applicationName, String taskConfiguration) throws IntCloudException {
        OMElement task = null;
        try {
            task = AXIOMUtil.stringToOM(taskConfiguration);
        } catch (XMLStreamException e) {
            throw new IntCloudException("Error stopping integration task for " + applicationName, e);
        }

        String taskName = task.getAttributeValue(new QName("name"));
        String taskGroup = task.getAttributeValue(new QName("group"));

        deleteTask(tenantId, taskName, taskGroup);
    }

    public void stopTask(String applicationName, String taskConfiguration) throws IntCloudException {
        OMElement task = null;
        try {
            task = AXIOMUtil.stringToOM(taskConfiguration);
        } catch (XMLStreamException e) {
            throw new IntCloudException("Error stopping integration task for " + applicationName, e);
        }

        String taskName = task.getAttributeValue(new QName("name"));
        String taskGroup = task.getAttributeValue(new QName("group"));

        deleteTask(taskName, taskGroup);
    }
}