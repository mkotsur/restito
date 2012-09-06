package com.xebialabs.restito;

import com.jayway.restassured.RestAssured;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.server.StubServer;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.xebialabs.restito.builder.StubsHttp.whenHttp;
import static com.xebialabs.restito.builder.VerifyHttp.Times.once;
import static com.xebialabs.restito.builder.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Condition.*;

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
	}

	@Test
	public void shouldRecordGetRequest() {

		// Rest-assured
		expect().statusCode(200).when().get("/demo");

		// Restito
		whenHttp(server).condition(
				Condition.endsWithUri("/demo")
		).then().status(HttpStatus.OK_200);
		verifyHttp(server, once()).once(
				method(Method.GET),
				uri("/demo")
		);

	}

	@Test
	public void shouldVerifyGetRequestWithParameters() {
		whenHttp(server).condition(
				Condition.endsWithUri("/demo")
		).then().status(HttpStatus.OK_200);

		given().param("foo", "bar").get("/demo");

		verifyHttp(server).once(
				method(Method.GET),
				uri("/demo"),
				parameter("foo", "bar")
		);
	}

	@Test(expected = AssertionError.class)
	public void shouldFailWhenParametersExpectedButWrongOnesGiven() {
		whenHttp(server).condition(
				Condition.endsWithUri("/demo")
		).then().status(HttpStatus.OK_200);

		given().param("foo", "bar").get("/demo");

		verifyHttp(server).once(
				method(Method.GET),
				uri("/demo"),
				parameter("another", "pair")
		);
	}

	@Test(expected = AssertionError.class)
	public void shouldFailWhenMethodIsWrong() {
		whenHttp(server).condition(
				Condition.endsWithUri("/demo")
		).then().status(HttpStatus.OK_200);

		given().param("foo", "bar").get("/demo");

		verifyHttp(server).once(
				method(Method.POST),
				uri("/demo"),
				parameter("foo", "bar")
		);
	}

}
