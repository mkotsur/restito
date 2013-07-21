package com.xebialabs.restito.examples;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xebialabs.restito.semantics.Action;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.server.StubServer;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.resourceContent;
import static com.xebialabs.restito.semantics.Condition.get;
import static com.xebialabs.restito.semantics.Condition.parameter;
import static com.xebialabs.restito.semantics.Condition.uri;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class WikiClientTest {

    private StubServer server;

     @Before
     public void start() {
         server = new StubServer().run();
     }

    @After
    public void stop() {
        server.stop();
    }

    @Test
    public void shouldWork() throws Exception {
        whenHttp(server).match(
                get("/w/api.php"),
                parameter("titles", "Title 1|Title 2")
        ).then(resourceContent("revisions.json"));

        String entryPoint = "http://localhost:" + server.getPort();
        PageRevision revision = new WikiClient(entryPoint).getMostRecentRevision("Title 1", "Title 2");

        assertThat(revision.name, is("Title 2"));
    }


}
