package com.heal.dashboard.service.util;

import com.appnomic.appsone.model.JWTData;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

@Slf4j
public class Utility {

    public static String extractUserIdFromJWT(String jwtToken) {
        JWTData jwtData;
        try {
            String payload = jwtToken.split("\\.")[1];
            String body = new String(Base64.getDecoder().decode(payload.getBytes()));
            jwtData = (new Gson()).fromJson(body, JWTData.class);
        } catch (Exception e) {
            log.error("Invalid token supplied for username extraction. Details: ", e);
            return null;
        }

        return jwtData.getSub();
    }
}
