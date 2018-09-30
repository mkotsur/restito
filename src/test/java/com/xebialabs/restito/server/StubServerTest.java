package com.xebialabs.restito.server;

import org.apache.mina.util.AvailablePortFinder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.restassured.RestAssured;

import com.xebialabs.restito.semantics.Call;
import com.xebialabs.restito.semantics.Stub;

import static io.restassured.RestAssured.expect;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.ok;
import static com.xebialabs.restito.semantics.Condition.get;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class StubServerTest {

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
    public void shouldStartServerInRangeStartingFromDefaultPort() {
        assertTrue("Server port not in range", server.getPort() >= StubServer.DEFAULT_PORT && server.getPort() <= AvailablePortFinder.MAX_PORT_NUMBER);
        server.stop();
    }

    @Test
    public void shouldStartServerOnRandomPortWhenDefaultPortIsBusy() {
        StubServer server1 = new StubServer().run();
        StubServer server2 = new StubServer().run();
        assertTrue(server2.getPort() != server1.getPort());
    }

    @Test
    public void shouldBePossibleToSpecifyPort() {
        StubServer server = new StubServer(8888).run();
        assertEquals(8888, server.getPort());
        server.stop();
    }

    @Test
    public void shouldBePossibleToStubRootUri() {
        whenHttp(server).match(get("/")).then(ok()).mustHappen();
        expect().statusCode(200).when().get("/");
    }

    @Test
    public void shouldBePossibleToIterateThroughCallsWhileNewItemsAreAdded() {
        whenHttp(server).match(get("/")).then(ok()).mustHappen();

        expect().statusCode(200).when().get("/");
        expect().statusCode(200).when().get("/");

        assertEquals(server.getCalls().size(), 2);
        for (Call ignored : server.getCalls()) {
            expect().statusCode(200).when().get("/");
            expect().statusCode(200).when().get("/");
        }
    }

    @Test
    public void shouldBePossibleToIterateThroughStubsAndModifyIt() {
        whenHttp(server).match(get("/")).then(ok());

        expect().statusCode(200).when().get("/");

        for (Stub ignored : server.getStubs()) {
            whenHttp(server).match(get("/newstub")).then(ok());
        }
    }

    @Test
    public void shouldClearRegisteredStubs() {
        whenHttp(server).match(get("/")).then(ok());
        assertEquals(1, server.getStubs().size());

        server.clearStubs();

        assertEquals(0, server.getStubs().size());
    }

    @Test
    public void shouldClearStubCalls() {
        whenHttp(server).match(get("/")).then(ok());
        expect().statusCode(200).when().get("/");

        assertEquals(1, server.getCalls().size());

        server.clearCalls();

        assertEquals(0, server.getCalls().size());
    }

    @Test
    public void shouldClearStubAndCalls() {
        whenHttp(server).match(get("/")).then(ok());
        expect().statusCode(200).when().get("/");

        assertEquals(1, server.getStubs().size());
        assertEquals(1, server.getCalls().size());

        server.clear();

        assertEquals(0, server.getStubs().size());
        assertEquals(0, server.getCalls().size());
    }

    @Test
    public void shouldNotRegisterCallsWhenFlagDisabled() {
        server.setRegisterCalls(false);
        whenHttp(server).match(get("/")).then(ok());
        expect().statusCode(200).when().get("/");

        assertEquals(0, server.getCalls().size());
    }

    @Test
    public void shouldReturn404WhenTheRequestIsNotCoveredByStubs() {
        expect().statusCode(404).when().get("/");
    }
}
