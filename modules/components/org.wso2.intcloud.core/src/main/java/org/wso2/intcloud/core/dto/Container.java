/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.intcloud.core.dto;

import java.util.List;
import java.util.Set;

public class Container {
    private int id;
    private String imageName;
    private String imageVersion;
    private Set<ContainerServiceProxy> serviceProxies;
    private List<RuntimeProperty> runtimeProperties;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageVersion() {
        return imageVersion;
    }

    public void setImageVersion(String imageVersion) {
        this.imageVersion = imageVersion;
    }

    public Set<ContainerServiceProxy> getServiceProxies() {
        return serviceProxies;
    }

    public void setServiceProxies(Set<ContainerServiceProxy> serviceProxies) {
        this.serviceProxies = serviceProxies;
    }

    public void setRuntimeProperties(List<RuntimeProperty> runtimeProperties) {
        this.runtimeProperties = runtimeProperties;
    }

    public List<RuntimeProperty> getRuntimeProperties() {
        return runtimeProperties;
    }
}
