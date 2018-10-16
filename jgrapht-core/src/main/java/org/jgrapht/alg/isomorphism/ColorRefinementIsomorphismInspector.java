/*
 * (C) Copyright 2018-2018, by Christoph Grüne, Dennis Fischer and Contributors.
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
package org.jgrapht.alg.isomorphism;

import org.jgrapht.*;
import org.jgrapht.alg.color.ColorRefinementAlgorithm;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm.Coloring;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.AsGraphUnion;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.util.*;

/**
 * Implementation of the color refinement algorithm isomorphism test using its feature of detecting
 * <a href="http://mathworld.wolfram.com/GraphIsomorphism.html">isomorphism between two graphs</a>
 * as described in
 * C. Berkholz, P. Bonsma, and M. Grohe.  Tight lower and upper bounds for the complexity of canonical
 * colour refinement. Theory of Computing Systems,doi:10.1007/s00224-016-9686-0, 2016 (color refinement)
 * The complexity of this algorithm is O(|V| + |E| log |V|).
 *
 * @param <V> the type of the vertices
 * @param <E> the type of the edges
 *
 * @author Christoph Grüne
 * @author Dennis Fischer
 */
public class ColorRefinementIsomorphismInspector<V, E> implements IsomorphismInspector<V, E> {

    /**
     * The input graphs
     */
    private Graph<V, E> graph1, graph2;

    /**
     * The isomorphism that is calculated by this color refinement isomorphism inspector
     */
    private GraphMapping<V, E> isomorphicGraphMapping;

    /**
     * contains whether the graphs are isomorphic or not.
     * If we cannot decide whether they are isomorphic the value will be not present.
     */
    private Boolean isIsomorphic;
    /**
     * contains whether the two graphs produce a discrete coloring.
     * Then, we can decide whether the graphs are isomorphic.
     */
    private boolean isColoringDiscrete;
    /**
     * contains whether the two graphs are forests. Forests can be identified to be isomorphic or not.
     */
    private boolean isForest;

    /**
     * contains whether the isomorphism test is executed to ensure that every operation is defined all the time
     */
    private boolean isomorphismTestExecuted;

    /**
     * Constructor for a isomorphism inspector based on color refinement. It checks whether <code>graph1</code> and
     * <code>graph2</code> are isomorphic.
     *
     * @param graph1 the first graph
     * @param graph2 the second graph
     */
    public ColorRefinementIsomorphismInspector(Graph<V, E> graph1, Graph<V, E> graph2) {

        GraphType type1 = graph1.getType();
        GraphType type2 = graph2.getType();
        if (type1.isAllowingMultipleEdges() || type2.isAllowingMultipleEdges()) {
            throw new IllegalArgumentException("graphs with multiple (parallel) edges are not supported");
        }

        if (type1.isMixed() || type2.isMixed()) {
            throw new IllegalArgumentException("mixed graphs not supported");
        }

        if (type1.isUndirected() && type2.isDirected() || type1.isDirected() && type2.isUndirected()) {
            throw new IllegalArgumentException("can not match directed with " + "undirected graphs");
        }

        this.graph1 = graph1;
        this.graph2 = graph2;
        this.isomorphicGraphMapping = null;
        this.isColoringDiscrete = false;
        this.isomorphismTestExecuted = false;
        this.isForest = false;
    }

    /**
     * Returns whether <code>isomorphism</code> is an isomorphism between <code>graph1</code> and <code>graph2</code>
     *
     * @param graph1 the first graph
     * @param graph2 the second graph
     * @param isomorphism the isomorphim to test
     * @return whether <code>isomorphism</code> is an isomorphism between <code>graph1</code> and <code>graph2</code>
     */
    public boolean assertIsomorphism(Graph<V, E> graph1, Graph<V, E> graph2, IsomorphicGraphMapping<V, E> isomorphism) {
        if(graph1 == graph2) {
            return true;
        }

        if(graph1.vertexSet().size() != graph2.vertexSet().size()) {
            return false;
        }
        if (graph1.edgeSet().size() != graph2.edgeSet().size()) {
            return false;
        }

        for(V vertex : graph1.vertexSet()) {
            V vertexCorrespondence = isomorphism.getVertexCorrespondence(vertex, true);
            if(vertexCorrespondence == null) {
                return false;
            }

            if(graph1.inDegreeOf(vertex) != graph2.inDegreeOf(vertexCorrespondence)) {
                return false;
            }
            if(graph1.outDegreeOf(vertex) != graph2.outDegreeOf(vertexCorrespondence)) {
                return false;
            }

            for(E edge : graph1.outgoingEdgesOf(vertex)) {
                V neighbor = Graphs.getOppositeVertex(graph1, edge, vertex);

                V neighborCorrespondence = isomorphism.getVertexCorrespondence(neighbor, true);
                if(neighborCorrespondence == null) {
                    return false;
                }

                if(!graph2.containsEdge(vertexCorrespondence, neighborCorrespondence)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IsomorphismUndecidableException if the isomorphism test was not executed and the inspector cannot decide whether the graphs are isomorphic
     */
    @Override
    public Iterator<GraphMapping<V, E>> getMappings() {
        if(!isomorphismTestExecuted) {
            isomorphismExists();
        }
        ArrayList<GraphMapping<V, E>> iteratorList = new ArrayList<>(1);
        if(isIsomorphic != null && isIsomorphic) {
            iteratorList.add(isomorphicGraphMapping);
        }
        return iteratorList.iterator();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IsomorphismUndecidableException if the inspector cannot decide whether the graphs are isomorphic
     */
    @Override
    public boolean isomorphismExists() {
        if(isomorphismTestExecuted) {
            if(isIsomorphic != null) {
                return isIsomorphic;
            } else {
                throw new IsomorphismUndecidableException();
            }
        }

        if(graph1.vertexSet().size() != graph2.vertexSet().size()) {
            return false;
        }

        Graph<DistinctGraphVertex<V, E>, DistinctGraphEdge<V, E>> graph = getDisjointGraphUnion(graph1, graph2);
        ColorRefinementAlgorithm<DistinctGraphVertex<V, E>, DistinctGraphEdge<V, E>> colorRefinementAlgorithm =
                new ColorRefinementAlgorithm<>(graph);

        // execute color refinement for graph
        Coloring<DistinctGraphVertex<V, E>> coloring = colorRefinementAlgorithm.getColoring();

        isomorphismTestExecuted = true;

        isIsomorphic = coarseColoringAreEqual(coloring);

        if(isIsomorphic && !assertIsomorphism(graph1, graph2, (IsomorphicGraphMapping<V, E>) isomorphicGraphMapping)) {
            throw new IllegalStateException("Either there is no isomorphism or the mapping is incorrect!");
        }

        return isIsomorphic;
    }

    /**
     * Returns whether the coarse colorings of the two given graphs are discrete if the colorings are the same.
     * Otherwise, we do not computed if the coloring is discrete, hence, this method is undefined.
     *
     * @return if the both colorings are discrete if they are the same on both graphs.
     *
     * @throws IsomorphismUndecidableException if the isomorphism test was not executed and the inspector cannot decide whether the graphs are isomorphic
     */
    public boolean isColoringDiscrete() {
        if(!isomorphismTestExecuted) {
            isomorphismExists();
        }
        return isColoringDiscrete;
    }

    /**
     * Returns whether the two given graphs are forests if an isomorphism exists and the coloring is not discrete,
     * because if the coloring is discrete, it does not have to be calculated if the graphs are forests.
     * Otherwise, we do not compute if the graphs are forests, hence, this method is undefined.
     *
     * @return if the both colorings are discrete if they are the same on both graphs.
     *
     * @throws IsomorphismUndecidableException if the isomorphism test was not executed and the inspector cannot decide whether the graphs are isomorphic
     */
    public boolean isForest() {
        if(!isomorphismTestExecuted) {
            isomorphismExists();
        }
        return isForest;
    }

    /**
     * Checks whether two coarse colorings are equal. Furthermore, it sets <code>isColoringDiscrete</code> to true iff the colorings are discrete.
     *
     * @param coloring the coarse coloring of the union graph
     * @return if the given coarse colorings are equal
     */
    private boolean coarseColoringAreEqual(Coloring<DistinctGraphVertex<V, E>> coloring) throws IsomorphismUndecidableException {
        Pair<Coloring<V>, Coloring<V>> coloringPair = splitColoring(coloring);
        Coloring<V> coloring1 = coloringPair.getFirst();
        Coloring<V> coloring2 = coloringPair.getSecond();
        if (coloring1.getNumberColors() != coloring2.getNumberColors()) {
            return false;
        }

        List<Set<V>> colorClasses1 = coloring1.getColorClasses();
        List<Set<V>> colorClasses2 = coloring2.getColorClasses();

        if(colorClasses1.size() != colorClasses2.size()) {
            return false;
        }

        sortColorClasses(colorClasses1, coloring1);
        sortColorClasses(colorClasses2, coloring2);

        Iterator<Set<V>> it1 = colorClasses1.iterator();
        Iterator<Set<V>> it2 = colorClasses2.iterator();

        // check the color classes
        while(it1.hasNext() && it2.hasNext()) {
            Set<V> cur1 = it1.next();
            Set<V> cur2 = it2.next();

            // check if the size for the current color class are the same for both graphs.
            if(cur1.size() != cur2.size()) {
                return false;
            }
            // safety check whether the color class is not empty.
            if(cur1.iterator().hasNext()) {
                // check if the color are not the same (works as colors are integers).
                if(!coloring1.getColors().get(cur1.iterator().next()).equals(coloring2.getColors().get(cur2.iterator().next()))) {
                    // colors are not the same -> graphs are not isomorphic.
                    return false;
                }
            }
        }

        // no more color classes for both colorings, that is, the graphs have the same coloring.
        if(!it1.hasNext() && !it2.hasNext()) {

            /*
             * Check if the colorings are discrete, that is, the color mapping is injective.
             * Check if the graphs are forests.
             * In both cases color refinement can decide if there is an isomorphism.
             */
            if(coloring1.getColorClasses().size() == graph1.vertexSet().size() && coloring2.getColorClasses().size() == graph2.vertexSet().size()) {
                isColoringDiscrete = true;
                calculateGraphMapping(coloring1, coloring2);
                return true;
            } else if(GraphTests.isForest(graph1) && GraphTests.isForest(graph2)) {
                isForest = true;
                calculateGraphMapping(coloring1, coloring2);
                return true;
            }
            isIsomorphic = null;
            throw new IsomorphismUndecidableException("Color refinement cannot decide whether the two graphs are isomorphic or not.");
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
     private Pair<Coloring<V>, Coloring<V>> splitColoring(Coloring<DistinctGraphVertex<V, E>> coloring) {
        Map<V, Integer> col1 = new HashMap<>();
        Map<V, Integer> col2 = new HashMap<>();
        int index = 0;
        for(Set<DistinctGraphVertex<V, E>> set1 : coloring.getColorClasses()) {
            for (DistinctGraphVertex<V, E> entry : set1) {
                if (entry.getGraph() == graph1) {
                    col1.put(entry.getVertex(), index);
                } else {
                    col2.put(entry.getVertex(), index);
                }
            }
            index++;
        }
        Coloring<V> coloring1 =  new VertexColoringAlgorithm.ColoringImpl<>(col1, col1.size());
        Coloring<V> coloring2 =  new VertexColoringAlgorithm.ColoringImpl<>(col2, col2.size());
        return new Pair<>(coloring1, coloring2);
     }

    /**
     * Sorts a list of color classes by the size and the color (integer representation of the color) and
     *
     * @param colorClasses the list of the color classes
     * @param coloring the coloring
     */
    private void sortColorClasses(List<Set<V>> colorClasses, Coloring<V> coloring) {
        colorClasses.sort((o1, o2) -> {
            if(o1.size() == o2.size()) {
                Iterator it1 = o1.iterator();
                Iterator it2 = o2.iterator();
                if(!it1.hasNext() || !it2.hasNext()) {
                    return Integer.compare(o1.size(), o2.size());
                }
                return coloring.getColors().get(it1.next()).compareTo(coloring.getColors().get(it2.next()));
            }
            return Integer.compare(o1.size(), o2.size());
        });
    }

    /**
     * calculates the graph isomorphism as GraphMapping and assigns it to attribute <code>isomorphicGraphMapping</code>
     *
     * @param coloring1 the discrete vertex coloring of graph1
     * @param coloring2 the discrete vertex coloring of graph2
     */
    private void calculateGraphMapping(Coloring<V> coloring1, Coloring<V> coloring2) {
        GraphOrdering<V, E> graphOrdering1 = new GraphOrdering<>(graph1);
        GraphOrdering<V, E> graphOrdering2 = new GraphOrdering<>(graph2);

        int[] core1 = new int[graph1.vertexSet().size()];
        int[] core2 = new int[graph2.vertexSet().size()];

        Iterator<Set<V>> setIterator1 = coloring1.getColorClasses().iterator();
        Iterator<Set<V>> setIterator2 = coloring2.getColorClasses().iterator();

        // we only have to check one iterator as the color classes have the same size
        while(setIterator1.hasNext()) {
            Iterator<V> vertexIterator1 = setIterator1.next().iterator();
            Iterator<V> vertexIterator2 = setIterator2.next().iterator();

            while(vertexIterator1.hasNext()) {
                V v1 = vertexIterator1.next();
                V v2 = vertexIterator2.next();

                int numberOfV1 = graphOrdering1.getVertexNumber(v1);
                int numberOfV2 = graphOrdering2.getVertexNumber(v2);

                core1[numberOfV1] = numberOfV2;
                core2[numberOfV2] = numberOfV1;
            }
        }

        isomorphicGraphMapping = new IsomorphicGraphMapping<>(graphOrdering1, graphOrdering2, core1, core2);
    }

    /**
     * Calculates and returns a disjoint graph union of <code>graph1</code> and <code>graph2</code>
     *
     * @param graph1 the first graph of the disjoint union
     * @param graph2 the second graph of the disjoint union
     * @return the disjoint union of the two graphs
     */
    private Graph<DistinctGraphVertex<V, E>, DistinctGraphEdge<V, E>> getDisjointGraphUnion(Graph<V, E> graph1, Graph<V, E> graph2) {
        return new AsGraphUnion<>(getDistinctObjectGraph(graph1), getDistinctObjectGraph(graph2));
    }

    private Graph<DistinctGraphVertex<V, E>, DistinctGraphEdge<V, E>> getDistinctObjectGraph(Graph<V, E> graph) {
        Graph<DistinctGraphVertex<V, E>, DistinctGraphEdge<V, E>> transformedGraph =
                GraphTypeBuilder.<DistinctGraphVertex<V, E>, DistinctGraphEdge<V, E>>forGraphType(graph.getType()).buildGraph();

        for(V vertex : graph.vertexSet()) {
            transformedGraph.addVertex(new DistinctGraphVertex<>(vertex, graph));
        }
        for(E edge : graph.edgeSet()) {
            transformedGraph.addEdge(
                    new DistinctGraphVertex<>(graph.getEdgeSource(edge), graph),
                    new DistinctGraphVertex<>(graph.getEdgeTarget(edge), graph),
                    new DistinctGraphEdge<>(edge, graph)
            );
        }

        return transformedGraph;
    }

    /**
     * Representation of a graph vertex in the disjoint union
     *
     * @param <V> the vertex type of the graph
     * @param <E> the edge type of the graph
     */
    private static class DistinctGraphVertex<V, E> {

        private Pair<V, Graph<V, E>> pair;

        private DistinctGraphVertex(V vertex, Graph<V, E> graph) {
            this.pair = Pair.of(vertex, graph);
        }

        public V getVertex() {
            return pair.getFirst();
        }

        public Graph<V, E> getGraph() {
            return pair.getSecond();
        }


        @Override
        public String toString()
        {
            return pair.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            else if (!(o instanceof DistinctGraphVertex))
                return false;

            @SuppressWarnings("unchecked") DistinctGraphVertex<V, Graph<V, E>> other = (DistinctGraphVertex<V, Graph<V, E>>) o;
            return Objects.equals(getVertex(), other.getVertex()) && getGraph() == other.getGraph();
        }

        @Override
        public int hashCode() {
            return pair.hashCode();
        }

        public static <A, B> DistinctGraphVertex<A, B> of(A a, Graph<A, B> b) {
            return new DistinctGraphVertex<>(a, b);
        }

    }

    /**
     * Representation of a graph edge in the disjoint union
     *
     * @param <V> the vertex type of the graph
     * @param <E> the edge type of the graph
     */
    private static class DistinctGraphEdge<V, E> {

        private Pair<E, Graph<V, E>> pair;

        private DistinctGraphEdge(E edge, Graph<V, E> graph) {
            this.pair = Pair.of(edge, graph);
        }

        public E getEdge() {
            return pair.getFirst();
        }

        public Graph<V, E> getGraph() {
            return pair.getSecond();
        }


        @Override
        public String toString()
        {
            return pair.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            else if (!(o instanceof DistinctGraphEdge))
                return false;

            @SuppressWarnings("unchecked") DistinctGraphEdge<E, Graph<V, E>> other = (DistinctGraphEdge<E, Graph<V, E>>) o;
            return Objects.equals(getEdge(), other.getEdge()) && getGraph() == other.getGraph();
        }

        @Override
        public int hashCode() {
            return pair.hashCode();
        }

        public static <V, E> DistinctGraphEdge<V, E> of(E e, Graph<V, E> g) {
            return new DistinctGraphEdge<>(e, g);
        }

    }
}
