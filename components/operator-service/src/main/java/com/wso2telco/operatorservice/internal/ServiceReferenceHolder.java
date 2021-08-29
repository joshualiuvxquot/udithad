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
package com.wso2telco.operatorservice.internal;

import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.utils.ConfigurationContextService;

// TODO: Auto-generated Javadoc
/**
 * The Class ServiceReferenceHolder.
 */
public class ServiceReferenceHolder {
	
	/** The Constant instance. */
	private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();
	
	/** The context service. */
	private static ConfigurationContextService contextService = null;
	
	/** The configuration. */
	private static APIManagerConfiguration configuration = null;
	

    /**
     * Gets the single instance of ServiceReferenceHolder.
     *
     * @return single instance of ServiceReferenceHolder
     */
    public static ServiceReferenceHolder getInstance() {
        return instance;
    }
    
    /**
     * Gets the context service.
     *
     * @return the context service
     */
    public static ConfigurationContextService getContextService() {
        return contextService;
    }

    /**
     * Sets the context service.
     *
     * @param contextService the new context service
     */
    public static void setContextService(ConfigurationContextService contextService) {
        ServiceReferenceHolder.contextService = contextService;
    }
    
    /**
     * Gets the API manager configuration.
     *
     * @return the API manager configuration
     */
    public static APIManagerConfiguration getAPIManagerConfiguration() {
		return configuration;
	}
    
    /**
     * Sets the api manager configuration.
     *
     * @return the API manager configuration
     */
    public static APIManagerConfiguration setAPIManagerConfiguration() {
		return configuration;
	}

}
