package com.xebialabs.restito.examples;

import com.xebialabs.restito.server.StubServer;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.contentType;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.get;

public class MyServer {

    public static void main(String[] args) throws Exception {

        final StubServer server = new StubServer().run();

        whenHttp(server).match(get("/hello")).then(contentType("text/html"), stringContent("World!"));

        while (true) { Thread.sleep(10000); }

    }

}
