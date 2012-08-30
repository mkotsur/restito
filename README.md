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

...

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

}
```