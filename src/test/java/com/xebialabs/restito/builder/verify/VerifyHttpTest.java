package com.xebialabs.restito.builder.verify;

import com.xebialabs.restito.semantics.Call;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.semantics.Predicates;
import com.xebialabs.restito.server.StubServer;
import org.glassfish.grizzly.http.Method;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;

import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Condition.custom;
import static com.xebialabs.restito.semantics.Condition.method;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VerifyHttpTest {

    @Mock
    private StubServer stubServer;

    @Mock
    private Call getCall, postCall;

    @Before
    public void init() {
        when(getCall.getMethod()).thenReturn(Method.GET).getMock();
        when(postCall.getMethod()).thenReturn(Method.POST).getMock();
    }

    @Test
    public void shouldPassWhenCorrectExpectations() {
        when(stubServer.getCalls()).thenReturn(new ArrayList<Call>() {{ add(mock(Call.class)); }});

        verifyHttp(stubServer).once(conditionTrue());
        verifyHttp(stubServer).times(1, conditionTrue());
        verifyHttp(stubServer).never(conditionFalse());
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenExpectedOnceButNeverHappens() {
        when(stubServer.getCalls()).thenReturn(new ArrayList<Call>() {{ add(mock(Call.class)); }});
        verifyHttp(stubServer).once(conditionFalse());
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenNeverExpectedButHappens() {
        when(stubServer.getCalls()).thenReturn(new ArrayList<Call>() {{ add(mock(Call.class)); }});
        verifyHttp(stubServer).never(conditionTrue());
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenHappensLessTimesThenExpected() {
        when(stubServer.getCalls()).thenReturn(new ArrayList<Call>() {{ add(mock(Call.class)); }});
        verifyHttp(stubServer).times(10, conditionTrue());
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenHappensMoreTimesThenExpected() {
        when(stubServer.getCalls()).thenReturn(new ArrayList<Call>() {{ add(mock(Call.class)); add(mock(Call.class)); add(mock(Call.class)); }});
        verifyHttp(stubServer).times(2, conditionTrue());
    }

    @Test
    public void shouldPassWhenHappensMoreTimesThenAtLeastExpected() {
        when(stubServer.getCalls()).thenReturn(new ArrayList<Call>() {{ add(mock(Call.class)); add(mock(Call.class)); add(mock(Call.class)); }});
        verifyHttp(stubServer).atLeast(2, conditionTrue());
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenHappensLessTimesThenAtLeastExpected() {
        when(stubServer.getCalls()).thenReturn(new ArrayList<Call>() {{ add(mock(Call.class)); add(mock(Call.class)); add(mock(Call.class)); }});
        verifyHttp(stubServer).times(4, conditionTrue());
    }

    @Test
    public void shouldPassWhen2CallsInOrderHappenAsExpected() {
        when(stubServer.getCalls()).thenReturn(new ArrayList<Call>() {{ add(getCall); add(postCall); }});
        verifyHttp(stubServer).once(method(Method.GET)).then().once(method(Method.POST));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenSecondCallInOrderMissing() {
        when(stubServer.getCalls()).thenReturn(new ArrayList<Call>() {{ add(getCall); }});
        verifyHttp(stubServer).once(method(Method.GET)).then().once(method(Method.POST));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenFirstCallInOrderMissing() {
        when(stubServer.getCalls()).thenReturn(new ArrayList<Call>() {{ add(postCall); }});
        verifyHttp(stubServer).once(method(Method.GET)).then().once(method(Method.POST));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenOrderIsWrong() {
        when(stubServer.getCalls()).thenReturn(new ArrayList<Call>() {{ add(postCall); add(getCall); }});
        verifyHttp(stubServer).once(method(Method.GET)).then().once(method(Method.POST));
    }

    @Test(expected = AssertionError.class)
    public void shouldVerifyTwoIdenticalWhenCalledOnlyOne() {
        when(stubServer.getCalls()).thenReturn(new ArrayList<Call>() {{ add(mock(Call.class)); }});
        verifyHttp(stubServer).once(
                conditionTrue()
        ).then().once(
                conditionTrue()
        );
    }

    @Test
    public void shouldPassWhenFirstConditionDoesNotHappen() {
        when(stubServer.getCalls()).thenReturn(new ArrayList<Call>() {{ add(mock(Call.class)); }});
        verifyHttp(stubServer).never(
                conditionFalse()
        ).then().once(
                conditionTrue()
        );
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenConditionHappensMoreTimesThenExpectedEvenIfThenItExpectedAgain() {
        when(stubServer.getCalls()).thenReturn(new ArrayList<Call>() {{ add(mock(Call.class)); add(mock(Call.class)); add(mock(Call.class)); }});
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
