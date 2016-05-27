package org.wso2.intcloud.core.client;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediation.templates.stub.types.TemplateAdminServiceStub;
import org.wso2.carbon.mediation.templates.stub.types.common.TemplateInfo;
import org.wso2.intcloud.common.IntCloudException;
import org.wso2.intcloud.common.util.IntCloudUtil;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SequenceTemplateClient {

    private static Log log = LogFactory.getLog(SequenceTemplateClient.class);

    private static SequenceTemplateClient sequenceTemplateClient = null;

    TemplateAdminServiceStub stub = null;

    private SequenceTemplateClient() throws IntCloudException {
        try {
            stub = new TemplateAdminServiceStub(IntCloudUtil.getPropertyValue("TemplateAdminService"));
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

    public static SequenceTemplateClient getInstance() throws IntCloudException {
        if (sequenceTemplateClient == null) {
            synchronized (SequenceTemplateClient.class) {
                if (sequenceTemplateClient == null) {
                    sequenceTemplateClient = new SequenceTemplateClient();
                }
            }
        }
        return sequenceTemplateClient;
    }

    public TemplateInfo[] getSequenceTemplates() throws RemoteException {
        TemplateInfo[] templateInfo = stub.getTemplates(0, 200);
        if (templateInfo == null || templateInfo.length == 0) {
            return null;
        }
        return templateInfo;
    }

    public String getSequenceTemplate(String carbonApplicationName) throws IntCloudException {
        carbonApplicationName = carbonApplicationName.substring(0, carbonApplicationName.lastIndexOf("_"));

        log.info("carbon application name = " + carbonApplicationName);

        StringBuilder jsonConfiguration = new StringBuilder();
        jsonConfiguration.append("{");

        try {
            sequenceTemplateClient = SequenceTemplateClient.getInstance();
            TemplateInfo[] templates = sequenceTemplateClient.getSequenceTemplates();

            //TODO : Assume only one template is available in a single capp
            if (templates != null) {
                for (TemplateInfo template : templates) {

                    String artifactContainerName = template.getArtifactContainerName();
                    if (artifactContainerName != null) {
                        artifactContainerName = artifactContainerName
                                .substring((artifactContainerName.lastIndexOf(":") + 2),
                                           (artifactContainerName.length() - 3));

                        if (artifactContainerName.equalsIgnoreCase(carbonApplicationName)) {
                            OMElement seqOMElement = sequenceTemplateClient.stub.getTemplate(template.getName());

                            AXIOMXPath parametersXpath = new AXIOMXPath("//ns:parameter/@name");
                            parametersXpath.addNamespace("ns", "http://ws.apache.org/ns/synapse");

                            List paramList = parametersXpath.selectNodes(seqOMElement);

                            if (paramList == null) {
                                log.info("No parameters found for the integration template");
                            } else {

                                List<String> paramNames = new ArrayList();

                                Iterator paramIterator = paramList.iterator();
                                while (paramIterator.hasNext()) {
                                    OMAttribute paramOM = (OMAttribute) paramIterator.next();
                                    paramNames.add(paramOM.getAttributeValue());
                                }

                                if (paramNames.size() > 0) {
                                    jsonConfiguration.append("\"param\":[");
                                    for (int i = 0; i < paramNames.size(); i++) {
                                        jsonConfiguration.append("{\"param\":\"");
                                        jsonConfiguration.append(paramNames.get(i));
                                        jsonConfiguration.append("\", \"value\":\"\"}");
                                        if (i < paramNames.size() - 1) {
                                            jsonConfiguration.append(",");
                                        } else {
                                            jsonConfiguration.append("],");
                                        }
                                    }
                                }
                            }
                            break;
                        }

                    }
                }

            }
        } catch (RemoteException | JaxenException e) {
            throw new IntCloudException(e.getMessage(), e);
        }

        jsonConfiguration.append("\"schedule\": {\"interval\" : \"\", \"count\" : \"\"}}");
        log.info("Setting json configuration : " + jsonConfiguration);
        return jsonConfiguration.toString();
    }
}