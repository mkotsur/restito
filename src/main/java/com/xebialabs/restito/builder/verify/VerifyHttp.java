package com.xebialabs.restito.builder.verify;

import com.google.common.collect.Lists;
import com.xebialabs.restito.semantics.Call;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.server.StubServer;

import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static junit.framework.Assert.assertEquals;

public class VerifyHttp {

	private final List<Call> calls;

	public static VerifyHttp verifyHttp(StubServer stubServer) {
		return new VerifyHttp(stubServer.getCalls());

	}
	public static VerifyHttp verifyHttp(List<Call> calls) {
		return new VerifyHttp(calls);
	}

	private VerifyHttp(List<Call> calls) {
		this.calls = calls;
	}

	public VerifySequenced once(Condition... conditions) {
		return times(1, conditions);
	}

	public VerifySequenced never(Condition... conditions) {
		return times(0, conditions);
	}

	public VerifySequenced times(int t, Condition... conditions) {
		final List<Call> foundCalls = filterByConditions(conditions);
		assertEquals(
				String.format("Expected to happen %s time(s), but happened %s times instead", t, foundCalls.size()),
				t, foundCalls.size()
		);

		List<Call> callsAfterLastFound = foundCalls.size() == 0 ? calls :
				calls.subList(
						calls.indexOf(foundCalls.get(foundCalls.size() - 1)) + 1,
						calls.size()
				);

		return new VerifySequenced(callsAfterLastFound);
	}

	private List<Call> filterByConditions(Condition[] conditions) {
		List<Call> filteredCalls = calls;

		for (Condition condition : conditions) {
			filteredCalls = Lists.newArrayList(filter(filteredCalls, condition.getPredicate()));
		}
		return filteredCalls;
	}

}
