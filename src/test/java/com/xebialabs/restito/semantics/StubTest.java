package com.xebialabs.restito.semantics;

import org.glassfish.grizzly.http.server.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.xebialabs.restito.semantics.Action.contentType;
import static com.xebialabs.restito.semantics.Action.ok;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
        Stub stub = new Stub(Condition.custom(Predicates.<Call>alwaysTrue()), ok());
        assertTrue(stub.isApplicable(mock(Call.class)));
    }

    @Test
    public void shouldBeNotApplicableWhenConditionIsFalse() {
        Stub stub = new Stub(Condition.custom(Predicates.<Call>alwaysFalse()), ok());
        assertFalse(stub.isApplicable(mock(Call.class)));
    }

    @Test
    public void shouldExecuteApplyFunctionOnResponse() {
        new Stub(mock(Condition.class), contentType("boo")).apply(r);
        verify(r, times(1)).setContentType("boo");
    }

    @Test
    public void shouldComposeConditionsNegative() {
        Stub stub = new Stub(Condition.custom(Predicates.<Call>alwaysTrue()), ok());

        assertTrue(stub.isApplicable(mock(Call.class)));

        stub.alsoWhen(Condition.custom(Predicates.<Call>alwaysFalse()));

        assertFalse(stub.isApplicable(mock(Call.class)));
    }

    @Test
    public void shouldComposeConditionsPositive() {
        Stub stub = new Stub(Condition.custom(Predicates.<Call>alwaysTrue()), ok());

        assertTrue(stub.isApplicable(mock(Call.class)));

        stub.alsoWhen(Condition.custom(Predicates.<Call>alwaysTrue()));

        assertTrue(stub.isApplicable(mock(Call.class)));
    }

    @Test
    public void shouldComposeMutationsNegative() {
        Stub stub = new Stub(Condition.custom(Predicates.<Call>alwaysTrue()), contentType("myType"));

        stub.alsoWhat(Action.custom(new Function<Response, Response>() {
            @Override
            public Response apply(Response input) {
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
                Action.custom(new Function<Response, Response>() {
                    @Override
                    public Response apply(Response input) {
                        return input;
                    }
                })
        );

        assertEquals(0, stub.getAppliedTimes());

        stub.apply(mock(Response.class));

        assertEquals(1, stub.getAppliedTimes());
    }

}

