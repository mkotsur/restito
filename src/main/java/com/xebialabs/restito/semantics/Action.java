package com.xebialabs.restito.semantics;

import com.google.common.base.Function;
import com.google.common.io.Resources;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Action is a modifier for Response
 *
 * @see org.glassfish.grizzly.http.server.Response
 */
public class Action {

	private Function<Response, Response> function;

	public Action(Function<Response, Response> function) {
		this.function = function;
	}

	public Function<Response, Response> getFunction() {
		return function;
	}

	public Response apply(Response r) {
		return this.function.apply(r);
	}

	// Factory methods
	public static Action success() {
		return status(HttpStatus.OK_200);
	}

	public static Action status(final HttpStatus status) {
		return new Action(new Function<Response, Response>() {
			public Response apply(Response input) {
				input.setStatus(status);
				return input;
			}
		});
	}

	public static Action forXmlResourceContent(String resourcePath) {
		try {
			String asd = Resources.toString(Resources.getResource(resourcePath), Charset.defaultCharset());
			return forStringContent(asd);
		} catch (IOException e) {
			throw new RuntimeException("Can not read resource for restito stubbing.");
		}
	}

	public static Action forStringContent(final String content) {
		return new Action(new Function<Response, Response>() {
			public Response apply(Response input) {
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
	}

	public static Action header(final String key, final String value) {
		return new Action(new Function<Response, Response>() {
			@Override
			public Response apply(Response input) {
				input.setHeader(key, value);
				return input;
			}
		});
	}

	public static Action custom(Function<Response, Response> f) {
		return new Action(f);
	}

	/**
	 * Creates a composite action which contains all passed actions and
	 * executes them in the same order.
	 */
	public static Action composite(final Action... actions) {
		return new Action(new Function<Response, Response>() {
			@Override
			public Response apply(Response input) {
				for (Action action : actions) {
					action.apply(input);
				}

				return input;
			}
		});
	}
}
