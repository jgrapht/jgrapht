/*
 * (C) Copyright 2003-2017, by Christoph Zauner and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
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
    private static final Pattern ALPHA_DIG = Pattern.compile("[a-zA-Z_][\\w]*");
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
