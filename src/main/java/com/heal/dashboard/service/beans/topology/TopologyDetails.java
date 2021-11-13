package com.heal.dashboard.service.beans.topology;



import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class TopologyDetails {
	 List<String> impactedServiceName = new ArrayList();
     List<Nodes> nodes;
     List<Edges> edges;
}

