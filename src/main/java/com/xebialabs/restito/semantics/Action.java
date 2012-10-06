package com.xebialabs.restito.semantics;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.io.Files;
import com.google.common.io.Resources;

/**
 * Action is a modifier for Response
 *
 * @see org.glassfish.grizzly.http.server.Response
 */
public class Action implements Applicable {

    private Function<Response, Response> function;

    private Action(Function<Response, Response> function) {
        this.function = function;
    }

    /**
     * Perform the action with response
     */
    @Override
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
     * Writes content and content-type of resource file to response.
     * Tries to detect content type based on file extension. If can not detect => content-type is not set.
     * For now there are following bindings:
     * <ul>
     *     <li>.xml => application/xml</li>
     *      <li>.json => application/xml</li>
     * </ul>
     *
     */
    public static Action resourceContent(String resourcePath) {
        return resourceContent(Resources.getResource(resourcePath));
    }

    /**
     * Does the same as Action.resourceContent(), the only difference is that it accepts an URL instead of resource path.
     */
    public static Action resourceContent(URL resourceUrl) {
        try {
            final String resourceContent = Resources.toString(resourceUrl, Charset.defaultCharset());

            Action contentTypeAction = custom(Functions.<Response>identity());

            String fileExtension = Files.getFileExtension(resourceUrl.getPath());

            if (fileExtension.equalsIgnoreCase("xml")) {
                contentTypeAction = contentType("application/xml");
            } else if (fileExtension.equalsIgnoreCase("json")) {
                contentTypeAction = contentType("application/json");
            }

            return composite(contentTypeAction, stringContent(resourceContent));

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
     * Sets content type to the response
     */
    public static Action contentType(final String contentType) {
        return new Action(new Function<Response, Response>() {
            @Override
            public Response apply(final Response r) {
                r.setContentType(contentType);
                return r;
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
    public static Action composite(final Applicable... actions) {
        return new Action(new Function<Response, Response>() {
            @Override
            public Response apply(Response input) {
                for (Applicable action : actions) {
                    action.apply(input);
                }

                return input;
            }
        });
    }
}
