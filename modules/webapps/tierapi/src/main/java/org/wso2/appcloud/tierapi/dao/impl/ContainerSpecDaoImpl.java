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
import org.wso2.appcloud.tierapi.dao.ContainerSpecsDao;
import org.wso2.appcloud.tierapi.util.DBConfiguration;

import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ContainerSpecDaoImpl implements ContainerSpecsDao {
	private static final Log log = LogFactory.getLog(ContainerSpecDaoImpl.class);

	@Override
	public List<ContainerSpecifications> getAllContainerSpecs() throws SQLException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		List<ContainerSpecifications> containerSpecsList = new ArrayList<ContainerSpecifications>();
		String sql = "select * from AC_CONTAINER_SPECIFICATIONS";
		try {
			DBConfiguration dbCon = new DBConfiguration();
			dbConnection = dbCon.getConnection();
			preparedStatement = dbConnection.prepareStatement(sql);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				ContainerSpecifications containerSpec = new ContainerSpecifications();
				containerSpec.setId(rs.getInt("CON_SPEC_ID"));
				containerSpec.setConSpecName(rs.getString("CON_SPEC_NAME"));
				containerSpec.setCpu(rs.getInt("CPU"));
				containerSpec.setMemory(rs.getInt("MEMORY"));
				containerSpec.setCostPerHour(rs.getInt("COST_PER_HOUR"));
				containerSpecsList.add(containerSpec);
			}
			rs.close();
		} catch (SQLException e) {
			String msg = "Error while getting details of Container Specifications";
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
		return containerSpecsList;
	}

	@Override
	public List<ContainerSpecifications> getContainerSpecByRuntimeID(int runtimeId) throws SQLException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		List<ContainerSpecifications> containerSpecsList = new ArrayList<ContainerSpecifications>();
		String sql = "SELECT * FROM AC_CONTAINER_SPECIFICATIONS JOIN AC_RUNTIME_CONTAINER_SPECIFICATIONS "
		             + "ON AC_CONTAINER_SPECIFICATIONS.CON_SPEC_ID = AC_RUNTIME_CONTAINER_SPECIFICATIONS.CON_SPEC_ID"
		             + " WHERE AC_RUNTIME_CONTAINER_SPECIFICATIONS.id =" + runtimeId;
		try {
			DBConfiguration dbCon = new DBConfiguration();
			dbConnection = dbCon.getConnection();
			preparedStatement = dbConnection.prepareStatement(sql);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				ContainerSpecifications containerSpec = new ContainerSpecifications();
				containerSpec.setId(rs.getInt("CON_SPEC_ID"));
				containerSpec.setConSpecName(rs.getString("CON_SPEC_NAME"));
				containerSpec.setCpu(rs.getInt("CPU"));
				containerSpec.setMemory(rs.getInt("MEMORY"));
				containerSpec.setCostPerHour(rs.getInt("COST_PER_HOUR"));
				containerSpecsList.add(containerSpec);
			}
			rs.close();
		} catch (SQLException e) {
			String msg = "Error while getting details of Container Specifications for Runtime ID " + runtimeId;
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
		return containerSpecsList;
	}

	@Override
	public ContainerSpecifications getContainerSpecById(int containerSpecId) throws SQLException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ContainerSpecifications containerSpec = new ContainerSpecifications();
		String sql = "select * from AC_CONTAINER_SPECIFICATIONS WHERE CON_SPEC_ID =" + containerSpecId;
		try {
			DBConfiguration dbCon = new DBConfiguration();
			dbConnection = dbCon.getConnection();
			preparedStatement = dbConnection.prepareStatement(sql);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				containerSpec.setId(rs.getInt("CON_SPEC_ID"));
				containerSpec.setConSpecName(rs.getString("CON_SPEC_NAME"));
				containerSpec.setCpu(rs.getInt("CPU"));
				containerSpec.setMemory(rs.getInt("MEMORY"));
				containerSpec.setCostPerHour(rs.getInt("COST_PER_HOUR"));
			}
			rs.close();
		} catch (SQLException e) {
			String msg =
					"Error while getting details of Container Specification with the ID" + containerSpecId;
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
		return containerSpec;
	}

	@Override
	public ContainerSpecifications defineContainerSpec(ContainerSpecifications containerSpec) throws SQLException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		String sql = "INSERT INTO AC_CONTAINER_SPECIFICATIONS (CON_SPEC_NAME, CPU, MEMORY,COST_PER_HOUR) "
		             + "VALUES (?, ?, ?, ?)";
		try {
			DBConfiguration dbCon = new DBConfiguration();
			dbConnection = dbCon.getConnection();
			preparedStatement = dbConnection.prepareStatement(sql);
			preparedStatement.setString(1, containerSpec.getConSpecName());
			preparedStatement.setInt(2, containerSpec.getCpu());
			preparedStatement.setInt(3, containerSpec.getMemory());
			preparedStatement.setInt(4, containerSpec.getCostPerHour());
			preparedStatement.executeUpdate();
			preparedStatement.close();
			String sql2 = "select * from AC_CONTAINER_SPECIFICATIONS WHERE CON_SPEC_NAME= ?";
			preparedStatement = dbConnection.prepareStatement(sql2);
			preparedStatement.setString(1, containerSpec.getConSpecName());
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				containerSpec.setId(rs.getInt("CON_SPEC_ID"));
				containerSpec.setConSpecName(rs.getString("CON_SPEC_NAME"));
				containerSpec.setCpu(rs.getInt("CPU"));
				containerSpec.setMemory(rs.getInt("MEMORY"));
				containerSpec.setCostPerHour(rs.getInt("COST_PER_HOUR"));
			}
		} catch (SQLException e) {
			String msg = "Error while defining the Container Specifications";
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
		return containerSpec;
	}

	@Override
	public boolean deleteContainerSpecById(int containerSpecId) throws SQLException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		boolean isDeleted;
		String sql = "DELETE FROM AC_CONTAINER_SPECIFICATIONS WHERE CON_SPEC_ID=" + containerSpecId;
		try {
			DBConfiguration dbCon = new DBConfiguration();
			dbConnection = dbCon.getConnection();
			preparedStatement = dbConnection.prepareStatement(sql);
			isDeleted = preparedStatement.executeUpdate() == 1 ? true : false;
		} catch (SQLException e) {
			String msg =
					"Error while deleting the Container Specifications with ID " + containerSpecId;
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
	public ContainerSpecifications updateContainerSpecById(int containerSpecId, ContainerSpecifications containerSpec) throws SQLException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		String sql = "Update AC_CONTAINER_SPECIFICATIONS SET CON_SPEC_NAME=?, CPU= ?, MEMORY=?,"
		             + "COST_PER_HOUR=? WHERE CON_SPEC_ID = ?";
		try {
			DBConfiguration dbCon = new DBConfiguration();
			dbConnection = dbCon.getConnection();
			preparedStatement = dbConnection.prepareStatement(sql);
			preparedStatement.setString(1, containerSpec.getConSpecName());
			preparedStatement.setInt(2, containerSpec.getCpu());
			preparedStatement.setInt(3, containerSpec.getMemory());
			preparedStatement.setInt(4, containerSpec.getCostPerHour());
			preparedStatement.setInt(5, containerSpecId);
			preparedStatement.executeUpdate();
			preparedStatement.close();
			String sql2 = "select * from AC_CONTAINER_SPECIFICATIONS WHERE CON_SPEC_ID= ?";
			preparedStatement = dbConnection.prepareStatement(sql2);
			preparedStatement.setInt(1, containerSpecId);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				containerSpec.setId(rs.getInt("CON_SPEC_ID"));
				containerSpec.setConSpecName(rs.getString("CON_SPEC_NAME"));
				containerSpec.setCpu(rs.getInt("CPU"));
				containerSpec.setMemory(rs.getInt("MEMORY"));
				containerSpec.setCostPerHour(rs.getInt("COST_PER_HOUR"));
			}
		} catch (SQLException e) {
			String msg =
					"Error while Updating the Container Specifications with ID " + containerSpecId;
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
		return containerSpec;
	}
}
