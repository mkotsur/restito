package guide;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.jayway.restassured.RestAssured;

import com.xebialabs.restito.semantics.Call;
import com.xebialabs.restito.server.StubServer;

import java.util.function.Predicate;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Action.ok;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Action.unauthorized;
import static com.xebialabs.restito.semantics.Condition.alwaysTrue;
import static com.xebialabs.restito.semantics.Condition.basicAuth;
import static com.xebialabs.restito.semantics.Condition.custom;
import static com.xebialabs.restito.semantics.Condition.endsWithUri;
import static com.xebialabs.restito.semantics.Condition.get;
import static com.xebialabs.restito.semantics.Condition.method;
import static com.xebialabs.restito.semantics.Condition.not;
import static com.xebialabs.restito.semantics.Condition.parameter;
import static com.xebialabs.restito.semantics.Condition.post;
import static com.xebialabs.restito.semantics.Condition.uri;
import static com.xebialabs.restito.semantics.Condition.url;
import static org.hamcrest.Matchers.equalTo;

public class StubConditionsAndActionsTest {

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
                then(ok(), stringContent("GET asd"));

        whenHttp(server).
                match(post("/asd")).
                then(ok(), stringContent("POST asd"));

        whenHttp(server).
                match(get("/asd"), parameter("bar", "foo")).
                then(ok(), stringContent("GET asd with parameter"));

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
        Predicate<Call> uriEndsWithA = input -> input.getUri().endsWith("a");
        whenHttp(server).match(custom(uriEndsWithA)).then(ok());
        expect().statusCode(200).get("/a");
        expect().statusCode(404).get("/b");
    }

    @Test
    public void shouldReturn404forNotDefinedUris() {
        whenHttp(server).match(endsWithUri("/asd")).then(ok());

        given().param("foo", "bar").
                expect().statusCode(404).
                when().get("/undefined");
    }

    @Test
    public void shouldMatchTheWholeUrl() throws Exception {
        String fullUrl = "http://localhost:" + server.getPort() + "/asd";
        whenHttp(server).match(url(fullUrl)).then(ok());

        expect().statusCode(200).when().get(fullUrl);
    }

    @Test
    public void shouldResolveStubsInReverseOrder() {
        whenHttp(server).match(alwaysTrue()).then(status(HttpStatus.OK_200));
        whenHttp(server).match(get("/bad")).then(status(HttpStatus.BAD_REQUEST_400));
        expect().statusCode(400).get("/bad");
        expect().statusCode(200).get("/any/other/url");
    }

    @Test
    public void shouldNotAllowWithoutHttpAuth() throws Exception {
        whenHttp(server).match(basicAuth("admin", "secret")).then(status(HttpStatus.OK_200));
        whenHttp(server).match(not(basicAuth("admin", "secret"))).then(unauthorized());

        expect().statusCode(401).header("WWW-Authenticate", "Basic realm=\"Restito realm\"").get("/");
        given().auth().basic("admin", "secret").expect().statusCode(200).when().get("/");
    }

    @Test
    public void shouldAnswerWithCustomRealmName() {
        whenHttp(server).match(not(basicAuth("admin", "secret"))).then(unauthorized("Custom realm"));
        expect().statusCode(401).header("WWW-Authenticate", "Basic realm=\"Custom realm\"").get("/");
    }

}
