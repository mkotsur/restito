package guide;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.xebialabs.restito.server.StubServer;
import com.xebialabs.restito.support.junit.NeedsServer;
import com.xebialabs.restito.support.junit.ServerDependencyRule;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;


// This Suite stuff is just to run tests from a subclass
@RunWith(Suite.class)
@Suite.SuiteClasses(JunitIntegrationTest.MyTest.class)
public class JunitIntegrationTest {

    public static abstract class MyParentTest {

        protected StubServer stubServer;

        @Rule
        public ServerDependencyRule serverDependency = new ServerDependencyRule();

        @Before
        public void startServer() {
            if (serverDependency.isServerDependent()) {
                stubServer = new StubServer().run();
            }
        }

        @After
        public void stopServer() {
            if (stubServer != null) {
                stubServer.stop();
            }
        }
    }

    public static class MyTest extends MyParentTest {
        @Test
        @NeedsServer
        public void shouldStartServer() {
            assertTrue(stubServer.getPort() > 0);
        }

        @Test
        public void shouldStartNoServer() {
            assertNull(stubServer);
        }

        @Test
        @NeedsServer
        public void shouldStartServerAgain() {
            assertTrue(stubServer.getPort() > 0);
        }
    }
}
