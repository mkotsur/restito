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
import static com.xebialabs.restito.semantics.Action.success;
import static com.xebialabs.restito.semantics.Condition.get;
import static com.xebialabs.restito.semantics.Condition.uri;

public class ExpectedStubTest {

    StubServer server;

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
        whenHttp(server).match(get("/asd")).then(success()).mustHappen();
        expect().statusCode(200).get("/asd");
        ensureHttp(server).gotStubsCommitmentsDone();
    }

    @Test
    public void shouldPassWhenStubTriggeredExactNumberOfTimes() {
        whenHttp(server).
                match(uri("/demo")).
                then(success()).
                mustHappen(2);

        given().when().get("/demo");
        given().when().get("/demo");
        given().when().get("/miss");

        ensureHttp(server).gotStubsCommitmentsDone();
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenSecondExpectedStubDidNotHappen() {
        whenHttp(server).match(get("/asd")).then(success()).mustHappen();
        whenHttp(server).match(get("/neverHappens")).then(success()).mustHappen();
        expect().statusCode(200).get("/asd");
        ensureHttp(server).gotStubsCommitmentsDone();
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenStubNotTriggeredMoreThenExpected() {
        whenHttp(server).
                match(uri("/demo")).
                then(success()).
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
}
