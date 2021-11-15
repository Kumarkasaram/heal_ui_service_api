package com.heal.dashboard.service.businesslogic;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.datastax.oss.driver.api.core.cql.Row;
import com.heal.dashboard.service.beans.AccountBean;
import com.heal.dashboard.service.beans.ApplicationHealthDetail;
import com.heal.dashboard.service.beans.ApplicationHealthResponse;
import com.heal.dashboard.service.beans.Controller;
import com.heal.dashboard.service.beans.SignalType;
import com.heal.dashboard.service.beans.TagDetails;
import com.heal.dashboard.service.beans.TagMapping;
import com.heal.dashboard.service.beans.UtilityBean;
import com.heal.dashboard.service.beans.ViewApplicationServiceMappingBean;
import com.heal.dashboard.service.beans.ViewTypeBean;
import com.heal.dashboard.service.dao.mysql.AccountCassandraDao;
import com.heal.dashboard.service.dao.mysql.AccountDao;
import com.heal.dashboard.service.dao.mysql.ControllerDao;
import com.heal.dashboard.service.dao.mysql.MasterDataDao;
import com.heal.dashboard.service.dao.mysql.TagsDao;
import com.heal.dashboard.service.exception.ClientException;
import com.heal.dashboard.service.exception.DataProcessingException;
import com.heal.dashboard.service.exception.ServerException;
import com.heal.dashboard.service.util.Constants;
import com.heal.dashboard.service.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ApplicationHealthBL
		implements BusinessLogic<String, ApplicationHealthResponse, List<ApplicationHealthDetail>> {

	@Autowired
	AccountDao accountDao;
	@Autowired
	AccountCassandraDao accountCassandraDao;
	@Autowired
	CommonServiceBL commonServiceBL;
	@Autowired
	TagsDao tagDao;
	@Autowired
	ControllerDao controllerDao;
	@Autowired
	MaintainanceWindowsBL maintenancWindowBL;
	@Autowired
	MasterDataDao masterDataDao;


	@Override
	public UtilityBean<String> clientValidation(Object body,String... requestParams) throws ClientException {
		String identifier =requestParams[0];
		if (null == identifier || identifier.trim().isEmpty()) {
			throw new ClientException("identifier cannot be null or empty");
		}
		String userId = requestParams[2];
		if (null == userId || userId.trim().isEmpty()) {
			throw new ClientException("autharization token cannot be null or empty");
		}

		String toTimeString = requestParams[1];
		if (null == toTimeString || toTimeString.trim().isEmpty()) {
			throw new ClientException("toTimeString cannot be null or empty");
		}
		return UtilityBean.<String>builder().authToken(userId).accountIdentifier(identifier).pojoObject(toTimeString)
				.build();
	}

	@Override
	public ApplicationHealthResponse serverValidation(UtilityBean<String> utilityBean) throws ServerException {
		AccountBean accountBean = null;
		List<TagMapping> tags = null;
		long toTime = 0;
		toTime = Long.parseLong(utilityBean.getPojoObject());
		long fromTime = toTime - TimeUnit.MINUTES.toMillis(Long.parseLong(Constants.SIGNAL_CLOSE_WINDOW_TIME));

		List<AccountBean> accountList = accountDao.getAccountDetails(Constants.TIME_ZONE_TAG,
				Constants.ACCOUNT_TABLE_NAME_MYSQL_DEFAULT);
		if (accountList != null || !accountList.isEmpty()) {
			Optional<AccountBean> accountOptional = accountList.stream()
					.filter(it -> it.getIdentifier().equals(utilityBean.getAccountIdentifier())).findAny();
			if (accountOptional.isPresent()) {
				accountBean = accountOptional.get();
			}
		}
		List<Row> problemList = getProblemList(accountBean.getIdentifier(), fromTime, toTime);
		List<Controller> accessibleApps = commonServiceBL.getAccessibleApplicationsForUser(utilityBean.getAuthToken(),
				utilityBean.getAccountIdentifier(), String.valueOf(accountBean.getId()));
		if (accessibleApps != null && accessibleApps.size() > 0) {
			List<String> accessibleApplications = accessibleApps.parallelStream().map(Controller::getIdentifier).collect(Collectors.toList());
			long time = System.currentTimeMillis();
			try {
				List<ApplicationHealthDetail> appHealthData = getOpenProblems(accountBean, problemList, accessibleApplications);
				
				if (accessibleApplications.size() != appHealthData.size()) {
					time = System.currentTimeMillis();
					List<String> healthAppIds = appHealthData.parallelStream()
							.map(ApplicationHealthDetail::getIdentifier).collect(Collectors.toList());
						accessibleApps.forEach(c -> {
						if (!healthAppIds.contains(c.getIdentifier())) {
							ApplicationHealthDetail detail = new ApplicationHealthDetail();
							detail.setId(Integer.parseInt(c.getAppId()));
							detail.setIdentifier(c.getIdentifier());
							detail.setName(c.getName());
							appHealthData.add(detail);
						}
					});
					log.debug("Time taken to add delta applications without any services: {}",
							System.currentTimeMillis() - time);
				}
				TagDetails tagdetail = tagDao.getTagDetails(Constants.DASHBOARD_UID_TAG, Constants.DEFAULT_ACCOUNT_ID);
				if (tagdetail != null) {
					tags = tagDao.getTagMappingDetailsByAccountId(accountBean.getId()).parallelStream().filter(
							tag -> tag.getTagId() == tagdetail.getId() && tag.getObjectRefTable().equals(Constants.CONTROLLER))
							.collect(Collectors.toList());
				}
				return new ApplicationHealthResponse(tags, appHealthData, accessibleApps);
			} catch (ParseException e) {
				throw new ServerException("Error in ApplicationHealthBL class while parsing the number  : "+e.getMessage());
			}
		}
		return new ApplicationHealthResponse();	
	}

	
	@Override
	public List<ApplicationHealthDetail> process(ApplicationHealthResponse applicationHealthResponse) throws DataProcessingException {
		List<ApplicationHealthDetail> appHealthData = applicationHealthResponse.getAppHealthData();
		 if (applicationHealthResponse.getAccessibleApps().isEmpty()) {
             log.warn("There are no applications mapped to the user [{}]");
             return new ArrayList<>();
         } else {
			appHealthData.forEach(app -> {
				Optional<TagMapping> tag = applicationHealthResponse.getTags().parallelStream()
						.filter(t -> t.getObjectId() == app.getId()).findAny();
				tag.ifPresent(tagMappingDetails -> app.setDashboardUId(tagMappingDetails.getTagValue()));
			});
		return appHealthData;
       }
	}
	
	
	
	
	public List<Row> getProblemList(String accountIdentifier, long fromTime, long toTime) throws ServerException {
		List<Row> signalList = null;
		Set<String> signalIds = accountCassandraDao.getSignalId(accountIdentifier, fromTime, toTime);
		if (signalIds != null) {
			signalList = accountCassandraDao.getSignalList(signalIds);	
		}
		return signalList;
	}

	
	
	
	
	
	public List<ApplicationHealthDetail> getOpenProblems(AccountBean account, List<Row> signalsRaw,
			List<String> accessibleApplicationList) throws ParseException, ServerException {
	
		long time = System.currentTimeMillis();
		// application identifier, list of services for given account
		List<ViewApplicationServiceMappingBean> ViewApplicationServiceMappingBeanList = controllerDao
				.getApplicationServicesByAccount(account.getId());	

		Map<String, List<ViewApplicationServiceMappingBean>> appIdentifierMap = ViewApplicationServiceMappingBeanList.stream()
				.collect(Collectors.groupingBy(ViewApplicationServiceMappingBean::getApplicationIdentifier));

		// Filter only accessible applications from above map and this map has appid is
		// key, value is list of services
		Map<Integer, List<ViewApplicationServiceMappingBean>> appIdVsServiceMapping = accessibleApplicationList.stream()
				.map(appIdentifier -> appIdentifierMap.getOrDefault(appIdentifier, null)).filter(Objects::nonNull)
				.flatMap(Collection::stream)
				.collect(Collectors.groupingBy(ViewApplicationServiceMappingBean::getApplicationId));
		log.debug("Time taken to fetch serviceApplication mapping from percona: {}", System.currentTimeMillis() - time);

		time = System.currentTimeMillis();
		Map<Integer, ApplicationHealthDetail> dataMap = initializeHealthData(account, appIdVsServiceMapping);
		log.debug("Time taken to initializeHealthData: {}", System.currentTimeMillis() - time);

		if (signalsRaw != null) {
			// Map of serviceIdentifier vs applicationIds
			time = System.currentTimeMillis();
			// Converting all the accessible services into map. Key is service identifier,
			// Value is list of application id.
			Map<String, Set<Integer>> serviceIdentifierVsAppMapping = appIdVsServiceMapping.values().stream()
					.flatMap(Collection::stream)
					.collect(Collectors.groupingBy(ViewApplicationServiceMappingBean::getServiceIdentifier,
							Collectors.mapping(ViewApplicationServiceMappingBean::getApplicationId, Collectors.toSet())));

			// In case of batch jobs, application identifiers will be present in service
			// identifier column in cassandra for signal details
			// Hence adding application identifiers as well in this map
			serviceIdentifierVsAppMapping.putAll(controllerDao.getApplicationList(account.getId()).parallelStream()
					.collect(Collectors.groupingBy(Controller::getIdentifier,
							Collectors.mapping(c -> Integer.parseInt(c.getAppId()), Collectors.toSet()))));
			log.debug("Time taken to create map of service identifier vs appId: {}", System.currentTimeMillis() - time);
			time = System.currentTimeMillis();
			signalsRaw.parallelStream().forEach(signal -> {
				if (isProblemOpen(signal)
						&& !signal.getString("signal_type").equalsIgnoreCase(SignalType.INFO.name())) {
					String prblmId = signal.getString("signal_id");
					Set<String> affectedServices = signal.getSet("service_ids", String.class);
					String signalType = signal.getString("signal_type");
					if (prblmId == null || affectedServices == null) {
						log.warn("Invalid fields found in problem problemId: {}, service id: {}.", prblmId,
								affectedServices);
						return;
					}
					Set<Integer> affectedAppIds = new HashSet<Integer>();

					affectedServices.parallelStream().forEach(service -> affectedAppIds
							.addAll(serviceIdentifierVsAppMapping.getOrDefault(service, new HashSet<>())));
					try {
						addProblemData(dataMap, affectedAppIds, signal.getString("severity"), signalType);
					} catch (ServerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			log.debug("Time taken to go through signals rows: {}", System.currentTimeMillis() - time);
		}

		return new ArrayList<>(dataMap.values());
	}

	
	public Map<Integer, ApplicationHealthDetail> initializeHealthData(AccountBean account,
		Map<Integer, List<ViewApplicationServiceMappingBean>> appIdVsServiceIdentifiers) throws ParseException, ServerException {
		Map<Integer, ApplicationHealthDetail> result = new HashMap<>();
		Timestamp date = new Timestamp(DateUtil.getDateInGMT(System.currentTimeMillis()).getTime());
		List<ViewTypeBean> viewTypesList = masterDataDao.getAllViewTypes();	
		appIdVsServiceIdentifiers.forEach((appId, serviceIdentifiers) -> {
			ApplicationHealthDetail temp = new ApplicationHealthDetail();
			boolean isWindow = serviceIdentifiers.parallelStream().allMatch(service -> maintenancWindowBL
					.getServiceMaintenanceStatus(account, service.getServiceIdentifier(), date));
			temp.setMaintenanceWindowStatus(isWindow);
			temp.setName(serviceIdentifiers.get(0).getApplicationName());
			temp.setIdentifier(serviceIdentifiers.get(0).getApplicationIdentifier());
			temp.setId(appId);
			temp.setApplicationHealthStatus(viewTypesList);
			result.put(appId, temp);
		});

		return result;
	}

	public boolean isProblemOpen(Row signal) {
		log.trace("{} isProblemOpen().", "");
		String status = signal.getString("current_status");
		String problemId = signal.getString("signal_id");
		if ("open".equalsIgnoreCase(status)) {
			log.debug("Problem: {} is open.", problemId);
			return true;
		} else {
			log.debug("Problem: {} is {}.", problemId, status);
			return false;
		}
	}

	public void addProblemData(Map<Integer, ApplicationHealthDetail> data, Set<Integer> affectedAppIds,
		String signalSeverity, String signalType) throws ServerException {
		log.trace("{} addProblemData().", Constants.INVOKED_METHOD);
		ViewTypeBean viewTypeBean=null;
		List<ViewTypeBean> viewTypesList = masterDataDao.getAllViewTypes();
		
		if(viewTypesList!=null && viewTypesList.size()>0) {
			 Optional<ViewTypeBean> subTypeOptional = viewTypesList
	                    .stream()
	                    .filter(it -> (Integer.parseInt(signalSeverity) == it.getSubTypeId()))
	                    .findAny();

	            if(subTypeOptional.isPresent())
	            	viewTypeBean = subTypeOptional.get();
		}
		
		String severityTypeStr = viewTypeBean.getSubTypeName();
		affectedAppIds.parallelStream().map(data::get).filter(Objects::nonNull).forEach(temp -> {
			if (Constants.PROBLEM_LITERAL.equalsIgnoreCase(signalType)) {
				temp.getProblem().parallelStream().filter(p -> p.getName().equals(severityTypeStr)).forEach(p -> {
					int currCount = p.getCount();
					p.setCount(currCount + 1);
				});
			} else if (Constants.BATCH_JOB_LITERAL.equalsIgnoreCase(signalType)) {
				temp.getBatch().parallelStream().filter(b -> b.getName().equals(severityTypeStr)).forEach(b -> {
					int currCount = b.getCount();
					b.setCount(currCount + 1);
				});
			} else {
				temp.getWarning().parallelStream().filter(w -> w.getName().equals(severityTypeStr)).forEach(w -> {
					int currCount = w.getCount();
					w.setCount(currCount + 1);
				});
			}
		});
	}


}
