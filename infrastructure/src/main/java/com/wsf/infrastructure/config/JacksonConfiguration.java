//package com.wsf.infrastructure.config;
//
//import com.fasterxml.jackson.annotation.JsonAutoDetect;
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.databind.Module;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.ObjectProvider;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
//
///**
// * open
// * 2022/5/30
// */
//@Configuration
//public class JacksonConfiguration {
//    private final static Logger log = LoggerFactory.getLogger(JacksonConfiguration.class);
//    public JacksonConfiguration() {
//    }
//
//    @Autowired
//    public void configObjectMapper(ObjectProvider<ObjectMapper> provider) {
////        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
//        ObjectMapper objectMapper = (ObjectMapper) provider.getIfAvailable();
////        objectMapper.setAnnotationIntrospector(new BoostAnnotationIntrospector());
//        //忽略value为null 时 key的输出
//        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
//
//        objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
//                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
//                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
//                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
//                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
//        objectMapper.registerModule(this.hibernate5Module());
//    }
//
////    @Autowired
////    public void configObjectMapper(ObjectProvider<ObjectMapper> provider) {
////        ObjectMapper objectMapper = (ObjectMapper)provider.getIfAvailable();
////        objectMapper.setAnnotationIntrospector(new BoostAnnotationIntrospector());
////    }
//
//    @Bean
//    public Hibernate5Module hibernate5Module() {
//        Hibernate5Module module = new Hibernate5Module();
//        module.enable(Hibernate5Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS);
//        log.debug("{}",module.isEnabled(Hibernate5Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS));
//        return module;
//    }
//}
