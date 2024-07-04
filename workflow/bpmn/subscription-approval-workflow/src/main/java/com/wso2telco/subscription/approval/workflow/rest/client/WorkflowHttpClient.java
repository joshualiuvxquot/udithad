package com.wso2telco.subscription.approval.workflow.rest.client;

import com.wso2telco.subscription.approval.workflow.exception.SubscriptionApprovalWorkflowException;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface WorkflowHttpClient {
	@RequestLine("POST site/blocks/workflow/workflow-listener/ajax/workflow-listener.jag?workflowReference={workflowRefId}&status={status}")
	@Headers("Content-Type: application/x-www-form-urlencoded")
	void invokeCallback (@Param("workflowRefId") String workflowRefId, @Param("status") String status) throws
	                                                                                                   SubscriptionApprovalWorkflowException;
}
