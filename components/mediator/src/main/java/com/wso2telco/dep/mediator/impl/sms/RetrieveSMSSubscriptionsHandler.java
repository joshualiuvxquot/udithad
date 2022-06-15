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
package com.wso2telco.dep.mediator.impl.sms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wso2telco.datapublisher.DataPublisherConstants;
import com.wso2telco.dep.operatorservice.model.OperatorSubscriptionDTO;
import com.wso2telco.dbutils.fileutils.FileReader;
import com.wso2telco.dep.mediator.OperatorEndpoint;
import com.wso2telco.dep.mediator.entity.CallbackReference;
import com.wso2telco.dep.mediator.entity.nb.DestinationAddresses;
import com.wso2telco.dep.mediator.entity.nb.NBSubscribeRequest;
import com.wso2telco.dep.mediator.entity.sb.SBSubscribeRequest;
import com.wso2telco.dep.mediator.entity.sb.Subscription;
import com.wso2telco.dep.mediator.internal.ApiUtils;
import com.wso2telco.dep.mediator.internal.Type;
import com.wso2telco.dep.mediator.internal.UID;
import com.wso2telco.dep.mediator.mediationrule.OriginatingCountryCalculatorIDD;
import com.wso2telco.dep.mediator.service.SMSMessagingService;
import com.wso2telco.oneapivalidation.exceptions.CustomException;
import com.wso2telco.oneapivalidation.service.IServiceValidate;
import com.wso2telco.oneapivalidation.service.impl.sms.ValidateCancelSubscription;
import com.wso2telco.oneapivalidation.service.impl.sms.nb.ValidateNBSubscription;
import com.wso2telco.oneapivalidation.service.impl.sms.sb.ValidateSBSubscription;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.json.JSONObject;
import org.json.JSONException;

// TODO: Auto-generated Javadoc
/**
 * The Class RetrieveSMSSubscriptionsHandler.
 */
public class RetrieveSMSSubscriptionsHandler implements SMSHandler {

	/** The log. */
	private static Log log = LogFactory.getLog(RetrieveSMSSubscriptionsHandler.class);

	/** The Constant API_TYPE. */
	private static final String API_TYPE = "sms";

	/** The occi. */
	private OriginatingCountryCalculatorIDD occi;

	/** The smsMessagingDAO. */
	private SMSMessagingService smsMessagingService;

	/** The executor. */
	private SMSExecutor executor;

	/** The api utils. */
	private ApiUtils apiUtils;

	/**
	 * Instantiates a new retrieve sms subscriptions handler.
	 *
	 * @param executor
	 *            the executor
	 */
	public RetrieveSMSSubscriptionsHandler(SMSExecutor executor) {

		this.executor = executor;
		occi = new OriginatingCountryCalculatorIDD();
		smsMessagingService = new SMSMessagingService();
		apiUtils = new ApiUtils();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wso2telco.mediator.impl.sms.SMSHandler#handle(org.apache.synapse.
	 * MessageContext)
	 */
	@Override
	public boolean handle(MessageContext context) throws CustomException, AxisFault, Exception {
		if (executor.getHttpMethod().equalsIgnoreCase("POST")) {
			return createSubscriptions(context);
		} else if (executor.getHttpMethod().equalsIgnoreCase("DELETE")) {
			return deleteSubscriptions(context);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wso2telco.mediator.impl.sms.SMSHandler#validate(java.lang.String,
	 * java.lang.String, org.json.JSONObject, org.apache.synapse.MessageContext)
	 */
	@Override
	public boolean validate(String httpMethod, String requestPath, JSONObject jsonBody, MessageContext context)
			throws Exception {
		context.setProperty(DataPublisherConstants.OPERATION_TYPE, 205);
		IServiceValidate validator;
		if (httpMethod.equalsIgnoreCase("POST")) {

			JSONObject jsondstaddr = jsonBody.getJSONObject("subscription");

			if (!jsondstaddr.isNull("criteria")) {
				validator = new ValidateSBSubscription();
				validator.validateUrl(requestPath);
				validator.validate(jsonBody.toString());
			} else {
				validator = new ValidateNBSubscription();
				validator.validateUrl(requestPath);
				validator.validate(jsonBody.toString());
			}
			return true;
		} else if (httpMethod.equalsIgnoreCase("DELETE")) {
			String moSubscriptionId = requestPath.substring(requestPath.lastIndexOf("/") + 1);
			String[] params = { moSubscriptionId };
			validator = new ValidateCancelSubscription();
			validator.validateUrl(requestPath);
			validator.validate(params);
			return true;
		} else {
			((Axis2MessageContext) context).getAxis2MessageContext().setProperty("HTTP_SC", 405);
			throw new Exception("Method not allowed");
		}
	}

	/**
	 * Creates the subscriptions.
	 *
	 * @param context
	 *            the context
	 * @return true, if successful
	 * @throws Exception
	 *             the exception
	 */
	private boolean createSubscriptions(MessageContext context) throws Exception {

		String requestid = UID.getUniqueID(Type.RETRIVSUB.getCode(), context, executor.getApplicationid());
		Gson gson = new GsonBuilder().serializeNulls().create();

		FileReader fileReader = new FileReader();
		Map<String, String> mediatorConfMap = fileReader.readMediatorConfFile();

		HashMap<String, String> jwtDetails = apiUtils.getJwtTokenDetails(context);
		JSONObject jsonBody = executor.getJsonBody();
		JSONObject jsondstaddr = jsonBody.getJSONObject("subscription");

		String orgclientcl = "";
		if (!jsondstaddr.isNull("clientCorrelator")) {
			orgclientcl = jsondstaddr.getString("clientCorrelator");
		}

		String serviceProvider = jwtDetails.get("subscriber");
		log.debug("Subscriber Name : " + serviceProvider);

		if (!jsondstaddr.isNull("criteria")) {

			SBSubscribeRequest subsrequst = gson.fromJson(jsonBody.toString(), SBSubscribeRequest.class);
			String origNotiUrl = subsrequst.getSubscription().getCallbackReference().getNotifyURL();

			List<OperatorEndpoint> endpoints = occi.getAPIEndpointsByApp(API_TYPE, executor.getSubResourcePath(),
					executor.getValidoperators());

			Integer moSubscriptionId = smsMessagingService.subscriptionEntry(
					subsrequst.getSubscription().getCallbackReference().getNotifyURL(), serviceProvider);

			String subsEndpoint = mediatorConfMap.get("hubSubsGatewayEndpoint") + "/" + moSubscriptionId;
			jsondstaddr.getJSONObject("callbackReference").put("notifyURL", subsEndpoint);

			jsondstaddr.put("clientCorrelator", orgclientcl + ":" + requestid);

			List<OperatorSubscriptionDTO> domainsubs = new ArrayList<OperatorSubscriptionDTO>();
			SBSubscribeRequest subsresponse = null;
			for (OperatorEndpoint endpoint : endpoints) {

				String notifyres = executor.makeRequest(endpoint, endpoint.getEndpointref().getAddress(),
						jsonBody.toString(), true, context);
				if (notifyres == null) {
					throw new CustomException("POL0299", "", new String[] { "Error registering subscription" });
				} else {
					// plugin exception handling
					subsresponse = gson.fromJson(notifyres, SBSubscribeRequest.class);
					if (subsresponse.getSubscription() == null) {
						executor.handlePluginException(notifyres);
					}
					domainsubs.add(new OperatorSubscriptionDTO(endpoint.getOperator(),
							subsresponse.getSubscription().getResourceURL()));
				}
			}

			boolean issubs = smsMessagingService.operatorSubsEntry(domainsubs, moSubscriptionId);

			String ResourceUrlPrefix = mediatorConfMap.get("hubGateway");
			subsresponse.getSubscription()
					.setResourceURL(ResourceUrlPrefix + executor.getResourceUrl() + "/" + moSubscriptionId);

			JSONObject replyobj = new JSONObject(subsresponse);
			JSONObject replysubs = replyobj.getJSONObject("subscription");

			replysubs.put("clientCorrelator", orgclientcl);

			replysubs.getJSONObject("callbackReference").put("notifyURL", origNotiUrl);

			jsondstaddr.put("resourceURL", ResourceUrlPrefix + executor.getResourceUrl() + "/" + moSubscriptionId);

			executor.removeHeaders(context);
			((Axis2MessageContext) context).getAxis2MessageContext().setProperty("HTTP_SC", 201);
			executor.setResponse(context, replyobj.toString());
		} else {

			NBSubscribeRequest nbSubsrequst = gson.fromJson(jsonBody.toString(), NBSubscribeRequest.class);
			String origNotiUrl = nbSubsrequst.getSubscription().getCallbackReference().getNotifyURL();

			List<OperatorEndpoint> endpoints = occi.getAPIEndpointsByApp(API_TYPE, executor.getSubResourcePath(),
					executor.getValidoperators());

			Integer moSubscriptionId = smsMessagingService.subscriptionEntry(
					nbSubsrequst.getSubscription().getCallbackReference().getNotifyURL(), serviceProvider);

			String subsEndpoint = mediatorConfMap.get("hubSubsGatewayEndpoint") + "/" + moSubscriptionId;
			jsondstaddr.getJSONObject("callbackReference").put("notifyURL", subsEndpoint);

			jsondstaddr.put("clientCorrelator", orgclientcl + ":" + requestid);

			log.debug("Subscription northbound request body : " + gson.toJson(nbSubsrequst));

			List<OperatorSubscriptionDTO> domainsubs = new ArrayList<OperatorSubscriptionDTO>();
			SBSubscribeRequest sbSubsresponse = null;

			DestinationAddresses[] destinationAddresses = nbSubsrequst.getSubscription().getDestinationAddresses();

			for (OperatorEndpoint endpoint : endpoints) {

				for (int i = 0; i < destinationAddresses.length; i++) {
					if (destinationAddresses[i].getOperatorCode().equalsIgnoreCase(endpoint.getOperator())) {
						log.debug("Operator name: " + endpoint.getOperator());
						SBSubscribeRequest sbSubsrequst = new SBSubscribeRequest();
						Subscription sbrequest = new Subscription();
						CallbackReference callbackReference = new CallbackReference();

						callbackReference.setCallbackData(
								nbSubsrequst.getSubscription().getCallbackReference().getCallbackData());
						callbackReference.setNotifyURL(subsEndpoint);
						sbrequest.setCallbackReference(callbackReference);
						sbrequest.setClientCorrelator(orgclientcl + ":" + requestid);
						sbrequest.setNotificationFormat(nbSubsrequst.getSubscription().getNotificationFormat());
						sbrequest.setCriteria(destinationAddresses[i].getCriteria());
						sbrequest.setDestinationAddress(destinationAddresses[i].getDestinationAddress());
						sbSubsrequst.setSubscription(sbrequest);

						String sbRequestBody = removeResourceURL(gson.toJson(sbSubsrequst));
						log.debug("Subscription southbound request body of " + endpoint.getOperator() + " operator: "
								+ sbRequestBody);

						String notifyres = executor.makeRequest(endpoint, endpoint.getEndpointref().getAddress(),
								sbRequestBody, true, context);

						log.debug("Subscription southbound response body of " + endpoint.getOperator() + " operator: "
								+ notifyres);

						if (notifyres == null) {
							destinationAddresses[i].setStatus("Failed");

						} else {
							// plugin exception handling
							sbSubsresponse = gson.fromJson(notifyres, SBSubscribeRequest.class);
							if (sbSubsresponse.getSubscription() == null) {

								destinationAddresses[i].setStatus("NotCreated");
							} else {
								domainsubs.add(new OperatorSubscriptionDTO(endpoint.getOperator(),
										sbSubsresponse.getSubscription().getResourceURL()));
								destinationAddresses[i].setStatus("Created");
							}
						}
						break;
					}
				}
			}

			boolean issubs = smsMessagingService.operatorSubsEntry(domainsubs, moSubscriptionId);

			String ResourceUrlPrefix = mediatorConfMap.get("hubGateway");

			DestinationAddresses[] responseDestinationAddresses = new DestinationAddresses[destinationAddresses.length];
			int destinationAddressesCount = 0;
			int successResultCount = 0;
			for (DestinationAddresses destinationAddressesResult : destinationAddresses) {
				String subscriptionStatus = destinationAddressesResult.getStatus();
				if (subscriptionStatus == null) {
					destinationAddressesResult.setStatus("Failed");
				} else if (subscriptionStatus.equals("Created")) {
					successResultCount++;
				}
				responseDestinationAddresses[destinationAddressesCount] = destinationAddressesResult;
				destinationAddressesCount++;
			}

			if (successResultCount == 0) {
				throw new CustomException("POL0299", "", new String[] { "Error registering subscription" });
			}

			nbSubsrequst.getSubscription().setDestinationAddresses(responseDestinationAddresses);
			nbSubsrequst.getSubscription()
					.setResourceURL(ResourceUrlPrefix + executor.getResourceUrl() + "/" + moSubscriptionId);
			nbSubsrequst.getSubscription().setClientCorrelator(orgclientcl);
			nbSubsrequst.getSubscription().getCallbackReference().setNotifyURL(origNotiUrl);

			String nbResponseBody = gson.toJson(nbSubsrequst);

			log.debug("Subscription northbound response body : " + nbResponseBody);

			executor.removeHeaders(context);
			((Axis2MessageContext) context).getAxis2MessageContext().setProperty("HTTP_SC", 201);
			executor.setResponse(context, nbResponseBody.toString());
		}

		return true;
	}

	/**
	 * Delete subscriptions.
	 *
	 * @param context
	 *            the context
	 * @return true, if successful
	 * @throws Exception
	 *             the exception
	 */
	private boolean deleteSubscriptions(MessageContext context) throws Exception {

		String requestPath = executor.getSubResourcePath();
		String moSubscriptionId = requestPath.substring(requestPath.lastIndexOf("/") + 1);

		String requestid = UID.getUniqueID(Type.DELRETSUB.getCode(), context, executor.getApplicationid());

		List<OperatorSubscriptionDTO> domainsubs = (smsMessagingService
				.subscriptionQuery(Integer.valueOf(moSubscriptionId)));
		if (domainsubs.isEmpty()) {

			throw new CustomException("POL0001", "",
					new String[] { "SMS Receipt Subscription Not Found: " + moSubscriptionId });
		}

		String resStr = "";

		for (OperatorSubscriptionDTO subs : domainsubs) {

			resStr = executor.makeDeleteRequest(
					new OperatorEndpoint(new EndpointReference(subs.getDomain()), subs.getOperator()), subs.getDomain(),
					null, true, context);
		}

		smsMessagingService.subscriptionDelete(Integer.valueOf(moSubscriptionId));
		executor.removeHeaders(context);
		((Axis2MessageContext) context).getAxis2MessageContext().setProperty("HTTP_SC", 204);

		return true;
	}

	/**
	 * Removes the resource url.
	 *
	 * @param sbSubsrequst
	 *            the sb subsrequst
	 * @return the string
	 */
	private String removeResourceURL(String sbSubsrequst) {

		String sbrequestString = "";

		try {

			JSONObject objJSONObject = new JSONObject(sbSubsrequst);
			JSONObject objSubscriptionRequest = (JSONObject) objJSONObject.get("subscription");
			objSubscriptionRequest.remove("resourceURL");

			sbrequestString = objSubscriptionRequest.toString();
		} catch (JSONException ex) {

			log.error("Error in removeResourceURL" + ex.getMessage());
			throw new CustomException("POL0299", "", new String[] { "Error registering subscription" });
		}

		return "{\"subscription\":" + sbrequestString + "}";
	}
}
