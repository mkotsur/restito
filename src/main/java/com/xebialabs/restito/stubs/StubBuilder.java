package com.xebialabs.restito.stubs;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.io.Resources;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class StubBuilder {

	private Stub stub;

	public StubBuilder() {
		this.stub = new Stub();
	}

	public StubBuilder forSuccess() {
		return forStatus(HttpStatus.OK_200);
	}

	public StubBuilder forStatus(final HttpStatus status) {
		this.stub.setWhat(new Function<Response, Response>() {
			public Response apply(@Nullable Response input) {
				input.setStatus(status);
				return input;
			}
		});
		return this;
	}

	public StubBuilder forXmlResourceContent(String resourcePath) {
		try {
			String asd = Resources.toString(Resources.getResource(resourcePath), Charset.defaultCharset());
			return forStringContent(asd);
		} catch (IOException e) {
			throw new RuntimeException("Can not read resource for restito stubbing.");
		}
	}

	public StubBuilder forStringContent(final String content) {
		this.stub.setWhat(new Function<Response, Response>() {
			public Response apply(@Nullable Response input) {
				input.setContentType("application/xml");
				input.setContentLength(content.length());
				try {
					input.getWriter().write(content);
				} catch (IOException e) {
					throw new RuntimeException("Can not write resource content for restito stubbing.");
				}
				return input;
			}
		});


		return this;
	}

	public StubBuilder withUri(final String uri) {
		withPredicate(new Predicate<Request>() {
			public boolean apply(Request input) {
				return input.getRequestURI().endsWith(uri);
			}
		});
		return this;
	}


	public StubBuilder withMethod(final Method m) {
		withPredicate(new Predicate<Request>() {
			public boolean apply(Request input) {
				return m.equals(input.getMethod());
			}
		});
		return this;
	}

	public StubBuilder withParameter(final String parameterName, final String... parameterValues) {

		return withPredicate(new Predicate<Request>() {
			public boolean apply(Request input) {
				return Arrays.equals(input.getParameterValues(parameterName), parameterValues);
			}
		});
	}

	public StubBuilder withPredicate(final Predicate<Request> newPredicate) {

		final Predicate<Request> currentPredicate = this.stub.getWhen();

		this.stub.setWhen(new Predicate<Request>() {
			public boolean apply(Request input) {
				return newPredicate.apply(input) && currentPredicate.apply(input);
			}
		});

		return this;
	}

	public Stub build() {
		return stub;
	}
}
