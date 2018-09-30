package integration;

import io.restassured.RestAssured;
import com.xebialabs.restito.server.StubServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.expect;
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
                .match(startsWithUri("/test-large-json")).
                then(resourceContent("large-content.json"));

        expect()
                .header("Content-Type", is("application/json"))
                .header("Content-Length", is(not(nullValue())))
                .when().get("/test-large-json");
    }

    @Test
    // See https://github.com/mkotsur/restito/issues/57
    public void testLargeXmlContent() {
        whenHttp(server)
                .match(startsWithUri("/test-large-xml")).
                then(resourceContent("large-content.xml"));

        expect()
                .header("Content-Type", is("application/xml"))
                .header("Content-Length", is(not(nullValue())))
                .when().get("/test-large-xml");
    }
}
