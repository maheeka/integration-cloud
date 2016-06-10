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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Utilities for the Service Activation Module for Tenants.
 */
public class Util {

    private static RegistryService registryService = null;
    private static RealmService realmService = null;
    private static BundleContext bundleContext= null;

    private static final Log log = LogFactory.getLog(Util.class);

    /**
     * Stores an instance of the Registry Service that can be used to access the registry.
     *
     * @param service the Registry Service instance.
     */
    public static synchronized void setRegistryService(RegistryService service) {
        registryService = service;
    }

    /**
     * Method to retrieve the Registry Service instance.
     *
     * @return the Registry Service instance if it has been stored or null if not.
     */
    @SuppressWarnings("unused")
    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static synchronized void setRealmService(RealmService service) {
        realmService = service;
    }

    public static synchronized RealmService getRealmService() {
        return realmService;
    }

    public static void setBundleContext(BundleContext context) {
        Util.bundleContext = context;
    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }
}
