package org.wso2.intcloud.core.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.wso2.carbon.mediation.templates.stub.types.TemplateAdminServiceStub;
import org.wso2.intcloud.common.IntCloudException;
import org.wso2.intcloud.common.util.IntCloudUtil;

public class SequenceTemplateClient {

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

    public String[] getAllSequenceTemplates(){
        return new String[] {"test", "test"};

    }
}
