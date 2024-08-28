/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.exceptions.XmlFileReaderException;

public class DefaultFilterProvider {

    private static final String DEFAULT_ENODEBFUNCTION_FILTER = "filters/enodebfunction-default-filter.xml";
    private static final String DEFAULT_NODESUPPORT_FILTER = "filters/nodesupport-default-filter.xml";
    private static final String DEFAULT_EQUIPMENT_FILTER = "filters/equipment-default-filter.xml";
    private static final Map<FilterBlockName, FilterHolder> DEFAULT_FILTERS = new ConcurrentHashMap<>();
    private static final DefaultFilterProvider INSTANCE = new DefaultFilterProvider();

    static {
        populateFilters(FilterBlockName.ENODEB_FUNCTION, DEFAULT_ENODEBFUNCTION_FILTER);
        populateFilters(FilterBlockName.NODE_SUPPORT, DEFAULT_NODESUPPORT_FILTER);
        populateFilters(FilterBlockName.EQUIPMENT, DEFAULT_EQUIPMENT_FILTER);
    }

    private DefaultFilterProvider() {
    }

    public static DefaultFilterProvider getInstance() {
        return INSTANCE;
    }

    public String getDefaultFilter(final FilterBlockName filterBlockName) {
        FilterHolder filterHolder = DEFAULT_FILTERS.get(filterBlockName);
        return filterHolder != null ? filterHolder.getDefaultFilterAllData() : null;
    }

    public String getDefaultFilterConfigDataOnly(final FilterBlockName filterBlockName) {
        FilterHolder filterHolder = DEFAULT_FILTERS.get(filterBlockName);
        return filterHolder != null ? filterHolder.getDefaultFilterConfigData() : null;
    }

    private static void populateFilters(final FilterBlockName filterBlockName, final String fileLocation) {
        final InputStream inputStream = readFile(fileLocation);
        try {
            populateFiltersFromInputStream(inputStream, filterBlockName);
        } catch (final IOException exception) {
            throw new XmlFileReaderException(exception.getMessage(), exception);
        }

    }

    private static InputStream readFile(final String fileLocation) {
        final InputStream inputStream = DefaultFilterProvider.class.getClassLoader().getResourceAsStream(fileLocation);
        if (inputStream == null) {
            throw new XmlFileReaderException("Failed to locate the file " + fileLocation);
        }
        return inputStream;
    }

    private static void populateFiltersFromInputStream(final InputStream inputStream, final FilterBlockName filterBlockName) throws IOException {
        final StringBuilder stringBuilderDefaultFilter = new StringBuilder();
        final StringBuilder stringBuilderDefaultFilterConfig = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (isConfigData(line)) {
                    addLineTo(stringBuilderDefaultFilter, line);
                    addLineTo(stringBuilderDefaultFilterConfig, line);
                } else {
                    final String lineToAdd = stripOutConfigAttribute(line);
                    addLineTo(stringBuilderDefaultFilter, lineToAdd);
                }
            }
        }
        FilterHolder filterHolder = INSTANCE.new FilterHolder(stringBuilderDefaultFilter.toString(), stringBuilderDefaultFilterConfig.toString());
        DEFAULT_FILTERS.put(filterBlockName, filterHolder);
    }

    private static void addLineTo(final StringBuilder stringBuilder, final String line) {
        stringBuilder.append(line).append("\n");
    }

    private static boolean isConfigData(final String line) {
        return !line.contains("config=\"false\"");
    }

    private static String stripOutConfigAttribute(final String line) {
        final int indexOfStartAngleBracket = line.indexOf('<');
        final int indexOfFirstSpaceBeforeConfigAttribute = line.trim().indexOf(' ');
        final int endIndex = indexOfStartAngleBracket + indexOfFirstSpaceBeforeConfigAttribute;
        return line.substring(0, endIndex) + "/>";
    }

    private class FilterHolder {

        private final String defaultFilterAllData;
        private final String defaultFilterConfigData;

        private FilterHolder(final String defaultFilterAllData, final String defaultFilterConfigData) {
            this.defaultFilterAllData = defaultFilterAllData;
            this.defaultFilterConfigData = defaultFilterConfigData;
        }

        private String getDefaultFilterAllData() {
            return defaultFilterAllData;
        }

        private String getDefaultFilterConfigData() {
            return defaultFilterConfigData;
        }

    }

}
