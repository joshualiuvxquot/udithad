/*******************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 * 
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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
package com.wso2telco.dep.verificationhandler.verifier;

import com.wso2telco.dep.datapublisher.DataPublisherConstants;
import com.wso2telco.dep.datapublisher.NorthboundDataPublisherClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

// TODO: Auto-generated Javadoc

/**
 */
public class APIResponseHandler extends AbstractMediator {

    /** The Constant log. */
    private static final Log log   = LogFactory.getLog(APIResponseHandler.class);
    
    /** The data publisher client. */
    private NorthboundDataPublisherClient dataPublisherClient;

    /* (non-Javadoc)
     * @see org.apache.synapse.rest.Handler#handleResponse(org.apache.synapse.MessageContext)
     */
    public boolean mediate(MessageContext messageContext) {
        String jsonBody = JsonUtil.jsonPayloadToString(((Axis2MessageContext) messageContext).getAxis2MessageContext());

        messageContext.setProperty(DataPublisherConstants.MSISDN, messageContext.getProperty("UserMSISDN"));
        dataPublisherClient.publishResponse(messageContext, jsonBody);
        return true; // Should never stop the message flow
    }

}
