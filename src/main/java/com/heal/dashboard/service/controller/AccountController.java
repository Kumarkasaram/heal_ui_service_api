package com.heal.dashboard.service.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.heal.dashboard.service.beans.AccountBean;
import com.heal.dashboard.service.beans.ApplicationDetailBean;
import com.heal.dashboard.service.beans.ApplicationHealthDetail;
import com.heal.dashboard.service.beans.ApplicationHealthResponse;
import com.heal.dashboard.service.beans.DateComponentBean;
import com.heal.dashboard.service.beans.DateComponentDetailBean;
import com.heal.dashboard.service.beans.MasterFeatureDetails;
import com.heal.dashboard.service.beans.MasterFeaturesBean;
import com.heal.dashboard.service.beans.UserAccessAccountsBean;
import com.heal.dashboard.service.beans.UserAccessDetails;
import com.heal.dashboard.service.beans.UtilityBean;
import com.heal.dashboard.service.beans.topology.TopologyDetails;
import com.heal.dashboard.service.beans.topology.TopologyValidationResponseBean;
import com.heal.dashboard.service.businesslogic.ApplicationHealthBL;
import com.heal.dashboard.service.businesslogic.DateComponentBL;
import com.heal.dashboard.service.businesslogic.GetAccountsBL;
import com.heal.dashboard.service.businesslogic.GetApplicationBL;
import com.heal.dashboard.service.businesslogic.MasterFeaturesBL;
import com.heal.dashboard.service.businesslogic.TopologyServiceBL;
import com.heal.dashboard.service.exception.ClientException;
import com.heal.dashboard.service.exception.DataProcessingException;
import com.heal.dashboard.service.exception.ServerException;
import com.heal.dashboard.service.util.JsonFileParser;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Api(value = "Accounts")
public class AccountController {

    @Autowired
    JsonFileParser headersParser;
    @Autowired
    GetAccountsBL getAccountsBL;
    @Autowired
    GetApplicationBL getApplicationBL;
    @Autowired
    TopologyServiceBL topologyServiceBL;
    @Autowired
    ApplicationHealthBL applicationHealthBL;
    @Autowired
    DateComponentBL dateComponentBL;
    @Autowired
    MasterFeaturesBL masterFeaturesBL;

    @ApiOperation(value = "Retrieve accounts list", response = AccountBean.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully retrieved data"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
            @ApiResponse(code = 400, message = "Invalid Request")})
    @RequestMapping(value = "/accounts", method = RequestMethod.GET)
    public ResponseEntity<List<AccountBean>> getAccountList(@RequestHeader(value = "Authorization", required = false) String authorizationToken)
            throws ClientException, ServerException, DataProcessingException {

        UtilityBean<String> utilityBean = getAccountsBL.clientValidation(null, authorizationToken);
        UserAccessAccountsBean userAccessBean = getAccountsBL.serverValidation(utilityBean);
        List<AccountBean> accounts = getAccountsBL.process(userAccessBean);

        return ResponseEntity.ok().headers(headersParser.loadHeaderConfiguration()).body(accounts);
    }

    @ApiOperation(value = "Retrieve applications List", response = ApplicationDetailBean.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully retrieved data"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
            @ApiResponse(code = 400, message = "Invalid Request")})
    @RequestMapping(value = "/accounts/{identifier}/applications", method = RequestMethod.GET)
    public ResponseEntity<List<ApplicationDetailBean>> getApplicationList(@RequestHeader(value = "Authorization") String authorizationToken, @PathVariable("identifier") String identifier)
            throws ClientException, ServerException, DataProcessingException {
        UtilityBean<String> utilityBean = getApplicationBL.clientValidation(null, authorizationToken, identifier);
        UserAccessDetails userAccessDetails = getApplicationBL.serverValidation(utilityBean);
        List<ApplicationDetailBean> applicationDetailBean = getApplicationBL.process(userAccessDetails);

        return ResponseEntity.ok().headers(headersParser.loadHeaderConfiguration()).body(applicationDetailBean);
    }

    @ApiOperation(value = "Retrieve TopologyDetails", response = TopologyDetails.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully retrieved data"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
            @ApiResponse(code = 400, message = "Invalid Request")})
    @RequestMapping(value = "/accounts/{identifier}/topology", method = RequestMethod.GET)
    public ResponseEntity<TopologyDetails> getTopologyDetails(@RequestHeader(value = "Authorization") String
                                                                          authorizationToken, @PathVariable("identifier") String
                                                                      identifier, @RequestParam(value = "applicationId", required = false) String applicationId)
            throws ClientException, ServerException, DataProcessingException {

        UtilityBean<String> utilityBean = topologyServiceBL.clientValidation(null, authorizationToken, identifier, applicationId);
        TopologyValidationResponseBean topologyValidationResponseBean = topologyServiceBL.serverValidation(utilityBean);
        TopologyDetails response = topologyServiceBL.process(topologyValidationResponseBean);

        return ResponseEntity.ok().headers(headersParser.loadHeaderConfiguration()).body(response);
    }
    
    @ApiOperation(value = "Retrieve features List", response = AccountBean.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully retrieved data"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
            @ApiResponse(code = 400, message = "Invalid Request")})
    @RequestMapping(value = "/features", method = RequestMethod.GET)
    public ResponseEntity<MasterFeatureDetails> getMasterFeatures() throws ClientException, ServerException, DataProcessingException {         
              UtilityBean<String> utilityBean = masterFeaturesBL.clientValidation(null,"");
              List<MasterFeaturesBean> masterFeaturesBeans =masterFeaturesBL.serverValidation(utilityBean);
              MasterFeatureDetails response = masterFeaturesBL.process(masterFeaturesBeans);
              return ResponseEntity.ok().headers(headersParser.loadHeaderConfiguration()).body(response);

    }
    @ApiOperation(value = "Retrieve Date Component Drop Down List", response = AccountBean.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully retrieved data"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
            @ApiResponse(code = 400, message = "Invalid Request")})
    @RequestMapping(value = "/date-components", method = RequestMethod.GET)
    public ResponseEntity<DateComponentDetailBean> getDateTimeDropdownList() throws ServerException, ClientException, DataProcessingException {
              UtilityBean<String> utilityBean = dateComponentBL.clientValidation(null,"");
              List<DateComponentBean> dateComponentBeans =dateComponentBL.serverValidation(utilityBean);
              DateComponentDetailBean response = dateComponentBL.process(dateComponentBeans);
              return ResponseEntity.ok().headers(headersParser.loadHeaderConfiguration()).body(response);
    }
    
    @ApiOperation(value = "Retrieve Application Health", response = AccountBean.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully retrieved data"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
            @ApiResponse(code = 400, message = "Invalid Request")})
    @RequestMapping(value = "/accounts/{identifier}/application-health", method = RequestMethod.GET)
    public ResponseEntity< List<ApplicationHealthDetail>> getApplicationHealthStatus(@RequestHeader(value = "Authorization") String authorizationToken,@PathVariable(value = "identifier") String identifier,
                                                                              @RequestParam(value = "toTime") String toTimeString) throws ClientException, ServerException, DataProcessingException {
              UtilityBean<String> utilityBean = applicationHealthBL.clientValidation(null,identifier,toTimeString,authorizationToken);
              ApplicationHealthResponse applicationResponseBean = applicationHealthBL.serverValidation(utilityBean);
              List<ApplicationHealthDetail>  response  = applicationHealthBL.process(applicationResponseBean);
              return ResponseEntity.ok().headers(headersParser.loadHeaderConfiguration()).body(response);
    }
}
