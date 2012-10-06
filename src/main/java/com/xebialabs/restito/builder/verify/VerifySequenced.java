package com.xebialabs.restito.builder.verify;

import com.xebialabs.restito.semantics.Call;

import java.util.List;

import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;

/**
 * Responsible for providing ability to have sequenced verifications
 */
public class VerifySequenced {

	private List<Call> calls;

    VerifySequenced(List<Call> calls) {
		this.calls = calls;
	}

    /**
     * Returns next round of verification
     */
	public VerifyHttp then() {
		return verifyHttp(calls);
	}
}
