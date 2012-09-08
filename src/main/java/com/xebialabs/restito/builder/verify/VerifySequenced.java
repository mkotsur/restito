package com.xebialabs.restito.builder.verify;

import com.xebialabs.restito.semantics.Call;

import java.util.List;

import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;

public class VerifySequenced {

	private List<Call> calls;

	public VerifySequenced(List<Call> calls) {
		this.calls = calls;
	}

	public VerifyHttp then() {
		return verifyHttp(calls);
	}
}
