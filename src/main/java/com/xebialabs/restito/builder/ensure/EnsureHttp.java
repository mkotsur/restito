package com.xebialabs.restito.builder.ensure;

import java.util.List;

import com.xebialabs.restito.semantics.Stub;
import com.xebialabs.restito.server.StubServer;

import static java.lang.String.format;
/**
 * <p><u><b>!EXPERIMENTAL!</b> This stuff is experimental. Which means it may change significantly in future versions.</u></p>
 * <p>Responsible for validating expected stubs</p>
 */
public class EnsureHttp {

    private List<Stub> stubs;

    private EnsureHttp(List<Stub> stubs) {
        this.stubs = stubs;
    }

    /**
     * Static factory to instantiate the class
     */
    public static EnsureHttp ensureHttp(final StubServer stubServer) {
        return new EnsureHttp(stubServer.getStubs());
    }

    public void gotStubsCommitmentsDone() {
        for (Stub stub : stubs) {
            if (stub.getExpectedTimes() == stub.getAppliedTimes()) {
                continue;
            }
            throw new AssertionError(
                format("Expected stub %s to be called %s times, called %s times instead", stub.toString(), stub.getExpectedTimes(), stub.getAppliedTimes())
            );
        }

    }
}
