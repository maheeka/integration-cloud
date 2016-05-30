package org.wso2.intcloud.core.client;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.task.stub.TaskAdminStub;
import org.wso2.carbon.task.stub.TaskManagementException;
import org.wso2.intcloud.common.IntCloudException;
import org.wso2.intcloud.common.util.IntCloudUtil;

import javax.activation.DataHandler;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;

public class ScheduledTaskClient {

    private static Log log = LogFactory.getLog(ScheduledTaskClient.class);

    private static ScheduledTaskClient scheduledTaskClient = null;

    TaskAdminStub stub = null;

    private ScheduledTaskClient() throws IntCloudException {

        System.setProperty("javax.net.ssl.trustStore", "/Users/maheeka/ESB_WORK/CLOUD/APP_CLOUD/wso2esb-5.0.0-SNAPSHOT/repository/resources/security/wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

        try {
            stub = new TaskAdminStub("https://localhost:9453/services/TaskAdmin");
            //            stub = new TemplateAdminServiceStub(IntCloudUtil.getPropertyValue("TaskAdminService"));
        } catch (AxisFault axisFault) {
            throw new IntCloudException(axisFault.getMessage(), axisFault);
        }
        ServiceClient client = stub._getServiceClient();
        Options client_options = client.getOptions();
        HttpTransportProperties.Authenticator authenticator = new HttpTransportProperties.Authenticator();
        authenticator.setUsername("admin");
        //        authenticator.setUsername(IntCloudUtil.getPropertyValue("ESBServerUserName"));
        authenticator.setPassword("admin");
        //        authenticator.setPassword(IntCloudUtil.getPropertyValue("ESBServerPassword"));
        authenticator.setPreemptiveAuthentication(true);
        client_options.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, authenticator);
        client.setOptions(client_options);
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

    public void addTask(String applicationName, JSONObject paramConfigsJSON)
            throws XMLStreamException, IOException, TaskManagementException {

        String strParamConfiguration = "{\n" +
                                       "\t\"template_name\": \"Salesforce_to_Gmail\",\n" +
                                       "\t\"params\": [{\n" +
                                       "\t\t\"param\": \"query\",\n" +
                                       "\t\t\"value\": \"qq\"\n" +
                                       "\t}, {\n" +
                                       "\t\t\"param\": \"subject\",\n" +
                                       "\t\t\"value\": \"ss\"\n" +
                                       "\t}, {\n" +
                                       "\t\t\"param\": \"recipient\",\n" +
                                       "\t\t\"value\": \"rr\"\n" +
                                       "\t}],\n" +
                                       "\t\"schedule\": {\n" +
                                       "\t\t\"interval\": \"1\",\n" +
                                       "\t\t\"count\": \"5\"\n" +
                                       "\t}\n" +
                                       "}";

        paramConfigsJSON = new JSONObject(strParamConfiguration);

        StringBuilder taskConfiguration = new StringBuilder();
        taskConfiguration.append("<task:task xmlns=\"http://ws.apache.org/ns/synapse\" xmlns:task=\"http://www.wso2" +
                                 ".org/products/wso2commons/tasks\" name=\"");
        taskConfiguration.append(applicationName);
        taskConfiguration.append("Task\"");
        taskConfiguration.append(" class=\"org.apache.synapse.startup.tasks.TemplateMessageExecutor\"\n" +
                                 "        group=\"synapse.simple.quartz\">");

        taskConfiguration.append("<task:trigger count=\"");
        taskConfiguration.append(paramConfigsJSON.getJSONObject("schedule").get("count"));
        taskConfiguration.append("\" interval=\"");
        taskConfiguration.append(paramConfigsJSON.getJSONObject("schedule").get("interval"));
        taskConfiguration.append("\" />");

        taskConfiguration.append("<task:property xmlns:task=\"http://www.wso2.org/products/wso2commons/tasks\"\n" +
                                 "        name=\"templateParams\"><params>");

        JSONArray paramsArray = paramConfigsJSON.getJSONArray("params");

        for(int i = 0; i < paramsArray.length(); i ++) {
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
        taskConfiguration.append(paramConfigsJSON.get("template_name"));
        taskConfiguration.append("\"/></task:task>");

        stub.addTaskDescription(AXIOMUtil.stringToOM(taskConfiguration.toString()));
    }
    
}