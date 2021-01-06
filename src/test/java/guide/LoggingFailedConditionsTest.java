package guide;

import com.xebialabs.restito.server.StubServer;
import io.restassured.RestAssured;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.ok;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.*;
import static io.restassured.RestAssured.expect;
import static org.hamcrest.Matchers.containsString;

public class LoggingFailedConditionsTest {

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
    @Ignore
    public void shouldFindXmlResourceFileByUrl() {
        whenHttp(server).match(
                get("/test"),
                withHeader("foo", "bar"),
                parameter("foo", "42")
        ).then(ok(), stringContent("Hello"));
        expect().body(containsString("Hello")).when().get("/test");
    }

}
