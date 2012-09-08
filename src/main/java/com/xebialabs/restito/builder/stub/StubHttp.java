package com.xebialabs.restito.builder.stub;

import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.server.StubServer;

public class StubHttp {

	private StubServer stubServer;

	private StubHttp(StubServer stubServer) {
		this.stubServer = stubServer;
	}

	public static StubHttp whenHttp(StubServer server) {
		return new StubHttp(server);
	}

	public StubActioned match(Condition... conditions) {
		return new StubActioned(stubServer, Condition.composite(conditions));
	}
}
