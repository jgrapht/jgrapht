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
 * EdgeFactory.java
 * ----------------
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
 * An edge factory used by graphs for creating new edges.
 *
 * @author Barak Naveh
 * @since Jul 14, 2003
 */
public interface EdgeFactory<V, E>
{
    

    /**
     * Creates a new edge whose endpoints are the specified source and target
     * vertices.
     *
     * @param sourceVertex the source vertex.
     * @param targetVertex the target vertex.
     *
     * @return a new edge whose endpoints are the specified source and target
     * vertices.
     */
    public E createEdge(V sourceVertex, V targetVertex);
}

// End EdgeFactory.java
