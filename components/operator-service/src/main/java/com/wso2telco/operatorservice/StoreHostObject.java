/*******************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *  
 *  WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.operatorservice;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import com.wso2telco.operatorservice.dao.DAO;
import com.wso2telco.operatorservice.model.Operator;

import org.mozilla.javascript.*;

 
// TODO: Auto-generated Javadoc
/**
 * The Class StoreHostObject.
 */
public class StoreHostObject extends ScriptableObject {

	/** The Constant log. */
	private static final Log log = LogFactory.getLog(StoreHostObject.class);
	
	/** The hostobject name. */
	private String hostobjectName = "AxiataStore";

	/* (non-Javadoc)
	 * @see org.mozilla.javascript.ScriptableObject#getClassName()
	 */
	@Override
	public String getClassName() {
		return hostobjectName;
	}

	/**
	 * Instantiates a new axiata store host object.
	 */
	public StoreHostObject() {
		log.info("::: Initialized HostObject ");
	}

	 
	/**
	 * Js function_retrieve operator list.
	 *
	 * @param cx the cx
	 * @param thisObj the this obj
	 * @param args the args
	 * @param funObj the fun obj
	 * @return the list
	 * @throws APIManagementException the API management exception
	 */
	public static List<Operator> jsFunction_retrieveOperatorList(Context cx,
													Scriptable thisObj, Object[] args, Function funObj)
													throws APIManagementException {
		
		List<Operator> operatorList = null;
		
		try {
			DAO axiataDAO = new DAO();
			operatorList = axiataDAO.retrieveOperatorList();
			
		} catch(Exception e) {
			handleException("Error occured while retrieving operator list. ", e);
		}
		
		return operatorList;
	}
	
	 
	/**
	 * Js function_persist sub operator list.
	 *
	 * @param cx the cx
	 * @param thisObj the this obj
	 * @param args the args
	 * @param funObj the fun obj
	 * @return true, if successful
	 * @throws APIManagementException the API management exception
	 */
	public static boolean jsFunction_persistSubOperatorList(Context cx,
													Scriptable thisObj, Object[] args, Function funObj)
													throws APIManagementException {
		
		boolean status = false;
		
		String apiName = (String)args[0];
		String apiVersion = (String)args[1];
		String apiProvider = (String)args[2];;
		int appId = ((Double)args[3]).intValue();
		String operatorList = (String)args[4];
		
		try {
			DAO axiataDAO = new DAO();
			axiataDAO.persistOperators(apiName, apiVersion, apiProvider, appId, operatorList);
			
		} catch(Exception e) {
			handleException("Error occured while retrieving operator list. ", e);
		}
		
		return status;
	}
	
	 
	/**
	 * Handle exception.
	 *
	 * @param msg the msg
	 * @throws APIManagementException the API management exception
	 */
	private static void handleException(String msg) throws APIManagementException {
		log.error(msg);
		throw new APIManagementException(msg);
	}

	 
	/**
	 * Handle exception.
	 *
	 * @param msg the msg
	 * @param t the t
	 * @throws APIManagementException the API management exception
	 */
	private static void handleException(String msg, Throwable t) throws APIManagementException {
		log.error(msg, t);
		throw new APIManagementException(msg, t);
	}
}
