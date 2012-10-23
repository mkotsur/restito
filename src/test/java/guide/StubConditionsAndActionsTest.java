package guide;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.google.common.base.Predicate;
import com.jayway.restassured.RestAssured;

import com.xebialabs.restito.semantics.Call;
import com.xebialabs.restito.server.StubServer;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Action.success;
import static com.xebialabs.restito.semantics.Condition.custom;
import static com.xebialabs.restito.semantics.Condition.endsWithUri;
import static com.xebialabs.restito.semantics.Condition.get;
import static com.xebialabs.restito.semantics.Condition.method;
import static com.xebialabs.restito.semantics.Condition.parameter;
import static com.xebialabs.restito.semantics.Condition.post;
import static com.xebialabs.restito.semantics.Condition.uri;
import static org.hamcrest.Matchers.equalTo;

public class StubConditionsAndActionsTest {

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
    public void shouldStubServerBehavior() {
        whenHttp(server).
                match(endsWithUri("/demo")).
                then(status(HttpStatus.OK_200));

        given().param("foo", "bar").get("/demo");

        verifyHttp(server).once(
                method(Method.GET),
                uri("/demo"),
                parameter("foo", "bar")
        );
    }



    @Test
    public void shouldReturnProperContentForProperRequests() {
        whenHttp(server).
                match(get("/asd")).
                then(success(), stringContent("GET asd"));

        whenHttp(server).
                match(post("/asd")).
                then(success(), stringContent("POST asd"));

        whenHttp(server).
                match(get("/asd"), parameter("bar", "foo")).
                then(success(), stringContent("GET asd with parameter"));

        expect().statusCode(200).and().body(equalTo("GET asd")).
                when().get("/asd");

        given().param("bar", "foo").
                expect().statusCode(200).and().body(equalTo("GET asd with parameter")).
                when().get("/asd");

        expect().statusCode(200).and().body(equalTo("POST asd")).
                when().post("/asd");
    }

    @Test
    public void shouldAllowStubbingWithCustomCondition() {
        Predicate<Call> uriEndsWithA = new Predicate<Call>() {
            @Override
            public boolean apply(final Call input) {
                return input.getUri().endsWith("a");
            }
        };
        whenHttp(server).match(custom(uriEndsWithA)).then(success());
        expect().statusCode(200).get("/a");
        expect().statusCode(404).get("/b");
    }

    @Test
    public void shouldReturn404forNotDefinedUris() {
        whenHttp(server).match(endsWithUri("/asd")).then(success());

        given().param("foo", "bar").
                expect().statusCode(404).
                when().get("/undefined");
    }
}
