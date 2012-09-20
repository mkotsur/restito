package com.xebialabs.restito;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.jayway.restassured.RestAssured;

import com.xebialabs.restito.server.StubServer;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.xebialabs.restito.builder.ensure.EnsureHttp.ensureHttp;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Action.success;
import static com.xebialabs.restito.semantics.Condition.composite;
import static com.xebialabs.restito.semantics.Condition.endsWithUri;
import static com.xebialabs.restito.semantics.Condition.get;
import static com.xebialabs.restito.semantics.Condition.method;
import static com.xebialabs.restito.semantics.Condition.parameter;
import static com.xebialabs.restito.semantics.Condition.post;
import static com.xebialabs.restito.semantics.Condition.uri;
import static org.hamcrest.Matchers.equalTo;

public class SimpleRequestsTest {

	StubServer server;

	@Before
	public void start() {
		server = new StubServer().run();
		RestAssured.port = server.getPort();
	}

	@After
	public void stop() {
		server.stop();
	}

	@Test
	public void shouldRecordGetRequest() {

		// Restito
		whenHttp(server).
				match(endsWithUri("/demo")).
				then(status(HttpStatus.OK_200));

		// Rest-assured
		expect().statusCode(200).when().get("/demo");

		// Restito
		verifyHttp(server).once(
				method(Method.GET),
				uri("/demo")
		);
	}

	@Test
	public void shouldVerifyGetRequestWithParameters() {
		whenHttp(server).
				match(endsWithUri("/demo")).
				then(status(HttpStatus.OK_200));

		given().param("foo", "bar").get("/demo");

		verifyHttp(server).once(
				method(Method.GET),
				uri("/demo"),
				parameter("foo", "bar")
		);
	}

	@Test(expected = AssertionError.class)
	public void shouldFailWhenParametersExpectedButWrongOnesGiven() {
		whenHttp(server).
				match(endsWithUri("/demo")).
				then(status(HttpStatus.OK_200));

		given().param("foo", "bar").get("/demo");

		verifyHttp(server).once(
				method(Method.GET),
				uri("/demo"),
				parameter("another", "pair")
		);
	}

	@Test(expected = AssertionError.class)
	public void shouldFailWhenMethodIsWrong() {
		whenHttp(server).
				match(endsWithUri("/demo")).
				then(status(HttpStatus.OK_200));

		given().param("foo", "bar").get("/demo");

		verifyHttp(server).once(
				method(Method.POST),
				uri("/demo"),
				parameter("foo", "bar")
		);
	}

	@Test
	public void shouldReturn404forNotDefinedUris() {
		whenHttp(server).match(endsWithUri("/asd")).then(success());

		given().param("foo", "bar").
		expect().statusCode(404).
		when().get("/undefined");
	}

	@Test
	public void shouldReturnProperContentForProperRequests() {
		whenHttp(server).
				match(get("/asd")).
				then(success(), stringContent("GET asd"));

		whenHttp(server).
				match(post("/asd")).
				then(success(), stringContent("POST asd"));

		whenHttp(server).
				match(get("/asd"), parameter("bar", "foo")).
				then(success(), stringContent("GET asd with parameter"));

		expect().statusCode(200).and().body(equalTo("GET asd")).
		when().get("/asd");

		given().param("bar", "foo").
		expect().statusCode(200).and().body(equalTo("GET asd with parameter")).
		when().get("/asd");

		expect().statusCode(200).and().body(equalTo("POST asd")).
		when().post("/asd");
	}

	@Test(expected = AssertionError.class)
	public void shouldFailWhenSecondExpectedStubDidNotHappen() {
		whenHttp(server).match(get("/asd")).then(success()).mustHappen();
		whenHttp(server).match(get("/neverHappens")).then(success()).mustHappen();
		expect().statusCode(200).get("/asd");
		ensureHttp(server).gotStubsCommitmentsDone();
	}

	@Test
	public void shouldPassWhenExpectedStubDidHappen() {
		whenHttp(server).match(get("/asd")).then(success()).mustHappen();
		expect().statusCode(200).get("/asd");
		ensureHttp(server).gotStubsCommitmentsDone();
	}

	@Test
	public void shouldPassWhenNoStubCommitments() {
		ensureHttp(server).gotStubsCommitmentsDone();
	}

    @Test
    public void shouldAutoDiscoverResponseForGetRequestBasedOnUri() {
        whenHttp(server).match(composite(get("/demo/path%20to%20data/data"))).then(success());
        expect().statusCode(200).and().body(equalTo("<content>from data.xml</content>")).when().get("/demo/path to data/data");
    }

}
