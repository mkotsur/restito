package com.xebialabs.restito.semantics;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.OutputStream;

import static org.glassfish.grizzly.http.util.Constants.DEFAULT_HTTP_CHARACTER_ENCODING;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

public class ConditionWithApplicablesTest {

    @Mock
    private Response response;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(response.getCharacterEncoding()).thenReturn(DEFAULT_HTTP_CHARACTER_ENCODING);
    }

    @Test
    public void shouldApplyApplicablesDefinedInConditionWhenConditionTrue() {
        new Stub(
                new Condition(Predicates.alwaysTrue(), Action.ok()),
                Action.header("foo", "bar")
        ).apply(response);

        InOrder inOrder = inOrder(response);
        inOrder.verify(response).setStatus(HttpStatus.OK_200);
        inOrder.verify(response).setHeader("foo", "bar");
    }

    @Test
    public void shouldApplyApplicablesDefinedInConditionWhenConditionHadBeenComposed() {
        new Stub(
                Condition.composite(Condition.alwaysTrue(), new Condition(Predicates.alwaysTrue(), Action.ok())),
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

        Condition condition = Condition.get("/demo/path%20to%20data/data.xml");

        condition.getApplicable().apply(response);

        verify(os).write("<content>from data.xml</content>".getBytes());
        verify(response).setContentType("application/xml");
    }

    @Test
    public void shouldNotFailIfAutoDiscoveryIsNotPossible() {
        Call call = mock(Call.class);
        when(call.getUri()).thenReturn("/blablabla.xml");
        when(call.getMethod()).thenReturn(Method.GET);

        Condition condition = Condition.post("/blablabla.xml");
        condition.getApplicable().apply(response);
        assertFalse(condition.validate(call).isValid());
    }

}
