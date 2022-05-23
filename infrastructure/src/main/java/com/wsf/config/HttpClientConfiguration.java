package com.wsf.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * open
 * SoulLose
 * 2022-05-20 14:23
 */
@Configuration
public class HttpClientConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientConfiguration.class);
    
    //最大连接数
    private static final Integer MAX_TOTAL = 3000;
    //最大并发数
    private static final Integer DEFAULT_MAX_CONCURRENCY = 20;
    //连接的最长时间(毫秒)
    private static final Integer CONNECT_TIMEOUT = 1000;
    //从连接池中获取到连接的最长时间(毫秒)
    private static final Integer CONNECTION_REQUEST_TIMEOUT = 500;
    //数据传输的最长时间(毫秒)
    private static final Integer SOCKET_TIMEOUT = 10000;
    //提交请求前测试连接是否可用
    private static final boolean CONNECTION_IS_AVAILABLE = true;
    
    @Bean
    public PoolingHttpClientConnectionManager httpClientConnectionManager() {
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        poolingHttpClientConnectionManager.setMaxTotal(MAX_TOTAL);
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_CONCURRENCY);
        poolingHttpClientConnectionManager.setValidateAfterInactivity(2000);
        return poolingHttpClientConnectionManager;
    }
    
    @Bean
    public RequestConfig requestConfig() {
        return RequestConfig.custom().setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)  //从链接池获取连接的超时时间
                .setConnectTimeout(CONNECT_TIMEOUT)    //与服务器连接超时时间，创建socket连接的超时时间
                .setSocketTimeout(SOCKET_TIMEOUT)   //socket读取数据的超时时间，从服务器获取数据的超时时间
                .build();
    }
    
    @Bean
    public HttpClientBuilder httpClientBuilder(PoolingHttpClientConnectionManager httpClientConnectionManager) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        //设置连接池
        httpClientBuilder.setConnectionManager(httpClientConnectionManager);
        //设置超时时间
        httpClientBuilder.setDefaultRequestConfig(requestConfig());
        return httpClientBuilder;
    }
    
    @Bean
    public CloseableHttpClient closeableHttpClient(HttpClientBuilder httpClientBuilder) {
        return httpClientBuilder.build();
    }
    

}
