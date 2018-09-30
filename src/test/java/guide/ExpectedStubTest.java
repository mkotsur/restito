package guide;

import com.xebialabs.restito.server.StubServer;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.*;

import static com.xebialabs.restito.builder.ensure.EnsureHttp.ensureHttp;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.*;
import static com.xebialabs.restito.semantics.ActionSequence.sequence;
import static com.xebialabs.restito.semantics.Condition.get;
import static com.xebialabs.restito.semantics.Condition.uri;
import static org.glassfish.grizzly.http.util.HttpStatus.OK_200;

public class ExpectedStubTest {

    private StubServer server;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();


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
        expectedEx.expect(AssertionError.class);
        expectedEx.expectMessage(errorMessageMatcher("Expected stub Stub@", " to be called 1 times, called 0 times instead"));

        whenHttp(server).match(get("/asd")).then(ok()).mustHappen();
        whenHttp(server).match(get("/neverHappens")).then(ok()).mustHappen();
        expect().statusCode(200).when().get("/asd");
        ensureHttp(server).gotStubsCommitmentsDone();
    }

    @Test
    public void shouldFailWithTheErrorMessageThatContainsStubLabel() {
        expectedEx.expect(AssertionError.class);
        expectedEx.expectMessage(errorMessageMatcher("Expected stub Stub@", " [Fancy stub] to be called 1 times, called 0 times instead"));

        whenHttp(server).match(get("/asd")).then(ok()).mustHappen();
        whenHttp(server).match("Fancy stub", get("/neverHappens")).then(ok()).mustHappen();
        expect().statusCode(200).when().get("/asd");
        ensureHttp(server).gotStubsCommitmentsDone();
    }

    @Test
    public void shouldFailWhenStubNotTriggeredMoreThenExpected() {
        expectedEx.expect(AssertionError.class);
        expectedEx.expectMessage(errorMessageMatcher("Expected stub Stub@", " [Demo URL] to be called 2 times, called 3 times instead"));

        whenHttp(server).
                match("Demo URL", uri("/demo")).
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



    @Test
    public void shouldFailWhenTheSequenceHasNotBeenCompleted() {
        expectedEx.expect(AssertionError.class);
        expectedEx.expectMessage(errorMessageMatcher("Expected stub Stub@", " [Some stub] to cover all 2 sequence steps, called 1 times instead"));

        whenHttp(server).
                match("Some stub", get("/should-be-completed")).
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

    private CustomTypeSafeMatcher<String> errorMessageMatcher(String prefix, String suffix) {
        return new CustomTypeSafeMatcher<>("Error message didn't match") {
            @Override
            protected boolean matchesSafely(String item) {
                return item.startsWith(prefix) && item.endsWith(suffix);
            }
        };
    }
}
