package integration;

import com.jayway.restassured.RestAssured;
import com.xebialabs.restito.server.StubServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.expect;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.*;
import static com.xebialabs.restito.semantics.Condition.startsWithUri;
import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_16BE;
import static org.hamcrest.Matchers.*;

public class LargeHttpDataTest {

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
    public void testSmallContent() {
        whenHttp(server)
                .match(startsWithUri("/test-small")).
                then(resourceContent("content.json"));

        expect()
                .header("Content-Type", is("application/json"))
                .header("Content-Length", is(not(nullValue())))
                .when().get("/test-small");
    }

    @Test
    public void testLargeContent() {
        whenHttp(server)
                .match(startsWithUri("/test-large")).
                then(resourceContent("large-content.json"));

        expect()
                .header("Content-Type", is("application/json"))
                .header("Content-Length", is(not(nullValue())))
                .when().get("/test-large");
    }
}
