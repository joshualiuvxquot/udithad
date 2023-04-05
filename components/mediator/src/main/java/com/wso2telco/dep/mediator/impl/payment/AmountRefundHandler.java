/*******************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 * 
 * WSO2.Telco Inc. licences this file to you under  the Apache License, Version 2.0 (the "License");
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
package com.wso2telco.dep.mediator.impl.payment;

import java.util.List;
import java.util.Map;

import com.wso2telco.dbutils.fileutils.FileReader;
import com.wso2telco.dep.mediator.service.PaymentService;
import com.wso2telco.dep.mediator.MSISDNConstants;
import com.wso2telco.dep.mediator.OperatorEndpoint;
import com.wso2telco.dep.mediator.ResponseHandler;
import com.wso2telco.dep.mediator.internal.AggregatorValidator;
import com.wso2telco.dep.mediator.internal.UID;
import com.wso2telco.dep.mediator.mediationrule.OriginatingCountryCalculatorIDD;
import com.wso2telco.dep.operatorservice.model.OperatorEndPointDTO;
import com.wso2telco.oneapivalidation.exceptions.CustomException;
import com.wso2telco.oneapivalidation.service.IServiceValidate;
import com.wso2telco.oneapivalidation.service.impl.payment.ValidateRefund;
import com.wso2telco.subscriptionvalidator.util.ValidatorUtils;
import com.wso2telco.dep.mediator.internal.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.json.JSONObject;
import org.apache.synapse.core.axis2.Axis2MessageContext;

/**
 *
 * @author User
 */
public class AmountRefundHandler implements PaymentHandler {

	private static Log log = LogFactory.getLog(AmountRefundHandler.class);
	private static final String API_TYPE = "payment";
	private OriginatingCountryCalculatorIDD occi;
	private ResponseHandler responseHandler;
	private PaymentExecutor executor;
	private PaymentService dbservice;

	public AmountRefundHandler(PaymentExecutor executor) {
		this.executor = executor;
		occi = new OriginatingCountryCalculatorIDD();
		responseHandler = new ResponseHandler();
		dbservice = new PaymentService();
	}

	@Override
	public boolean validate(String httpMethod, String requestPath, JSONObject jsonBody, MessageContext context) throws Exception {
		if (!httpMethod.equalsIgnoreCase("POST")) {
			            ((Axis2MessageContext) context).getAxis2MessageContext().setProperty("HTTP_SC", 405);
			            throw new Exception("Method not allowed");
			        }
			
			        IServiceValidate validator = new ValidateRefund();
			        validator.validateUrl(requestPath);
			        validator.validate(jsonBody.toString());
			        return true;
	}

	@Override
	public boolean handle(MessageContext context) throws Exception {
		String requestid = UID.getUniqueID(Type.PAYMENT.getCode(), context,	executor.getApplicationid());
        JSONObject jsonBody = executor.getJsonBody();
        String endUserId = jsonBody.getJSONObject("amountTransaction").getString("endUserId");
        String msisdn = endUserId.substring(5);
        context.setProperty(MSISDNConstants.USER_MSISDN, msisdn);
        OperatorEndpoint endpoint = null;
        if (ValidatorUtils.getValidatorForSubscription(context).validate(context)) {
            endpoint = occi.getAPIEndpointsByMSISDN(endUserId.replace("tel:", ""), API_TYPE, executor.getSubResourcePath(), false, executor.getValidoperators());
        }

        String sending_add = endpoint.getEndpointref().getAddress();
        log.debug("sending endpoint found: " +  sending_add);

        JSONObject clientclr = jsonBody.getJSONObject("amountTransaction");
        String originalClientCorrelator = clientclr.getString("clientCorrelator");
        clientclr.put("clientCorrelator", originalClientCorrelator + ":"+ requestid);

        JSONObject chargingdmeta = clientclr.getJSONObject("paymentAmount").getJSONObject("chargingMetaData");

        String subscriber = PaymentUtil.storeSubscription(context);
        boolean isaggrigator = PaymentUtil.isAggregator(context);

        if (isaggrigator) {
            //JSONObject chargingdmeta = clientclr.getJSONObject("paymentAmount").getJSONObject("chargingMetaData");
            if (!chargingdmeta.isNull("onBehalfOf")) {
                new AggregatorValidator().validateMerchant(Integer.valueOf(executor.getApplicationid()), endpoint.getOperator(), subscriber, chargingdmeta.getString("onBehalfOf"));
            }
        }

        //validate payment categoreis
        List<String> validCategoris = dbservice.getValidPayCategories();
        PaymentUtil.validatePaymentCategory(chargingdmeta, validCategoris);

        String responseStr = executor.makeRequest(endpoint, sending_add, jsonBody.toString(), true, context, false);

        // Payment Error Exception Correction
        String base = PaymentUtil.str_piece(PaymentUtil.str_piece(responseStr, '{', 2), ':', 1);

        String errorReturn = "\"" + "requestError" + "\"";

        executor.removeHeaders(context);

        if (base.equals(errorReturn)) {
            executor.handlePluginException(responseStr);
        }

        responseStr = makeRefundResponse(responseStr, requestid, originalClientCorrelator);

        //set response re-applied
        executor.setResponse(context, responseStr);
        ((Axis2MessageContext) context).getAxis2MessageContext().setProperty("messageType", "application/json");
        ((Axis2MessageContext) context).getAxis2MessageContext().setProperty("ContentType", "application/json");

        return true;
    }

    private String makeRefundResponse(String responseStr, String requestid, String originalClientCorrelator) {

        String jsonResponse = null;

        try {

        	FileReader fileReader = new FileReader();
			Map<String, String> mediatorConfMap = fileReader.readMediatorConfFile();
            String ResourceUrlPrefix = mediatorConfMap.get("hubGateway");

            JSONObject jsonObj = new JSONObject(responseStr);
            JSONObject objAmountTransaction = jsonObj.getJSONObject("amountTransaction");

            objAmountTransaction.put("clientCorrelator", originalClientCorrelator);
            objAmountTransaction.put("resourceURL", ResourceUrlPrefix + executor.getResourceUrl() + "/" + requestid);
            jsonResponse = jsonObj.toString();
        } catch (Exception e) {

            log.error("Error in formatting amount refund response : " + e.getMessage());
            throw new CustomException("SVC1000", "", new String[]{null});
        }

        log.debug("Formatted amount refund response : " + jsonResponse);
        return jsonResponse;



	}

}