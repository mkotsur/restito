Restito - testing framework for rest clients [![Build Status](https://circleci.com/gh/mkotsur/restito.svg?&style=shield&circle-token=2cd0c54c357ba4e7962777c4fde462c86a1aa194)](https://circleci.com/gh/mkotsur/restito)
[![Maven Central](https://img.shields.io/maven-central/v/com.xebialabs.restito/restito.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.xebialabs.restito%22)
============================================

Restito is a tool for validating your code's interactions with REST services. It provides the Middle Way between hammering real HTTP services from your tests (thus making them brittle) and mocking too much and not testing the HTTP communication layer at all.     
Inspired by [mockito](http://code.google.com/p/mockito/) and [Rest Assured](https://github.com/rest-assured/rest-assured), Restito provides a handy DSL for:
* Mimicking a behaviour of a REST server from your tests;
* Recording your code's HTTP calls to the server and verifying them;
* Integration with JUnit;
* Avoiding boilerplate code.

It helps you to test an application which makes calls to some HTTP service. Restito sets up a [StubServer](http://mkotsur.github.io/restito/javadoc/current/com/xebialabs/restito/server/StubServer.html) instance which responds to your application's [Calls](http://mkotsur.github.io/restito/javadoc/current/com/xebialabs/restito/semantics/Call.html) based on defined [Stubs](http://mkotsur.github.io/restito/javadoc/current/com/xebialabs/restito/semantics/Stub.html). A stub makes some [Action](http://mkotsur.github.io/restito/javadoc/current/com/xebialabs/restito/semantics/Action.html) to response when [Condition](http://mkotsur.github.io/restito/javadoc/current/com/xebialabs/restito/semantics/Condition.html) is respected.

For more motivation, please read [Motivation](https://github.com/mkotsur/restito/blob/master/guide.md#motivation) section of the [Developer's guide](https://github.com/mkotsur/restito/blob/master/guide.md).

Developer's guide is the best place to start. FOR LOTS OF EXAMPLES CLICK [**-> HERE <-**](https://github.com/mkotsur/restito/blob/master/guide.md) :-)

For more details you can also check [Restito's javadoc](http://mkotsur.github.io/restito/javadoc/current/).

# Quick example:

```java
package com.xebialabs.restito;

...

public class SimpleRequestsTest {

    private StubServer server;

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
    public void shouldPassVerification() {
        // Restito
        whenHttp(server).
                match(get("/demo")).
                then(status(HttpStatus.OK_200));

        // Rest-assured
        expect().statusCode(200).when().get("/demo");

        // Restito
        verifyHttp(server).once(
                method(Method.GET),
                uri("/demo")
        );
    }

}
```

# Version compatibility

* Use 1.x if you run JDK 11+;
* Use [0.9.x](https://github.com/mkotsur/restito/tree/0.9.x) if you run JDK 8+

# Maven instructions

```
<dependency>
    <groupId>com.xebialabs.restito</groupId>
    <artifactId>restito</artifactId>
    <version>1.0.0</version>
</dependency>
```


# Building instructions

```
$ gradle clean build
```
