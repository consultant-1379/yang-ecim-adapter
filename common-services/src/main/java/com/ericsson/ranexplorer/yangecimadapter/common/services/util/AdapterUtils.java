/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.common.services.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class AdapterUtils {
    private AdapterUtils(){
        //just to satisfy sonar...
    }

    public static String readFileToString(String fileName) throws IOException {
        InputStream inputStream = AdapterUtils.class.getResourceAsStream(fileName);
        return IOUtils.toString(inputStream, Charset.defaultCharset());
    }

    public static String removeWhitespaceBetweenTags(final String xmlString) {
        final char[] result = new char[xmlString.length()];
        final char[] input = xmlString.toCharArray();
        boolean keep = false;
        int j = 0;
        for (int i = 0; i < input.length; i++) {
            if (input[i] == '<') {
                keep = true;
            } else if (input[i] == '>') {
                keep = false;
            }

            if (isNotWhiteSpace(input[i]) || keep) {
                result[j] = input[i];
                j++;
            }
        }
        return String.valueOf(result).trim();
    }

    private static boolean isNotWhiteSpace(final char ch) {
        return !Character.isWhitespace(ch);
    }
}
