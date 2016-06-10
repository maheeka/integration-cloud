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

package org.wso2.appcloud.tierapi.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.appcloud.tierapi.bean.ContainerSpecifications;
import org.wso2.appcloud.tierapi.bean.Plan;
import org.wso2.appcloud.tierapi.dao.PlanDao;
import org.wso2.appcloud.tierapi.util.DBConfiguration;

import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@XmlRootElement
public class PlanDaoImpl implements PlanDao{

	private static final Log log = LogFactory.getLog(PlanDaoImpl.class);

	@Override
	public List<Plan> getAllPlans() throws SQLException{
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;

		List<Plan> plans = new ArrayList<Plan>();
		String sql = "select * from AC_SUBSCRIPTION_PLANS";

		try {
			DBConfiguration dbCon = new DBConfiguration();
			dbConnection = dbCon.getConnection();


			preparedStatement = dbConnection.prepareStatement(sql);
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {
				Plan plan = new Plan();
				plan.setId(rs.getInt("PLAN_ID"));
				plan.setPlanName(rs.getString("PLAN_NAME"));
				plan.setMaxApplications(rs.getInt("MAX_APPLICATIONS"));

				plans.add(plan);
			}
		} catch (SQLException e) {
			String msg = "Error while getting details of Plans";
			log.error(msg, e);
			throw e;
		} finally {

			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return plans;
	}

	@Override
	public Plan getPlanByPlanId(int planId) throws SQLException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		Logger log = Logger.getLogger(PlanDaoImpl.class.getName());
		Plan plan = new Plan();
		String sql = "select * from AC_SUBSCRIPTION_PLANS WHERE PLAN_ID ="+planId;
		try {
			DBConfiguration dbCon = new DBConfiguration();
			dbConnection = dbCon.getConnection();
			preparedStatement = dbConnection.prepareStatement(sql);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				plan.setId(rs.getInt("PLAN_ID"));
				plan.setPlanName(rs.getString("PLAN_NAME"));
				plan.setMaxApplications(rs.getInt("MAX_APPLICATIONS"));
			}
			rs.close();
		} catch (SQLException e) {
			String msg = "Error while getting details of Plan with ID "+planId;
			throw e;
		} finally {

			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return plan;
	}

	@Override
	public Plan definePlan(Plan plan) throws SQLException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;

		String sql = "INSERT INTO AC_SUBSCRIPTION_PLANS (PLAN_NAME, TEAM, MAX_INSTANCES) VALUES (?, ?)";

		try {
			DBConfiguration dbCon = new DBConfiguration();
			dbConnection = dbCon.getConnection();
			preparedStatement = dbConnection.prepareStatement(sql);
			preparedStatement.setString(1, plan.getPlanName());
			preparedStatement.setInt(2, plan.getMaxApplications());

			preparedStatement.executeUpdate();
			preparedStatement.close();

			String sql2 = "select * from AC_SUBSCRIPTION_PLANS WHERE PLAN_NAME= ?";
			preparedStatement= dbConnection.prepareStatement(sql2);
			preparedStatement.setString(1, plan.getPlanName());
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {
				plan.setId(rs.getInt("PLAN_ID"));
				plan.setPlanName(rs.getString("PLAN_NAME"));
				plan.setMaxApplications(rs.getInt("MAX_APPLICATIONS"));
			}

		} catch (SQLException e) {
			String msg = "Error while adding the Plans to Data Base";
			log.error(msg, e);
			throw e;
		} finally {

			if (preparedStatement != null) {
				preparedStatement.close();
			}

			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return plan;
	}

	@Override
	public boolean deletePlanById(int planId) throws SQLException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		boolean isDeleted;
		String sql = "DELETE FROM AC_SUBSCRIPTION_PLANS WHERE PLAN_ID="+planId;
		try {
			DBConfiguration dbCon = new DBConfiguration();
			dbConnection = dbCon.getConnection();
			preparedStatement = dbConnection.prepareStatement(sql);
			isDeleted = preparedStatement.executeUpdate() == 1 ? true : false;
		} catch (SQLException e) {
			String msg = "Error while deleting the Plan with ID "+planId+"from Data Base";
			log.error(msg, e);
			throw e;
		} finally {
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return isDeleted;
	}

	@Override
	public Plan updatePlanById(int planId, Plan plan) throws SQLException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		String sql = "Update AC_SUBSCRIPTION_PLANS SET PLAN_NAME=?, MAX_INSTANCES=?, WHERE PLAN_ID = ?";
		try {
			DBConfiguration dbCon = new DBConfiguration();
			dbConnection = dbCon.getConnection();
			preparedStatement = dbConnection.prepareStatement(sql);

			preparedStatement.setString(1, plan.getPlanName());
			preparedStatement.setInt(2, plan.getMaxApplications());
			preparedStatement.setInt(3, planId);
			preparedStatement.executeUpdate();
			preparedStatement.close();

			String sql2 = "select * from AC_SUBSCRIPTION_PLANS WHERE PLAN_ID= ?";
			preparedStatement = dbConnection.prepareStatement(sql2);
			preparedStatement.setInt(1, planId);
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {
				plan.setId(rs.getInt("PLAN_ID"));
				plan.setPlanName(rs.getString("PLAN_NAME"));
				plan.setMaxApplications(rs.getInt("MAX_APPLICATIONS"));
			}
		} catch (SQLException e) {
			String msg = "Error while updating the Plan with ID "+planId+"from Data Base";
			log.error(msg, e);
			throw e;
		} finally {
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return plan;
	}

	@Override
	public List<ContainerSpecifications> getAllowedConSpecs(int planId) throws SQLException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		List<ContainerSpecifications> allowedContainerSpecs = new ArrayList<ContainerSpecifications>();
		String sqlAllContainerSpecs = "select * from AC_CONTAINER_SPECIFICATIONS WHERE CON_SPEC_ID NOT IN "
		                              + "(SELECT CON_SPEC_ID FROM AC_SUBSCRIPTION_PLANS JOIN RestrictedPlanContainerSpecs ON"
		                              + " AC_SUBSCRIPTION_PLANS.PLAN_ID = RestrictedPlanContainerSpecs.PLAN_ID WHERE"
		                              + " RestrictedPlanContainerSpecs.PLAN_ID ="+planId+")";
		try {
			DBConfiguration dbCon = new DBConfiguration();
			dbConnection = dbCon.getConnection();
			preparedStatement = dbConnection.prepareStatement(sqlAllContainerSpecs);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				ContainerSpecifications containerSpecification = new ContainerSpecifications();
				containerSpecification.setId(rs.getInt("CON_SPEC_ID"));
				containerSpecification.setConSpecName(rs.getString("CON_SPEC_NAME"));
				containerSpecification.setCpu(rs.getInt("CPU"));
				containerSpecification.setMemory(rs.getInt("MEMORY"));
				containerSpecification.setCostPerHour(rs.getInt("COST_PER_HOUR"));
				allowedContainerSpecs.add(containerSpecification);
			}
		} catch (SQLException e) {
			String msg = "Error while getting details of container specifications that are allowed in Plan "
			             + "with ID the Plan with ID "+planId;
			log.error(msg, e);
			throw e;
		} finally {
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return allowedContainerSpecs;
	}
}
