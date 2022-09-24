package com.xebialabs.restito.server;

import com.xebialabs.restito.semantics.Call;
import com.xebialabs.restito.semantics.Stub;
import com.xebialabs.restito.support.log.CallsHelper;
import org.apache.mina.util.AvailablePortFinder;
import org.glassfish.grizzly.PortRange;
import org.glassfish.grizzly.http.server.*;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.unmodifiableList;

/**
 * The HttpServer wrapper which is responsible for operations like starting and stopping and holding objects that describe server behavior.
 */
public class StubServer {

    @SuppressWarnings("WeakerAccess")
    public final static int DEFAULT_PORT = 6666;

    private static final String SERVER_PRIVATE_KEYSTORE = "keystore_server";
    private static final String SERVER_PRIVATE_KEYSTORE_PASS = "secret";

    static final String SERVER_CERT_KEYSTORE = "keystore_server_cert";

    static final String SERVER_CERT_KEYSTORE_PASS = "changeit";

    private final List<Call> calls = new CopyOnWriteArrayList<>();
    private final List<Stub> stubs = new CopyOnWriteArrayList<>();
    private final HttpServer simpleServer;
    private boolean registerCalls = true;

    /**
     * Whether the server should run in HTTPS mode.
     */
    public boolean secured;

    /**
     * Whether the server requires client authentication in HTTPS mode.
     */
    private boolean clientAuth;
    private byte[] clientAuthTrustStore;
    private String clientAuthTrustStorePass;

    private Logger log = LoggerFactory.getLogger(StubServer.class);


    /**
     * Creates a server based on stubs that are used to determine behavior.
     */
    public StubServer(Stub... stubs) {
        this(DEFAULT_PORT, AvailablePortFinder.MAX_PORT_NUMBER, stubs);
    }

    /**
     * This constructor allows to specify the port range beside stubs.
     * Grizzly will select the first available port.
     */
    public StubServer(int portRangeStart, int portRangeEnd, Stub... stubs) {
        this.stubs.addAll(Arrays.asList(stubs));
        simpleServer = HttpServer.createSimpleServer(null, new PortRange(portRangeStart, portRangeEnd));
    }

    /**
     * This constructor allows to specify the port beside stubs.
     * If the port is busy, Restito won't try to pick different one and java.net.BindException will be thrown.
     */
    public StubServer(int port, Stub... stubs) {
        this.stubs.addAll(Arrays.asList(stubs));
        simpleServer = HttpServer.createSimpleServer(null, port);
    }

    /**
     * It is possible to add a stub even after the server is started
     */
    public StubServer addStub(Stub s) {
        this.stubs.add(s);
        return this;
    }

    /**
     * Removes any previously registered stubs.
     */
    public StubServer clearStubs() {
        this.stubs.clear();
        return this;
    }

    /**
     * Removes any registered stubs calls.
     */
    public StubServer clearCalls() {
        this.calls.clear();
        return this;
    }

    /**
     * Clears the server state.
     */
    public StubServer clear() {
        this.clearStubs();
        this.clearCalls();
        return this;
    }

    /**
     * Defines whether stubs calls will be registered or not.
     *
     * @return current flag value
     */
    public boolean isRegisterCalls() {
        return registerCalls;
    }

    /**
     * Defines whether stubs calls will be registered or not.
     *
     * @param registerCalls new flag value
     */
    public void setRegisterCalls(boolean registerCalls) {
        this.registerCalls = registerCalls;
    }

    /**
     * Starts the server
     */
    public StubServer run() {
        simpleServer.getServerConfiguration().addHttpHandler(stubsToHandler(), "/");
        try {
            if (secured) {
                for (NetworkListener networkListener : simpleServer.getListeners()) {
                    networkListener.setSecure(true);
                    SSLEngineConfigurator sslEngineConfig = new SSLEngineConfigurator(getSslConfig(), false, clientAuth, clientAuth);
                    networkListener.setSSLEngineConfig(sslEngineConfig);
                }
            }
            simpleServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    private SSLContextConfigurator getSslConfig() throws IOException {
        SSLContextConfigurator.DEFAULT_CONFIG.retrieve(System.getProperties()); // refresh in case of changes
        if (SSLContextConfigurator.DEFAULT_CONFIG.validateConfiguration(true)) {
            return SSLContextConfigurator.DEFAULT_CONFIG;
        }
        SSLContextConfigurator sslConfig = new SSLContextConfigurator();
        // key store for server certificate
        byte[] keystore_server = readCertificateStore(SERVER_PRIVATE_KEYSTORE);
        sslConfig.setKeyStoreBytes(keystore_server);
        sslConfig.setKeyStorePass(SERVER_PRIVATE_KEYSTORE_PASS);
        // trust store for client authentication
        if (clientAuth) {
            sslConfig.setTrustStoreBytes(clientAuthTrustStore);
            sslConfig.setTrustStorePass(clientAuthTrustStorePass);
        }
        return sslConfig;
    }

    /**
     * Read the certificate store as bytes for Grizzly to pick it up.
     *
     * @param resourceName The Store to copy
     * @return The keystore in bytes.
     * @throws IOException If the store could not be copied.
     */
    private static byte[] readCertificateStore(String resourceName) throws IOException {
        URL resource = StubServer.class.getResource("/" + resourceName);
        try (InputStream input = resource.openStream()) {
            return input.readAllBytes();
        }
    }

    /**
     * Alias for StubServer.run()
     */
    public void start() {
        run();
    }

    /**
     * Stops the server
     */
    public StubServer stop() {
        simpleServer.shutdownNow();
        return this;
    }

    /**
     * Sets the Server in Secure mode. If it is already running, ignores the call.
     */
    public StubServer secured() {
        if (!simpleServer.isStarted()) {
            this.secured = true;
        }
        return this;
    }

    /**
     * Sets the Server to require client authentication (implies Secure mode).
     * If it is already running, ignores the call.
     */
    public StubServer clientAuth(byte[] trustStore, String trustStorePass) {
        if (!simpleServer.isStarted()) {
            secured();
            this.clientAuth = true;
            this.clientAuthTrustStore = trustStore;
            this.clientAuthTrustStorePass = trustStorePass;
        }
        return this;
    }

    /**
     * Returns the port which the server is running at
     */
    public int getPort() {
        return simpleServer.getListeners().iterator().next().getPort();
    }

    /**
     * Returns calls performed to the server. Returned list is actually a copy of the original one. This is done to prevent concurrency issues. See <a href="https://github.com/mkotsur/restito/issues/33">#33</a>.
     */
    public List<Call> getCalls() {
        return unmodifiableList(calls);
    }

    /**
     * Returns stubs associated with the server. Returned list is actually a copy of the original one. This is done to prevent concurrency issues. See <a href="https://github.com/mkotsur/restito/issues/33">#33</a>.
     */
    public List<Stub> getStubs() {
        return unmodifiableList(stubs);
    }

    private HttpHandler stubsToHandler() {
        return new HttpHandler() {
            @Override
            public void service(Request request, Response response) throws Exception {
                Call call = Call.fromRequest(request);

                CallsHelper.logCall(call);

                if (isRegisterCalls()) {
                    calls.add(call);
                }

                boolean processed = false;
                ListIterator<Stub> iterator = stubs.listIterator(stubs.size());
                while (iterator.hasPrevious()) {
                    Stub stub = iterator.previous();
                    if (!stub.isApplicable(call)) {
                        continue;
                    }

                    stub.apply(response);
                    processed = true;
                    break;
                }

                if (!processed) {
                    response.setStatus(HttpStatus.NOT_FOUND_404);
                    log.warn("Request {} hasn't been covered by any of {} stubs.", request.getRequestURI(), stubs.size());
                }
            }
        };
    }

}
