package com.xebialabs.restito.builder.stub;

import org.junit.Test;

import com.xebialabs.restito.server.StubServer;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class StubHttpTest {

    @Test
    public void shouldReturnProperChainObject() {
        assertNotNull(StubHttp.whenHttp(mock(StubServer.class)));
    }
}
