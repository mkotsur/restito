package com.xebialabs.restito.builder;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.io.Resources;
import com.xebialabs.restito.semantics.Call;
import com.xebialabs.restito.semantics.Stub;
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

	@Deprecated
	public StubBuilder withUri(final String uri) {
		withPredicate(new Predicate<Call>() {
			public boolean apply(Call input) {
				return input.getUri().endsWith(uri);
			}
		});
		return this;
	}


	@Deprecated
	public StubBuilder withMethod(final Method m) {
		withPredicate(new Predicate<Call>() {
			public boolean apply(Call input) {
				return m.equals(input.getMethod());
			}
		});
		return this;
	}

	@Deprecated
	public StubBuilder withParameter(final String parameterName, final String... parameterValues) {

		return withPredicate(new Predicate<Call>() {
			public boolean apply(Call input) {
				return Arrays.equals(input.getParameters().get(parameterName), parameterValues);
			}
		});
	}

	@Deprecated
	public StubBuilder withPredicate(final Predicate<Call> newPredicate) {
		final Predicate<Call> currentPredicate = this.stub.getWhen();
		this.stub.setWhen(new Predicate<Call>() {
			public boolean apply(Call input) {
				return newPredicate.apply(input) && currentPredicate.apply(input);
			}
		});
		return this;
	}

	public Stub build() {
		return stub;
	}
}
