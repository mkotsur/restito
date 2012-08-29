package com.xebialabs.restito.verify;

import com.google.common.base.Function;
import com.xebialabs.restito.StubServer;
import com.xebialabs.restito.calls.Call;
import org.glassfish.grizzly.http.Method;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class VerifyHttp {

	private final StubServer stubServer;

	private int times = 1;

	public static VerifyHttp verifyHttp(StubServer stubServer, int times) {
		return new VerifyHttp(stubServer, times);
	}

	public static VerifyHttp verifyHttp(StubServer stubServer) {
		return new VerifyHttp(stubServer);
	}

	private VerifyHttp(StubServer stubServer) {
		this.stubServer = stubServer;
	}

	private VerifyHttp(StubServer stubServer, int times) {
		this.stubServer = stubServer;
		this.times = times;
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
		Function<List<Call>, Integer> check = new Function<List<Call>, Integer>() {
			@Override
			public Integer apply(List<Call> input) {
				int i = 0;
				for (Call call : input) {
					if (method.equals(call.getMethod()) && uri.equals(call.getUri())) {
						i++;
					}
				}

				return i;
			}
		};

		Integer happenedTimes = check.apply(stubServer.getCalls());
		assertThat(
				String.format("Expected to meet a %s call to %s %s times, got %s instead .", method, uri, times, happenedTimes),
				happenedTimes.equals(times)
		);
	}


	public static class Times {
		public static int never() {
			return 0;
		}

		public static int once() {
			return 1;
		}

		public static int times(int i) {
			return i;
		}
	}

}
