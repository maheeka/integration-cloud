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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediation.templates.stub.types.TemplateAdminServiceStub;
import org.wso2.intcloud.common.IntCloudException;
import org.wso2.intcloud.common.util.IntCloudUtil;
import org.wso2.intcloud.services.tenant.templates.common.TemplateInfo;
import org.wso2.intcloud.services.tenant.templates.stub.types.TenantTemplateAdminServiceStub;

import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//import org.wso2.carbon.mediation.templates.stub.types.TemplateAdminServiceStub;
//import org.wso2.carbon.mediation.templates.stub.types.common.TemplateInfo;

public class SequenceTemplateClient {

    public static final String TEMPLATE_NAME_XPATH = "//ns:template/@name";
    public static final String PARAMETER_NAME_XPATH = "//ns:parameter/@name";
    public static final String NS_SYNAPSE = "http://ws.apache.org/ns/synapse";
    public static final String PREFIX = "ns";

    private static Log log = LogFactory.getLog(SequenceTemplateClient.class);

    private static SequenceTemplateClient sequenceTemplateClient = null;

    private TemplateAdminServiceStub templateAdminServiceStub = null;

    private TenantTemplateAdminServiceStub tenantTemplateAdminServiceStub = null;

    private SequenceTemplateClient() throws IntCloudException {
                try {
                    templateAdminServiceStub = new TemplateAdminServiceStub(IntCloudUtil.getPropertyValue
         ("TemplateAdminService"));
                } catch (AxisFault axisFault) {
                    throw new IntCloudException(axisFault.getMessage(), axisFault);
                }
                ServiceClient client = templateAdminServiceStub._getServiceClient();
                Options client_options = client.getOptions();
                HttpTransportProperties.Authenticator authenticator = new HttpTransportProperties.Authenticator();
                authenticator.setUsername(IntCloudUtil.getPropertyValue("ESBServerUserName"));
                authenticator.setPassword(IntCloudUtil.getPropertyValue("ESBServerPassword"));
                authenticator.setPreemptiveAuthentication(true);
                client_options.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, authenticator);
                client.setOptions(client_options);

        try {
            tenantTemplateAdminServiceStub =
                    new TenantTemplateAdminServiceStub(IntCloudUtil.getPropertyValue("TenantTemplateAdminService"));
        } catch (AxisFault axisFault) {
            throw new IntCloudException(axisFault.getMessage(), axisFault);
        }
        ServiceClient tenantClient = tenantTemplateAdminServiceStub._getServiceClient();
        Options tenant_client_options = tenantClient.getOptions();
        HttpTransportProperties.Authenticator tenant_authenticator = new HttpTransportProperties.Authenticator();
        tenant_authenticator.setUsername(IntCloudUtil.getPropertyValue("ESBServerUserName"));
        tenant_authenticator.setPassword(IntCloudUtil.getPropertyValue("ESBServerPassword"));
        tenant_authenticator.setPreemptiveAuthentication(true);
        tenant_client_options
                .setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, tenant_authenticator);
        tenantClient.setOptions(tenant_client_options);
    }

    public static SequenceTemplateClient getInstance() throws IntCloudException {
        if (sequenceTemplateClient == null) {
            synchronized (SequenceTemplateClient.class) {
                if (sequenceTemplateClient == null) {
                    log.info("initializing sequence template client");
                    sequenceTemplateClient = new SequenceTemplateClient();
                }
            }
        }
        log.info("getting sequence template instance");
        return sequenceTemplateClient;
    }


    public org.wso2.carbon.mediation.templates.stub.types.common.TemplateInfo[] getSequenceTemplates() throws RemoteException {
        org.wso2.carbon.mediation.templates.stub.types.common.TemplateInfo[]
                templateInfo = templateAdminServiceStub.getTemplates(0, 200);
        if (templateInfo == null || templateInfo.length == 0) {
            return null;
        }
        return templateInfo;
    }

    public String getSequenceTemplate(String carbonApplicationName) throws IntCloudException {
        carbonApplicationName = carbonApplicationName.substring(0, carbonApplicationName.lastIndexOf("_"));

        log.info("Getting integration template from carbon application name = " + carbonApplicationName);

        StringBuilder jsonConfiguration = new StringBuilder();
        jsonConfiguration.append("{");

        try {
            org.wso2.carbon.mediation.templates.stub.types.common.TemplateInfo[] templates = getSequenceTemplates();

            //TODO : Assume only one template is available in a single capp
            if (templates != null) {
                for (org.wso2.carbon.mediation.templates.stub.types.common.TemplateInfo template : templates) {

                    log.info("Integration template found : " + template.getName());

                    String artifactContainerName = template.getArtifactContainerName();
                    if (artifactContainerName != null) {
                        artifactContainerName = artifactContainerName
                                .substring((artifactContainerName.lastIndexOf(":") + 2),
                                           (artifactContainerName.length() - 3));

                        if (artifactContainerName.equalsIgnoreCase(carbonApplicationName)) {
                            OMElement seqOMElement = sequenceTemplateClient.templateAdminServiceStub.getTemplate
                            (template.getName());

                            log.debug("Found integration template : " + seqOMElement);

                            AXIOMXPath templateNameXpath = new AXIOMXPath("//ns:template/@name");
                            templateNameXpath.addNamespace("ns", "http://ws.apache.org/ns/synapse");

                            OMAttribute templateName = (OMAttribute) templateNameXpath.selectSingleNode(seqOMElement);

                            jsonConfiguration
                                    .append("\"template_name\" : \"" + templateName.getAttributeValue() + "\",");

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
                                    jsonConfiguration.append("\"params\":[");
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

            } else {
                log.info("Integration templates not found");
            }
        } catch (RemoteException | JaxenException e) {
            throw new IntCloudException(e.getMessage(), e);
        }

        jsonConfiguration.append("\"schedule\": {\"interval\" : \"\", \"count\" : \"\"}}");
        log.info("Setting json configuration : " + jsonConfiguration);
        return jsonConfiguration.toString();
    }

    public String getSequenceTemplate(int tenantId, String carbonApplicationName) throws IntCloudException {

        carbonApplicationName = carbonApplicationName.substring(0, carbonApplicationName.lastIndexOf("_"));

        log.info("Getting integration template from carbon application name = " + carbonApplicationName +
                 " for tenant " + tenantId);

        StringBuilder jsonConfiguration = new StringBuilder();
        jsonConfiguration.append("{");

        try {
            TemplateInfo[] templates = getSequenceTemplates(tenantId);

            //TODO : Assume only one template is available in a single capp
            if (templates != null) {
                for (TemplateInfo template : templates) {

                    log.info("Integration template found : " + template.getName());

                    String artifactContainerName = template.getArtifactContainerName();
                    if (artifactContainerName != null) {
                        artifactContainerName = artifactContainerName
                                .substring((artifactContainerName.lastIndexOf(":") + 2),
                                           (artifactContainerName.length() - 3));

                        if (artifactContainerName.equalsIgnoreCase(carbonApplicationName)) {
                            OMElement seqOMElement = AXIOMUtil.stringToOM(
                                    tenantTemplateAdminServiceStub.getTemplateInTenant(tenantId, template.getName()));

                            log.debug("Found integration template : " + seqOMElement);

                            AXIOMXPath templateNameXpath = new AXIOMXPath(TEMPLATE_NAME_XPATH);
                            templateNameXpath.addNamespace(PREFIX, NS_SYNAPSE);

                            OMAttribute templateName = (OMAttribute) templateNameXpath.selectSingleNode(seqOMElement);

                            jsonConfiguration
                                    .append("\"template_name\" : \"" + templateName.getAttributeValue() + "\",");

                            AXIOMXPath parametersXpath = new AXIOMXPath(PARAMETER_NAME_XPATH);
                            parametersXpath.addNamespace(PREFIX, NS_SYNAPSE);

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
                                    jsonConfiguration.append("\"params\":[");
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

            } else {
                log.info("Integration templates not found");
            }
        } catch (RemoteException | JaxenException | XMLStreamException e) {
            throw new IntCloudException(e.getMessage(), e);
        }

        jsonConfiguration.append("\"schedule\": {\"interval\" : \"\", \"count\" : \"\"}}");
        log.info("Setting json configuration : " + jsonConfiguration);
        return jsonConfiguration.toString();
    }

    public TemplateInfo[] getSequenceTemplates(int tenantId) throws RemoteException {
        TemplateInfo[] templateInfo = tenantTemplateAdminServiceStub.getTemplatesInTenant(tenantId, 0, 200);
        if (templateInfo == null || templateInfo.length == 0) {
            return null;
        }
        return templateInfo;
    }
}