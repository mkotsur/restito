package com.xebialabs.restito.examples;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import com.google.common.base.Joiner;
import com.google.gson.Gson;

import static com.google.common.collect.Lists.newArrayList;
import static java.net.URLEncoder.encode;

public class WikiClient {

    private String entryPoint;

    public WikiClient(final String entryPoint) {
        this.entryPoint = entryPoint;
    }

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Returns the latest revision of the page which was edited last
     */
    public PageRevision getMostRecentRevision(String... titles) throws Exception {

        URL url = new URL(entryPoint +
                "/w/api.php?format=json&action=query&prop=revisions&rvprop=user|timestamp&titles=" +
                encode(Joiner.on("|").join(titles), "UTF-8")
        );

        String response = readURL(url);

        List<PageRevision> revisions = extractRevisions(response);

        Collections.sort(revisions, new Comparator<PageRevision>() {
            @Override
            public int compare(final PageRevision o1, final PageRevision o2) {
                return o2.date.compareTo(o1.date);
            }
        });

        return revisions.get(0);
    }

    private List<PageRevision> extractRevisions(final String response) throws ParseException {
        Map<String, Map<String, Map<String, Map<String, Object>>>> result = new Gson().fromJson(response, Map.class);

        List<PageRevision> revisions = newArrayList();

        for (Map<String, Object> p : result.get("query").get("pages").values()) {
            Map<String, Object> lastRevision = ((List<Map<String, Object>>)p.get("revisions")).get(0);

            revisions.add(new PageRevision(
                    (String)p.get("title"),
                    (String)lastRevision.get("user"),
                    DATE_FORMAT.parse((String) lastRevision.get("timestamp")))
            );
        }
        return revisions;
    }

    private String readURL(URL url) throws IOException {
        URLConnection conn = url.openConnection();
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        StringWriter stringWriter = new StringWriter();

        BufferedWriter bw = new BufferedWriter(stringWriter);

        String inputLine;
        while ((inputLine = br.readLine()) != null) {
            bw.write(inputLine);
        }

        bw.close();
        br.close();

        return stringWriter.toString();
    }
}
