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
 * StringNameProvider.java
 * ------------------
 * (C) Copyright 2005-2008, by Trevor Harmon.
 *
 * Original Author:  Trevor Harmon
 *
 */
package org.jgrapht.ext;

/**
 * Generates edge names by invoking {@link #toString()} on them. This assumes
 * that the edge's {@link #toString()} method returns a unique String
 * representation for each edge.
 *
 * @author Trevor Harmon
 */
public class StringEdgeNameProvider<E>
    implements EdgeNameProvider<E>
{
    

    public StringEdgeNameProvider()
    {
    }

    

    /**
     * Returns the String representation an edge.
     *
     * @param edge the edge to be named
     */
    public String getEdgeName(E edge)
    {
        return edge.toString();
    }
}

// End StringEdgeNameProvider.java
