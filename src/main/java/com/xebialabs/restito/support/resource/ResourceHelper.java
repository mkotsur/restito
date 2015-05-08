package com.xebialabs.restito.support.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ResourceHelper {

    public static byte[] getBytes(URL url) throws IOException {
        try (InputStream inputStream = url.openStream()) {
            return getBytes(inputStream);
        }
    }

    public static byte[] getBytes(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int read;
        byte[] data = new byte[4096];

        while ((read = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, read);
        }

        buffer.flush();
        return buffer.toByteArray();
    }

}
