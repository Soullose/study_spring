package com.wsf.infrastructure.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * open
 * SoulLose
 * 2022-05-20 16:07
 */
@Configuration
public class RestTemplateConfig {
    
    private CloseableHttpClient httpClient;
    
    public RestTemplateConfig(CloseableHttpClient closeableHttpClient) {
        this.httpClient = closeableHttpClient;
    }
    
    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory(){
        HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpComponentsClientHttpRequestFactory.setHttpClient(httpClient);
        return httpComponentsClientHttpRequestFactory;
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(clientHttpRequestFactory());
    }
}
