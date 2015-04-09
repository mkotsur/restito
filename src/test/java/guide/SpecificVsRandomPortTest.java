package guide;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.jayway.restassured.RestAssured;

import com.xebialabs.restito.server.StubServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Covers "Starting and stopping stub server"->"Specific vs random port" user guide chapter
 */
public class SpecificVsRandomPortTest {

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
    public void shouldStartAndStopStubServer() {
        assertTrue(server.getPort() > 0);
    }

    @Test
    public void shouldBePossibleToSpecifyPort() {
        StubServer server1 = new StubServer(8888).run();
        assertEquals(8888, server1.getPort());
        server1.stop();
    }

    @Test
    public void shouldSelectRandomFreePortWhenDefaultOneIsBusy() {
        StubServer server2 = new StubServer().run();
        assertTrue(
                "Expected port " + server2.getPort() + " > " + StubServer.DEFAULT_PORT,
                server2.getPort() > StubServer.DEFAULT_PORT
        );
    }
}
