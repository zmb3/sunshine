package com.zmb.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by zberg_000 on 8/9/2014.
 */
public class IoUtils {
    private IoUtils() { }

    /**
     * Reads all input from an input stream.
     * Closes the stream when done.
     * @param input
     * @return
     * @throws IOException
     */
    public static String readAll(InputStream input) throws IOException {
        try {
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            String line;
            while ((line = reader.readLine()) != null) {
                // readLine() consumes the newline, so we restore it here
                buffer.append(line).append("\n");
            }
            return buffer.toString();
        } finally {
            input.close();
        }
    }
}
