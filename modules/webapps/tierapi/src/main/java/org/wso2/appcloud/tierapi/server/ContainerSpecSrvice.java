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

package org.wso2.appcloud.tierapi.server;

import org.wso2.appcloud.tierapi.bean.ContainerSpecifications;
import org.wso2.appcloud.tierapi.dao.impl.ContainerSpecDaoImpl;
import org.wso2.appcloud.tierapi.delegate.DAOdelegate;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.List;

@Path("/containerSpecs")
public class ContainerSpecSrvice {

	private ContainerSpecDaoImpl ContainerSpecInstance = (ContainerSpecDaoImpl) DAOdelegate.getContainerSpecInstance();

	/*
	 * Get all Container Specifications.
	 * @return                  Return all Container Specifications
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<ContainerSpecifications> getContainerSpecifications() throws SQLException {

		return ContainerSpecInstance.getAllContainerSpecs();
	}

	/*
	 * Get Plan using Container Specifications ID.
	 * @param containerSpecId            Container Specifications ID of the Container Specification
	 * @return                  Return the Container Specifications refer to the Container Specifications ID
	 */
	@GET
	@Path("/{containerSpecId}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public ContainerSpecifications getContainerSpecification(@PathParam("containerSpecId") int containerSpecId) throws SQLException {

		return ContainerSpecInstance.getContainerSpecById(containerSpecId);
	}

	/*
	 * Get Container Specifications using Runtime ID.
	 * @param RuntimeId         runTimeId ID
	 * @return                  Return the Container Specification refer to Runtime ID
	 */
	@GET
	@Path("allowedruntime/{runTimeId}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<ContainerSpecifications> getContainerSpecificationbyRuntimeId(@PathParam("runTimeId") int runtimeId) throws SQLException {

		return ContainerSpecInstance.getContainerSpecByRuntimeID(runtimeId);
	}

}
