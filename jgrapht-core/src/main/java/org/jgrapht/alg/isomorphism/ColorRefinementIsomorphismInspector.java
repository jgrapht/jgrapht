/*
 * (C) Copyright 2018-2021, by Christoph Grüne, Dennis Fischer and Contributors.
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
package org.jgrapht.alg.isomorphism;

import org.jgrapht.*;
import org.jgrapht.alg.color.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;

import java.util.*;

/**
 * Implementation of the color refinement algorithm isomorphism test using its feature of detecting
 * <a href="http://mathworld.wolfram.com/GraphIsomorphism.html">isomorphism between two graphs</a>
 * as described in C. Berkholz, P. Bonsma, and M. Grohe. Tight lower and upper bounds for the
 * complexity of canonical colour refinement. Theory of Computing
 * Systems,doi:10.1007/s00224-016-9686-0, 2016 (color refinement) The complexity of this algorithm
 * is O(|V| + |E| log |V|).
 *
 * @param <V> the type of the vertices
 * @param <E> the type of the edges
 *
 * @author Christoph Grüne
 * @author Dennis Fischer
 */
public class ColorRefinementIsomorphismInspector<V, E>
    implements
    IsomorphismInspector<V, E>
{

    /**
     * The input graphs
     */
    private Graph<V, E> graph1, graph2;

    /**
     * The isomorphism that is calculated by this color refinement isomorphism inspector
     */
    private IsomorphicGraphMapping<V, E> isomorphicGraphMapping;

    /**
     * contains whether the graphs are isomorphic or not. If we cannot decide whether they are
     * isomorphic the value will be not present.
     */
    private Boolean isIsomorphic;
    /**
     * contains whether the two graphs produce a discrete coloring. Then, we can decide whether the
     * graphs are isomorphic.
     */
    private boolean isColoringDiscrete;
    /**
     * contains whether the two graphs are forests. Forests can be identified to be isomorphic or
     * not.
     */
    private boolean isForest;

    /**
     * contains whether the isomorphism test is executed to ensure that every operation is defined
     * all the time
     */
    private boolean isomorphismTestExecuted;

    /**
     * Constructor for a isomorphism inspector based on color refinement. It checks whether
     * <code>graph1</code> and <code>graph2</code> are isomorphic.
     *
     * @param graph1 the first graph
     * @param graph2 the second graph
     */
    public ColorRefinementIsomorphismInspector(Graph<V, E> graph1, Graph<V, E> graph2)
    {

        GraphType type1 = graph1.getType();
        GraphType type2 = graph2.getType();
        if (type1.isAllowingMultipleEdges() || type2.isAllowingMultipleEdges()) {
            throw new IllegalArgumentException(
                "graphs with multiple (parallel) edges are not supported");
        }

        if (type1.isMixed() || type2.isMixed()) {
            throw new IllegalArgumentException("mixed graphs not supported");
        }

        if (type1.isUndirected() && type2.isDirected()
            || type1.isDirected() && type2.isUndirected())
        {
            throw new IllegalArgumentException(
                "can not match directed with " + "undirected graphs");
        }

        this.graph1 = graph1;
        this.graph2 = graph2;
        this.isomorphicGraphMapping = null;
        this.isColoringDiscrete = false;
        this.isomorphismTestExecuted = false;
        this.isForest = false;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IsomorphismUndecidableException if the isomorphism test was not executed and the
     *         inspector cannot decide whether the graphs are isomorphic
     */
    @Override
    public Iterator<GraphMapping<V, E>> getMappings()
    {
        if (!isomorphismTestExecuted) {
            isomorphismExists();
        }
        ArrayList<GraphMapping<V, E>> iteratorList = new ArrayList<>(1);
        if (isIsomorphic != null && isIsomorphic) {
            iteratorList.add(isomorphicGraphMapping);
        }
        return iteratorList.iterator();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IsomorphismUndecidableException if the inspector cannot decide whether the graphs are
     *         isomorphic
     */
    @Override
    public boolean isomorphismExists()
    {
        if (isomorphismTestExecuted) {
            if (isIsomorphic != null) {
                return isIsomorphic;
            } else {
                throw new IsomorphismUndecidableException();
            }
        }

        if (graph1 == graph2) {
            isomorphismTestExecuted = true;
            isIsomorphic = true;
            isomorphicGraphMapping = IsomorphicGraphMapping.identity(graph1);
            return isIsomorphic;
        }

        if (graph1.vertexSet().size() != graph2.vertexSet().size()) {
            isomorphismTestExecuted = true;
            isIsomorphic = false;
            return isIsomorphic;
        }

        Graph<DistinctGraphObject<V, V, E>, DistinctGraphObject<E, V, E>> graph =
            getDisjointGraphUnion(graph1, graph2);
        ColorRefinementAlgorithm<DistinctGraphObject<V, V, E>,
            DistinctGraphObject<E, V, E>> colorRefinementAlgorithm =
                new ColorRefinementAlgorithm<>(graph);

        // execute color refinement for graph
        Coloring<DistinctGraphObject<V, V, E>> coloring = colorRefinementAlgorithm.getColoring();

        isomorphismTestExecuted = true;

        isIsomorphic = coarseColoringAreEqual(coloring);

        if (isIsomorphic) {
            assert isomorphicGraphMapping.isValidIsomorphism();
        }

        return isIsomorphic;
    }

    /**
     * Returns whether the coarse colorings of the two given graphs are discrete. This method is
     * undefined if the colorings are not the same or graph1 == graph2.
     *
     * @return if both colorings are discrete.
     *
     * @throws IsomorphismUndecidableException if the isomorphism test was not executed and the
     *         inspector cannot decide whether the graphs are isomorphic
     */
    boolean isColoringDiscrete()
    {
        if (!isomorphismTestExecuted) {
            isomorphismExists();
        }
        return isColoringDiscrete;
    }

    /**
     * Returns whether the two given graphs are forests. This method is undefined if an isomorphism
     * exists and the coloring is discrete, or graph1 == graph2.
     *
     * @return if both graphs are forests.
     *
     * @throws IsomorphismUndecidableException if the isomorphism test was not executed and the
     *         inspector cannot decide whether the graphs are isomorphic
     */
    boolean isForest()
    {
        if (!isomorphismTestExecuted) {
            isomorphismExists();
        }
        return isForest;
    }

    /**
     * Checks whether two coarse colorings are equal. Furthermore, it sets
     * <code>isColoringDiscrete</code> to true iff the colorings are discrete.
     *
     * @param coloring the coarse coloring of the union graph
     * @return if the given coarse colorings are equal
     */
    private boolean coarseColoringAreEqual(Coloring<DistinctGraphObject<V, V, E>> coloring)
        throws IsomorphismUndecidableException
    {
        Pair<Coloring<V>, Coloring<V>> coloringPair = splitColoring(coloring);
        Coloring<V> coloring1 = coloringPair.getFirst();
        Coloring<V> coloring2 = coloringPair.getSecond();
        if (coloring1.getNumberColors() != coloring2.getNumberColors()) {
            return false;
        }

        List<Set<V>> colorClasses1 = coloring1.getColorClasses();
        List<Set<V>> colorClasses2 = coloring2.getColorClasses();

        if (colorClasses1.size() != colorClasses2.size()) {
            return false;
        }

        sortColorClasses(colorClasses1, coloring1);
        sortColorClasses(colorClasses2, coloring2);

        Iterator<Set<V>> it1 = colorClasses1.iterator();
        Iterator<Set<V>> it2 = colorClasses2.iterator();

        // check the color classes
        while (it1.hasNext() && it2.hasNext()) {
            Set<V> cur1 = it1.next();
            Set<V> cur2 = it2.next();

            // check if the size for the current color class are the same for both graphs.
            if (cur1.size() != cur2.size()) {
                return false;
            }
            // safety check whether the color class is not empty.
            if (cur1.iterator().hasNext()) {
                // check if the color are not the same (works as colors are integers).
                if (!coloring1
                    .getColors().get(cur1.iterator().next())
                    .equals(coloring2.getColors().get(cur2.iterator().next())))
                {
                    // colors are not the same -> graphs are not isomorphic.
                    return false;
                }
            }
        }

        // no more color classes for both colorings, that is, the graphs have the same coloring.
        if (!it1.hasNext() && !it2.hasNext()) {

            /*
             * Check if the colorings are discrete, that is, the color mapping is injective. Check
             * if the graphs are forests. In both cases color refinement can decide if there is an
             * isomorphism.
             */
            if (coloring1.getColorClasses().size() == graph1.vertexSet().size()
                && coloring2.getColorClasses().size() == graph2.vertexSet().size())
            {
                isColoringDiscrete = true;
                calculateGraphMapping(coloring1, coloring2);
                return true;
            } else if (GraphTests.isForest(graph1) && GraphTests.isForest(graph2)) {
                isForest = true;
                calculateGraphMapping(coloring1, coloring2);
                return true;
            }
            isIsomorphic = null;
            throw new IsomorphismUndecidableException(
                "Color refinement cannot decide whether the two graphs are isomorphic or not.");
        } else {
            return false;
        }
    }

    /**
     * Splits up the coloring of the union graph into the two colorings of the original graphs
     *
     * @param coloring the coloring to split up
     * @return the two colorings of the original graphs
     */
    private Pair<Coloring<V>, Coloring<V>> splitColoring(
        Coloring<DistinctGraphObject<V, V, E>> coloring)
    {
        Map<V, Integer> col1 = new HashMap<>();
        Map<V, Integer> col2 = new HashMap<>();
        int index = 0;
        for (Set<DistinctGraphObject<V, V, E>> set1 : coloring.getColorClasses()) {
            for (DistinctGraphObject<V, V, E> entry : set1) {
                if (entry.getGraph() == graph1) {
                    col1.put(entry.getObject(), index);
                } else {
                    col2.put(entry.getObject(), index);
                }
            }
            index++;
        }
        Coloring<V> coloring1 = new VertexColoringAlgorithm.ColoringImpl<>(col1, col1.size());
        Coloring<V> coloring2 = new VertexColoringAlgorithm.ColoringImpl<>(col2, col2.size());
        return new Pair<>(coloring1, coloring2);
    }

    /**
     * Sorts a list of color classes by the size and the color (integer representation of the color)
     * and
     *
     * @param colorClasses the list of the color classes
     * @param coloring the coloring
     */
    private void sortColorClasses(List<Set<V>> colorClasses, Coloring<V> coloring)
    {
        colorClasses.sort((o1, o2) -> {
            if (o1.size() == o2.size()) {
                Iterator<V> it1 = o1.iterator();
                Iterator<V> it2 = o2.iterator();
                if (!it1.hasNext() || !it2.hasNext()) {
                    return Integer.compare(o1.size(), o2.size());
                }
                return coloring
                    .getColors().get(it1.next()).compareTo(coloring.getColors().get(it2.next()));
            }
            return Integer.compare(o1.size(), o2.size());
        });
    }

    /**
     * calculates the graph isomorphism as GraphMapping and assigns it to attribute
     * <code>isomorphicGraphMapping</code>
     *
     * @param coloring1 the discrete vertex coloring of graph1
     * @param coloring2 the discrete vertex coloring of graph2
     */
    private void calculateGraphMapping(Coloring<V> coloring1, Coloring<V> coloring2)
    {
        GraphOrdering<V, E> graphOrdering1 = new GraphOrdering<>(graph1);
        GraphOrdering<V, E> graphOrdering2 = new GraphOrdering<>(graph2);

        int[] core1 = new int[graph1.vertexSet().size()];
        int[] core2 = new int[graph2.vertexSet().size()];

        Iterator<Set<V>> setIterator1 = coloring1.getColorClasses().iterator();
        Iterator<Set<V>> setIterator2 = coloring2.getColorClasses().iterator();

        // we only have to check one iterator as the color classes have the same size
        while (setIterator1.hasNext()) {
            Iterator<V> vertexIterator1 = setIterator1.next().iterator();
            Iterator<V> vertexIterator2 = setIterator2.next().iterator();

            while (vertexIterator1.hasNext()) {
                V v1 = vertexIterator1.next();
                V v2 = vertexIterator2.next();

                int numberOfV1 = graphOrdering1.getVertexNumber(v1);
                int numberOfV2 = graphOrdering2.getVertexNumber(v2);

                core1[numberOfV1] = numberOfV2;
                core2[numberOfV2] = numberOfV1;
            }
        }

        isomorphicGraphMapping =
            new IsomorphicGraphMapping<>(graphOrdering1, graphOrdering2, core1, core2);
    }

    /**
     * Calculates and returns a disjoint graph union of <code>graph1</code> and <code>graph2</code>
     *
     * @param graph1 the first graph of the disjoint union
     * @param graph2 the second graph of the disjoint union
     * @return the disjoint union of the two graphs
     */
    private Graph<DistinctGraphObject<V, V, E>, DistinctGraphObject<E, V, E>> getDisjointGraphUnion(
        Graph<V, E> graph1, Graph<V, E> graph2)
    {
        return new AsGraphUnion<>(getDistinctObjectGraph(graph1), getDistinctObjectGraph(graph2));
    }

    private Graph<DistinctGraphObject<V, V, E>,
        DistinctGraphObject<E, V, E>> getDistinctObjectGraph(Graph<V, E> graph)
    {
        Graph<DistinctGraphObject<V, V, E>,
            DistinctGraphObject<E, V, E>> transformedGraph = GraphTypeBuilder
                .<DistinctGraphObject<V, V, E>,
                    DistinctGraphObject<E, V, E>> forGraphType(graph.getType())
                .buildGraph();

        for (V vertex : graph.vertexSet()) {
            transformedGraph.addVertex(new DistinctGraphObject<>(vertex, graph));
        }
        for (E edge : graph.edgeSet()) {
            transformedGraph
                .addEdge(
                    new DistinctGraphObject<>(graph.getEdgeSource(edge), graph),
                    new DistinctGraphObject<>(graph.getEdgeTarget(edge), graph),
                    new DistinctGraphObject<>(edge, graph));
        }

        return transformedGraph;
    }

    /**
     * Representation of a graph vertex in the disjoint union
     *
     * @param <V> the vertex type of the graph
     * @param <E> the edge type of the graph
     */
    private static class DistinctGraphObject<T, V, E>
    {

        private Pair<T, Graph<V, E>> pair;

        private DistinctGraphObject(T object, Graph<V, E> graph)
        {
            this.pair = Pair.of(object, graph);
        }

        public T getObject()
        {
            return pair.getFirst();
        }

        public Graph<V, E> getGraph()
        {
            return pair.getSecond();
        }

        @Override
        public String toString()
        {
            return pair.toString();
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            else if (!(o instanceof DistinctGraphObject))
                return false;

            @SuppressWarnings("unchecked") DistinctGraphObject<T, V, E> other =
                (DistinctGraphObject<T, V, E>) o;
            return Objects.equals(getObject(), other.getObject()) && getGraph() == other.getGraph();
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(getObject(), System.identityHashCode(getGraph()));
        }

        public static <T, V, E> DistinctGraphObject<T, V, E> of(T object, Graph<V, E> graph)
        {
            return new DistinctGraphObject<>(object, graph);
        }
    }
}
