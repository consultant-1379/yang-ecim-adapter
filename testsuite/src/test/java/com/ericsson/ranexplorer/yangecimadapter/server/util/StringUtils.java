package com.ericsson.ranexplorer.yangecimadapter.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ericsson.ranexplorer.yangecimadapter.server.constants.IntTestConstants.ONE_DOT_ONE_TERMINATION;
import static com.ericsson.ranexplorer.yangecimadapter.server.constants.IntTestConstants.ONE_DOT_ZERO_TERMINATION;

public class StringUtils {

    private static final Logger logger = LoggerFactory.getLogger(StringUtils.class);

    private static final int END_OF_STRING_LENGTH = 20;

    private StringUtils() {
        throw new IllegalAccessError("This is a utility class");
    }

    public static String removeInitialAndTerminationStringFrom(final String response) {
        logger.debug("Starting to remove Netconf initial and termination strings");
        logger.trace("Before removing, response is\n{}", response);
        if (response == null) {
            logger.warn("No response available to process");
            return "";
        } else if (response.contains(ONE_DOT_ZERO_TERMINATION)) {
            return get10Message(response);
        } else if (response.contains(ONE_DOT_ONE_TERMINATION)) {
            return get11Message(response);
        } else {
            logger.warn("Expected {} or {} at end of message. Instead message ends with {}",
                    ONE_DOT_ZERO_TERMINATION, ONE_DOT_ONE_TERMINATION, getEndOfString(response) );
            return response;
        }
    }

    public static String getEndOfString(String string) {
        return string.substring(string.length() < END_OF_STRING_LENGTH ? 0 : string.length() - END_OF_STRING_LENGTH,string.length());
    }

    static int getIndexOfFirstMessageCharacter(String message) {
        Pattern messageStartPattern = Pattern.compile("^#[0-9]+\n");
        Matcher matcher = messageStartPattern.matcher(message);
        if (matcher.lookingAt()) {
            if ( logger.isDebugEnabled() ) {
                logger.debug("First characters are:\n{}\nIndex is {}.", message.substring(0, 9),
                        matcher.end());
            }
            return matcher.end();
        } else {
            if ( logger.isWarnEnabled() ) {
                logger.warn("Expected first character of message to be '#' followed by digits of " +
                        "number of characters in message. This number is message length in" +
                        "characters. Expected line feed character after the number. Instead first " +
                        "characters are {}", message.substring(0, 9));
            }
            return 0;
        }
    }

    public static String get10Message(String message) {
        return message.substring(0, message.lastIndexOf(ONE_DOT_ZERO_TERMINATION));
    }

    public static String get11Message(String message) {
        return message.substring(getIndexOfFirstMessageCharacter(message), message.indexOf(
                ONE_DOT_ONE_TERMINATION) );
    }
}
