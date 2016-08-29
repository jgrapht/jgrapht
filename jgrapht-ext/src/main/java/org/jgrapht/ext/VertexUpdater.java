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
 * VertexUpdater.java
 * ------------------
 * (C) Copyright 2015, by  Wil Selwood.
 *
 * Original Author:  Wil Selwood <wselwood@ijento.com>
 *
 */
package org.jgrapht.ext;

import java.util.Map;

/**
 * Type to handle updates to a vertex when an import gets more information about
 * a vertex after it has been created.
 *
 * @param <V> the vertex type
 */
public interface VertexUpdater<V>
{
    /**
     * Update vertex with the extra attributes.
     *
     * @param vertex to update
     * @param attributes to add to the vertex
     */
    void updateVertex(V vertex, Map<String, String> attributes);
}

// End VertexUpdater.java
