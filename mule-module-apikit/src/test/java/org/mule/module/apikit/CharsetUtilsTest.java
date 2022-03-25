/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import junit.framework.TestCase;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.charset.Charset;
import java.util.Arrays;

import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CharsetUtils.class })
public class CharsetUtilsTest extends TestCase {
    /** Hello ASCII encoded, also Hello UTF-8 encoded */
    static final byte[] HELLO_ASCII = {'H', 'e', 'l', 'l', 'o'};
    /** Hello UTF-16 Little Endian encoded */
    static final byte[] HELLO_UTF_16_LE = {-1, -2, 'H', 0, 'e', 0, 'l', 0, 'l', 0, 'o', 0};
    /** Hello UTF-16 Big Endian encoded */
    static final byte[] HELLO_UTF_16_BE = {-2, -1, 0, 'H', 0, 'e', 0, 'l', 0, 'l', 0, 'o'};
    /** Привет UTF-8 encoded */
    static final byte[] HELLO_RU_UTF_8 = {-48, -97, -47, -128, -48, -72, -48, -78, -48, -75, -47, -126};
    /** Привет IBM866 encoded */
    static final byte[] HELLO_RU_CP866 = {-113, -32, -88, -94, -91, -30};
    /** {"TruckCode": "川112345"} UTF-8 encoded, also {"TruckCode": "тиЮ112345"} CP855 encoded */
    static final byte[] UNLUCKY_PLATE_NUMBER = {123, 34, 84, 114, 117, 99, 107, 67, 111, 100, 101, 34, 58, 32, 34, -27, -73, -99, 49, 49, 50, 51, 52, 53, 34, 125};

    static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
    static final Charset LATIN1_CHARSET = Charset.forName("ISO-8859-1");

    String apikitDisableEncodingGuessingValue;
    @Override
    protected void setUp() {
        apikitDisableEncodingGuessingValue = System.getProperty("apikit.disableEncodingGuessing");
    }

    @Override
    protected void tearDown() throws Exception {
        if (apikitDisableEncodingGuessingValue == null)
            System.clearProperty("apikit.disableEncodingGuessing");
        else
            System.setProperty("apikit.disableEncodingGuessing", apikitDisableEncodingGuessingValue);
    }

    public void testGuessingShouldWorkForASCII() {
        System.setProperty("apikit.disableEncodingGuessing", "false");
        // Both of these are valid encodings for the message
        assertTrue(Arrays.asList("ASCII", "UTF-8").contains(CharsetUtils.detectEncodingOrDefault(HELLO_ASCII)));
    }

    public void testGuessingShouldWorkForUTF16() {
        System.setProperty("apikit.disableEncodingGuessing", "false");
        assertEquals("UTF-16LE", CharsetUtils.detectEncodingOrDefault(HELLO_UTF_16_LE));
        assertEquals("UTF-16BE", CharsetUtils.detectEncodingOrDefault(HELLO_UTF_16_BE));
    }

    public void testGuessingShouldWorkForRussian() {
        System.setProperty("apikit.disableEncodingGuessing", "false");
        assertEquals("UTF-8", CharsetUtils.detectEncodingOrDefault(HELLO_RU_UTF_8));
        assertEquals("IBM866", CharsetUtils.detectEncodingOrDefault(HELLO_RU_CP866));
    }

    public void testGuessingSometimesFails() {
        System.setProperty("apikit.disableEncodingGuessing", "false");
        assertEquals("IBM855", CharsetUtils.detectEncodingOrDefault(UNLUCKY_PLATE_NUMBER));
    }

    public void testGuessingIsEnabledByDefault() {
        System.clearProperty("apikit.disableEncodingGuessing");
        assertEquals("IBM855", CharsetUtils.detectEncodingOrDefault(UNLUCKY_PLATE_NUMBER));
    }

    public void testGuessingCanBeDisabled() {
        System.setProperty("apikit.disableEncodingGuessing", "true");
        PowerMockito.mockStatic(Charset.class);

        when(Charset.defaultCharset()).thenReturn(UTF_8_CHARSET);
        assertEquals("UTF-8", CharsetUtils.detectEncodingOrDefault(UNLUCKY_PLATE_NUMBER));

        when(Charset.defaultCharset()).thenReturn(LATIN1_CHARSET);
        assertEquals("ISO-8859-1", CharsetUtils.detectEncodingOrDefault(UNLUCKY_PLATE_NUMBER));
    }
}
