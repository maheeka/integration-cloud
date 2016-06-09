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
package org.wso2.intcloud.services.tenantcappuploader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ArchiveManipulator;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Utilities for the Service Activation Module for Tenants.
 */
public class Util {

    private static RegistryService registryService = null;
    private static RealmService realmService = null;
    private static BundleContext bundleContext= null;

    private static final String CONFIG_FILENAME = "cloud-services-desc.xml";
    private static final String SAMPLES_FILENAME = "samples-desc.xml";
    private static final String TENANT_ID_KEY = "${tenant.id}";
    private static final String TENANT_DOMAIN_KEY = "${tenant.domain}";
    private static final String USER_NAME = "${user.name}";
    private static final String PROPERTIES_FILE_EXTENSION = ".properties";
    private static final String TENANT_APP_TEMP_DIRECTORY_PATH = "cAppExtractionTemp";
    private static final String TENANT_APP_TEMP_FILE_PATH = "cAppExtractionFile";

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

    private static UserRegistry getSuperTenantGovernanceSystemRegistry() throws RegistryException {
        return registryService.getGovernanceSystemRegistry();
    }



    /**
     * Generate a temporary application archive for a tenant that can be uploaded. This method will
     * deduce the name of the property file used in
     * {@link #generateAppArchiveForTenant(String, HttpSession, String)}, using the name of
     * the source file. It expects a property file with an extension ".properties" having the same
     * name as the source file, to be present at the same location where the source file is found.
     *
     * @param source  the input path
     * @param session the tenant's HTTP Session
     *
     * @return the path of the temporary application archive;
     * @throws IOException if the operation failed.
     */
    public static String generateAppArchiveForTenant(String source, HttpSession session)
            throws IOException {
        return generateAppArchiveForTenant(source, session, null);
    }

    /**
     * Generate a temporary application archive for a tenant that can be uploaded.
     *
     * @param source       the input path
     * @param session      the tenant's HTTP Session
     * @param propertyFile the property file to be used for replacements.
     *
     * @return the path of the temporary application archive;
     * @throws IOException if the operation failed.
     */
    public static String generateAppArchiveForTenant(String source, HttpSession session,
                                                     String propertyFile)
            throws IOException {
        PrivilegedCarbonContext carbonContext =
                PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        if (tenantId <= 0) {
            return source;
        }
        String tenantDomain = carbonContext.getTenantDomain(true);
        String username = carbonContext.getUsername();
        File tempDir = File.createTempFile(TENANT_APP_TEMP_DIRECTORY_PATH, "");
        File tempFile = File.createTempFile(TENANT_APP_TEMP_FILE_PATH, "");
        String dir = tempDir.getAbsolutePath();
        String destination = tempFile.getAbsolutePath() +
                source.substring(source.lastIndexOf(File.separator));
        try {
            if (!tempDir.delete() || !tempDir.mkdir()) {
                tempDir = null;
                throw new IllegalStateException("Unable to create temporary directory");
            }
            if (!tempFile.delete() || !tempFile.mkdir()) {
                throw new IllegalStateException("Unable to create temporary directory");
            }
            ArchiveManipulator am = new ArchiveManipulator();
            am.extract(source, dir);
            Properties properties;
            try {
                if (propertyFile == null) {
                    propertyFile = source.substring(0, source.lastIndexOf(".")) +
                            PROPERTIES_FILE_EXTENSION;
                }
                properties = loadProperties(propertyFile);
                if (properties.size() > 0) {
                    for (String str : am.check(source)) {
                        try {
                            String filePath = dir + File.separator + str;
                            File file = new File(filePath);
                            if (file.isFile()) {
                                List<String> archiveTypes = Arrays.asList("jar", "aar", "war",
                                        "dar", "mar", "gar", "zip");
                                List<String> textTypes = Arrays.asList("xml", "dbs", "xslt",
                                        "properties", "service", "js", "jsp", "css", "txt", "wsdl",
                                        "bpel");
                                if (FilenameUtils.isExtension(filePath, archiveTypes)) {
                                    File modified = new File(generateAppArchiveForTenant(filePath,
                                            session, propertyFile));
                                    FileUtils.copyFile(modified, file);
                                    FileUtils.deleteDirectory(modified.getParentFile());
                                } else if (FilenameUtils.isExtension(filePath, textTypes)) {
                                    String line;
                                    StringBuffer oldText = new StringBuffer();
                                    BufferedReader reader = new BufferedReader(
                                            new FileReader(file));
                                    FileWriter writer = null;
                                    try {
                                        while ((line = reader.readLine()) != null) {
                                            oldText.append(line).append("\n");
                                        }
                                        String newText = oldText.toString();
                                        for (String key : properties.stringPropertyNames()) {
                                            String value = properties.getProperty(key)
                                                    .replace(TENANT_ID_KEY,
                                                            Integer.toString(tenantId))
                                                    .replace(TENANT_DOMAIN_KEY, tenantDomain)
                                                    .replace(USER_NAME, username);
                                            newText = newText.replace(key, value);
                                        }
                                        writer = new FileWriter(file);
                                        writer.write(newText);
                                    } finally {
                                        try {
                                            reader.close();
                                        } finally {
                                            if (writer != null) {
                                                writer.close();
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (IOException e) {
                            log.warn("An error occurred while making replacements in a file in: " +
                                    source, e);
                        }
                    }
                }
            } catch (IOException e) {
                log.warn("An error occurred while reading properties file for: " + source, e);
            }
            am.archiveDir(destination, dir);
        } finally {
            if (tempDir != null) {
                FileUtils.deleteDirectory(tempDir);
            }
        }
        return destination;
    }

    // loads a set of properties from the given file path.
    private static Properties loadProperties(String propertyFileName) throws IOException {
        Properties properties = new Properties();
        File props = new File(propertyFileName);
        if (props.exists()) {
            FileInputStream fileIn = null;
            try {
                fileIn = new FileInputStream(props);
                properties.load(fileIn);
            } finally {
                if (fileIn != null) {
                    fileIn.close();
                }
            }
        }
        return properties;
    }




    public static void setBundleContext(BundleContext context) {
        Util.bundleContext = context;
    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }
}
