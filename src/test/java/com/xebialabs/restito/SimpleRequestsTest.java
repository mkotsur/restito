package com.xebialabs.restito;

import com.jayway.restassured.RestAssured;
import com.xebialabs.restito.calls.CallsHelper;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.expect;
import static com.xebialabs.restito.stubs.StubsHttp.whenHttp;
import static com.xebialabs.restito.verify.VerifyHttp.Times.once;
import static com.xebialabs.restito.verify.VerifyHttp.verifyHttp;

public class SimpleRequestsTest {

	StubServer server;


	@Before
	public void start() {
		server = new StubServer().run();
		RestAssured.port = 6666;
	}

	@After
	public void stop() {
		server.stop();
		CallsHelper.logCalls(server.getCalls());
	}

	@Test
	public void shouldRecordGetRequest() {

		// Rest-assured
		expect().statusCode(200).when().get("/demo");

		// Restito
		whenHttp(server).get("/demo").then().status(HttpStatus.OK_200);
		verifyHttp(server, once()).get("/demo");

	}

}
