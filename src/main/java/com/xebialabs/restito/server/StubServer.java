package com.xebialabs.restito.server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.mina.util.AvailablePortFinder;
import org.glassfish.grizzly.PortRange;
import org.glassfish.grizzly.http.server.*;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import com.xebialabs.restito.semantics.Call;
import com.xebialabs.restito.semantics.Stub;
import com.xebialabs.restito.support.behavior.Behavior;
import com.xebialabs.restito.support.log.CallsHelper;

/**
 * The HttpServer wrapper which is responsible for operations like starting and stopping and holding objects that describe server behavior.
 */
public class StubServer {

    @SuppressWarnings("WeakerAccess")
    public final static int DEFAULT_PORT = 6666;

    private final List<Call> calls = Lists.newArrayList();
    private List<Stub> stubs = Lists.newArrayList();
    private final HttpServer simpleServer;

    /**
     * Whether or not the server should run in HTTPS mode.
     */
    public boolean secured;

    private Logger log = LoggerFactory.getLogger(StubServer.class);


    /**
     * Creates a server based on stubs that are used to determine behavior.
     */
    public StubServer(Stub... stubs) {
        this.stubs = new ArrayList<>(Arrays.asList(stubs));
        simpleServer = HttpServer.createSimpleServer(null, new PortRange(DEFAULT_PORT, AvailablePortFinder.MAX_PORT_NUMBER));
    }

    /**
     * This constructor allows to specify the port beside stubs.
     * If the port is busy, Restito won't try to pick different one and java.net.BindException will be thrown.
     */
    public StubServer(int port, Stub... stubs) {
        this.stubs = new ArrayList<>(Arrays.asList(stubs));
        simpleServer = HttpServer.createSimpleServer(null, port);
    }

    /**
     * Creates a server based on Behavior object
     */
    public StubServer(Behavior behavior) {
        this(behavior.getStubs().toArray(new Stub[behavior.getStubs().size()]));
    }

    /**
     * It is possible to add a stub even after the server is started
     */
    public StubServer addStub(Stub s) {
        this.stubs.add(s);
        return this;
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
                    SSLEngineConfigurator sslEngineConfig = new SSLEngineConfigurator(getSslConfig(), false, false, false);
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
        SSLContextConfigurator defaultConfig = SSLContextConfigurator.DEFAULT_CONFIG;
        String keystore_server = createCertificateStore("keystore_server");
        String truststore_server = createCertificateStore("truststore_server");
        defaultConfig.setKeyStoreFile(keystore_server);
        defaultConfig.setKeyStorePass("secret");
        defaultConfig.setTrustStoreFile(truststore_server);
        defaultConfig.setTrustStorePass("secret");
        return defaultConfig;
    }

    /**
     * Copy the Certificate store to the temporary directory, as it needs to be in a real file, not inside a jar for Grizzly to pick it up.
     * @param resourceName The Store to copy
     * @return The absolute path to the temporary keystore.
     * @throws IOException If the store could not be copied.
     */
    private String createCertificateStore(String resourceName) throws IOException {
        URL resource = StubServer.class.getResource("/" + resourceName);
        File store = File.createTempFile(resourceName, "store");
        Files.copy(Resources.newInputStreamSupplier(resource), store);
        store.deleteOnExit();
        return store.getAbsolutePath();
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
     * Returns the port which the server is running at
     */
    public int getPort() {
        return simpleServer.getListeners().iterator().next().getPort();
    }

    /**
     * Returns calls performed to the serer
     */
    public List<Call> getCalls() {
        return calls;
    }

    /**
     * Returns stubs associated with the server
     */
    public List<Stub> getStubs() {
        return stubs;
    }

    private HttpHandler stubsToHandler() {
        return new HttpHandler() {
            @Override
            public void service(Request request, Response response) throws Exception {
                Call call = Call.fromRequest(request);

                CallsHelper.logCall(call);

                boolean processed = false;

                for (Stub stub : Lists.reverse(stubs)) {
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

                synchronized (calls) {
                    calls.add(call);
                }
            }
        };
    }

}
