package com.heal.dashboard.service.beans.topology;



import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopologyValidationResponseBean {

	private List<Nodes> nodeList = null;
	private List<Edges> edgesList =null;
	private String serviceId;
	
}
