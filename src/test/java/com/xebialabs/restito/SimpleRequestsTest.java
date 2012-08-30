package com.xebialabs.restito;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.specification.RequestSpecification;
import com.xebialabs.restito.calls.CallsHelper;
import com.xebialabs.restito.verify.Condition;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.xebialabs.restito.stubs.StubsHttp.whenHttp;
import static com.xebialabs.restito.verify.Condition.method;
import static com.xebialabs.restito.verify.Condition.parameter;
import static com.xebialabs.restito.verify.Condition.uri;
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
		verifyHttp(server, once()).once(
				method(Method.GET),
				uri("/demo")
		);

	}

	@Test
	public void shouldVerifyGetRequestWithParameters() {
		whenHttp(server).get("/demo").then().status(HttpStatus.OK_200);

		given().param("foo", "bar").get("/demo");

		verifyHttp(server).once(
				method(Method.GET),
				uri("/demo"),
				parameter("foo", "bar")
		);
	}

	@Test(expected = AssertionError.class)
	public void shouldFailWhenParametersExpectedButWrongOnesGiven() {
		whenHttp(server).get("/demo").then().status(HttpStatus.OK_200);

		given().param("foo", "bar").get("/demo");

		verifyHttp(server).once(
				method(Method.GET),
				uri("/demo"),
				parameter("another", "pair")
		);
	}

	@Test(expected = AssertionError.class)
	public void shouldFailWhenMethodIsWrong() {
		whenHttp(server).get("/demo").then().status(HttpStatus.OK_200);

		given().param("foo", "bar").get("/demo");

		verifyHttp(server).once(
				method(Method.POST),
				uri("/demo"),
				parameter("foo", "bar")
		);
	}

}
