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

package org.wso2.intcloud.services.tenant.tasks;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.task.CarbonTaskManagementService;
import org.wso2.carbon.task.TaskManagementException;

import javax.xml.stream.XMLStreamException;

public class TenantTaskAdminService extends CarbonTaskManagementService {

    private static final Log log = LogFactory.getLog(TenantTaskAdminService.class);

    public boolean addTaskDescriptionInTenant(int tenantId, String taskElement) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);

            return addTaskDescription(AXIOMUtil.stringToOM(taskElement));

        } catch (XMLStreamException e) {
            log.error(e.getMessage(),e);
            return false;
        } catch (TaskManagementException e) {
            log.error(e.getMessage(), e);
            return false;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public boolean deleteTaskDescriptionInTenant(int tenantId, String name, String group)
            throws TaskManagementException {
        log.info("Deleting task " + name);
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);

            return deleteTaskDescription(name, group);

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public boolean taskExists(int tenantId, String name, String group)
            throws TaskManagementException {
        log.info("Getting task " + name);
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);

            OMElement taskDescription = getTaskDescription(name, group);

            return (taskDescription != null);

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}