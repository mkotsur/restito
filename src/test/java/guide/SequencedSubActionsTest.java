package guide;

import com.jayway.restassured.RestAssured;
import com.xebialabs.restito.semantics.Action;
import com.xebialabs.restito.server.StubServer;
import org.glassfish.grizzly.http.Method;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Action.sequence;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.get;
import static com.xebialabs.restito.semantics.Condition.method;
import static com.xebialabs.restito.semantics.Condition.put;
import static com.xebialabs.restito.semantics.Condition.uri;
import static com.xebialabs.restito.semantics.Condition.withPostBody;
import static org.glassfish.grizzly.http.util.HttpStatus.NO_CONTENT_204;
import static org.glassfish.grizzly.http.util.HttpStatus.OK_200;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;

/**
 * Demonstrates use of {@link com.xebialabs.restito.semantics.Action#sequence(Action...)}.
 */
public class SequencedSubActionsTest {

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
    public void shouldReturnDifferentBodyForSameRequest() {
        whenHttp(server).
                match(get("/demo")).
                then(status(OK_200),
                     sequence(stringContent("Hello Restito."),
                              stringContent("Hello, again!"))
                );

        // First action from sequence:
        given().get("/demo").then().assertThat().body(equalTo("Hello Restito."));
        // Second action from sequence:
        given().get("/demo").then().assertThat().body(equalTo("Hello, again!"));
        // Noop action:
        given().get("/demo").then().assertThat().body(isEmptyString());

        verifyHttp(server).times(3,
                                 method(Method.GET),
                                 uri("/demo"));
    }

    @Test
    public void shouldReturnDifferentBodyForSameRequestExpectedUsage() {
        whenHttp(server).
                match(get("/demo")).
                then(status(OK_200),
                     sequence(stringContent("INITIAL VALUE"),
                              stringContent("UPDATED VALUE"))
                );
        whenHttp(server).
                match(put("/demo"), withPostBody()).
                then(status(NO_CONTENT_204));

        // business login to be tested
        // 1. get current data
        given().get("/demo")
                .then().assertThat().body(equalTo("INITIAL VALUE"));
        // 2. update data
        given().body("UPDATED VALUE").put("/demo")
                .then().assertThat().statusCode(is(NO_CONTENT_204.getStatusCode()));
        // 3. use new data
        given().get("/demo")
                .then().assertThat().body(equalTo("UPDATED VALUE"));
    }

}
