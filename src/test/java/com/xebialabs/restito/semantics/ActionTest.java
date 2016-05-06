package com.xebialabs.restito.semantics;

import java.io.OutputStream;
import java.io.Writer;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.xebialabs.restito.semantics.Action.charset;
import static com.xebialabs.restito.semantics.Action.composite;
import static com.xebialabs.restito.semantics.Action.header;
import static com.xebialabs.restito.semantics.Action.resourceContent;
import static com.xebialabs.restito.semantics.Action.sequence;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ActionTest {

    @Mock
    private Response response;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(response.getWriter()).thenReturn(mock(Writer.class));
        when(response.getOutputStream()).thenReturn(mock(OutputStream.class));
        when(response.getCharacterEncoding()).thenReturn("UTF-8");
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
        verify(response.getOutputStream()).write("<test></test>".getBytes());
    }

    @Test
    public void shouldApplyJsonContent() throws Exception {
        resourceContent("content.json").apply(response);

        verify(response).setContentType("application/json");
        verify(response).setContentLength(15);
        verify(response.getOutputStream()).write("{\"asd\": \"cool\"}".getBytes());
    }

    @Test
    public void shouldApplyCharset() throws Exception {
        charset("my-amazing-encoding").apply(response);

        verify(response).setCharacterEncoding("my-amazing-encoding");
    }

    @Test
    public void shouldApplyUnicodeJsonContent() throws Exception {
        resourceContent("unicode-content.json").apply(response);

        verify(response).setContentType("application/json");
        verify(response).setContentLength(40); //40 bytes / 22 characters
        verify(response.getOutputStream()).write("{\"test\" : \"的这款单肩包集经典\"}".getBytes());
    }

    @Test
    public void shouldApplyContentWithCustomType() throws Exception {
        resourceContent("content.cst").apply(response);

        verify(response, never()).setContentType(any(String.class));
        verify(response).setContentLength(14);
        verify(response.getOutputStream()).write("Custom content".getBytes());
    }

    @Test
    public void shouldApplyStringContent() throws Exception {
        stringContent("asd").apply(response);

        verify(response.getOutputStream()).write("asd".getBytes());
    }

    @Test
    public void shouldApplyHeader() throws Exception {
        header("Location", "google.com").apply(response);

        verify(response).setHeader("Location", "google.com");
    }

    @Test
    public void shouldCreateCompositeActionFromActions() {
        composite(status(HttpStatus.OK_200), header("foo", "bar")).apply(response);

        InOrder inOrder = inOrder(response);
        inOrder.verify(response).setStatus(HttpStatus.OK_200);
        inOrder.verify(response).setHeader("foo", "bar");

    }

    @Test
    public void shouldCreateCompositeActionFromApplicables() {
        composite((Applicable)status(HttpStatus.NOT_ACCEPTABLE_406)).apply(response);

        verify(response).setStatus(HttpStatus.NOT_ACCEPTABLE_406);
    }

    @Test
    public void shouldCreateSequenceActionFromSingleAction() throws Exception {
        Action action = sequence(stringContent("foo"));

        action.apply(response);
        verify(response.getOutputStream()).write("foo".getBytes());
    }

    @Test
    public void shouldCreateSequenceActionFromActions() throws Exception {
        Action action = sequence(stringContent("foo"), stringContent("bar"), stringContent("baz"));

        action.apply(response);
        verify(response.getOutputStream()).write("foo".getBytes());

        action.apply(response);
        verify(response.getOutputStream()).write("bar".getBytes());

        action.apply(response);
        verify(response.getOutputStream()).write("baz".getBytes());
    }

}
