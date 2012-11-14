/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/* ------------------
 * IntegerNameProvider.java
 * ------------------
 * (C) Copyright 2005-2008, by Charles Fry and Contributors.
 *
 * Original Author:  Charles Fry
 *
 * $Id$
 *
 * Changes
 * -------
 * 13-Dec-2005 : Initial Version (CF);
 *
 */
package org.jgrapht.ext;

import com.google.common.collect.Maps;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;

import java.util.Map;


/**
 * Assigns a unique integer to represent each vertex. Each instance of
 * IntegerNameProvider maintains an internal map between every vertex it has
 * ever seen and the unique integer representing that vertex. As a result it is
 * probably desirable to have a separate instance for each distinct graph.
 *
 * @author Charles Fry
 */
public class IntegerNameProvider<V>
    implements VertexNameProvider<V>
{
    //~ Instance fields --------------------------------------------------------

    private int nextID = 1;
    private final Map<V, Integer> idMap = Maps.newHashMap();

    //~ Methods ----------------------------------------------------------------

    /**
     * Clears all cached identifiers, and resets the unique identifier counter.
     */
    public void clear()
    {
        nextID = 1;
        idMap.clear();
    }

    /**
     * Returns the String representation of the unique integer representing a
     * vertex.
     *
     * @param vertex the vertex to be named
     *
     * @return the name of
     *
     * @see GraphListener#edgeAdded(GraphEdgeChangeEvent)
     */
    @Override
    public String getVertexName(final V vertex)
    {
        Integer id = idMap.get(vertex);
        if (id == null) {
            id = nextID++;
            idMap.put(vertex, id);
        }

        return id.toString();
    }
}

// End IntegerNameProvider.java
