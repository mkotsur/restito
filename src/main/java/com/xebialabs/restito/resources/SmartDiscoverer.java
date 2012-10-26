package com.xebialabs.restito.resources;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import org.glassfish.grizzly.http.Method;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;

import static java.lang.String.format;

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
     * <li>GET asd/bsd/asd => resource: {resourcePrefix}/get.asd.bsd.asd</li>
     * <li>GET asd/bsd/asd => resource: {resourcePrefix}/get/asd/bsd/asd</li>
     * <li>GET asd/bsd/asd => resource: {resourcePrefix}/asd.bsd.asd</li>
     * <li>GET asd/bsd/asd => resource: {resourcePrefix}/asd/bsd/asd</li>
     * <li>GET asd/bsd/asd => resource: {resourcePrefix}/get.asd.bsd.asd.xml</li>
     * <li>GET asd/bsd/asd => resource: {resourcePrefix}/get/asd/bsd/asd.xml</li>
     * <li>GET asd/bsd/asd => resource: {resourcePrefix}/asd.bsd.asd.xml</li>
     * <li>GET asd/bsd/asd => resource: {resourcePrefix}/asd/bsd/asd.xml</li>
     * <li>GET asd/bsd/asd => resource: {resourcePrefix}/get.asd.bsd.asd.json</li>
     * <li>GET asd/bsd/asd => resource: {resourcePrefix}/get/asd/bsd/asd.json</li>
     * <li>GET asd/bsd/asd => resource: {resourcePrefix}/asd.bsd.asd.json</li>
     * <li>GET asd/bsd/asd => resource: {resourcePrefix}/asd/bsd/asd.json</li>
     * </ul>
     */
    public URL discoverResource(Method m, String uri) {
        for (String s : possibleLocations(m, uri)) {
            try {
                URL resource = Resources.getResource(resourcePrefix + "/" + URLDecoder.decode(s, "UTF-8"));
                if (!new File(URLDecoder.decode(resource.getFile(), "UTF-8")).isFile()) {
                    continue; // Probably directory
                }
                return resource;
            } catch (IllegalArgumentException ignored) {
            } // just go on
            catch (UnsupportedEncodingException ignored) {
            } // just go on
        }

        throw new IllegalArgumentException(format("Can not discover resource for method [%s] and URI [%s]", m, uri));
    }

    private List<String> possibleLocations(Method m, String uri) {

        Iterable<String> split = Splitter.on("/").omitEmptyStrings().split(uri);

        return Lists.newArrayList(
                m.toString().toLowerCase() + "." + Joiner.on(".").join(split),
                m.toString().toLowerCase() + "/" + Joiner.on("/").join(split),
                Joiner.on(".").join(split),
                Joiner.on("/").join(split),

                m.toString().toLowerCase() + "." + Joiner.on(".").join(split) + ".xml",
                m.toString().toLowerCase() + "/" + Joiner.on("/").join(split) + ".xml",
                Joiner.on(".").join(split) + ".xml",
                Joiner.on("/").join(split) + ".xml",
                m.toString().toLowerCase() + "." + Joiner.on(".").join(split) + ".json",
                m.toString().toLowerCase() + "/" + Joiner.on("/").join(split) + ".json",
                Joiner.on(".").join(split) + ".json",
                Joiner.on("/").join(split) + ".json"
        );
    }
}
