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
import java.net.URL;
import java.nio.charset.Charset;

public class StubBuilder {

	private Stub stub;

	public StubBuilder() {
		this.stub = new Stub();
	}

	public StubBuilder withMethod(final Method m) {
		this.stub.setWhen(new Predicate<Request>() {
			@Override
			public boolean apply(@Nullable Request input) {
				return m.equals(input.getMethod());
			}
		});
		return this;
	}


	public StubBuilder forSuccess() {
		this.stub.setWhat(new Function<Response, Response>() {
			@Override
			public Response apply(@Nullable Response input) {
				input.setStatus(HttpStatus.OK_200);
				return input;
			}
		});
		return this;
	}

	public StubBuilder withXmlResourceContent(String resourcePath) {
		try {
			String asd = Resources.toString(Resources.getResource(resourcePath), Charset.defaultCharset());
			return withXmlContent(asd);
		} catch (IOException e) {
			throw new RuntimeException("Can not read resource for restito stubbing.");
		}
	}

	private StubBuilder withXmlContent(final String content) {
		this.stub.setWhat(new Function<Response, Response>() {
			@Override
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
		stub.setWhen(new Predicate<Request>() {
			@Override
			public boolean apply(@Nullable Request input) {
				return input.getRequestURI().endsWith(uri);
			}
		});
		return this;
	}

	public Stub build() {
		return stub;
	}
}
