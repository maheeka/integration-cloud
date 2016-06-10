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

package org.wso2.intcloud.services.tenant.templates;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.templates.common.TemplateInfo;
import org.wso2.carbon.mediation.templates.services.TemplateEditorAdmin;

import javax.jws.WebParam;

public class TenantTemplateAdminService extends TemplateEditorAdmin {

    private static final Log log = LogFactory.getLog(TenantTemplateAdminService.class);

    public TemplateInfo[] getTemplatesInTenant(@WebParam(name = "tenantId") int tenantId,
                                               @WebParam(name = "pageNumber") int pageNumber,
                                               @WebParam(name = "templatePerPage") int templatePerPage)
            throws AxisFault {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);

            return getTemplates(pageNumber, templatePerPage);

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public String getTemplateInTenant(@WebParam(name = "tenantId") int tenantId,
                                      @WebParam(name = "templateName") String templateName) throws AxisFault {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);

            return getTemplate(templateName).toString();

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}