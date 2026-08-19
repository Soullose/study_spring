package com.wsf.infrastructure.jpa.id.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.hibernate.annotations.IdGeneratorType;

import com.wsf.infrastructure.jpa.id.CustomIdGenerator;

@Target({FIELD})
@Retention(RUNTIME)
@IdGeneratorType(CustomIdGenerator.class)
public @interface BaseId {
}