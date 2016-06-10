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
package org.wso2.intcloud.services.tenant.carbonapps.deployer.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.intcloud.services.tenant.carbonapps.deployer.Util;

/**
 * The Declarative Service Component Carbon App Deployer in Tenant Mode
 *
 * @scr.component name="org.wso2.intcloud.services.tenant.carbonapps.deployer" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 */
@SuppressWarnings({ "JavaDoc", "unused" })
public class TenantCarbonAppDeployerServiceComponent {

    private static final Log log = LogFactory.getLog(TenantCarbonAppDeployerServiceComponent.class);

    /**
     * Activates the Registry Kernel bundle.
     *
     * @param context the OSGi component context.
     */
    protected void activate(ComponentContext context) {
        try {
            Util.setBundleContext(context.getBundleContext());
            log.info("Tenant Carbon Application Uploader Service bundle activated");
        } catch (Exception e) {
            log.error("Tenant Carbon Application Uploader Service bundle failed to activate", e);
        }
    }

    /**
     * Deactivates the Registry Kernel bundle.
     *
     * @param context the OSGi component context.
     */
    protected void deactivate(ComponentContext context) {
        Util.setBundleContext(null);
        log.info("Tenant Carbon Application Uploader Service bundle de-activated");
    }

    /**
     * Method to set the registry service used. This will be used when accessing the registry. This
     * method is called when the OSGi Registry Service is available.
     *
     * @param registryService the registry service.
     */
    protected void setRegistryService(RegistryService registryService) {
        Util.setRegistryService(registryService);
    }

    /**
     * This method is called when the current registry service becomes un-available.
     *
     * @param registryService the current registry service instance, to be used for any
     *                        cleaning-up.
     */
    protected void unsetRegistryService(RegistryService registryService) {
        Util.setRegistryService(null);
    }

    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Tenant Carbon Application Uploader Service : Setting the Realm Service");
        }
        Util.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        Util.setRealmService(null);
    }

}
