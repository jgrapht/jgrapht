/*
 * (C) Copyright 2020-2020, by Semen Chudakov and Contributors.
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

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.GraphWalk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.jgrapht.alg.interfaces.ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths;
import static org.jgrapht.alg.shortestpath.ContractionHierarchyPrecomputation.ContractionHierarchy;
import static org.jgrapht.alg.shortestpath.ContractionHierarchyPrecomputation.ContractionVertex;
import static org.jgrapht.alg.shortestpath.TransitNodeRoutingPrecomputation.AccessVertex;
import static org.jgrapht.alg.shortestpath.TransitNodeRoutingPrecomputation.AccessVertices;
import static org.jgrapht.alg.shortestpath.TransitNodeRoutingPrecomputation.LocalityFiler;
import static org.jgrapht.alg.shortestpath.TransitNodeRoutingPrecomputation.TransitNodeRouting;

/**
 * Implementation of the shortest paths algorithm based on {@link TransitNodeRoutingPrecomputation}.
 *
 * <p>
 * The algorithm is originally described the article: Arz, Julian &amp; Luxen, Dennis &amp; Sanders,
 * Peter. (2013). Transit Node Routing Reconsidered. 7933. 10.1007/978-3-642-38527-8_7.
 *
 * <p>
 * The shortest paths between vertices $u$ and $v$ is computed in the following way.
 * First, a locality filter is used to determine is the vertices are local to each other.
 * If so, a fallback shortest path algorithm is used to compute the path. Otherwise, there
 * is a shortest path between the vertices which contain a transit vertex. Therefore the
 * forward access vertices of $u$ and backward access vertices of $v$ are inspected to find
 * a pair of such access vertices $(a_u, a_v)$ so that the value of $d(u,a_u) + d(a_u, a_v)
 * + d(a_u, v)$ is minimum over all such pairs. Here $d(s,t)$ is the distance from vertex
 * $s$ to vertex $t$.
 *
 * @param <V> graph vertex type
 * @param <E> graph edge type
 * @see TransitNodeRoutingPrecomputation
 */
public class TransitNodeRoutingShortestPath<V, E> extends BaseShortestPathAlgorithm<V, E> {

    /**
     * Contraction hierarchy which is used to compute shortest paths.
     */
    private ContractionHierarchy<V, E> contractionHierarchy;

    /**
     * Fallback shortest path algorithm for local queries.
     */
    private ShortestPathAlgorithm<V, E> localQueriesAlgorithm;

    /**
     * Many-to-many shortest paths between transit vertices.
     */
    private ManyToManyShortestPaths<V, E> manyToManyShortestPaths;
    /**
     * Stores access vertices for each vertex in the {@code graph}.
     */
    private AccessVertices<V, E> accessVertices;
    /**
     * Locality filter which is used to determine if two vertices in the graph
     * are local to each other or not.
     */
    private LocalityFiler<V> localityFiler;

    /**
     * Constructs a new instance for the given {@code graph}.
     *
     * @param graph graph
     */
    public TransitNodeRoutingShortestPath(Graph<V, E> graph) {
        this(new TransitNodeRoutingPrecomputation<>(graph).computeTransitNodeRouting());
    }

    /**
     * Constructs a new instance of the algorithm for a given {@code transitNodeRouting}.
     *
     * @param transitNodeRouting transit node routing for {@code graph}
     */
    public TransitNodeRoutingShortestPath(TransitNodeRouting<V, E> transitNodeRouting) {
        super(transitNodeRouting.getContractionHierarchy().getGraph());
        this.contractionHierarchy = transitNodeRouting.getContractionHierarchy();
        this.localityFiler = transitNodeRouting.getLocalityFiler();
        this.accessVertices = transitNodeRouting.getAccessVertices();
        this.manyToManyShortestPaths = transitNodeRouting.getTransitVerticesPaths();
        this.localQueriesAlgorithm = new ContractionHierarchyBidirectionalDijkstra<>(
                transitNodeRouting.getContractionHierarchy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphPath<V, E> getPath(V source, V sink) {
        if (localityFiler.isLocal(source, sink)) {
            return localQueriesAlgorithm.getPath(source, sink);
        } else {
            Pair<AccessVertex<V, E>, AccessVertex<V, E>> p = getMinWeightAccessVertices(source, sink);
            AccessVertex<V, E> forwardAccessVertex = p.getFirst();
            AccessVertex<V, E> backwardAccessVertex = p.getSecond();

            if (forwardAccessVertex == null) {
                return createEmptyPath(source, sink);
            }

            return mergePaths(forwardAccessVertex.getPath(),
                    manyToManyShortestPaths.getPath(forwardAccessVertex.getVertex(), backwardAccessVertex.getVertex()),
                    backwardAccessVertex.getPath());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getPathWeight(V source, V sink) {
        if (localityFiler.isLocal(source, sink)) {
            return localQueriesAlgorithm.getPathWeight(source, sink);
        } else {
            Pair<AccessVertex<V, E>, AccessVertex<V, E>> p = getMinWeightAccessVertices(source, sink);
            AccessVertex<V, E> forwardAccessVertex = p.getFirst();
            AccessVertex<V, E> backwardAccessVertex = p.getSecond();

            if (forwardAccessVertex == null) {
                return Double.POSITIVE_INFINITY;
            }

            return forwardAccessVertex.getPath().getWeight() +
                    manyToManyShortestPaths.getWeight(forwardAccessVertex.getVertex(), backwardAccessVertex.getVertex()) +
                    backwardAccessVertex.getPath().getWeight();
        }
    }

    /**
     * For vertices {@code source} and {@code sink} finds pair of access vertices
     * with smallest weight over all pairs.
     *
     * @param source source vertex
     * @param sink   sink vertex
     * @return pair of access vertices with shortest path between them
     */
    private Pair<AccessVertex<V, E>, AccessVertex<V, E>> getMinWeightAccessVertices(V source, V sink) {
        ContractionVertex<V> contractedSource = contractionHierarchy.getContractionMapping().get(source);
        ContractionVertex<V> contractedSink = contractionHierarchy.getContractionMapping().get(sink);

        AccessVertex<V, E> forwardAccessVertex = null;
        AccessVertex<V, E> backwardAccessVertex = null;
        double minimumWeight = Double.POSITIVE_INFINITY;

        for (AccessVertex<V, E> sourceAccessVertex : accessVertices.getForwardAccessVertices(contractedSource)) {
            for (AccessVertex<V, E> sinkAccessVertex : accessVertices.getBackwardAccessVertices(contractedSink)) {
                double currentWeight = sourceAccessVertex.getPath().getWeight() +
                        manyToManyShortestPaths.getWeight(sourceAccessVertex.getVertex(), sinkAccessVertex.getVertex()) +
                        sinkAccessVertex.getPath().getWeight();
                if (currentWeight < minimumWeight) {
                    minimumWeight = currentWeight;
                    forwardAccessVertex = sourceAccessVertex;
                    backwardAccessVertex = sinkAccessVertex;
                }
            }
        }

        if (minimumWeight == Double.POSITIVE_INFINITY) {
            return new Pair<>(null, null);
        }

        return Pair.of(forwardAccessVertex, backwardAccessVertex);
    }

    /**
     * Computes a path which consists of {@code first}, {@code second} and {@code third} paths.
     *
     * @param first  first part of the path
     * @param second second part of the path
     * @param third  third part of the path
     * @return merged path
     */
    private GraphPath<V, E> mergePaths(GraphPath<V, E> first, GraphPath<V, E> second, GraphPath<V, E> third) {
        V startVertex = first.getStartVertex();
        V endVertex = third.getEndVertex();
        double totalWeight = first.getWeight() + second.getWeight() + third.getWeight();

        int vertexListSize = first.getVertexList().size() + second.getVertexList().size() + third.getVertexList().size() - 2;
        List<V> vertexList = new ArrayList<>(vertexListSize);
        int edgeListSize = first.getLength() + second.getLength() + third.getLength();
        List<E> edgeList = new ArrayList<>(edgeListSize);

        // form vertex list
        Iterator<V> firstIt = first.getVertexList().iterator();
        while (firstIt.hasNext()) {
            V element = firstIt.next();
            if (firstIt.hasNext()) {
                vertexList.add(element);
            }
        }
        vertexList.addAll(second.getVertexList());
        Iterator<V> thirdIt = third.getVertexList().iterator();
        thirdIt.next();
        while (thirdIt.hasNext()) {
            vertexList.add(thirdIt.next());
        }

        // form edge list
        edgeList.addAll(first.getEdgeList());
        edgeList.addAll(second.getEdgeList());
        edgeList.addAll(third.getEdgeList());

        return new GraphWalk<>(graph, startVertex, endVertex, vertexList, edgeList, totalWeight);
    }
}
