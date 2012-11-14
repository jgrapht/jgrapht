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
 * (C) Copyright 2005-2008, by Trevor Harmon.
 *
 * Original Author:  Trevor Harmon
 *
 */
package org.jgrapht.ext;

import com.google.common.collect.Maps;

import java.util.Map;


/**
 * Assigns a unique integer to represent each edge. Each instance of
 * IntegerEdgeNameProvider maintains an internal map between every edge it has
 * ever seen and the unique integer representing that edge. As a result it is
 * probably desirable to have a separate instance for each distinct graph.
 *
 * @author Trevor Harmon
 */
public class IntegerEdgeNameProvider<E>
    implements EdgeNameProvider<E>
{
    //~ Instance fields --------------------------------------------------------

    private int nextID = 1;
    private final Map<E, Integer> idMap = Maps.newHashMap();

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
     * Returns the String representation of an edge.
     *
     * @param edge the edge to be named
     */
    @Override
    public String getEdgeName(final E edge)
    {
        Integer id = idMap.get(edge);
        if (id == null) {
            id = nextID++;
            idMap.put(edge, id);
        }

        return id.toString();
    }
}

// End IntegerEdgeNameProvider.java
