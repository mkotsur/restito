package com.xebialabs.restito;

import com.google.common.collect.Lists;
import com.xebialabs.restito.behavior.Behavior;
import com.xebialabs.restito.calls.Call;
import com.xebialabs.restito.stubs.Stub;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StubServer {

	public static final int PORT = 6666;
	private List<Call> calls = Lists.newArrayList();
	private List<Stub> stubs = Lists.newArrayList();
	private HttpServer simpleServer;

	private Logger log = LoggerFactory.getLogger(StubServer.class);


	public StubServer(Stub... stubs) {
		this.stubs = new ArrayList<Stub>(Arrays.asList(stubs));
		simpleServer = HttpServer.createSimpleServer(".", PORT);
	}

	public StubServer addStub(Stub s) {
		this.stubs.add(s);
		return this;
	}

	public StubServer(Behavior behavior) {
		this.stubs = behavior.getStubs();
		simpleServer = HttpServer.createSimpleServer(".", PORT);
	}

	public StubServer run() {
		simpleServer.getServerConfiguration().addHttpHandler(stubsToHandler(stubs), "/");
		start();
		return this;
	}

	public void start() {
		try {
			simpleServer.start();
		} catch (Exception e) {
			throw new RuntimeException("Can not start Grizzly server for REST tests.");
		}
	}

	public StubServer stop() {
		simpleServer.stop();
		return this;
	}

	public List<Call> getCalls() {
		return calls;
	}

	private HttpHandler stubsToHandler(List<Stub> stub) {
		return new HttpHandler() {
			@Override
			public void service(Request request, Response response) throws Exception {

				boolean processed = false;

				for (Stub stub : Lists.reverse(stubs)) {
					if (!stub.getWhen().apply(request)) {
						continue;
					}

					stub.getWhat().apply(response);
					processed = true;
					break;
				}

				if (!processed) {
					log.warn("Request {} hasn't been covered by any of {} stubs.", request.getRequestURI(), stubs.size());
				}

				calls.add(Call.fromRequest(request));
			}
		};
	}
}
