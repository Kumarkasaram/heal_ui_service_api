package com.heal.dashboard.service.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringEscapeUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.heal.dashboard.service.beans.AgentBean;
import com.heal.dashboard.service.beans.ControllerBean;
import com.heal.dashboard.service.beans.TagMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonUtils {

	public static ObjectMapper getObjectMapperWithHtmlEncoder() {
		ObjectMapper objectMapper = new ObjectMapper();

		SimpleModule simpleModule = new SimpleModule("HTML-Encoder", objectMapper.version()).addDeserializer(String.class, new EscapeHTML());

		objectMapper.registerModule(simpleModule);

		return objectMapper;
	}
	
	public static boolean isJimEnabled(ControllerBean serviceDetail, List<AgentBean> agentList,
			List<TagMapping> tagList, int jimAgentSubTypeId, int controllerTagDetailId) {

		List<String> jimEnabledServiceIds = getJIMEnabledServiceIdsForAccount(agentList, tagList, jimAgentSubTypeId,
				controllerTagDetailId);
		if (serviceDetail != null) {
			boolean jimFlag = jimEnabledServiceIds.contains(String.valueOf(serviceDetail.getId()));
			log.debug("JIM is enabled for service: {} is {}.", serviceDetail.getName(), jimFlag);
			return jimFlag;
		}
		log.debug("Invalid service details received for JIM enabled validation.");
		return false;
	}

	public static List<String> getJIMEnabledServiceIdsForAccount(List<AgentBean> agentList,
			List<TagMapping> tagList, int jimAgentSubTypeId, int controllerTagDetailId) {
		if (!agentList.isEmpty() && !tagList.isEmpty()) {
			Set<Integer> jimAgentIds = agentList.stream().filter(agent -> agent.getAgentTypeId() == jimAgentSubTypeId)
					.map(AgentBean::getId).collect(Collectors.toSet());

			List<TagMapping> serviceCtrlMappings = tagList.stream()
					.filter(tag -> tag.getTagId() == controllerTagDetailId
							&& Constants.AGENT_TABLE.equalsIgnoreCase(tag.getObjectRefTable())
							&& jimAgentIds.contains(tag.getObjectId()))
					.collect(Collectors.toList());

			if (serviceCtrlMappings.size() > 0) {
				List<String> serviceIds = serviceCtrlMappings.stream().map(TagMapping::getTagKey)
						.collect(Collectors.toList());
				log.debug("JIM is enable on {} services. services: {}", serviceIds.size(), serviceIds);
				return serviceIds;
			}
		}
		log.debug("agent list or tag list is empty, hence checking action [JIM enabled] has failed.");
		return new ArrayList<>();

	}
	
	
}

class EscapeHTML extends JsonDeserializer<String> {

	@Override
	public String deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException {
		String s = jp.getValueAsString();
		return StringEscapeUtils.escapeHtml4(s);
	}
}

