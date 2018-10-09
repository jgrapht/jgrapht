/*
 * (C) Copyright 2017-2018, by Joris Kinable and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.generate;

import org.jgrapht.*;

import java.util.*;

/**
 * Generator for <a href="http://mathworld.wolfram.com/GeneralizedPetersenGraph.html">Generalized
 * Petersen Graphs</a> The Generalized Petersen graphs $GP(n,k)$ are a family of cubic graphs formed
 * by connecting the vertices of a regular polygon (cycle graph $C_n$) to the corresponding vertices
 * of a star polygon ${n,k}$. Several special cases of the generalized Petersen graph are predefined
 * in the {@link NamedGraphGenerator}.
 *
 * @author Joris Kinable
 *
 * @param <V> graph vertex type
 * @param <E> graph edge type
 */
public class GeneralizedPetersenGraphGenerator<V, E>
    implements
    GraphGenerator<V, E, List<V>>
{

    private final int n;
    private final int k;

    /**
     * Key used to access the star polygon vertices in the resultMap
     */
    public final String STAR = "star";
    /**
     * Key used to access the regular polygon vertices in the resultMap
     */
    public final String REGULAR = "regular";

    /**
     * Constructs a GeneralizedPetersenGraphGenerator used to generate a Generalized Petersen graphs
     * $GP(n,k)$.
     * 
     * @param n size of the regular polygon (cycle graph $C_n$)
     * @param k size of the star polygon ${n,k}$
     */
    public GeneralizedPetersenGraphGenerator(int n, int k)
    {
        if (n < 3)
            throw new IllegalArgumentException("n must be larger or equal than 3");
        if (k < 1 || k > Math.floor((n - 1) / 2.0))
            throw new IllegalArgumentException("k must be in the range [1, floor((n-1)/2.0)]");

        this.n = n;
        this.k = k;
    }

    /**
     * Generates the Generalized Petersen Graph
     * 
     * @param target receives the generated edges and vertices; if this is non-empty on entry, the
     *        result will be a disconnected graph since generated elements will not be connected to
     *        existing elements
     * @param resultMap if non-null, the resultMap contains a mapping from the key "star" to a list
     *        of vertices constituting the star polygon, as well as a key "regular" which maps to a
     *        list of vertices constituting the regular polygon.
     */
    @Override
    public void generateGraph(Graph<V, E> target, Map<String, List<V>> resultMap)
    {
        List<V> verticesU = new ArrayList<>(n); // Regular polygon vertices
        List<V> verticesV = new ArrayList<>(n); // Star polygon vertices
        for (int i = 0; i < n; i++) {
            verticesU.add(target.addVertex());
            verticesV.add(target.addVertex());
        }

        for (int i = 0; i < n; i++) {
            target.addEdge(verticesU.get(i), verticesU.get((i + 1) % n));
            target.addEdge(verticesU.get(i), verticesV.get(i));
            target.addEdge(verticesV.get(i), verticesV.get((i + k) % n));
        }
        if (resultMap != null) {
            resultMap.put(REGULAR, verticesU);
            resultMap.put(STAR, verticesV);
        }
    }
}
