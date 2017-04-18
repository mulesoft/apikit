package org.mule.module.apikit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils
{
    protected static Integer bufferSize = 4096;//System.getProperty("mule.streaming.bufferSize") == null? Integer.parseInt(System.getProperty("mule.streaming.bufferSize")): 4096;

    public static long copyLarge(InputStream input, OutputStream output) throws IOException
    {
        byte[] buffer = new byte[bufferSize];
        long count = 0L;

        int n1;
        for(boolean n = false; -1 != (n1 = input.read(buffer)); count += (long)n1) {
            output.write(buffer, 0, n1);
        }

        return count;
    }
}
