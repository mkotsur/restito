package com.xebialabs.restito.resources;

import org.glassfish.grizzly.http.Method;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SmartDiscovererTest {

    private String prefix;

    private SmartDiscoverer smartDiscoverer;

    @Before
    public void setUp() {
        prefix = getClass().getSimpleName();
        smartDiscoverer = new SmartDiscoverer(prefix);
    }

    @Test
    public void shouldDiscoverResourceByRequestTypePlusSlashToDotStrategy() {
        assertTrue(smartDiscoverer.discoverResource(Method.GET, "/foo/bar.xml").getPath().endsWith(prefix + "/get.foo.bar.xml"));
    }

    @Test
    public void shouldFallbackToRequestTypePlusSlashToSlashStrategy() {
        assertTrue(smartDiscoverer.discoverResource(Method.GET, "/foo/bar1.xml").getPath().endsWith(prefix + "/get/foo/bar1.xml"));
    }

    @Test
    public void shouldFallbackToSlashToDotStrategy() {
        assertTrue(smartDiscoverer.discoverResource(Method.DELETE, "/foo/bar2.xml").getPath().endsWith(prefix + "/foo.bar2.xml"));
    }

    @Test
    public void shouldFallbackToSlashToSlashStrategy() {
        assertTrue(smartDiscoverer.discoverResource(Method.POST, "/foo/bar3.xml").getPath().endsWith(prefix + "/foo/bar3.xml"));
    }

    @Test
    public void shouldFallbackToSlashToSlashXmlExtensionStrategy() {
        assertTrue(smartDiscoverer.discoverResource(Method.POST, "/foo/bar3").getPath().endsWith(prefix + "/foo/bar3.xml"));
    }

    @Test
    public void shouldFallbackToSlashToDotJsonExtensionStrategy() {
        assertTrue(smartDiscoverer.discoverResource(Method.POST, "/foo/bar5").getPath().endsWith(prefix + "/foo.bar5.json"));
    }

    @Test
    public void shouldWorkIfPathContainsSpaces() {
        String path = smartDiscoverer.discoverResource(Method.POST, "/spaces%20here/1.xml").getPath();
        assertTrue(path + " does not ends as expected", path.endsWith(prefix + "/spaces%20here/1.xml"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldIgnoreFolders() {
        smartDiscoverer.discoverResource(Method.GET, "/foo").getPath();
    }
}
