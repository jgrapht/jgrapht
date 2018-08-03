/*
 * (C) Copyright 2018-2018, by Timofey Chudakov and Contributors.
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
package org.jgrapht.alg.flow.min_cost;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MinimumCostFlowAlgorithm;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

import java.util.*;

/**
 * This class computes a solution to a <a href="https://en.wikipedia.org/wiki/Minimum-cost_flow_problem">
 * minimum cost flow problem</a> using the successive shortest path algorithm with capacity scaling.
 * More precisely, this class computes a b-flow of minimum cost, i.e. for each node $v$ in the network
 * the sum of all outgoing flows minus the sum of all incoming flows should be equal to the node supply $b_v$
 * <p>
 * This algorithm supports two modes: with and without scaling. An integral scaling factor can be specified
 * during construction time. If the specified scaling factor is less than 2, then the algorithm solves the
 * specified problem using regular successive shortest path. Currently the algorithm doesn't support undirected
 * flow networks. The algorithm also imposes two constraints on the directed flow networks, namely, is doesn't
 * support infinite capacity arcs with negative cost and self-loops aren't allowed. Note, that in this case a cost
 * of a flow on the network can be bounded from below by some constant, i.e. a feasible finite weight solution
 * can exist. The default scaling factor is {@link CapacityScalingMinimumCostFlow#DEFAULT_SCALING_FACTOR}.
 * <p>
 * An arc with capacity greater that ot equal to {@link CapacityScalingMinimumCostFlow#CAP_INF} is considered to be
 * an infinite capacity arc. The algorithm also uses {@link CapacityScalingMinimumCostFlow#COST_INF} during the computation,
 * therefore, the magnitude of the cost of any arc can exceed this values.
 * <p>
 * In the capacity scaling mode, the algorithm performs $\mathcal{O}(log_a U)$ $\Delta$-scaling phases, where $U$ is the
 * largest magnitude of any supply/demand or finite arc capacity, and $a$ is a scaling factor, which is considered to
 * be constant. During each $\Delta$-scaling phase the algorithm first ensures that all arc with capacity with capacity
 * greater than or equal to $\Delta$ satisfy optimality condition, i.e. its reduced cost must be non-negative
 * (saturated arcs don't belong to the residual network). After saturating all arcs in the $\Delta$-residual network with
 * negative reduced cost the sum of the excesses is bounded by $2\Delta(m + n)$. Since the algorithm ensures that each
 * augmentation carries at least $\Delta$ units of flow, at most $\mathcal{O}(m)$ flow augmentations are performed during
 * each scaling phase. Therefore, the overall running time of the algorithm with capacity scaling is
 * $\mathcal{O}(m\log_a U(m + n\log n))$, which is a weakly polynomial time bound.
 * <p>
 * If the algorithm is used without scaling, each flow augmentation carries at least $\mathcal{O}(1)$ flow units,
 * therefore the overall time complexity if $\mathcal{O}(nU(m + n\log n))$, which is a pseudo-polynomial time bound.
 * <p>
 * For more information about the capacity scaling algorithm see: <i>K. Ahuja, Ravindra &amp; L. Magnanti, Thomas &amp;
 * Orlin, James. (1993). Network Flows.</i> This implementation is based on the algorithm description presented
 * in this book.
 *
 * @param <V> graph vertex type
 * @param <E> graph edge type
 * @author Timofey Chudakov
 * @see MinimumCostFlowProblem
 * @see MinimumCostFlowAlgorithm
 * @since July 2018
 */
public class CapacityScalingMinimumCostFlow<V, E> implements MinimumCostFlowAlgorithm<V, E> {
    /**
     * A capacity which is considered to be infinite. Every arc, which has upper capacity greater that or
     * equal to this value is considered to be an infinite capacity arc.
     */
    public static final int CAP_INF = 1000 * 1000 * 1000;
    /**
     * A cost which is considered to be infinite. This value is used internally for flow network transformation.
     * That is why arcs with cost magnitude greater than or equal to this value are not allowed.
     */
    public static final double COST_INF = 1e9;
    /**
     * Default scaling factor
     */
    public static final int DEFAULT_SCALING_FACTOR = 2;
    /**
     * Debug variable
     */
    private static final boolean DEBUG = false;
    /**
     * Scaling factor of this algorithm
     */
    private final int scalingFactor;
    /**
     * Variable that is used to determine whether a vertex has been labeled temporarily or permanently during
     * Dijkstra's algorithm
     */
    private int counter = 1;
    /**
     * Specified minimum cost flow problem
     */
    private MinimumCostFlowProblem<V, E> problem;
    /**
     * Computed minimum cost flow
     */
    private MinimumCostFLow<E> minimumCostFLow;
    /**
     * Array of internal nodes used by the algorithm
     */
    private Node[] nodes;
    /**
     * Array of internal arcs. Note: these arcs are stored in the same order as edges of the specified flow network.
     * This allows to determine quickly their counterparts in the graph.
     */
    private Arc[] arcs;
    /**
     * List of edges of the flow network.
     */
    private List<E> graphEdges;
    /**
     * Number of vertices in the network
     */
    private int n;
    /**
     * Number of edges in the network
     */
    private int m;

    /**
     * Constructs a new instance of the algorithm which uses default scaling factor.
     *
     * @param problem a minimum cost flow problem
     */
    public CapacityScalingMinimumCostFlow(MinimumCostFlowProblem<V, E> problem) {
        this(problem, DEFAULT_SCALING_FACTOR);
    }

    /**
     * Constructs a new instance of the algorithm with custom {@code scalingFactor}. If the {@code scalingFactor}
     * is less than 2, the algorithm doesn't use scaling.
     *
     * @param problem       a minimum cost flow problem
     * @param scalingFactor custom scaling factor
     */
    public CapacityScalingMinimumCostFlow(MinimumCostFlowProblem<V, E> problem, int scalingFactor) {
        if (problem.graph.getType().isUndirected()) {
            throw new IllegalArgumentException("The algorithm doesn't support undirected flow networks");
        }
        this.problem = Objects.requireNonNull(problem);
        this.scalingFactor = scalingFactor;
        n = problem.graph.vertexSet().size();
        m = problem.graph.edgeSet().size();
        Node.ID = 0; // for debug
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getFlowDirection(E edge) {
        return problem.graph.getEdgeTarget(edge);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MinimumCostFLow<E> getMinimumCostFlow() {
        if (minimumCostFLow == null) {
            lazyCalculateMinimumCostFlow();
        }
        return minimumCostFLow;
    }

    /**
     * Tests the optimality conditions after a flow of minimum cost has been computed.
     * <p>
     * More precisely, tests, whether the reduced cost of every arc in the residual network is non-negative.
     * This validation is performed with precision of {@code eps}. If the solution doesn't meet this condition,
     * returns, false. Otherwise, returns true.
     *
     * @param eps the precision to use
     * @return true, if the computed solution is optimal, false otherwise.
     */
    public boolean testOptimality(double eps) {
        lazyCalculateMinimumCostFlow();
        for (Node node : nodes) {
            for (Arc arc = node.firstNonSaturated; arc != null; arc = arc.next) {
                if (arc.getReducedCost() < -eps) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Converts the flow network in the form convenient for the algorithm. Validated the arc capacities and costs.
     * <p>
     * Also, adds a dummy node to the network and arcs from every node to this dummy node, and from this dummy node
     * to every other node. These added arcs have infinite capacities {@link CapacityScalingMinimumCostFlow#CAP_INF}
     * and infinite costs {@link CapacityScalingMinimumCostFlow#COST_INF}. This ensures, that every search for an
     * augmenting path to send at least $\Delta$ units of flow succeeds.
     * <p>
     * If the flow network has a feasible solution, at the end there will be no flow on the added arcs.
     * Otherwise, the specified problem has no feasible solution.
     */
    private void init() {
        int supply, upperCap, lowerCap;
        double cost;
        int supplySum = 0;
        Node node, opposite;

        // initialize data structures
        nodes = new Node[n + 1];
        nodes[n] = new Node(0); // dummy node
        arcs = new Arc[m];
        graphEdges = new ArrayList<>(m);

        Map<V, Node> nodeMap = new HashMap<>(n);
        Graph<V, E> graph = problem.graph;

        // convert vertices into internal nodes
        int i = 0;
        for (V vertex : graph.vertexSet()) {
            supply = problem.supplyMap.getOrDefault(vertex, 0);
            supplySum += supply;
            nodes[i] = new Node(supply);
            nodeMap.put(vertex, nodes[i]);
            // reduction
            nodes[i].addArcTo(nodes[n], CAP_INF, COST_INF);
            nodes[n].addArcTo(nodes[i], CAP_INF, COST_INF);
            ++i;
        }
        if (Math.abs(supplySum) > 0) {
            throw new IllegalArgumentException("Total node supply isn't equal to 0");
        }
        i = 0;
        // convert edges into their internal counterparts
        for (E edge : graph.edgeSet()) {
            graphEdges.add(edge);
            node = nodeMap.get(graph.getEdgeSource(edge));
            opposite = nodeMap.get(graph.getEdgeTarget(edge));
            upperCap = problem.upperCapacityMap.get(edge);
            lowerCap = problem.lowerCapacityMap.getOrDefault(edge, 0);
            cost = problem.graph.getEdgeWeight(edge);

            if (upperCap < 0) {
                throw new IllegalArgumentException("Negative edge capacities are not allowed");
            } else if (lowerCap > upperCap) {
                throw new IllegalArgumentException("Lower edge capacity must not exceed upper edge capacity");
            } else if (lowerCap >= CAP_INF) {
                throw new IllegalArgumentException("The problem is unbounded due to the infinite lower capacity");
            } else if (upperCap >= CAP_INF && cost < 0) {
                throw new IllegalArgumentException("The algorithm doesn't support infinite capacity arcs with negative cost");
            } else if (Math.abs(cost) >= COST_INF) {
                throw new IllegalArgumentException("Specified flow network contains an edge of infinite cost");
            } else if (node == opposite) {
                throw new IllegalArgumentException("Self-loops aren't allowed");
            }
            // remove non-zero lower capacity
            node.excess -= lowerCap;
            opposite.excess += lowerCap;
            if (cost < 0) {
                // removing negative edge costs
                node.excess -= upperCap - lowerCap;
                opposite.excess += upperCap - lowerCap;
                Node t = node;
                node = opposite;
                opposite = t;
                cost *= -1;
            }
            arcs[i] = node.addArcTo(opposite, upperCap - lowerCap, cost);
            if (DEBUG) {
                System.out.println(arcs[i]);
            }
            ++i;
        }
        if (DEBUG) {
            System.out.println("Printing mapping");
            for (Map.Entry<V, Node> entry : nodeMap.entrySet()) {
                System.out.println(entry + " -> " + entry);
            }
        }
    }

    /**
     * Lazily calculated a solution to the specified minimum cost flow problem. If the scaling factor is greater than 1,
     * performs scaling phases, otherwise uses simple capacity scaling algorithm.
     */
    private void lazyCalculateMinimumCostFlow() {
        if (minimumCostFLow != null) {
            return;
        }
        Pair<List<Node>, Set<Node>> pair;
        init();
        if (scalingFactor > 1) {
            // run with scaling
            int U = getU();
            int delta = scalingFactor;
            while (U >= delta) {
                delta *= scalingFactor;
            }
            delta /= scalingFactor;
            while (delta >= 1) {
                pair = scale(delta);
                pushAllFlow(pair.getFirst(), pair.getSecond(), delta);
                delta /= scalingFactor;
            }
        } else {
            // run without scaling
            pair = scale(1);
            pushAllFlow(pair.getFirst(), pair.getSecond(), 1);
        }
        minimumCostFLow = finish();
    }

    /**
     * For every node in the {@code positiveExcessNodes} pushes all flow from it until its excess is less than
     * {@code delta}. This is always possible due to the performed flow network reduction during the initialization
     * phase.
     *
     * @param positiveExcessNodes nodes from the network with positive excesses no less than {@code delta}
     * @param negativeExcessNodes nodes from the network with negative excesses no greater than {@code delta}
     * @param delta               the current value of $\Delta$
     */
    private void pushAllFlow(List<Node> positiveExcessNodes, Set<Node> negativeExcessNodes, int delta) {
        for (Node node : positiveExcessNodes) {
            while (node.excess >= delta) {
                if (negativeExcessNodes.isEmpty()) {
                    return;
                }
                pushDijkstra(node, negativeExcessNodes, delta);
            }
        }
    }

    /**
     * Performs a scaling phase by saturating all negative reduced cost arcs with residual capacity greater
     * than or equal to the {@code delta}, so that they don't belong to the $\Delta$-residual network and,
     * hence, don't violate optimality conditions. After that this method computes and returns nodes with
     * positive excess greater than or equal to the {@code delta} and nodes with negative excesses that
     * are less than or equal to {@code delta}
     *
     * @param delta current value of $\Delta$
     * @return the nodes with excesses no less than {@code delta} and no greater than {@code -delta}
     */
    private Pair<List<Node>, Set<Node>> scale(int delta) {
        if (DEBUG) {
            System.out.println(String.format("Current delta = %d", delta));
        }
        int residualCapacity;
        Arc arc, nextArc;

        // saturate all non-saturated arcs with negative edge costs in the delta-residual network
        for (Node node : nodes) {
            nextArc = node.firstNonSaturated;
            for (arc = nextArc; arc != null; arc = nextArc) {
                nextArc = nextArc.next;
                residualCapacity = arc.residualCapacity;
                if (arc.residualCapacity >= delta && arc.getReducedCost() < 0) {
                    if (DEBUG) {
                        System.out.println("Saturating arc " + arc);
                    }
                    arc.sendFlow(residualCapacity);
                    arc.head.excess += residualCapacity;
                    arc.revArc.head.excess -= residualCapacity;
                }
            }
        }

        // finding all nodes with excess magnitude no less than delta
        List<Node> positiveExcessNodes = new ArrayList<>();
        Set<Node> negativeExcessNodes = new HashSet<>();
        for (Node node : nodes) {
            if (node.excess >= delta) {
                positiveExcessNodes.add(node);
            } else if (node.excess <= -delta) {
                negativeExcessNodes.add(node);
            }
        }
        return new Pair<>(positiveExcessNodes, negativeExcessNodes);
    }

    /**
     * Returns the largest magnitude of any supply/demand or finite arc capacity.
     *
     * @return the largest magnitude of any supply/demand or finite arc capacity.
     */
    private int getU() {
        int result = 0;
        for (Node node : nodes) {
            result = Math.max(result, Math.abs(node.excess));
        }
        for (Arc arc : arcs) {
            if (!arc.isInfiniteCapacityArc()) {
                result = Math.max(result, arc.residualCapacity);
            }
        }
        return result;
    }

    /**
     * Runs the Dijkstra's algorithm in the residual network using {@link Arc#getReducedCost()} as arc distances.
     * <p>
     * After reaching a node with excess no greater than {@code -delta}, augments it. Since the search is performed
     * in the $\Delta$-residual network, the augmentation carries at least {@code delta} units of flow. The search always
     * succeeds due to the flow network reduction performed during the initialization phase.
     * <p>
     * Updates the potentials of the nodes so than they:
     * <ul>
     * <li>Satisfy optimality conditions in the $\Delta$-residual network</li>
     * <li>The reduced cost of the augmented path is equal to $0$</li>
     * </ul>
     *
     * @param start               the start node for Dijkstra's algorithm
     * @param negativeExcessNodes nodes from the network with negative excesses no greater than {@code delta}
     * @param delta               the current value of $\Delta$
     */
    private void pushDijkstra(Node start, Set<Node> negativeExcessNodes, int delta) {
        Node currentNode, opposite;
        Arc currentArc;
        double distance;
        FibonacciHeapNode<Node> currentFibNode;
        int TEMPORARILY_LABELED = counter++;
        int PERMANENTLY_LABELED = counter++;
        FibonacciHeap<Node> heap = new FibonacciHeap<>();
        List<Node> permanentlyLabeled = new LinkedList<>();
        start.parentArc = null;
        insertIntoHeap(heap, start, 0);

        while (!heap.isEmpty()) {
            currentFibNode = heap.removeMin();
            currentNode = currentFibNode.getData();
            distance = currentFibNode.getKey();
            if (negativeExcessNodes.contains(currentNode)) {
                // the path to push at least delta units of flow is found
                augmentPath(start, currentNode);
                if (currentNode.excess > -delta) {
                    negativeExcessNodes.remove(currentNode);
                }
                // updating potentials
                for (Node node : permanentlyLabeled) {
                    node.potential += distance;
                }
                if (DEBUG) {
                    System.out.println(String.format("Distance = %.1f", distance));
                    for (Node node : nodes) {
                        System.out.println(String.format("Id = %d, potential = %.1f", node.id, node.potential));
                    }
                }
                return;
            }
            currentNode.labelType = PERMANENTLY_LABELED; // currentNode becomes permanently labeled
            permanentlyLabeled.add(currentNode);
            for (currentArc = currentNode.firstNonSaturated; currentArc != null; currentArc = currentArc.next) {
                // looking only for arcs with residual capacity greater than delta
                if (currentArc.residualCapacity < delta) {
                    continue;
                }
                opposite = currentArc.head;
                if (opposite.labelType != PERMANENTLY_LABELED) {
                    if (opposite.labelType == TEMPORARILY_LABELED) {
                        // opposite has been labeled already
                        if (distance + currentArc.getReducedCost() < opposite.fibNode.getKey()) {
                            heap.decreaseKey(opposite.fibNode, distance + currentArc.getReducedCost());
                            opposite.parentArc = currentArc;
                        }
                    } else {
                        // opposite is encountered for the first time
                        opposite.labelType = TEMPORARILY_LABELED;
                        insertIntoHeap(heap, opposite, distance + currentArc.getReducedCost());
                        opposite.parentArc = currentArc;
                    }
                }
            }
            currentNode.potential -= distance; // allows not to store the distances of the nodes
        }
    }

    /**
     * Augments the path from {@code start} to the {@code end} sending as much flow as possible. Uses
     * {@link Node#parentArc} computed by the Dijkstra's algorithm. Updates the excesses of the
     * {@code start} and the {@code end} nodes.
     *
     * @param start the start of the augmenting path
     * @param end   the end of the augmenting path
     */
    private void augmentPath(Node start, Node end) {
        // compute delta to augment
        int valueToAugment = Math.min(start.excess, -end.excess);
        for (Arc arc = end.parentArc; arc != null; arc = arc.revArc.head.parentArc) {
            valueToAugment = Math.min(valueToAugment, arc.residualCapacity);
        }
        if (DEBUG) {
            ArrayList<Node> stack = new ArrayList<>();
            for (Arc arc = end.parentArc; arc != null; arc = arc.revArc.head.parentArc) {
                stack.add(arc.head);
            }
            stack.add(start);
            System.out.println("Printing augmenting path");
            for (int i = stack.size() - 1; i > 0; i--) {
                System.out.print(stack.get(i).id + " -> ");
            }
            System.out.println(stack.get(0).id + ", delta = " + valueToAugment);
        }
        // augmenting the flow
        end.excess += valueToAugment;
        for (Arc arc = end.parentArc; arc != null; arc = arc.revArc.head.parentArc) {
            arc.sendFlow(valueToAugment);
        }
        start.excess -= valueToAugment;
    }

    /**
     * Helper method for correct insertions of nodes into the Fibonacci heap.
     *
     * @param heap a heap to insert node into
     * @param node a node to insert into the heap
     * @param key  the key of the {@code node}
     */
    private void insertIntoHeap(FibonacciHeap<Node> heap, Node node, double key) {
        node.fibNode = new FibonacciHeapNode<>(node);
        heap.insert(node.fibNode, key);
    }

    /**
     * Finishes the computation by checking the flow feasibility, computing arc flows, and creating an instance
     * of {@link MinimumCostFLow}. The resulting flow mapping contains all edges of the specified minimum cost
     * flow problem.
     *
     * @return the solution to the minimum cost flow problem
     */
    private MinimumCostFLow<E> finish() {
        Map<E, Integer> flowMap = new HashMap<>(m);
        E graphEdge;
        int flowOnArc;
        double totalCost = 0;
        // check feasibility
        for (Arc arc = nodes[n].firstNonSaturated; arc != null; arc = arc.next) {
            if (arc.revArc.residualCapacity > 0) {
                throw new IllegalArgumentException("Specified flow network problem has no feasible solution");
            }
        }
        Arc arc;
        // create the solution object
        for (int i = 0; i < m; i++) {
            graphEdge = graphEdges.get(i);
            arc = arcs[i];
            flowOnArc = arc.revArc.residualCapacity; // this value equals to the flow on the initial arc
            if (problem.graph.getEdgeWeight(graphEdge) < 0) {
                // the initial arc goes in the opposite direction
                flowOnArc = problem.upperCapacityMap.get(graphEdge) - problem.lowerCapacityMap.getOrDefault(graphEdge, 0) - flowOnArc;
            }
            flowOnArc += problem.lowerCapacityMap.getOrDefault(graphEdge, 0);
            flowMap.put(graphEdge, flowOnArc);
            totalCost += flowOnArc * problem.graph.getEdgeWeight(graphEdge);
        }
        return new MinimumCostFlowImpl<>(totalCost, flowMap);
    }

    /**
     * Supporting data structure for the {@link CapacityScalingMinimumCostFlow}.
     * <p>
     * Is used as an internal representation of the vertices of the flow network. Contains all information
     * needed during the computation.
     *
     * @author Timofey Chudakov
     * @since July 2018
     */
    private static class Node {
        /**
         * Variable for debug purposes
         */
        private static int ID = 0;
        /**
         * Reference to the {@link FibonacciHeapNode} this node is contained in
         */
        FibonacciHeapNode<Node> fibNode;
        /**
         * An arc on the augmenting path which head is this node.
         */
        Arc parentArc;
        /**
         * The label of this node. Is used to distinguish temporarily and permanently labeled nodes during
         * the Dijkstra's algorithm
         */
        int labelType;
        /**
         * The excess of this node. If this value is positive, then this is a source node. If this value is 0, than
         * this is a transhipment node. If this value if negative, this is a sink node.
         */
        int excess;
        /**
         * The dual variable of this node. This is used to search for an augmenting path in the residual network
         * using the reduced costs of the arcs as arc lengths.
         */
        double potential;
        /**
         * Reference of the first <em>outgoing</em> saturated arc (with zero residual capacity) incident to this node
         */
        Arc firstSaturated;
        /**
         * Reference of the first <em>outgoing</em> non-saturated arc (with positive residual capacity) incident
         * to this node.
         */
        Arc firstNonSaturated;
        /**
         * Variable for debug purposes
         */
        private int id = ID++;

        /**
         * Constructs a new node with {@code excess}
         *
         * @param excess the excess of this node
         */
        public Node(int excess) {
            this.excess = excess;
        }

        /**
         * Adds a new arc with {@code capacity}, {@code cost} to the {@code opposite}. This method also creates
         * a reverse arc with zero capacity and {@code -cost}.
         *
         * @param opposite the head of the resulting arc.
         * @param capacity the capacity of the resulting arc.
         * @param cost     the cost of the resulting arc
         * @return the resulting arc to the {@code opposite} node
         */
        Arc addArcTo(Node opposite, int capacity, double cost) {
            Arc forwardArc = new Arc(opposite, capacity, cost);
            if (capacity > 0) {
                // forward arc becomes the first arc in the linked list of non-saturated arcs
                if (firstNonSaturated != null) {
                    firstNonSaturated.prev = forwardArc;
                }
                forwardArc.next = firstNonSaturated;
                firstNonSaturated = forwardArc;
            } else {
                // forward arc becomes the first arc in the linked list of saturated arcs
                if (firstSaturated != null) {
                    firstSaturated.prev = forwardArc;
                }
                forwardArc.next = firstSaturated;
                firstSaturated = forwardArc;
            }
            Arc reverseArc = new Arc(this, 0, -cost);
            if (opposite.firstSaturated != null) {
                opposite.firstSaturated.prev = reverseArc;
            }
            reverseArc.next = opposite.firstSaturated;
            opposite.firstSaturated = reverseArc;

            forwardArc.revArc = reverseArc;
            reverseArc.revArc = forwardArc;

            return forwardArc;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return String.format("Id = %d, excess = %d, potential = %.1f", id, excess, potential);
        }
    }

    /**
     * Supporting data structure for the {@link CapacityScalingMinimumCostFlow}.
     * <p>
     * Represents a directed edge (arc) in the residual flow network. Contains all information needed during
     * the computation.
     *
     * @author Timofey Chudakov
     * @since July 2018
     */
    private static class Arc {
        /**
         * The head (target) of this arc.
         */
        final Node head;
        /**
         * The cost of sending one unit of flow across this arc. This value is positive for initial network arcs,
         * negative - for the reverse residual arcs, and equals to the {@link CapacityScalingMinimumCostFlow#COST_INF}
         * for the arcs used for the reduction.
         */
        final double cost;
        /**
         * The reverse counterpart of this arc.
         */
        Arc revArc;
        /**
         * The previous arc. This variable is used to maintain the presence of this arc in the linked list of arc
         * which are either saturated or not.
         */
        Arc prev;
        /**
         * The next arc. This variable is used to maintain the presence of this arc in the linked list of arc
         * which are either saturated or not.
         */
        Arc next;
        /**
         * The residual capacity of this arc. For forward arcs $(i, j)$ it equals $c_{i, j} - x_{i, j}$ where
         * $x_{i, j}$ is the flow on this arc. For reverse arcs it equals $x_{i,j}$.
         */
        int residualCapacity;

        /**
         * Creates a new arc
         *
         * @param head             the head (target) of this arc
         * @param residualCapacity its residual capacity
         * @param cost             its cost
         */
        Arc(Node head, int residualCapacity, double cost) {
            this.head = head;
            this.cost = cost;
            this.residualCapacity = residualCapacity;
        }

        /**
         * Returns reduced cost of this arc.
         *
         * @return reduced cost of this arc.
         */
        double getReducedCost() {
            return cost + head.potential - revArc.head.potential;
        }

        /**
         * Sends {@code value units of flow across this arc}.
         *
         * @param value how many units of flow to send
         */
        void sendFlow(int value) {
            decreaseResidualCapacity(value);
            revArc.increaseResidualCapacity(value);
        }

        /**
         * Decreases residual capacity of this arc by {@code value} units of flow. Moves this arc from list of
         * non-saturated arc to the list of saturated arcs if necessary.
         *
         * @param value the value to subtract from the residual capacity of this arc
         */
        private void decreaseResidualCapacity(int value) {
            if (residualCapacity >= CAP_INF) {
                return;
            }
            residualCapacity -= value;
            if (residualCapacity == 0) {
                // need to move this arc from list of non-saturated arcs to list of saturated arcs
                Node tail = revArc.head;
                if (next != null) {
                    next.prev = prev;
                }
                if (prev != null) {
                    prev.next = next;
                } else {
                    tail.firstNonSaturated = next;
                }
                next = tail.firstSaturated;
                if (tail.firstSaturated != null) {
                    tail.firstSaturated.prev = this;
                }
                tail.firstSaturated = this;
                prev = null;
            }
        }

        /**
         * Increases residual capacity of this arc by {@code value} units of flow. Moves this arc from list of
         * saturated arc to the list of non-saturated arcs if necessary.
         *
         * @param value the value to add to the residual capacity of this arc
         */
        private void increaseResidualCapacity(int value) {
            if (residualCapacity >= CAP_INF) {
                return;
            }
            if (residualCapacity == 0) {
                // need to move this arc from list of saturated arcs to list of non-saturated arcs
                Node tail = revArc.head;
                if (next != null) {
                    next.prev = prev;
                }
                if (prev != null) {
                    prev.next = next;
                } else {
                    tail.firstSaturated = next;
                }
                next = tail.firstNonSaturated;
                if (tail.firstNonSaturated != null) {
                    tail.firstNonSaturated.prev = this;
                }
                tail.firstNonSaturated = this;
                prev = null;
            }
            residualCapacity += value;
        }

        /**
         * Returns true if the arc has infinite capacity, false otherwise.
         *
         * @return true if the arc has infinite capacity, false otherwise.
         */
        public boolean isInfiniteCapacityArc() {
            return residualCapacity >= CAP_INF;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return String.format("(%d, %d), residual capacity = %s, reduced cost = %.1f, cost = %.1f", revArc.head.id, head.id, residualCapacity >= CAP_INF ? "INF" : String.valueOf(residualCapacity), getReducedCost(), cost);
        }
    }
}
