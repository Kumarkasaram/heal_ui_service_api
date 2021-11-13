package com.heal.dashboard.service.controller;


import com.heal.dashboard.service.beans.*;
import com.heal.dashboard.service.beans.topology.TopologyDetails;
import com.heal.dashboard.service.beans.topology.TopologyValidationResponseBean;
import com.heal.dashboard.service.businesslogic.GetAccountsBL;
import com.heal.dashboard.service.businesslogic.GetApplicationBL;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
