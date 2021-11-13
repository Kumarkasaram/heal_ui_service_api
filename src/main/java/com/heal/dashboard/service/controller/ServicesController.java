package com.heal.dashboard.service.controller;

import com.heal.dashboard.service.beans.UtilityBean;
import com.heal.dashboard.service.beans.topology.TopologyDetails;
import com.heal.dashboard.service.beans.topology.TopologyValidationResponseBean;
import com.heal.dashboard.service.businesslogic.ServiceDetailsBL;
import com.heal.dashboard.service.exception.ClientException;
import com.heal.dashboard.service.exception.DataProcessingException;
import com.heal.dashboard.service.exception.ServerException;
import com.heal.dashboard.service.util.JsonFileParser;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class ServicesController {

    @Autowired
    JsonFileParser headersParser;
    @Autowired
    ServiceDetailsBL serviceDetailsBL;

    @ApiOperation(value = "Retrieve Service Details", response = TopologyDetails.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully retrieved data"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
            @ApiResponse(code = 400, message = "Invalid Request")})
    @RequestMapping(value = "/accounts/{identifier}/services/{serviceId}/topology/{ndegree}", method = RequestMethod.GET)
    public ResponseEntity<TopologyDetails> getServiceDetails(@RequestHeader(value = "Authorization", required = false) String authorizationToken, @PathVariable("identifier") String identifier, @PathVariable("serviceId") String
            serviceId, @PathVariable("ndegree") String ndegree)
            throws ClientException, ServerException, DataProcessingException {

        UtilityBean<String> utilityBean = serviceDetailsBL.clientValidation(null, authorizationToken, identifier, serviceId, ndegree);
        TopologyValidationResponseBean topologyValidationResponseBean = serviceDetailsBL.serverValidation(utilityBean);
        TopologyDetails response = serviceDetailsBL.process(topologyValidationResponseBean);

        return ResponseEntity.ok().headers(headersParser.loadHeaderConfiguration()).body(response);
    }
}
