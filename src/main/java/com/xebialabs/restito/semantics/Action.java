package com.xebialabs.restito.semantics;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import javax.ws.rs.core.MediaType;
import org.glassfish.grizzly.http.HttpResponsePacket;
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
     * Sets HTTP status 200 to response.
     * Consider using {@link #ok()} as more concise and less ambiguous option.
     * @see {@link #ok()}
     */
    public static Action success() {
        return status(HttpStatus.OK_200);
    }

    /**
     * Sets HTTP status 200 to response
     */
    public static Action ok() {
        return status(HttpStatus.OK_200);
    }

    /**
     * Sets HTTP status 204 to response
     */
    public static Action noContent() {
        return status(HttpStatus.NO_CONTENT_204);
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
     * <li>.xml => application/xml</li>
     * <li>.json => application/xml</li>
     * </ul>
     */
    public static Action resourceContent(final String resourcePath) {
        return new Action(new Function<Response, Response>() {
            @Override
            public Response apply(Response input) {
                final HttpResponsePacket responsePacket = input.getResponse();
                String encoding = responsePacket == null ? input.getCharacterEncoding() : responsePacket.getCharacterEncoding();
                return resourceContent(resourcePath, encoding).apply(input);
            }
        });
    }

    /**
     * @deprecated Use {@link #resourceContent(String)} and specify charset using {@link #charset(String)}
     */
    @Deprecated
    public static Action resourceContent(String resourcePath, String charset) {
        return resourceContent(Resources.getResource(resourcePath), charset == null ? null : Charset.forName(charset));
    }

    /**
     * Writes content using the specified encoding and content-type of resource file to response.
     * Tries to detect content type based on file extension. If can not detect => content-type is not set.
     * For now there are following bindings:
     * <ul>
     * <li>.xml => application/xml</li>
     * <li>.json => application/xml</li>
     * </ul>
     */
    public static Action resourceContent(URL resourceUrl) {
        return resourceContent(resourceUrl, null);
    }

    /**
     * @deprecated Use {@link #resourceContent(java.net.URL)} and specify charset using {@link #charset(java.nio.charset.Charset)}
     */
    @Deprecated
    public static Action resourceContent(URL resourceUrl, Charset charset) {
        try {
            final byte[] bytes = Resources.toByteArray(resourceUrl);

            String fileExtension = Files.getFileExtension(resourceUrl.getPath());

            final Action charsetAction = charset != null ? charset(charset) : Action.noop();
            final Action contentTypeAction;

            if (fileExtension.equalsIgnoreCase("xml")) {
                contentTypeAction = composite(charsetAction, contentType("application/xml"));
            } else if (fileExtension.equalsIgnoreCase("json")) {
                contentTypeAction = composite(charsetAction, contentType("application/json"));
            } else {
                contentTypeAction = charsetAction;
            }

            return composite(contentTypeAction, bytesContent(bytes));

        } catch (IOException e) {
            throw new RuntimeException("Can not read resource for restito stubbing.");
        }
    }

    /**
     * Writes bytes content to response
     */
    public static Action bytesContent(final byte[] content) {
        return new Action(new Function<Response, Response>() {
            public Response apply(Response response) {

                response.setContentLength(content.length);
                try {
                    response.getOutputStream().write(content);
                } catch (IOException e) {
                    throw new RuntimeException("Can not write resource content for restito stubbing.");
                }
                return response;
            }
        });
    }

    /**
     * Writes string content to response
     */
    public static Action stringContent(final String content) {
        return bytesContent(content.getBytes());
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
    public static Action contentType(final MediaType contentType) {
        return contentType(contentType.toString());
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
     * Sets charset of the response (must come before stringContent/resourceContent Action)
     */
    public static Action charset(final String charset) {
        return new Action(new Function<Response, Response>() {
            @Override
            public Response apply(final Response r) {
                r.setCharacterEncoding(charset);
                return r;
            }
        });
    }

    /**
     * Sets charset of the response (must come before stringContent/resourceContent Action)
     */
    public static Action charset(final Charset charset) {
        return charset(charset.name());
    }

    /**
     * Returns unauthorized response with default realm name
     */
    public static Action unauthorized() {
        return unauthorized("Restito realm");
    }

    /**
     * Returns unauthorized response
     */
    public static Action unauthorized(final String realm) {
        return new Action(new Function<Response, Response>() {
            @Override
            public Response apply(Response r) {
                r.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
                r.setStatus(HttpStatus.UNAUTHORIZED_401);
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

    /**
     * Creates a composite action which contains all passed actions and
     * executes them in the same order.
     */
    public static Action composite(final Collection<Applicable> applicables) {
        return new Action(new Function<Response, Response>() {
            @Override
            public Response apply(Response input) {
                for (Applicable action : applicables) {
                    action.apply(input);
                }

                return input;
            }
        });
    }

    /**
     * Creates a composite action which contains all passed actions and
     * executes them in the same order.
     */
    public static Action composite(final Action... actions) {
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

    /**
     * Doing nothing. To be used in DSLs for nicer syntax.
     */
    public static Action noop() {
        return new Action(Functions.<Response>identity());
    }
}
