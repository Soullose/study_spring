//package com.wsf.infrastructure.datasource.aspect;
//
//import com.wsf.infrastructure.datasource.annotation.DynamicDataSource;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.After;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.BeanFactory;
//import org.springframework.context.expression.BeanFactoryResolver;
//import org.springframework.core.annotation.Order;
//import org.springframework.expression.Expression;
//import org.springframework.expression.ExpressionParser;
//import org.springframework.expression.spel.standard.SpelExpressionParser;
//import org.springframework.expression.spel.support.StandardEvaluationContext;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import java.lang.reflect.Method;
//
///**
// * 动态数据源切换切面
// * 支持SpEL表达式解析
// * Order设置为最高优先级，确保在事务之前执行
// */
//@Aspect
//@Component
//@Order(-1000)  // 最高优先级，确保在事务切面之前执行
//public class DynamicDataSourceAspect {
//
//    private static final Logger logger = LoggerFactory.getLogger(DynamicDataSourceAspect.class);
//
//    private final ExpressionParser parser = new SpelExpressionParser();
//    private final BeanFactory beanFactory;
//
//    public DynamicDataSourceAspect(BeanFactory beanFactory) {
//        this.beanFactory = beanFactory;
//    }
//
//    @Before("@annotation(dynamicDataSource)")
//    public void beforeSwitchDataSource(JoinPoint joinPoint, DynamicDataSource dynamicDataSource) {
//        String dataSourceName = null;
//
//        // 1. 如果注解指定了数据源名称（包括SpEL表达式）
//        if (StringUtils.hasText(dynamicDataSource.value())) {
//            dataSourceName = evaluateSpelExpression(joinPoint, dynamicDataSource.value());
//            logger.debug("从注解获取数据源: {}", dataSourceName);
//        }
//
//        // 2. 如果注解没有指定数据源，或者指定了但force=false，则检查拦截器是否已设置
//        if (!dynamicDataSource.force() &&
//                (dataSourceName == null || dataSourceName.isEmpty())) {
//            // 检查拦截器是否已经设置了数据源
//            String interceptorDataSource = DataSourceContextHolder.getDataSource();
//            if (interceptorDataSource != null) {
//                logger.debug("使用拦截器设置的数据源: {}", interceptorDataSource);
//                return;  // 拦截器已设置，直接使用
//            }
//        }
//
//        // 3. 设置数据源
//        if (dataSourceName != null && !dataSourceName.isEmpty()) {
//            DataSourceContextHolder.setDataSource(dataSourceName);
//            logger.debug("设置数据源: {}", dataSourceName);
//        } else {
//            // 没有指定数据源，使用默认
//            DataSourceContextHolder.useDefaultDataSource();
//            logger.debug("使用默认数据源");
//        }
//    }
//
//    @After("@annotation(dynamicDataSource)")
//    public void afterSwitchDataSource(JoinPoint joinPoint, DynamicDataSource dynamicDataSource) {
//        // 只有在注解强制指定了数据源时才清除，否则让拦截器负责清除
//        if (dynamicDataSource.force() || StringUtils.hasText(dynamicDataSource.value())) {
//            DataSourceContextHolder.clearDataSource();
//            logger.debug("清除注解设置的数据源");
//        }
//    }
//
//    /**
//     * 解析SpEL表达式
//     */
//    private String evaluateSpelExpression(JoinPoint joinPoint, String expressionStr) {
//        try {
//            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//            Method method = signature.getMethod();
//            Object[] args = joinPoint.getArgs();
//
//            StandardEvaluationContext context = new StandardEvaluationContext();
//
//            // 设置方法参数
//            String[] parameterNames = signature.getParameterNames();
//            if (parameterNames != null) {
//                for (int i = 0; i < parameterNames.length; i++) {
//                    context.setVariable(parameterNames[i], args[i]);
//                }
//            }
//
//            // 设置Spring Bean解析器
//            context.setBeanResolver(new BeanFactoryResolver(beanFactory));
//
//            // 解析表达式
//            Expression expression = parser.parseExpression(expressionStr);
//            Object result = expression.getValue(context);
//
//            return result != null ? result.toString() : null;
//
//        } catch (Exception e) {
//            logger.error("解析SpEL表达式失败: {}", expressionStr, e);
//            return null;
//        }
//    }
//}
