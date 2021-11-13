package com.heal.dashboard.service.beans;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAccessDetails extends BaseEntity {

	private List<String> applicationIdentifiers;
    private List<Integer> applicationIds;
    private List<Integer> serviceIds;
    private List<String> serviceIdentifiers;
    private List<Integer> transactionIds;
    private List<AgentBean> agents;
    private List<ViewApplicationServiceMappingBean> applicationServiceMappingBeans;
}
