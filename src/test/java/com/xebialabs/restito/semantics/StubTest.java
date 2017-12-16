package com.xebialabs.restito.semantics;

import org.glassfish.grizzly.http.server.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.function.Function;

import static com.xebialabs.restito.semantics.Action.*;
import static com.xebialabs.restito.semantics.ActionSequence.sequence;
import static java.lang.String.format;
import static org.glassfish.grizzly.http.util.HttpStatus.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class StubTest {

    private static Condition ALWAYS_TRUE = Condition.custom(Predicates.alwaysTrue());

    @Mock
    Response response;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldBeApplicableWhenConditionIsTrue() {
        Stub stub = new Stub(ALWAYS_TRUE, ok());
        assertTrue(stub.isApplicable(mock(Call.class)));
    }

    @Test
    public void shouldBeNotApplicableWhenConditionIsFalse() {
        Stub stub = new Stub(Condition.custom(Predicates.alwaysFalse()), ok());
        assertFalse(stub.isApplicable(mock(Call.class)));
    }

    @Test
    public void shouldExecuteApplyFunctionOnResponse() {
        new Stub(mock(Condition.class), contentType("boo")).apply(response);
        verify(response, times(1)).setContentType("boo");
    }

    @Test
    public void shouldComposeConditionsNegative() {
        Stub stub = new Stub(Condition.custom(Predicates.alwaysTrue()), ok());

        assertTrue(stub.isApplicable(mock(Call.class)));

        stub.alsoWhen(Condition.custom(Predicates.alwaysFalse()));

        assertFalse(stub.isApplicable(mock(Call.class)));
    }

    @Test
    public void shouldComposeConditionsPositive() {
        Stub stub = new Stub(Condition.custom(Predicates.alwaysTrue()), ok());

        assertTrue(stub.isApplicable(mock(Call.class)));

        stub.alsoWhen(Condition.custom(Predicates.alwaysTrue()));

        assertTrue(stub.isApplicable(mock(Call.class)));
    }

    @Test
    public void shouldComposeMutationsNegative() {
        Stub stub = new Stub(Condition.custom(Predicates.alwaysTrue()), contentType("myType"));

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
                Condition.custom(Predicates.alwaysTrue()),
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

        Stub stub = new Stub(Condition.custom(Predicates.alwaysTrue()), action);

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
    @SuppressWarnings("unchecked")
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

    @Test
    public void shouldAllowToSetLabel() {
        Stub stub = new Stub(ALWAYS_TRUE).withLabel("source of truth");
        assertEquals(format("Stub@%s [%s]", stub.hashCode(), "source of truth"), stub.toString());
    }

    @Test
    public void shouldGenerateLabelWithHashCode() {
        Stub stub = new Stub(ALWAYS_TRUE);
        assertEquals(format("Stub@%s", Integer.toString(stub.hashCode())), stub.toString());
    }

}

