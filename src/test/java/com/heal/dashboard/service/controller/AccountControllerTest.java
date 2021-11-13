package com.heal.dashboard.service.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.heal.dashboard.service.beans.*;
import com.heal.dashboard.service.businesslogic.GetApplicationBL;
import com.heal.dashboard.service.businesslogic.GetAccountsBL;
import com.heal.dashboard.service.businesslogic.ServiceDetailsBL;
import com.heal.dashboard.service.businesslogic.TopologyServiceBL;
import com.heal.dashboard.service.exception.ClientException;
import com.heal.dashboard.service.exception.DataProcessingException;
import com.heal.dashboard.service.exception.ServerException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.heal.dashboard.service.beans.topology.Edges;
import com.heal.dashboard.service.beans.topology.Nodes;
import com.heal.dashboard.service.beans.topology.TopologyDetails;

@RunWith(SpringRunner.class)
public class AccountControllerTest {

    @InjectMocks
    AccountController accountcontroller;
    @InjectMocks
    ServicesController servicesController;
    @MockBean
    GetAccountsBL getAccountsBL;
    @MockBean
    GetApplicationBL getApplicationBL;
    @MockBean
    ServiceDetailsBL serviceDetailsBL;
    @Mock
    List<AccountBean> accountBeansList;
    @Mock
    TopologyServiceBL topologyServiceBL;
    @Mock
    UserAccessBean userAccessDetails;

    TopologyDetails topologyDetails;

    List<ApplicationDetailBean> applicationDetailBeanList;

    UserAccessAccountsBean accessAccountsBean;

    @Before
    public void setup() {
        accountBeansList = new ArrayList<>();
        AccountBean accountBean = new AccountBean();
        accountBean.setAccountId(2);
        accountBean.setIdentifier("qa-d681ef13-d690-4917-jkhg-6c79b-1");
        accountBean.setName("India");
        accountBeansList.add(accountBean);

        topologyDetails = new TopologyDetails();
        List<Nodes> nodeslist = new ArrayList<Nodes>();
        List<Edges> edgeslist = new ArrayList<>();

        Nodes nodes = new Nodes();
        nodes.setId("1");
        nodes.setIdentifier("qa-d681ef13-d690-4917-jkhg-6c79b-1");
        nodes.setName("test");
        nodes.setStartNode(true);
        nodeslist.add(nodes);

        Edges edges = new Edges();
        edges.setSource("test");
        edges.setTarget("qa-d681ef13-d690-4917-jkhg-6c79b-1");
        edges.setData(new HashMap<String, String>());
        edgeslist.add(edges);

        applicationDetailBeanList = new ArrayList<>();
        ApplicationDetailBean applicationDetailBean = new ApplicationDetailBean();
        applicationDetailBean.setId(1);
        applicationDetailBean.setIdentifier("qa-d681ef13-d690-4917-jkhg-6c79b-1");
        applicationDetailBean.setName("test");
        applicationDetailBeanList.add(applicationDetailBean);

        //setting up mock data in  userAccessDetails
        userAccessDetails = new UserAccessBean();
        userAccessDetails.setAccessDetails("{\"accounts\": [\"*\"]}");
        userAccessDetails.setId(1);
        userAccessDetails.setUpdatedTime(LocalDateTime.now());
        userAccessDetails.setCreatedTime(LocalDateTime.now());
        userAccessDetails.setUserIdentifier("7640123a-fbde-4fe5-9812-581cd1e3a9c1");
        accessAccountsBean = new UserAccessAccountsBean(new Gson().fromJson(userAccessDetails.getAccessDetails(), AccountMappingBean.class), accountBeansList);
    }

    @Test(expected = RuntimeException.class)
    public void getAccountListTest_InternalServerErrror() throws ServerException, ClientException, DataProcessingException {
        Mockito.when(accountcontroller.getAccountList(Mockito.anyString())).thenThrow(new RuntimeException());
        accountcontroller.getAccountList("tetsing");
    }

    @Test
    public void getAccountListTest_Ok() throws Exception {
        ResponseEntity<List<AccountBean>> responseEntity = ResponseEntity.ok(accountBeansList);
        Mockito.when(accountcontroller.getAccountList(Mockito.anyString())).thenReturn(responseEntity);
        Assert.assertEquals(HttpStatus.OK, accountcontroller.getAccountList("testing").getStatusCode());
    }

    @Test
    public void getApplicationList_OK() throws Exception {
        ResponseEntity<List<ApplicationDetailBean>> responseEntity = ResponseEntity.ok(applicationDetailBeanList);
        Mockito.when(accountcontroller.getApplicationList(Mockito.anyString(), Mockito.anyString())).thenReturn(responseEntity);
        Assert.assertEquals(HttpStatus.OK, accountcontroller.getApplicationList("testing", "identifier").getStatusCode());
    }

    @Test
    public void getServiceDetails() throws Exception {
        ResponseEntity<TopologyDetails> responseEntity = ResponseEntity.ok(topologyDetails);
        Mockito.when(servicesController.getServiceDetails(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(responseEntity);
        Assert.assertEquals(HttpStatus.OK, servicesController.getServiceDetails("testing", "", "", "").getStatusCode());
    }

    @Test
    public void getTopologyDetails() throws Exception {
        ResponseEntity<TopologyDetails> responseEntity = ResponseEntity.ok(topologyDetails);
        Mockito.when(accountcontroller.getTopologyDetails(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(responseEntity);
        Assert.assertEquals(HttpStatus.OK, accountcontroller.getTopologyDetails("testing", "", "").getStatusCode());
    }

    @Test
    public void getAccountList_Success() throws Exception {
        Mockito.when(getAccountsBL.clientValidation(Mockito.any())).thenReturn(UtilityBean.<String>builder().pojoObject("7640123a-fbde-4fe5-9812-581cd1e3a9c1").build());
        Mockito.when(getAccountsBL.serverValidation(Mockito.any())).thenReturn(accessAccountsBean);
        Mockito.when(getAccountsBL.process(Mockito.any())).thenReturn(accountBeansList);
        Assert.assertEquals("India", accountcontroller.getAccountList("7640123a-fbde-4fe5-9812-581cd1e3a9c1").getBody().get(0).getName());
    }

    @Test(expected = Exception.class)
    public void getAccountList_BadRequestException() throws Exception {
        Mockito.when(getAccountsBL.clientValidation(Mockito.any())).thenThrow(new ClientException("authentication failed"));
        Assert.assertNull(accountcontroller.getAccountList(null));
    }

    @Test(expected = RuntimeException.class)
    public void getAccountList_InternalServerError() throws Exception {
        Mockito.when(getAccountsBL.clientValidation(Mockito.any())).thenReturn(UtilityBean.<String>builder().pojoObject("7640123a-fbde-4fe5-9812-581cd1e3a9c1").build());
        Mockito.when(getAccountsBL.serverValidation(Mockito.any())).thenReturn(accessAccountsBean);
        Mockito.when(getAccountsBL.process(Mockito.any())).thenThrow(new RuntimeException());
        Assert.assertNull(accountcontroller.getAccountList("7640123a-fbde-4fe5-9812-581cd1e3a9c1"));
    }

    @Test
    public void getApplicationList_Success() throws Exception {
        applicationDetailBeanList = new ArrayList<>();
        ApplicationDetailBean applicationDetailBean = new ApplicationDetailBean();
        applicationDetailBean.setId(1);
        applicationDetailBean.setIdentifier("7640123a-fbde-4fe5-9812-581cd1e3a9c1");
        applicationDetailBean.setName("test");
        applicationDetailBeanList.add(applicationDetailBean);
        Mockito.when(getApplicationBL.clientValidation(Mockito.any())).thenReturn(UtilityBean.<String>builder().pojoObject("7640123a-fbde-4fe5-9812-581cd1e3a9c1").build());
        Mockito.when(getApplicationBL.serverValidation(Mockito.any())).thenReturn(new UserAccessDetails());
        Mockito.when(getApplicationBL.process(Mockito.any())).thenReturn(applicationDetailBeanList);
        Assert.assertEquals("test", accountcontroller.getApplicationList("7640123a-fbde-4fe5-9812-581cd1e3a9c1", "").getBody().get(0).getName());
    }
}
