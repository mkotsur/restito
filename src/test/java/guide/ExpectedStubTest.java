package guide;

import com.xebialabs.restito.server.StubServer;
import org.junit.*;
import io.restassured.RestAssured;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static io.restassured.RestAssured.*;

import static com.xebialabs.restito.builder.ensure.EnsureHttp.ensureHttp;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.*;
import static com.xebialabs.restito.semantics.ActionSequence.sequence;
import static com.xebialabs.restito.semantics.Condition.get;
import static com.xebialabs.restito.semantics.Condition.uri;
import static org.glassfish.grizzly.http.util.HttpStatus.OK_200;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
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
        expect().statusCode(200).when().get("/asd");
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

    @Test
    public void shouldFailWhenSecondExpectedStubDidNotHappen() {
        var error = assertThrows(AssertionError.class, () -> {
            whenHttp(server).match(get("/asd")).then(ok()).mustHappen();
            whenHttp(server).match(get("/neverHappens")).then(ok()).mustHappen();
            expect().statusCode(200).when().get("/asd");
            ensureHttp(server).gotStubsCommitmentsDone();
        });

        assertThat(error.getMessage(), startsWith("Expected stub Stub@"));
        assertThat(error.getMessage(), endsWith(" to be called 1 times, called 0 times instead"));
    }

    @Test
    public void shouldFailWithTheErrorMessageThatContainsStubLabel() {
        var error = assertThrows(AssertionError.class, () -> {

            whenHttp(server).match(get("/asd")).then(ok()).mustHappen();
            whenHttp(server).match("Fancy stub", get("/neverHappens")).then(ok()).mustHappen();
            expect().statusCode(200).when().get("/asd");
            ensureHttp(server).gotStubsCommitmentsDone();
        });

        assertThat(error.getMessage(), startsWith("Expected stub Stub@"));
        assertThat(error.getMessage(), endsWith(" [Fancy stub] to be called 1 times, called 0 times instead"));
    }

    @Test
    public void shouldFailWhenStubNotTriggeredMoreThenExpected() {
        var error = assertThrows(AssertionError.class, () -> {
            whenHttp(server).
                    match("Demo URL", uri("/demo")).
                    then(ok()).
                    mustHappen(2);

            given().when().get("/demo");
            given().when().get("/demo");
            given().when().get("/demo");

            ensureHttp(server).gotStubsCommitmentsDone();
        });

        assertThat(error.getMessage(), startsWith("Expected stub Stub@"));
        assertThat(error.getMessage(), endsWith(" [Demo URL] to be called 2 times, called 3 times instead"));
    }

    @Test
    public void shouldPassWhenNoStubCommitments() {
        ensureHttp(server).gotStubsCommitmentsDone();
    }

    @Test
    public void shouldFailWhenTheSequenceHasNotBeenCompleted() {
        var error = assertThrows(AssertionError.class, () -> {
            whenHttp(server).
                    match("Some stub", get("/should-be-completed")).
                    then(sequence(
                            composite(status(OK_200), stringContent("1")),
                            composite(status(OK_200), stringContent("2"))
                    )).mustComplete();

            given().get("/should-be-completed");

            ensureHttp(server).gotStubsCommitmentsDone();
        });

        assertThat(error.getMessage(), startsWith("Expected stub Stub@"));
        assertThat(error.getMessage(), endsWith(" [Some stub] to cover all 2 sequence steps, called 1 times instead"));
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
