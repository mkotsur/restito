package com.xebialabs.restito.builder;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.xebialabs.restito.semantics.Call;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.server.StubServer;
import org.glassfish.grizzly.http.Method;

import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static junit.framework.Assert.assertEquals;
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

	@Deprecated
	public void get(final String url) {
		method(Method.GET, url);
	}

	@Deprecated
	public void post(final String url) {
		method(Method.POST, url);
	}

	@Deprecated
	public void put(final String url) {
		method(Method.PUT, url);
	}

	@Deprecated
	public void delete(final String url) {
		method(Method.DELETE, url);
	}

	@Deprecated
	public void method(final Method method,final String uri) {
		Function<List<Call>, Integer> check = new Function<List<Call>, Integer>() {
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

	public void once(Condition... conditions) {

		List<Call> calls = stubServer.getCalls();

		for (Condition condition : conditions) {
			calls = Lists.newArrayList(filter(calls, condition.getPredicate()));
		}


		assertEquals(
				String.format("Expected to happen %s time(s), but happened %s times instead", 1, calls.size()),
				1, calls.size()
		);
	}


	@Deprecated
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
