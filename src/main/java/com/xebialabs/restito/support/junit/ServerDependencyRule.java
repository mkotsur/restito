package com.xebialabs.restito.support.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * This rule can be used together with {@link NeedsServer} annotation. It allows to start/stop server from base test case reducing boilerplate in your test classes.
 */
public class ServerDependencyRule implements TestRule {

    private NeedsServer annotation;

    @Override
    public Statement apply(Statement base, Description description) {
        annotation = description.getAnnotation(NeedsServer.class);
        return base;
    }

    /**
     * Returns true when current test case is marked with {@link NeedsServer} and thus needs the server to be started.
     */
    public boolean isServerDependent() {
        return annotation != null;
    }
}
