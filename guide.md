# Developer's guide

One test can be better then dozen lines of documentation, so there are tests in [guide package](https://github.com/mkotsur/restito/blob/master/src/test/java/guide) which illustrate sections from this manual. Don't underestimate them :-)

* [Motivation](#motivation)
* [Starting and stopping stub server](#starting_and_stopping_stub_server)
    * [Specific vs random port](#specific_vs_random_port)
    * [Junit integration](#junit_integration)
* [Stubbing server behavior](#stubbing_server_behavior)
    * [Stub conditions](#stub_conditions)
    * [Stub actions](#stub_actions)
    * [Automatic content type](#automatic_content_type)
    * [Expected stubs](#expected_stubs)
    * [Autodiscovery of stubs content](#autodiscovery_of_stubs_content) <sup style="color: orange">Experimental!</sup>
* [Verifying calls to server](#mimic_server_behavior)
    * [Simeple verifications](#simple_verifications)
    * [Limiting number of calls](#limiting_number_of_calls)
    * [Sequenced verifications](#sequenced_verification)

<a name="motivation"/>
# Motivation
Let's imagine you have an application or library which uses a REST interface. At some point you would like to make sure that it works exactly as expected and mocking low-level HTTP client is not always the best option. That's where <b>Restito</b> comes to play: it gives you a DSL to test your application with mocked server just like you would do it with any mocking framework.

<a name="starting_and_stopping_stub_server"/>
# Starting and stopping stub server

```java

	@Before
	public void start() {
		server = new StubServer().run();
	}

	...

	@After
    public void stop() {
        server.stop();
    }
```

<a name="specific_vs_random_port" />
## Specific vs random port

By default, [StubServer.DEFAULT_PORT](http://mkotsur.github.com/restito/javadoc/current/com/xebialabs/restito/server/StubServer.html#DEFAULT_PORT) is used, but if it's busy, then next available will be taken.

```java
	@Test
	public void shouldStartServerOnRandomPortWhenDefaultPortIsBusy() {
		StubServer server1 = new StubServer().run();
		StubServer server2 = new StubServer().run();
		assertTrue(server2.getPort() > server1.getPort());
	}
```
If you want to specify port explicitly, then you can do something like that:

```java
    @Test
    public void shouldUseSpecificPort() {
        StubServer server = new StubServer(8888).run();
        assertEquals(8888, server.getPort());
    }
```

<a name="junit_integration" />
#Junit integration
When you use [Junit](http://junit.org) and want to reduce boilerplate code which starts/stops server you can use [@NeedsServer](http://mkotsur.github.com/restito/javadoc/current/com/xebialabs/restito/support/junit/NeedsServer.html) annotation and [ServerDependencyRule](http://mkotsur.github.com/restito/javadoc/current/com/xebialabs/restito/support/junit/ServerDependencyRule.html) to start/stop server in parent class only for cases that require it. Check (this test)[https://github.com/mkotsur/restito/blob/master/src/test/java/guide/JunitIntegrationTest.java] for more details.

<a name="stubbing_server_behavior" />
# Stubbing server behavior.


<a name="stub_conditions" />
## Stub conditions
_In fact, Restito's stub server is not just a stub. It also behaves like a mock and spy object according to M. Fowler's terminology. However it's called just a StubServer._

Stubbing is a way to teach server to behave as you wish.

```java
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.*;
    ...
		whenHttp(server).
				match(get("/asd"), parameter("bar", "foo")).
				then(success(), stringContent("GET asd with parameter bar=foo"));
```

In this example your stub will return mentioned string content when GET request with HTTP parameter bar=foo is done.

List of all avaialble conditions can be checked in the javadoc for [Condition](http://mkotsur.github.com/restito/javadoc/current/com/xebialabs/restito/semantics/Condition.html)

If you want to use a custom condition, it's also very easy:

```java
import static com.xebialabs.restito.semantics.Condition.*;
import com.google.common.base.Predicate;
import com.xebialabs.restito.semantics.Call;
    ...
        Predicate<Call> uriEndsWithA = new Predicate<Call>() {
            @Override
            public boolean apply(final Call input) {
                return input.getUri().endsWith("a");
            }
        };
        whenHttp(server).match(custom(uriEndsWithA)).then(success());
```

<a name="stub_actions" />
## Stub actions

Action is a second component of Stubs. When condition is met, action will be performed on the response (like adding content, setting header, etc.)

```java
import static com.xebialabs.restito.semantics.Action.*;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Condition.endsWithUri;
import static com.xebialabs.restito.semantics.Action.stringContent;
    ...
        whenHttp(server).
                match(endsWithUri("/demo")).
                then(status(HttpStatus.OK_200), stringContent("Hello world!"));
```
This example will make your stub output "Hello world!" with http status 200 for all types of requests (POST, GET, PUT, ...) when URI ends with "/demo".

Full list of actions can be found in the [appropriate javadoc](http://mkotsur.github.com/restito/javadoc/current/com/xebialabs/restito/semantics/Condition.html).

<a name="automatic_content_type" />
## Automatic content type

When you use action [resourceContent\(\)](http://mkotsur.github.com/restito/javadoc/current/com/xebialabs/restito/semantics/Action.html#resourceContent\(java.lang.String\)), Restito will look at file extension and it it's one of the types below, appropriate Content-Type will be set:

* .xml => application/xml
* .json => application/json

See [AutomaticContentTypeTest](https://github.com/mkotsur/restito/blob/master/src/test/java/guide/AutomaticContentTypeTest.java) for more details.

<a name="expected_stubs" />
## Expected stubs

Makes sure that certain stubbed condition has been called some number of times. See [StubExpectedTest](https://github.com/mkotsur/restito/blob/master/src/test/java/guide/StubExpectedTest.java) to learn how to do it.

<a name="autodiscovery_of_stubs_content" />
## Autodiscovery of stubs content

**This is an experimental feature, api will be changed in next releases**

When you use _get()_, _put()_, _post()_ or _delete()_ condition, Restito will try to find resource on the classpath according to the rules defined defined below in the same order:

* GET asd/bsd/asd => resource: restito/get.asd.bsd.asd
* GET asd/bsd/asd => resource: restito/get/asd/bsd/asd
* GET asd/bsd/asd => resource: restito/asd.bsd.asd
* GET asd/bsd/asd => resource: restito/asd/bsd/asd
* GET asd/bsd/asd => resource: restito/get.asd.bsd.asd.xml
* GET asd/bsd/asd => resource: restito/get/asd/bsd/asd.xml
* GET asd/bsd/asd => resource: restito/asd.bsd.asd.xml
* GET asd/bsd/asd => resource: restito/asd/bsd/asd.xml
* GET asd/bsd/asd => resource: restito/get.asd.bsd.asd.json
* GET asd/bsd/asd => resource: restito/get/asd/bsd/asd.json
* GET asd/bsd/asd => resource: restito/asd.bsd.asd.json
* GET asd/bsd/asd => resource: restito/asd/bsd/asd.json

See [AutodiscoveryOfStubsContentTest](https://github.com/mkotsur/restito/blob/master/src/test/java/guide/AutodiscoveryOfStubsContentTest.java) to get some inspiration.





