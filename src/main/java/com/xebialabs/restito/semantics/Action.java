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

	private Action(Function<Response, Response> function) {
		this.function = function;
	}

	/**
	 * Get function which represents the action
	 */
	public Function<Response, Response> getFunction() {
		return function;
	}

	/**
	 * Perform the action with response
	 */
	public Response apply(Response r) {
		return this.function.apply(r);
	}

	// Factory methods

	/**
	 * Sets HTTP status 200 to response
	 */
	public static Action success() {
		return status(HttpStatus.OK_200);
	}

	/**
	 * Sets HTTP status to response
	 */
	public static Action status(final HttpStatus status) {
		return new Action(new Function<Response, Response>() {
			public Response apply(Response input) {
				input.setStatus(status);
				return input;
			}
		});
	}

	/**
	 * Writes content of resource file to response
	 */
	public static Action resourceContent(String resourcePath) {
		try {
			String asd = Resources.toString(Resources.getResource(resourcePath), Charset.defaultCharset());
			return stringContent(asd);
		} catch (IOException e) {
			throw new RuntimeException("Can not read resource for restito stubbing.");
		}
	}

	/**
	 * Writes string content to response
	 */
	public static Action stringContent(final String content) {
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

	/**
	 * Sets key-value header on response
	 */
	public static Action header(final String key, final String value) {
		return new Action(new Function<Response, Response>() {
			@Override
			public Response apply(Response input) {
				input.setHeader(key, value);
				return input;
			}
		});
	}

	/**
	 * Perform set of custom actions on response
	 */
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
