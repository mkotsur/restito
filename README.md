Restito - testing framework for rest clients
============================================

Restito is a tool which functionally is diametrically opposite to the [Rest Assured](http://code.google.com/p/rest-assured).

* Mimics rest server behavior
* Records HTTP calls to the server
* Has mockito-style API

Example:
---------
```java
package com.xebialabs.restito;

import com.jayway.restassured.RestAssured;
import com.xebialabs.restito.calls.CallsHelper;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.*;

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
```