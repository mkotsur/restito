package guide;

import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.restassured.RestAssured;

import com.xebialabs.restito.server.StubServer;

import static com.xebialabs.restito.builder.ensure.EnsureHttp.ensureHttp;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.ok;
import static com.xebialabs.restito.semantics.Condition.get;
import static org.apache.http.client.config.RequestConfig.custom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class UsingHttpsTest {
    private StubServer server;

    @Before
    public void start() {
        server = new StubServer().secured();
        RestAssured.port = server.getPort();
    }

    @After
    public void stop() {
        server.stop();
    }

    @Test
    public void shouldPassWhenExpectedStubDidHappen() throws GeneralSecurityException, IOException {
        server.run();
        whenHttp(server).match(get("/asd")).then(ok()).mustHappen();

        HttpResponse execute = sslReadyHttpClient().execute(new HttpGet("https://localhost:" + server.getPort() + "/asd"));

        assertThat(execute.getStatusLine().getStatusCode(), equalTo(200));
        ensureHttp(server).gotStubsCommitmentsDone();
    }

    @Test
    public void shouldBePossibleToSpecifyKeyStoresWithStandardProperties() throws GeneralSecurityException, IOException {

        System.setProperty("javax.net.ssl.trustStore","build/resources/main/keystore_server");

        System.setProperty("javax.net.ssl.keyStore","build/resources/main/truststore_server");

        System.setProperty("javax.net.ssl.trustStorePassword","secret");

        System.setProperty("javax.net.ssl.keyStorePassword","secret");

        server.run();
        whenHttp(server).match(get("/asd")).then(ok()).mustHappen();

        HttpResponse execute = sslReadyHttpClient().execute(new HttpGet("https://localhost:" + server.getPort() + "/asd"));

        assertThat(execute.getStatusLine().getStatusCode(), equalTo(200));
        ensureHttp(server).gotStubsCommitmentsDone();
    }

    /**
     * Helper which returns HTTP client configured for https session
     */
    private HttpClient sslReadyHttpClient() throws GeneralSecurityException {
        final SSLContext context = new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();

        final SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(context);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", socketFactory)
                .build();

        return HttpClientBuilder.create()
                .setDefaultRequestConfig(custom().setConnectionRequestTimeout(10000).build())
                .setConnectionManager(new PoolingHttpClientConnectionManager(registry))
                .setSSLSocketFactory(socketFactory)
                .build();

    }

}
