/**
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


package org.wso2.intcloud.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.intcloud.common.IntCloudException;
import org.wso2.intcloud.common.util.IntCloudUtil;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtil {

    private static Log log = LogFactory.getLog(DBUtil.class);

    private static DataSource dataSource;
    private static final String DATASOURCE_NAME = "DataSourceName";

    public static void initDatasource() {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext threadLocalCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            threadLocalCarbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID, true);
            try {
                String datasourceName = IntCloudUtil.getPropertyValue(DATASOURCE_NAME);
                InitialContext context = new InitialContext();
                dataSource = (DataSource)context.lookup(datasourceName);
                if(log.isDebugEnabled()){
                    log.debug("Initialized datasource : " + datasourceName + " successfully");
                }
            } catch (NamingException e) {
                log.error("Error while initializing datasource : " + DATASOURCE_NAME, e);
                throw new ExceptionInInitializerError(e);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }

    public static Connection getDBConnection() throws IntCloudException {

        Connection connection;
        try {

            connection = getDataSource().getConnection();
            connection.setAutoCommit(false);

        } catch (SQLException e) {
            String msg = "Error while getting  database connection";
            throw new IntCloudException(msg, e);
        }

        return connection;
    }

    private static DataSource getDataSource() {
        if(dataSource == null){
            initDatasource();
        }
        return dataSource;
    }

    public static void closeConnection(Connection dbConnection){

        if(dbConnection != null){
            try {
                dbConnection.close();
            } catch (SQLException e) {
                String msg = "Error while closing the database connection";
                log.error(msg, e);
            }
        }
    }

    public static void closePreparedStatement(PreparedStatement preparedStatement){

        if(preparedStatement != null){
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                String msg = "Error while closing prepared statement";
                log.error(msg, e);
            }
        }
    }

    /**
     * This method is used to close the database result set.
     * The intended use of this method is within a finally block.
     * This method will log any exceptions that is occurred while closing the database result set.
     *
     * @param resultSet The database result set that needs to be closed.
     */
    public static void closeResultSet(ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException e) {
            // This method is called within a finally block. Hence we do not throw an error from here
            String msg = "Could not close resultSet";
            log.error(msg, e);
        }
    }

    public static void rollbackTransaction(Connection dbConnection){

        if(dbConnection != null){
            try {
                dbConnection.rollback();
            } catch (SQLException e1) {
                log.error("Error while rolling back the failed transaction", e1);
            }
        }
    }
}
