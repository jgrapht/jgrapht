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
/* --------------------
 * UndirectedGraph.java
 * --------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 * 11-Mar-2004 : Made generic (CH);
 *
 */
package org.jgrapht;

/**
 * A graph whose all edges are undirected. This is the root interface of all
 * undirected graphs.
 *
 * <p>See <a href="http://mathworld.wolfram.com/Graph.html">
 * http://mathworld.wolfram.com/Graph.html</a> for more on undirected and on
 * directed graphs.</p>
 *
 * @author Barak Naveh
 * @since Jul 14, 2003
 */
public interface UndirectedGraph<V, E>
    extends Graph<V, E>
{
    

    /**
     * Returns the degree of the specified vertex. A degree of a vertex in an
     * undirected graph is the number of edges touching that vertex.
     *
     * @param vertex vertex whose degree is to be calculated.
     *
     * @return the degree of the specified vertex.
     */
    public int degreeOf(V vertex);
}

// End UndirectedGraph.java
