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

package org.wso2.appcloud.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.appcloud.common.AppCloudException;
import org.wso2.appcloud.core.dao.ApplicationDAO;
import org.wso2.appcloud.core.dto.*;
import org.wso2.carbon.context.CarbonContext;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * This class provide the interface for accessing the dao layer.
 */
public class ApplicationManager {

    private static Log log = LogFactory.getLog(ApplicationManager.class);

    /**
     * Method for adding application.
     *
     * @param application application object
     * @return
     * @throws AppCloudException
     */
    public static void addApplication(Application application) throws AppCloudException {
        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {

            applicationDAO.addApplication(dbConnection, application, tenantId);
            dbConnection.commit();

        } catch (SQLException e) {
            String msg = "Error while committing the application adding transaction for application : " +
                         application.getApplicationName() + " in tenant : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }

    }

    /**
     * Method for adding application version.
     *
     * @param version version object
     * @throws AppCloudException
     */
    public static void addApplicationVersion(Version version, String applicationHashId) throws AppCloudException {
        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            int applicationId = applicationDAO.getApplicationId(dbConnection, applicationHashId);
            applicationDAO.addVersion(dbConnection, version,"",applicationId, tenantId);
            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Error while committing the application version adding transaction for application id : " +
                    applicationHashId + ", version:"+ version.getVersionName()+" in tenant : " + tenantId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }

    }

    /**
     * Method for adding runtime properties for a specific version.
     *
     * @param runtimeProperties list of runtime properties
     * @param versionHashId version hash id
     * @throws AppCloudException
     */
    public static void addRuntimeProperties(List<RuntimeProperty> runtimeProperties, String versionHashId) throws AppCloudException {
        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            int versionId = applicationDAO.getVersionId(dbConnection, versionHashId);

            if (runtimeProperties != null) {
                applicationDAO.addRunTimeProperties(dbConnection, runtimeProperties, versionHashId, tenantId);
                dbConnection.commit();
            }
        } catch (SQLException e) {
            String msg = "Error while committing the transaction when adding runtime properties for version with version" +
                         " id : " + versionHashId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for adding tags for a specific version.
     *
     * @param tags list of tags
     * @param versionHashId version hash id
     * @throws AppCloudException
     */
    public static void addTags(List<Tag> tags, String versionHashId)
            throws AppCloudException {
        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            if (tags != null) {
                applicationDAO.addTags(dbConnection, tags, versionHashId, tenantId);
                dbConnection.commit();
            }
        } catch (SQLException e) {
            String msg = "Error while committing the transaction when adding tags for version with hash id : " + versionHashId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }


    /**
     * Method for getting the list of application of a tenant.
     *
     * @return
     * @throws AppCloudException
     */
    public static Application[] getApplicationList() throws AppCloudException {

        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<Application> applications;

        try {
            applications = applicationDAO.getAllApplicationsList(dbConnection, tenantId);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }

        return applications.toArray(new Application[applications.size()]);
    }


    public static List<String> getVersionListOfApplication(String applicationHashId) throws AppCloudException {

        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();

        try {
            return applicationDAO.getAllVersionListOfApplication(dbConnection, applicationHashId);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    public static List<String> getVersionHashIdsOfApplication(String applicationHashId) throws AppCloudException {

        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();

        try {
            return applicationDAO.getAllVersionHashIdsOfApplication(dbConnection, applicationHashId);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    public static boolean isSingleVersion(String versionHashId) throws AppCloudException {
        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();

        try {
            return applicationDAO.isSingleVersion(dbConnection, versionHashId);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    public static String getApplicationHashIdByVersionHashId(String versionHashId) throws AppCloudException {
        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();

        try {
            return applicationDAO.getApplicationHashIdByVersionHashId(dbConnection, versionHashId);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    public static String getApplicationNameByHashId(String applicationHashId) throws AppCloudException {

        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();

        try {

            return applicationDAO.getApplicationNameByHashId(dbConnection, applicationHashId);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    public static String getApplicationHashIdByName(String applicationName) throws AppCloudException {

        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            return applicationDAO.getApplicationHashIdByName(dbConnection, applicationName, tenantId);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Method for getting application by hash id.
     *
     * @param applicationHashId application hash id
     * @return
     * @throws AppCloudException
     */
    public static Application getApplicationByHashId(String applicationHashId) throws AppCloudException {

        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();

        try {
            return applicationDAO.getApplicationByHashId(dbConnection, applicationHashId);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    public static List<RuntimeProperty> getAllRuntimePropertiesOfVersion (String versionHashId) throws AppCloudException {

        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();

        try {
            return applicationDAO.getAllRuntimePropertiesOfVersion(dbConnection, versionHashId);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }


    public static List<Tag> getAllTagsOfVersion(String versionHashId) throws AppCloudException {

        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();

        try {
            return applicationDAO.getAllTagsOfVersion(dbConnection, versionHashId);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }


    public static void updateRuntimeProperty(String versionHashId, String oldKey, String newKey,
                                                        String newValue) throws AppCloudException {
        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();

        try {
            applicationDAO.updateRuntimeProperty(dbConnection, versionHashId, oldKey, newKey, newValue);
            dbConnection.commit();
        }  catch (SQLException e) {
            String msg = "Error while committing transaction when adding runtime property with key : " + oldKey +
                         " for version with hash id : " + versionHashId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }


    public static void updateTag(String versionHashId, String oldKey, String newKey, String newValue)
            throws AppCloudException {

        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();

        try {
            applicationDAO.updateTag(dbConnection, versionHashId, oldKey, newKey, newValue);
            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Error while committing the transaction when updating tag with the key : " + oldKey +
                         " for version with hash id : " + versionHashId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    public static void deleteRuntimeProperty(String versionHashId, String key)
            throws AppCloudException {

        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();

        try {
            applicationDAO.deleteRuntimeProperty(dbConnection, versionHashId, key);
            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Error while deleting runtime property with key : " + key + " for version with hash id : " +
                         versionHashId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }


    public static void deleteTag(String versionHashId, String key)
            throws AppCloudException {

        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();

        try {
            applicationDAO.deleteTag(dbConnection, versionHashId, key);
            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Error while committing transaction when deleting tag with key : " + key +
                         " for version with hash id : " + versionHashId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    public static void updateApplicationIcon(String applicationHashId, Object iconStream)
            throws AppCloudException {

        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();

        if( iconStream instanceof InputStream){
            InputStream iconInputStream = (InputStream) iconStream;
            try {
                int applicationId = applicationDAO.getApplicationId(dbConnection, applicationHashId);
                applicationDAO.updateApplicationIcon(dbConnection, iconInputStream, applicationId);
                dbConnection.commit();
            } catch (SQLException e) {
                String msg = "Error while committing the transaction when updating the application icon for application " +
                             "with hash id : " + applicationHashId;
                log.error(msg, e);
                throw new AppCloudException(msg, e);
            } finally {
                try {
                    iconInputStream.close();
                } catch (IOException e) {
                    log.error("Error occurred while closing input stream for application with hash id : " +
                              applicationHashId);
                } finally {
                    DBUtil.closeConnection(dbConnection);
                }
            }
        } else {
            String msg = "Cannot read the provided icon stream for application with hash id : " + applicationHashId;
            log.error(msg);
            throw new AppCloudException(msg);
        }
    }


    /**
     * Method for getting all apptypes.
     *
     * @return
     * @throws AppCloudException
     */
    public static ApplicationType[] getAllAppTypes() throws AppCloudException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        ApplicationDAO applicationDAO = new ApplicationDAO();
        List<ApplicationType> applicationTypeList = applicationDAO.getAllApplicationTypes();
        return applicationTypeList.toArray(new ApplicationType[applicationTypeList.size()]);
    }


    /**
     * Method for getting all runtimes for a given application type.
     *
     * @param appType application type
     * @return
     * @throws AppCloudException
     */
    public static ApplicationRuntime[] getAllRuntimesForAppType(String appType)
            throws AppCloudException {
        ApplicationDAO applicationDAO = new ApplicationDAO();
        List<ApplicationRuntime> runtimes = applicationDAO.getRuntimesForAppType(appType);
        return runtimes.toArray(new ApplicationRuntime[runtimes.size()]);
    }

    /**
     * Method for updating application status.
     *
     * @param status status of application
     * @return
     * @throws AppCloudException
     */
    public static boolean updateVersionStatus(String versionHashId, String status) throws AppCloudException {

        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();
        boolean isUpdateSuccess = false;

        try {
            isUpdateSuccess = applicationDAO.updateVersionStatus(dbConnection, status, versionHashId);
            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Error while committing the transaction when updating version status with status : " + status +
                         " for version with hash id : " + versionHashId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }

        return isUpdateSuccess;
    }

    /**
     * Method for delete an application completely.
     *
     * @param applicationHashId application hash id
     * @throws AppCloudException
     */
    public static void deleteApplication(String applicationHashId) throws AppCloudException {
        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();

        try {
            applicationDAO.deleteAllDeploymentOfApplication(dbConnection, applicationHashId);
            applicationDAO.deleteApplication(dbConnection, applicationHashId);
            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Error while deleting application with hash id : " + applicationHashId;
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }


    public static void deleteVersion(String versionHashId) throws AppCloudException {
        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();

        try {
            applicationDAO.deleteDeployment(dbConnection, versionHashId);
            applicationDAO.deleteVersion(dbConnection, versionHashId);
            dbConnection.commit();
        }  catch (SQLException e) {
            String msg = "Error while committing the transaction when deleting the version with hash id : " + versionHashId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    public static void addDeployment(String versionHashId, Deployment deployment)throws AppCloudException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbcConnection = DBUtil.getDBConnection();
        try {
            applicationDAO.addDeployment(dbcConnection, versionHashId, deployment, tenantId);
            dbcConnection.commit();
        } catch (SQLException e) {
            String msg = "Error while committing transaction when adding deployment for version with hash id : " +
                         versionHashId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbcConnection);
        }

    }

    public static Deployment getDeployment(String versionHashId)throws AppCloudException {
        ApplicationDAO applicationDAO = new ApplicationDAO();
        return applicationDAO.getDeployment(versionHashId);
    }

    public static void deleteDeployment(String versionHashId)throws AppCloudException {

        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();
        try {
            applicationDAO.deleteDeployment(dbConnection, versionHashId);
            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Error while committing transaction when deleting deployment for version with hash id : " +
                         versionHashId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }
    }

    public static Transport[] getTransportsForRuntime (int runtimeId) throws AppCloudException {
        ApplicationDAO applicationDAO = new ApplicationDAO();
        List<Transport> transports = applicationDAO.getTransportsForRuntime(runtimeId);
        return transports.toArray(new Transport[transports.size()]);
    }

    public static ApplicationRuntime getRuntimeById (int runtimeId) throws AppCloudException {
        ApplicationDAO applicationDAO = new ApplicationDAO();
        return applicationDAO.getRuntimeById(runtimeId);
    }
	
	public static int getApplicationCount() throws AppCloudException {
        ApplicationDAO applicationDAO = new ApplicationDAO();

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        int applicationCount = applicationDAO.getApplicationCount(tenantId);

        return applicationCount;
    }
    /**
     * Get container service proxy by version hash id.
     *
     * @param versionHashId
     * @return
     * @throws AppCloudException
     */
    public static List<ContainerServiceProxy> getContainerServiceProxyByVersion(String versionHashId)
            throws AppCloudException {
        ApplicationDAO applicationDAO = new ApplicationDAO();
        List<ContainerServiceProxy> containerServiceProxies = null;

        try {
            containerServiceProxies = applicationDAO.getContainerServiceProxyByVersion(versionHashId);
        } catch (AppCloudException e) {
            String message = "Error while getting container service proxy with version hash id : " + versionHashId;
            throw new AppCloudException(message, e);
        }

        return containerServiceProxies;
    }

    /**
     * Update container service proxy service by version hash id.
     *
     * @param versionHashId
     * @param host_url
     * @return
     * @throws AppCloudException
     */
    public static boolean updateContainerServiceProxyService(String versionHashId, String host_url)
            throws AppCloudException {
        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();
        boolean isUpdateSuccess = false;

        try {
            isUpdateSuccess = applicationDAO.updateContainerServiceProxy(dbConnection, versionHashId, host_url);
            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Error while updating the container service proxy with version hash id : " + versionHashId;
            log.error(msg, e);
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }

        return isUpdateSuccess;
    }

    /**
     * Update default version field with mapped version for custom url
     *
     * @param applicationHashId
     * @param defaultVersionName
     * @return
     * @throws AppCloudException
     */
    public static boolean updateDefaultVersion(String applicationHashId, String defaultVersionName)
            throws AppCloudException {
        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();
        boolean isUpdatedSuccess = false;

        try {
            isUpdatedSuccess = applicationDAO.updateDefaultVersion(dbConnection, applicationHashId, defaultVersionName);
            dbConnection.commit();
        } catch (SQLException e) {
            String message = "Error while updating default version with application hash id : " + applicationHashId;
            throw new AppCloudException(message, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }

        return isUpdatedSuccess;
    }

    public static Version[] getApplicationVersionsByRunningTimePeriod(int numberOfHours) throws AppCloudException {
        ApplicationDAO applicationDAO = new ApplicationDAO();

        return applicationDAO.getApplicationVersionsByRunningTimePeriod(numberOfHours);
    }

	public static int getMaxAppCountForWhiteListedTenants(int tenantID) throws AppCloudException {
		ApplicationDAO applicationDAO = new ApplicationDAO();
		Connection dbConnection = DBUtil.getDBConnection();

		int maxAppCount = 0;
		try {
			maxAppCount = applicationDAO.getWhiteListedTenantMaxAppCount(dbConnection, tenantID);
		} finally {
			DBUtil.closeConnection(dbConnection);
		}
		return maxAppCount;
	}

    public static List<Version> getAllVersionsOfApplication(String applicationHashId) throws AppCloudException {

        ApplicationDAO applicationDAO = new ApplicationDAO();
        Connection dbConnection = DBUtil.getDBConnection();

        try {
            return applicationDAO.getAllVersionsOfApplication(dbConnection, applicationHashId);
        } catch (AppCloudException e) {
            String msg = "Error while getting versions list for application with hash id : " + applicationHashId;
            throw new AppCloudException(msg, e);
        } finally {
            DBUtil.closeConnection(dbConnection);
        }

    }

	public static void whiteListApplicationVersion(String versionHashId) throws AppCloudException {
		ApplicationDAO applicationDAO = new ApplicationDAO();
		Connection dbConnection = DBUtil.getDBConnection();
		try {
			applicationDAO.whiteListApplicationVersion(dbConnection, versionHashId);
			dbConnection.commit();
		} catch (AppCloudException e){
			String msg = "Error whitelisting application version hash id : " + versionHashId;
			throw new AppCloudException(msg, e);
		} catch (SQLException e) {
			String msg = "Error whitelisting application version hash id : " + versionHashId;
			throw new AppCloudException(msg, e);
		} finally {
			DBUtil.closeConnection(dbConnection);
		}
	}

	public static void whiteListTenant(int tenantId, int maxAppCount) throws AppCloudException {
		ApplicationDAO applicationDAO = new ApplicationDAO();
		Connection dbConnection = DBUtil.getDBConnection();
		try {
			applicationDAO.whiteListTenant(dbConnection, tenantId, maxAppCount);
			dbConnection.commit();
		} catch (AppCloudException e){
			String msg = "Error whitelisting tenant for tenant id : " + tenantId;
			throw new AppCloudException(msg, e);
		} catch (SQLException e) {
			String msg = "Error whitelisting tenant for tenant id : " + tenantId;
			throw new AppCloudException(msg, e);
		} finally {
			DBUtil.closeConnection(dbConnection);
		}
	}


}
