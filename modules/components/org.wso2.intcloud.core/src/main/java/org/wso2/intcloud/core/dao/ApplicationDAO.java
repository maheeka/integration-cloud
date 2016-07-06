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

package org.wso2.intcloud.core.dao;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.intcloud.common.IntCloudException;
import org.wso2.intcloud.core.DBUtil;
import org.wso2.intcloud.core.SQLQueryConstants;
import org.wso2.intcloud.core.dto.Application;
import org.wso2.intcloud.core.dto.ApplicationRuntime;
import org.wso2.intcloud.core.dto.ApplicationType;
import org.wso2.intcloud.core.dto.Transport;
import org.wso2.intcloud.core.dto.Version;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DAO class for persisting or retrieving application related data to database.
 */
public class ApplicationDAO {

    private static final Log log = LogFactory.getLog(ApplicationDAO.class);

    /**
     * Method for adding application details to database.
     *
     * @param dbConnection database connection
     * @param application application object
     * @param tenantId tenant id
     * @return
     * @throws IntCloudException
     */
    public void addApplication(Connection dbConnection, Application application, int tenantId) throws IntCloudException {

        PreparedStatement preparedStatement = null;
        int applicationId = 0;
        ResultSet resultSet = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.ADD_APPLICATION,
                                                              Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, application.getApplicationName());
            preparedStatement.setString(2, application.getHashId());
            preparedStatement.setString(3, application.getDescription());
            preparedStatement.setInt(4, tenantId);
            preparedStatement.setString(5, application.getDefaultVersion());
            preparedStatement.setString(6, application.getCarbonApplicationName());
            preparedStatement.setString(7, application.getApplicationType());

            preparedStatement.execute();

            resultSet = preparedStatement.getGeneratedKeys();
            while (resultSet.next()) {
                applicationId = resultSet.getInt(1);
            }

            List<Version> versions = application.getVersions();

            if (versions != null) {
                for (Version version : versions) {
                    addVersion(dbConnection, version, applicationId, tenantId);
                }
            }

            InputStream iconInputStream = null;
            if (application.getIcon() != null) {
                iconInputStream = IOUtils.toBufferedInputStream(application.getIcon().getBinaryStream());
            }
            updateApplicationIcon(dbConnection, iconInputStream, applicationId);

        } catch (SQLException e) {

            String msg =
                    "Error occurred while adding application : " + application.getApplicationName() + " to database " +
                            "in tenant : " + tenantId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);

        } catch (IOException e) {
            String msg =
                    "Error while generating stream of the icon for application : " + application.getApplicationName() +
                    " in tenant : " + tenantId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

    }


    /**
     * Method for adding version details to database.
     *
     * @param dbConnection database connection
     * @param version version object
     * @param applicationId application id
     * @param tenantId tenant id
     * @return
     * @throws IntCloudException
     */
    public void addVersion(Connection dbConnection, Version version, int applicationId, int tenantId)
            throws IntCloudException {


        PreparedStatement preparedStatement = null;
        int versionId = 0;
        ResultSet resultSet = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.ADD_VERSION, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, version.getVersionName());
            preparedStatement.setString(2, version.getHashId());
            preparedStatement.setInt(3, applicationId);
            preparedStatement.setInt(4, version.getRuntimeId());
            preparedStatement.setInt(5, tenantId);

            preparedStatement.execute();

            resultSet = preparedStatement.getGeneratedKeys();
            while (resultSet.next()){
                versionId = resultSet.getInt(1);
            }

//            List<Tag> tags = version.getTags();
//            if (tags != null) {
//                addTags(dbConnection, tags, version.getHashId(), tenantId);
//            }
//
//            List<RuntimeProperty> runtimeProperties = version.getRuntimeProperties();
//            if (runtimeProperties != null) {
//                addRunTimeProperties(dbConnection, runtimeProperties, version.getHashId(), tenantId);
//            }

        } catch (SQLException e) {
            String msg = "Error occurred while adding application version to database for application id : " + applicationId +
                         " version : " + version.getVersionName() + " in tenant : " + tenantId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

    }

    public void updateApplicationIcon(Connection dbConnection, InputStream inputStream, int applicationId)
            throws IntCloudException {

        PreparedStatement preparedStatement = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.UPDATE_APPLICATION_ICON);
            preparedStatement.setBlob(1, inputStream);
            preparedStatement.setInt(2, applicationId);
            preparedStatement.execute();

        } catch (SQLException e) {
            String msg =
                    "Error occurred while updating application icon for application with id : " + applicationId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);

        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }


    /**
     * Method for updating the status of the given version.
     *
     * @param status status of the version
     * @param versionHashId version hash id
     * @return
     * @throws IntCloudException
     */
    public boolean updateVersionStatus(Connection dbConnection, String status, String versionHashId)
            throws IntCloudException {

        PreparedStatement preparedStatement = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.UPDATE_APPLICATION_STATUS);
            preparedStatement.setString(1, status);
            preparedStatement.setString(2, versionHashId);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            String msg = "Error while updating application status : " + status + " for version with the hash id : " +
                         versionHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return true;
    }

    /**
     * Method for getting the list of applications of a tenant from database with minimal information.
     *
     * @param dbConnection database connection
     * @param tenantId tenant id
     * @return
     * @throws IntCloudException
     */
    public List<Application> getAllApplicationsList(Connection dbConnection, int tenantId) throws IntCloudException {

        PreparedStatement preparedStatement = null;

        List<Application> applications = new ArrayList<>();
        Application application;
        ResultSet resultSet = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_ALL_APPLICATIONS_LIST);
            preparedStatement.setInt(1, tenantId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                application = new Application();
                application.setApplicationName(resultSet.getString(SQLQueryConstants.APPLICATION_NAME));
                application.setApplicationType(resultSet.getString(SQLQueryConstants.APPLICATION_TYPE_NAME));
                application.setHashId(resultSet.getString(SQLQueryConstants.HASH_ID));
                application.setIcon(resultSet.getBlob(SQLQueryConstants.ICON));

                applications.add(application);
            }

        } catch (SQLException e) {
            String msg = "Error while retrieving application list from database for tenant : " + tenantId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return applications;
    }


    public List<String> getAllVersionListOfApplication(Connection dbConnection, String applicationHashId)
            throws IntCloudException {

        PreparedStatement preparedStatement = null;
        ArrayList<String> versionList = new ArrayList<>();
        ResultSet resultSet = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_VERSION_LIST_OF_APPLICATION);
            preparedStatement.setString(1, applicationHashId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                versionList.add(resultSet.getString(SQLQueryConstants.NAME));
            }

        } catch (SQLException e) {
            String msg = "Error while getting the list of versions for the application with hash id : " + applicationHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return versionList;
    }


    public List<String> getAllVersionHashIdsOfApplication(Connection dbConnection, String applicationHashId)
            throws IntCloudException {

        PreparedStatement preparedStatement = null;
        ArrayList<String> hashIdList = new ArrayList<>();
        ResultSet resultSet = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_VERSION_HASH_IDS_OF_APPLICATION);
            preparedStatement.setString(1, applicationHashId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                hashIdList.add(resultSet.getString(SQLQueryConstants.HASH_ID));
            }

        } catch (SQLException e) {
            String msg = "Error while getting the list of version hash ids of application : " + applicationHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return hashIdList;
    }

    public List<String> getAllApplicationsUsingCarbonApplication(Connection dbConnection, String applicationHashId)
            throws IntCloudException {

        PreparedStatement preparedStatement = null;
        ArrayList<String> hashIdList = new ArrayList<>();
        ResultSet resultSet = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_APPLICATIONS_USING_CARBON_APPLICATION);
            preparedStatement.setString(1, applicationHashId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                hashIdList.add(resultSet.getString(SQLQueryConstants.HASH_ID));
            }

        } catch (SQLException e) {
            String msg = "Error while getting the list of version hash ids of application : " + applicationHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return hashIdList;
    }

    public List<String> getAllApplicationsUsingCarbonApplication(int tenantId, Connection dbConnection, String applicationHashId)
            throws IntCloudException {

        PreparedStatement preparedStatement = null;
        ArrayList<String> hashIdList = new ArrayList<>();
        ResultSet resultSet = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_APPLICATIONS_USING_CARBON_APPLICATION_IN_TENANT);
            preparedStatement.setString(1, applicationHashId);
            preparedStatement.setString(2, String.valueOf(tenantId));

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                hashIdList.add(resultSet.getString(SQLQueryConstants.HASH_ID));
            }

        } catch (SQLException e) {
            String msg = "Error while getting the list of version hash ids of application : " + applicationHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return hashIdList;
    }

    public String getCarbonApplicationNameOfApplication(Connection dbConnection, String applicationHashId)
            throws IntCloudException {

        PreparedStatement preparedStatement = null;
        String cAppName = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_CARBON_APPLICATION_NAME_OF_APPLICATION);
            preparedStatement.setString(1, applicationHashId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                cAppName = (resultSet.getString(SQLQueryConstants.CARBON_APPLICATION_NAME));
                return cAppName;
            }

        } catch (SQLException e) {
            String msg = "Error while getting the carbon application name of application : " + applicationHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return cAppName;
    }

    public String getTaskConfigurationOfApplication(Connection dbConnection, String applicationHashId)
            throws IntCloudException {

        PreparedStatement preparedStatement = null;
        String taskConfiguration = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_TASK_CONFIGURATION_OF_APPLICATION);
            preparedStatement.setString(1, applicationHashId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                taskConfiguration = (resultSet.getString(SQLQueryConstants.TASK_CONFIGURATION));
                return taskConfiguration;
            }

        } catch (SQLException e) {
            String msg = "Error while getting the task configuration of application : " + applicationHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return taskConfiguration;
    }

    public boolean isSingleVersion(Connection dbConnection, String versionHashId) throws IntCloudException {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = dbConnection.prepareStatement(
                    SQLQueryConstants.GET_VERSION_HASH_IDS_OF_APPLICATION_BY_VERSION_HASH_ID);
            preparedStatement.setString(1, versionHashId);

            resultSet = preparedStatement.executeQuery();
            resultSet.last();

            if(resultSet.getRow() > 1){
                return false;
            } else {
                return true;
            }

        } catch (SQLException e) {
            String msg = "Error while retrieving the data for checking whether an application has multiple versions";
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

    public String getApplicationHashIdByVersionHashId(Connection dbConnection, String versionHashId)
            throws IntCloudException {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String applicatinHashId = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_APPLICATION_HASH_ID_BY_VERSION_HASH_ID);
            preparedStatement.setString(1, versionHashId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                applicatinHashId = resultSet.getString(SQLQueryConstants.HASH_ID);
            }

        } catch (SQLException e) {
            String msg = "Error while getting application hash id by version hash id : " + versionHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return applicatinHashId;
    }


    public String getApplicationNameByHashId(Connection dbConnection, String applicationHashId)
            throws IntCloudException {
        PreparedStatement preparedStatement;
        ResultSet resultSet;
        String applicationName = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_APPLICATION_NAME_BY_HASH_ID);
            preparedStatement.setString(1, applicationHashId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                applicationName = resultSet.getString(SQLQueryConstants.NAME);
            }

        } catch (SQLException e) {
            String msg = "Error while getting the application name of application with hash id : " + applicationHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        }

        return applicationName;
    }


    public String getApplicationHashIdByName(Connection dbConnection, String applicationName, int tenantId)
            throws IntCloudException {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String applicationHashId = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_APPLICATION_HASH_ID_BY_NAME);
            preparedStatement.setString(1, applicationName);
            preparedStatement.setInt(2, tenantId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                applicationHashId = resultSet.getString(SQLQueryConstants.HASH_ID);
            }

        } catch (SQLException e) {
            String msg = "Error while retrieving application hash id using application name : " + applicationName +
                         " in tenant : " + tenantId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return applicationHashId;
    }

    /**
     * Method for getting application from database using application hash id.
     *
     * @param dbConnection database connection
     * @param applicationHashId application hash id
     * @return
     * @throws IntCloudException
     */
    public Application getApplicationByHashId(Connection dbConnection, String applicationHashId) throws IntCloudException {

        PreparedStatement preparedStatement = null;
        Application application = new Application();
        ResultSet resultSet = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_APPLICATION_BY_HASH_ID);

            preparedStatement.setString(1, applicationHashId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                application.setApplicationName(resultSet.getString(SQLQueryConstants.NAME));
                application.setHashId(applicationHashId);
                application.setDescription(resultSet.getString(SQLQueryConstants.DESCRIPTION));
                application.setDefaultVersion(resultSet.getString(SQLQueryConstants.DEFAULT_VERSION));
                application.setApplicationType(resultSet.getString(SQLQueryConstants.APPLICATION_TYPE_NAME));
                application.setIcon(resultSet.getBlob(SQLQueryConstants.ICON));
                application.setCarbonApplicationName(resultSet.getString(SQLQueryConstants.CARBON_APPLICATION_NAME));
                application.setParamConfiguration(resultSet.getString(SQLQueryConstants.PARAM_CONFIGURATION));
                application.setTaskConfiguration(resultSet.getString(SQLQueryConstants.TASK_CONFIGURATION));
                application.setVersions(getAllVersionsOfApplication(dbConnection, applicationHashId));

            }

        } catch (SQLException e) {
            String msg =
                    "Error while retrieving application detail for application with hash id : " + applicationHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return application;
    }

    /**
     * Method for retrieving all the versions of a specific application.
     *
     * @param dbConnection
     * @param applicationHashId
     * @return
     * @throws IntCloudException
     */
    public List<Version> getAllVersionsOfApplication(Connection dbConnection, String applicationHashId)
            throws IntCloudException {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Version> versions = new ArrayList<>();
        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_ALL_VERSIONS_OF_APPLICATION);
            preparedStatement.setString(1, applicationHashId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){

                Version version = new Version();
                version.setVersionName(resultSet.getString(SQLQueryConstants.NAME));
                version.setHashId(resultSet.getString(SQLQueryConstants.HASH_ID));
                version.setRuntimeName(resultSet.getString(SQLQueryConstants.RUNTIME_NAME));
                version.setRuntimeId(resultSet.getInt(SQLQueryConstants.RUNTIME_ID));
                version.setStatus(resultSet.getString(SQLQueryConstants.STATUS));
//                version.setTags(getAllTagsOfVersion(dbConnection, version.getHashId()));
//                version.setRuntimeProperties(getAllRuntimePropertiesOfVersion(dbConnection, version.getHashId()));

                versions.add(version);
            }

        } catch (SQLException e) {
            String msg = "Error while getting all versions of application with application hash id : " + applicationHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return versions;
    }

    /**
     * Method for getting the id of an application with the given hash id.
     *
     * @param dbConnection database connection
     * @param applicationHashId application hash id
     * @return
     * @throws IntCloudException
     */
    public int getApplicationId(Connection dbConnection, String applicationHashId) throws IntCloudException {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int applicationId = 0;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_APPLICATION_ID);
            preparedStatement.setString(1, applicationHashId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                applicationId = resultSet.getInt(SQLQueryConstants.ID);
            }

        } catch (SQLException e) {
            String msg = "Error while retrieving the id of application with hash value : " + applicationHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return applicationId;
    }


    /**
     * Method for getting the version id with the given hash id.
     *
     * @param dbConnection
     * @param hashId
     * @return
     * @throws IntCloudException
     */
    public int getVersionId(Connection dbConnection, String hashId) throws IntCloudException {

        PreparedStatement preparedStatement;
        int versionId = 0;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_VERSION_ID);
            preparedStatement.setString(1, hashId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                versionId = resultSet.getInt(SQLQueryConstants.ID);
            }

        } catch (SQLException e) {
            String msg = "Error while retreiving id of version with hash value : " + hashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        }
        return versionId;
    }


    /**
     * Method for retrieving all the application types.
     *
     * @return
     * @throws IntCloudException
     */
    public List<ApplicationType> getAllApplicationTypes() throws IntCloudException {

        Connection dbConnection = DBUtil.getDBConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        List<ApplicationType> applicationTypeList = new ArrayList<>();
        ApplicationType applicationType;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_ALL_APP_TYPES);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                applicationType = new ApplicationType();
                applicationType.setAppTypeName(resultSet.getString(SQLQueryConstants.NAME));
                applicationType.setDescription(resultSet.getString(SQLQueryConstants.DESCRIPTION));
                applicationType.setBuildable(resultSet.getInt(SQLQueryConstants.BUILDABLE) == 1 ? true : false);
                applicationTypeList.add(applicationType);
            }

        } catch (SQLException e) {
            String msg = "Error while retrieving app types from database";
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
            DBUtil.closeConnection(dbConnection);
        }
        return applicationTypeList;
    }


    /**
     * Method for retrieving all the runtimes for a given application type.
     *
     * @param appType application type
     * @return
     * @throws IntCloudException
     */
    public List<ApplicationRuntime> getRuntimesForAppType(String appType)
            throws IntCloudException {

        Connection dbConnection = DBUtil.getDBConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<ApplicationRuntime> applicationRuntimeList = new ArrayList<ApplicationRuntime>();
        ApplicationRuntime applicationRuntime;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_RUNTIMES_FOR_APP_TYPE_OF_TENANT);
            preparedStatement.setString(1, appType);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                applicationRuntime = new ApplicationRuntime();
                applicationRuntime.setId(resultSet.getInt(SQLQueryConstants.ID));
                applicationRuntime.setRuntimeName(resultSet.getString(SQLQueryConstants.NAME));
                applicationRuntime.setImageName(resultSet.getString(SQLQueryConstants.RUNTIME_IMAGE_NAME));
                applicationRuntime.setRepoURL(resultSet.getString(SQLQueryConstants.RUNTIME_REPO_URL));
                applicationRuntime.setTag(resultSet.getString(SQLQueryConstants.RUNTIME_TAG));
                applicationRuntime.setDescription(resultSet.getString(SQLQueryConstants.DESCRIPTION));

                applicationRuntimeList.add(applicationRuntime);
            }

        } catch (SQLException e) {
            String msg = "Error while retrieving list of runtime from database for app type : " + appType;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
            DBUtil.closeConnection(dbConnection);
        }
        return applicationRuntimeList;
    }


    public ApplicationRuntime getRuntimeById (int runtimeId) throws IntCloudException {

        Connection dbConnection = DBUtil.getDBConnection();
        PreparedStatement preparedStatement = null;
        ApplicationRuntime applicationRuntime = new ApplicationRuntime();
        ResultSet resultSet = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_RUNTIME_BY_ID);
            preparedStatement.setInt(1, runtimeId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                applicationRuntime.setId(resultSet.getInt(SQLQueryConstants.ID));
                applicationRuntime.setImageName(resultSet.getString(SQLQueryConstants.RUNTIME_IMAGE_NAME));
                applicationRuntime.setRepoURL(resultSet.getString(SQLQueryConstants.RUNTIME_REPO_URL));
                applicationRuntime.setRuntimeName(resultSet.getString(SQLQueryConstants.NAME));
                applicationRuntime.setTag(resultSet.getString(SQLQueryConstants.RUNTIME_TAG));
                applicationRuntime.setDescription(resultSet.getString(SQLQueryConstants.DESCRIPTION));
            }

        } catch (SQLException e) {
            String msg = "Error while retrieving runtime info from database for runtime : " + runtimeId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closeResultSet(resultSet);
            DBUtil.closePreparedStatement(preparedStatement);
            DBUtil.closeConnection(dbConnection);
        }
        return applicationRuntime;
    }

    public List<Transport> getTransportsForRuntime(int runtimeId) throws IntCloudException{

        Connection dbConnection = DBUtil.getDBConnection();
        PreparedStatement preparedStatement = null;
        List<Transport> transports = new ArrayList<>();

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_TRANSPORTS_FOR_RUNTIME);
            preparedStatement.setInt(1, runtimeId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                Transport transport = new Transport();
                transport.setServiceName(resultSet.getString(SQLQueryConstants.NAME));
                transport.setServiceProtocol(resultSet.getString(SQLQueryConstants.PROTOCOL));
                transport.setServicePort(resultSet.getInt(SQLQueryConstants.PORT));
                transport.setServiceNamePrefix(resultSet.getString(SQLQueryConstants.SERVICE_NAME_PREFIX));
                transports.add(transport);
            }
            dbConnection.commit();

        } catch (SQLException e) {
            String msg = "Error while retrieving runtime transport detail for runtime : " + runtimeId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
            DBUtil.closeConnection(dbConnection);
        }
        return transports;
    }


    public boolean deleteRuntimeProperty(Connection dbConnection, String versionHashId, String key) throws IntCloudException {

        PreparedStatement preparedStatement = null;
        boolean deleted=false;
        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.DELETE_RUNTIME_PROPERTY);
            preparedStatement.setString(1, versionHashId);
            preparedStatement.setString(2, key);

            deleted = preparedStatement.execute();

        } catch (SQLException e) {
            String msg = "Error while deleting runtime property Key : " + key + " for version with hash id : " +
                         versionHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return deleted;
    }


    public boolean deleteTag(Connection dbConnection, String versionHashId, String key) throws IntCloudException {

        PreparedStatement preparedStatement = null;
        boolean deleted=false;
        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.DELETE_TAG);
            preparedStatement.setString(1, versionHashId);
            preparedStatement.setString(2, key);

            deleted = preparedStatement.execute();

        } catch (SQLException e) {
            String msg = "Error while deleting tag with Key : " + key + " for version with hash id : " + versionHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return deleted;
    }


    /**
     * Delete an application.
     *
     * @param applicationHashId application hash id.
     * @return
     * @throws IntCloudException
     */
    public boolean deleteApplication(Connection dbConnection, String applicationHashId)
            throws IntCloudException {
        PreparedStatement preparedStatement = null;
        boolean deleted = false;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.DELETE_APPLICATION);
            preparedStatement.setString(1, applicationHashId);

            deleted = preparedStatement.execute();
        } catch (SQLException e) {
            String msg = "Error while executing the application deletion sql query with applicationHashId : " + applicationHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return deleted;
    }

    public boolean deleteVersion(Connection dbConnection, String versionHashId) throws IntCloudException {
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.DELETE_VERSION);
            preparedStatement.setString(1, versionHashId);

            return preparedStatement.execute();
        } catch (SQLException e) {
            String msg = "Error while executing the version deletion sql query with versionHashId : " + versionHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Delete all the versions of an application.
     *
     * @param applicationHashId application hash id
     * @return
     * @throws IntCloudException
     */
    public boolean deleteAllVersionsOfApplication(Connection dbConnection, String applicationHashId) throws IntCloudException {

        PreparedStatement preparedStatement = null;
        boolean deleted = false;
        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.DELETE_VERSIONS_OF_APPLICATION);
            preparedStatement.setString(1, applicationHashId);

            deleted = preparedStatement.execute();

        } catch (SQLException e) {
            String msg = "Error while deleting the versions of the application with hash id : " + applicationHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return deleted;
    }

    public void deleteAllDeploymentOfApplication(Connection dbConnection, String applicationHashId)
            throws IntCloudException {

        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.DELETE_ALL_DEPLOYMENT_OF_APPLICATION);
            preparedStatement.setString(1, applicationHashId);

            preparedStatement.execute();

        } catch (SQLException e) {
            String msg = "Error while deleting all deployment of application with hash id : " + applicationHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }

    }


    public void deleteDeployment(Connection dbConnection, String versionHashId) throws IntCloudException{

        PreparedStatement preparedStatement = null;

        try {

            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.DELETE_DEPLOYMENT);
            preparedStatement.setString(1, versionHashId);

            preparedStatement.execute();

        } catch (SQLException e) {
            String msg = "Error while deleting deployment record for version with the hash id : " + versionHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
    }

	public int getApplicationCount(int tenantId) throws IntCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        PreparedStatement preparedStatement = null;
        int appCount = 0;
        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_TENANT_APPLICATION_COUNT);
            preparedStatement.setInt(1, tenantId);
            ResultSet rs = preparedStatement.executeQuery();
            dbConnection.commit();
            if (rs.next()) {
                appCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            String msg = "Error while getting the application count of the tenant : " + tenantId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }
        return appCount;
    }


    /**
     * Update host url from container service proxy for custom url.
     *
     * @param dbConnection
     * @param versionHashId
     * @param host_url
     * @return
     * @throws IntCloudException
     */
    public boolean updateContainerServiceProxy(Connection dbConnection, String versionHashId, String host_url)
            throws IntCloudException {
        PreparedStatement preparedStatement = null;
        boolean success = false;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.UPDATE_CONTAINER_SERVICE_PROXY);
            preparedStatement.setString(1, host_url);
            preparedStatement.setString(2, versionHashId);
            success = preparedStatement.execute();
        } catch (SQLException e) {
            String msg =
                    "Error occurred while updating container service proxy with version hash id : " + versionHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return success;
    }

    /**
     * Update default version for given application.
     *
     * @param applicationHashId
     * @return if sucessfully update the default version
     * @throws IntCloudException
     */
    public boolean updateDefaultVersion(Connection dbConnection, String applicationHashId, String defaultVersionName)
            throws IntCloudException {
        PreparedStatement preparedStatement = null;
        boolean updated = false;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.UPDATE_APPLICATION_DEFAULT_VERSION);
            preparedStatement.setString(1, defaultVersionName);
            preparedStatement.setString(2, applicationHashId);
            updated = preparedStatement.execute();
        } catch (SQLException e) {
            String message = "Error while updating default version with application hash id : " + applicationHashId;
            log.error(message, e);
            throw new IntCloudException(message, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return updated;
    }

    public Version[] getApplicationVersionsByRunningTimePeriod(int numberOfHours) throws IntCloudException {
        Connection dbConnection = DBUtil.getDBConnection();
        PreparedStatement preparedStatement = null;
        List<Version> versions = new ArrayList<>();

        try {

            preparedStatement = dbConnection.prepareStatement(
                    SQLQueryConstants.GET_ALL_APP_VERSIONS_CREATED_BEFORE_X_DAYS_AND_NOT_WHITE_LISTED);
            preparedStatement.setInt(1, numberOfHours);

            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                Version version = new Version();
                version.setHashId(resultSet.getString(SQLQueryConstants.HASH_ID));
                version.setCreatedTimestamp(resultSet.getTimestamp(SQLQueryConstants.EVENT_TIMESTAMP));
                version.setTenantId(resultSet.getInt(SQLQueryConstants.TENANT_ID));

                versions.add(version);
            }
            dbConnection.commit();


        } catch (SQLException e) {
            String msg = "Error while retrieving application version detail for non white listed applications.";
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
            DBUtil.closeConnection(dbConnection);
        }
        return versions.toArray(new Version[versions.size()]);
    }

	public int getWhiteListedTenantMaxAppCount(Connection dbConnection, int tenantID) throws IntCloudException {
		PreparedStatement preparedStatement = null;
		int maxAppCount;

		try {
			preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_WHITE_LISTED_TENANT_DETAILS);
			preparedStatement.setInt(1, tenantID);
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next()){
				maxAppCount = resultSet.getInt(SQLQueryConstants.MAX_APP_COUNT);
			} else {
				maxAppCount = -1;
			}
		} catch (SQLException e) {
			String msg = "Get Max App Count failed for tenant id : " + tenantID;
			log.error(msg, e);
			throw new IntCloudException(msg, e);
		} finally {
			DBUtil.closePreparedStatement(preparedStatement);
		}
		return maxAppCount;
	}

	public void whiteListApplicationVersion(Connection dbConnection, String versionHashId) throws IntCloudException {
		PreparedStatement preparedStatement;
		try {
			preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.UPDATE_WHITE_LIST_APPLICATION_VERSION);
			preparedStatement.setBoolean(1, true);
			preparedStatement.setString(2, versionHashId);
			preparedStatement.execute();
		} catch (SQLException e) {
			String msg = "White listing failed for version hash : " + versionHashId;
			log.error(msg, e);
			throw new IntCloudException(msg, e);
		}
	}

	public void whiteListTenant(Connection dbConnection, int tenantId, int maxAppCount) throws IntCloudException {
		PreparedStatement preparedStatement;
		try {
			preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.ADD_WHITE_LISTED_TENANT);
			preparedStatement.setInt(1, tenantId);
			preparedStatement.setInt(2, maxAppCount);
			preparedStatement.execute();
		} catch (SQLException e) {
			String msg = "White listing failed for tenant id : " + tenantId;
			log.error(msg, e);
			throw new IntCloudException(msg, e);
		}
	}

    /**
     * Update parameter configuration for given application.
     *
     * @param applicationHashId
     * @return if sucessfully update the parameter configuration
     * @throws IntCloudException
     */
    public boolean updateParamConfiguration(Connection dbConnection, String applicationHashId,
                                            String paramConfiguration) throws IntCloudException {
        PreparedStatement preparedStatement = null;
        boolean updated = false;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.UPDATE_APPLICATION_PARAM_CONFIGURATION);
            preparedStatement.setString(1, paramConfiguration);
            preparedStatement.setString(2, applicationHashId);
            updated = preparedStatement.execute();
        } catch (SQLException e) {
            String message =
                    "Error while updating parameter configuration with application hash id : " + applicationHashId;
            log.error(message, e);
            throw new IntCloudException(message, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return updated;
    }

    public boolean updateTaskConfiguration(Connection dbConnection, String applicationHashId, String taskConfiguration)
            throws IntCloudException {
        PreparedStatement preparedStatement = null;
        boolean updated = false;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.UPDATE_APPLICATION_TASK_CONFIGURATION);
            preparedStatement.setString(1, taskConfiguration);
            preparedStatement.setString(2, applicationHashId);
            updated = preparedStatement.execute();
        } catch (SQLException e) {
            String message =
                    "Error while updating parameter configuration with application hash id : " + applicationHashId;
            log.error(message, e);
            throw new IntCloudException(message, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
        }

        return updated;
    }
}
