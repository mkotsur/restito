package guide;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.jayway.restassured.RestAssured;

import com.xebialabs.restito.server.StubServer;

import static com.jayway.restassured.RestAssured.expect;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.ok;
import static com.xebialabs.restito.semantics.Condition.get;
import static org.hamcrest.Matchers.containsString;

public class AutodiscoveryOfStubsContentTest {

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
    public void shouldFindXmlResourceFileByUrl() {
        whenHttp(server).
                match(get("/demo/path%20to%20data/data")).then(ok());

        expect().content(containsString("from data.xml")).when().get("/demo/path to data/data");
    }

}
