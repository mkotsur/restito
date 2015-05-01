package guide;

import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.jayway.restassured.RestAssured;

import com.xebialabs.restito.server.StubServer;

import static com.jayway.restassured.RestAssured.expect;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Condition.*;

public class SequencedVerificationsTest {


    private StubServer server;

    @Before
    public void start() {
        server = new StubServer().run();
        RestAssured.port = server.getPort();

        whenHttp(server).
                match(startsWithUri("/")).
                then(status(HttpStatus.OK_200));
    }

    @After
    public void stop() {
        server.stop();
    }

    @Test
    public void shouldPassWhenCallsAreInProperOrder() {
        expect().statusCode(200).when().get("/first");
        expect().statusCode(200).when().get("/second");

        verifyHttp(server)
                .once(get("/first")).then()
                .once(get("/second"));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenCallsAreInReversedOrder() {
        expect().statusCode(200).when().get("/second");
        expect().statusCode(200).when().get("/first");

        verifyHttp(server)
                .once(get("/first")).then()
                .once(get("/second"));
    }

    @Test
    public void shouldNotDependOnOrderByDefault() {
        expect().statusCode(200).when().get("/second");
        expect().statusCode(200).when().get("/first");

        verifyHttp(server).once(get("/first"));
        verifyHttp(server).once(get("/second"));
    }

    @Test
    public void shouldAllowIntermediateChecks() {
        expect().statusCode(200).when().get("/first");

        verifyHttp(server).once(get("/first"));

        expect().statusCode(200).when().get("/first");

        verifyHttp(server).times(2, get("/first"));
    }
}
