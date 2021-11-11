package org.gsm.oneapi.sms;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

import org.apache.log4j.Logger;
import org.gsm.oneapi.endpoints.ServiceEndpoints;
import org.gsm.oneapi.foundation.FormParameters;
import org.gsm.oneapi.foundation.JSONRequest;
import org.gsm.oneapi.responsebean.RequestError;
import org.gsm.oneapi.responsebean.sms.SMSDeliveryReceiptSubscriptionResponse;
import org.gsm.oneapi.responsebean.sms.SMSSendDeliveryStatusResponse;
import org.gsm.oneapi.responsebean.sms.SMSSendResponse;
import org.gsm.oneapi.server.OneAPIServlet;
import org.json.JSONArray;
import org.json.JSONObject;

public class SMSSend {

    static Logger logger = Logger.getLogger(SMSSend.class);
    ServiceEndpoints endPoints = null;
    String authorisationHeader = null;
    public static boolean dumpRequestAndResponse = false;
    private static JSONRequest<SMSSendResponse> smsSendResponseProcessor = new JSONRequest<SMSSendResponse>(new SMSSendResponse());
    private static JSONRequest<SMSSendDeliveryStatusResponse> smsSendDeliveryStatusProcessor = new JSONRequest<SMSSendDeliveryStatusResponse>(new SMSSendDeliveryStatusResponse());
    private static JSONRequest<SMSDeliveryReceiptSubscriptionResponse> smsDeliveryReceiptSubscriptionProcessor = new JSONRequest<SMSDeliveryReceiptSubscriptionResponse>(new SMSDeliveryReceiptSubscriptionResponse());

    /**
     * Creates a new instance of the Send SMS API main interface. Requires
     * endPoints to define the URL targets of the various Send SMS network calls
     * and authorisationHeader containing the username/password used for HTTP
     * Basic authorisation with the OneAPI server.      *
     * @param endPoints contains a set of service/ call specific endpoints
     * @param authorisationHeader Base 64 encoded username/ password
     *
     * @see org.gsm.oneapi.endpoints.ServiceEndpoints
     * @see org.gsm.oneapi.foundation.JSONRequest#getAuthorisationHeader(String,
     * String)
     */
     public SMSSend(ServiceEndpoints endPoints, String authorisationHeader) {
         
        this.endPoints = endPoints;
        this.authorisationHeader = authorisationHeader;
    }

    /**
     * Can be used to update the service endpoints      *
     * @param endPoints contains a set of service/ call specific endpoints      *
     * @see org.gsm.oneapi.endpoints.ServiceEndpoints
     */
    public void setEndpoints(ServiceEndpoints endPoints) {
        this.endPoints = endPoints;
    }

    /**
     * Can be used to update the service authorisation header      *
     * @param authorisationHeader Base 64 encoded username/ password
     *
     * @see org.gsm.oneapi.foundation.JSONRequest#getAuthorisationHeader(String,
     * String)
     */
    public void setAuthorisationHeader(String authorisationHeader) {
        this.authorisationHeader = authorisationHeader;
    }

    /**
     * Send an SMS to one or more mobile terminals      *
     * @param senderAddress (mandatory) is the address to whom a responding SMS
     * may be sent. Do not URL encode this value prior to passing to this
     * function
     * @param address (mandatory) contains at least one address for each end
     * user ID to send to; in this case their MSISDN including the �tel:�
     * protocol identifier and the country code preceded by �+�. i.e.,
     * tel:+16309700001. OneAPI also supports the Anonymous Customer Reference
     * (ACR) if provided by the operator.
     * @param message (mandatory) contains the message text to send.Messages
     * over 160 characters may end up being sent as two or more messages by the
     * operator.
     * @param clientCorrelator (optional) uniquely identifies this create MMS
     * request. If there is a communication failure during the request, using
     * the same clientCorrelator when retrying the request allows the operator
     * to avoid sending the same MMS twice.
     * @param senderName (optional) is the name to appear on the user�s terminal
     * as the sender of the message
     * @param notifyURL (optional) is the URL to which you would like a
     * notification of delivery sent
     * @param callbackData (optional) will be passed back to the notifyURL
     * location, so you can use it to identify the message the receipt relates
     * to (or any other useful data, such as a function name)
     *
     * @see SMSSendResponse
     */
    public SMSSendResponse sendSMS(String requestJSONString) {
        
        SMSSendResponse response = new SMSSendResponse();

        String endpoint = endPoints.getSendSMSEndpoint();

        int responseCode = 0;
        String contentType = null;

        try {
//            if (dumpRequestAndResponse) {
//                JSONRequest.dumpRequestVariables(endpoint, authorisationHeader, formParameters);
//            }

            HttpURLConnection con = JSONRequest.setupConnection(endpoint, authorisationHeader);
            con.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());

            //String requestBody = JSONRequest.formEncodeParams(formParameters);
            String requestBody = requestJSONString;
            out.write(requestBody);
            out.close();

            responseCode = con.getResponseCode();
            contentType = con.getContentType();

            response = smsSendResponseProcessor.getResponse(con, OneAPIServlet.CREATED);
        } catch (Exception e) {
            response.setHTTPResponseCode(responseCode);
            response.setContentType(contentType);

            response.setRequestError(new RequestError(RequestError.SERVICEEXCEPTION, "SVCJAVA", e.getMessage(), e.getClass().getName()));

            logger.error("Exception " + e.getMessage() + " " + e.getLocalizedMessage());
        }
        return response;
    }
    
    public String generateJsonRequest(String senderAddress, String[] address, String message, String clientCorrelator, String notifyURL, String senderName, String callbackData) {
        
        String requestJSONString = null;

        try {
            /**
             * START:Generating request JSON String
             */            
            JSONObject objOutboundSMSMessageRequestJson = new JSONObject();
            
            JSONArray addressArray = new JSONArray();
            for (String addr : address) {
                
                if (addr != null && addr.length() > 0) {
                    
                    addressArray.put(addr);
                }                
            }
            
            objOutboundSMSMessageRequestJson.put("address", addressArray);
            objOutboundSMSMessageRequestJson.put("senderAddress", senderAddress);
            
            JSONObject objOutboundSMSTextMessageJson = new JSONObject();
            objOutboundSMSTextMessageJson.put("message", message);
            
            objOutboundSMSMessageRequestJson.put("outboundSMSTextMessage", objOutboundSMSTextMessageJson);
            objOutboundSMSMessageRequestJson.put("clientCorrelator", clientCorrelator);
            
            JSONObject objReceiptRequestJson = new JSONObject();
            objReceiptRequestJson.put("notifyURL", notifyURL);
            objReceiptRequestJson.put("callbackData", callbackData);
            
            objOutboundSMSMessageRequestJson.put("receiptRequest", objReceiptRequestJson);
            objOutboundSMSMessageRequestJson.put("senderName", senderName);
            
            requestJSONString = "{\"outboundSMSMessageRequest\":" + objOutboundSMSMessageRequestJson.toString() + "}";

            /**
             * END:Generating request JSON String
             */             
        } catch (Exception e) {
            
            logger.error("Creating JSON Exception " + e.getMessage() + " " + e.getLocalizedMessage());
        }   
        return requestJSONString;
    }

    /**
     * Query the delivery status for an SMS sent to one or more mobile terminals      *
     * @param senderAddress (mandatory) is the address from which SMS messages
     * are being sent. Do not URL encode this value prior to passing to this
     * function
     * @param requestId (mandatory) contains the requestId returned from a
     * previous call to the sendSMS function
     *
     * @see SMSSendDeliveryStatusResponse
     */
    public SMSSendDeliveryStatusResponse queryDeliveryStatus(String senderAddress, String requestId) {
        SMSSendDeliveryStatusResponse response = new SMSSendDeliveryStatusResponse();

        //if (senderAddress != null && requestId != null) {

            int responseCode = 0;
            String contentType = null;

            try {
                logger.debug("endpoint=" + endPoints.getQuerySMSDeliveryEndpoint());
                String endpoint = endPoints.getQuerySMSDeliveryEndpoint().replaceAll("\\{senderAddress\\}", URLEncoder.encode(senderAddress, "utf-8")).replaceAll("\\{requestId\\}", URLEncoder.encode(requestId, "utf-8"));

                /*if (dumpRequestAndResponse) {
                    JSONRequest.dumpRequestVariables(endpoint, authorisationHeader, null);
                }*/

                HttpURLConnection con = JSONRequest.setupConnection(endpoint, authorisationHeader);

                responseCode = con.getResponseCode();
                contentType = con.getContentType();

                response.setHTTPResponseCode(responseCode);
                response.setContentType(contentType);

                response = smsSendDeliveryStatusProcessor.getResponse(con, OneAPIServlet.OK);
            } catch (Exception e) {
                logger.error("Exception " + e.getMessage() + " " + e.getLocalizedMessage());
                e.printStackTrace();
                response.setHTTPResponseCode(responseCode);
                response.setContentType(contentType);

                response.setRequestError(new RequestError(RequestError.SERVICEEXCEPTION, "SVCJAVA", e.getMessage(), e.getClass().getName()));
            }
       // }
        return response;
    }

    /**
     * Start subscribing to delivery status notifications for all your sent SMS      *
     * @param senderAddress (mandatory) is the address from which SMS messages
     * are being sent. Do not URL encode this value prior to passing to this
     * function
     * @param notifyURL (mandatory) is the URL to which you would like a
     * notification of delivery sent
     * @param clientCorrelator (optional) uniquely identifies this subscription
     * request. If there is a communication failure during the request, using
     * the same clientCorrelator when retrying the request allows the operator
     * to avoid setting up the same subscription twice
     * @param callbackData (optional) will be passed back to the notifyURL
     * location, so you can use it to identify the message the delivery receipt
     * relates to (or any other useful data, such as a function name)
     *
     * @see SMSDeliveryReceiptSubscriptionResponse
     */
    public SMSDeliveryReceiptSubscriptionResponse subscribeToDeliveryNotifications(String senderAddress, String clientCorrelator, String notifyURL, String callbackData) {
        SMSDeliveryReceiptSubscriptionResponse response = new SMSDeliveryReceiptSubscriptionResponse();

        if (senderAddress != null && notifyURL != null) {
            FormParameters formParameters = new FormParameters();
            formParameters.put("clientCorrelator", clientCorrelator);
            formParameters.put("notifyURL", notifyURL);
            formParameters.put("callbackData", callbackData);

            int responseCode = 0;
            String contentType = null;

            try {
                String endpoint = endPoints.getSMSDeliverySubscriptionsEndpoint().replaceAll("\\{senderAddress\\}", URLEncoder.encode(senderAddress, "utf-8"));

                if (dumpRequestAndResponse) {
                    JSONRequest.dumpRequestVariables(endpoint, authorisationHeader, formParameters);
                }

                HttpURLConnection con = JSONRequest.setupConnection(endpoint, authorisationHeader);
                con.setDoOutput(true);
                OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());

                String requestBody = JSONRequest.formEncodeParams(formParameters);
                out.write(requestBody);
                out.close();

                responseCode = con.getResponseCode();
                contentType = con.getContentType();

                response = smsDeliveryReceiptSubscriptionProcessor.getResponse(con, OneAPIServlet.CREATED);
            } catch (Exception e) {
                response.setHTTPResponseCode(responseCode);
                response.setContentType(contentType);

                response.setRequestError(new RequestError(RequestError.SERVICEEXCEPTION, "SVCJAVA", e.getMessage(), e.getClass().getName()));
                logger.error("Exception " + e.getMessage() + " " + e.getLocalizedMessage());
            }
        }
        return response;
    }

    /**
     * Stop subscribing to delivery status notifications for all your sent SMS      *
     * @param subscriptionId (mandatory) contains the subscriptionId of a
     * previously created SMS delivery receipt subscription
     *
     */
    public int cancelDeliveryNotifications(String subscriptionId) {
        int responseCode = 0;

        if (subscriptionId != null) {

            try {
                logger.debug("endpoint=" + endPoints.getCancelSMSDeliverySubscriptionEndpoint());
                String endpoint = endPoints.getCancelSMSDeliverySubscriptionEndpoint().replaceAll("\\{subscriptionId\\}", URLEncoder.encode(subscriptionId, "utf-8"));

                if (dumpRequestAndResponse) {
                    JSONRequest.dumpRequestVariables(endpoint, authorisationHeader, null);
                }

                HttpURLConnection con = JSONRequest.setupConnection(endpoint, authorisationHeader);

                con.setRequestMethod("DELETE");

                responseCode = con.getResponseCode();
            } catch (Exception e) {
                logger.error("Exception " + e.getMessage() + " " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
        return responseCode;
    }

    /*public static void main(String[] args) {
        BasicConfigurator.configure();

        dumpRequestAndResponse = true;

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

        logger.debug("Demonstration of outbound SMS API");

        logger.debug("SMS send endpoint=" + serviceEndpoints.getSendSMSEndpoint());

        String authorisationHeader = JSONRequest.getAuthorisationHeader(username, password);

        //SMSSend me = new SMSSend(serviceEndpoints, authorisationHeader);

        //String[] sendTo = {"tel:123", "tel:124", "tel:125", "tel126"};
        //String notifyURL = "http://test.com/notifyThis/Message=" + JSONRequest.urlEncode("This is encoded");
        //SMSSendResponse sendResponse = me.sendSMS("tel:9876", sendTo, "welcome to the GSMA One API demonstration.", "ref12345", notifyURL, "ACME Inc.", "some-data-useful-to-the-requester");

        /*if (sendResponse != null) {
            logger.debug("Have SMS send response:\n" + sendResponse.toString());
        } else {
            logger.debug("No response obtained");
        }*/

        /*logger.debug("Query delivery status using endpoint=" + serviceEndpoints.getQuerySMSDeliveryEndpoint());

        SMSSendDeliveryStatusResponse deliveryStatus = me.queryDeliveryStatus("tel:987654", "ref2781398");
        if (deliveryStatus != null) {
            logger.debug("SMSSendDeliveryStatusResponse=" + deliveryStatus.toString());
        } else {
            logger.debug("No response obtained");
        }

        logger.debug("Register a delivery report receipt notification endpoint=" + serviceEndpoints.getSMSDeliverySubscriptionsEndpoint());

        /*SMSDeliveryReceiptSubscriptionResponse deliveryReceiptSubcription = me.subscribeToDeliveryNotifications("tel:12345", "ref12345", notifyURL, "subscription1");
        if (deliveryReceiptSubcription != null) {
            logger.debug("SMSDeliveryReceiptSubscriptionResponse=" + deliveryReceiptSubcription.toString());
        } else {
            logger.debug("No response obtained");
        }*/

        /*logger.debug("Cancel delivery receipt notification endpoint=" + serviceEndpoints.getCancelSMSDeliverySubscriptionEndpoint());

        int cancelSubscriptionResponse = me.cancelDeliveryNotifications("sub789");
        logger.debug("Cancel subscription response code=" + cancelSubscriptionResponse);

    }*/
}
