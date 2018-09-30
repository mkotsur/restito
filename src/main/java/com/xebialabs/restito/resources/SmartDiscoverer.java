package com.xebialabs.restito.resources;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.glassfish.grizzly.http.Method;

import java.lang.String;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * <p><u><b>!EXPERIMENTAL!</b> This stuff is experimental. Which means it may change significantly in future versions.</u></p>
 * <p>Responsible for discovering a resource which will be used as a response body. Discovery happens based on URI and and request method.</p>
 */
public class SmartDiscoverer {

    private String resourcePrefix;

    public SmartDiscoverer(final String resourcePrefix) {
        this.resourcePrefix = resourcePrefix;
    }

    /**
     * Discovers resource based on request
     * Tries different options:
     * <ul>
     * <li>GET asd/bsd/asd - resource: {resourcePrefix}/get.asd.bsd.asd</li>
     * <li>GET asd/bsd/asd - resource: {resourcePrefix}/get/asd/bsd/asd</li>
     * <li>GET asd/bsd/asd - resource: {resourcePrefix}/asd.bsd.asd</li>
     * <li>GET asd/bsd/asd - resource: {resourcePrefix}/asd/bsd/asd</li>
     * <li>GET asd/bsd/asd - resource: {resourcePrefix}/get.asd.bsd.asd.xml</li>
     * <li>GET asd/bsd/asd - resource: {resourcePrefix}/get/asd/bsd/asd.xml</li>
     * <li>GET asd/bsd/asd - resource: {resourcePrefix}/asd.bsd.asd.xml</li>
     * <li>GET asd/bsd/asd - resource: {resourcePrefix}/asd/bsd/asd.xml</li>
     * <li>GET asd/bsd/asd - resource: {resourcePrefix}/get.asd.bsd.asd.json</li>
     * <li>GET asd/bsd/asd - resource: {resourcePrefix}/get/asd/bsd/asd.json</li>
     * <li>GET asd/bsd/asd - resource: {resourcePrefix}/asd.bsd.asd.json</li>
     * <li>GET asd/bsd/asd - resource: {resourcePrefix}/asd/bsd/asd.json</li>
     * </ul>
     */
    public URL discoverResource(Method m, String uri) {
        for (String s : possibleLocations(m, uri)) {
            try {
                URL resource = this.getClass().getClassLoader().getResource(resourcePrefix + "/" + URLDecoder.decode(s, UTF_8.name()));
                if (resource == null) {
                    throw new IllegalArgumentException(String.format("Resource %s not found.", uri));
                }
                if (!new File(URLDecoder.decode(resource.getFile(), UTF_8.name())).isFile()) {
                    continue; // Probably directory
                }
                return resource;
            } catch (IllegalArgumentException | UnsupportedEncodingException ignored) {
            } // just go on
        }

        throw new IllegalArgumentException(String.format("Can not discover resource for method [%s] and URI [%s]", m, uri));
    }

    private List<String> possibleLocations(final Method m, String uri) {
        final Iterable<String> split = split(uri, "/");

        return new ArrayList<String>() {{
            add(m.toString().toLowerCase() + "." + join(split, "."));
            add(m.toString().toLowerCase() + "/" + join(split, "/"));
            add(join(split, "."));
            add(join(split, "/"));
            add(m.toString().toLowerCase() + "." + join(split, ".") + ".xml");
            add(m.toString().toLowerCase() + "/" + join(split, "/") + ".xml");
            add(join(split, ".") + ".xml");
            add(join(split, "/") + ".xml");
            add(m.toString().toLowerCase() + "." + join(split, ".") + ".json");
            add(m.toString().toLowerCase() + "/" + join(split, "/") + ".json");
            add(join(split, ".") + ".json");
            add(join(split, "/") + ".json");
        }};
    }

    private Iterable<String> split(String data, String delimiter) {
        List<String> result = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(data, delimiter);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            if (token.length() > 0) {
                result.add(token);
            }
        }
        return result;
    }

    private String join(Iterable<String> elements, String delimiter) {
        Iterator<String> iter = elements.iterator();
        StringBuilder result = new StringBuilder();
        while (iter.hasNext()) {
            result.append(iter.next());
            if (iter.hasNext()) {
                result.append(delimiter);
            }
        }
        return result.toString();
    }
}
