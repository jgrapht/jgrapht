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
 * VertexFactory.java
 * ------------------
 * (C) Copyright 2003-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 16-Sep-2003 : Initial revision (JVS);
 * 11-Mar-2004 : Made generic (CH);
 *
 */
package org.jgrapht;

/**
 * A vertex factory used by graph algorithms for creating new vertices.
 * Normally, vertices are constructed by user code and added to a graph
 * explicitly, but algorithms which generate new vertices require a factory.
 *
 * @author John V. Sichi
 * @since Sep 16, 2003
 */
public interface VertexFactory<V>
{
    

    /**
     * Creates a new vertex.
     *
     * @return the new vertex
     */
    public V createVertex();
}

// End VertexFactory.java
