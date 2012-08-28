package com.xebialabs.restito.verify;

import com.google.common.base.Function;
import com.xebialabs.restito.StubServer;
import com.xebialabs.restito.calls.Call;
import org.glassfish.grizzly.http.Method;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class VerifyHttp {

	private final StubServer stubServer;

	public static VerifyHttp verifyHttp(StubServer stubServer) {
		return new VerifyHttp(stubServer);
	}

	private VerifyHttp(StubServer stubServer) {
		this.stubServer = stubServer;
	}

	public void get(final String url) {
		method(Method.GET, url);
	}

	public void post(final String url) {
		method(Method.POST, url);
	}

	public void put(final String url) {
		method(Method.PUT, url);
	}

	public void delete(final String url) {
		method(Method.DELETE, url);
	}

	public void method(final Method method,final String uri) {
		Function<List<Call>, Boolean> check = new Function<List<Call>, Boolean>() {
			@Override
			public Boolean apply(List<Call> input) {
				for (Call call : input) {
					if (method.equals(call.getMethod()) && uri.equals(call.getUri())) {
						return true;
					}
				}

				return false;
			}
		};

		calls(
				String.format("Expected to meet a %s call to %s, but this never happened.", method, uri),
				stubServer.getCalls(),
				check
		);
	}

	protected void calls(String message, List<Call> call, Function<List<Call>, Boolean> check) {
		assertThat(message, check.apply(call));
	}

}
