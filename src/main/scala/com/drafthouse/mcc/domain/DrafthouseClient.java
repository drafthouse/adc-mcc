package com.drafthouse.mcc.domain;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** Annotation for identifying the drafthouse.com HTTP client used for EE and Mother endpoints */
@BindingAnnotation
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface DrafthouseClient {}
