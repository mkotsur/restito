package integration;

import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.jayway.restassured.RestAssured;

import com.xebialabs.restito.server.StubServer;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Condition.startsWithUri;

public class VeryFastRequestsTest {

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
    public void shouldSendRawDataFromFileWithoutCaringAboutEncoding() {
        whenHttp(server)
                .match(startsWithUri("/test")).
                then(status(HttpStatus.OK_200));


    }

}
