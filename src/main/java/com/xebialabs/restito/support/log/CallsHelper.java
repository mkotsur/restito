package com.xebialabs.restito.support.log;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.restito.semantics.Call;
import com.xebialabs.restito.server.StubServer;

public class CallsHelper {

    private static Logger log = LoggerFactory.getLogger(StubServer.class);

    public static void logCalls(List<Call> calls) {

        log.info(String.format("There are %s entries recorded: ", calls.size()));

        for (Call call : calls) {
            logCall(call);
        }

    }

    public static void logCall(Call call) {
        log.info("{} Request to URL: {} of type {}", va(call.getMethod(), call.getUrl(), call.getContentType()));
        for (Map.Entry<String, String> e : call.getHeaders().entrySet()) {
            log.info(" --> Header [{}] with value: [{}]", e.getKey(), e.getValue());
        }

        for (Map.Entry<String, String[]> e : call.getParameters().entrySet()) {
            log.info(" --> Parameter [{}] with value(s): ", e.getKey());
            for (String v : e.getValue()) {
                log.info("      -> [{}]", v);
            }
        }

        // Prevents IntelliJ from crash :-)
        if (call.getContentType() == null || !call.getContentType().startsWith("multipart/form-data")) {
            log.info(" --> Body: ");
            log.info(call.getPostBody());
        }
    }

    private static Object[] va(Object... args) {
        return args;
    }
}
