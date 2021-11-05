/*
 * (C) Copyright 2010-2021, by Michael Behrisch and Contributors.
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
package org.jgrapht.alg.color;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.util.*;

import java.util.*;

/**
 * Brown graph coloring algorithm.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Michael Behrisch
 */
public class BrownBacktrackColoring<V, E>
    implements
    VertexColoringAlgorithm<V>
{
    private final List<V> vertexList; // list of all vertices
    private final int[][] neighbors; // for every vertex v, neighbors[v] stores the neighbors of v
    private final Map<V, Integer> indexMap; // assigned unique index to each vertex. maps to vertex
                                            // list

    private int[] partialColorAssignment; // color assigned to a specific vertex
    private int[] colorCount; // Number of colors used up to the ith vertex that has been colored
    private BitSet[] allowedColors;
    private int chi; // chromatic number

    private int[] completeColorAssignment;
    private Coloring<V> vertexColoring;

    /**
     * Construct a new Brown backtracking algorithm.
     * 
     * @param graph the input graph
     */
    public BrownBacktrackColoring(Graph<V, E> graph)
    {
        Objects.requireNonNull(graph, "Graph cannot be null");
        final int numVertices = graph.vertexSet().size();
        vertexList = new ArrayList<>(numVertices);
        neighbors = new int[numVertices][];
        indexMap = CollectionUtil.newHashMapWithExpectedSize(numVertices);
        for (V vertex : graph.vertexSet()) {
            neighbors[vertexList.size()] = new int[graph.edgesOf(vertex).size()];
            indexMap.put(vertex, vertexList.size());
            vertexList.add(vertex);
        }
        for (int i = 0; i < numVertices; i++) {
            int nbIndex = 0;
            final V vertex = vertexList.get(i);
            for (E e : graph.edgesOf(vertex)) {
                neighbors[i][nbIndex++] = indexMap.get(Graphs.getOppositeVertex(graph, e, vertex));
            }
        }
    }

    private void recursiveColor(int pos)
    {
        colorCount[pos] = colorCount[pos - 1];
        allowedColors[pos].set(0, colorCount[pos] + 1); // To color the ith vertex, one can use the
                                                        // number of colors needed to color the
                                                        // i-1th vertex plus 1
        // Determine which colors have been used by the neighbors of the ith vertex
        for (int i = 0; i < neighbors[pos].length; i++) {
            final int nb = neighbors[pos][i];
            if (partialColorAssignment[nb] > 0) {
                allowedColors[pos].clear(partialColorAssignment[nb]);
            }
        }

        // Try to assign each of the already used colors to vertex i. Prune search if partial
        // coloring will never be better than chromatic number of best solution found thus far
        for (int i = 1; (i <= colorCount[pos]) && (colorCount[pos] < chi); i++) {
            if (allowedColors[pos].get(i)) { // Try all available colors for vertex i. A color is
                                             // available if its not used by its neighbor
                partialColorAssignment[pos] = i;
                if (pos < (neighbors.length - 1)) { // If not all vertices have been colored,
                                                    // proceed with the next uncolored vertex
                    recursiveColor(pos + 1);
                } else { // Otherwise we have found a feasible coloring
                    chi = colorCount[pos];
                    System
                        .arraycopy(
                            partialColorAssignment, 0, completeColorAssignment, 0,
                            partialColorAssignment.length);
                }
            }
        }
        // consider using a new color for vertex i
        if ((colorCount[pos] + 1) < chi) {
            colorCount[pos]++;
            partialColorAssignment[pos] = colorCount[pos];
            if (pos < (neighbors.length - 1)) {
                recursiveColor(pos + 1);
            } else {
                chi = colorCount[pos];
                System
                    .arraycopy(
                        partialColorAssignment, 0, completeColorAssignment, 0,
                        partialColorAssignment.length);
            }
        }
        partialColorAssignment[pos] = 0;
    }

    private void lazyComputeColoring()
    {
        if (vertexColoring != null)
            return;

        chi = neighbors.length + 1;
        partialColorAssignment = new int[neighbors.length];
        completeColorAssignment = new int[neighbors.length];
        partialColorAssignment[0] = 1; // Prefix color of first vertex. Optimization: Could prefix
                                       // all colors of largest clique
        colorCount = new int[neighbors.length];
        colorCount[0] = 1;
        allowedColors = new BitSet[neighbors.length];
        for (int i = 0; i < neighbors.length; i++) {
            allowedColors[i] = new BitSet(1);
        }
        recursiveColor(1);

        Map<V, Integer> colorMap = new LinkedHashMap<>();
        for (int i = 0; i < vertexList.size(); i++)
            colorMap.put(vertexList.get(i), completeColorAssignment[i]);
        vertexColoring = new ColoringImpl<>(colorMap, chi);
    }

    /**
     * Returns the <a href="http://mathworld.wolfram.com/ChromaticNumber.html">chromatic number</a>
     * of the input graph
     * 
     * @return chromatic number of the graph
     */
    public int getChromaticNumber()
    {
        lazyComputeColoring();
        return vertexColoring.getNumberColors();
    }

    @Override
    public Coloring<V> getColoring()
    {
        lazyComputeColoring();
        return vertexColoring;
    }
}
