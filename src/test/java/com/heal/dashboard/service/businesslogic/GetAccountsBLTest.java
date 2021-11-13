package com.heal.dashboard.service.businesslogic;


import com.google.gson.Gson;
import com.heal.dashboard.service.dao.mysql.AccountDao;
import com.heal.dashboard.service.beans.*;
import com.heal.dashboard.service.exception.ClientException;
import com.heal.dashboard.service.exception.ServerException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
public class GetAccountsBLTest {

	@InjectMocks
    GetAccountsBL  getAccountBL;
    @Mock
    AccountDao jdbcTemplateDao;
   
    @Mock
    List<AccountBean> accountBeansList;
    @Mock
    UserAccessBean userAccessDetails;
    
    UserAccessAccountsBean accessAccountsBean;
    
    @Before
    public void setup() {
//      setting up mock data in  accountBean
        accountBeansList = new ArrayList<>();
        AccountBean  accountBean = new AccountBean();
        accountBean.setAccountId(2);
        accountBean.setIdentifier("qa-d681ef13-d690-4917-jkhg-6c79b-1");
        accountBean.setName("India");
        accountBeansList.add(accountBean);

//      setting up mock data in  userAccessDetails
        userAccessDetails = new UserAccessBean();
        userAccessDetails.setAccessDetails("{\"accounts\": [\"*\"]}");
        userAccessDetails.setId(1);
        userAccessDetails.setUpdatedTime(LocalDateTime.now());
        userAccessDetails.setCreatedTime(LocalDateTime.now());
        userAccessDetails.setUserIdentifier("7640123a-fbde-4fe5-9812-581cd1e3a9c1");
        accessAccountsBean = new UserAccessAccountsBean(new Gson().fromJson(userAccessDetails.getAccessDetails(), AccountMappingBean.class), accountBeansList);
    }	

    @Test
    public void getClientValidation_Success() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJYeldqTWZBV25zaGpGUHFWcDdiRWNjMkZ2b0d4TDBCSjhzMnVhazFUUWd3In0.eyJleHAiOjE2MzYyODcyMjMsImlhdCI6MTYzNjI4NDUyMywianRpIjoiYWYwNGQ5MTYtYjNjNC00MTlhLTk3YTAtYTE1Y2JhNDk4MTlmIiwiaXNzIjoiaHR0cHM6Ly8xOTIuMTY4LjEzLjQ0Ojg0NDMvYXV0aC9yZWFsbXMvbWFzdGVyIiwic3ViIjoiNzY0MDEyM2EtZmJkZS00ZmU1LTk4MTItNTgxY2QxZTNhOWMxIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiYWRtaW4tY2xpIiwic2Vzc2lvbl9zdGF0ZSI6Ijc1ZTg2MGM4LTdmZDAtNGFlOS05OGFhLWU2M2IzMTE5MzUzMiIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiKiJdLCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6ImFwcHNvbmVhZG1pbiIsImVtYWlsIjoiYXBwc29uZWFkbWluLmtAYXBwbm9taWMuY29tIn0.ngy-45jwAX-CU2KBSlws3_U4QeDpkOzRMQEeeRyrD7OvDnnUegWBv6jaT3bGMXQDmF_Ph7xfvqHLQcsDTDzhnxPXw8RKIoC6DFEK9r01Jug9zmt5L7Rco44xN9mYodh1xLdpL6a-vN57ZFvSqxen41r2G1_CKb_yqSVE67QmUxiM-vK9ShpSuy69qJ9Divy2GdM6DNPjN05Rsp25nF6XeNRE2QF8bnC1f_18yPZCyk9_XiLxJFHecbcpbDgYI-M4Tu_RuHnCCZOg-xZIfb2CSKPPwDqWQ3vV1cclIj3Ba4BLH2qAi-8Be1ktkBlM1LJ6xfiahT33pS-zWC5C6XLbeQ";
        Assert.assertEquals("7640123a-fbde-4fe5-9812-581cd1e3a9c1", getAccountBL.clientValidation(null, token).getPojoObject());
    }

    @Test(expected = ClientException.class)  
    public void getClientValidation_ClientException1() throws ClientException {
        getAccountBL.clientValidation(null, (String) null);
    }

    @Test
    public void serverValidation() throws Exception {
    	 UtilityBean<String> utilityBean = UtilityBean.<String>builder().pojoObject("7640123a-fbde-4fe5-9812-581cd1e3a9c1").build();
           Mockito.when(jdbcTemplateDao.fetchUserAccessDetailsUsingIdentifier(Mockito.anyString())).thenReturn(userAccessDetails);
           Mockito.when(jdbcTemplateDao.getAccountDetails(Mockito.anyString(), Mockito.anyString())).thenReturn(accountBeansList);
        Assert.assertEquals(accountBeansList.get(0).getIdentifier(), getAccountBL.serverValidation(utilityBean).getAccounts().get(0).getIdentifier());
    }

    @Test(expected = ServerException.class )
    public void serverValidation_Case2() throws Exception {
    	 UtilityBean<String> utilityBean = UtilityBean.<String>builder().pojoObject("7640123a-fbde-4fe5-9812-581cd1e3a9c1").build();
           Mockito.when(jdbcTemplateDao.fetchUserAccessDetailsUsingIdentifier(Mockito.anyString())).thenReturn(null);
           Mockito.when(jdbcTemplateDao.getAccountDetails(Mockito.anyString(), Mockito.anyString())).thenReturn(accountBeansList);
           Assert.assertEquals(accountBeansList.get(0).getIdentifier(), getAccountBL.serverValidation(utilityBean).getAccounts().get(0).getIdentifier());
    }

    @Test(expected = ServerException.class )
    public void serverValidation_Case3() throws Exception {
    	 UtilityBean<String> utilityBean = UtilityBean.<String>builder().pojoObject("7640123a-fbde-4fe5-9812-581cd1e3a9c1").build();
           Mockito.when(jdbcTemplateDao.fetchUserAccessDetailsUsingIdentifier(Mockito.anyString())).thenReturn(userAccessDetails);
           Mockito.when(jdbcTemplateDao.getAccountDetails(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
           Assert.assertEquals(accountBeansList.get(0).getIdentifier(), getAccountBL.serverValidation(utilityBean).getAccounts().get(0).getIdentifier());
    }

    @Test
    public void processData() throws Exception {
           Assert.assertEquals(accountBeansList.get(0).getIdentifier(), getAccountBL.process(accessAccountsBean).get(0).getIdentifier());
    }
    @Test
    public void processData_Case2() throws Exception {
    		userAccessDetails.setAccessDetails("{\"accounts\": [\"qa-d681ef13-d690-4917-jkhg-6c79b-1\"]}");
    		accessAccountsBean.setAccountMappingBean(new Gson().fromJson(userAccessDetails.getAccessDetails(), AccountMappingBean.class));
           Assert.assertEquals(accountBeansList.get(0).getIdentifier(), getAccountBL.process(accessAccountsBean).get(0).getIdentifier());
    }
}
