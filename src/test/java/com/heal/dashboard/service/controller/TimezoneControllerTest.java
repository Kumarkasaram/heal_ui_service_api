package com.heal.dashboard.service.controller;


import com.heal.dashboard.service.beans.UserTimezonePojo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class TimezoneControllerTest {

    @InjectMocks
    UserTimeZoneController timezoneController;

    @Before
    public void setup() {
//	      setting up mock data in  accountBean
    }

    @Test
    public void setUserPreferredTimezone_OK() throws Exception {
		ResponseEntity<String> responseEntity = ResponseEntity.ok("7640123a-fbde-4fe5-9812-581cd1e3a9c1");
        Mockito.when(timezoneController.setUserPreferedZone(Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn(responseEntity);
        Assert.assertEquals(HttpStatus.CREATED, timezoneController.setUserPreferedZone("appsone", "header", new UserTimezonePojo()).getStatusCode());
    }
}
