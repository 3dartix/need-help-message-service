package ru.pugart.messag.eservice.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
@Slf4j
public class AppConfig {
    private Integer limitMessage;

    @PostConstruct
    private void init(){
        log.info("app configuration: {}", this);
    }
}
