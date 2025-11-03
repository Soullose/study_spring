package com.wsf.domain.annotation;

import com.wsf.domain.CustomIdGenerator;
import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@IdGeneratorType(CustomIdGenerator.class)
public @interface BaseId {
}