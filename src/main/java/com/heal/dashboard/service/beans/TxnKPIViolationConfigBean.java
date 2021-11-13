package com.heal.dashboard.service.beans;



import java.util.ArrayList;
import java.util.List;

import com.heal.dashboard.service.enums.ViolationType;

import lombok.Data;

@Data
public class TxnKPIViolationConfigBean {
		private ViolationType txnKpiType;
	    private String responseTimeType;
	    private List<KpiViolationConfigBean> kpiViolationConfig = new ArrayList<>();
}
