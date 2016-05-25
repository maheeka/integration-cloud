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
import org.apache.axis2.transport.http.HttpTransportProperties.Authenticator;
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

public class ESBClient {

    private static Log log = LogFactory.getLog(ESBClient.class);

    private static ESBClient esbClient = new ESBClient();

    private ESBClient() {
        log.debug("Initializing ESBClient");
        System.setProperty("javax.net.ssl.trustStore", IntCloudUtil.getPropertyValue("ESBKeyStorePath"));
        System.setProperty("javax.net.ssl.trustStorePassword", IntCloudUtil.getPropertyValue("ESBTrustStorePassword"));
    }

    public static ESBClient getInstance(){
        return esbClient;
    }

    public void deployCarbonApp(String carbonApplicationName, String carbonApplicationPath) throws IntCloudException {
        try {
            CarbonAppUploaderStub stub =
                    new CarbonAppUploaderStub(IntCloudUtil.getPropertyValue("CarbonAppUploaderService"));
            ServiceClient client = stub._getServiceClient();
            Options client_options = client.getOptions();
            Authenticator authenticator = new Authenticator();
            authenticator.setUsername(IntCloudUtil.getPropertyValue("ESBServerUserName"));
            authenticator.setPassword(IntCloudUtil.getPropertyValue("ESBServerPassword"));
            authenticator.setPreemptiveAuthentication(true);
            client_options.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, authenticator);
            client.setOptions(client_options);

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
                throw new IntCloudException(e.getMessage(),e);
            }






        } catch (AxisFault axisFault) {
            log.error("Error deploying carbon application", axisFault);
            throw new IntCloudException("Error deploying carbon application : " + axisFault.getMessage());

        }
    }




//
//    public static void main(String[] args) {
//        ESBClient ec = new ESBClient();
//        try {
//            ec.deployCarbonApp("salesforce_gmail_car_1.0.0.car","/Users/maheeka/ESB_WORK/CLOUD/APP_CLOUD/salesforce_gmail_car_1.0.0.car");
//        } catch (IntCloudException e) {
//            e.printStackTrace();
//        }
//    }

}
