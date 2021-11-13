package com.heal.dashboard.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan
@PropertySource(value="classpath:conf.properties")
@Slf4j
public class HealServiceMain {
    public static void main(String[] args) {
        log.info("Heal dashboard service main is starting.......");
       // System.setProperty("logging.config", "classpath:logback.xml");
        SpringApplication.run(HealServiceMain.class, args);
    }
}
