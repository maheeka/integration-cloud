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

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.application.upload.CarbonAppUploader;
import org.wso2.carbon.application.upload.UploadedFileItem;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryClientUtils;

import javax.activation.DataHandler;
import java.io.File;

public class TenantCarbonAppDeployer extends CarbonAppUploader {

    private static final Log log = LogFactory.getLog(TenantCarbonAppDeployer.class);

    private static final String REGISTRY_CAPP_LOCATION = "/cAppTemps/";

    /**
     * Fetches a sample from the registry and deploys it as a CarbonApp
     *
     * @param tenantId tenant id
     * @param carbonApplicationName Name of the carbon application
     * @return true if the operation successfully completed.
     * @throws AxisFault         Thrown if and error occurs while uploading the sample
     * @throws RegistryException Thrown if an error occurs while accessing the Registry
     */
    public boolean deployCarbonApplication(int tenantId, String carbonApplicationName, String carbonApplicationPath)
            throws AxisFault, RegistryException {
        try {

            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);


            //upload to registry
            Registry registry = (Registry) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                                                  .getRegistry(RegistryType.SYSTEM_GOVERNANCE);
            String carbonAppPath = getCarbonAppPath(carbonApplicationName);

            if (registry.resourceExists(carbonAppPath)) {
                log.info("Carbon application already exists in " + carbonAppPath);
                return true;
            }

            File carbonApplicationFile = new File(carbonApplicationPath);
            RegistryClientUtils.importToRegistry(carbonApplicationFile, REGISTRY_CAPP_LOCATION, registry);


            //deploy carbon app
            Resource sampleFile = (Resource) registry.get(carbonAppPath);
            UploadedFileItem[] fileItems = new UploadedFileItem[1];
            fileItems[0] = new UploadedFileItem();
            fileItems[0].setDataHandler(new DataHandler(
                    new ByteArrayDataSource((byte[]) sampleFile.getContent(), "application/octet-stream")));
            fileItems[0].setFileName(carbonApplicationName);
            fileItems[0].setFileType("jar");

            uploadApp(fileItems);


            //delete from registry
            if (registry.resourceExists(carbonAppPath)) {
                log.info("Carbon application exists in " + carbonAppPath + " . deleting ..");
                registry.delete(carbonAppPath);
            }

            return true;

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Upload a sample from the file system to the registry
     *
     * @param tenantId              tenant id
     * @param carbonApplicationName The name of the sample file to be uploaded to the registry
     * @param carbonApplicationPath path to upload carbon application from file system
     * @return true if the operation successfully completed.
     * @throws RegistryException Thrown if an error occurs while accessing the Registry
     */
    private boolean uploadCarbonApplicationToRegistry(int tenantId, String carbonApplicationName,
                                                     String carbonApplicationPath) throws RegistryException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);

            Registry registry = (Registry) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                                                  .getRegistry(RegistryType.SYSTEM_GOVERNANCE);
            String carbonAppPath = getCarbonAppPath(carbonApplicationName);
            if (registry.resourceExists(carbonAppPath)) {
                log.info("Carbon application already exists in " + carbonAppPath);
                return true;
            }
            File carbonApplicationFile = new File(carbonApplicationPath);
            RegistryClientUtils.importToRegistry(carbonApplicationFile, REGISTRY_CAPP_LOCATION, registry);
        } finally {
            // Ultimately cleanup the tenant information before exiting the thread.
            PrivilegedCarbonContext.endTenantFlow();
        }
        return true;
    }

    private String getCarbonAppPath(String carbonApplicationName) {
        return REGISTRY_CAPP_LOCATION + carbonApplicationName;
    }

    private boolean removeCarbonApplicationInRegistry(int tenantId, String carbonApplicationName)
            throws RegistryException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);

            Registry registry = (Registry) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                                                  .getRegistry(RegistryType.SYSTEM_GOVERNANCE);
            String carbonAppPath = getCarbonAppPath(carbonApplicationName);
            if (registry.resourceExists(carbonAppPath)) {
                log.info("Carbon application exists in " + carbonAppPath + " . deleting ..");
                registry.delete(carbonAppPath);
                return true;
            }
        } finally {
            // Ultimately cleanup the tenant information before exiting the thread.
            PrivilegedCarbonContext.endTenantFlow();
        }
        return true;
    }

}
