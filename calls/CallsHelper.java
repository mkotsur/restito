package com.xebialabs.restito.calls;

import com.google.common.base.Optional;
import com.xebialabs.restito.StubServer;
import org.glassfish.grizzly.http.server.Request;
import org.slf4j.LoggerFactory;
import sun.rmi.runtime.Log;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

public class CallsHelper {

	private static Logger log = LoggerFactory.getLogger(StubServer.class);

	public static void logCalls(List<Call> calls) {

		log.info(
				String.format("There are %s entries recorded: ", calls.size())
		);

		for (Call call : calls) {
			log.info("Request to URL: {}, URI: {} of type {}", va(call.getUrl(), call.getUri(), call.getContentType()));
			for (Map.Entry<String, String> e : call.getHeaders().entrySet()) {
				log.info(" --> Header [{}] with value: [{}]", e.getKey(), e.getValue());
			}
		}

	}

	private static Object[] va(Object... args) {
		return args;
	}
}
