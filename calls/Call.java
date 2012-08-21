package com.xebialabs.restito.calls;

import com.google.common.collect.Maps;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.Request;

import java.util.Map;

/**
 * Call that happened to server
 */
public class Call {

	private Method method;
	private String uri;
	private String contentType;
	private String url;
	private Map<String, String> headers = Maps.newHashMap();

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
}
