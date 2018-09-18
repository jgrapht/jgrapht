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
 * Generator for <a href="http://mathworld.wolfram.com/WindmillGraph.html">Windmill Graphs</a>,
 * <a href="http://mathworld.wolfram.com/DutchWindmillGraph.html">Dutch Windmill Graphs</a> and
 * <a href="https://en.wikipedia.org/wiki/Friendship_graph">Friendship Graphs</a>.
 * <p>
 * The windmill graph $W_n^{(m)}$ is the graph obtained by taking $m$ copies of the complete graph
 * $K_n$ with a vertex in common. The Dutch windmill graph $D_n^{(m)}$, is the graph obtained by
 * taking $m$ copies of the cycle graph $C_3$ with a vertex in common. For the special case where
 * $n=3$, $D_n^{(m)}$ and $W_n^{(m)}$ are identical. The class of graphs $D_3^{(m)}$ is sometimes
 * referred to as the Friendship graph, denoted by $F_m$.
 *
 * @author Joris Kinable
 *
 * @param <V> graph vertex type
 * @param <E> graph edge type
 */
public class WindmillGraphsGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{
    /**
     * WINDMILL and DUTCHWINDMILL Modes for the Constructor
     */
    public enum Mode
    {
        WINDMILL,
        DUTCHWINDMILL
    }

    private final Mode mode;
    private final int m;
    private final int n;

    /**
     * Constructs a GeneralizedPetersenGraphGenerator used to generate a Generalized Petersen graphs
     * $GP(n,k)$.
     * 
     * @param mode indicate whether the generator should generate Windmill graphs or Dutch Windmill
     *        graphs
     * @param m number of copies of $C_n$ (Dutch Windmill graph) or $K_n$ (Windmill graph)
     * @param n size of $C_n$ (Dutch Windmill graph) or $K_n$ (Windmill graph). To generate
     *        friendship graphs, set $n=3$ (the mode is irrelevant).
     */
    public WindmillGraphsGenerator(Mode mode, int m, int n)
    {
        if (m < 2)
            throw new IllegalArgumentException("m must be larger or equal than 2");
        if (n < 3)
            throw new IllegalArgumentException("n must be larger or equal than 3");

        this.mode = mode;
        this.m = m;
        this.n = n;
    }

    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        V center = target.addVertex();
        List<V> sub = new ArrayList<>(n);

        if (mode == Mode.DUTCHWINDMILL) { // Generate Dutch windmill graph
            for (int i = 0; i < m; i++) { // m copies of cycle graph Cn
                sub.clear();
                sub.add(center);
                for (int j = 1; j < n; j++) {
                    sub.add(target.addVertex());
                }

                for (int r = 0; r < sub.size(); r++)
                    target.addEdge(sub.get(r), sub.get((r + 1) % n));
            }
        } else { // Generate windmill graph
            for (int i = 0; i < m; i++) { // m copies of complete graph Kn
                sub.clear();
                sub.add(center);
                for (int j = 1; j < n; j++) {
                    sub.add(target.addVertex());
                }

                for (int r = 0; r < sub.size() - 1; r++)
                    for (int s = r + 1; s < sub.size(); s++)
                        target.addEdge(sub.get(r), sub.get(s));
            }
        }
    }
}
