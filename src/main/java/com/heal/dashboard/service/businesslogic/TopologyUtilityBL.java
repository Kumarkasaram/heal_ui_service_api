package com.heal.dashboard.service.businesslogic;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.heal.dashboard.service.beans.AccountBean;
import com.heal.dashboard.service.beans.AgentBean;
import com.heal.dashboard.service.beans.ConnectionDetails;
import com.heal.dashboard.service.beans.ControllerBean;
import com.heal.dashboard.service.beans.TagDetails;
import com.heal.dashboard.service.beans.TagMapping;
import com.heal.dashboard.service.beans.UserAccessDetails;
import com.heal.dashboard.service.beans.ViewTypeBean;
import com.heal.dashboard.service.beans.topology.Edges;
import com.heal.dashboard.service.beans.topology.Nodes;
import com.heal.dashboard.service.dao.mysql.AgentDao;
import com.heal.dashboard.service.dao.mysql.ControllerDao;
import com.heal.dashboard.service.dao.mysql.MasterDataDao;
import com.heal.dashboard.service.dao.mysql.TagsDao;
import com.heal.dashboard.service.exception.ServerException;
import com.heal.dashboard.service.util.CommonUtils;
import com.heal.dashboard.service.util.Constants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TopologyUtilityBL {

	@Autowired
	ControllerDao controllerDao;
	@Autowired
	MasterDataDao masterDataDao;
	@Autowired
	AgentDao agentDao;
	@Autowired
	CommonServiceBL commonServiceBL;
	@Autowired
	TagsDao tagDao;
	@Autowired
	MaintainanceWindowsBL maintainanceWindowsBL;
	
	
	public List<Nodes> getNodeList(AccountBean account, UserAccessDetails userAccessDetails, int applicationId,
			long toTime) throws ServerException  {

		List<Nodes> nodesList = Collections.synchronizedList(new ArrayList<>());
		try {
			// get the service for controller
			List<ControllerBean> serviceList = null;
			if (applicationId == 0) {
				serviceList = controllerDao.getAllServicesForAccount(account.getId());
			} else {
				serviceList = controllerDao.getServicesByAppId(account.getId(), applicationId);
			}
			List<String> accessibleServiceList = userAccessDetails.getServiceIdentifiers();

			long start = System.currentTimeMillis();
			log.debug("Time taken to fetch service app map cache is {} ms.", (System.currentTimeMillis() - start));

			long startEnriching = System.currentTimeMillis();
			long[] startArray = new long[1];
			nodesList = serviceList.parallelStream().map(s -> {
				startArray[0] = System.currentTimeMillis();
				boolean flag = false;
				Nodes temp = null;
				List<TagMapping> serviceTags = null;
				List<ControllerBean> applications = null;
				try {
					applications = controllerDao.getApplicationsBySvcId(account.getId(), s.getId());
					serviceTags = commonServiceBL.getServiceTags(s.getId(), account.getId());
					temp = getNode(s, account, applications, flag, serviceTags, toTime);

				} catch (ServerException e) {
					log.error("Exception occur in getNodeList () method while   fething applicationBySVCId");
					e.printStackTrace();
				}
				if (accessibleServiceList.contains(s.getIdentifier())) {
					flag = true;
				}

				log.debug("Time taken for fetching node for service: {} is {} ms.", s.getName(),
						(System.currentTimeMillis() - startArray[0]));
				return temp;
			}).collect(Collectors.toList());
			log.debug("Time taken to enrich {} SDM nodes is {} ms.", nodesList.size(),
					(System.currentTimeMillis() - startEnriching));
		} catch (RuntimeException ex) {
			log.error("Error occurred while getting nodes for accountId : {}", account.getId(), ex);
		}

		return nodesList;
	}

	
	
	public List<Edges> getEdgeList(int accountId, int applicationId) {

		List<Edges> edgesList = new ArrayList<>();
		try {
			// get the service for controller
			List<ControllerBean> serviceList = controllerDao.getServicesByAppId(applicationId, accountId);

			List<ConnectionDetails> connectionDetailsList = masterDataDao.getConnectionDetails(accountId);

			List<ConnectionDetails> filterConnectionDetails = connectionDetailsList.stream()
					.filter(t -> t.getSourceRefObject().equalsIgnoreCase(Constants.CONTROLLER_TAG))
					.filter(t -> t.getDestinationRefObject().equalsIgnoreCase(Constants.CONTROLLER_TAG))
					.filter(t -> t.getSourceId() > 0).filter(t -> t.getDestinationId() > 0)
					.collect(Collectors.toList());

			for (ConnectionDetails connectionDetails : filterConnectionDetails) {
				Edges edges = new Edges();
				// checking for services only
				Optional<ControllerBean> controller = serviceList.stream()
						.filter(t -> t.getId() == connectionDetails.getSourceId()
								|| t.getId() == connectionDetails.getDestinationId())
						.findAny();

				// extra validation so that duplicate edges are not created
				if (controller.isPresent() && (edgesList.stream()
						.noneMatch(it -> (it.getSource().equals(String.valueOf(connectionDetails.getSourceId()))
								&& it.getTarget().equals(String.valueOf(connectionDetails.getDestinationId())))))) {
					edges.setSource(String.valueOf(connectionDetails.getSourceId()));
					edges.setTarget(String.valueOf(connectionDetails.getDestinationId()));
					edgesList.add(edges);
				}
			}
		} catch (Exception e) {
			log.error("Error occurred while getting Edges for accountId:" + accountId, e);
		}
		return edgesList;
	}

	
	public Nodes getNode(ControllerBean service, AccountBean account, List<ControllerBean> appTag, boolean isAccessible,
			List<TagMapping> tagList, long toTime) throws ServerException {
		Nodes serviceNode = new Nodes();
		try {
			serviceNode.setId(String.valueOf(service.getId()));
			serviceNode.setName(service.getName());
			serviceNode.setIdentifier(service.getIdentifier());
			// serviceNode.setType(ComponentType.unknown.name());
			serviceNode.setUserAccessible(isAccessible);
			if (appTag != null) {
				log.debug("Service not mapped to application. Service Identifier:{}", service.getIdentifier());
				serviceNode
						.setApplicationName(appTag.stream().map(ControllerBean::getName).collect(Collectors.toList()));
			}
			long start = System.currentTimeMillis();
			serviceNode.setMaintenance(maintainanceWindowsBL.getServiceMaintenanceStatus(account, service.getName(),
					new Timestamp(toTime)));
			log.debug("Time taken for maintenance window details is {} ms.", (System.currentTimeMillis() - start));

			if (tagList == null)
				tagList = new ArrayList<>();

			start = System.currentTimeMillis();
			TagDetails tagDetailsBean = tagDao.getTagDetails(Constants.LAYER_TAG, Constants.DEFAULT_ACCOUNT_ID);
			if (tagDetailsBean == null) {
				log.error("Tag {} does not exist in database.", Constants.LAYER_TAG);
				return serviceNode;
			}

			TagMapping layerDetails = tagList.stream().filter(tag -> tag.getTagId() == tagDetailsBean.getId())
					.filter(it -> it.getObjectId() == service.getId()).findAny().orElse(null);
			if (layerDetails != null) {
				String[] splitType = layerDetails.getTagValue().toLowerCase()
						.split(Constants.ICON_TITLE_SPLITTER_DEFAULT);
				if (splitType.length == 2) {
					serviceNode.setType(splitType[0]);
					serviceNode.setTitle(splitType[1]);
				} else {
					serviceNode.setType(layerDetails.getTagValue().toLowerCase());
				}
			}
			log.debug("Time taken for layer details details is {} ms.", (System.currentTimeMillis() - start));

			start = System.currentTimeMillis();
			TagDetails ctrlTagDetail = tagDao.getTagDetails(Constants.CONTROLLER_TAG, Constants.DEFAULT_ACCOUNT_ID);
			if (ctrlTagDetail == null) {
				log.error("Tag {} does not exist in database.", Constants.CONTROLLER_TAG);
				return serviceNode;
			}
			ViewTypeBean jimAgentType = null;
			List<ViewTypeBean> viewTypeList = masterDataDao.getAllViewTypes();
			Optional<ViewTypeBean> subTypeOptional = viewTypeList.stream()
					.filter(it -> (Constants.AGENT_TYPE.trim().equalsIgnoreCase(it.getTypeName())))
					.filter(it -> (Constants.JIM_AGENT_TYPE.trim().equalsIgnoreCase(it.getSubTypeName()))).findAny();
			if (subTypeOptional.isPresent()) {
				jimAgentType = subTypeOptional.get();
			}

			List<AgentBean> agentBeans = agentDao.getAgentList();
			boolean isJimEnabled =false;
			try{
			 isJimEnabled = CommonUtils.isJimEnabled(service, agentBeans, tagList, jimAgentType.getSubTypeId(),
					ctrlTagDetail.getId());
			if (isJimEnabled) {
				serviceNode.addToMetaData("jimEnabled", 1);
			} else {
				serviceNode.addToMetaData("jimEnabled", 0);
			}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			log.debug("Time taken for JIM details is {} ms.", (System.currentTimeMillis() - start));

			start = System.currentTimeMillis();
			TagDetails entryTagDetail = tagDao.getTagDetails(Constants.ENTRY_POINT, Constants.DEFAULT_ACCOUNT_ID);
			if (entryTagDetail == null) {
				log.error("Entry Tag {} does not exist in database.", Constants.ENTRY_POINT);
				return serviceNode;
			}
			List<TagMapping> entrypointSvcList = tagList.stream()
					.filter(tag -> tag.getTagId() == entryTagDetail.getId())
					.filter(it -> it.getObjectId() == service.getId())
					.filter(tag -> tag.getTagKey().equalsIgnoreCase(Constants.DEFAULT_ENTRY_POINT))
					.collect(Collectors.toList());
			boolean isEntryEnabled = (entrypointSvcList.size() > 0);
			serviceNode.setEntryPointNode(isEntryEnabled);
			log.debug("Time taken for entry point details is {} ms.", (System.currentTimeMillis() - start));

			start = System.currentTimeMillis();
			List<TagMapping> kubernetesTagList = tagList.stream()
					.filter(tag -> tag.getTagValue().equalsIgnoreCase(Constants.KUBERNETES))
					.filter(it -> it.getObjectId() == service.getId()).collect(Collectors.toList());
			boolean isKubernetesEnabled = (kubernetesTagList.size() > 0);
			if (isKubernetesEnabled) {
				serviceNode.addToMetaData("isKubernetes", 1);
			} else {
				serviceNode.addToMetaData("isKubernetes", 0);
			}
			log.debug("Time taken for Kubernetes details is {} ms.", (System.currentTimeMillis() - start));
		} catch (ServerException ex) {
			ex.printStackTrace();
		}
		return serviceNode;
	}

}
