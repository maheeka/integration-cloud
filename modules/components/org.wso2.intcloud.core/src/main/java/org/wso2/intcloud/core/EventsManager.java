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

import org.wso2.intcloud.common.IntCloudException;
import org.wso2.intcloud.core.dao.EventsDAO;
import org.wso2.intcloud.core.dto.Event;
import org.wso2.carbon.context.CarbonContext;

import java.util.List;

public class EventsManager {

    /**
     * Method for updating app creation events.
     *
     * @param versionHashId version Hash id
     * @param event
     * @throws IntCloudException
     */
    public void addAppCreationEvent(String versionHashId, Event event) throws IntCloudException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventsDAO eventsDAO = new EventsDAO();
        eventsDAO.addAppCreationEvent(versionHashId, event, tenantId);
    }

    /**
     * Method for retrieve application creation event stream.
     *
     * @param versionHashId version hash id
     * @return
     * @throws IntCloudException
     */
    public Event[] getEventsOfApplication(String versionHashId) throws IntCloudException {

        EventsDAO eventsDAO = new EventsDAO();
        List<Event> events = eventsDAO.getEventsOfApplication(versionHashId);

        return events.toArray(new Event[events.size()]);
    }

    /**
     * Delete all events related to a particluar version.
     * @param versionHashId version hash id
     * @throws IntCloudException
     */
    public void deleteAllEventsofAppVersion(String versionHashId)throws IntCloudException {

        EventsDAO eventsDAO = new EventsDAO();
        eventsDAO.deleteAppVersionEvents(versionHashId);

    }
}
