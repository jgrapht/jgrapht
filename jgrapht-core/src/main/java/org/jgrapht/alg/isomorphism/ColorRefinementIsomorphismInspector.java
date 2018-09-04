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

import java.util.*;
import java.util.function.Supplier;

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

        Graph<Pair<V, Integer>, Pair<E, Integer>> graph = new UnionGraph<>(graph1, graph2);
        ColorRefinementAlgorithm<Pair<V, Integer>, Pair<E, Integer>> colorRefinementAlgorithm = new ColorRefinementAlgorithm<>(graph);

        // execute color refinement for graph
        Coloring<Pair<V, Integer>> coloring = colorRefinementAlgorithm.getColoring();

        isomorphismTestExecuted = true;

        isIsomorphic = coarseColoringAreEqual(coloring);
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
    private boolean coarseColoringAreEqual(Coloring<Pair<V, Integer>> coloring) throws IsomorphismUndecidableException {
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
     private Pair<Coloring<V>, Coloring<V>> splitColoring(Coloring<Pair<V, Integer>> coloring) {
        Map<V, Integer> col1 = new HashMap<>();
        Map<V, Integer> col2 = new HashMap<>();
        int index = 0;
        for(Set<Pair<V, Integer>> set1 : coloring.getColorClasses()) {
            for (Pair<V, Integer> entry : set1) {
                if (entry.getSecond() == 1) {
                    col1.put(entry.getFirst(), index);
                } else {
                    col2.put(entry.getFirst(), index);
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
     * UnionGraph is a special union graph version representing the disjoint union of two graphs, which is optimized for
     * the color refinement algorithm. It caches the vertex set of the union graph of the two input graphs.
     *
     * @param <V> the vertex type of the graphs
     * @param <E> the edge type of the graphs
     */
    class UnionGraph<V, E> implements Graph<Pair<V, Integer>, Pair<E, Integer>> {

        Graph<V, E> graph1, graph2;
        Set<Pair<V, Integer>> vertexSet;

        public UnionGraph(Graph<V, E> graph1, Graph<V, E> graph2) {
            this.graph1 = graph1;
            this.graph2 = graph2;
        }
        @Override
        public Set<Pair<E, Integer>> getAllEdges(Pair<V, Integer> sourceVertex, Pair<V, Integer> targetVertex) {
            return null;
        }
        @Override
        public Pair<E, Integer> getEdge(Pair<V, Integer> sourceVertex, Pair<V, Integer> targetVertex) {
            return null;
        }
        @Override
        public Supplier<Pair<V, Integer>> getVertexSupplier() {
            return null;
        }
        @Override
        public Supplier<Pair<E, Integer>> getEdgeSupplier() {
            return null;
        }
        @Override
        public Pair<E, Integer> addEdge(Pair<V, Integer> sourceVertex, Pair<V, Integer> targetVertex) {
            return null;
        }
        @Override
        public boolean addEdge(Pair<V, Integer> sourceVertex, Pair<V, Integer> targetVertex, Pair<E, Integer> e) {
            return false;
        }
        @Override
        public Pair<V, Integer> addVertex() {
            return null;
        }
        @Override
        public boolean addVertex(Pair<V, Integer> vIntegerTuple) {
            return false;
        }
        @Override
        public boolean containsEdge(Pair<V, Integer> sourceVertex, Pair<V, Integer> targetVertex) {
            return false;
        }
        @Override
        public boolean containsEdge(Pair<E, Integer> e) {
            return false;
        }
        @Override
        public boolean containsVertex(Pair<V, Integer> vIntegerTuple) {
            return false;
        }
        @Override
        public Set<Pair<E, Integer>> edgeSet() {
            return null;
        }
        @Override
        public int degreeOf(Pair<V, Integer> vertex) {
            return 0;
        }
        @Override
        public Set<Pair<E, Integer>> edgesOf(Pair<V, Integer> vertex) {
            return null;
        }
        @Override
        public int inDegreeOf(Pair<V, Integer> vertex) {
            return 0;
        }
        @Override
        public Set<Pair<E, Integer>> incomingEdgesOf(Pair<V, Integer> vertex) {
            Graph<V, E> graph = vertex.getSecond() == 1 ? graph1 : graph2;
            return new PairSet<>(graph.incomingEdgesOf(vertex.getFirst()), vertex.getSecond());
        }
        @Override
        public int outDegreeOf(Pair<V, Integer> vertex) {
            return 0;
        }
        @Override
        public Set<Pair<E, Integer>> outgoingEdgesOf(Pair<V, Integer> vertex) {
            return null;
        }
        @Override
        public boolean removeAllEdges(Collection<? extends Pair<E, Integer>> edges) {
            return false;
        }
        @Override
        public Set<Pair<E, Integer>> removeAllEdges(Pair<V, Integer> sourceVertex, Pair<V, Integer> targetVertex) {
            return null;
        }
        @Override
        public boolean removeAllVertices(Collection<? extends Pair<V, Integer>> vertices) {
            return false;
        }
        @Override
        public Pair<E, Integer> removeEdge(Pair<V, Integer> sourceVertex, Pair<V, Integer> targetVertex) {
            return null;
        }
        @Override
        public boolean removeEdge(Pair<E, Integer> e) {
            return false;
        }
        @Override
        public boolean removeVertex(Pair<V, Integer> vIntegerTuple) {
            return false;
        }
        @Override
        public Set<Pair<V, Integer>> vertexSet() {
            if (vertexSet == null) {
                vertexSet = new HashSet<>();
                for (V v : graph1.vertexSet()) {
                    vertexSet.add(new Pair<>(v, 1));
                }
                for (V v : graph2.vertexSet()) {
                    vertexSet.add(new Pair<>(v, 2));
                }
            }
            return vertexSet;
        }
        @Override
        public Pair<V, Integer> getEdgeSource(Pair<E, Integer> e) {
            if (e.getSecond() == 1){
                return new Pair<>(graph1.getEdgeSource(e.getFirst()), 1);
            } else {
                return new Pair<>(graph2.getEdgeSource(e.getFirst()), 2);
            }
        }
        @Override
        public Pair<V, Integer> getEdgeTarget(Pair<E, Integer> e) {
            if (e.getSecond() == 1){
                return new Pair<>(graph1.getEdgeTarget(e.getFirst()), 1);
            } else {
                return new Pair<>(graph2.getEdgeTarget(e.getFirst()), 2);
            }
        }
        @Override
        public GraphType getType() {
            return null;
        }
        @Override
        public double getEdgeWeight(Pair<E, Integer> e) {
            return 0;
        }
        @Override
        public void setEdgeWeight(Pair<E, Integer> e, double weight) {
        }
    }

    /**
     * Implementation of a set of pairs. It is optimized for the usage of class UnionGraph in the color refinement
     * algorithm; it uses the innerSet of UnionGraph.
     *
     * @param <V> the vertex type of the graph
     */
    class PairSet<V> implements Set<Pair<V, Integer>> {
        Set<V> innerSet;
        Integer index;
        public PairSet(Set<V> innerSet, Integer index) {
            this.innerSet = innerSet;
            this.index = index;
        }
        @Override
        public int size() {
            return innerSet.size();
        }
        @Override
        public boolean isEmpty() {
            return innerSet.isEmpty();
        }
        @Override
        public boolean contains(Object o) {
            Pair<V, Integer> val = (Pair<V, Integer>)o;
            return val.getSecond().equals(index) && innerSet.contains(val.getSecond());
        }
        @Override
        public Iterator<Pair<V, Integer>> iterator() {
            Iterator<V> innerIterator = innerSet.iterator();
            return new Iterator<Pair<V, Integer>>() {
                @Override
                public boolean hasNext() {
                    return innerIterator.hasNext();
                }
                @Override
                public Pair<V, Integer> next() {
                    return new Pair<>(innerIterator.next(), index);
                }
            };
        }
        @Override
        public Object[] toArray() {
            return new Object[0];
        }
        @Override
        public <T> T[] toArray(T[] a) {
            return null;
        }
        @Override
        public boolean add(Pair<V, Integer> vIntegerTuple) {
            if (!index.equals(vIntegerTuple.getSecond())) {
                return false;
            }
            return innerSet.add(vIntegerTuple.getFirst());
        }
        @Override
        public boolean remove(Object o) {
            return false;
        }
        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }
        @Override
        public boolean addAll(Collection<? extends Pair<V, Integer>> c) {
            return false;
        }
        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }
        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }
        @Override
        public void clear() {
        }
    }
}
