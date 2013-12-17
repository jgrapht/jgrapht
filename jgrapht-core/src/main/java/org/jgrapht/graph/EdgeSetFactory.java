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
/* ----------------
 * EdgeSetFactory.java
 * ----------------
 * (C) Copyright 2005-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 01-Jun-2005 : Initial revision (JVS);
 * 06-Aug-2005 : Made generic (CH);
 * 07-May-2006 : Renamed and changed from List<Edge> to Set<Edge> (JVS);
 *
 */
package org.jgrapht.graph;

import java.util.*;


/**
 * A factory for edge sets. This interface allows the creator of a graph to
 * choose the {@link java.util.Set} implementation used internally by the graph
 * to maintain sets of edges. This provides control over performance tradeoffs
 * between memory and CPU usage.
 *
 * @author John V. Sichi
 */
public interface EdgeSetFactory<V, E>
{
    

    /**
     * Create a new edge set for a particular vertex.
     *
     * @param vertex the vertex for which the edge set is being created;
     * sophisticated factories may be able to use this information to choose an
     * optimal set representation (e.g. ArrayUnenforcedSet for a vertex expected
     * to have low degree, and LinkedHashSet for a vertex expected to have high
     * degree)
     *
     * @return new set
     */
    public Set<E> createEdgeSet(V vertex);
}

// End EdgeSetFactory.java
