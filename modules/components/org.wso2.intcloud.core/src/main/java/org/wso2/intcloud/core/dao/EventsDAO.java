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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.intcloud.common.IntCloudException;
import org.wso2.intcloud.core.DBUtil;
import org.wso2.intcloud.core.SQLQueryConstants;
import org.wso2.intcloud.core.dto.Event;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for persisting or retrieving application creation related events.
 */

public class EventsDAO {

    private static final Log log = LogFactory.getLog(EventsDAO.class);

    /**
     * Method for adding application creation events to database.
     *
     * @param versionHashId version hash id
     * @param event application creation event
     * @return
     * @throws IntCloudException
     */
    public boolean addAppCreationEvent(String versionHashId, Event event, int tenantId) throws IntCloudException {

        Connection dbConnection = DBUtil.getDBConnection();
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.ADD_APP_CREATION_EVENT);
            preparedStatement.setString(1, event.getEventName());
            preparedStatement.setString(2, event.getEventStatus());
            preparedStatement.setString(3, versionHashId);
            preparedStatement.setTimestamp(4, event.getTimestamp());
            preparedStatement.setString(5, event.getEventDescription());
            preparedStatement.setInt(6, tenantId);

            boolean result = preparedStatement.execute();
            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Error occurred while adding app creation event: " + event.getEventName() + " status: " + event
                    .getEventStatus() + " timestamp: " + event.getTimestamp();
            log.error(msg, e);
            throw new IntCloudException(msg, e);

        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
            DBUtil.closeConnection(dbConnection);
        }
        return true;
    }

    /**
     * Delete all the events related to a particular app version.
     * @param versionHashId version hash id
     * @return
     * @throws IntCloudException
     */
    public boolean deleteAppVersionEvents(String versionHashId) throws IntCloudException {

        Connection dbConnection = DBUtil.getDBConnection();
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.DELETE_ALL_APP_VERSION_EVENTS);
            preparedStatement.setString(1, versionHashId);

            int result = preparedStatement.executeUpdate();
            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Error occurred while deleting all the events for the app version has id " + versionHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);

        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
            DBUtil.closeConnection(dbConnection);
        }
        return true;
    }



    /**
     *  Method to get event stream of an application.
     *
     * @param versionHashId application id
     * @return
     * @throws IntCloudException
     */
    public List<Event> getEventsOfApplication(String versionHashId) throws IntCloudException {

        Connection dbConnection = DBUtil.getDBConnection();
        PreparedStatement preparedStatement = null;

        List<Event> eventList = new ArrayList<>();

        try {
            preparedStatement = dbConnection.prepareStatement(SQLQueryConstants.GET_ALL_EVENTS_OF_APPLICATION);
            preparedStatement.setString(1, versionHashId);

            ResultSet resultSet = preparedStatement.executeQuery();
            Event event;
            while (resultSet.next()) {
                event = new Event();
                event.setEventName(resultSet.getString(SQLQueryConstants.NAME));
                event.setEventStatus(resultSet.getString(SQLQueryConstants.STATUS));
                event.setTimestamp(resultSet.getTimestamp(SQLQueryConstants.EVENT_TIMESTAMP));
                event.setEventDescription(resultSet.getString(SQLQueryConstants.DESCRIPTION));

                eventList.add(event);
            }

        } catch (SQLException e) {
            String msg = "Error while retrieving Application creation event stream for application with hash id : " + versionHashId;
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        } finally {
            DBUtil.closePreparedStatement(preparedStatement);
            DBUtil.closeConnection(dbConnection);
        }
        return eventList;
    }
}
