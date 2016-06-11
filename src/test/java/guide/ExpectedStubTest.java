package guide;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.jayway.restassured.RestAssured;

import com.xebialabs.restito.server.StubServer;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.xebialabs.restito.builder.ensure.EnsureHttp.ensureHttp;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.*;
import static com.xebialabs.restito.semantics.ActionSequence.sequence;
import static com.xebialabs.restito.semantics.Condition.get;
import static com.xebialabs.restito.semantics.Condition.uri;
import static org.glassfish.grizzly.http.util.HttpStatus.OK_200;

public class ExpectedStubTest {

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
    public void shouldPassWhenExpectedStubDidHappen() {
        whenHttp(server).match(get("/asd")).then(ok()).mustHappen();
        expect().statusCode(200).get("/asd");
        ensureHttp(server).gotStubsCommitmentsDone();
    }

    @Test
    public void shouldPassWhenStubTriggeredExactNumberOfTimes() {
        whenHttp(server).
                match(uri("/demo")).
                then(ok()).
                mustHappen(2);

        given().when().get("/demo");
        given().when().get("/demo");
        given().when().get("/miss");

        ensureHttp(server).gotStubsCommitmentsDone();
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenSecondExpectedStubDidNotHappen() {
        whenHttp(server).match(get("/asd")).then(ok()).mustHappen();
        whenHttp(server).match(get("/neverHappens")).then(ok()).mustHappen();
        expect().statusCode(200).get("/asd");
        ensureHttp(server).gotStubsCommitmentsDone();
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenStubNotTriggeredMoreThenExpected() {
        whenHttp(server).
                match(uri("/demo")).
                then(ok()).
                mustHappen(2);

        given().when().get("/demo");
        given().when().get("/demo");
        given().when().get("/demo");

        ensureHttp(server).gotStubsCommitmentsDone();
    }

    @Test
    public void shouldPassWhenNoStubCommitments() {
        ensureHttp(server).gotStubsCommitmentsDone();
    }



    @Test(expected = AssertionError.class)
    public void shouldFailWhenTheSequenceHasNotBeenCompleted() {
        whenHttp(server).
                match(get("/should-be-completed")).
                then(sequence(
                        composite(status(OK_200), stringContent("1")),
                        composite(status(OK_200), stringContent("2"))
                )).mustComplete();

        given().get("/should-be-completed");

        ensureHttp(server).gotStubsCommitmentsDone();
    }

    @Test
    public void shouldPassWhenTheSequenceHasBeenCompleted() {
        whenHttp(server).
                match(get("/should-be-completed")).
                then(sequence(
                        composite(status(OK_200), stringContent("1")),
                        composite(status(OK_200), stringContent("2"))
                )).mustComplete();

        given().get("/should-be-completed");
        given().get("/should-be-completed");

        ensureHttp(server).gotStubsCommitmentsDone();
    }
}
