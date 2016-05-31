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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.UUID;

public class ScheduledTaskClient {

    private static Log log = LogFactory.getLog(ScheduledTaskClient.class);

    private static ScheduledTaskClient scheduledTaskClient = null;

    TaskAdminStub stub = null;

    private ScheduledTaskClient() throws IntCloudException {

        try {
            stub = new TaskAdminStub(IntCloudUtil.getPropertyValue("TaskAdminService"));
        } catch (AxisFault axisFault) {
            throw new IntCloudException(axisFault.getMessage(), axisFault);
        }
        ServiceClient client = stub._getServiceClient();
        Options client_options = client.getOptions();
        HttpTransportProperties.Authenticator authenticator = new HttpTransportProperties.Authenticator();
        authenticator.setUsername(IntCloudUtil.getPropertyValue("ESBServerUserName"));
        authenticator.setPassword(IntCloudUtil.getPropertyValue("ESBServerPassword"));
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

    public OMElement addTask(String applicationName, JSONObject paramConfigurationJSON)
            throws XMLStreamException, IOException, TaskManagementException {

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

        String taskConfigurationStr = taskConfiguration.toString();

        log.info("Deploying task configuration : " + taskConfigurationStr);

        stub.addTaskDescription(AXIOMUtil.stringToOM(taskConfigurationStr));

        return AXIOMUtil.stringToOM(taskConfigurationStr);
    }

    public void addTestTask(String applicationName, JSONObject paramConfigurationJSON)
            throws TaskManagementException, XMLStreamException, IOException, InterruptedException {

        String randomApplicationName = applicationName + UUID.randomUUID();

        paramConfigurationJSON.put("count", "1");
        paramConfigurationJSON.put("interval", "1");

        OMElement task = addTask(randomApplicationName, paramConfigurationJSON);

        String taskName = task.getAttributeValue(new QName("name"));
        String taskGroup = task.getAttributeValue(new QName("group"));

        deleteTask(taskName, taskGroup);

    }

    public void deleteTask(String name, String group) throws TaskManagementException, RemoteException {
        log.info("Deleting task " + name);
        stub.deleteTaskDescription(name, group);
    }
}