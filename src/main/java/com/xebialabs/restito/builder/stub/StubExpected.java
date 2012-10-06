package com.xebialabs.restito.builder.stub;

import com.xebialabs.restito.semantics.Stub;

/**
 * <p>Stub which must be called.</p>
 * @see com.xebialabs.restito.builder.ensure.EnsureHttp
 */
public class StubExpected {

	private Stub stub;

	public StubExpected(Stub stub) {
		this.stub = stub;
	}

	public void mustHappen() {
		stub.setExpectedTimes(1);
	}
}
