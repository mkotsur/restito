package com.xebialabs.restito.builder.ensure;

import com.xebialabs.restito.semantics.Stub;
import com.xebialabs.restito.server.StubServer;

import java.util.List;

import static java.lang.String.format;
import static junit.framework.Assert.assertEquals;

public class EnsureHttp {

	private List<Stub> stubs;

	private EnsureHttp(List<Stub> stubs) {
		this.stubs = stubs;
	}

	public static EnsureHttp ensureHttp(final StubServer stubServer) {
		return new EnsureHttp(stubServer.getStubs());
	}

	public void gotStubsCommitmentsDone() {
		for (Stub stub : stubs) {
			assertEquals(
					format("Expected stub %s to be called %s times, called %s times instead", stub.toString(), stub.getExpectedTimes(), stub.getAppliedTimes()),
					stub.getExpectedTimes(), stub.getAppliedTimes()
			);
		}

	}
}
