package com.heal.dashboard.service.beans;



import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountServiceKey {
	  	private int accountId;
	    private int serviceId;
}
