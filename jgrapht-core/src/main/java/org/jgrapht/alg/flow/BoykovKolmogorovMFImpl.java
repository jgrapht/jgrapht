/*
 * (C) Copyright 2020-2021, by Timofey Chudakov and Contributors.
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
package org.jgrapht.alg.flow;

import org.jgrapht.Graph;
import org.jgrapht.alg.util.extension.ExtensionFactory;

import java.util.*;

/**
 * This is an implementation of the
 * <a href="https://en.wikipedia.org/wiki/Graph_cuts_in_computer_vision#Implementation_(exact)">
 * Boykov-Kolmogorov maximum flow algorithm</a>. This algorithm is a special-purpose approach to
 * solving computer vision related maximum flow problems. The algorithm was initially described in:
 * <i>Y. Boykov and V. Kolmogorov, "An experimental comparison of min-cut/max-flow algorithms for
 * energy minimization in vision," in IEEE Transactions on Pattern Analysis and Machine
 * Intelligence, vol. 26, no. 9, pp. 1124-1137, Sept. 2004, doi: 10.1109/TPAMI.2004.60.</i>. An
 * extended description is given in: <i>Vladimir Kolmogorov. 2004. Graph based algorithms for scene
 * reconstruction from two or more views. Ph.D. Dissertation. Cornell University, USA. Advisor(s)
 * Ramin Zabih. Order Number: AAI3114475.</i>.
 * <p>
 * This implementation uses 2 heuristics described in Vladimir Kolmogorov's original PhD thesis:
 * <ul>
 * <li>Timestamp heuristic.</li>
 * <li>Distance heuristic;</li>
 * </ul>
 * <p>
 * The worse-case running time of this algorithm on a network $G = (V, E)$ with a capacity function
 * $c: E \rightArrow R^{+}$ is $\mathcal{O}(E\times f)$, where $f$ is the maximum flow value. The
 * reason for this is that the algorithm doesn't necessarily augments shortest $s-t$ paths in a
 * residual network. That's why the argument about the running time complexity is the same as with
 * the Ford-Fulkerson algorithm.
 * <p>
 * This algorithm doesn't have the best performance on all types of networks. It's recommended to
 * check if this algorithm gives substantial performance improvement before using it in a particular
 * application. A good general-purpose alternative which works fast in all scenarios is the
 * {@link PushRelabelMFImpl}.
 * <p>
 * This algorithm works with both directed and undirected networks. The algorithm doesn't have
 * internal synchronization, thus any concurrent network modification has undefined behaviour.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 */
public class BoykovKolmogorovMFImpl<V, E>
    extends
    MaximumFlowAlgorithmBase<V, E>
{

    /**
     * Whether to print debug related messages.
     */
    private static final boolean DEBUG = false;
    /**
     * The timestamp used for free nodes. This value is the smallest among all node timestamps and
     * is assigned only to free vertices.
     */
    private static final long FREE_NODE_TIMESTAMP = 0;
    /**
     * A timestamp for the first algorithm loop iteration.
     */
    private static final long INITIAL_TIMESTAMP = 1;

    /**
     * The value of the current iteration timestamp. After each iteration, the current timestamp is
     * incremented.
     */
    private long currentTimestamp;

    /**
     * Vertex extension factory used during initialization.
     */
    private final ExtensionFactory<VertexExtension> vertexExtensionsFactory;
    /**
     * Edge extension factory used during initialization.
     */
    private final ExtensionFactory<AnnotatedFlowEdge> edgeExtensionsFactory;

    /**
     * The network source of the current algorithm invocation.
     */
    private VertexExtension currentSource;
    /**
     * The network sink of the current algorithm invocation.
     */
    private VertexExtension currentSink;

    /**
     * The queue of active vertices. An active vertex is a network vertex which: (a) belongs to
     * source or sink flow tree. (b) has an outgoing edge with positive capacity, which target is a
     * free vertex. The active vertices are processed according to the FIFO principle.
     */
    private final Deque<VertexExtension> activeVertices;
    /**
     * A list of orphans emerged after an s-t path augmentation. An orphan is a network node which
     * parent edge in the residual network flow tree became saturated.
     */
    private final List<VertexExtension> orphans;

    /**
     * A queue of child orphans. A child orphan is a descendant of an orphan, which didn't get a new
     * parent in corresponding flow free. These child orphans have precedence over regular orphans
     * and are processed according to the FIFO principle.
     */
    private final Deque<VertexExtension> childOrphans;

    /**
     * Creates a new algorithm instance with the specified {@code network}. The created algorithm
     * uses default epsilon.
     *
     * @param network flow network.
     */
    public BoykovKolmogorovMFImpl(Graph<V, E> network)
    {
        this(network, DEFAULT_EPSILON);
    }

    /**
     * Construct a new algorithm instance with the specifies {@code network} and {@code epsilon}.
     *
     * @param network flow network
     * @param epsilon tolerance for the comparison of floating point values
     */
    public BoykovKolmogorovMFImpl(Graph<V, E> network, double epsilon)
    {
        super(Objects.requireNonNull(network, "Network must be not null!"), epsilon);

        vertexExtensionsFactory = VertexExtension::new;
        edgeExtensionsFactory = AnnotatedFlowEdge::new;

        activeVertices = new ArrayDeque<>();
        orphans = new ArrayList<>();
        childOrphans = new ArrayDeque<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MaximumFlow<E> getMaximumFlow(V source, V sink)
    {
        this.calculateMaximumFlow(source, sink);
        maxFlow = composeFlow();
        return new MaximumFlowImpl<>(maxFlowValue, maxFlow);
    }

    /**
     * Computes the maximum flow value.
     * <p>
     * This is the main algorithm loop. First, an algorithm initialization is performed. The
     * initialization includes augmenting all source-sink and source-node-sink paths. After that,
     * the algorithm finds the rest of the augmenting path by iteratively:
     * <p>
     * - growing the source and sink flow trees using active vertices - augmenting s-t paths using
     * bounding edges between source and sink flow trees. - adopting orphan nodes emerged after s-t
     * path augmentation.
     *
     * @param source network source
     * @param sink network sink.
     */
    private void calculateMaximumFlow(V source, V sink)
    {
        super.init(source, sink, vertexExtensionsFactory, edgeExtensionsFactory);

        if (!network.containsVertex(source)) {
            throw new IllegalArgumentException("invalid source (null or not from this network)");
        }
        if (!network.containsVertex(sink)) {
            throw new IllegalArgumentException("invalid sink (null or not from this network)");
        }
        if (source.equals(sink)) {
            throw new IllegalArgumentException("source is equal to sink");
        }

        currentSource = getVertexExtension(source);
        currentSink = getVertexExtension(sink);
        currentTimestamp = INITIAL_TIMESTAMP;

        augmentShortPaths(currentSource, currentSink);

        currentSource.treeStatus = VertexTreeStatus.SOURCE_TREE_VERTEX;
        currentSink.treeStatus = VertexTreeStatus.SINK_TREE_VERTEX;

        makeActive(currentSource);
        makeActive(currentSink);

        for (;;) {
            AnnotatedFlowEdge boundingEdge = grow();
            if (boundingEdge == null) {
                break;
            }
            augment(boundingEdge);

            nextIteration();
            adopt();
        }
    }

    /**
     * Augments all source-sink and source-node-sink paths. This improved performance on the
     * computer vision maximum flow networks.
     *
     * @param source network source.
     * @param sink network sink.
     */
    private void augmentShortPaths(VertexExtension source, VertexExtension sink)
    {
        for (AnnotatedFlowEdge sourceEdge : source.getOutgoing()) {
            VertexExtension mediumVertex = sourceEdge.getTarget();
            if (mediumVertex == sink) {
                double flow = sourceEdge.getResidualCapacity();
                pushFlowThrough(sourceEdge, flow);
                maxFlowValue += flow;
            } else {
                for (AnnotatedFlowEdge sinkEdge : mediumVertex.getOutgoing()) {
                    VertexExtension targetVertex = sinkEdge.getTarget();
                    if (targetVertex == sink) {
                        double flow = Math
                            .min(sourceEdge.getResidualCapacity(), sinkEdge.getResidualCapacity());
                        pushFlowThrough(sourceEdge, flow);
                        pushFlowThrough(sinkEdge, flow);
                        maxFlowValue += flow;
                    }
                    // if all the capacity of the source edge was used,
                    // it doesn't make sense to continue searching for s-t path
                    if (!sourceEdge.hasCapacity()) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Performs an algorithm grow phase.
     * <p>
     * During the grow phase, the network active vertices are iteratively processed. The goal of
     * this processing is to find an (outgoing for source tree / incoming for sink tree) edge with
     * positive capacity which opposite node is either a free node or belongs to the other tree. In
     * the first case, the tree gets one more node, in the second case, a bounding edge is found and
     * the algorithm can proceed to the augment phase.
     * <p>
     * Since processing logic is different for source and sink trees, the code handles there cases
     * separately. This method returns either a bounding edge or {@code null}. The {@code null}
     * value can be returned only after all of the active vertices are processed and no bounding
     * edge is found. This means that the residual network is disconnected and the algorithm can
     * terminate.
     *
     * @return a bounding edge or {@code null} if no bounding edge exists.
     */
    private AnnotatedFlowEdge grow()
    {
        for (VertexExtension activeVertex = nextActiveVertex(); activeVertex != null;
            activeVertex = nextActiveVertex())
        {

            if (activeVertex.isSourceTreeVertex()) {
                // processing source tree vertex
                for (AnnotatedFlowEdge edge : activeVertex.getOutgoing()) {

                    if (edge.hasCapacity()) {
                        VertexExtension target = edge.getTarget();

                        if (target.isSinkTreeVertex()) {
                            // found a bounding edge
                            if (DEBUG) {
                                System.out.printf("Bounding edge = %s\n\n", edge);
                            }

                            return edge;
                        } else if (target.isFreeVertex()) {
                            // found a node which can be added to the source tree
                            if (DEBUG) {
                                System.out
                                    .printf(
                                        "Growing source tree: %s -> %s\n\n", edge,
                                        target.prototype);
                            }

                            target.parentEdge = edge;
                            target.treeStatus = VertexTreeStatus.SOURCE_TREE_VERTEX;
                            target.distance = activeVertex.distance + 1;
                            target.timestamp = activeVertex.timestamp;
                            makeActive(target);
                        } else {
                            /*
                             * The target node belongs to the source tree the distance heuristic can
                             * be applied to possibly build a tree with smaller height.
                             */
                            assert target.isSourceTreeVertex();
                            if (isCloserToTerminal(activeVertex, target)) {
                                target.parentEdge = edge;
                                target.distance = activeVertex.distance + 1;
                                target.timestamp = activeVertex.timestamp;
                            }
                        }
                    }
                }
            } else {
                assert activeVertex.isSinkTreeVertex();

                // the logic for processing sink tree vertices is symmetrical
                for (AnnotatedFlowEdge edge : activeVertex.getOutgoing()) {

                    if (edge.hasCapacity()) {
                        VertexExtension source = edge.getSource();

                        if (source.isSourceTreeVertex()) {

                            if (DEBUG) {
                                System.out.printf("Bounding edge = %s\n\n", edge);
                            }

                            return edge;
                        } else if (source.isFreeVertex()) {
                            if (DEBUG) {
                                System.out
                                    .printf(
                                        "Growing sink tree: %s -> %s\n\n", source.prototype, edge);
                            }

                            source.parentEdge = edge;
                            source.treeStatus = VertexTreeStatus.SINK_TREE_VERTEX;
                            source.distance = activeVertex.distance + 1;
                            source.timestamp = activeVertex.timestamp;
                            makeActive(source);
                        } else {
                            assert source.isSinkTreeVertex();

                            if (isCloserToTerminal(activeVertex, source)) {
                                source.parentEdge = edge;
                                source.distance = activeVertex.distance + 1;
                                source.timestamp = activeVertex.timestamp;
                            }
                        }
                    }
                }
            }

            // remove the vertex from the active vertex list
            finishVertex(activeVertex);
        }

        return null;
    }

    /**
     * Augments an s-t path specified using the {@code boundingEdge} and computes the set of tree
     * orphans emerged after augmentation.
     * <p>
     * First, the path flow bottleneck is found. Then the bottleneck flow value is pushed through
     * every path edge. If some path edge gets saturated, the corresponding tree node is added to
     * the orphan set. In the case the saturated edge connects source tree vertices, the edge target
     * becomes an orphan, otherwise if the saturated edge connects sink tree vertices, that the edge
     * source becomes an orphan.
     *
     * @param boundingEdge s-t path bounding edge between source and sink trees.
     */
    private void augment(AnnotatedFlowEdge boundingEdge)
    {
        double bottleneck = findBottleneck(boundingEdge);

        if (DEBUG) {
            Deque<AnnotatedFlowEdge> pathEdges = new ArrayDeque<>();

            pathEdges.addFirst(boundingEdge);

            VertexExtension debugSource = boundingEdge.getSource();
            while (debugSource != currentSource) {
                pathEdges.addFirst(debugSource.parentEdge);
                debugSource = debugSource.parentEdge.getSource();
            }

            VertexExtension debugTarget = boundingEdge.getTarget();
            while (debugTarget != currentSink) {
                pathEdges.addLast(debugTarget.parentEdge);
                debugTarget = debugTarget.parentEdge.getTarget();
            }

            System.out.printf("Pushing %.0f flow through path:\n", bottleneck);
            for (AnnotatedFlowEdge edge : pathEdges) {
                System.out
                    .printf("(%s, %s) - ", edge.getSource().prototype, edge.getTarget().prototype);
            }
            System.out.println("\n");
        }

        pushFlowThrough(boundingEdge, bottleneck);

        // pushing flow through source tree part of the path
        VertexExtension source = boundingEdge.getSource();
        while (source != currentSource) {
            AnnotatedFlowEdge parentEdge = source.parentEdge;

            pushFlowThrough(parentEdge, bottleneck);
            if (!parentEdge.hasCapacity()) {
                source.makeOrphan();
                orphans.add(source);
            }

            source = parentEdge.getSource();
        }

        // pushing flow through sink tree part of the path
        VertexExtension target = boundingEdge.getTarget();
        while (target != currentSink) {
            AnnotatedFlowEdge parentEdge = target.parentEdge;

            pushFlowThrough(target.parentEdge, bottleneck);
            if (!parentEdge.hasCapacity()) {
                target.makeOrphan();
                orphans.add(target);
            }

            target = parentEdge.getTarget();
        }

        maxFlowValue += bottleneck;
    }

    /**
     * Finds augmenting path bottleneck by traversing the path edges.
     *
     * @param boundingEdge s-t path bounding edge.
     * @return the computed bottleneck.
     */
    private double findBottleneck(AnnotatedFlowEdge boundingEdge)
    {
        double bottleneck = boundingEdge.getResidualCapacity();

        VertexExtension source = boundingEdge.getSource();
        while (source != currentSource) {
            bottleneck = Math.min(bottleneck, source.parentEdge.getResidualCapacity());
            source = source.parentEdge.getSource();
        }

        VertexExtension target = boundingEdge.getTarget();
        while (target != currentSink) {
            bottleneck = Math.min(bottleneck, target.parentEdge.getResidualCapacity());
            target = target.parentEdge.getTarget();
        }

        return bottleneck;
    }

    /**
     * Adopts all orphans.
     * <p>
     * Processing every orphan, the goal of this procedure is to either find a parent node within
     * the same tree, or identify that no such parent can be found, make the orphan a free vertex
     * and process all descendants of this node the same way. If multiple parents exist, the closest
     * to terminal is selected using distance and timestamp heuristic.
     */
    private void adopt()
    {
        while (!orphans.isEmpty() || !childOrphans.isEmpty()) {
            VertexExtension currentVertex;

            // child orphans take precedence
            if (childOrphans.isEmpty()) {
                currentVertex = orphans.get(orphans.size() - 1);
                orphans.remove(orphans.size() - 1);
            } else {
                currentVertex = childOrphans.removeLast();
            }

            if (currentVertex.isSourceTreeVertex()) {

                AnnotatedFlowEdge newParentEdge = null;
                int minDistance = Integer.MAX_VALUE;
                // find a parent edge which source has the smaller distance
                // to a terminal vertex according the distance heuristic

                for (AnnotatedFlowEdge edge : currentVertex.getOutgoing()) {
                    if (edge.getInverse().hasCapacity()) {
                        VertexExtension targetNode = edge.getTarget();

                        if (targetNode.isSourceTreeVertex()
                            && hasConnectionToTerminal(targetNode))
                        {
                            if (targetNode.distance < minDistance) {
                                minDistance = targetNode.distance;
                                newParentEdge = edge.getInverse();
                            }
                        }
                    }
                }

                if (newParentEdge == null) {

                    if (DEBUG) {
                        System.out.printf("Vertex %s becomes free\n\n", currentVertex.prototype);
                    }

                    // can't adopt this vertex
                    currentVertex.timestamp = FREE_NODE_TIMESTAMP;
                    currentVertex.treeStatus = VertexTreeStatus.FREE_VERTEX;

                    for (AnnotatedFlowEdge edge : currentVertex.getOutgoing()) {
                        VertexExtension targetVertex = edge.getTarget();
                        if (targetVertex.isSourceTreeVertex()) {
                            if (edge.getInverse().hasCapacity()) {
                                makeActive(targetVertex);
                            }
                            if (targetVertex.parentEdge == edge) {
                                // target vertex is a child of the current vertex
                                targetVertex.makeOrphan();
                                childOrphans.addFirst(targetVertex);
                            }
                        }
                    }
                } else {

                    if (DEBUG) {
                        System.out
                            .printf(
                                "Vertex %s get's adopted via %s\n\n", currentVertex.prototype,
                                newParentEdge);
                    }
                    // adopt this vertex
                    makeCheckedInThisIteration(currentVertex);
                    currentVertex.parentEdge = newParentEdge;
                    currentVertex.distance = minDistance + 1;
                }

            } else {
                // current node is from sink tree
                // the processing logic is symmetrical
                assert currentVertex.isSinkTreeVertex();

                AnnotatedFlowEdge newParentEdge = null;
                int minDistance = Integer.MAX_VALUE;
                for (AnnotatedFlowEdge edge : currentVertex.getOutgoing()) {
                    if (edge.hasCapacity()) {
                        VertexExtension targetNode = edge.getTarget();

                        if (targetNode.isSinkTreeVertex() && hasConnectionToTerminal(targetNode)) {
                            if (targetNode.distance < minDistance) {
                                minDistance = targetNode.distance;
                                newParentEdge = edge;
                            }
                        }
                    }
                }

                if (newParentEdge == null) {

                    if (DEBUG) {
                        System.out.printf("Vertex %s becomes free\n\n", currentVertex.prototype);
                    }

                    // can't adopt this vertex
                    currentVertex.timestamp = FREE_NODE_TIMESTAMP;
                    currentVertex.treeStatus = VertexTreeStatus.FREE_VERTEX;

                    for (AnnotatedFlowEdge edge : currentVertex.getOutgoing()) {
                        VertexExtension targetVertex = edge.getTarget();
                        if (targetVertex.isSinkTreeVertex()) {
                            if (edge.hasCapacity()) {
                                makeActive(targetVertex);
                            }
                            if (targetVertex.parentEdge == edge.getInverse()) {
                                // target vertex is a child of the current vertex
                                targetVertex.makeOrphan();
                                childOrphans.addFirst(targetVertex);
                            }
                        }
                    }
                } else {

                    if (DEBUG) {
                        System.out
                            .printf(
                                "Vertex %s get's adopted via %s\n\n", currentVertex.prototype,
                                newParentEdge);
                    }
                    // adopt this vertex
                    makeCheckedInThisIteration(currentVertex);
                    currentVertex.parentEdge = newParentEdge;
                    currentVertex.distance = minDistance + 1;
                }
            }
        }
    }

    /**
     * Initializes a new algorithm iteration.
     */
    private void nextIteration()
    {
        currentTimestamp++;
        makeCheckedInThisIteration(currentSource);
        makeCheckedInThisIteration(currentSink);
    }

    /**
     * Makes the {@code vertex} an active vertex.
     *
     * @param vertex network vertex.
     */
    private void makeActive(VertexExtension vertex)
    {
        if (!vertex.active) {
            vertex.active = true;
            activeVertices.addFirst(vertex);
        }
    }

    /**
     * Returns the next active vertex to be processed.
     *
     * @return the next active vertex to be processed.
     */
    private VertexExtension nextActiveVertex()
    {
        while (!activeVertices.isEmpty()) {
            VertexExtension nextActive = activeVertices.getLast();
            assert nextActive.active;
            if (!nextActive.isFreeVertex()) {
                return nextActive;
            } else {
                activeVertices.removeLast();
                nextActive.active = false;
            }
        }
        return null;
    }

    /**
     * Makes the {@code vertex} inactive.
     *
     * @param vertex network vertex.
     */
    private void finishVertex(VertexExtension vertex)
    {
        assert activeVertices.getLast() == vertex;
        activeVertices.pollLast();
        vertex.active = false;
    }

    /**
     * Sets the timestamp of the {@code vertex} equal to the {@code currentTimestamp}.
     *
     * @param vertex network vertex.
     */
    private void makeCheckedInThisIteration(VertexExtension vertex)
    {
        vertex.timestamp = currentTimestamp;
    }

    /**
     * Checks if the distance of the {@code vertex} was updated during this iteration.
     *
     * @param vertex network vertex.
     * @return {@code true} if the distance of the {@code vertex} was updated in this iteration,
     *         {@code false} otherwise.
     */
    private boolean wasCheckedInThisIteration(VertexExtension vertex)
    {
        return vertex.timestamp == currentTimestamp;
    }

    /**
     * Checks if the {@code vertex} is connected to a terminal vertex (source or sink).
     *
     * @param vertex network vertex.
     * @return {@code true} if the {@code vertex} is connected to a terminal vertex, {@code false}
     *         otherwise.
     */
    private boolean hasConnectionToTerminal(VertexExtension vertex)
    {
        int distance = 0;

        for (VertexExtension currentVertex = vertex;
            currentVertex != currentSource && currentVertex != currentSink;
            currentVertex = currentVertex.getParent())
        {

            if (currentVertex.parentEdge == null) {
                return false;
            } else if (wasCheckedInThisIteration(vertex)) {
                distance += currentVertex.distance;
                break;
            }
            distance++;
        }

        // update distance and timestamp values for every path vertex
        for (VertexExtension currentVertex = vertex; !wasCheckedInThisIteration(currentVertex);
            currentVertex = currentVertex.getParent())
        {

            currentVertex.distance = distance;
            distance--;
            makeCheckedInThisIteration(currentVertex);
        }

        return true;
    }

    /**
     * Checks if the vertex {@code p} is closer to terminal than the vertex {@code t} using the
     * distance heuristic.
     *
     * @param p network vertex.
     * @param t network vertex.
     * @return {@code true} is {@code p} is closer to terminal than {@code t}, {@code false}
     *         otherwise.
     */
    private boolean isCloserToTerminal(VertexExtension p, VertexExtension t)
    {
        return p.timestamp >= t.timestamp && p.distance + 1 < t.distance;
    }

    /**
     * Returns a vertex extension which corresponds to the network {@code vertex}.
     *
     * @param vertex network vertex.
     * @return a vertex extension which corresponds to the network {@code vertex}.
     */
    private VertexExtension getVertexExtension(V vertex)
    {
        return (VertexExtension) vertexExtensionManager.getExtension(vertex);
    }

    /**
     * Enum specifying vertex tree status
     */
    private enum VertexTreeStatus
    {
        SOURCE_TREE_VERTEX
        {
            @Override
            public String toString()
            {
                return "SOURCE_TREE_VERTEX";
            }
        },
        SINK_TREE_VERTEX
        {
            @Override
            public String toString()
            {
                return "SINK_TREE_VERTEX";
            }
        },
        FREE_VERTEX
        {
            @Override
            public String toString()
            {
                return "FREE_VERTEX";
            }
        };

        public abstract String toString();
    }

    /**
     * Network vertex extension used to store auxiliary vertex information.
     */
    private class VertexExtension
        extends
        VertexExtensionBase
    {

        /**
         * This vertex timestamp. The timestamp is the last iteration in which the distance to
         * terminal of this vertex was updated. If this value isn't equal to the most recent
         * iteration index, the distance value may be outdated.
         */
        long timestamp;
        /**
         * The distance of this vertex to a terminal vertex (network source or sink). This value may
         * not represent the actual distance as it's not updated every iteration.
         */
        int distance;
        /**
         * If this vertex is in the active vertex list.
         */
        boolean active;

        /**
         * Edge to the tree parent.
         */
        AnnotatedFlowEdge parentEdge;
        /**
         * Tree status of this vertex.
         */
        VertexTreeStatus treeStatus;

        /**
         * Creates a new free vertex.
         */
        VertexExtension()
        {
            parentEdge = null;
            treeStatus = VertexTreeStatus.FREE_VERTEX;
        }

        /**
         * Checks if this vertex belongs to the source tree.
         *
         * @return {@code true} if this vertex belongs to the source tree, {@code false} otherwise.
         */
        boolean isSourceTreeVertex()
        {
            return treeStatus == VertexTreeStatus.SOURCE_TREE_VERTEX;
        }

        /**
         * Checks if this vertex belongs to the sink tree.
         *
         * @return {@code true} if this vertex belongs to the sink tree, {@code false} otherwise.
         */
        boolean isSinkTreeVertex()
        {
            return treeStatus == VertexTreeStatus.SINK_TREE_VERTEX;
        }

        /**
         * Checks if this vertex belongs to no tree, i.e. is a free vertex.
         *
         * @return {@code true} if this vertex is free, {@code false} otherwise.
         */
        boolean isFreeVertex()
        {
            return treeStatus == VertexTreeStatus.FREE_VERTEX;
        }

        /**
         * Disconnects this vertex from its parent.
         */
        void makeOrphan()
        {
            parentEdge = null;
        }

        /**
         * Returns the parent of this vertex.
         *
         * @return the parent of this vertex.
         */
        VertexExtension getParent()
        {
            assert parentEdge != null;
            return this == parentEdge.getSource() ? parentEdge.getTarget() : parentEdge.getSource();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return String
                .format(
                    "{%s}: parent_edge = %s, tree_status = %s, distance = %d, timestamp = %d",
                    prototype,
                    parentEdge == null ? "null"
                        : String
                            .format(
                                "(%s, %s)", parentEdge.getSource().prototype,
                                parentEdge.getTarget().prototype),
                    treeStatus, distance, timestamp);

        }
    }
}
