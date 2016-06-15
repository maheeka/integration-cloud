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

package org.wso2.intcloud.services.tenant.carbonapps.deployer;

import org.apache.axis2.AxisFault;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.application.upload.CarbonAppUploader;
import org.wso2.carbon.application.upload.UploadedFileItem;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;

public class TenantCarbonAppDeployer extends CarbonAppUploader {

    private static final Log log = LogFactory.getLog(TenantCarbonAppDeployer.class);

    /**
     * Fetches a sample from the registry and deploys it as a CarbonApp
     *
     * @param tenantId              tenant id
     * @param carbonApplicationName Name of the carbon application
     * @param carbonApplicationPath path to carbon application
     * @return true if the operation successfully completed.
     * @throws AxisFault Thrown if and error occurs while uploading the sample
     */
    public boolean deployCarbonApplication(int tenantId, String carbonApplicationName, String carbonApplicationPath)
            throws AxisFault {
        try {

            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);

            File file = new File(carbonApplicationPath);

            byte[] byteArray;
            byteArray = FileUtils.readFileToByteArray(file);
            DataHandler dataHandler = new javax.activation.DataHandler(byteArray, "application/octet-stream");

            UploadedFileItem i = new UploadedFileItem();
            i.setDataHandler(dataHandler);
            i.setFileName(carbonApplicationName);
            i.setFileType("jar");

            UploadedFileItem[] ii = new UploadedFileItem[1];
            ii[0] = i;

            uploadApp(ii);
            return true;

        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
