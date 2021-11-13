package com.heal.dashboard.service.businesslogic;



import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.heal.dashboard.service.dao.mysql.TimezoneDao;
import com.heal.dashboard.service.beans.TagDetails;
import com.heal.dashboard.service.beans.TagMapping;
import com.heal.dashboard.service.beans.TimezoneDetail;
import com.heal.dashboard.service.beans.UserAttributeBeen;
import com.heal.dashboard.service.beans.UserDetailsBean;
import com.heal.dashboard.service.beans.UserTimezonePojo;
import com.heal.dashboard.service.beans.UserTimezoneRequestData;
import com.heal.dashboard.service.beans.UtilityBean;
import com.heal.dashboard.service.exception.ClientException;
import com.heal.dashboard.service.util.Constants;

@RunWith(SpringRunner.class)
public class UserTimezoneBLTest {

	@InjectMocks
	UserTimezoneBL serTimezoneBL;
	@Mock
	UserTimezoneBL userTimezoneBLMock;
	@Mock
	TimezoneDao timezoneDao;

	@MockBean
	private UserAttributeBeen userAttributeBeen;
	@MockBean
	private UserDetailsBean userDetailsBean;
	@MockBean
	private TimezoneDetail timezoneDetail;
	@MockBean
	private TagDetails tagDetails;
	@MockBean
	private UserTimezonePojo tzResponse;
	@MockBean
	private TagMapping tagmapping;

	private UtilityBean<UserTimezoneRequestData> utility;
	private UserTimezoneRequestData userTimezoneRequestData;

	@Before
	public void setup() {
		// setting up mock data for client validation response
		userTimezoneRequestData = new UserTimezoneRequestData();
		tzResponse = new UserTimezonePojo();
		tzResponse.setIsNotificationsTimezoneMychoice(1);
		tzResponse.setIsTimezoneMychoice(1);
		tzResponse.setTimezoneId(0);
		userTimezoneRequestData.setUserTimezonePojo(tzResponse);
		userTimezoneRequestData.setUsername("appsoneadmin");

		// setting up mock data for server validation response
		userAttributeBeen = new UserAttributeBeen();
		userAttributeBeen.setId(1);
		userAttributeBeen.setIsTimezoneMychoice(1);
		userAttributeBeen.setIsNotificationsTimezoneMychoice(0);
		userAttributeBeen.setIsTimezoneMychoice(0);
		userAttributeBeen.setStatus(1);
		userAttributeBeen.setUsername("appsoneadmin");
		userTimezoneRequestData.setUserAttributeBeen(userAttributeBeen);
		// UserDetailsBean Mockup
		userDetailsBean = new UserDetailsBean();
		userDetailsBean.setId(1);
		userDetailsBean.setStatus(1);
		userDetailsBean.setUserIdentifier("7640123a-fbde-4fe5-9812-581cd1e3a9c1");
		userDetailsBean.setUserName("");
		userDetailsBean.setCreatedBy("7640123a-fbde-4fe5-9812-581cd1e3a9c1");

		// TimezoneDetail Mockup
		timezoneDetail = new TimezoneDetail();
		timezoneDetail.setAccountId(1);
		timezoneDetail.setId(1);
		timezoneDetail.setObjectId(2);
		timezoneDetail.setTimeOffset(1L);

		// TagDetails Mockup
		tagDetails = new TagDetails();
		tagDetails.setId(1);
		tagDetails.setAccountId(2);
		tagDetails.setName("");

		// TagMapping mockup
		tagmapping = new TagMapping();
		tagmapping.setAccountId(Constants.DEFAULT_ACCOUNT_ID);
		tagmapping.setObjectId(userAttributeBeen.getId());
		tagmapping.setObjectRefTable(Constants.USER_ATTRIBUTES_TABLE_NAME_MYSQL);
		tagmapping.setTagValue(String.valueOf(timezoneDetail.getTimeOffset()));
		tagmapping.setTagId(tagDetails.getId());
		tagmapping.setTagKey(String.valueOf(userTimezoneRequestData.getUserTimezonePojo().getTimezoneId()));
		tagmapping.setUserDetailsId("7640123a-fbde-4fe5-9812-581cd1e3a9c1");

		utility = UtilityBean.<UserTimezoneRequestData>builder().authToken("7640123a-fbde-4fe5-9812-581cd1e3a9c1")
				.pojoObject(userTimezoneRequestData).build();

	}

	@Test
	public void getClientValidation_Success() throws Exception {
		String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJYeldqTWZBV25zaGpGUHFWcDdiRWNjMkZ2b0d4TDBCSjhzMnVhazFUUWd3In0.eyJleHAiOjE2MzYyODcyMjMsImlhdCI6MTYzNjI4NDUyMywianRpIjoiYWYwNGQ5MTYtYjNjNC00MTlhLTk3YTAtYTE1Y2JhNDk4MTlmIiwiaXNzIjoiaHR0cHM6Ly8xOTIuMTY4LjEzLjQ0Ojg0NDMvYXV0aC9yZWFsbXMvbWFzdGVyIiwic3ViIjoiNzY0MDEyM2EtZmJkZS00ZmU1LTk4MTItNTgxY2QxZTNhOWMxIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiYWRtaW4tY2xpIiwic2Vzc2lvbl9zdGF0ZSI6Ijc1ZTg2MGM4LTdmZDAtNGFlOS05OGFhLWU2M2IzMTE5MzUzMiIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiKiJdLCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6ImFwcHNvbmVhZG1pbiIsImVtYWlsIjoiYXBwc29uZWFkbWluLmtAYXBwbm9taWMuY29tIn0.ngy-45jwAX-CU2KBSlws3_U4QeDpkOzRMQEeeRyrD7OvDnnUegWBv6jaT3bGMXQDmF_Ph7xfvqHLQcsDTDzhnxPXw8RKIoC6DFEK9r01Jug9zmt5L7Rco44xN9mYodh1xLdpL6a-vN57ZFvSqxen41r2G1_CKb_yqSVE67QmUxiM-vK9ShpSuy69qJ9Divy2GdM6DNPjN05Rsp25nF6XeNRE2QF8bnC1f_18yPZCyk9_XiLxJFHecbcpbDgYI-M4Tu_RuHnCCZOg-xZIfb2CSKPPwDqWQ3vV1cclIj3Ba4BLH2qAi-8Be1ktkBlM1LJ6xfiahT33pS-zWC5C6XLbeQ";
		Assert.assertEquals("appsoneadmin",
				serTimezoneBL.clientValidation(tzResponse, token, "appsoneadmin").getPojoObject().getUsername());
	}

	@Test(expected = ClientException.class)
	public void getClientValidation_ClientException1() throws ClientException {
		serTimezoneBL.clientValidation(userTimezoneRequestData, (String) null);
	}

	@Test
	public void serverValidation() throws Exception {
		Mockito.when(timezoneDao.getUserAttributes(Mockito.anyString())).thenReturn(userAttributeBeen);
		Mockito.when(timezoneDao.getUsers(Mockito.anyString())).thenReturn(userDetailsBean);
		Mockito.when(timezoneDao.getTimezonesById(Mockito.anyString())).thenReturn(timezoneDetail);
		Assert.assertEquals("appsoneadmin",
				serTimezoneBL.serverValidation(utility).getUserAttributeBeen().getUsername());
	}
	
	@Test
	public void serverValidation_case2() throws Exception {
		utility.getPojoObject().getUserTimezonePojo().setTimezoneId(1);
		Mockito.when(timezoneDao.getUserAttributes(Mockito.anyString())).thenReturn(userAttributeBeen);
		Mockito.when(timezoneDao.getUsers(Mockito.anyString())).thenReturn(userDetailsBean);
		Mockito.when(timezoneDao.getTimezonesById(Mockito.anyString())).thenReturn(timezoneDetail);
		Assert.assertEquals("appsoneadmin",
				serTimezoneBL.serverValidation(utility).getUserAttributeBeen().getUsername());
	}

	@Test
	public void processData_AddUserTagMapping() throws Exception {
		userTimezoneRequestData.getUserTimezonePojo().setTimezoneId(1);
		userTimezoneRequestData.setTimezoneDetail(timezoneDetail);
		userTimezoneRequestData.setUserDetailsBean(userDetailsBean);

		Mockito.when(timezoneDao.getTagDetails(Mockito.anyString(), Mockito.anyInt())).thenReturn(tagDetails);
		Mockito.when(timezoneDao.getUserTagMappingId(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
				.thenReturn(0);
		Mockito.when(userTimezoneBLMock.populateTagMapping(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(tagmapping);
		Mockito.when(timezoneDao.updateUserTimezoneChoice(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString())).thenReturn(6);
		Mockito.when(timezoneDao.addUserTagMapping(Mockito.any())).thenReturn(1);
		Mockito.when(userTimezoneBLMock.batchUpdate(Mockito.any(), Mockito.anyInt(), Mockito.any()))
				.thenReturn("7640123a-fbde-4fe5-9812-581cd1e3a9c1");

		Assert.assertEquals("7640123a-fbde-4fe5-9812-581cd1e3a9c1", serTimezoneBL.process(userTimezoneRequestData));
	}

	@Test
	public void processData_DeleteUserTagMapping() throws Exception {
		userTimezoneRequestData.setTimezoneDetail(timezoneDetail);
		userTimezoneRequestData.setUserDetailsBean(userDetailsBean);

		Mockito.when(timezoneDao.getTagDetails(Mockito.anyString(), Mockito.anyInt())).thenReturn(tagDetails);
		Mockito.when(timezoneDao.getUserTagMappingId(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
				.thenReturn(2095);
		Mockito.when(timezoneDao.updateUserTimezoneChoice(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString())).thenReturn(1);
		Mockito.when(timezoneDao.deleteUserTagMapping(Mockito.anyInt())).thenReturn(1);
		Mockito.when(userTimezoneBLMock.batchUpdate(Mockito.any(), Mockito.anyInt(), Mockito.any()))
				.thenReturn("7640123a-fbde-4fe5-9812-581cd1e3a9c1");

		Assert.assertEquals("7640123a-fbde-4fe5-9812-581cd1e3a9c1", serTimezoneBL.process(userTimezoneRequestData));
	}

	@Test
	public void processData_updateUserTagMapping() throws Exception {
		userTimezoneRequestData.getUserTimezonePojo().setTimezoneId(1);
		userTimezoneRequestData.setTimezoneDetail(timezoneDetail);
		userTimezoneRequestData.setUserDetailsBean(userDetailsBean);

		Mockito.when(timezoneDao.getTagDetails(Mockito.anyString(), Mockito.anyInt())).thenReturn(tagDetails);
		Mockito.when(timezoneDao.getUserTagMappingId(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
				.thenReturn(2095);
		Mockito.when(timezoneDao.updateUserTimezoneChoice(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString())).thenReturn(6);
		Mockito.when(timezoneDao.updateUserTagMapping(Mockito.anyInt(), Mockito.anyString(), Mockito.anyInt(),
				Mockito.anyString())).thenReturn(1);
		Mockito.when(userTimezoneBLMock.batchUpdate(Mockito.any(), Mockito.anyInt(), Mockito.any()))
				.thenReturn("7640123a-fbde-4fe5-9812-581cd1e3a9c1");

		Assert.assertEquals("7640123a-fbde-4fe5-9812-581cd1e3a9c1", serTimezoneBL.process(userTimezoneRequestData));
	}
	


}
