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

package org.wso2.intcloud.common.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.intcloud.common.IntCloudConstant;
import org.wso2.intcloud.common.IntCloudException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class is responsible for keeping utils method which needs for other modules.
 */
public class IntCloudUtil {

    private static Properties properties = new Properties();
    private static final Log log = LogFactory.getLog(IntCloudUtil.class);

    static {
        try {
            loadIntCloudConfig();
        } catch (IntCloudException e) {
            String message = "Unable to load intcloud configuration file";
            log.error(message, e);
        }
    }

    /**
     * Load key and value from the AppCloud.property file.
     *
     * @throws IntCloudException
     */
    private static void loadIntCloudConfig() throws IntCloudException {
        String fileLocation = new StringBuilder().append(CarbonUtils.getCarbonConfigDirPath()).append(File.separator)
                                                 .append(IntCloudConstant.CONFIG_FOLDER).append(File.separator)
                                                 .append(IntCloudConstant.CONFIG_FILE_NAME).toString();
        File file = new File(fileLocation);
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            properties.load(inputStream);
        } catch (FileNotFoundException e) {
            String message =
                    "The " + IntCloudConstant.CONFIG_FILE_NAME + " file not found from file location: " + fileLocation;
            throw new IntCloudException(message, e);
        } catch (IOException e) {
            String message =
                    "Unable to read " + IntCloudConstant.CONFIG_FILE_NAME + " file from file location: " + fileLocation;
            throw new IntCloudException(message, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String message = "Unable to close file input stream while reading intcloud configuration";
                log.error(message, e);
            }
        }
    }

    /**
     * Get a value from given property.
     *
     * @param property
     * @return
     */
    public static String getPropertyValue(String property) {
        String value = properties.getProperty(property);
        if (value == null) {
            String message =
                    "The given property: " + property + " is not found from the " + IntCloudConstant.CONFIG_FILE_NAME +
                    " file";
            log.warn(message);
        }
        return value;
    }

}
