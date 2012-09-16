package com.xebialabs.restito.resources;

import java.net.URL;
import java.util.List;
import org.glassfish.grizzly.http.Method;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;

import static java.lang.String.format;

public class SmartDiscoverer {

    private String resourcePrefix;

    public SmartDiscoverer(final String resourcePrefix) {
        this.resourcePrefix = resourcePrefix;
    }

    /**
     * Discovers resource based on request
     * Tries different options:
     * 1. GET asd/bsd/asd.ext => resource: {resourcePrefix}/get.asd.bsd.asd.ext
     * 2. GET asd/bsd/asd.ext => resource: {resourcePrefix}/get/asd/bsd/asd.ext
     * 3. GET asd/bsd/asd.ext => resource: {resourcePrefix}/asd.bsd.asd.ext
     * 4. GET asd/bsd/asd.ext => resource: {resourcePrefix}/asd/bsd/asd.ext
     */
    public URL discoverResource(Method m, String uri) {
        for (String s : possibleLocations(m, uri)) {
            try {
                return Resources.getResource(resourcePrefix + "/" + s);
            } catch (IllegalArgumentException e) {
                // just go on
            }
        }

        throw new IllegalArgumentException(format("Can not discover resource for method [%s] and URI [%s]", m, uri));
    }

    private List<String> possibleLocations(Method m, String uri) {

        Iterable<String> split = Splitter.on("/").omitEmptyStrings().split(uri);

        return Lists.newArrayList(
                m.toString().toLowerCase() + "." + Joiner.on(".").join(split),
                m.toString().toLowerCase() + "/" + Joiner.on("/").join(split),
                Joiner.on(".").join(split),
                Joiner.on("/").join(split)
        );
    }
}
