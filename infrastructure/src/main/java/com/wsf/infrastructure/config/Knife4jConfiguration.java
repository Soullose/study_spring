package com.wsf.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.apache.commons.lang3.RandomStringUtils;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class Knife4jConfiguration {

//    @Bean(value = "knife4jConfigurationBean")
//    public Docket knife4jConfigurationBean() {
//        //指定使用Swagger2规范
//        return new Docket(DocumentationType.SWAGGER_2)
//                .apiInfo(new ApiInfoBuilder()
//                        //描述字段支持Markdown语法
//                        .title("接口文档")
//                        .description("# spring-template RESTful APIs")
//                        .termsOfServiceUrl("https://w2.api.cn/")
//                        .contact(new Contact("w2", "", "xxx@xxx.com"))
//                        .version("1.0")
//                        .build())
//                //分组名称
//                .groupName("学习Spring")
//                .select()
//                //这里指定Controller扫描包路径
//                .apis(RequestHandlerSelectors.basePackage("com.wsf.controller"))
//                .paths(PathSelectors.any())
//                .build();
//    }

    /**
     * 根据@Tag 上的排序，写入x-order
     *
     * @return the global open api customizer
     */
    @Bean
    public GlobalOpenApiCustomizer orderGlobalOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getTags() != null) {
                openApi.getTags().forEach(tag -> {
                    Map<String, Object> map = new HashMap<>();

                    map.put("x-order", RandomStringUtils.secure().next(3, 0, 100, false, false));
                    tag.setExtensions(map);
                });
            }
            if (openApi.getPaths() != null) {
                openApi.addExtension("x-test123", "333");
                openApi.getPaths().addExtension("x-abb", RandomStringUtils.secure().next(3, 0, 100, false, false));
            }

        };
    }


    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("XXX用户系统API")
                        .version("1.0")

                        .description("Knife4j集成springdoc-openapi示例")
                        .termsOfService("http://doc.xiaominfo.com")
                        .license(new License().name("Apache 2.0")
                                .url("http://doc.xiaominfo.com")));
    }

}