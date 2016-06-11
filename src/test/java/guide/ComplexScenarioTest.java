package guide;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

import com.xebialabs.restito.semantics.Applicable;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.semantics.ConditionWithApplicables;
import com.xebialabs.restito.server.StubServer;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.parameter;
import static org.junit.Assert.assertEquals;

public class ComplexScenarioTest {

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
    public void shouldWork() {

        whenHttp(server).match(
                Condition.get("/serviceB"),
                Condition.withHeader("Content-Type", "application/json; charset=ISO-8859-1")
        ).then(stringContent("B1"));


        whenHttp(server).match(
                Condition.post("/serviceC"),
                Condition.withHeader("Content-Type", "application/json; charset=ISO-8859-1"),
                Condition.withPostBodyContaining("{}")
        ).then(stringContent("C1"));

        final ServiceA a = new ServiceA("http://localhost:" + server.getPort());
        assertEquals("B1C1", a.concatenate());
    }


    class ServiceA {

        String entryPoint;

        public ServiceA(String entryPoint) {
            this.entryPoint = entryPoint;
        }

        public String concatenate() {
            final Response b = given()
                    .contentType("application/json; charset=ISO-8859-1")
                    .get("/serviceB");
            final Response c = given().
                    body("{}").
                    contentType("application/json; charset=ISO-8859-1").
                    when().
                    post("/serviceC");

            return b.getBody().asString() + c.getBody().asString();
        }

    }
}
