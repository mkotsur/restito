package com.xebialabs.restito.semantics;

import java.io.OutputStream;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConditionWithApplicablesTest {

    @Mock
    private Response response;

    private static final Condition trueCondition = Condition.custom(Predicates.<Call>alwaysTrue());

    private static final Applicable ok200Applicable = new Applicable() {
        @Override
        public Response apply(final Response r) {
            r.setStatus(HttpStatus.OK_200);
            return r;
        }
    };

    @Test
    public void shouldApplyApplicablesDefinedInConditionWhenConditionTrue() {
        new Stub(
                new ConditionWithApplicables(Predicates.<Call>alwaysTrue(), ok200Applicable),
                Action.header("foo", "bar")
        ).apply(response);

        InOrder inOrder = inOrder(response);
        inOrder.verify(response).setStatus(HttpStatus.OK_200);
        inOrder.verify(response).setHeader("foo", "bar");
    }

    @Test
    public void shouldApplyApplicablesDefinedInConditionWhenConditionHadBeenComposed() {
        new Stub(
                Condition.composite(trueCondition, new ConditionWithApplicables(Predicates.<Call>alwaysTrue(), ok200Applicable)),
                Action.header("foo", "bar")
        ).apply(response);

        InOrder inOrder = inOrder(response);
        inOrder.verify(response).setStatus(HttpStatus.OK_200);
        inOrder.verify(response).setHeader("foo", "bar");
    }


    @Test
    public void shouldDiscoverXmlByPath() throws Exception {

        OutputStream os = mock(OutputStream.class);
        when(response.getOutputStream()).thenReturn(os);

        ConditionWithApplicables condition = Condition.get("/demo/path%20to%20data/data.xml");

        condition.getApplicables().get(0).apply(response);

        verify(os).write("<content>from data.xml</content>".getBytes());
        verify(response).setContentType("application/xml");
    }

    @Test
    public void shouldNotFailIfAutoDiscoveryIsNotPossible() throws Exception {

        Call call = mock(Call.class);
        when(call.getMethod()).thenReturn(Method.GET);

        ConditionWithApplicables condition = Condition.post("/blablabla.xml");
        condition.getApplicables().get(0).apply(response);
        assertFalse(condition.getPredicate().test(call));
    }

}
