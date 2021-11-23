/*
 * OneAPI Example Java Library Copyright (C) 2011 GSM Association
 * 
 * This file is part of the GSM Association's example implementation of
 * OneAPI (OneAPI Example Java Library). 
 * Please see http://www.gsmworld.com/oneapi for more information on OneAPI.
 *
 * OneAPI Example Java Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License
 * along with OneAPI Example Java Library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * GSM Association hereby disclaims all copyright interest in the
 * OneAPI Example Java Library.
 * 
 */
package org.gsm.oneapi.payment;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.gsm.oneapi.endpoints.ServiceEndpoints;
import org.gsm.oneapi.foundation.CommandLineOptions;
import org.gsm.oneapi.foundation.FormParameters;
import org.gsm.oneapi.foundation.JSONRequest;
import org.gsm.oneapi.responsebean.RequestError;
import org.gsm.oneapi.responsebean.payment.PaymentResponse;
import org.gsm.oneapi.server.OneAPIServlet;
import org.json.JSONObject;

public class Charge {

    static Logger logger = Logger.getLogger(Charge.class);
    ServiceEndpoints endPoints = null;
    String authorisationHeader = null;
    public static boolean dumpRequestAndResponse = false;
    private static JSONRequest<PaymentResponse> paymentResponseProcessor = new JSONRequest<PaymentResponse>(new PaymentResponse());
    private String sessionAccountBalance;

    /**
     * Creates a new instance of the Payment Charge API main interface. Requires
     * endPoints to define the URL targets of the charge network call and
     * authorisationHeader containing the username/password used for HTTP Basic
     * authorisation with the OneAPI server.
     *
     *
     * @param endPoints contains a set of service/ call specific endpoints
     * @param authorisationHeader Base 64 encoded username/ password
     *
     * @see org.gsm.oneapi.endpoints.ServiceEndpoints
     * @see org.gsm.oneapi.foundation.JSONRequest#getAuthorisationHeader(String,
     * String)
     */
    public Charge(ServiceEndpoints endPoints, String authorisationHeader) {
        this.endPoints = endPoints;
        this.authorisationHeader = authorisationHeader;
    }

    /**
     * Can be used to update the service endpoints
     *
     *
     * @param endPoints contains a set of service/ call specific endpoints
     *
     * @see org.gsm.oneapi.endpoints.ServiceEndpoints
     */
    public void setEndpoints(ServiceEndpoints endPoints) {
        this.endPoints = endPoints;
    }

    /**
     * Can be used to update the service authorisation header
     *
     *
     * @param authorisationHeader Base 64 encoded username/ password
     *
     * @see org.gsm.oneapi.foundation.JSONRequest#getAuthorisationHeader(String,
     * String)
     */
    public void setAuthorisationHeader(String authorisationHeader) {
        this.authorisationHeader = authorisationHeader;
    }

    /**
     * Charge an amount to the end user's bill / mobile phone account.
     *
     *
     * @param endUserId (mandatory) is the end user ID; in this case their
     * MSISDN including the 'tel:' protocol identifier and the country code
     * preceded by '+'. i.e., tel:+16309700001. OneAPI also supports the
     * Anonymous Customer Reference (ACR) if provided by the operator. Do not
     * URL encode this value prior to passing
     * @param referenceCode (mandatory, unique per charge event) is your
     * reference for reconciliation purposes. The operator should include it in
     * reports so that you can match their view of what has been sold with yours
     * by matching the referenceCodes.
     * @param description (mandatory) is the human-readable text to appear on
     * the bill, so the user can easily see what they bought.
     * @param currency (mandatory) is the 3-figure code as per ISO 4217. Note
     * either amount and currency or code must be provided.
     * @param amount (mandatory) can be a whole number or decimal. Note either
     * amount and currency or code must be provided.
     * @param code (mandatory) a code provided by the OneAPI implementation that
     * is used to reference an operator price point. Note either amount and
     * currency or code must be provided.
     * @param callbackURL (optional) URL to post the result of the charge
     * operation to
     * @param clientCorrelator (optional) uniquely identifies this create charge
     * request. If there is a communication failure during the charge request,
     * using the same clientCorrelator when retrying the request allows the
     * operator to avoid applying the same charge twice.
     * @param onBehalfOf (optional) allows aggregators/partners to specify the
     * actual payee.
     * @param purchaseCategoryCode (optional) specifies the type/category of
     * purchase e.g. "Video", "Game"
     * @param channel (optional) can be "Wap", "Web", "SMS", depending on the
     * source of user interaction
     * @param taxAmount (optional) tax already charged by the merchant.
     * @param serviceId (optional) The ID of the partner/merchant service
     * @param productId (optional) combines with the serviceID to uniquely
     * identify the product being purchased.
     *
     * @see PaymentResponse
     */
    public PaymentResponse charge(String requestJSON, String callbackURL) {
        PaymentResponse response = new PaymentResponse();

        
//        FormParameters formParameters = new FormParameters();
//        formParameters.put("endUserId", endUserId);
//        formParameters.put("transactionOperationStatus", "charged");
//        formParameters.put("referenceCode", referenceCode);
//        formParameters.put("description", description);
//        formParameters.put("currency", currency);
//        formParameters.put("amount", Double.toString(amount));
//        formParameters.put("code", code);
//        formParameters.put("callbackURL", callbackURL);
//
//        formParameters.put("clientCorrelator", clientCorrelator);
//        formParameters.put("onBehalfOf", onBehalfOf);
//        formParameters.put("purchaseCategoryCode", purchaseCategoryCode);
//        formParameters.put("channel", channel);
//        formParameters.put("taxAmount", Double.toString(taxAmount));
//        formParameters.put("serviceId", serviceId);
//        formParameters.put("productId", productId);


        String endpoint = endPoints.getAmountChargeEndpoint();

        int responseCode = 0;
        String contentType = null;

        try {
//            if (dumpRequestAndResponse) {
//                JSONRequest.dumpRequestVariables(endpoint.replace("{endUserId}", URLEncoder.encode(endUserId, "utf-8")), authorisationHeader, formParameters);
//            }

//            HttpURLConnection con = JSONRequest.setupConnection(endpoint.replace("{endUserId}", URLEncoder.encode(endUserId, "utf-8")), authorisationHeader);
            HttpURLConnection con = JSONRequest.setupConnection(endpoint, authorisationHeader);
            con.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());

//            String requestBody = JSONRequest.formEncodeParams(formParameters);
            String requestBody = requestJSON;
            out.write(requestBody);
            out.close();


            responseCode = con.getResponseCode();
            contentType = con.getContentType();
            
            setSessionAccountBalance(con.getHeaderField("account"));

//            if (callbackURL == null) {
//                response = paymentResponseProcessor.getResponse(con, OneAPIServlet.CREATED);
//            } else {
                response = paymentResponseProcessor.getResponse(con, OneAPIServlet.ACCEPTED);
//            }
        } catch (Exception e) {
            logger.error("Exception " + e.getMessage() + " " + e.getLocalizedMessage());
            response.setHTTPResponseCode(responseCode);
            response.setContentType(contentType);

            response.setRequestError(new RequestError(RequestError.SERVICEEXCEPTION, "SVCJAVA", e.getMessage(), e.getClass().getName()));
        }
        return response;
    }

    /**
     * Refund an amount to the end user's bill / mobile phone account.
     *
     *
     * @param endUserId (mandatory) is the end user ID; in this case their
     * MSISDN including the 'tel:' protocol identifier and the country code
     * preceded by '+'. i.e., tel:+16309700001. OneAPI also supports the
     * Anonymous Customer Reference (ACR) if provided by the operator. Do not
     * URL encode this value prior to passing
     * @param referenceCode (mandatory, unique per charge event) is your
     * reference for reconciliation purposes. The operator should include it in
     * reports so that you can match their view of what has been sold with yours
     * by matching the referenceCodes.
     * @param description (mandatory) is the human-readable text to appear on
     * the bill, so the user can easily see what they bought.
     * @param currency (mandatory) is the 3-figure code as per ISO 4217. Note
     * either amount and currency or code must be provided.
     * @param amount (mandatory) can be a whole number or decimal. Here means
     * the amount to refund, which can be a full or partial refund of the
     * original charge. Note either amount and currency or code must be
     * provided.
     * @param code (mandatory) a code provided by the OneAPI implementation that
     * is used to reference an operator price point. Note either amount and
     * currency or code must be provided.
     * @param clientCorrelator (optional) uniquely identifies the refund
     * request. If there is a communication failure during the refund request,
     * using the same clientCorrelator when retrying the request allows the
     * operator to avoid applying the same refund twice.
     * @param originalServerReferenceCode (optional) if a serverReferenceCode
     * was provided in the response to the orignal charge request, then you must
     * include it in your refund request. Omitting it risks a policy exception
     * being thrown.
     * @param onBehalfOf (optional) allows aggregators/partners to specify the
     * actual payee.
     * @param purchaseCategoryCode (optional) specifies the type/category of
     * purchase e.g. "Video", "Game"
     * @param channel (optional) can be "Wap", "Web", "SMS", depending on the
     * source of user interaction
     * @param taxAmount (optional) tax already charged by the merchant.
     * @param serviceId (optional) The ID of the partner/merchant service
     * @param productId (optional) combines with the serviceID to uniquely
     * identify the product being purchased.
     *
     * @see PaymentResponse
     */
    public PaymentResponse refund(String endUserId, String referenceCode, String description, String currency, double amount, String code,
            String clientCorrelator, String originalServerReferenceCode, String onBehalfOf, String purchaseCategoryCode, String channel, double taxAmount, String serviceId, String productId) {
        PaymentResponse response = new PaymentResponse();

        FormParameters formParameters = new FormParameters();
        formParameters.put("endUserId", endUserId);
        formParameters.put("transactionOperationStatus", "refunded");
        formParameters.put("referenceCode", referenceCode);
        formParameters.put("description", description);
        formParameters.put("currency", currency);
        formParameters.put("amount", Double.toString(amount));
        formParameters.put("code", code);

        formParameters.put("clientCorrelator", clientCorrelator);
        formParameters.put("originalServerReferenceCode", originalServerReferenceCode);

        formParameters.put("onBehalfOf", onBehalfOf);
        formParameters.put("purchaseCategoryCode", purchaseCategoryCode);
        formParameters.put("channel", channel);
        formParameters.put("taxAmount", Double.toString(taxAmount));
        formParameters.put("serviceId", serviceId);
        formParameters.put("productId", productId);

        String endpoint = endPoints.getAmountRefundEndpoint();

        int responseCode = 0;
        String contentType = null;

        try {
            if (dumpRequestAndResponse) {
                JSONRequest.dumpRequestVariables(endpoint.replace("{endUserId}", URLEncoder.encode(endUserId, "utf-8")), authorisationHeader, formParameters);
            }

            HttpURLConnection con = JSONRequest.setupConnection(endpoint.replace("{endUserId}", URLEncoder.encode(endUserId, "utf-8")), authorisationHeader);
            con.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());

            String requestBody = JSONRequest.formEncodeParams(formParameters);
            out.write(requestBody);
            out.close();

            responseCode = con.getResponseCode();
            contentType = con.getContentType();

            response = paymentResponseProcessor.getResponse(con, OneAPIServlet.OK);
        } catch (Exception e) {
            logger.error("Exception " + e.getMessage() + " " + e.getLocalizedMessage());
            response.setHTTPResponseCode(responseCode);
            response.setContentType(contentType);

            response.setRequestError(new RequestError(RequestError.SERVICEEXCEPTION, "SVCJAVA", e.getMessage(), e.getClass().getName()));
        }
        return response;
    }
    public String generateRequestJSON(String endUserId, String referenceCode, String description, String currency, double amount, String code, String callbackURL,
            String clientCorrelator, String onBehalfOf, String purchaseCategoryCode, String channel, double taxAmount){
        String requestJSONString = null;

        try {
            /* START:Generating request JSON String */
            JSONObject chargeRequestJson = new JSONObject();
            chargeRequestJson.put("clientCorrelator", clientCorrelator);
            chargeRequestJson.put("endUserId", endUserId);
            //chargeRequestJson.put("notifyURL", callbackURL);

            JSONObject chargingInfo = new JSONObject();
            chargingInfo.put("amount", Double.toString(amount));
            chargingInfo.put("currency", currency);
            chargingInfo.put("description", description);

            JSONObject chargingMetaData = new JSONObject();
            chargingMetaData.put("onBehalfOf", onBehalfOf);
            chargingMetaData.put("purchaseCategoryCode", purchaseCategoryCode);
            chargingMetaData.put("channel", channel);
            chargingMetaData.put("taxAmount", Double.toString(taxAmount));

            JSONObject payAmount = new JSONObject();
            payAmount.put("chargingInformation", chargingInfo);
            payAmount.put("chargingMetaData", chargingMetaData);

            chargeRequestJson.put("paymentAmount", payAmount);
            chargeRequestJson.put("referenceCode", referenceCode);
            chargeRequestJson.put("transactionOperationStatus", "charged");

            requestJSONString = "{\"amountTransaction\":" + chargeRequestJson.toString()+ "}";
            /* END:Generating request JSON String */
        } catch (Exception e) {
            logger.error("Creating JSON Exception " + e.getMessage() + " " + e.getLocalizedMessage());
            //response.setRequestError(new RequestError(RequestError.SERVICEEXCEPTION, "SVCJAVA", e.getMessage(), e.getClass().getName()));
        }
        return requestJSONString;
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();

        ServiceEndpoints serviceEndpoints = CommandLineOptions.getServiceEndpoints(args);

        String username = CommandLineOptions.getUsername(args);
        String password = CommandLineOptions.getPassword(args);

        if (username == null) {
            username = "Fred.Jones";
        }
        if (password == null) {
            password = "1234";
        }
        if (serviceEndpoints == null) {
            serviceEndpoints = new ServiceEndpoints();
        }

        dumpRequestAndResponse = true;

        logger.debug("Demonstration of Payment API charging calls");

        logger.debug("Charging endpoint=" + serviceEndpoints.getAmountChargeEndpoint());

        String authorisationHeader = JSONRequest.getAuthorisationHeader(username, password);

        Charge me = new Charge(serviceEndpoints, authorisationHeader);

//        PaymentResponse chargeResponse = me.charge("tel:1234567890", "REF-12345", "Space Invaders", "USD", 2.5, "C100", "http://notaurl.com", "54321", "Amazing Apps", "Game", "Wap", 0.0);
//
//        if (chargeResponse != null) {
//            logger.debug("Charge response:\n" + chargeResponse.toString());
//        } else {
//            logger.debug("No response obtained");
//        }

        logger.debug("Refund endpoint=" + serviceEndpoints.getAmountRefundEndpoint());
        PaymentResponse refundResponse = me.refund("tel:1234567890", "REF-12345", "Space Invaders", "USD", 2.5, "C100", "54321", "ABC-123", "Amazing Apps", "Game", "Wap", 0.0, "SID1234", "PID8976");

        if (refundResponse != null) {
            logger.debug("Refund response:\n" + refundResponse.toString());
        } else {
            logger.debug("No response obtained");
        }

    }

    /**
     * @return the sessionAccountBalance
     */
    public String getSessionAccountBalance() {
        return sessionAccountBalance;
    }

    /**
     * @param sessionAccountBalance the sessionAccountBalance to set
     */
    public void setSessionAccountBalance(String sessionAccountBalance) {
        this.sessionAccountBalance = sessionAccountBalance;
    }
}
