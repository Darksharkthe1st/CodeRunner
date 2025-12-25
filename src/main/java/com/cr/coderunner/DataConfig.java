package com.cr.coderunner;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//Set proxyBeanMethods to true to have one shared configuration across all methods
@Configuration(proxyBeanMethods = true)
public class DataConfig {

    @Bean
    @ConfigurationProperties("app.user")
    public UserData getUserData() {
        return new UserData("username");
    }
}
