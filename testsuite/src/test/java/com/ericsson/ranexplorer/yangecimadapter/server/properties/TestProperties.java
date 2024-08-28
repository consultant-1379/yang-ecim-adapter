/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */
package com.ericsson.ranexplorer.yangecimadapter.server.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.ericsson.ranexplorer.yangecimadapter.server.constants.IntTestConstants.NETSIM;
import static com.ericsson.ranexplorer.yangecimadapter.server.constants.IntTestConstants.REALNODE;

public class TestProperties {

    private static final Logger logger = LoggerFactory.getLogger(TestProperties.class);

    public TestProperties() {
        // Keep SonarQube happy
    }

    private static final Map<TestProperty, String> properties = createMap();

    private static Map<TestProperty, String> createMap() {
        Map<TestProperty, String> result = new HashMap<>();

        String nodeType = System.getProperty("realnode", NETSIM);
        result.put(TestProperty.NODE_TYPE, NETSIM.equals(nodeType) ? NETSIM : REALNODE);

        result.put(TestProperty.ADAPTER_HOST, System.getProperty("adapter-host", "localhost"));

        if (NETSIM.equals(nodeType)) {
            result.put(TestProperty.ADAPTER_PORT, System.getProperty("adapter-port", "2222"));
            result.put(TestProperty.NODE_HOST, System.getProperty("node-host", "192.168.100.1")); //NOSONAR
            result.put(TestProperty.NODE_PORT, System.getProperty("node-port", "22"));
            result.put(TestProperty.SSH_USER, System.getProperty("ssh-user", "netsim"));
            result.put(TestProperty.SSH_PASS, System.getProperty("ssh-pass", "netsim"));
        } else {
            result.put(TestProperty.ADAPTER_PORT, System.getProperty("adapter-port", "2022"));
            result.put(TestProperty.NODE_HOST, System.getProperty("node-host", "lienb4916"));
            result.put(TestProperty.NODE_PORT, System.getProperty("node-port", "2022"));
            result.put(TestProperty.SSH_USER, System.getProperty("ssh-user", "expert"));
            result.put(TestProperty.SSH_PASS, System.getProperty("ssh-pass", "expert"));
        }
        return Collections.unmodifiableMap(result);
    }

    public String valueOf(TestProperty property) {
        return properties.get(property);
    }

    public int valueOfInt(TestProperty property) {
        int value = 0;
        try {
            value = Integer.parseInt(properties.get(property));
        } catch (NumberFormatException exception) {
            logger.error("Invalid conversion to int: property {}, value {}", property, properties.get(property));
        }
        return value;
    }

    public boolean isRealNodeTest() {
        return REALNODE.equals(properties.get(TestProperty.NODE_TYPE));
    }

}
