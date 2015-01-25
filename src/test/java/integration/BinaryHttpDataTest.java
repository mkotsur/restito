package integration;

import javax.ws.rs.core.MediaType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.jayway.restassured.RestAssured;

import com.xebialabs.restito.server.StubServer;

import static com.jayway.restassured.RestAssured.expect;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.charset;
import static com.xebialabs.restito.semantics.Action.contentType;
import static com.xebialabs.restito.semantics.Action.resourceContent;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.startsWithUri;
import static java.nio.charset.StandardCharsets.*;
import static java.nio.charset.StandardCharsets.UTF_16;
import static javax.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class BinaryHttpDataTest {

    private StubServer server;

    @Before
    public void start() {
        server = new StubServer().run();
        RestAssured.port = server.getPort();
    }

    @After
    public void stop() {
        server.stop();
    }


    @Test
    public void shouldSendRawDataFromFileWithoutCaringAboutEncoding() {
        whenHttp(server)
                .match(startsWithUri("/my-file-utf-16")).
                then(resourceContent("content.UTF-16.ukr"));

        whenHttp(server)
                .match(startsWithUri("/my-file-utf-8")).
                then(resourceContent("content.UTF-8.ukr"));

        whenHttp(server)
                .match(startsWithUri("/my-file-utf-8-with-header")).
                then(resourceContent("content.UTF-8.ukr"));

        expect()
                .header(CONTENT_TYPE, is(nullValue()))
                .header(CONTENT_LENGTH, is(not(nullValue())))
                .content(equalTo("Привіт, світ!"))
                .when().get("/my-file-utf-8");

        expect()
                .header(CONTENT_TYPE, is(nullValue()))
                .header(CONTENT_LENGTH, is(not(nullValue())))
                .content(equalTo(new String("Привіт, світ!".getBytes(UTF_16))))
                .when().get("/my-file-utf-16");

        expect()
                .header(CONTENT_TYPE, is(nullValue()))
                .header(CONTENT_LENGTH, is(not(nullValue())))
                .content(equalTo(new String("Привіт, світ!".getBytes(UTF_16))))
                .when().get("/my-file-utf-16");
    }

    @Test
    public void shouldSupportSpecificationOfMediaTypeStringInResourceContentCall() {
        whenHttp(server)
                .match(startsWithUri("/my-file-utf-16")).
                then(
                        resourceContent("content.UTF-16.ukr", "UTF-16"),
                        contentType("application/text")
                );

        expect()
                .header(CONTENT_TYPE, equalTo("application/text;charset=UTF-16"))
                .content(equalTo("Привіт, світ!"))
                .when().get("/my-file-utf-16");
    }

    @Test
    public void shouldSupportSpecificationOfContentTypeAsString() {
        whenHttp(server)
                .match(startsWithUri("/my-file-utf-16")).
                then(
                        charset(UTF_16),
                        resourceContent("content.UTF-16.ukr"),
                        contentType("application/text")
                );

        expect()
                .header(CONTENT_TYPE, equalTo("application/text;charset=UTF-16"))
                .content(equalTo("Привіт, світ!"))
                .when().get("/my-file-utf-16");
    }

    @Test
    public void shouldSupportSpecificationOfContentTypeAsAnObject() {
        whenHttp(server)
                .match(startsWithUri("/my-file-utf-16")).
                then(
                        resourceContent("content.UTF-16.ukr"),
                        contentType(MediaType.TEXT_PLAIN_TYPE),
                        charset(UTF_16)
                );

        expect()
                .header(CONTENT_TYPE, equalTo("text/plain;charset=UTF-16"))
                .content(equalTo("Привіт, світ!"))
                .when().get("/my-file-utf-16");
    }


    @Test
    public void shouldSupportCharsetSpecBeforeStringContent() {
        whenHttp(server)
                .match(startsWithUri("/bla")).
                then(
                        charset(UTF_16),
                        stringContent("Bla"),
                        contentType("application/text")
                );

        expect()
                .header(CONTENT_TYPE, equalTo("application/text;charset=UTF-16"))
                .when().get("/bla");
    }

    @Test
    public void shouldSupportCharsetSpecAfterStringContent() {
        whenHttp(server)
                .match(startsWithUri("/bla")).
                then(
                        stringContent("Bla"),
                        charset(UTF_16),
                        contentType("application/text")
                );

        expect()
                .header(CONTENT_TYPE, equalTo("application/text;charset=UTF-16"))
                .when().get("/bla");
    }

    @Test
    public void shouldSupportCharsetSpecBeforeResourceContent() {
        whenHttp(server)
                .match(startsWithUri("/bla")).
                then(
                        charset(UTF_16),
                        resourceContent("content.cst"),
                        contentType("application/text")
                );

        expect()
                .header(CONTENT_TYPE, equalTo("application/text;charset=UTF-16"))
                .when().get("/bla");
    }

    @Test
    public void shouldSupportCharsetSpecAfterResourceContent() {
        whenHttp(server)
                .match(startsWithUri("/bla")).
                then(
                        resourceContent("content.cst"),
                        charset(UTF_16),
                        contentType("application/text")
                );

        expect()
                .header(CONTENT_TYPE, equalTo("application/text;charset=UTF-16"))
                .when().get("/bla");
    }

    @Test
    public void shouldBePossibleToOverrideCharsetAfterResourceContentWithCharsetSetThere() {
        whenHttp(server)
                .match(startsWithUri("/bla")).
                then(
                        resourceContent("content.cst", UTF_16BE.name()),
                        charset(UTF_16),
                        contentType("application/text")
                );

        expect()
                .header(CONTENT_TYPE, equalTo("application/text;charset=UTF-16"))
                .when().get("/bla");
    }

    @Test
    public void shouldBePossibleToSpecifyResourceWithContentTypeWithoutCharset() {
        whenHttp(server)
                .match(startsWithUri("/bla")).
                then(
                        resourceContent("content.cst"),
                        contentType("application/text")
                );

        expect()
                .header(CONTENT_TYPE, equalTo("application/text"))
                .when().get("/bla");
    }

    @Test
    public void shouldBePossibleToSpecifyStringContentWithContentTypeWithoutCharset() {
        whenHttp(server)
                .match(startsWithUri("/bla")).
                then(
                        stringContent("Bla"),
                        contentType("application/text")
                );

        expect()
                .header(CONTENT_TYPE, equalTo("application/text"))
                .when().get("/bla");
    }



}
