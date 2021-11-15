package com.heal.dashboard.service.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.heal.dashboard.service.util.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApplicationHealthDetail {
	private int id;
	private String identifier;
	private String name;
	private boolean maintenanceWindowStatus;
	private String dashboardUId;

	private ArrayList<ApplicationHealthStatus> problem = new ArrayList<>();
	private ArrayList<ApplicationHealthStatus> warning = new ArrayList<>();
	private ArrayList<ApplicationHealthStatus> batch = new ArrayList<>();

	public void setApplicationHealthStatus(List<ViewTypeBean> viewTypesList) {
		ViewTypeBean typeSevere = null;
		ViewTypeBean typeDefault = null;

		if (viewTypesList != null && viewTypesList.size() > 0) {
			Optional<ViewTypeBean> typeSevereOptional = viewTypesList.stream()
					.filter(it -> Constants.SEVERITY_295 == it.getSubTypeId()).findAny();

			if (typeSevereOptional.isPresent()) {
				typeSevere = typeSevereOptional.get();
			}
			Optional<ViewTypeBean> typeDefaultOptional = viewTypesList.stream()
					.filter(it -> Constants.SEVERITY_296 == it.getSubTypeId()).findAny();

			if (typeDefaultOptional.isPresent()) {
				typeDefault = typeDefaultOptional.get();
			}
		}
		String severeTypeStr = typeSevere.getSubTypeName();

		String defaultTypeStr = typeDefault.getSubTypeName();

		ApplicationHealthStatus severeTypeProblem = new ApplicationHealthStatus();
		severeTypeProblem.setName(severeTypeStr);
		severeTypeProblem.setCount(0);
		severeTypeProblem.setPriority(1);

		ApplicationHealthStatus defaultTypeProblem = new ApplicationHealthStatus();
		defaultTypeProblem.setName(defaultTypeStr);
		defaultTypeProblem.setCount(0);
		defaultTypeProblem.setPriority(0);

		problem.add(severeTypeProblem);
		problem.add(defaultTypeProblem);

		ApplicationHealthStatus severeTypeWarning = new ApplicationHealthStatus();
		severeTypeWarning.setName(severeTypeStr);
		severeTypeWarning.setCount(0);
		severeTypeWarning.setPriority(1);

		ApplicationHealthStatus defaultTypeWarning = new ApplicationHealthStatus();
		defaultTypeWarning.setName(defaultTypeStr);
		defaultTypeWarning.setCount(0);
		defaultTypeWarning.setPriority(0);

		warning.add(severeTypeWarning);
		warning.add(defaultTypeWarning);

		ApplicationHealthStatus batchSevereTypeStr = new ApplicationHealthStatus();
		batchSevereTypeStr.setName(severeTypeStr);
		batchSevereTypeStr.setPriority(1);

		batch.add(batchSevereTypeStr);
		batch.add(defaultTypeWarning);

	}
}
