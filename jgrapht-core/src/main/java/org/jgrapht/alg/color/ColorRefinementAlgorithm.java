/*
 * (C) Copyright 2018-2018, by Christoph Grüne, Daniel Mock, Oliver Feith and Contributors.
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

import java.util.*;
import java.util.stream.*;

/**
 * Color refinement algorithm that finds the coarsest stable coloring of a graph based on a given
 * <code>alpha</code> coloring as described in the following
 * <a href="https://doi.org/10.1007/s00224-016-9686-0">paper</a>: C. Berkholz, P. Bonsma, and M.
 * Grohe. Tight lower and upper bounds for the complexity of canonical colour refinement. Theory of
 * Computing Systems, 60(4), p581--614, 2017.
 * 
 * <p>
 * The complexity of this algorithm is $O((|V| + |E|)log |V|)$.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Christoph Grüne
 * @author Daniel Mock
 * @author Oliver Feith
 */
public class ColorRefinementAlgorithm<V, E>
    implements
    VertexColoringAlgorithm<V>
{
    private final Graph<V, E> graph;
    private final Coloring<V> alpha;

    /**
     * Construct a new coloring algorithm.
     *
     * @param graph the input graph
     * @param alpha the coloring on the graph to be refined
     */
    public ColorRefinementAlgorithm(Graph<V, E> graph, Coloring<V> alpha)
    {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        this.alpha = Objects.requireNonNull(alpha, "alpha cannot be null");
        if (!isAlphaConsistent(alpha, graph)) {
            throw new IllegalArgumentException(
                "alpha is not a valid surjective l-coloring for the given graph.");
        }
    }

    /**
     * Construct a new coloring algorithm.
     *
     * @param graph the input graph
     */
    public ColorRefinementAlgorithm(Graph<V, E> graph)
    {
        this(graph, getDefaultAlpha(graph.vertexSet()));
    }

    /**
     * Calculates a canonical surjective k-coloring of the given graph such that the classes of the
     * coloring form the coarsest stable partition that refines alpha.
     *
     * @return the calculated coloring
     */
    @Override
    public Coloring<V> getColoring()
    {
        // initialize internal representation
        ColoringRepresentation rep = new ColoringRepresentation(graph, alpha);

        // get a sorted (ascending) stack of all colors that are predefined by alpha
        Deque<Integer> refineStack = getSortedStack(alpha);

        // main iteration
        while (!refineStack.isEmpty()) {
            Integer currentColor = refineStack.pop();

            Set<Integer> adjacentColors = calculateColorDegrees(currentColor, rep);

            // split colors
            adjacentColors
                .stream().filter(c -> rep.minColorDegree[c] < rep.maxColorDegree[c])
                .sorted(Comparator.comparingInt(o -> o)) // canonical order
                .forEach(color -> splitUpColor(color, refineStack, rep));

            cleanupColorDegrees(adjacentColors, rep);
        }

        // return result
        return new ColoringImpl<>(rep.coloring, rep.coloring.size());
    }

    /**
     * Helper method that calculates the color degree for every vertex and the maximum and minimum
     * color degree for every color.
     *
     * @param refiningColor color to refine
     * @param rep the coloring representation
     * @return the list of all colors that have at least one vertex with colorDegree >= 1
     */
    private Set<Integer> calculateColorDegrees(int refiningColor, ColoringRepresentation rep)
    {
        int n = graph.vertexSet().size();
        Set<Integer> adjacentColors = new LinkedHashSet<>(n);

        // calculate color degree and update maxColorDegree
        for (V v : rep.colorClasses.get(refiningColor)) {
            Set<V> inNeighborhood = graph
                .incomingEdgesOf(v).stream().map(e -> Graphs.getOppositeVertex(graph, e, v))
                .collect(Collectors.toSet());

            for (V w : inNeighborhood) {
                rep.colorDegree.put(w, rep.colorDegree.get(w) + 1);
                if (rep.colorDegree.get(w) == 1) {
                    rep.positiveDegreeColorClasses.get(rep.coloring.get(w)).add(w);
                }
                adjacentColors.add(rep.coloring.get(w));

                // update maxColorDegree for color(w) if maximum color degree has increased.
                if (rep.colorDegree.get(w) > rep.maxColorDegree[rep.coloring.get(w)]) {
                    rep.maxColorDegree[rep.coloring.get(w)] = rep.colorDegree.get(w);
                }
            }
        }

        // update minColorDegree
        for (Integer c : adjacentColors) {
            // if there is a vertex with colorDegree(v) = 0 < 1, set minimum color degree to
            // 0
            if (rep.colorClasses.get(c).size() != rep.positiveDegreeColorClasses.get(c).size()) {
                rep.minColorDegree[c] = 0;
            } else {
                rep.minColorDegree[c] = rep.maxColorDegree[c];
                for (V v : rep.positiveDegreeColorClasses.get(c)) {
                    if (rep.colorDegree.get(v) < rep.minColorDegree[c]) {
                        rep.minColorDegree[c] = rep.colorDegree.get(v);
                    }
                }
            }
        }

        return adjacentColors;
    }

    /**
     * Helper method that cleanups the internal representation of color degrees for a new iteration.
     *
     * @param adjacentColors the list of all colors that have at least one vertex with colorDegree
     *        >= 1
     * @param rep the coloring representation
     */
    private void cleanupColorDegrees(Set<Integer> adjacentColors, ColoringRepresentation rep)
    {
        for (int c : adjacentColors) {
            for (V v : rep.positiveDegreeColorClasses.get(c)) {
                rep.colorDegree.put(v, 0);
            }
            rep.maxColorDegree[c] = 0;
            rep.positiveDegreeColorClasses.put(c, new ArrayList<>());
        }
    }

    /**
     * Helper method for splitting up a color.
     *
     * @param color the color to split the color class for
     * @param refineStack the stack containing all colors that have to be refined
     * @param rep the coloring representation
     */
    private void splitUpColor(Integer color, Deque<Integer> refineStack, ColoringRepresentation rep)
    {
        // Initialize and calculate numColorDegree (mapping from the color degree to the number of
        // vertices with that color degree).
        Map<Integer, Integer> numColorDegree = new HashMap<>();
        for (int i = 1; i <= rep.maxColorDegree[color]; ++i) {
            numColorDegree.put(i, 0);
        }
        numColorDegree.put(
            0,
            rep.colorClasses.get(color).size() - rep.positiveDegreeColorClasses.get(color).size());
        for (V v : rep.positiveDegreeColorClasses.get(color)) {
            numColorDegree
                .put(rep.colorDegree.get(v), numColorDegree.get(rep.colorDegree.get(v)) + 1);
        }

        // Helper variable storing the index with the maximum number of vertices with the
        // corresponding color degree
        int maxColorDegreeIndex = 0;
        for (int i = 1; i <= rep.maxColorDegree[color]; ++i) {
            if (numColorDegree.get(i) > numColorDegree.get(maxColorDegreeIndex)) {
                maxColorDegreeIndex = i;
            }
        }

        // Go through all indices (color degrees) of numColorDegree
        Map<Integer, Integer> newMapping = new HashMap<>();
        boolean isCurrentColorInStack = refineStack.contains(color);
        int currentMaxColorDegree = rep.maxColorDegree[color];
        for (int i = 0; i <= currentMaxColorDegree; ++i) {
            if (numColorDegree.get(i) >= 1) {
                if (i == rep.minColorDegree[color]) {
                    newMapping.put(i, color); // keep current color

                    // Push current color on the stack if it is not in the stack and i is not the
                    // index with the maximum number of vertices with the corresponding color degree
                    if (!isCurrentColorInStack && maxColorDegreeIndex != i) {
                        refineStack.push(newMapping.get(i));
                    }
                } else {
                    newMapping.put(i, ++rep.lastUsedColor); // new color

                    // Push current color on the stack if it is in the stack and i is not the index
                    // with the maximum number of vertices with the corresponding color degree
                    if (isCurrentColorInStack || i != maxColorDegreeIndex) {
                        refineStack.push(newMapping.get(i));
                    }
                }
            }
        }

        // Update colors classes if some color has changed
        for (V v : rep.positiveDegreeColorClasses.get(color)) {
            if (!newMapping.get(rep.colorDegree.get(v)).equals(color)) {
                rep.colorClasses.get(color).remove(v);
                rep.colorClasses.get(newMapping.get(rep.colorDegree.get(v))).add(v);
                rep.coloring.replace(v, newMapping.get(rep.colorDegree.get(v)));
            }
        }
    }

    /**
     * Checks whether alpha is a valid surjective l-coloring for the given graph
     *
     * @param alpha the surjective l-coloring to be checked
     * @param graph the graph that is colored by alpha
     * @return whether alpha is a valid surjective l-coloring for the given graph
     */
    private boolean isAlphaConsistent(Coloring<V> alpha, Graph<V, E> graph)
    {
        /*
         * Check if the coloring is restricted to the graph, i.e. there are exactly as many vertices
         * in the graph as in the coloring
         */
        if (alpha.getColors().size() != graph.vertexSet().size()) {
            return false;
        }

        // check surjectivity, i.e. are the colors in the set {0, ..., maximumColor-1}
        // used?
        if (alpha.getColorClasses().size() != alpha.getNumberColors()) {
            return false;
        }

        for (V v : graph.vertexSet()) {
            // ensure that the key set of alpha and the vertex set of the graph actually
            // coincide
            if (!alpha.getColors().containsKey(v)) {
                return false;
            }

            // ensure the colors lie in in the set {0, ..., maximumColor-1}
            Integer currentColor = alpha.getColors().get(v);
            if (currentColor + 1 > alpha.getNumberColors() || currentColor < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a coloring such that all vertices have the same (zero) color.
     *
     * @param vertices the vertices that should be colored
     * @return the all-0 coloring
     */
    private static <V> Coloring<V> getDefaultAlpha(Set<V> vertices)
    {
        Map<V, Integer> alpha = new HashMap<>();
        for (V v : vertices) {
            alpha.put(v, 0);
        }
        return new ColoringImpl<>(alpha, 1);
    }

    /**
     * Returns a canonically sorted stack of all colors of alpha. It is important that alpha is
     * consistent.
     *
     * @param alpha the surjective l-coloring
     * @return a canonically sorted stack of all colors of alpha
     */
    private Deque<Integer> getSortedStack(Coloring<V> alpha)
    {
        int numberColors = alpha.getNumberColors();
        Deque<Integer> stack = new ArrayDeque<>(graph.vertexSet().size());
        for (int i = numberColors - 1; i >= 0; --i) {
            stack.push(i);
        }
        return stack;
    }

    private class ColoringRepresentation
    {
        /**
         * mapping from all colors to their classes
         */
        HashMap<Integer, List<V>> colorClasses;
        /**
         * mapping from color to their classes, whereby every vertex in the classes has
         * colorDegree(v) >= 1
         */
        HashMap<Integer, List<V>> positiveDegreeColorClasses;
        /**
         * mapping from color to its maximum color degree
         */
        int[] maxColorDegree;
        /**
         * mapping from color to its minimum color degree
         */
        int[] minColorDegree;
        /**
         * mapping from vertex to the vertex color degree (number of neighbors with different
         * colors)
         */
        Map<V, Integer> colorDegree;
        /**
         * The actual coloring
         */
        Map<V, Integer> coloring;
        /**
         * Last used color
         */
        int lastUsedColor;

        public ColoringRepresentation(Graph<V, E> graph, Coloring<V> alpha)
        {
            int n = graph.vertexSet().size();
            this.colorClasses = new HashMap<>(n);
            this.positiveDegreeColorClasses = new HashMap<>(n);
            this.maxColorDegree = new int[n];
            this.minColorDegree = new int[n];
            this.colorDegree = new HashMap<>();
            this.coloring = new HashMap<>();

            for (int c = 0; c < n; ++c) {
                colorClasses.put(c, new ArrayList<>());
                positiveDegreeColorClasses.put(c, new ArrayList<>());
            }
            for (V v : graph.vertexSet()) {
                colorClasses.get(alpha.getColors().get(v)).add(v);
                colorDegree.put(v, 0);
                coloring.put(v, alpha.getColors().get(v));
            }

            lastUsedColor = alpha.getNumberColors() - 1;
        }
    }

}
