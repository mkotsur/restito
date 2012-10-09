package com.xebialabs.restito;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Before;
import com.jayway.restassured.RestAssured;

import com.xebialabs.restito.server.StubServer;
import org.junit.*;

import static com.jayway.restassured.RestAssured.given;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Condition.endsWithUri;
import static com.xebialabs.restito.semantics.Condition.method;
import static com.xebialabs.restito.semantics.Condition.parameter;
import static com.xebialabs.restito.semantics.Condition.uri;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class UserGuideTest {

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
    public void shouldStartAndStopStubServer() {
        assertTrue(server.getPort() > 0);
    }

    @Test
    public void shouldBePossibleToSpecifyPort() {
        StubServer server1 = new StubServer(8888).run();
        assertEquals(8888, server1.getPort());
        server1.stop();
    }

    @Test
    public void shouldSelectRandomFreePortWhenDefaultOneIsBusy() {
        StubServer server2 = new StubServer().run();
        assertTrue(server2.getPort() > server.getPort());
    }


    @Test
    public void shouldStubServerBehavior() {
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
}
