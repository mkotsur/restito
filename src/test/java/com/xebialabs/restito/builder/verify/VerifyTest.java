package com.xebialabs.restito.builder.verify;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.xebialabs.restito.semantics.Call;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.server.StubServer;
import junit.framework.AssertionFailedError;
import org.glassfish.grizzly.http.Method;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.xebialabs.restito.builder.verify.Verify.verifyHttp;
import static com.xebialabs.restito.semantics.Condition.custom;
import static com.xebialabs.restito.semantics.Condition.method;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VerifyTest {

	@Mock
	private StubServer stubServer;

	@Mock
	private Call getCall, postCall;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(getCall.getMethod()).thenReturn(Method.GET).getMock();
		when(postCall.getMethod()).thenReturn(Method.POST).getMock();
	}

	@Test
	public void shouldPassWhenCorrectExpectations() {
		when(stubServer.getCalls()).thenReturn(Lists.<Call>newArrayList(mock(Call.class)));

		verifyHttp(stubServer).once(conditionTrue());
		verifyHttp(stubServer).times(1, conditionTrue());
		verifyHttp(stubServer).never(conditionFalse());
	}

	@Test(expected = AssertionFailedError.class)
	public void shouldFailWhenExpectedOnceButNeverHappens() {
		when(stubServer.getCalls()).thenReturn(Lists.<Call>newArrayList(mock(Call.class)));
		verifyHttp(stubServer).once(conditionFalse());
	}

	@Test(expected = AssertionFailedError.class)
	public void shouldFailWhenNeverExpectedButHappens() {
		when(stubServer.getCalls()).thenReturn(Lists.<Call>newArrayList(mock(Call.class)));
		verifyHttp(stubServer).never(conditionTrue());
	}

	@Test(expected = AssertionFailedError.class)
	public void shouldFailWhenHappensLessTimesThenExpected() {
		when(stubServer.getCalls()).thenReturn(Lists.<Call>newArrayList(mock(Call.class)));
		verifyHttp(stubServer).times(10, conditionTrue());
	}

	@Test
	public void shouldPassWhen2CallsInOrderHappenAsExpected() {
		when(stubServer.getCalls()).thenReturn(Lists.<Call>newArrayList(getCall, postCall));
		verifyHttp(stubServer).once(method(Method.GET)).then().once(method(Method.POST));
	}

	@Test(expected = AssertionFailedError.class)
	public void shouldFailWhenSecondCallInOrderMissing() {
		when(stubServer.getCalls()).thenReturn(Lists.<Call>newArrayList(getCall));
		verifyHttp(stubServer).once(method(Method.GET)).then().once(method(Method.POST));
	}

	@Test(expected = AssertionFailedError.class)
	public void shouldFailWhenFirstCallInOrderMissing() {
		when(stubServer.getCalls()).thenReturn(Lists.<Call>newArrayList(postCall));
		verifyHttp(stubServer).once(method(Method.GET)).then().once(method(Method.POST));
	}

	@Test(expected = AssertionFailedError.class)
	public void shouldFailWhenOrderIsWrong() {
		when(stubServer.getCalls()).thenReturn(Lists.<Call>newArrayList(postCall, getCall));
		verifyHttp(stubServer).once(method(Method.GET)).then().once(method(Method.POST));
	}

	@Test(expected = AssertionFailedError.class)
	public void shouldVerifyTwoIdenticalWhenCalledOnlyOne() {
		when(stubServer.getCalls()).thenReturn(Lists.<Call>newArrayList(mock(Call.class)));
		verifyHttp(stubServer).once(
				conditionTrue()
		).then().once(
				conditionTrue()
		);
	}

	@Test
	public void shouldPassWhenFirstConditionDoesNotHappen() {
		when(stubServer.getCalls()).thenReturn(Lists.<Call>newArrayList(mock(Call.class)));
		verifyHttp(stubServer).never(
				conditionFalse()
		).then().once(
				conditionTrue()
		);
	}

	@Test(expected = AssertionFailedError.class)
	public void shouldFailWhenConditionHappensMoreTimesThenExpectedEvenIfThenItExpectedAgain() {
		when(stubServer.getCalls()).thenReturn(Lists.<Call>newArrayList(mock(Call.class), mock(Call.class), mock(Call.class)));
		verifyHttp(stubServer).times(2,
				conditionTrue()
		).then().once(
				conditionTrue()
		);
	}

	private Condition conditionFalse() {
		return custom(Predicates.<Call>alwaysFalse());
	}

	private Condition conditionTrue() {
		return custom(Predicates.<Call>alwaysTrue());
	}
}
