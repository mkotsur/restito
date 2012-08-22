package com.xebialabs.restito;

import com.google.common.collect.Lists;
import com.xebialabs.restito.behavior.Behavior;
import com.xebialabs.restito.calls.Call;
import com.xebialabs.restito.stubs.Stub;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StubServer {

	public static final int PORT = 6666;
	private List<Call> calls = Lists.newArrayList();
	private List<Stub> stubs = Lists.newArrayList();
	private HttpServer simpleServer;


	public StubServer(Stub... stubs) {
		this.stubs = new ArrayList<Stub>(Arrays.asList(stubs));
		simpleServer = HttpServer.createSimpleServer(".", PORT);
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
				for (Stub stub : stubs) {
					if (!stub.getWhen().apply(request)) {
						continue;
					}

					response = stub.getWhat().apply(response);
				}

				calls.add(Call.fromRequest(request));
			}
		};
	}
}
