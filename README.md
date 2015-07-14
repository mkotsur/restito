Restito - testing framework for rest clients [![Build Status](https://buildhive.cloudbees.com/job/mkotsur/job/restito/badge/icon)](https://buildhive.cloudbees.com/job/mkotsur/job/restito/)
============================================

Restito is a tool which is inspired by [mockito](http://code.google.com/p/mockito/) and functionally is diametrically opposite to the [Rest Assured](http://code.google.com/p/rest-assured).

Restito provides a DSL to:

* Mimic rest server behavior
* Record HTTP calls to the server
* Perform verification against happened calls

Which means that it helps you to test an application which makes calls to some HTTP service. Restito sets up a [StubServer](http://mkotsur.github.com/restito/javadoc/current/com/xebialabs/restito/server/StubServer.html) instance which responds to your application's [Calls](http://mkotsur.github.com/restito/javadoc/current/com/xebialabs/restito/semantics/Call.html) based on defined [Stubs](http://mkotsur.github.com/restito/javadoc/current/com/xebialabs/restito/semantics/Stub.html). A stub makes some [Action](http://mkotsur.github.com/restito/javadoc/current/com/xebialabs/restito/semantics/Action.html) to response when [Condition](http://mkotsur.github.com/restito/javadoc/current/com/xebialabs/restito/semantics/Condition.html) is respected.

For more motivation, please read [Motivation](https://github.com/mkotsur/restito/blob/master/guide.md#motivation) section of the [Developer's guide](https://github.com/mkotsur/restito/blob/master/guide.md).

Developer's guide is the best place to start. FOR LOTS OF EXAMPLES CLICK [**-> HERE <-**](https://github.com/mkotsur/restito/blob/master/guide.md) :-)

For more details you can also check [Restito's javadoc](http://mkotsur.github.com/restito/javadoc/current/).

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

# Maven instructions

```
<dependency>
    <groupId>com.xebialabs.restito</groupId>
    <artifactId>restito</artifactId>
    <version>0.6</version>
</dependency>
```


# Building instructions

```
$ gradle clean build
```
