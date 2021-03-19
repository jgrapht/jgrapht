/*
 * (C) Copyright 2019-2021, by Semen Chudakov and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.ConcurrencyUtil;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.*;

import static org.jgrapht.alg.shortestpath.ContractionHierarchyPrecomputation.*;

/**
 * Efficient algorithm for the many-to-many shortest paths problem based on contraction hierarchy.
 *
 * <p>
 * The algorithm is originally described in the article: Sebastian Knopp, Peter Sanders, Dominik
 * Schultes, Frank Schulz, and Dorothea Wagner. 2007. Computing many-to-many shortest paths using
 * highway hierarchies. In Proceedings of the Meeting on Algorithm Engineering &amp; Expermiments.
 * Society for Industrial and Applied Mathematics, Philadelphia, PA, USA, 36-45.
 *
 * <p>
 * First contraction hierarchy is constructed. Then for each target vertex a backward single source
 * shortest paths search is performed on the contracted graph. During the searches a bucket $b(v)$
 * is associated with each vertex $v$ in the graph. A bucket stores a set of pairs $(t,d)$, where
 * $t$ is a target vertex current search is performed from and $d$ is the computed distance from $v$
 * to this target. Then a forward single source shortest paths search is performed from every source
 * vertex. When a search settles a vertex $v$ with distance $d(s,v)$, where $s$ is current source
 * vertex, its bucket is scanned. For each entry $(t,d)$ if $d(s,t) &gt; d(s,v) + d$ values of paths
 * weight between $s$ and $t$ and its middle vertex is updated. The middle vertices are then used to
 * restored actual path from the information in the shortest paths trees.
 *
 * <p>
 * Additionally if $|S| > |T|$ the algorithm is executed on the reversed graph. This allows to
 * reduce the number of buckets and optimize memory usage of the algorithm.
 *
 * <p>
 * The efficiency of this algorithm is derived from the fact that contraction hierarchy produces
 * fairly small shortest paths trees. This allows to both speedup the computations and decrease
 * memory usage to store the paths. The bottleneck of the algorithm is the contraction hierarchy
 * computation, which can lead to significant overhead for dense graphs both in terms of running
 * time and space complexity. Therefore the ideal use cases for this algorithm are sparse graphs of
 * any size with low average out-degree of vertices.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Semen Chudakov
 * @see DefaultManyToManyShortestPaths
 * @see DijkstraManyToManyShortestPaths
 */
public class CHManyToManyShortestPaths<V, E>
    extends
    BaseManyToManyShortestPaths<V, E>
{
    /**
     * Contraction hierarchy of {@code graph}.
     */
    private ContractionHierarchy<V, E> contractionHierarchy;
    /**
     * Contracted version of {@code graph}.
     */
    private Graph<ContractionVertex<V>, ContractionEdge<E>> contractionGraph;
    /**
     * Mapping from vertices in the original {@code graph} to vertices in the
     * {@code contractionGraph}.
     */
    private Map<V, ContractionVertex<V>> contractionMapping;

    /**
     * Constructs an instance of the algorithm for a given {@code graph}.
     *
     * @param graph a graph
     * @deprecated replaced with {@link #CHManyToManyShortestPaths(Graph, ThreadPoolExecutor)}
     */
    @Deprecated
    public CHManyToManyShortestPaths(Graph<V, E> graph)
    {
        this(new ContractionHierarchyPrecomputation<>(graph).computeContractionHierarchy());
    }

    /**
     * Constructs an instance of the algorithm for a given {@code graph} and {@code executor}. It is
     * up to a user of this algorithm to handle the creation and termination of the provided
     * {@code executor}. For utility methods to manage a {@code ThreadPoolExecutor} see
     * {@link ConcurrencyUtil}.
     *
     * @param graph a graph
     * @param executor executor which will be used to compute {@link ContractionHierarchy}
     */
    public CHManyToManyShortestPaths(Graph<V, E> graph, ThreadPoolExecutor executor)
    {
        this(
            new ContractionHierarchyPrecomputation<>(graph, executor)
                .computeContractionHierarchy());
    }

    /**
     * Constructs an instance of the algorithm for a given {@code contractionHierarchy}.
     *
     * @param contractionHierarchy contraction of the {@code graph}
     */
    public CHManyToManyShortestPaths(ContractionHierarchy<V, E> contractionHierarchy)
    {
        super(contractionHierarchy.getGraph());
        this.contractionHierarchy = contractionHierarchy;
        this.contractionGraph = contractionHierarchy.getContractionGraph();
        this.contractionMapping = contractionHierarchy.getContractionMapping();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ManyToManyShortestPaths<V, E> getManyToManyPaths(Set<V> sources, Set<V> targets)
    {
        Objects.requireNonNull(sources, "sources cannot be null!");
        Objects.requireNonNull(targets, "targets cannot be null!");

        Graph<ContractionVertex<V>, ContractionEdge<E>> searchContractionGraph;
        boolean reversed;
        if (sources.size() <= targets.size()) {
            searchContractionGraph = contractionGraph;
            reversed = false;
        } else {
            searchContractionGraph = new EdgeReversedGraph<>(contractionGraph);
            reversed = true;
            Set<V> tmp = targets;
            targets = sources;
            sources = tmp;
        }

        Map<ContractionVertex<V>,
            Map<ContractionVertex<V>, Pair<Double, ContractionEdge<E>>>> forwardSearchSpaces =
                new HashMap<>();
        Map<ContractionVertex<V>,
            Map<ContractionVertex<V>, Pair<Double, ContractionEdge<E>>>> backwardSearchSpaces =
                new HashMap<>();
        Map<Pair<ContractionVertex<V>, ContractionVertex<V>>,
            Pair<Double, ContractionVertex<V>>> middleVertices = new HashMap<>();

        Set<ContractionVertex<V>> contractedSources = sources
            .stream().map(contractionMapping::get).collect(Collectors.toCollection(HashSet::new));
        Set<ContractionVertex<V>> contractedTargets = targets
            .stream().map(contractionMapping::get).collect(Collectors.toCollection(HashSet::new));

        Map<ContractionVertex<V>, List<BucketEntry>> bucketsMap = new HashMap<>();
        for (ContractionVertex<V> vertex : searchContractionGraph.vertexSet()) {
            bucketsMap.put(vertex, new ArrayList<>());
        }

        for (ContractionVertex<V> contractedTarget : contractedTargets) {
            backwardSearch(
                searchContractionGraph, contractedTarget, contractedSources, bucketsMap,
                backwardSearchSpaces, reversed);
        }

        for (ContractionVertex<V> contractedSource : contractedSources) {
            forwardSearch(
                searchContractionGraph, contractedSource, contractedTargets, bucketsMap,
                forwardSearchSpaces, middleVertices, reversed);
        }

        if (reversed) {
            return new CHManyToManyShortestPathsImpl(
                graph, contractionHierarchy, targets, sources, backwardSearchSpaces,
                forwardSearchSpaces, middleVertices);
        } else {
            return new CHManyToManyShortestPathsImpl(
                graph, contractionHierarchy, sources, targets, forwardSearchSpaces,
                backwardSearchSpaces, middleVertices);
        }
    }

    /**
     * Performs backward single source shortest paths search in {@code contractionGraph} starting
     * from {@code target} to {@code sources}. For each vertex $v$ in {@code contractionGraph} a
     * bucket is created that records entries $(t,d)$, where $t$ is a current {@code target} and $d$
     * is a distance computed during current search. A constructed shortest paths tree is then put
     * in {@code backwardSearchSpaces}. If {@code reversed} flag is set to $true$ the specified
     * {@code target} belongs to the original source vertices and therefore downward edges should be
     * masked in the contraction graph instead of upward.
     *
     * @param contractionGraph graph to perform search in
     * @param target search start vertex
     * @param contractedSources vertices to end search at
     * @param bucketsMap map from vertices to their buckets
     * @param backwardSearchSpaces map from vertices to their search spaces
     * @param reversed indicates if current search is reversed
     */
    private void backwardSearch(
        Graph<ContractionVertex<V>, ContractionEdge<E>> contractionGraph,
        ContractionVertex<V> target, Set<ContractionVertex<V>> contractedSources,
        Map<ContractionVertex<V>, List<BucketEntry>> bucketsMap,
        Map<ContractionVertex<V>,
            Map<ContractionVertex<V>, Pair<Double, ContractionEdge<E>>>> backwardSearchSpaces,
        boolean reversed)
    {
        Graph<ContractionVertex<V>, ContractionEdge<E>> maskSubgraph;

        if (reversed) {
            maskSubgraph = new MaskSubgraph<>(
                new EdgeReversedGraph<>(contractionGraph), v -> false, e -> !e.isUpward);
        } else {
            maskSubgraph = new MaskSubgraph<>(
                new EdgeReversedGraph<>(contractionGraph), v -> false, e -> e.isUpward);
        }

        Map<ContractionVertex<V>, Pair<Double, ContractionEdge<E>>> distanceAndPredecessorMap =
            getDistanceAndPredecessorMap(maskSubgraph, target, contractedSources);

        backwardSearchSpaces.put(target, distanceAndPredecessorMap);

        for (Map.Entry<ContractionVertex<V>,
            Pair<Double, ContractionEdge<E>>> entry : distanceAndPredecessorMap.entrySet())
        {
            bucketsMap
                .get(entry.getKey()).add(new BucketEntry(target, entry.getValue().getFirst()));
        }
    }

    /**
     * Performs forward search from the given {@code source} to {@code targets}. A constructed
     * shortest paths tree is then put in {@code forwardSearchSpaces}. If {@code reversed} flag is
     * set to $true$ the specified {@code source} belongs to the original target vertices and
     * therefore upward edges should be masked in the contraction graph instead of the downward.
     *
     * @param contractionGraph graph to perform search in
     * @param source start vertex of the search
     * @param contractedTargets vertices to end search at
     * @param bucketsMap map from vertices to their buckets
     * @param forwardSearchSpaces map from vertices to their search spaces
     * @param middleVerticesMap map from source-target pairs to theirs distances and middle nodes
     * @param reversed indicates if current search is reversed
     */
    private void forwardSearch(
        Graph<ContractionVertex<V>, ContractionEdge<E>> contractionGraph,
        ContractionVertex<V> source, Set<ContractionVertex<V>> contractedTargets,
        Map<ContractionVertex<V>, List<BucketEntry>> bucketsMap,
        Map<ContractionVertex<V>,
            Map<ContractionVertex<V>, Pair<Double, ContractionEdge<E>>>> forwardSearchSpaces,
        Map<Pair<ContractionVertex<V>, ContractionVertex<V>>,
            Pair<Double, ContractionVertex<V>>> middleVerticesMap,
        boolean reversed)
    {
        Graph<ContractionVertex<V>, ContractionEdge<E>> maskSubgraph;
        if (reversed) {
            maskSubgraph = new MaskSubgraph<>(contractionGraph, v -> false, e -> e.isUpward);
        } else {
            maskSubgraph = new MaskSubgraph<>(contractionGraph, v -> false, e -> !e.isUpward);
        }

        Map<ContractionVertex<V>, Pair<Double, ContractionEdge<E>>> distanceAndPredecessorMap =
            getDistanceAndPredecessorMap(maskSubgraph, source, contractedTargets);

        forwardSearchSpaces.put(source, distanceAndPredecessorMap);

        for (Map.Entry<ContractionVertex<V>,
            Pair<Double, ContractionEdge<E>>> entry : distanceAndPredecessorMap.entrySet())
        {
            ContractionVertex<V> middleVertex = entry.getKey();
            double forwardDistance = entry.getValue().getFirst();

            for (BucketEntry bucketEntry : bucketsMap.get(middleVertex)) {
                double pathDistance = forwardDistance + bucketEntry.distance;
                Pair<ContractionVertex<V>, ContractionVertex<V>> pair;
                if (reversed) {
                    pair = Pair.of(bucketEntry.target, source);
                } else {
                    pair = Pair.of(source, bucketEntry.target);
                }
                middleVerticesMap.compute(pair, (p, distanceAndMiddleNode) -> {
                    if (distanceAndMiddleNode == null
                        || distanceAndMiddleNode.getFirst() > pathDistance)
                    {
                        return Pair.of(pathDistance, middleVertex);
                    }
                    return distanceAndMiddleNode;
                });
            }
        }
    }

    /**
     * Computes distance and predecessor map for a single source shortest paths search starting at
     * source and finishing the search as soon as all {@code targets} are reached.
     *
     * @param contractionGraph a graph
     * @param source search start vertex
     * @param targets search end vertices
     * @return distance and predecessor map
     */
    private Map<ContractionVertex<V>,
        Pair<Double, ContractionEdge<E>>> getDistanceAndPredecessorMap(
            Graph<ContractionVertex<V>, ContractionEdge<E>> contractionGraph,
            ContractionVertex<V> source, Set<ContractionVertex<V>> targets)
    {
        return ((TreeSingleSourcePathsImpl<ContractionVertex<V>,
            ContractionEdge<E>>) getShortestPathsTree(contractionGraph, source, targets)).map;
    }

    /**
     * Stores data computed during the backward searches.
     */
    private class BucketEntry
    {
        /**
         * Start vertex of the backward search during which this entry is created.
         */
        ContractionVertex<V> target;
        /**
         * Distance from a vertex this entry is created for to {@code target}.
         */
        double distance;

        /**
         * Constrcuts an instance of an entry for the given {@code target} and {@code distance}.
         *
         * @param target backward search start vertex
         * @param distance distance to {@code target}
         */
        public BucketEntry(ContractionVertex<V> target, double distance)
        {
            this.target = target;
            this.distance = distance;
        }
    }

    /**
     * Implementation of
     * {@link org.jgrapht.alg.interfaces.ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths}
     * for many-to-many shortest paths algorithm based on contraction hierarchy. Paths are stored in
     * form of bidirectional single source shortest paths trees. When a path weight is queried a
     * value that is stored in {@code distanceAndMiddleVertexMap} is returned. When an actual paths
     * is required it is constructed by recursively unpacking edges stored in the shortest paths
     * trees corresponding to source and target vertices.
     */
    private class CHManyToManyShortestPathsImpl
        extends
        BaseManyToManyShortestPathsImpl<V, E>
    {
        /**
         * The underlying graph.
         */
        private final Graph<V, E> graph;
        /**
         * Contraction hierarchy for {@code graph}.
         */
        private final Graph<ContractionVertex<V>, ContractionEdge<E>> contractionGraph;
        /**
         * Mapping from original to contracted vertices.
         */
        private final Map<V, ContractionVertex<V>> contractionMapping;

        /**
         * Stores forward search space for each start vertex.
         */
        private Map<ContractionVertex<V>,
            Map<ContractionVertex<V>, Pair<Double, ContractionEdge<E>>>> forwardSearchSpaces;
        /**
         * Stores backward search space for each target vertex.
         */
        private Map<ContractionVertex<V>,
            Map<ContractionVertex<V>, Pair<Double, ContractionEdge<E>>>> backwardSearchSpaces;

        /**
         * Stores pair of path weight and middle vertex for each source-target pair.
         */
        private Map<Pair<ContractionVertex<V>, ContractionVertex<V>>,
            Pair<Double, ContractionVertex<V>>> distanceAndMiddleVertexMap;

        /**
         * Constructs a new instance for the given {@code graph}, {@code contractionGraph},
         * {@code contractionMapping}, {@code forwardSearchSpaces}, {@code backwardSearchSpaces} and
         * {@code distanceAndMiddleVertexMap}.
         *
         * @param graph underlying graph.
         * @param hierarchy contraction hierarchy
         * @param forwardSearchSpaces search spaces of source vertices
         * @param backwardSearchSpaces search spaces of target vertices
         * @param distanceAndMiddleVertexMap weights and middle vertices of paths
         */
        public CHManyToManyShortestPathsImpl(
            Graph<V, E> graph, ContractionHierarchy<V, E> hierarchy, Set<V> sources, Set<V> targets,
            Map<ContractionVertex<V>,
                Map<ContractionVertex<V>, Pair<Double, ContractionEdge<E>>>> forwardSearchSpaces,
            Map<ContractionVertex<V>,
                Map<ContractionVertex<V>, Pair<Double, ContractionEdge<E>>>> backwardSearchSpaces,
            Map<Pair<ContractionVertex<V>, ContractionVertex<V>>,
                Pair<Double, ContractionVertex<V>>> distanceAndMiddleVertexMap)
        {
            super(sources, targets);
            this.graph = graph;
            this.contractionGraph = hierarchy.getContractionGraph();
            this.contractionMapping = hierarchy.getContractionMapping();
            this.forwardSearchSpaces = forwardSearchSpaces;
            this.backwardSearchSpaces = backwardSearchSpaces;
            this.distanceAndMiddleVertexMap = distanceAndMiddleVertexMap;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public GraphPath<V, E> getPath(V source, V target)
        {
            assertCorrectSourceAndTarget(source, target);

            LinkedList<E> edgeList = new LinkedList<>();
            LinkedList<V> vertexList = new LinkedList<>();

            ContractionVertex<V> contractedSource = contractionMapping.get(source);
            ContractionVertex<V> contractedTarget = contractionMapping.get(target);
            Pair<ContractionVertex<V>, ContractionVertex<V>> contractedVertices =
                Pair.of(contractedSource, contractedTarget);

            Map<ContractionVertex<V>, Pair<Double, ContractionEdge<E>>> forwardTree =
                forwardSearchSpaces.get(contractedSource);
            Map<ContractionVertex<V>, Pair<Double, ContractionEdge<E>>> backwardTree =
                backwardSearchSpaces.get(contractedTarget);

            Pair<Double, ContractionVertex<V>> distanceAndCommonVertex =
                distanceAndMiddleVertexMap.get(contractedVertices);

            if (distanceAndCommonVertex == null) {
                return null;
            }

            ContractionVertex<V> commonVertex = distanceAndCommonVertex.getSecond();

            // add common vertex
            vertexList.add(commonVertex.vertex);

            // traverse forward path
            ContractionVertex<V> v = commonVertex;
            while (true) {
                ContractionEdge<E> e = forwardTree.get(v).getSecond();

                if (e == null) {
                    break;
                }

                contractionHierarchy.unpackBackward(e, vertexList, edgeList);
                v = contractionGraph.getEdgeSource(e);
            }

            // traverse reverse path
            v = commonVertex;
            while (true) {
                ContractionEdge<E> e = backwardTree.get(v).getSecond();

                if (e == null) {
                    break;
                }

                contractionHierarchy.unpackForward(e, vertexList, edgeList);
                v = contractionGraph.getEdgeTarget(e);
            }

            return new GraphWalk<>(
                graph, source, target, vertexList, edgeList, distanceAndCommonVertex.getFirst());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getWeight(V source, V target)
        {
            assertCorrectSourceAndTarget(source, target);

            Pair<ContractionVertex<V>, ContractionVertex<V>> contractedVertices =
                Pair.of(contractionMapping.get(source), contractionMapping.get(target));

            if (distanceAndMiddleVertexMap.containsKey(contractedVertices)) {
                return distanceAndMiddleVertexMap.get(contractedVertices).getFirst();
            } else {
                return Double.POSITIVE_INFINITY;
            }
        }
    }
}
