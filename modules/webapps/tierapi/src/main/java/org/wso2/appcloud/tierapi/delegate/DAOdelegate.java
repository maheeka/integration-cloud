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

package org.wso2.appcloud.tierapi.delegate;

import org.wso2.appcloud.tierapi.dao.ContainerSpecsDao;
import org.wso2.appcloud.tierapi.dao.PlanDao;
import org.wso2.appcloud.tierapi.dao.impl.ContainerSpecDaoImpl;
import org.wso2.appcloud.tierapi.dao.impl.PlanDaoImpl;

public class DAOdelegate {

	private static PlanDao planInstance = new PlanDaoImpl();
	private static ContainerSpecsDao containerSpecInstance = new ContainerSpecDaoImpl();

	/**
	 * Get PlanDAO object.
	 */
	public static PlanDao getPlanInstance() {
		return planInstance;
	}

	/**
	 * Get PlanContainerSpec object.
	 */
	public static ContainerSpecsDao getContainerSpecInstance() {
		return containerSpecInstance;
	}

}
