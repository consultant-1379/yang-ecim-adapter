/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */
package com.ericsson.ranexplorer.yangecimadapter.server.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.ericsson.ranexplorer.yangecimadapter.server.exceptions.FileReadException;

public class FileReader {

    private FileReader() {

    }

    public static String getXmlFromFile(final String fileLocation, final String... values) {
        try {
            final URI fileLocationURI = FileReader.class.getClassLoader().getResource(fileLocation).toURI();
            final List<String> lines = Files.readAllLines(Paths.get(fileLocationURI));
            final StringBuilder builder = new StringBuilder();
            lines.forEach(line -> builder.append(line.replaceAll("[\n\t]", "")));
            return addValues(builder.toString(), values);
        } catch (IOException | URISyntaxException exception) {
            throw new FileReadException(String.format("Failed to read file %s", fileLocation), exception);
        }
    }

    private static String addValues(final String inputString, final String... values) {
        if (values != null) {
            return String.format(inputString, (Object[]) values);
        }
        return inputString;
    }
}
