package guide;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.restassured.RestAssured;

import com.xebialabs.restito.server.StubServer;

import static io.restassured.RestAssured.expect;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Condition.*;

public class LimitingNumberOfCallsTest {

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

    @Test(expected = AssertionError.class)
    public void shouldFailWhenNotExpectedButCalled() {
        whenHttp(server).
                match(endsWithUri("/demo")).
                then(status(HttpStatus.OK_200));

        expect().statusCode(200).when().get("/demo");

        verifyHttp(server).never(
                method(Method.GET),
                uri("/demo")
        );
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenCalledWrongAmountOfTimes() {
        whenHttp(server).
                match(endsWithUri("/demo")).
                then(status(HttpStatus.OK_200));

        expect().statusCode(200).when().get("/demo");

        verifyHttp(server).times(2,
                method(Method.GET),
                uri("/demo")
        );
    }
}
