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
/* -----------------
 * MaximumCliqueAlgorithm.java
 * -----------------
 * (C) Copyright 2016, by Nils Olberg and Contributors.
 *
 * Original Author: Nils Olberg
 * Contributor(s): Joris Kinable
 *
 */

package org.jgrapht.alg.interfaces;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.util.WeightedVertexSet;

/**
 * Computes a maximum clique in an undirected graph. A clique in a graph is a set of vertices V such that for each pair
 * u, v in V there is an edge between u and v. A maximum clique is a clique having the highest possible number of vertices 
 * for a given graph. A clique of maximum weight is a clique where the sum of weights assigned to the individual vertices 
 * in the clique has been maximized. The maximum clique problem is a special case of the maximum weighted clique problem 
 * where all vertices have equal weight.
 */

public interface MaximumCliqueAlgorithm<V,E> {

    /**
     * Computes a maximum clique; all vertices are considered to have equal weight.
     * @return a vertex cover
     */
    
    WeightedVertexSet<V> getClique(UndirectedGraph<V,E> graph, Class<? extends E> edgeClass);
}
