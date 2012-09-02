package com.xebialabs.restito.support.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ServerDependencyRule implements TestRule {

	private NeedsServer annotation;

	@Override
	public Statement apply(Statement base, Description description) {
		annotation = description.getAnnotation(NeedsServer.class);
		return base;
	}

	public boolean isServerDependent() {
		return annotation != null;
	}
}
