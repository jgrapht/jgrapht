/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
/* ------------------
 * DSVUtils.java
 * ------------------
 * (C) Copyright 2016, by Dimitrios Michail and Contributors.
 *
 * Original Author:  Dimitrios Michail
 * Contributors: -
 *
 * Changes
 * -------
 * 20-Aug-2016 : Initial Version (DM);
 *
 */
package org.jgrapht.ext;

/**
 * Helper utilities for escaping and unescaping Delimiter-separated values.
 * 
 * Used in {@link CSVExporter} and {@link CSVImporter}.
 * 
 * @see CSVImporter
 * @see CSVExporter
 * 
 * @author Dimitrios Michail
 * @since August 2016
 */
class DSVUtils
{
    private static final char DSV_QUOTE = '"';
    private static final char DSV_LF = '\n';
    private static final char DSV_CR = '\r';
    private static final String DSV_QUOTE_AS_STRING = String.valueOf(DSV_QUOTE);

    /**
     * Test if a character can be used as a delimiter in a Delimiter-separated
     * values file.
     * 
     * @param delimiter the character to test
     * @return {@code true} if the character can be used as a delimiter,
     *         {@code} false otherwise
     */
    public static boolean isValidDelimiter(char delimiter)
    {
        return delimiter != DSV_LF && delimiter != DSV_CR
            && delimiter != DSV_QUOTE;
    }

    /**
     * Escape a Delimiter-separated values string.
     * 
     * @param input the input
     * @param delimiter the delimiter
     * @return the escaped output
     */
    public static String escapeDSV(String input, char delimiter)
    {
        char[] specialChars = new char[] {
            delimiter,
            DSV_QUOTE,
            DSV_LF,
            DSV_CR };

        boolean containsSpecial = false;
        for (int i = 0; i < specialChars.length; i++) {
            if (input.contains(String.valueOf(specialChars[i]))) {
                containsSpecial = true;
                break;
            }
        }

        if (containsSpecial) {
            return DSV_QUOTE_AS_STRING + input.replaceAll(
                DSV_QUOTE_AS_STRING,
                DSV_QUOTE_AS_STRING + DSV_QUOTE_AS_STRING)
                + DSV_QUOTE_AS_STRING;
        }

        return input;
    }

    /**
     * Unescape a Delimiter-separated values string.
     * 
     * @param input the input
     * @param delimiter the delimiter
     * @return the unescaped output
     */
    public static String unescapeDSV(String input, char delimiter)
    {
        char[] specialChars = new char[] {
            delimiter,
            DSV_QUOTE,
            DSV_LF,
            DSV_CR };

        if (input.charAt(0) != DSV_QUOTE
            || input.charAt(input.length() - 1) != DSV_QUOTE)
        {
            return input;
        }

        String noQuotes = input.subSequence(1, input.length() - 1).toString();

        boolean containsSpecial = false;
        for (int i = 0; i < specialChars.length; i++) {
            if (noQuotes.contains(String.valueOf(specialChars[i]))) {
                containsSpecial = true;
                break;
            }
        }

        if (containsSpecial) {
            return noQuotes.replaceAll(
                DSV_QUOTE_AS_STRING + DSV_QUOTE_AS_STRING,
                DSV_QUOTE_AS_STRING);
        }

        return input;
    }
}

// End DSVUtils.java
