/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://org.org.jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2013, by Barak Naveh and Contributors.
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
/* -------------------------
 * MinimumSpanningTree.java
 * -------------------------
 *
 * Original Author:  Alexey Kudinkin
 * Contributor(s):
 *
 */
package org.jgrapht.alg.interfaces;

import java.util.Set;

/**
 * Allows to derive <a href=http://en.wikipedia.org/wiki/Minimum_spanning_tree>minimum spanning tree</a>
 * from given undirected connected graph. In the case of disconnected graphs it would rather
 * derive minimum spanning <i>forest<i/>
 *
 * @param <V>   vertex concept type
 * @param <E>   edge concept type
 */
public interface MinimumSpanningTree<V, E> {

    /**
     * Returns edges set constituting the minimum spanning tree/forest
     *
     * @return minimum spanning-tree edges set
     */
    public Set<E> getMinimumSpanningTreeEdgeSet();

    /**
     * Returns total weight of the minimum spanning tree/forest.
     *
     * @return minimum spanning-tree total weight
     */
    public double getMinimumSpanningTreeTotalWeight();

}
