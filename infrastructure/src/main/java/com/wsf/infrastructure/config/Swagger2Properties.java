package com.wsf.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * open
 * SoulLose
 * 2022-05-26 22:00
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "open.swagger2",ignoreUnknownFields = true)
public class Swagger2Properties {
    private String title;
    private String description;
    private String version;
    private String termsOfServiceUrl;
    private String license;
    private String licenseUrl;
    private Map<String, Swagger2Properties.Api> apis;
    private Swagger2Properties.Contact contact;
    
    @Getter
    @Setter
    public static class Api {
        private String name;
        private List<String> packages;
        
        public Api() {
        }
    }
    @Getter
    @Setter
    public static class Contact {
        private String name;
        private String url;
        private String email;
    
        public Contact() {
        }
    }
}
