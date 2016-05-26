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

public class DockerOpClientConstants {
    public static final String DOCKER_CLIENT_API_VERSION = "1.19";
    public static final int DOCKER_CLIENT_READ_TIMEOUT = 1000;
    public static final int DOCKER_CLIENT_TOTAL_CONNECTIONS = 1000;
    public static final int DOCKER_CLIENT_MAX_PER_ROUTE_CONNECTIONS = 10;
    public static final int DOCKER_CLIENT_CONNECTION_TIMEOUT = 1000;
    public static final String DEFAULT_DOCKER_URL = "http://localhost:2375";
    public static final String DEFAULT_DOCKER_REG_URL = "registry.docker.appfactory.private.wso2.com:5000";

    public static final String DOCKER_COMMAND_FROM = "FROM";
    public static final String DOCKER_COMMAND_COPY = "COPY";

    public static final String DOCKER_BASE_IMAGE_TOMCAT = "tomcat";
    public static final String DOCKER_BASE_IMAGE_JAXRS = "tomcat";
    public static final String DOCKER_BASE_IMAGE_MSF4J = "msf4j";
    public static final String DOCKER_BASE_IMAGE_UBUNTU = "ubuntu";
    public static final String DOCKER_BASE_IMAGE_WSO2AS = "wso2as";

    public static final String DOCKER_TOMCAT_VERSION = "8.0";
    public static final String DOCKER_MSF4J_VERSION = "1.0";
    public static final String DOCKER_UBUNTU_VERSION = "latest";

    public static final String DOCKER_WAR_LOCATION = "/usr/local/tomcat/webapps/";

    public static final String DOCKER_CONNECTION_TIMEOUT = "Docker.Connection.Timeout";
    public static final String DOCKER_REQUEST_TIMEOUT = "Docker.Request.Timeout";
    public static final String DOCKER_PUSH_TIMEOUT = "Docker.Push.Timeout";
    public static final String DOCKER_BUILLD_TIMEOUT = "Docker.Build.Timeout";
    public static final String DOCKER_SEARCH_TIMEOUT = "Docker.Search.Timeout";
}

