package com.heal.dashboard.service.beans;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UtilityBean<T> {

    T pojoObject;
    String accountIdentifier;
    String authToken;
}
