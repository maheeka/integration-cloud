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

package org.wso2.appcloud.tierapi.dao;

import org.wso2.appcloud.tierapi.bean.ContainerSpecifications;
import org.wso2.appcloud.tierapi.bean.Plan;

import javax.xml.bind.annotation.XmlRootElement;
import java.sql.SQLException;
import java.util.List;


@XmlRootElement
public interface PlanDao {

	/*
	 * Get all defined subscription plans.
	 */
	public List<Plan> getAllPlans() throws SQLException;

	/*
	 * Get plan by ID.
	 */
	public Plan getPlanByPlanId(int planId) throws SQLException;

	/*
	 * Define new Plan.
	 */
	public Plan definePlan(Plan plan) throws SQLException;

	/*
	 * Delete Plan by ID.
	 */
	public boolean deletePlanById (int planId) throws SQLException;

	/*
	 * Update Plan by ID.
	 */
	public Plan updatePlanById (int planId, Plan plan) throws SQLException;

	/*
	 * Get the allowed container specifications within the subscription plan.
	 */
	public List<ContainerSpecifications> getAllowedConSpecs(int planId) throws SQLException ;


}