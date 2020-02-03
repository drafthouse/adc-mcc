package com.drafthouse.mcc.domain;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** Annotation for identifying the Market feed HTTP client */
@BindingAnnotation
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface FeedsClient {}
