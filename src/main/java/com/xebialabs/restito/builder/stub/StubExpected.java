package com.xebialabs.restito.builder.stub;

import com.xebialabs.restito.semantics.Stub;

public class StubExpected {

	private Stub stub;

	public StubExpected(Stub stub) {
		this.stub = stub;
	}

	public void mustHappen() {
		stub.setExpectedTimes(1);
	}
}
