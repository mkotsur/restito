package com.xebialabs.restito.builder.stub;

import com.google.common.base.Functions;
import com.google.common.base.Predicates;
import com.jayway.restassured.RestAssured;
import com.xebialabs.restito.semantics.Action;
import com.xebialabs.restito.semantics.Call;
import com.xebialabs.restito.server.StubServer;
import org.glassfish.grizzly.http.server.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static com.xebialabs.restito.builder.ensure.EnsureHttp.ensureHttp;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Condition.custom;

public class StubExpectedTest {

	private StubServer server = new StubServer();
	@Before
	public void start() {
		server = new StubServer().run();
		RestAssured.port = 6666;
	}

	@After
	public void stop() {
		server.stop();
	}

	@Test
	public void shouldPassWhenStubTriggered() {

		whenHttp(server).
				match(custom(Predicates.<Call>alwaysTrue())).
				then(Action.custom(Functions.<Response>identity())).
				mustHappen();

		given().param("foo", "bar").get("/demo");

		ensureHttp(server).gotStubsCommitmentsDone();
	}

	@Test(expected = AssertionError.class)
	public void shouldFailWhenStubNotTriggered() {

		StubServer stubServer = new StubServer();
		whenHttp(stubServer).
				match(custom(Predicates.<Call>alwaysFalse())).
				then(Action.custom(Functions.<Response>identity())).
				mustHappen();


		ensureHttp(stubServer).gotStubsCommitmentsDone();
	}
}
