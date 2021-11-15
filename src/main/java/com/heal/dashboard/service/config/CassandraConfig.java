package com.heal.dashboard.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories(basePackages ="com.heal.dashboard.service.config")
public class CassandraConfig  extends AbstractCassandraConfiguration{

	    @Value("${spring.data.cassandra.keyspace:appsone}")
	    private String keySpace;

		@Override
		protected String getKeyspaceName() {
			// TODO Auto-generated method stub
			return keySpace;
		}
		protected boolean getMetricsEnabled() { return false; }
}
