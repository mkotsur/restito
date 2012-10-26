package guide;

import java.io.IOException;
import java.security.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.CoreConnectionPNames;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.jayway.restassured.RestAssured;

import com.xebialabs.restito.server.StubServer;

import static com.jayway.restassured.RestAssured.given;
import static com.xebialabs.restito.builder.ensure.EnsureHttp.ensureHttp;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.success;
import static com.xebialabs.restito.semantics.Condition.get;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class UsingHttpsTest {
    private StubServer server;

    @Before
    public void start() {
        server = new StubServer().secured().run();
        RestAssured.port = server.getPort();
    }

    @After
    public void stop() {
        server.stop();
    }

    @Test
    public void shouldPassWhenExpectedStubDidHappen() throws GeneralSecurityException, IOException {
        whenHttp(server).match(get("/asd")).then(success()).mustHappen();

        HttpResponse execute = sslReadyHttpClient().execute(new HttpGet("https://localhost:" + server.getPort() + "/asd"));

        assertThat(execute.getStatusLine().getStatusCode(), equalTo(200));
        ensureHttp(server).gotStubsCommitmentsDone();
    }

    /**
     * Helper which returns HTTP client configured for https session
     */
    private HttpClient sslReadyHttpClient() throws GeneralSecurityException {
        DefaultHttpClient httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager());
        SSLSocketFactory sslSocketFactory = new SSLSocketFactory(new TrustSelfSignedStrategy(), null);
        Scheme scheme = new Scheme("https", server.getPort(), sslSocketFactory);
        httpClient.getConnectionManager().getSchemeRegistry().register(scheme);
        httpClient.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
        return httpClient;
    }

}
