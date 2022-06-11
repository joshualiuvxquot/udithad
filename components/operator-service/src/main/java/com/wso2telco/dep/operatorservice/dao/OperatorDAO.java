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
package com.wso2telco.dep.operatorservice.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import javax.cache.Cache;
import javax.cache.Caching;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import com.wso2telco.dbutils.AxataDBUtilException;
import com.wso2telco.dbutils.DbUtils;
import com.wso2telco.dbutils.util.DataSourceNames;
import com.wso2telco.dep.operatorservice.model.Operator;
import com.wso2telco.dep.operatorservice.model.OperatorSearchDTO;
import com.wso2telco.dep.operatorservice.util.OparatorTable;
import com.wso2telco.dep.operatorservice.model.OperatorApplicationDTO;
import com.wso2telco.dep.operatorservice.model.OperatorEndPointDTO;

// TODO: Auto-generated Javadoc
/**
 * The Class DAO.
 */
public class OperatorDAO {

	/** The Constant log. */
	private final Log log = LogFactory.getLog(OperatorDAO.class);

	private static final String MEDIATOR_CACHE_MANAGER = "MediatorCacheManager";

	/**
	 * Retrieve operator list.
	 *
	 * @return the list
	 * @throws APIManagementException
	 *             the API management exception
	 * @throws APIMgtUsageQueryServiceClientException
	 *             the API mgt usage query service client exception
	 */
	public List<Operator> retrieveOperatorList() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Operator> operatorList = new ArrayList<Operator>();

		try {
			conn = DbUtils.getDbConnection(DataSourceNames.WSO2TELCO_DEP_DB);

			String query = "SELECT ID, operatorname, description from operators";
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();

			while (rs.next()) {
				Operator operator = new Operator();
				operator.setOperatorId(rs.getInt("ID"));
				operator.setOperatorName(rs.getString("operatorname"));
				operator.setOperatorDescription(rs.getString("description"));

				operatorList.add(operator);
			}

		} catch (Exception e) {
			log.error("", e);
			throw e;
		} finally {
			DbUtils.closeAllConnections(ps, conn, rs);
		}
		return operatorList;
	}

	/**
	 * Persist operators.
	 *
	 * @param apiName
	 *            the api name
	 * @param apiVersion
	 *            the api version
	 * @param apiProvider
	 *            the api provider
	 * @param appId
	 *            the app id
	 * @param operatorList
	 *            the operator list
	 * @throws APIManagementException
	 *             the API management exception
	 * @throws APIMgtUsageQueryServiceClientException
	 *             the API mgt usage query service client exception
	 */
	public void persistOperators(String apiName, String apiVersion, String apiProvider, int appId, String operatorList)
			throws Exception {

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = DbUtils.getDbConnection(DataSourceNames.WSO2TELCO_DEP_DB);

			StringBuilder query = new StringBuilder();
			query.append("INSERT INTO ");
			query.append(OparatorTable.SUB_APPROVAL_OPERATORS.getTObject());
			query.append(" (API_NAME, API_VERSION, API_PROVIDER, APP_ID, OPERATOR_LIST) ");
			query.append("VALUES (?, ?, ?, ?, ?)");

			ps = conn.prepareStatement(query.toString());
			ps.setString(1, apiName);
			ps.setString(2, apiVersion);
			ps.setString(3, apiProvider);
			ps.setInt(4, appId);
			ps.setString(5, operatorList);
			ps.execute();

		} catch (Exception e) {
			log.error("persistOperators", e);
			throw e;

		} finally {
			DbUtils.closeAllConnections(ps, conn, null);
		}
	}

	public List<Operator> seachOparators(OperatorSearchDTO searchDTO) throws Exception {

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Operator> operatorList = new ArrayList<Operator>();

		try {
			conn = DbUtils.getDbConnection(DataSourceNames.WSO2TELCO_DEP_DB);
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT ID, operatorname, description ");
			sql.append(" FROM ").append(OparatorTable.OPARATOR.getTObject());
			sql.append(" WHERE 1=1 ");

			if (searchDTO.getName() != null && searchDTO.getName().trim().length() > 0) {
				sql.append(" AND operatorname").append(" =?");
			}

			log.debug(" seachOparators : " + sql);

			if (searchDTO.hasName()) {
				sql.append(" AND operatorname").append(" =?");
			}
			ps = conn.prepareStatement(sql.toString());

			if (searchDTO.hasName()) {
				ps.setString(1, searchDTO.getName().trim());
			}

			rs = ps.executeQuery();

			while (rs.next()) {
				Operator operator = new Operator();
				operator.setOperatorId(rs.getInt("ID"));
				operator.setOperatorName(rs.getString("operatorname"));
				operator.setOperatorDescription(rs.getString("description"));

				operatorList.add(operator);
			}

		} catch (SQLException e) {
			throw new Exception(e);
		} finally {
			DbUtils.closeAllConnections(ps, conn, rs);
		}
		return operatorList;
	}

	public void insertBlacklistAggregatoRows(final Integer appID, final String subscriber, final int operatorid,
			final String[] merchants) throws Exception {
		log.debug(" ");
		Connection con = null;
		final StringBuilder sql = new StringBuilder();
		PreparedStatement pst = null;

		try {
			con = DbUtils.getDbConnection(DataSourceNames.WSO2TELCO_DEP_DB);

			sql.append(" INSERT INTO ");
			sql.append(OparatorTable.MERCHANT_OPCO_BLACKLIST.getTObject());
			sql.append(" (application_id, operator_id, subscriber, merchant)");
			sql.append("VALUES (?, ?, ?, ?) ");

			pst = con.prepareStatement(sql.toString());

			/**
			 * Set autocommit off to handle the transaction
			 */
			con.setAutoCommit(false);

			/**
			 * each merchant log as black listed
			 */
			for (String merchant : merchants) {

				if (appID == null) {
					pst.setNull(1, Types.INTEGER);
				} else {
					pst.setInt(1, appID);
				}
				pst.setInt(2, operatorid);
				pst.setString(3, subscriber);
				pst.setString(4, merchant);
				pst.addBatch();
			}

			pst.executeBatch();

			/**
			 * commit the transaction if all success
			 */
			con.commit();

		} catch (Exception e) {
			log.error("DAO.blacklistAggregator ", e);
			/**
			 * rollback if Exception occurs
			 */
			con.rollback();

			/**
			 * throw it into upper layer
			 */
			throw e;
		} finally {

			DbUtils.closeAllConnections(pst, con, null);
		}
	}

	/**
	 * Operator endPoints.
	 *
	 * @return the list
	 * @throws Exception
	 *             the exception
	 */
	public List<OperatorEndPointDTO> getOperatorEndpoints() throws Exception {

		final int opEndpointsID = 0;

		Cache<Integer, List<OperatorEndPointDTO>> cache = Caching.getCacheManager(MEDIATOR_CACHE_MANAGER)
				.getCache("dbOperatorEndpoints");
		List<OperatorEndPointDTO> endPoints = cache.get(opEndpointsID);

		if (endPoints == null) {

			Connection con = DbUtils.getDbConnection(DataSourceNames.WSO2TELCO_DEP_DB);
			PreparedStatement ps = null;
			ResultSet rs = null;
			endPoints = new ArrayList<OperatorEndPointDTO>();

			try {

				if (con == null) {

					throw new Exception("Connection not found");
				}

				StringBuilder queryString = new StringBuilder("SELECT operatorid, operatorname, api, endpoint ");
				queryString.append("FROM operatorendpoints, operators ");
				queryString.append("WHERE operatorendpoints.operatorid = operators.id");

				ps = con.prepareStatement(queryString.toString());

				rs = ps.executeQuery();

				while (rs.next()) {

					endPoints.add(new OperatorEndPointDTO(rs.getInt("operatorid"), rs.getString("operatorname"),
							rs.getString("api"), rs.getString("endpoint")));
				}
			} catch (Exception e) {

				DbUtils.handleException("Error while retrieving operator endpoint. ", e);
			} finally {

				DbUtils.closeAllConnections(ps, con, rs);
			}
			if (!endPoints.isEmpty()) {

				cache.put(opEndpointsID, endPoints);
			}
		}

		return endPoints;
	}

	/**
	 * Application operators.
	 *
	 * @param applicationId
	 *            the applicationId
	 * @return the list
	 * @throws Exception
	 *             the exception
	 */
	public List<OperatorApplicationDTO> getApplicationOperators(Integer applicationId) throws Exception {

		Connection con = DbUtils.getDbConnection(DataSourceNames.WSO2TELCO_DEP_DB);
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<OperatorApplicationDTO> operators = new ArrayList<OperatorApplicationDTO>();

		try {

			if (con == null) {

				throw new Exception("Connection not found");
			}

			StringBuilder queryString = new StringBuilder(
					"SELECT oa.id id, oa.applicationid, oa.operatorid, o.operatorname, o.refreshtoken, o.tokenvalidity, o.tokentime, o.token, o.tokenurl, o.tokenauth ");
			queryString.append("FROM operatorapps oa, operators o ");
			queryString.append("WHERE oa.operatorid = o.id AND oa.isactive = 1  AND oa.applicationid = ?");

			ps = con.prepareStatement(queryString.toString());

			ps.setInt(1, applicationId);

			rs = ps.executeQuery();

			while (rs.next()) {

				OperatorApplicationDTO oper = new OperatorApplicationDTO();
				oper.setId(rs.getInt("id"));
				oper.setApplicationid(rs.getInt("applicationid"));
				oper.setOperatorid(rs.getInt("operatorid"));
				oper.setOperatorname(rs.getString("operatorname"));
				oper.setRefreshtoken(rs.getString("refreshtoken"));
				oper.setTokenvalidity(rs.getLong("tokenvalidity"));
				oper.setTokentime(rs.getLong("tokentime"));
				oper.setToken(rs.getString("token"));
				oper.setTokenurl(rs.getString("tokenurl"));
				oper.setTokenauth(rs.getString("tokenauth"));
				operators.add(oper);
			}
		} catch (Exception e) {

			DbUtils.handleException("Error while selecting from operatorapps, operators. ", e);
		} finally {

			DbUtils.closeAllConnections(ps, con, rs);
		}

		return operators;
	}

	/**
	 * Active application operators.
	 *
	 * @param appId
	 *            the appId
	 * @param apiType
	 *            the apiType
	 * @return the list
	 * @throws SQLException
	 *             the SQL exception
	 * @throws AxataDBUtilException
	 *             the AxataDBUtilException
	 */
	public List<Integer> getActiveApplicationOperators(Integer appId, String apiType)
			throws SQLException, AxataDBUtilException {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Integer> operators = new ArrayList<Integer>();

		try {

			con = DbUtils.getDbConnection(DataSourceNames.WSO2TELCO_DEP_DB);
			if (con == null) {

				throw new Exception("Connection not found.");
			}

			StringBuilder queryString = new StringBuilder("SELECT o.operatorid ");
			queryString.append("FROM endpointapps e, operatorendpoints o ");
			queryString.append("WHERE o.id = e.endpointid AND e.applicationid = ?");
			queryString.append(" AND e.isactive = 1 AND o.api = ?");

			ps = con.prepareStatement(queryString.toString());

			ps.setInt(1, appId);
			ps.setString(2, apiType);

			log.debug("getActiveApplicationOperators : " + queryString.toString());

			rs = ps.executeQuery();

			while (rs.next()) {

				Integer operatorid = (rs.getInt("operatorid"));
				operators.add(operatorid);
			}
		} catch (Exception e) {

			DbUtils.handleException("Error while selecting from endpointapps, operatorendpoints ", e);
		} finally {

			DbUtils.closeAllConnections(ps, con, rs);
		}

		return operators;
	}

	/**
	 * Token update.
	 *
	 * @param id
	 *            the id
	 * @param refreshToken
	 *            the refreshToken
	 * @param tokenValidity
	 *            the tokenValidity
	 * @param tokenTime
	 *            the tokenTime
	 * @param token
	 *            the token
	 * @return the integer
	 * @throws Exception
	 *             the exception
	 */
	public Integer updateOperatorToken(int id, String refreshToken, long tokenValidity, long tokenTime, String token)
			throws Exception {

		Connection con = null;
		PreparedStatement ps = null;
		Integer newid = 0;

		try {

			con = DbUtils.getDbConnection(DataSourceNames.WSO2TELCO_DEP_DB);
			if (con == null) {

				throw new Exception("Connection not found");
			}

			StringBuilder queryString = new StringBuilder("UPDATE operators ");
			queryString.append("SET refreshtoken = ?");
			queryString.append(" ,tokenvalidity = ?");
			queryString.append(" ,tokentime = ?");
			queryString.append(" ,token = ?");
			queryString.append(" WHERE id = ?");

			ps = con.prepareStatement(queryString.toString());

			ps.setString(1, refreshToken);
			ps.setLong(2, tokenValidity);
			ps.setLong(3, tokenTime);
			ps.setString(4, token);
			ps.setInt(5, id);

			ps.executeUpdate();
		} catch (Exception e) {

			DbUtils.handleException("Error while updating operators. ", e);
		} finally {

			DbUtils.closeAllConnections(ps, con, null);
		}

		return newid;
	}
}
