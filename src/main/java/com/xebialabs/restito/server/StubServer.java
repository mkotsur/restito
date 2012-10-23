package com.xebialabs.restito.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.mina.util.AvailablePortFinder;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;

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

    private List<Call> calls = Lists.newArrayList();
    private List<Stub> stubs = Lists.newArrayList();
    private HttpServer simpleServer;

    private Logger log = LoggerFactory.getLogger(StubServer.class);


    /**
     * Creates a server based on stubs that are used to determine behavior.
     */
    public StubServer(Stub... stubs) {
        this.stubs = new ArrayList<Stub>(Arrays.asList(stubs));
        simpleServer = HttpServer.createSimpleServer(".", AvailablePortFinder.getNextAvailable(DEFAULT_PORT));
    }

    /**
     * This constructor allows to specify the port beside stubs.
     * If the port is busy, Restito won't try to pick different one and java.net.BindException will be thrown.
     */
    public StubServer(int port, Stub... stubs) {
        this.stubs = new ArrayList<Stub>(Arrays.asList(stubs));
        simpleServer = HttpServer.createSimpleServer(".", port);
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
            simpleServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
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
        simpleServer.stop();
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

                calls.add(call);
            }
        };
    }

}
