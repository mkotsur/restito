package com.xebialabs.restito.support.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

/**
 * This annotation can be used together with {@link ServerDependencyRule} annotation. It allows to start/stop server from base test case reducing boilerplate in your test classes.
 */
public @interface NeedsServer {}
