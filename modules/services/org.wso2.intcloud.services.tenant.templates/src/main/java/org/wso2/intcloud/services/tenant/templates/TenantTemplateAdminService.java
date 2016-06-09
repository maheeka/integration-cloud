package org.wso2.intcloud.services.tenant.templates;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.templates.common.TemplateInfo;
import org.wso2.carbon.mediation.templates.services.TemplateEditorAdmin;

import javax.jws.WebParam;

public class TenantTemplateAdminService extends TemplateEditorAdmin {

    private static final Log log = LogFactory.getLog(TenantTemplateAdminService.class);

    public TemplateInfo[] getTemplatesInTenant(@WebParam(name = "tenantId") int tenantId, @WebParam(name = "pageNumber") int pageNumber, @WebParam(name = "templatePerPage") int templatePerPage) throws AxisFault {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);

            return getTemplates(pageNumber, templatePerPage);

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public String getTemplateInTenant(@WebParam(name = "tenantId")int tenantId, @WebParam(name = "templateName") String templateName) throws AxisFault {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);

            return getTemplate(templateName).toString();

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}