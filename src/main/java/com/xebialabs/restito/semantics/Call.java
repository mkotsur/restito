package com.xebialabs.restito.semantics;

import com.google.common.collect.Maps;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.Request;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Call that happened to server.
 * Handy wrapper for Request values.
 */
public class Call {

	private Method method;
	private String uri;
	private String contentType;
	private String postBody;
	private String url;
	private Map<String, String> headers = Maps.newHashMap();
	private Map<String, String[]> parameters = Maps.newHashMap();

	private Call() {}

	public static Call fromRequest(Request request) {
		Call call = new Call();

		call.method = request.getMethod();
		call.uri = request.getRequestURI();
		call.contentType = request.getContentType();
		call.url = request.getRequestURL().toString();

		for (String s : request.getHeaderNames()) {
			call.headers.put(s, request.getHeader(s));
		}

		call.parameters = new HashMap<String, String[]>(request.getParameterMap());

		try {
			call.postBody = request.getPostBody(999999).toStringContent(Charset.defaultCharset());
		} catch (IOException e) {
			throw new RuntimeException("Problem reading Post Body of HTTP call");
		}

		return call;
	}

	public String getUri() {
		return uri;
	}

	public String getContentType() {
		return contentType;
	}

	public String getUrl() {
		return url;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public Method getMethod() {
		return method;
	}

	public Map<String, String[]> getParameters() {
		return parameters;
	}

	public String getPostBody() {
		return postBody;
	}
}


