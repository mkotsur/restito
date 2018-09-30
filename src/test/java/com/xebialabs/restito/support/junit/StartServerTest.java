package com.xebialabs.restito.support.junit;

import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;

import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.restassured.RestAssured;
import com.xebialabs.restito.builder.stub.StubHttp;
import com.xebialabs.restito.semantics.Action;
import com.xebialabs.restito.semantics.Condition;

public class StartServerTest {

    @Rule
    public StartServer startServer = new StartServer();

    @Before
    public void setUpRestAssured() {
        RestAssured.port = this.startServer.getServer().getPort();
    }

    @Test
    public void shouldStartServerForInstanceRule() throws Exception {
        StubHttp.whenHttp(this.startServer.getServer())
                .match(Condition.get("/"))
                .then(Action.success());
        RestAssured.expect()
                .statusCode(SC_OK)
                .when().get("/");
    }

    @Test
    public void shouldStartAnotherServerForInstanceRule() throws Exception {
        StubHttp.whenHttp(this.startServer.getServer())
                .match(Condition.get("/"))
                .then(Action.status(HttpStatus.NOT_FOUND_404));
        RestAssured.expect()
                .statusCode(SC_NOT_FOUND)
                .when().get("/");
    }
}