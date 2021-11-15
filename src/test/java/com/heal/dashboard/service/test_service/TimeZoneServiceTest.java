package com.heal.dashboard.service.test_service;

import com.heal.dashboard.service.beans.UserAttributeBeen;
import com.heal.dashboard.service.beans.UserTimezonePojo;
import com.heal.dashboard.service.beans.UserTimezoneRequestData;
import com.heal.dashboard.service.beans.UtilityBean;
import com.heal.dashboard.service.businesslogic.UserTimezoneBL;
import com.heal.dashboard.service.controller.UserTimeZoneController;
import com.heal.dashboard.service.exception.ClientException;
import com.heal.dashboard.service.exception.DataProcessingException;
import com.heal.dashboard.service.exception.ServerException;
import com.heal.dashboard.service.util.JsonFileParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class TimeZoneServiceTest {

	@InjectMocks
	UserTimeZoneController userTimeZoneController;

	@Mock
    UserTimezoneBL setUserTimeZoneBL;

	private UserTimezoneRequestData userTimezoneRequestData;

	@Mock
	JsonFileParser headersParser;

	@Before
	public void setup() {
		// setting up mock data for client validation response
		UserTimezonePojo tzResponse = new UserTimezonePojo();
		tzResponse.setIsNotificationsTimezoneMychoice(1);
		tzResponse.setIsTimezoneMychoice(1);
		tzResponse.setTimezoneId(0);
		userTimezoneRequestData = new UserTimezoneRequestData();
		userTimezoneRequestData.setUserTimezonePojo(tzResponse);
		userTimezoneRequestData.setUsername("appsoneadmin");

		// setting up mock data for serverValidation response
		UserAttributeBeen userAttributeBeen = new UserAttributeBeen();
		userAttributeBeen.setId(1);
		userAttributeBeen.setIsTimezoneMychoice(1);
		userAttributeBeen.setIsNotificationsTimezoneMychoice(0);
		userAttributeBeen.setIsTimezoneMychoice(0);
		userAttributeBeen.setStatus(1);
		userTimezoneRequestData.setUserAttributeBeen(userAttributeBeen);

	}

	@Test
	public void setUserPreferredTimezone() throws Exception {
		Mockito.when(setUserTimeZoneBL.clientValidation(Mockito.any()))
				.thenReturn(UtilityBean.<UserTimezoneRequestData>builder()
						.authToken("7640123a-fbde-4fe5-9812-581cd1e3a9c1").pojoObject(userTimezoneRequestData).build());
		Mockito.when(setUserTimeZoneBL.serverValidation(Mockito.any())).thenReturn(userTimezoneRequestData);
		Mockito.when(setUserTimeZoneBL.process(Mockito.any())).thenReturn("7640123a-fbde-4fe5-9812-581cd1e3a9c1");

		Mockito.when(headersParser.loadHeaderConfiguration()).thenReturn(new HttpHeaders() {{
			set("authorization", "check2");
		}});

		ResponseEntity<String> responseEntity = userTimeZoneController
				.setUserPreferedZone("testing", "testing", userTimezoneRequestData.getUserTimezonePojo());

		Assert.assertEquals("7640123a-fbde-4fe5-9812-581cd1e3a9c1", responseEntity.getBody());
	}

	@Test(expected = Exception.class)
	public void setUserPreferedZone_BadRequestException() throws ClientException, ServerException, DataProcessingException {
		Mockito.when(setUserTimeZoneBL.clientValidation(Mockito.any()))
				.thenThrow(new Exception("authentication failed"));
		userTimeZoneController.setUserPreferedZone(null, null, null);
	}

	@Test(expected = RuntimeException.class)
	public void setUserPreferedZone_InternalServerError() throws Exception {
		Mockito.when(setUserTimeZoneBL.clientValidation(Mockito.any()))
				.thenReturn(UtilityBean.<UserTimezoneRequestData>builder()
						.authToken("7640123a-fbde-4fe5-9812-581cd1e3a9c1").pojoObject(userTimezoneRequestData).build());
		Mockito.when(setUserTimeZoneBL.serverValidation(Mockito.any())).thenReturn(userTimezoneRequestData);
		Mockito.when(setUserTimeZoneBL.process(Mockito.any())).thenThrow(new RuntimeException());
		userTimeZoneController.setUserPreferedZone(null, null, null);
	}
}
