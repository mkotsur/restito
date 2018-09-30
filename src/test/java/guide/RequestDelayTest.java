package guide;

import io.restassured.RestAssured;
import com.xebialabs.restito.server.StubServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.expect;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.delay;
import static com.xebialabs.restito.semantics.Action.ok;
import static com.xebialabs.restito.semantics.Condition.get;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

public class RequestDelayTest {

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
    public void shouldIntroduceADelay() {
        whenHttp(server).match(get("/asd")).then(ok(), delay(2000)).mustHappen();
        expect().statusCode(200).when().get("/asd").then().time(greaterThanOrEqualTo(2000L));
    }

}
