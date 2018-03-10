/*
 * (C) Copyright 2003-2017, by Christoph Zauner and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht.io;

import java.util.regex.*;

/**
 * Class with DOT format related utilities.
 * 
 * @author Christoph Zauner
 */
class DOTUtils
{
    /** Keyword for representing strict graphs. */
    static final String DONT_ALLOW_MULTIPLE_EDGES_KEYWORD = "strict";
    /** Keyword for directed graphs. */
    static final String DIRECTED_GRAPH_KEYWORD = "digraph";
    /** Keyword for undirected graphs. */
    static final String UNDIRECTED_GRAPH_KEYWORD = "graph";
    /** Edge operation for directed graphs. */
    static final String DIRECTED_GRAPH_EDGEOP = "->";
    /** Edge operation for undirected graphs. */
    static final String UNDIRECTED_GRAPH_EDGEOP = "--";

    // patterns for IDs
    private static final Pattern ALPHA_DIG = Pattern.compile("[a-zA-Z]+([\\w_]*)?");
    private static final Pattern DOUBLE_QUOTE = Pattern.compile("\".*\"");
    private static final Pattern DOT_NUMBER = Pattern.compile("[-]?([.][0-9]+|[0-9]+([.][0-9]*)?)");
    private static final Pattern HTML = Pattern.compile("<.*>");

    /**
     * Test if the ID candidate is a valid ID.
     *
     * @param idCandidate the ID candidate.
     *
     * @return <code>true</code> if it is valid; <code>false</code> otherwise.
     */
    static boolean isValidID(String idCandidate)
    {
        return ALPHA_DIG.matcher(idCandidate).matches()
            || DOUBLE_QUOTE.matcher(idCandidate).matches()
            || DOT_NUMBER.matcher(idCandidate).matches() || HTML.matcher(idCandidate).matches();
    }

}

// End DOTUtils.java
