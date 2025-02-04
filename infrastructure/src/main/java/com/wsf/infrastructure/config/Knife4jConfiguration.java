//package com.wsf.infrastructure.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import springfox.documentation.builders.ApiInfoBuilder;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.service.Contact;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;
//
//@Configuration
//@EnableSwagger2WebMvc
//public class Knife4jConfiguration {
//
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
//}