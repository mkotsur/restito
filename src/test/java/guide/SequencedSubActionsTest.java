package guide;

import com.jayway.restassured.RestAssured;
import com.xebialabs.restito.semantics.ActionSequence;
import com.xebialabs.restito.semantics.Applicable;
import com.xebialabs.restito.server.StubServer;
import org.glassfish.grizzly.http.Method;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Action.composite;
import static com.xebialabs.restito.semantics.ActionSequence.sequence;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.delete;
import static com.xebialabs.restito.semantics.Condition.get;
import static com.xebialabs.restito.semantics.Condition.method;
import static com.xebialabs.restito.semantics.Condition.post;
import static com.xebialabs.restito.semantics.Condition.put;
import static com.xebialabs.restito.semantics.Condition.uri;
import static com.xebialabs.restito.semantics.Condition.withPostBody;
import static org.glassfish.grizzly.http.util.HttpStatus.BAD_REQUEST_400;
import static org.glassfish.grizzly.http.util.HttpStatus.CONFLICT_409;
import static org.glassfish.grizzly.http.util.HttpStatus.CREATED_201;
import static org.glassfish.grizzly.http.util.HttpStatus.FORBIDDEN_403;
import static org.glassfish.grizzly.http.util.HttpStatus.NOT_FOUND_404;
import static org.glassfish.grizzly.http.util.HttpStatus.NO_CONTENT_204;
import static org.glassfish.grizzly.http.util.HttpStatus.OK_200;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;

/**
 * Demonstrates use of {@link ActionSequence#sequence(Applicable...)}.
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
                match(get("/foo")).
                then(status(OK_200)) // This action will be applied to each response of the sequence
                .withSequence(
                        stringContent("Hello Restito."),
                        stringContent("Hello, again!")
                );

        // First action from sequence:
        given().get("/foo").then().assertThat().body(equalTo("Hello Restito."));
        // Second action from sequence:
        given().get("/foo").then().assertThat().body(equalTo("Hello, again!"));
        // Noop action:
        given().get("/foo").then().assertThat().body(isEmptyString());

        verifyHttp(server).times(3,
                method(Method.GET),
                uri("/foo"));
    }

    @Test
    public void shouldReturnDifferentResponseTwoEndpoints() {
        whenHttp(server).
                match(get("/foo")).
                then(status(OK_200))
                .withSequence(stringContent("Hello Restito."),
                        stringContent("Hello, again!")
                );

        whenHttp(server).
                match(get("/bar")).
                then(sequence(status(NOT_FOUND_404),
                        composite(status(OK_200), stringContent("body")),
                        composite(status(FORBIDDEN_403), stringContent("admin required"))
                ));

        given().get("/foo").then().assertThat().statusCode(is(OK_200.getStatusCode())).body(equalTo("Hello Restito."));
        given().get("/bar").then().assertThat().statusCode(is(NOT_FOUND_404.getStatusCode())).body(isEmptyString());
        given().get("/foo").then().assertThat().statusCode(is(OK_200.getStatusCode())).body(equalTo("Hello, again!"));
        given().get("/bar").then().assertThat().statusCode(is(OK_200.getStatusCode())).body(equalTo("body"));
        given().get("/bar").then().assertThat().statusCode(is(FORBIDDEN_403.getStatusCode())).body(equalTo("admin required"));

        verifyHttp(server).times(2,
                method(Method.GET),
                uri("/foo"));
        verifyHttp(server).times(3,
                method(Method.GET),
                uri("/bar"));
    }

    @Test
    public void shouldReturnDifferentBodyAndStatusForSameRequestByTwoSequences() throws Exception {
        ActionSequence responseCodes = sequence(
                status(CREATED_201),
                status(CONFLICT_409),
                status(CREATED_201)
        );

        ActionSequence responseContents = sequence(
                stringContent("status=CREATED"),
                stringContent("status=CONFLICT"),
                stringContent("status=CREATED2")
        );

        whenHttp(server)
                .match(post("/foo"))
                .then(responseCodes, responseContents);

        given().post("/foo").then().assertThat().statusCode(is(201)).body(equalTo("status=CREATED"));
        given().post("/foo").then().assertThat().statusCode(is(409)).body(equalTo("status=CONFLICT"));
        given().post("/foo").then().assertThat().statusCode(is(201)).body(equalTo("status=CREATED2"));
        given().post("/foo").then().assertThat().statusCode(is(404)).body(isEmptyString());
        given().post("/foo").then().assertThat().body(isEmptyString());

        verifyHttp(server).times(5,
                method(Method.POST),
                uri("/foo"));
    }

    @Test
    public void shouldReturnDifferentBodyAndStatusForSameRequestBySingleSequence() {
        whenHttp(server).
                match(post("/foo")).
                then(sequence(composite(status(CREATED_201), stringContent("status=CREATED")),
                        composite(status(CONFLICT_409), stringContent("status=CONFLICT")),
                        composite(status(CREATED_201), stringContent("status=CREATED2")),
                        status(NOT_FOUND_404))
                );

        given().post("/foo").then().assertThat().statusCode(is(201)).body(equalTo("status=CREATED"));
        given().post("/foo").then().assertThat().statusCode(is(409)).body(equalTo("status=CONFLICT"));
        given().post("/foo").then().assertThat().statusCode(is(201)).body(equalTo("status=CREATED2"));
        given().post("/foo").then().assertThat().statusCode(is(404)).body(isEmptyString());
        given().post("/foo").then().assertThat().body(isEmptyString());

        verifyHttp(server).times(5,
                method(Method.POST),
                uri("/foo"));
    }

    // two sequences in multiple whenHttp, different endpoints

    @Test
    public void shouldReturnDifferentResponseComplex() {
        whenHttp(server).
                match(get("/foo")).
                then(sequence(
                        status(NOT_FOUND_404),
                        composite(status(OK_200), stringContent("INITIAL VALUE")),
                        composite(status(OK_200), stringContent("UPDATED VALUE")),
                        status(NOT_FOUND_404)
                ));
        whenHttp(server).
                match(post("/foo"), withPostBody()).
                then(sequence(
                        status(CREATED_201),
                        status(CONFLICT_409))
                );
        whenHttp(server).
                match(put("/foo"), withPostBody()).
                then(sequence(
                        status(BAD_REQUEST_400),
                        status(NO_CONTENT_204))
                );
        whenHttp(server).
                match(delete("/foo")).
                then(status(NO_CONTENT_204));

        given().get("/foo")
                .then().assertThat().statusCode(is(NOT_FOUND_404.getStatusCode())).body(isEmptyString());
        given().body("INITIAL VALUE").post("/foo")
                .then().assertThat().statusCode(is(CREATED_201.getStatusCode())).body(isEmptyString());
        given().body("UPDATED VALUE").put("/foo")
                .then().assertThat().statusCode(is(BAD_REQUEST_400.getStatusCode())).body(isEmptyString());
        given().get("/foo")
                .then().assertThat().statusCode(is(OK_200.getStatusCode())).body(equalTo("INITIAL VALUE"));
        given().body("INITIAL VALUE, AGAIN").post("/foo")
                .then().assertThat().statusCode(is(CONFLICT_409.getStatusCode())).body(isEmptyString());
        given().body("UPDATED VALUE").put("/foo")
                .then().assertThat().statusCode(is(NO_CONTENT_204.getStatusCode())).body(isEmptyString());
        given().get("/foo")
                .then().assertThat().statusCode(is(OK_200.getStatusCode())).body(equalTo("UPDATED VALUE"));
        given().delete("/foo")
                .then().assertThat().statusCode(is(NO_CONTENT_204.getStatusCode())).body(isEmptyString());
        given().get("/foo")
                .then().assertThat().statusCode(is(NOT_FOUND_404.getStatusCode())).body(isEmptyString());
    }

}
