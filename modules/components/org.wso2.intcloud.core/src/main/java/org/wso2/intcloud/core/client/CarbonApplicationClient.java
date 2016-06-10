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
import org.wso2.carbon.application.mgt.stub.ApplicationAdminExceptionException;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminStub;
import org.wso2.carbon.application.mgt.stub.upload.CarbonAppUploaderStub;
import org.wso2.carbon.application.mgt.stub.upload.types.carbon.UploadedFileItem;
import org.wso2.intcloud.common.IntCloudException;
import org.wso2.intcloud.common.util.IntCloudUtil;
import org.wso2.intcloud.services.tenant.carbonapps.stub.types.TenantCarbonAppAdminServiceRegistryExceptionException;
import org.wso2.intcloud.services.tenant.carbonapps.stub.types.TenantCarbonAppAdminServiceStub;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Calendar;

public class CarbonApplicationClient {

    private static Log log = LogFactory.getLog(CarbonApplicationClient.class);

    private static CarbonApplicationClient carbonApplicationClient = null;

    CarbonAppUploaderStub cAppUploaderStub = null;
    ApplicationAdminStub appAdminStub = null;
    TenantCarbonAppAdminServiceStub tenantCAppStub = null;

    int MAX_TIME = 200000;

    private CarbonApplicationClient() throws IntCloudException {
        try {
            cAppUploaderStub = new CarbonAppUploaderStub(IntCloudUtil.getPropertyValue("CarbonAppUploaderService"));

            appAdminStub = new ApplicationAdminStub(IntCloudUtil.getPropertyValue("ApplicationAdminService"));

            tenantCAppStub =
                    new TenantCarbonAppAdminServiceStub(IntCloudUtil.getPropertyValue("TenantCarbonAppAdminService"));

        } catch (AxisFault axisFault) {
            throw new IntCloudException(axisFault.getMessage(), axisFault);
        }
        ServiceClient clientCApp = cAppUploaderStub._getServiceClient();
        Options client_optionsCApp = clientCApp.getOptions();
        HttpTransportProperties.Authenticator authenticatorCApp = new HttpTransportProperties.Authenticator();
        authenticatorCApp.setUsername(IntCloudUtil.getPropertyValue("ESBServerUserName"));
        authenticatorCApp.setPassword(IntCloudUtil.getPropertyValue("ESBServerPassword"));
        authenticatorCApp.setPreemptiveAuthentication(true);
        client_optionsCApp.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, authenticatorCApp);
        clientCApp.setOptions(client_optionsCApp);

        ServiceClient clientAppAdmin = appAdminStub._getServiceClient();
        Options client_optionsAppAdmin = clientAppAdmin.getOptions();
        HttpTransportProperties.Authenticator authenticatorAppAdmin = new HttpTransportProperties.Authenticator();
        authenticatorCApp.setUsername(IntCloudUtil.getPropertyValue("ESBServerUserName"));
        authenticatorCApp.setPassword(IntCloudUtil.getPropertyValue("ESBServerPassword"));
        authenticatorAppAdmin.setPreemptiveAuthentication(true);
        client_optionsAppAdmin
                .setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, authenticatorAppAdmin);
        clientAppAdmin.setOptions(client_optionsAppAdmin);

        ServiceClient clientTenantCAppAdmin = tenantCAppStub._getServiceClient();
        Options client_optionsTenantCAppAdmin = clientTenantCAppAdmin.getOptions();
        HttpTransportProperties.Authenticator authenticatorTenantCAppAdmin =
                new HttpTransportProperties.Authenticator();
        authenticatorTenantCAppAdmin.setUsername(IntCloudUtil.getPropertyValue("ESBServerUserName"));
        authenticatorTenantCAppAdmin.setPassword(IntCloudUtil.getPropertyValue("ESBServerPassword"));
        authenticatorTenantCAppAdmin.setPreemptiveAuthentication(true);
        client_optionsTenantCAppAdmin
                .setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, authenticatorTenantCAppAdmin);
        clientTenantCAppAdmin.setOptions(client_optionsTenantCAppAdmin);
    }

    public static CarbonApplicationClient getInstance() throws IntCloudException {
        if (carbonApplicationClient == null) {
            synchronized (CarbonApplicationClient.class) {
                if (carbonApplicationClient == null) {
                    carbonApplicationClient = new CarbonApplicationClient();
                }
            }
        }
        return carbonApplicationClient;
    }

    public void deployCarbonApp(int tenantId, String carbonApplicationName, String carbonApplicationPath)
            throws IntCloudException {

        log.info("Deploying carbon application '" + carbonApplicationName + "' from '" + carbonApplicationPath +
                 "' to tenant " + tenantId);

        try {
            tenantCAppStub.uploadCarbonApplicationToRegistry(tenantId, carbonApplicationName, carbonApplicationPath);

            tenantCAppStub.deployCarbonApplication(tenantId, carbonApplicationName);

            tenantCAppStub.removeCarbonApplicationInRegistry(tenantId, carbonApplicationName);

        } catch (RemoteException | TenantCarbonAppAdminServiceRegistryExceptionException e) {
            throw new IntCloudException(e.getMessage(), e);
        }
    }

    public void deployCarbonApp(String carbonApplicationName, String carbonApplicationPath) throws IntCloudException {

        log.info("Deploying carbon application '" + carbonApplicationName + "' from '" + carbonApplicationPath);

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
            cAppUploaderStub.uploadApp(ii);
        } catch (RemoteException e) {
            throw new IntCloudException(e.getMessage(), e);
        }

        String cAppName = carbonApplicationName.substring(0, carbonApplicationName.length() - 4);

        log.info("Waiting " + MAX_TIME + " milliseconds for carbon application deployment : " + cAppName);
        boolean isCarFileDeployed = false;
        Calendar startTime = Calendar.getInstance();
        long time;
        while ((time = (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())) < MAX_TIME) {
            boolean carbonAppExists = isCarbonAppExists(carbonApplicationName);

            if (carbonAppExists) {
                log.info("Carbon application is deployed in " + time + " milliseconds");
                isCarFileDeployed = true;
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                //ignore
            }

        }

        if (isCarFileDeployed) {
            log.info("Carbon application deployed successfully");
        } else {
            log.warn("Carbon application deployment failed");
        }
    }

    public boolean isCarbonAppExists(String carbonApplicationName) throws IntCloudException {
        if (carbonApplicationName.endsWith(".car")) {
            carbonApplicationName = carbonApplicationName.substring(0, carbonApplicationName.length() - 4);
        }

        String[] applicationList;
        try {
            applicationList = appAdminStub.listAllApplications();
            log.info("Found applications " + applicationList);
        } catch (RemoteException | ApplicationAdminExceptionException e) {
            throw new IntCloudException("Error getting deployed application list from server", e);
        }
        if (applicationList != null) {
            if (Arrays.asList(applicationList).contains(carbonApplicationName)) {
                log.info("Carbon application " + carbonApplicationName + " exists");
                return true;
            }
        }
        return false;
    }

    public void unDeployCarbonApp(String carbonApplicationName) {
        String cAppName = carbonApplicationName.substring(0, carbonApplicationName.length() - 4);
        log.info("Un-deploying carbon application : " + cAppName);
        try {
            appAdminStub.deleteApplication(cAppName);
        } catch (Exception e) {
            log.error(e);
        }
    }
}
