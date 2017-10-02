package integration;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import com.xebialabs.restito.server.StubServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.charset.Charset;

import static com.jayway.restassured.RestAssured.expect;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.nullValue;

public class CharsetAndEncodingTest {

    private StubServer server;

    private static Charset CP_1251 = Charset.forName("Cp1251");

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
    public void shouldEncodeStringsUsingDefaultPlatformCharset() {
        whenHttp(server)
                .match("/default-charset").
                then(stringContent("Хеллоу"));

        expect().header("Content-Type", is(nullValue()))
                .content(equalTo("Хеллоу"))
                .when().get("/default-charset");
    }

    @Test
    public void shouldEncodeStringsUsingExplicitlyPassedCharset() {
        whenHttp(server)
                .match("/custom-charset-1").
                then(contentType("application/text"), stringContent("Хеллоу", CP_1251));

        whenHttp(server)
                .match("/custom-charset-2").
                then(stringContent("Хеллоу", CP_1251), contentType("application/text"));

        expect().header("Content-Type", is("application/text;charset=windows-1251"))
                .content(equalTo("Хеллоу"))
                .when().get("/custom-charset-1");

        expect().header("Content-Type", is("application/text;charset=windows-1251"))
                .content(equalTo("Хеллоу"))
                .when().get("/custom-charset-2");
    }

    @Test
    public void shouldNotIncludeExplicitlyPassedInHeadersCharsetWhenContentTypeHeaderIsMissing() {
        whenHttp(server)
                .match("/custom-charset-3").
                then(stringContent("Хеллоу", CP_1251));

        Response response = expect().header("Content-Type", is(nullValue()))
                .when().get("/custom-charset-1");

        assertArrayEquals(response.body().asByteArray(), "Хеллоу".getBytes(CP_1251));
    }


}
