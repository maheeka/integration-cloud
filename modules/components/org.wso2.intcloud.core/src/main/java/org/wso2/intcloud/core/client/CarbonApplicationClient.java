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

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.application.mgt.stub.upload.CarbonAppUploaderStub;
import org.wso2.carbon.application.mgt.stub.upload.types.carbon.UploadedFileItem;
import org.wso2.intcloud.common.IntCloudException;
import org.wso2.intcloud.common.util.IntCloudUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

public class CarbonApplicationClient {

    private static CarbonApplicationClient carbonApplicationClient = null;

    CarbonAppUploaderStub stub = null;

    private CarbonApplicationClient() throws IntCloudException {
        try {
            stub = new CarbonAppUploaderStub(IntCloudUtil.getPropertyValue("CarbonAppUploaderService"));
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

    public static CarbonApplicationClient getInstance() throws IntCloudException {
        if (carbonApplicationClient == null) {
            synchronized (CarbonApplicationClient.class){
                if (carbonApplicationClient == null) {
                    carbonApplicationClient = new CarbonApplicationClient();
                }
            }
        }
        return carbonApplicationClient;
    }

    public void deployCarbonApp(String carbonApplicationName, String carbonApplicationPath) throws IntCloudException {

        File file = new File(carbonApplicationPath);

        byte[] byteArray;
        try {
            byteArray = FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        DataHandler dataHandler = new javax.activation.DataHandler(byteArray, "application/octet-stream");

        UploadedFileItem i = new UploadedFileItem();
        i.setDataHandler(dataHandler);
        i.setFileName(carbonApplicationName);
        i.setFileType("jar");

        UploadedFileItem[] ii = new UploadedFileItem[1];
        ii[0] = i;
        try {
            stub.uploadApp(ii);
        } catch (RemoteException e) {
            throw new IntCloudException(e.getMessage(), e);
        }
    }
}
