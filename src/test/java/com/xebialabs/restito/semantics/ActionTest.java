package com.xebialabs.restito.semantics;

import java.io.Writer;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.xebialabs.restito.semantics.Action.*;
import static org.mockito.Mockito.*;

public class ActionTest {

    @Mock
    private Response response;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(response.getWriter()).thenReturn(mock(Writer.class));
    }


    @Test
    public void shouldBuildSuccessfulStub() {
        Action.success().apply(response);

        verify(response).setStatus(HttpStatus.OK_200);
    }

    @Test
    public void shouldBuildOkStub() {
        Action.ok().apply(response);

        verify(response).setStatus(HttpStatus.OK_200);
    }

    @Test
    public void shouldBuildNoContentStub() {
        Action.noContent().apply(response);

        verify(response).setStatus(HttpStatus.NO_CONTENT_204);
    }

    @Test
    public void shouldApplyXmlContent() throws Exception {
        resourceContent("content.xml").apply(response);

        verify(response).setContentType("application/xml");
        verify(response).setContentLength(13);
        verify(response.getWriter()).write("<test></test>");
    }

    @Test
    public void shouldApplyJsonContent() throws Exception {
        resourceContent("content.json").apply(response);

        verify(response).setContentType("application/json");
        verify(response).setContentLength(15);
        verify(response.getWriter()).write("{\"asd\": \"cool\"}");
    }

    @Test
    public void shouldApplyContentWithCustomType() throws Exception {
        resourceContent("content.cst").apply(response);

        verify(response, never()).setContentType(any(String.class));
        verify(response).setContentLength(14);
        verify(response.getWriter()).write("Custom content");
    }

    @Test
    public void shouldApplyStringContent() throws Exception {
        stringContent("asd").apply(response);

        verify(response.getWriter()).write("asd");
    }

    @Test
    public void shouldApplyHeader() throws Exception {
        header("Location", "google.com").apply(response);

        verify(response).setHeader("Location", "google.com");
    }

    @Test
    public void shouldCreateCompositeAction() {
        composite(status(HttpStatus.OK_200), header("foo", "bar")).apply(response);

        InOrder inOrder = inOrder(response);
        inOrder.verify(response).setStatus(HttpStatus.OK_200);
        inOrder.verify(response).setHeader("foo", "bar");

    }
}
