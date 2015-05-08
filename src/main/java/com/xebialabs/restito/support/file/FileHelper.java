package com.xebialabs.restito.support.file;

import java.io.File;

public class FileHelper {

    public static String getFileExtension(String path) {
        String name = new File(path).getName();
        int lastDotIndex = name.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : name.substring(lastDotIndex + 1);
    }

}
