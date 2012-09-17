package com.xebialabs.restito.resources;

import java.io.File;
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
     * 1. GET asd/bsd/asd => resource: {resourcePrefix}/get.asd.bsd.asd
     * 2. GET asd/bsd/asd => resource: {resourcePrefix}/get/asd/bsd/asd
     * 3. GET asd/bsd/asd => resource: {resourcePrefix}/asd.bsd.asd
     * 4. GET asd/bsd/asd => resource: {resourcePrefix}/asd/bsd/asd
     * 5. GET asd/bsd/asd => resource: {resourcePrefix}/get.asd.bsd.asd.xml
     * 6. GET asd/bsd/asd => resource: {resourcePrefix}/get/asd/bsd/asd.xml
     * 7. GET asd/bsd/asd => resource: {resourcePrefix}/asd.bsd.asd.xml
     * 8. GET asd/bsd/asd => resource: {resourcePrefix}/asd/bsd/asd.xml
     * 9. GET asd/bsd/asd => resource: {resourcePrefix}/get.asd.bsd.asd.json
     * 10. GET asd/bsd/asd => resource: {resourcePrefix}/get/asd/bsd/asd.json
     * 11. GET asd/bsd/asd => resource: {resourcePrefix}/asd.bsd.asd.json
     * 12. GET asd/bsd/asd => resource: {resourcePrefix}/asd/bsd/asd.json
     * */
    public URL discoverResource(Method m, String uri) {
        for (String s : possibleLocations(m, uri)) {
            try {
                URL resource = Resources.getResource(resourcePrefix + "/" + s);
                if (!new File(resource.getFile()).isFile()) {
                    continue; // Probably directory
                }
                return resource;
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
                Joiner.on("/").join(split),

                m.toString().toLowerCase() + "." + Joiner.on(".").join(split) + ".xml",
                m.toString().toLowerCase() + "/" + Joiner.on("/").join(split) + ".xml",
                Joiner.on(".").join(split) + ".xml",
                Joiner.on("/").join(split) + ".xml",
                m.toString().toLowerCase() + "." + Joiner.on(".").join(split) + ".json",
                m.toString().toLowerCase() + "/" + Joiner.on("/").join(split)+ ".json",
                Joiner.on(".").join(split) + ".json",
                Joiner.on("/").join(split) + ".json"
        );
    }
}
