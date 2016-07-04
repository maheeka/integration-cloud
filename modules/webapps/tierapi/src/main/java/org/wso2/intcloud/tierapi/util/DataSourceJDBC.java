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

package org.wso2.intcloud.tierapi.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.naming.SelectorContext;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

public class DataSourceJDBC {

	private static final Log log = LogFactory.getLog(DataSourceJDBC.class);

	public static Connection getConnection() {
		Connection conn = null;
		try {
			Context initCtx = new InitialContext();
			SelectorContext selectorContext =
					new SelectorContext((Hashtable<String, Object>) initCtx.getEnvironment(), false);
			Context envCtx = (Context) selectorContext.lookup("java:comp/env");

			DataSource ds = (DataSource)
					envCtx.lookup("jdbc/WSO2IntCloud");

			conn = ds.getConnection();

		} catch (NamingException e) {
			String msg =
					"Error while connecting to Data Source ";
			log.error(msg, e);
		} catch (SQLException e) {
			String msg =
					"Error while getting connection to Data Base ";
			log.error(msg, e);
		}
		return conn;
	}
}