package com.xebialabs.restito.semantics;

import java.io.Writer;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.google.common.base.Predicates;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConditionWithApplicablesTest {

    @Mock
    private Response response;

    public static final Condition trueCondition = Condition.custom(Predicates.<Call>alwaysTrue());

    public static final Applicable ok200Applicable = new Applicable() {
        @Override
        public Response apply(final Response r) {
            r.setStatus(HttpStatus.OK_200);
            return r;
        }
    };

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

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

        Writer writer = mock(Writer.class);
        when(response.getWriter()).thenReturn(writer);

        ConditionWithApplicables condition = Condition.get("/demo/path%20to%20data/data.xml");

        condition.getApplicables().get(0).apply(response);

        verify(writer).write("<content>from data.xml</content>");
        verify(response).setContentType("application/xml");
    }

    @Test
    public void shouldNotFailIfAutoDiscoveryIsNotPossible() throws Exception {

        Call call = mock(Call.class);
        when(call.getUri()).thenReturn("/blablabla.xml");
        when(call.getMethod()).thenReturn(Method.GET);

        ConditionWithApplicables condition = Condition.post("/blablabla.xml");
        condition.getApplicables().get(0).apply(response);
        assertFalse(condition.getPredicate().apply(call));
    }


    @Test
    public void shouldDiscoverResourceByDirectSlashesStrategy() {

    }

}
