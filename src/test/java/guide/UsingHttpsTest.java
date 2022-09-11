package guide;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xebialabs.restito.server.StubServer;

import static com.xebialabs.restito.builder.ensure.EnsureHttp.ensureHttp;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.ok;
import static com.xebialabs.restito.semantics.Condition.get;
import static org.apache.http.client.config.RequestConfig.custom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.fail;

public class UsingHttpsTest {
    private StubServer server;

    @Before
    public void start() {
        server = new StubServer().secured();

        // reset to ensure start clean
        clearJavaSecureSocketExtensionProperties();
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
        // use a different key store
        System.setProperty("javax.net.ssl.keyStore", "build/resources/test/keystore_server_test");
        System.setProperty("javax.net.ssl.keyStorePassword", "secret");

        server.run();
        whenHttp(server).match(get("/asd")).then(ok()).mustHappen();

        KeyStore trustStore = createKeyStore("keystore_server_test_cert");
        HttpResponse execute = sslReadyHttpClient(trustStore).execute(new HttpGet("https://localhost:" + server.getPort() + "/asd"));

        assertThat(execute.getStatusLine().getStatusCode(), equalTo(200));
        ensureHttp(server).gotStubsCommitmentsDone();
    }

    @Test
    public void shouldFailWhenClientDoesNotConfigureTrust() {
        server.run();
        whenHttp(server).match(get("/asd")).then(ok()).mustHappen(0);

        try {
            sslReadyHttpClient(null).execute(new HttpGet("https://localhost:" + server.getPort() + "/asd"));
            fail("Request should have failed as client does not trust server's TLS certificate");
        } catch (Exception e) {
            assertThat(e, isA(SSLHandshakeException.class));
        }

        ensureHttp(server).gotStubsCommitmentsDone();
    }

    /**
     * Helper which returns HTTP client configured for https session
     */
    private HttpClient sslReadyHttpClient() throws GeneralSecurityException {
        return sslReadyHttpClient(StubServer.getTrustStore());
    }

    /**
     * Helper which returns HTTP client configured for https session
     *
     * @param trustStore trust store containing server's certificate
     */
    private HttpClient sslReadyHttpClient(KeyStore trustStore) throws GeneralSecurityException {
        SSLContextBuilder sslContextBuilder = SSLContexts.custom();

        // verifying server's identity
        if (trustStore != null) {
            sslContextBuilder.loadTrustMaterial(trustStore, null);
        }

        final SSLContext context = sslContextBuilder.build();

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

    /**
     * Clear Java SSL/TLS related properties to ensure clean state.
     */
    private void clearJavaSecureSocketExtensionProperties() {
        System.clearProperty("javax.net.ssl.keyStore");
        System.clearProperty("javax.net.ssl.keyStorePassword");
        System.clearProperty("javax.net.ssl.trustStore");
        System.clearProperty("javax.net.ssl.trustStorePassword");
    }

    /**
     * Create key store from resource file.
     */
    private KeyStore createKeyStore(@SuppressWarnings("SameParameterValue") String resource) {
        try (InputStream trustStore = getClass().getResourceAsStream("/" + resource)) {
            KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
            store.load(trustStore, "changeit".toCharArray());
            return store;
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }
}
