package com.xebialabs.restito.semantics;

import org.glassfish.grizzly.http.server.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.xebialabs.restito.semantics.Action.*;
import static com.xebialabs.restito.semantics.ActionSequence.sequence;
import static org.glassfish.grizzly.http.util.HttpStatus.ACCEPTED_202;
import static org.glassfish.grizzly.http.util.HttpStatus.CREATED_201;
import static org.glassfish.grizzly.http.util.HttpStatus.OK_200;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


public class StubTest {

    @Mock
    Response response;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    private static Condition ALWAYS_TRUE = Condition.custom(Predicates.<Call>alwaysTrue());

    @Test
    public void shouldBeApplicableWhenConditionIsTrue() {
        Stub stub = new Stub(ALWAYS_TRUE, ok());
        assertTrue(stub.isApplicable(mock(Call.class)));
    }

    @Test
    public void shouldBeNotApplicableWhenConditionIsFalse() {
        Stub stub = new Stub(Condition.custom(Predicates.<Call>alwaysFalse()), ok());
        assertFalse(stub.isApplicable(mock(Call.class)));
    }

    @Test
    public void shouldExecuteApplyFunctionOnResponse() {
        new Stub(mock(Condition.class), contentType("boo")).apply(response);
        verify(response, times(1)).setContentType("boo");
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

        stub.withExtraAction(Action.custom(new Function<Response, Response>() {
            @Override
            public Response apply(Response input) {
                input.setCharacterEncoding("UTF-9");
                return input;
            }
        }));

        stub.apply(response);

        verify(response).setContentType("myType");
        verify(response).setCharacterEncoding("UTF-9");
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

    @Test
    public void shouldCreateStubsWithSequenceActions() throws Exception {
        ActionSequence action = sequence(status(OK_200), status(CREATED_201), status(ACCEPTED_202));

        Stub stub = new Stub(Condition.custom(Predicates.<Call>alwaysTrue()), action);

        stub.apply(response);
        verify(response).setStatus(OK_200);

        stub.apply(response);
        verify(response).setStatus(CREATED_201);

        stub.apply(response);
        verify(response).setStatus(ACCEPTED_202);
    }

    @Test
    public void shouldCreateStubFromASequenceWithASingleAction() throws Exception {
        Stub stub = new Stub(ALWAYS_TRUE, sequence(status(CREATED_201)));

        stub.apply(response);
        verify(response).setStatus(CREATED_201);
    }


    @Test
    public void shouldAccumulateSequencedActions() {
        Function<Response, Response> f1 = mock(Function.class);
        Function<Response, Response> f2 = mock(Function.class);

        Stub stub = new Stub(ALWAYS_TRUE)
                .withSequenceItem(Action.custom(f1))
                .withSequenceItem(Action.custom(f2));

        stub.apply(response);

        assertEquals(1, stub.getAppliedTimes());
        verify(f1, times(1)).apply(response);
        verify(f2, never()).apply(response);

        stub.apply(response);
        assertEquals(2, stub.getAppliedTimes());
        verify(f2, times(1)).apply(response);
    }

}

