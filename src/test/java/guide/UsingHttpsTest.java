package guide;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.fail;

public class UsingHttpsTest {
    private StubServer server;

    /**
     * Keystore that contains a trusted certificate that matches
     * Stub server's default private key.
     */
    private final String defaultCertKeystore = "keystore_server_cert";
    private final String defaultCertKeystorePass = "changeit";

    /**
     * Keystore that contains an alternative private key used in this test only.
     */
    private final String testPrivateKeystore = "keystore_server_test";
    private final String testPrivateKeystorePass = "secret";
    private final URL testPrivateKeystoreURL = getClass().getResource("/" + testPrivateKeystore);

    /**
     * Keystore that contains a trusted certificate that matches
     * an alternative private key used only in this test.
     */
    private final String testCertKeystore = "keystore_server_test_cert";
    private final String testCertKeystorePass = "changeit";

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

        HttpResponse execute = sslReadyHttpClient(loadKeystore(defaultCertKeystore, defaultCertKeystorePass), null, null).execute(new HttpGet("https://localhost:" + server.getPort() + "/asd"));

        assertThat(execute.getStatusLine().getStatusCode(), equalTo(200));
        ensureHttp(server).gotStubsCommitmentsDone();
    }

    @Test
    public void shouldBePossibleToSpecifyKeyStoresWithStandardProperties() throws GeneralSecurityException, IOException, URISyntaxException {
        // use a different key store
        System.setProperty("javax.net.ssl.keyStore", findKeystorePath(testPrivateKeystore));
        System.setProperty("javax.net.ssl.keyStorePassword", testPrivateKeystorePass);

        server.run();
        whenHttp(server).match(get("/asd")).then(ok()).mustHappen();


        HttpResponse execute = sslReadyHttpClient(loadKeystore(testCertKeystore, testCertKeystorePass), null, null).execute(new HttpGet("https://localhost:" + server.getPort() + "/asd"));

        assertThat(execute.getStatusLine().getStatusCode(), equalTo(200));
        ensureHttp(server).gotStubsCommitmentsDone();
    }

    @Test
    public void shouldFailWhenClientDoesNotConfigureTrust() {
        server.run();
        whenHttp(server).match(get("/asd")).then(ok()).mustHappen(0);

        try {
            sslReadyHttpClient(null, null, null).execute(new HttpGet("https://localhost:" + server.getPort() + "/asd"));
            fail("Request should have failed as client does not trust server's TLS certificate");
        } catch (Exception e) {
            assertThat(e, isA(SSLHandshakeException.class));
        }

        ensureHttp(server).gotStubsCommitmentsDone();
    }

    @Test
    public void shouldFailWhenServerRequiresAuthentication() throws IOException, URISyntaxException {
        server.clientAuth(loadKeystoreAsBytes(testCertKeystore), testCertKeystorePass).run();
        whenHttp(server).match(get("/asd")).then(ok()).mustHappen(0);

        try {
            sslReadyHttpClient(loadKeystore(defaultCertKeystore, defaultCertKeystorePass), null, null).execute(new HttpGet("https://localhost:" + server.getPort() + "/asd"));

            fail("Request should have failed as server requires client authentication");
        } catch (Exception e) {
            assertThat(e, isA(IOException.class));
        }

        ensureHttp(server).gotStubsCommitmentsDone();
    }

    @Test
    public void shouldPassWhenSendingCertificateToServerRequiringAuthentication() throws GeneralSecurityException, IOException, URISyntaxException {
        // client authentication
        server.clientAuth(loadKeystoreAsBytes(testCertKeystore), testCertKeystorePass).run();
        whenHttp(server).match(get("/asd")).then(ok()).mustHappen();

        InputStream clientKeyStore = testPrivateKeystoreURL.openStream();
        String clientKeyStorePass = "secret";
        HttpResponse execute = sslReadyHttpClient(loadKeystore(defaultCertKeystore, defaultCertKeystorePass), clientKeyStore, clientKeyStorePass).execute(new HttpGet("https://localhost:" + server.getPort() + "/asd"));

        assertThat(execute.getStatusLine().getStatusCode(), equalTo(200));
        ensureHttp(server).gotStubsCommitmentsDone();
    }

    @Test
    public void shouldFailSendingNonAllowedCertificate() throws IOException, URISyntaxException {
        // client authentication
        server.clientAuth(loadKeystoreAsBytes(testPrivateKeystore), testPrivateKeystorePass).run();
        whenHttp(server).match(get("/asd")).then(ok()).mustHappen(0);

        // sending a client certificate that is not allowed
        InputStream clientKeyStore = StubServer.class.getResourceAsStream("keystore_server");
        String clientKeyStorePass = "secret";

        try {
            sslReadyHttpClient(loadKeystore(defaultCertKeystore, defaultCertKeystorePass), clientKeyStore, clientKeyStorePass).execute(new HttpGet("https://localhost:" + server.getPort() + "/asd"));
            fail("Request should have failed as server does not accept this client certificate");
        } catch (Exception e) {
            assertThat(e, isA(IOException.class));
        }

        ensureHttp(server).gotStubsCommitmentsDone();
    }

    /**
     * Helper which returns HTTP client configured for https session
     *
     * @param certKeystore        trust store containing server's certificate
     * @param privateKeystore     key store for client authentication
     * @param privateKeystorePass key store password
     */
    private HttpClient sslReadyHttpClient(KeyStore certKeystore, InputStream privateKeystore, String privateKeystorePass) throws GeneralSecurityException {
        SSLContextBuilder sslContextBuilder = SSLContexts.custom();

        // verifying server's identity
        if (certKeystore != null) {
            sslContextBuilder.loadTrustMaterial(certKeystore, null);
        }

        // client authentication
        if (privateKeystore != null) {
            var store = loadKeystore(privateKeystore, privateKeystorePass);
            sslContextBuilder.loadKeyMaterial(store, privateKeystorePass.toCharArray());
        }

        return HttpClientBuilder.create().setSSLSocketFactory(new SSLConnectionSocketFactory(sslContextBuilder.build())).build();
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
    @SuppressWarnings("SameParameterValue")
    private KeyStore loadKeystore(String resource, String password) {
        return loadKeystore(getClass().getResourceAsStream("/" + resource), password);
    }

    /**
     * Create key store from input stream
     */
    private KeyStore loadKeystore(InputStream keystore, String password) {
        try (InputStream trustStore = keystore) {
            KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
            store.load(trustStore, password.toCharArray());
            return store;
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] loadKeystoreAsBytes(String resource) throws URISyntaxException, IOException {
        return Files.readAllBytes(Path.of(getClass().getResource("/" + resource).toURI()));
    }

    @SuppressWarnings("SameParameterValue")
    private String findKeystorePath(String resource) throws URISyntaxException {
        return getClass().getResource("/" + resource).toURI().getPath();
    }
}
