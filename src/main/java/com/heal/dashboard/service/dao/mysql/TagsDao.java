package com.heal.dashboard.service.dao.mysql;


import com.heal.dashboard.service.beans.TagDetails;
import com.heal.dashboard.service.beans.TagMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
public class TagsDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public List<TagMapping> getTagMappingDetails(int tagId, int objectId, String objectRefTable, int accountId) {
        try {
            String query = "select id, tag_id , object_id , object_ref_table , tag_key , tag_value from tag_mapping where tag_id = ? and object_id = ? and object_ref_table = ? and account_id = ?";
            return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(TagMapping.class), tagId, objectId, objectRefTable, accountId);
        } catch (Exception e) {
            log.error("Error while fetching tag_mapping information. Details: ", e);
        }

        return Collections.emptyList();
    }

    public TagDetails getTagDetails(String name, int accountId) {
        try {
            String query = "select id, name, tag_type_id, is_predefined, ref_table, created_time, updated_time, " +
                    "account_id, user_details_id, ref_where_column_name, ref_select_column_name from tag_details where name = ? and account_id = ?";
            return jdbcTemplate.queryForObject(query, new BeanPropertyRowMapper<>(TagDetails.class), name, accountId);
        } catch (Exception e) {
            log.error("Error while fetching tag_details information for tag name [{}] and accountId [{}]. Details: ", name, accountId, e);
        }
        return null;
    }

    public List<TagMapping> getTagMappingDetailsByTagKey(String tagKey, String objectRefTable, int accountId) {
        try {
            String query = "select id, tag_id, object_id, object_ref_table, tag_key, tag_value from tag_mapping " +
                    "where tag_key = ? and object_ref_table = ? and account_id = ?";
            return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(TagMapping.class), tagKey, objectRefTable, accountId);
        } catch (Exception e) {
            log.error("Error while while fetching tag mapping information for key [{}], reference table [{}] and accountId [{}]. Details: ",
                    tagKey, objectRefTable, accountId, e);
        }

        return Collections.emptyList();
    }
}
