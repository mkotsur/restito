package com.xebialabs.restito;

import com.google.common.collect.Lists;
import com.xebialabs.restito.calls.Call;
import com.xebialabs.restito.stubs.Stub;
import org.glassfish.grizzly.http.server.*;

import java.util.Arrays;
import java.util.List;

public class StubServer {

	public static final int PORT = 6666;
	private List<Call> calls = Lists.newArrayList();
	private List<Stub> stubs = Lists.newArrayList();


	public StubServer(Stub... stubs) {
		this.stubs = Arrays.asList(stubs);
	}

	public StubServer run() {


		HttpServer simpleServer = HttpServer.createSimpleServer(".", PORT);
		simpleServer.getServerConfiguration().addHttpHandler(
				new HttpHandler() {
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
				},
				"/"
		);

		try {
			simpleServer.start();
		} catch (Exception e) {
			throw new RuntimeException("Can not start Grizzly server for REST tests.");
		}

		return this;
	}

	public StubServer stop() {
		return this;
	}

	public List<Call> getCalls() {
		return calls;
	}
}
