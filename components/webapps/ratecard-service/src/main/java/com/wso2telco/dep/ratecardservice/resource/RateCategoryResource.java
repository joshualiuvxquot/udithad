package com.wso2telco.dep.ratecardservice.resource;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.wso2telco.core.dbutils.exception.BusinessException;
import com.wso2telco.core.dbutils.exception.ServiceError;
import com.wso2telco.dep.ratecardservice.dao.model.ErrorDTO;
import com.wso2telco.dep.ratecardservice.dao.model.RateCategoryDTO;
import com.wso2telco.dep.ratecardservice.service.RateCategoryService;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RateCategoryResource {

	private final Log log = LogFactory.getLog(RateCategoryResource.class);
	private RateCategoryService rateCategoryService = new RateCategoryService();
	
	@POST
	public Response addRateCategory(@PathParam("rateDefId") int rateDefId, RateCategoryDTO rateCategory) {
		
		RateCategoryDTO newRateCategory = null;
		Status responseCode = null;
		Object responseString = null;

		try {

			newRateCategory = rateCategoryService.addRateCategory(rateCategory);

			if (newRateCategory != null) {

				responseString = newRateCategory;
				responseCode = Response.Status.CREATED;
			} else {

				log.error("Error in RateCategoryResource addRateCategory : rate category can not insert to database ");
				throw new BusinessException(ServiceError.SERVICE_ERROR_OCCURED);
			}
		} catch (Exception e) {

			ErrorDTO errorDTO = new ErrorDTO();
			ErrorDTO.ServiceException serviceException = new ErrorDTO.ServiceException();

			if (e instanceof BusinessException) {

				BusinessException be = (BusinessException) e;
				serviceException.setMessageId(be.getErrorType().getCode());
				serviceException.setText(be.getErrorType().getMessage());
				errorDTO.setServiceException(serviceException);
			}

			responseCode = Response.Status.BAD_REQUEST;
			responseString = errorDTO;
		}

		log.debug("RateCategoryResource addRateCategory -> response code : " + responseCode);
		log.debug("RateCategoryResource addRateCategory -> response body : " + responseString);

		return Response.status(responseCode).entity(responseString).build();
	}
	
	@GET
	public Response getRateCategories(@PathParam("rateDefId") int rateDefId) {

		List<RateCategoryDTO> rateCategories = null;
		Status responseCode = null;
		Object responseString = null;

		try {

			rateCategories = rateCategoryService.getRateCategories(rateDefId);

			if (!rateCategories.isEmpty()) {

				responseString = rateCategories;
				responseCode = Response.Status.OK;
			} else {

				log.error(
						"Error in RateCategoryResource getRateCategories : rate categories are not found in database ");
				throw new BusinessException(ServiceError.NO_RESOURCES);
			}
		} catch (BusinessException e) {

			ErrorDTO errorDTO = new ErrorDTO();
			ErrorDTO.ServiceException serviceException = new ErrorDTO.ServiceException();

			serviceException.setMessageId(e.getErrorType().getCode());
			serviceException.setText(e.getErrorType().getMessage());
			errorDTO.setServiceException(serviceException);

			responseCode = Response.Status.NOT_FOUND;
			responseString = errorDTO;
		} catch (Exception e) {

			ErrorDTO errorDTO = new ErrorDTO();
			ErrorDTO.ServiceException serviceException = new ErrorDTO.ServiceException();

			if (e instanceof BusinessException) {

				BusinessException be = (BusinessException) e;
				serviceException.setMessageId(be.getErrorType().getCode());
				serviceException.setText(be.getErrorType().getMessage());
				errorDTO.setServiceException(serviceException);
			}

			responseCode = Response.Status.BAD_REQUEST;
			responseString = errorDTO;
		}

		log.debug("RateCategoryResource getRateCategories -> response code : " + responseCode);
		log.debug("RateCategoryResource getRateCategories -> response body : " + responseString);

		return Response.status(responseCode).entity(responseString).build();
	}
}
