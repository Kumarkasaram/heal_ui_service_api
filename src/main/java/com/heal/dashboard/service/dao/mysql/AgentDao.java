package com.heal.dashboard.service.dao.mysql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
public class AgentDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public List<Integer> getJimAgentIds(int agentTypeTId) {
        try {
            String query = "select id from agent where agent_type_id=?";
            return jdbcTemplate.query(query, (rs, rowNum) -> rs.getInt("id"), agentTypeTId);
        } catch (Exception e) {
            log.error("Error while fetching JIM agents. Details: ", e);
        }

		return Collections.emptyList();
    }
}
