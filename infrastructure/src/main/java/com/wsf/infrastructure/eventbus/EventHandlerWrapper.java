package com.wsf.infrastructure.eventbus;

import com.wsf.domain.events.Event;
import com.wsf.infrastructure.eventbus.annotation.EventSubscribe;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * 事件处理器包装器
 * 封装事件处理器和过滤条件
 */
public class EventHandlerWrapper<T extends Event> {

    private final EventHandler<T> handler;
    private final int order;
    private final Expression conditionExpression;
    private final StandardEvaluationContext evaluationContext;

    public EventHandlerWrapper(EventHandler<T> handler, EventSubscribe annotation) {
        this.handler = handler;
        this.order = annotation.order();
        
        // 解析条件表达式
        ExpressionParser parser = new SpelExpressionParser();
        if (annotation.condition() != null && !annotation.condition().isEmpty()) {
            this.conditionExpression = parser.parseExpression(annotation.condition());
        } else {
            this.conditionExpression = null;
        }
        
        this.evaluationContext = new StandardEvaluationContext();
    }

    /**
     * 设置 Spring BeanFactory，用于在 SpEL 表达式中访问 Spring Bean
     */
    public void setBeanFactoryResolver(BeanFactoryResolver beanFactoryResolver) {
        if (evaluationContext != null) {
            evaluationContext.setBeanResolver(beanFactoryResolver);
        }
    }

    /**
     * 检查是否应该处理该事件
     */
    public boolean shouldHandle(T event) {
        if (conditionExpression == null) {
            return true;
        }
        
        try {
            evaluationContext.setVariable("event", event);
            Object result = conditionExpression.getValue(evaluationContext);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            // 表达式执行失败时，默认不处理
            return false;
        }
    }

    /**
     * 处理事件
     */
    public void handle(T event) {
        if (shouldHandle(event)) {
            handler.handle(event);
        }
    }

    public EventHandler<T> getHandler() {
        return handler;
    }

    public int getOrder() {
        return order;
    }
}