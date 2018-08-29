/*
 * (C) Copyright 2018-2018, by Christoph Grüne, Daniel Mock, Oliver Feith and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht.alg.color;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm;

import java.util.*;
import java.util.stream.Collectors;

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
    implements VertexColoringAlgorithm<V>
{
    private final Graph<V, E> graph;
    private Coloring<V> alpha;
    private Integer k;

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
        int n = graph.vertexSet().size();

        // number of colors used
        k = alpha.getNumberColors() - 1;

        // mapping from all colors to their classes
        HashMap<Integer, List<V>> colorClasses = new HashMap<>(n);
        // mapping from color to their classes, whereby every vertex in the classes has
        // colorDegree(v) >= 1
        HashMap<Integer, List<V>> positiveDegreeColorClasses = new HashMap<>(n);

        // mapping from color to its maximum color degree
        int[] maxColorDegree = new int[n];
        // mapping from color to its minimum color degree
        int[] minColorDegree = new int[n];
        // mapping from vertex to the vertex color degree (number of neighbors with different
        // colors) 
        Map<V, Integer> colorDegree = new HashMap<>();

        // init coloring
        Map<V, Integer> coloring = new HashMap<>();
        for (int c = 0; c < n; ++c) {
            colorClasses.put(c, new ArrayList<>());
            positiveDegreeColorClasses.put(c, new ArrayList<>());
        }
        for (V v : graph.vertexSet()) {
            colorClasses.get(alpha.getColors().get(v)).add(v);
            colorDegree.put(v, 0);
            coloring.put(v, alpha.getColors().get(v));
        }

        // get an ascendingly sorted stack of all colors that are predefined by alpha
        Deque<Integer> refineStack = getSortedStack(alpha);

        while (!refineStack.isEmpty()) {
            Integer currentColor = refineStack.pop();

            // list of all colors that have at least one vertex with colorDegree >= 1
            Set<Integer> adjacentColors = new LinkedHashSet<>(n);

            // calculate number of neighbours of v of current color, and calculate maximum and
            // minimum degree of all colors
            calculateColorDegree(
                currentColor, coloring, colorClasses, positiveDegreeColorClasses, adjacentColors,
                maxColorDegree, minColorDegree, colorDegree);

            // calculate new partition of the colors and update the color classes accordingly
            List<Integer> colorsToBeSplit =
                adjacentColors.stream().filter(c -> minColorDegree[c] < maxColorDegree[c]).collect(
                    Collectors.toCollection(ArrayList::new));
            colorsToBeSplit.sort(Comparator.comparingInt(o -> o)); // canonical order
            colorsToBeSplit.forEach(
                color -> splitUpColor(
                    color, coloring, colorClasses, positiveDegreeColorClasses, refineStack,
                    maxColorDegree, minColorDegree, colorDegree));

            // reset attributes for new iteration such that the invariants are still correct
            adjacentColors.stream().forEach(c -> {
                for (V v : positiveDegreeColorClasses.get(c)) {
                    colorDegree.put(v, 0);
                }
                maxColorDegree[c] = 0;
                positiveDegreeColorClasses.put(c, new ArrayList<>());
            });
        }

        return new ColoringImpl<>(coloring, coloring.size());
    }

    /**
     * Helper method that calculates the color degree for every vertex and the maximum and minimum
     * color degree for every color.
     *
     * @param refiningColor refining color (current color in the iteration)
     * @param coloring the color mapping
     * @param colorClasses the mapping from all colors to their classes
     * @param positiveDegreeColorClasses the mapping from all colors to their classes with
     *        colorDegree(v) >= 1
     * @param adjacentColors the list of all colors that have at least one vertex with colorDegree
     *        >= 1
     * @param maxColorDegree the mapping from color to its maximum color degree
     * @param minColorDegree the mapping from color to its minimum color degree
     * @param colorDegree the mapping from vertex to the color degree (number of neighbors with
     *        different colors) of the vertex
     */
    private void calculateColorDegree(
        Integer refiningColor, Map<V, Integer> coloring, HashMap<Integer, List<V>> colorClasses,
        HashMap<Integer, List<V>> positiveDegreeColorClasses, Set<Integer> adjacentColors,
        int[] maxColorDegree, int[] minColorDegree, Map<V, Integer> colorDegree)
    {
        for (V v : colorClasses.get(refiningColor)) {
            Set<V> inNeighborhood = graph
                .incomingEdgesOf(v).stream().map(e -> Graphs.getOppositeVertex(graph, e, v))
                .collect(Collectors.toSet());

            for (V w : inNeighborhood) {
                colorDegree.put(w, colorDegree.get(w) + 1); // increase color degree

                // Add vertex to positiveDegreeColorClasses if color degree of exactly 1 is reached.
                if (colorDegree.get(w) == 1) {
                    positiveDegreeColorClasses.get(coloring.get(w)).add(w);
                }
                // Add vertex to adjacentColors only if it is not already contained in
                // adjacentColors.
                if (!adjacentColors.contains(coloring.get(w))) {
                    adjacentColors.add(coloring.get(w));
                }
                // Update maxColorDegree for color(w) if maximum color degree has increased.
                if (colorDegree.get(w) > maxColorDegree[coloring.get(w)]) {
                    maxColorDegree[coloring.get(w)] = colorDegree.get(w);
                }
            }
        }

        /*
         * Go through all colors, which have at least one vertex with colorDegree >= 1, to update
         * minColorDegree.
         */
        for (Integer c : adjacentColors) {
            // if there is a vertex with colorDegree(v) = 0 < 1, set minimum color degree to 0
            if (colorClasses.get(c).size() != positiveDegreeColorClasses.get(c).size()) {
                minColorDegree[c] = 0;
            } else {
                minColorDegree[c] = maxColorDegree[c];
                for (V v : positiveDegreeColorClasses.get(c)) {
                    if (colorDegree.get(v) < minColorDegree[c]) {
                        minColorDegree[c] = colorDegree.get(v);
                    }
                }
            }
        }
    }

    /**
     * Helper method for splitting up a color.
     *
     * @param color the color to split the color class for
     * @param coloring the color mapping
     * @param colorClasses the mapping from all colors to their classes
     * @param positiveDegreeColorClasses the mapping from all colors to their classes with
     *        colorDegree(v) >= 1
     * @param refineStack the stack containing all colors that have to be refined
     * @param maxColorDegree the mapping from color to its maximum color degree
     * @param minColorDegree the mapping from color to its minimum color degree
     * @param colorDegree the mapping from vertex to the color degree (number of neighbors with
     *        different colors) of the vertex
     */
    private void splitUpColor(
        Integer color, Map<V, Integer> coloring, HashMap<Integer, List<V>> colorClasses,
        HashMap<Integer, List<V>> positiveDegreeColorClasses, Deque<Integer> refineStack,
        int[] maxColorDegree, int[] minColorDegree, Map<V, Integer> colorDegree)
    {
        /*
         * Initialize and calculate numColorDegree (mapping from the color degree to the number of
         * vertices with that color degree).
         */
        Map<Integer, Integer> numColorDegree = new HashMap<>();
        for (int i = 1; i <= maxColorDegree[color]; ++i) {
            numColorDegree.put(i, 0);
        }
        numColorDegree
            .put(0, colorClasses.get(color).size() - positiveDegreeColorClasses.get(color).size());
        for (V v : positiveDegreeColorClasses.get(color)) {
            numColorDegree.put(colorDegree.get(v), numColorDegree.get(colorDegree.get(v)) + 1);
        }

        /*
         * Add new colors to the refine stack, corresponding to the calculations before calculate
         * the new mapping. The result is a mapping from color degrees that occur in S to newly
         * introduced colors or to the provided color.
         */
        Map<Integer, Integer> newMapping = new HashMap<>();
        addColorsToRefineStackAndComputeNewMapping(
            color, maxColorDegree, minColorDegree, refineStack, numColorDegree, newMapping);

        /*
         * Update colors classes if some color has changed
         */
        for (V v : positiveDegreeColorClasses.get(color)) {
            if (!newMapping.get(colorDegree.get(v)).equals(color)) { 
                colorClasses.get(color).remove(v); 
                colorClasses.get(newMapping.get(colorDegree.get(v))).add(v);
                coloring.replace(v, newMapping.get(colorDegree.get(v))); 
            }
        }
    }

    /**
     * Helper method which adds all colors which have to be refined further to refineStack and
     * constructs the new mapping.
     *
     * @param currentColor the current color
     * @param maxColorDegree the mapping from color to its maximum color degree
     * @param minColorDegree the mapping from color to its minimum color degree
     * @param refineStack the stack refineStack that stores all colors to be refined
     * @param numColorDegree mapping from the color degree to the number of vertices with that color
     *        degree
     * @param result mapping from color degrees that occur in S to newly introduced colors or to
     *        color s
     */
    private void addColorsToRefineStackAndComputeNewMapping(
        Integer currentColor, int[] maxColorDegree, int[] minColorDegree,
        Deque<Integer> refineStack, Map<Integer, Integer> numColorDegree,
        Map<Integer, Integer> result)
    {
        boolean isCurrentColorInStack = refineStack.contains(currentColor);

        /*
         * Helper variable storing the index with the maximum number of vertices with the
         * corresponding color degree
         */
        int maxColorDegreeIndex = 0;
        for (int i = 1; i <= maxColorDegree[currentColor]; ++i) {
            if (numColorDegree.get(i) > numColorDegree.get(maxColorDegreeIndex)) {
                maxColorDegreeIndex = i;
            }
        }

        // go through all indices (color degrees) of numColorDegree
        int currentMaxColorDegree = maxColorDegree[currentColor];
        for (int i = 0; i <= currentMaxColorDegree; ++i) {
            if (numColorDegree.get(i) >= 1) {
                if (i == minColorDegree[currentColor]) {
                    // colors with minimum color degree keep current color
                    result.put(i, currentColor);

                    /*
                     * Push current color on the stack if it is not in the stack and i is not the
                     * index with the maximum number of vertices with the corresponding color degree
                     */
                    if (!isCurrentColorInStack && maxColorDegreeIndex != i) {
                        refineStack.push(result.get(i));
                    }
                } else {
                    // add a new color so we have to increase the number of colors
                    k++;
                    result.put(i, k);

                    /*
                     * Push current color on the stack if it is in the stack and i is not the index
                     * with the maximum number of vertices with the corresponding color degree
                     */
                    if (isCurrentColorInStack || i != maxColorDegreeIndex) {
                        refineStack.push(result.get(i));
                    }
                }
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

        // check surjectivity, i.e. are the colors in the set {1, ..., maximumColor} used?
        if (alpha.getColorClasses().size() != alpha.getNumberColors()) {
            return false;
        }

        for (V v : graph.vertexSet()) {
            // ensure that the key set of alpha and the vertex set of the graph actually coincide
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
     * Returns a coloring such that all vertices have color 0.
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
        for (int i = numberColors-1; i >= 0; --i) {
            stack.push(i);
        }
        return stack;
    }

}
