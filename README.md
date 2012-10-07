Restito - testing framework for rest clients
============================================

Restito is a tool which is inspired by [mockito](http://code.google.com/p/mockito/) and functionally is diametrically opposite to the [Rest Assured](http://code.google.com/p/rest-assured).

* Mimics rest server behavior
* Records HTTP calls to the server
* Performs verification against happened calls
* Has mockito-style API

Which means that it helps you to test an application which makes calls to some HTTP service. Restito sets up a <b>StubServer</b> instance which responds to your application's <b>Calls</b> based on defined <b>Stubs</b>. A stub makes some <b>Action</b> to response when <b>Condition</b> is respected.

For more motivation, please read [Motivation](restito/guide.md#motivation) section of the [Developer's guide](restito/guide.md).



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
		whenHttp(server).
				match(endsWithUri("/demo")).
				then(status(HttpStatus.OK_200));

		verifyHttp(server).once(
				method(Method.GET),
				uri("/demo")
		);
	}

}
```