package com.xebialabs.restito.semantics;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicates;
import org.glassfish.grizzly.http.server.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.Nullable;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

public class StubTest {

	@Mock
	Response r;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldBuildProperBaseWithInitialValues() {
		Call call = mock(Call.class);

		Stub stub = new Stub();
		assertTrue(stub.isApplicable(call));
		assertEquals(r, stub.apply(r));
	}


	@Test
	public void shouldBeApplicableWhenConditionIsTrue() {
		Stub stub = new Stub(Predicates.<Call>alwaysTrue(), Functions.<Response>identity());
		assertTrue(stub.isApplicable(mock(Call.class)));
	}
	@Test
	public void shouldBeNotApplicableWhenConditionIsFalse() {
		Stub stub = new Stub(Predicates.<Call>alwaysFalse(), Functions.<Response>identity());
		assertFalse(stub.isApplicable(mock(Call.class)));
	}

	@Test
	public void shouldExecuteApplyFunctionOnResponse() {

		new Stub(
				mock(Condition.class),
				new Function<Response, Response>() {
					@Override
					public Response apply(Response input) {
						input.setContentType("boo");
						return input;
					}
				}
		).apply(r);

		verify(r, times(1)).setContentType("boo");
	}

	@Test
	public void shouldComposeConditionsNegative() {
		Stub stub = new Stub(Condition.custom(Predicates.<Call>alwaysTrue()), Functions.<Response>identity());

		assertTrue(stub.isApplicable(mock(Call.class)));

		stub.alsoWhen(Condition.custom(Predicates.<Call>alwaysFalse()));

		assertFalse(stub.isApplicable(mock(Call.class)));
	}

	@Test
	public void shouldComposeConditionsPositive() {
		Stub stub = new Stub(Condition.custom(Predicates.<Call>alwaysTrue()), Functions.<Response>identity());

		assertTrue(stub.isApplicable(mock(Call.class)));

		stub.alsoWhen(Condition.custom(Predicates.<Call>alwaysTrue()));

		assertTrue(stub.isApplicable(mock(Call.class)));
	}

	@Test
	public void shouldComposeMutationsNegative() {
		Stub stub = new Stub(Condition.custom(Predicates.<Call>alwaysTrue()), new Function<Response, Response>() {
			@Override
			public Response apply(Response input) {
				input.setContentType("myType");
				return input;
			}
		});

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

