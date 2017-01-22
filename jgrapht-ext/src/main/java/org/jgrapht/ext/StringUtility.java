package org.jgrapht.ext;

public class StringUtility
{
    /**
     * Removes quotes from a string
     * 
     * @param in a quoted string
     * @return the string without enclosing quotes; if <code>in</code> is not
     *         quoted, the original string is returned.
     */
    public static String deQuote (String in)
    {
        int len = in.length();

        if (in.charAt(0) == '\"' && in.charAt(len - 1) == '\"') {
            if (len > 2) {
                return in.substring(1, len - 1);
            } else {
                return "";
            }
        }

        return in;
    }
}
