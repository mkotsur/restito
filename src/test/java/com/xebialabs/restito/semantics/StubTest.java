package com.xebialabs.restito.semantics;

import javax.annotation.Nullable;
import org.glassfish.grizzly.http.server.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicates;

import static com.xebialabs.restito.semantics.Action.contentType;
import static com.xebialabs.restito.semantics.Action.success;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class StubTest {

	@Mock
	Response r;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldBeApplicableWhenConditionIsTrue() {
		Stub stub = new Stub(Condition.custom(Predicates.<Call>alwaysTrue()), success());
		assertTrue(stub.isApplicable(mock(Call.class)));
	}
	@Test
	public void shouldBeNotApplicableWhenConditionIsFalse() {
		Stub stub = new Stub(Condition.custom(Predicates.<Call>alwaysFalse()), success());
		assertFalse(stub.isApplicable(mock(Call.class)));
	}

	@Test
	public void shouldExecuteApplyFunctionOnResponse() {
		new Stub(mock(Condition.class), contentType("boo")).apply(r);
		verify(r, times(1)).setContentType("boo");
	}

	@Test
	public void shouldComposeConditionsNegative() {
		Stub stub = new Stub(Condition.custom(Predicates.<Call>alwaysTrue()), success());

		assertTrue(stub.isApplicable(mock(Call.class)));

		stub.alsoWhen(Condition.custom(Predicates.<Call>alwaysFalse()));

		assertFalse(stub.isApplicable(mock(Call.class)));
	}

	@Test
	public void shouldComposeConditionsPositive() {
		Stub stub = new Stub(Condition.custom(Predicates.<Call>alwaysTrue()), success());

		assertTrue(stub.isApplicable(mock(Call.class)));

		stub.alsoWhen(Condition.custom(Predicates.<Call>alwaysTrue()));

		assertTrue(stub.isApplicable(mock(Call.class)));
	}

	@Test
	public void shouldComposeMutationsNegative() {
		Stub stub = new Stub(Condition.custom(Predicates.<Call>alwaysTrue()), contentType("myType"));

		stub.alsoWhat(Action.custom(new Function<Response, Response>() {
			@Override
			public Response apply(@Nullable Response input) {
				input.setCharacterEncoding("UTF-9");
				return input;
			}
		}));

		stub.apply(r);

		verify(r).setContentType("myType");
		verify(r).setCharacterEncoding("UTF-9");
	}

	@Test
	public void shouldIncreaseAppliedTimesCounter() {
		Stub stub = new Stub(
				Condition.custom(Predicates.<Call>alwaysTrue()),
				Action.custom(Functions.<Response>identity())
		);

		assertEquals(0, stub.getAppliedTimes());

		stub.apply(mock(Response.class));

		assertEquals(1, stub.getAppliedTimes());
	}

}

