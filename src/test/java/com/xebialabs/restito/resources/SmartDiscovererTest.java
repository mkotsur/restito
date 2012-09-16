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
}
