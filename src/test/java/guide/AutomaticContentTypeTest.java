package guide;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import com.xebialabs.restito.server.StubServer;

import static com.jayway.restassured.RestAssured.expect;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.resourceContent;
import static com.xebialabs.restito.semantics.Condition.endsWithUri;

public class AutomaticContentTypeTest {

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
    public void shouldSetContentTypeJsonAccordingToResourceExtension() {
        whenHttp(server).
                match(endsWithUri("/demo")).
                then(resourceContent("content.json"));

        expect().contentType(ContentType.JSON).when().get("/demo");
    }

    @Test
    public void shouldSetContentTypeXmlAccordingToResourceExtension() {
        whenHttp(server).
                match(endsWithUri("/demo")).
                then(resourceContent("content.xml"));

        expect().contentType(ContentType.XML).when().get("/demo");
    }
}
