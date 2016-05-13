# Developer's guide

One test can be better then dozen lines of documentation, so there are tests in [guide package](https://github.com/mkotsur/restito/blob/master/src/test/java/guide) which illustrate sections from this manual. Don't underestimate them :-)

* [Motivation](#motivation)
* [Starting and stopping stub server](#starting_and_stopping_stub_server)
    * [Specific vs random port](#specific_vs_random_port)
    * [Using HTTPS](#using_https)
    * [Junit integration](#junit_integration)
* [Stubbing server behavior](#stubbing_server_behavior)
    * [Stub conditions](#stub_conditions)
    * [Stub actions](#stub_actions)
    * [Basic authentication](#basic_authentication)
    * [Automatic content type](#automatic_content_type)
    * [Expected stubs](#expected_stubs)
    * [Sequenced stub actions](#sequenced_stub_actions)
    * [Autodiscovery of stubs content](#autodiscovery_of_stubs_content) <sup style="color: orange">Experimental!</sup>
* [Verifying calls to server](#verifying_calls_to_server)
    * [Simple verifications](#simple_verifications)
    * [Limiting number of calls](#limiting_number_of_calls)
    * [Sequenced verifications](#sequenced_verification)
* [Using like a standalone stub server](#using_like_a_standalone_stub_server)
* [Logging](#logging)

<a name="motivation"/>
# Motivation
Let's imagine you have an application or library which uses a REST interface. At some point you would like to make sure that it works exactly as expected and mocking low-level HTTP client is not always the best option. That's where <b>Restito</b> comes to play: it gives you a DSL to test your application with mocked server just like you would do it with any mocking framework.

There is a [nice example](https://github.com/mkotsur/restito/blob/master/examples/popular-page/README.md) of the use case when <b>Restito</b> helps.

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

See [SpecificVsRandomPortTest](https://github.com/mkotsur/restito/blob/master/src/test/java/guide/SpecificVsRandomPortTest.java).

<a name="using_https" />
#Using HTTPS

When you need to use HTTPS, this is just one configuration call...

See [UsingHttpsTest](https://github.com/mkotsur/restito/blob/master/src/test/java/guide/UsingHttpsTest.java).

```java
    server = new StubServer().secured().run();
```

<a name="junit_integration" />
#Junit integration

**!! To use this you must have junit 4.10+ on your classpath. Restito doesn't contain it bundled to save you from the dependency nightmare. !!**

When you use [Junit](http://junit.org) and want to reduce boilerplate code which starts/stops server you can use [@NeedsServer](http://mkotsur.github.com/restito/javadoc/current/com/xebialabs/restito/support/junit/NeedsServer.html) annotation and [ServerDependencyRule](http://mkotsur.github.com/restito/javadoc/current/com/xebialabs/restito/support/junit/ServerDependencyRule.html) to start/stop server in parent class only for cases that require it.

Check [this test](https://github.com/mkotsur/restito/blob/master/src/test/java/guide/JunitIntegrationTest.java) for more details.

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
                then(ok(), stringContent("GET asd with parameter bar=foo"));
```

In this example your stub will return mentioned string content when GET request with HTTP parameter bar=foo is done.

List of all available conditions can be checked in the javadoc for [Condition](http://mkotsur.github.com/restito/javadoc/current/com/xebialabs/restito/semantics/Condition.html)

If you want to use a custom condition, it's also very easy:

```java
import static com.xebialabs.restito.semantics.Condition.*;
import com.xebialabs.restito.semantics.Predicate;
import com.xebialabs.restito.semantics.Call;
    ...
        Predicate<Call> uriEndsWithA = new Predicate<Call>() {
            @Override
            public boolean apply(final Call input) {
                return input.getUri().endsWith("a");
            }
        };
        whenHttp(server).match(custom(uriEndsWithA)).then(ok());
```

Conditions are resolved in reverse order, which makes it easy to have some 'default' behavior.

```java
        whenHttp(server).match(alwaysTrue()).then(status(HttpStatus.OK_200));
        whenHttp(server).match(get("/bad")).then(status(HttpStatus.BAD_REQUEST_400));
```

In this case, when request comes, it will be first tested against last attached condition (i.e. "/bad" URL), and if it doesn't match, will fall back to the first condition which is always true.

If no matching conditions found at all, restito will respond with HTTP status _404 Not Found_.

See [StubConditionsAndActionsTest](https://github.com/mkotsur/restito/blob/master/src/test/java/guide/StubConditionsAndActionsTest.java).

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

Full list of actions can be found in the [appropriate javadoc](http://mkotsur.github.com/restito/javadoc/current/com/xebialabs/restito/semantics/Action.html).

See [StubConditionsAndActionsTest](https://github.com/mkotsur/restito/blob/master/src/test/java/guide/StubConditionsAndActionsTest.java).

<a name="basic_authentication" />
## Stub actions

You can make you server to respond with [basic access authentication](http://en.wikipedia.org/wiki/Basic_access_authentication).

```java
whenHttp(server).match(basicAuth("admin", "secret")).then(status(HttpStatus.OK_200));
whenHttp(server).match(not(basicAuth("admin", "secret"))).then(unauthorized());
```

The first line makes sure that server responds with status `200` when client sends username `admin` and password `secret`, and the second line tells the server to respond with status code `401` and special `WWW-Authenticate` header in other case.

<a name="automatic_content_type" />
## Automatic content type

When you use action [resourceContent\(\)](https://mkotsur.github.io/restito/javadoc/current/com/xebialabs/restito/semantics/Action.html#resourceContent-java.lang.String-), Restito will look at file extension and if it it's one of the types below, appropriate Content-Type will be set:

* .xml => application/xml
* .json => application/json

See [AutomaticContentTypeTest](https://github.com/mkotsur/restito/blob/master/src/test/java/guide/AutomaticContentTypeTest.java).

<a name="expected_stubs" />
## Expected stubs

Makes sure that certain stubbed condition has been called some number of times. See [ExpectedStubTest](https://github.com/mkotsur/restito/blob/master/src/test/java/guide/ExpectedStubTest.java) to learn how to do it.

<a name="sequenced_stub_actions" />
## Sequenced stub actions

You can easily chain stub actions:

```java
    whenHttp(server).
            match(get("/demo")).
            then(status(HttpStatus.OK_200),
                 sequence(stringContent("Hello Restito."),
                          stringContent("Hello, again!"))
            );
```

See [SequencedSubActionsTest](https://github.com/mkotsur/restito/blob/master/src/test/java/guide/SequencedSubActionsTest.java).

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

<a name="verifying_calls_to_server" />
# Verifying calls to server

<a name="simple_verifications" />
## Simple verifications

To verify that some call to the server has happened once, you may use following DSL:

```
    verifyHttp(server).once(
            method(Method.POST),
            uri("/demo"),
            parameter("foo", "bar")
    );
```

For verifications you use the same conditions as for stubbing and complete list of them can be found at [Condition](http://mkotsur.github.com/restito/javadoc/current/com/xebialabs/restito/semantics/Condition.html) javadoc and [custom conditions](https://mkotsur.github.io/restito/javadoc/current/com/xebialabs/restito/semantics/Condition.html#custom-com.xebialabs.restito.semantics.Predicate-) can easily be created.

See [SimpleVerificationsTest](https://github.com/mkotsur/restito/blob/master/src/test/java/guide/SimpleVerificationsTest.java).


<a name="limiting_number_of_calls" />
## Limiting number of calls

You have more options to limit number of calls:

* [never(Condition... conditions)](https://mkotsur.github.io/restito/javadoc/current/com/xebialabs/restito/builder/verify/VerifyHttp.html#never-com.xebialabs.restito.semantics.Condition...-)
* [times(int t, Condition... conditions)](https://mkotsur.github.io/restito/javadoc/current/com/xebialabs/restito/builder/verify/VerifyHttp.html#times-int-com.xebialabs.restito.semantics.Condition...-)

See [LimitingNumberOfCallsTest](https://github.com/mkotsur/restito/blob/master/src/test/java/guide/LimitingNumberOfCallsTest.java).

<a name="sequenced_verification" />
## Sequenced verifications

You can easily chain verifications:

```
    verifyHttp(server).once(
            method(Method.GET),
            uri("/first")
    ).then().once(
            method(Method.GET),
            uri("/second")
    );
```

See [SequencedVerificationsTest](https://github.com/mkotsur/restito/blob/master/src/test/java/guide/SequencedVerificationsTest.java).


<a name="using_like_a_standalone_stub_server" />
## Using like a standalone stub server

It is possible to use <b>Restito</b> as a standalone server (if you need to have a server, which runs continuously, e.g. to develop against it).
There is an [example](https://github.com/mkotsur/restito/blob/master/examples/standalone-server/README.md) how to achieve it.

<a name="logging" />
## Logging

Restito uses [slf4j](http://www.slf4j.org/index.html) as a logging abstraction, which by default does not have any implementations: it collect logs It allows you to receive all the logging via the library, that your application is using.
