/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.intcloud.core.docker;

import org.wso2.intcloud.common.IntCloudException;
import org.wso2.appcloud.common.util.AppCloudUtil;
import org.wso2.intcloud.core.dao.ApplicationDAO;
import org.wso2.intcloud.core.dto.ApplicationRuntime;

public class DockerUtil {

    public static String getDockerRegistryUrl() {
        return AppCloudUtil.getPropertyValue("DockerRegistryURL");
    }

    public static String getDeploymentLocation(String appType) {
        return DockerOpClientConstants.DOCKER_WAR_LOCATION;
    }

    public static String getDockerFileTemplatePath(String runtimeId, String dockerTemplateFilePath,
            String dockerFileCategory) throws IntCloudException {
        ApplicationDAO applicationDAO = new ApplicationDAO();
        ApplicationRuntime applicationRuntime = applicationDAO.getRuntimeById(Integer.parseInt(runtimeId));
        String dockerBaseImageName = applicationRuntime.getImageName();
        String dockerBaseImageVersion = applicationRuntime.getTag();

        return dockerTemplateFilePath + "/" + dockerBaseImageName + "/" + dockerFileCategory + "/Dockerfile" + "."
                + dockerBaseImageName + "." + dockerBaseImageVersion;
    }
}
