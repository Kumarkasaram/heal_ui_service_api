package com.heal.dashboard.service.beans;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ApplicationHealthResponse {
	
	List<TagMapping> tags = null;
	List<ApplicationHealthDetail> appHealthData =null;
	List<Controller> accessibleApps =null;

}
