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
package org.jgrapht.alg.matching.blossom_v;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.AsUndirectedGraph;

import java.util.*;

import static org.jgrapht.alg.matching.blossom_v.DualUpdater.DualUpdateStrategy.MULTIPLE_TREE_CONNECTED_COMPONENTS;
import static org.jgrapht.alg.matching.blossom_v.Initializer.InitializationType.GREEDY;
import static org.jgrapht.alg.matching.blossom_v.KolmogorovMinimumWeightPerfectMatching.SingleTreeDualUpdatePhase.UPDATE_DUAL_BEFORE;

/**
 * TODO: write complete class description
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 * @since June 2018
 */
public class KolmogorovMinimumWeightPerfectMatching<V, E> implements MatchingAlgorithm<V, E> {
    /**
     * Default epsilon used in the algorithm
     */
    public static final double EPS = 10e-9;
    /**
     * Default infinity value used in the algorithm
     */
    public static final double INFINITY = Integer.MAX_VALUE;
    /**
     * Defines the threshold for throwing an exception about no perfect matching existence
     */
    public static final double NO_PERFECT_MATCHING_THRESHOLD = INFINITY / 2;
    /**
     * Variable for debug purposes
     */
    static final boolean DEBUG = false;
    /**
     * Message about no perfect matching
     */
    static final String NO_PERFECT_MATCHING = "There is no perfect matching in the specified graph";
    /**
     * Default options
     */
    private static final Options DEFAULT_OPTIONS = new Options();

    /**
     * The graph we are matching on
     */
    private final Graph<V, E> graph;
    /**
     * Current state of the algorithm
     */
    private State<V, E> state;
    /**
     * Perform primal operations (grow, augment, shrink and expand)
     */
    private PrimalUpdater<V, E> primalUpdater;
    /**
     * Performs dual updates using the strategy defined by the {@code options}
     */
    private DualUpdater<V, E> dualUpdater;
    /**
     * The computed matching of the {@code graph}
     */
    private MatchingAlgorithm.Matching<V, E> matching;
    /**
     * Defines solution to the dual linear program formulated on the {@code graph}
     */
    private DualSolution dualSolution;
    /**
     * Options used by the algorithm to getMatching the problem instance
     */
    private Options options;

    /**
     * Constructs a new instance of the algorithm with the default options for it.
     *
     * @param graph the graph a minimum weight perfect matching would be searched in
     */
    public KolmogorovMinimumWeightPerfectMatching(Graph<V, E> graph) {
        this(graph, DEFAULT_OPTIONS);
    }

    /**
     * Constructs a new instance of the algorithm with the specified {@code options}
     *
     * @param graph   the graph a minimum weight perfect matching would be searched in
     * @param options the options which define the strategies for the initialization and dual updates
     */
    public KolmogorovMinimumWeightPerfectMatching(Graph<V, E> graph, Options options) {
        Objects.requireNonNull(graph);
        if ((graph.vertexSet().size() & 1) == 1) {
            throw new IllegalArgumentException(NO_PERFECT_MATCHING);
        } else if (graph.getType().isDirected()) {
            this.graph = new AsUndirectedGraph<>(graph);
        } else {
            this.graph = graph;
        }
        this.options = Objects.requireNonNull(options);
    }

    /**
     * Computes and returns a minimum weight perfect matching in the {@code graph}.
     * TODO: add profound description
     *
     * @return the perfect matching in the {@code graph} of minimum weight
     */
    @Override
    public MatchingAlgorithm.Matching<V, E> getMatching() {
        if (matching == null) {
            solve();
        }
        return matching;
    }

    private void solve() {
        Initializer<V, E> initializer = new Initializer<>(graph);
        this.state = initializer.initialize(options);
        this.primalUpdater = new PrimalUpdater<>(state);
        this.dualUpdater = new DualUpdater<>(state, primalUpdater);
        if (DEBUG)
            printMap();

        Node currentRoot;
        Node nextRoot;
        Node nextNextRoot = null;
        Tree tree;

        while (true) {
            int cycleTreeNum = state.treeNum;
            for (currentRoot = state.nodes[state.nodeNum].treeSiblingNext; currentRoot != null; ) {
                // initializing variables
                nextRoot = currentRoot.treeSiblingNext;
                if (nextRoot != null) {
                    nextNextRoot = nextRoot.treeSiblingNext;
                }
                tree = currentRoot.tree;
                int iterationTreeNum = state.treeNum;

                if (DEBUG)
                    printState();

                // first phase
                state.setCurrentEdges(tree);

                if (options.singleTreeDualUpdatePhase == UPDATE_DUAL_BEFORE) {
                    dualUpdater.updateDualsSingle(tree);
                }

                // second phase
                // applying primal operations to the current tree while it is possible
                while (iterationTreeNum == state.treeNum) {
                    if (DEBUG) {
                        printState();
                        System.out.println("Current tree is " + tree + ", current root is " + currentRoot);
                    }

                    Edge edge;
                    Node node;
                    if (!tree.plusInfinityEdges.isEmpty()) {
                        // can grow tree
                        edge = tree.plusInfinityEdges.min().getData();
                        if (edge.slack <= tree.eps) {
                            primalUpdater.grow(edge, true);
                            continue;
                        }
                    }
                    if (!tree.plusPlusEdges.isEmpty()) {
                        // can shrink blossom
                        edge = tree.plusPlusEdges.min().getData();
                        if (edge.slack <= 2 * tree.eps) {
                            primalUpdater.shrink(edge);
                            continue;
                        }
                    }
                    if (!tree.minusBlossoms.isEmpty()) {
                        // can expand blossom
                        node = tree.minusBlossoms.min().getData();
                        if (node.dual <= tree.eps) {
                            primalUpdater.expand(node);
                            continue;
                        }
                    }
                    // can't do anything
                    if (DEBUG) {
                        System.out.println("Can't do anything");
                    }
                    break;
                }
                if (DEBUG) {
                    printState();
                }

                // third phase
                if (state.treeNum == iterationTreeNum) {
                    tree.currentEdge = null;
                    if (options.singleTreeDualUpdatePhase == SingleTreeDualUpdatePhase.UPDATE_DUAL_AFTER) {
                        if (dualUpdater.updateDualsSingle(tree)) {
                            // since some progress has been made, continue with the same trees
                            continue;
                        }
                    }
                    // clearing current edge pointers
                    state.clearCurrentEdges(tree);
                }
                currentRoot = nextRoot;
                if (nextRoot != null && nextRoot.isInfinityNode()) {
                    currentRoot = nextNextRoot;
                }
            }

            if (DEBUG) {
                printTrees();
                printState();
            }

            if (state.treeNum == 0) {
                // we are done
                break;
            }
            if (cycleTreeNum == state.treeNum) {
                if (dualUpdater.updateDuals(options.dualUpdateStrategy) <= 0) {
                    // don't understand why MULTIPLE_TREE_FIXED_DELTA is used in blossom V code in this case
                    dualUpdater.updateDuals(MULTIPLE_TREE_CONNECTED_COMPONENTS);
                }
            }
        }
        finish();
    }

    /**
     * Returns the computed solution to the dual linear program with respect to the
     * minimum weight perfect matching linear program formulation.
     *
     * @return the solution to the dual linear program formulated on the {@code graph}
     */
    public DualSolution getDualSolution() {
        return dualSolution;
    }

    /**
     * Performs an optimality test after the perfect matching is computed. More precisely,
     * checks whether dual variables of all pseudonodes and resulting slacks of all edges are non-negative and
     * that slacks of all matched edges are exactly 0. Since the algorithm uses floating point arithmetic,
     * this check is done with precision of {@link KolmogorovMinimumWeightPerfectMatching#EPS}
     *
     * @return true iff the assigned dual variable satisfy the dual linear program formulation and
     * complementary slackness conditions are also satisfied. The total error must not exceed EPS
     */
    public boolean testOptimality() {
        if (matching == null) {
            return false;
        }
        return getError() < EPS; // getError() won't return -1 since matching != null
    }

    /**
     * Computes the error in the solution to the dual linear program. More precisely, the total error
     * equals to the sum of:
     * <ul>
     * <li>Absolute value of edge slack if it negative or the edge is matched</li>
     * <li>Absolute value of pseudonode variable if it is negative</li>
     * </ul>
     *
     * @return the total numeric error
     */
    public double getError() {
        if (matching != null) {
            double error = testNonNegativity();
            Set<E> matchedEdges = matching.getEdges();
            for (E e : graph.edgeSet()) {
                if (graph.getEdgeSource(e) != graph.getEdgeTarget(e)) {
                    Edge edge = state.edgeMap.get(e);
                    double slack = graph.getEdgeWeight(e);
                    Node a = edge.headOriginal[0];
                    Node b = edge.headOriginal[1];
                    Pair<Node, Node> lca = lca(a, b);
                    slack -= totalDual(a, lca.getFirst());
                    slack -= totalDual(b, lca.getSecond());
                    if (lca.getFirst() == lca.getSecond()) {
                        // if a and b have a common ancestor, its dual is subtracted from edge's slack
                        slack += 2 * lca.getFirst().getTrueDual();
                    }
                    if (slack < 0 || matchedEdges.contains(e)) {
                        error += Math.abs(slack);
                    }
                }
            }
            return error;
        } else {
            return -1;
        }
    }


    /**
     * Tests whether a non-negative dual variable is assigned to every blossom
     *
     * @return true iff the condition described above holds
     */
    private double testNonNegativity() {
        Node[] nodes = state.nodes;
        Node node;
        boolean nonNegative = true;
        double error = 0;
        for (int i = 0; i < state.nodeNum && nonNegative; i++) {
            node = nodes[i].blossomParent;
            while (node != null && !node.isMarked) {
                if (node.dual < 0) {
                    error += Math.abs(node.dual);
                    break;
                }
                node.isMarked = true;
                node = node.blossomParent;
            }
        }
        clearMarked();
        return error;
    }

    /**
     * Computes the sum of all duals from {@code start} inclusive to {@code end} inclusive
     *
     * @param start the node to start from
     * @param end   the node to end with
     * @return the sum = start.dual + start.blossomParent.dual + ... + end.dual
     */
    private double totalDual(Node start, Node end) {
        if (end == start) {
            return start.getTrueDual();
        } else {
            double result = 0;
            Node current = start;
            do {
                result += current.getTrueDual();
                current = current.blossomParent;
            } while (current != null && current != end);
            result += end.getTrueDual();
            return result;
        }
    }

    /**
     * In the case the vertices {@code a} and {@code b} have a common ancestor blossom $b$ returns $(b, b)$.
     * Otherwise, returns the outermost parent blossoms of nodes {@code a} and {@code b}
     *
     * @param a a vertex to search a lca of
     * @param b a vertex to search a lca of
     * @return either an lca blossom of {@code a} and {@code b} or their outermost blossoms
     */
    private Pair<Node, Node> lca(Node a, Node b) {
        Node[] branches = new Node[]{a, b};
        int dir = 0;
        Node varNode;
        Pair<Node, Node> result;
        while (true) {
            if (branches[dir].isMarked) {
                result = new Pair<>(branches[dir], branches[dir]);
                break;
            }
            branches[dir].isMarked = true;
            if (branches[dir].isOuter) {
                varNode = branches[1 - dir];
                while (!varNode.isOuter && !varNode.isMarked) {
                    varNode = varNode.blossomParent;
                }
                if (varNode.isMarked) {
                    result = new Pair<>(varNode, varNode);
                } else {
                    result = dir == 0 ? new Pair<>(branches[dir], varNode) : new Pair<>(varNode, branches[dir]);
                }
                break;
            }
            branches[dir] = branches[dir].blossomParent;
            dir = 1 - dir;
        }
        clearMarked(a);
        clearMarked(b);
        return result;
    }

    /**
     * Clears the marking of {@code node} and all its ancestors until the first unmarked vertex is encountered
     *
     * @param node the node to start from
     */
    private void clearMarked(Node node) {
        do {
            node.isMarked = false;
            node = node.blossomParent;
        } while (node != null && node.isMarked);
    }

    /**
     * Computes and returns the map from blossoms to original graph's vertices contained in them
     *
     * @return the mapping from every pseudonode to the set of original nodes in the
     * {@code graph} that are contained in it.
     */
    private Map<Node, Set<V>> computeNodesInBlossoms() {
        prepareForDualSolution();
        Map<Node, Set<V>> result = new HashMap<>();
        Node[] nodes = state.nodes;
        Node node;
        for (int i = 0; i < state.nodeNum; i++) {
            node = nodes[i].blossomParent;
            while (node != null && !node.isMarked && !node.isRemoved) {
                getBlossomNodes(node, result);
                node = node.blossomParent;
            }
        }
        clearMarked();
        return result;
    }

    /**
     * Helper method for {@link KolmogorovMinimumWeightPerfectMatching#computeNodesInBlossoms()}, computes
     * the set of original contracted vertices in the {@code pseudonode} and puts computes value into the
     * {@code blossomNodes}. If {@code node} contains other pseudonodes, which haven't been processed already,
     * recursively computes the same set for them.
     *
     * @param pseudonode   the pseudonode whose contracted nodes are computed
     * @param blossomNodes the mapping from pseudonodes to the original nodes contained in them
     */
    private void getBlossomNodes(Node pseudonode, Map<Node, Set<V>> blossomNodes) {
        pseudonode.isMarked = true;
        Set<V> result = new HashSet<>();
        Node endNode = pseudonode.blossomGrandparent;
        Node current = endNode;
        do {
            if (current.isBlossom) {
                if (!blossomNodes.containsKey(current)) {
                    getBlossomNodes(current, blossomNodes);
                }
                result.addAll(blossomNodes.get(current));
            } else {
                result.add(state.backVertexMap.get(current));
            }
            current = current.blossomSibling.getOpposite(current);
        } while (current != endNode);
        blossomNodes.put(pseudonode, result);
    }

    /**
     * Clears the marking of all nodes and pseudonodes
     */
    private void clearMarked() {
        Node[] nodes = state.nodes;
        Node current;
        for (int i = 0; i < state.nodeNum; i++) {
            current = nodes[i];
            do {
                current.isMarked = false;
                current = current.blossomParent;
            } while (current != null && current.isMarked);
        }
    }

    /**
     * This method finishes the algorithm after all nodes are matched. The main problem it solves is that
     * the matching after the end of primal and dual operations can be not valid in the contracted blossoms.
     * <p>
     * Property: if a matching is changed in the parent blossom, the matching in all lower blossoms can become invalid.
     * Therefore, we traverse all nodes, find an unmatched node (it is necessarily contracted), go up to the first
     * blossom, whose matching hasn't been fixed (we set blossomGrandparent references to point to the previous nodes on
     * the path). Then we start to change the matching accordingly all the way down to the initial node.
     * <p>
     * Let's call an edge that is matched to a blossom root a "blossom edge". To make the matching valid we move the
     * blossom edge one layer down at a time so that in the end its endpoints are valid initial nodes of the graph.
     * After this transformation we can't traverse the blossomSibling references no more. That is why we initially compute
     * a mapping of every pseudonode to the set of nodes that are contracted in it. This map is needed to
     * construct a dual solution after the matching in the graph becomes valid.
     */
    private void finish() {
        // compute the mapping from pseudonodes to the set of vertices of initial graph
        Map<Node, Set<V>> nodesInBlossoms = computeNodesInBlossoms();

        Set<E> edges = new HashSet<>();
        double weight = 0;
        Node[] nodes = state.nodes;
        Node blossomRoot;
        Node node;
        Node nextNode;
        Node blossomPrev;
        Node blossom;
        E edge;

        if (DEBUG) {
            System.out.println("Finishing matching");
        }

        for (int i = 0; i < state.nodeNum; i++) {
            if (nodes[i].matched == null) {
                blossomPrev = null;
                blossom = nodes[i];
                // traversing the path from unmatched node to the first unprocessed pseudonode
                do {
                    blossom.isMarked = true;
                    blossom.blossomGrandparent = blossomPrev;
                    blossomPrev = blossom;
                    blossom = blossomPrev.blossomParent;
                } while (!blossom.isOuter && !blossom.blossomParent.isMarked);
                blossom.isMarked = true;
                // now node.blossomGrandparent points to the previous blossom in the hierarchy except for the blossom node
                while (true) {
                    // finding the root of the blossom. This can be a pseudonode
                    for (blossomRoot = blossom.matched.getCurrentOriginal(blossom); blossomRoot.blossomParent != blossom; blossomRoot = blossomRoot.blossomParent) {
                    }
                    blossomRoot.matched = blossom.matched;
                    state.moveEdgeTail(blossom, blossomRoot, blossom.matched);
                    node = blossomRoot.blossomSibling.getOpposite(blossomRoot);
                    // changing the matching in the blossom
                    while (node != blossomRoot) {
                        node.matched = node.blossomSibling;
                        nextNode = node.blossomSibling.getOpposite(node);
                        nextNode.matched = node.matched;
                        node = nextNode.blossomSibling.getOpposite(nextNode);
                    }
                    if (!blossomPrev.isBlossom) {
                        break;
                    }
                    blossom = blossomPrev;
                    blossomPrev = blossom.blossomGrandparent;
                }
            }
        }
        clearMarked();
        // compute the final matching
        for (int i = 0; i < state.nodeNum; i++) {
            edge = state.backEdgeMap.get(nodes[i].matched);
            if (!edges.contains(edge)) {
                edges.add(edge);
                weight += state.graph.getEdgeWeight(edge);
            }
        }
        matching = new MatchingAlgorithm.MatchingImpl<>(state.graph, edges, weight);
        dualSolution = computeDualSolution(nodesInBlossoms);
    }

    /**
     * Setting the blossomGrandparent references so that from a pseudonode we can make
     * one step down to some node that belongs to that pseudonode
     */
    private void prepareForDualSolution() {
        Node[] nodes = state.nodes;
        Node current;
        Node prev;
        for (int i = 0; i < state.nodeNum; i++) {
            current = nodes[i];
            prev = null;
            do {
                current.blossomGrandparent = prev;
                current.isMarked = true;
                prev = current;
                current = current.blossomParent;
            } while (current != null && !current.isMarked);
        }
        clearMarked();
    }

    /**
     * Computes a solution to a dual linear program formulated on the initial graph. This method
     * uses a mapping from pseudonodes to the set of vertices in contains
     *
     * @param nodesInBlossoms a mapping from pseudonodes to the set of vertices it contains
     * @return the solution to the dual linear program
     */
    private DualSolution computeDualSolution(Map<Node, Set<V>> nodesInBlossoms) {
        Map<Set<V>, Double> dualMap = new HashMap<>();
        Node[] nodes = state.nodes;
        Node current;
        for (int i = 0; i < state.nodeNum; i++) {
            current = nodes[i];
            // jump up while the first already processed node in encountered
            do {
                if (current.isBlossom) {
                    if (Math.abs(current.dual) > EPS) {
                        dualMap.put(nodesInBlossoms.get(current), current.getTrueDual());
                    }
                } else {
                    dualMap.put(new HashSet<>(Collections.singletonList(state.backVertexMap.get(current))), current.getTrueDual());
                }
                current.isMarked = true;
                current = current.blossomParent;
            } while (current != null && !current.isMarked);
        }
        clearMarked();
        return new DualSolution(graph, dualMap);
    }

    /**
     * Debug method
     */
    private void printState() {
        Node[] nodes = state.nodes;
        Edge[] edges = state.edges;
        System.out.println();
        for (int i = 0; i < 20; i++) {
            System.out.print("-");
        }
        System.out.println();
        Set<Edge> matched = new HashSet<>();
        for (int i = 0; i < state.nodeNum; i++) {
            Node node = nodes[i];
            if (node.matched != null) {
                Edge matchedEdge = node.matched;
                matched.add(node.matched);
                if (matchedEdge.head[0].matched == null || matchedEdge.head[1].matched == null) {
                    System.out.println("Problem with edge " + matchedEdge);
                    throw new RuntimeException();
                }
            }
            System.out.println(nodes[i]);
        }
        for (int i = 0; i < 20; i++) {
            System.out.print("-");
        }
        System.out.println();
        for (int i = 0; i < state.edgeNum; i++) {
            System.out.println(edges[i] + (matched.contains(edges[i]) ? ", matched" : ""));
        }
    }

    /**
     * Debug method
     */
    private void printTrees() {
        System.out.println("Printing trees");
        for (Node root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            Tree tree = root.tree;
            System.out.println(tree);
        }
    }

    /**
     * Debug method
     */
    private void printMap() {
        System.out.println(state.nodeNum + " " + state.edgeNum);
        for (Map.Entry<V, Node> entry : state.vertexMap.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }

    /**
     * Returns the statistics describing the performance characteristics of the algorithm.
     *
     * @return the statistics describing the algorithms characteristics
     */
    public Statistics getStatistics() {
        return state.statistics;
    }

    /**
     * Enum for choosing a phase for single tree dual updates
     */
    enum SingleTreeDualUpdatePhase {
        /**
         * Update the duals of the tree before processing it
         */
        UPDATE_DUAL_BEFORE,
        /**
         * Update the duals of the tree after processing it with an opportunity to processes it immediately
         * again if some dual progress has been made
         */
        UPDATE_DUAL_AFTER
    }

    /**
     * Options that define the strategies to use during the algorithm for updating duals and initializing the matching
     */
    public static class Options {
        private static final SingleTreeDualUpdatePhase DEFAULT_PHASE = UPDATE_DUAL_BEFORE;
        private static final DualUpdater.DualUpdateStrategy DEFAULT_DUAL_UPDATE_TYPE = MULTIPLE_TREE_CONNECTED_COMPONENTS;
        private static final Initializer.InitializationType DEFAULT_INITIALIZATION_TYPE = GREEDY;

        /**
         * When to update the duals of a single tree: either before or after processing it
         */
        SingleTreeDualUpdatePhase singleTreeDualUpdatePhase;
        /**
         * What greedy strategy to use to perform a global dual update
         */
        DualUpdater.DualUpdateStrategy dualUpdateStrategy;
        /**
         * What strategy to choose to initialize the matching before the main phase of the algorithm
         */
        Initializer.InitializationType initializationType;

        /**
         * Constructs a custom options for the algorithm
         *
         * @param singleTreeDualUpdatePhase phase of a single tree dual updates
         * @param dualUpdateStrategy        greedy strategy to update dual variables globally
         * @param initializationType        strategy for initializing the matching
         */
        public Options(SingleTreeDualUpdatePhase singleTreeDualUpdatePhase, DualUpdater.DualUpdateStrategy dualUpdateStrategy, Initializer.InitializationType initializationType) {
            this.singleTreeDualUpdatePhase = singleTreeDualUpdatePhase;
            this.dualUpdateStrategy = dualUpdateStrategy;
            this.initializationType = initializationType;
        }

        /**
         * Construct a new options instance with a {@code initializationType}
         *
         * @param initializationType defines a strategy to use to initialize the matching
         */
        public Options(Initializer.InitializationType initializationType) {
            this.initializationType = initializationType;
        }

        /**
         * Construct a default options for the algorithm
         */

        public Options() {
            this(DEFAULT_PHASE, DEFAULT_DUAL_UPDATE_TYPE, DEFAULT_INITIALIZATION_TYPE);
        }

    }

    /**
     * Describes the performance characteristics of the algorithm and numeric data about the number
     * of performed dual operations during the main phase of the algorithm
     */
    public static class Statistics {
        /**
         * Number of shrink operations
         */
        int shrinkNum = 0;
        /**
         * Number of expand operations
         */
        int expandNum = 0;
        /**
         * Number of grow operations
         */
        int growNum = 0;

        /**
         * Time spent during the augment operation in nano seconds
         */
        long augmentTime = 0;
        /**
         * Time spent during the expand operation in nano seconds
         */
        long expandTime = 0;
        /**
         * Time spent during the shrink operation in nano seconds
         */
        long shrinkTime = 0;
        /**
         * Time spent during the grow operation in nano seconds
         */
        long growTime = 0;
        /**
         * Time spent during the dual update phase (either single tree or global) in nano seconds
         */
        long dualUpdatesTime = 0;

        /**
         * @return the number of shrink operations
         */
        public int getShrinkNum() {
            return shrinkNum;
        }

        /**
         * @return the number of expand operations
         */
        public int getExpandNum() {
            return expandNum;
        }

        /**
         * @return the number of grow operations
         */
        public int getGrowNum() {
            return growNum;
        }

        /**
         * @return the time spent during the augment operation in nano seconds
         */
        public long getAugmentTime() {
            return augmentTime;
        }

        /**
         * @return the time spent during the expand operation in nano seconds
         */
        public long getExpandTime() {
            return expandTime;
        }

        /**
         * @return the time spent during the shrink operation in nano seconds
         */
        public long getShrinkTime() {
            return shrinkTime;
        }

        /**
         * @return the time spent during the grow operation in nano seconds
         */
        public long getGrowTime() {
            return growTime;
        }

        /**
         * @return the time spent during the dual update phase (either single tree or global) in nano seconds
         */
        public long getDualUpdatesTime() {
            return dualUpdatesTime;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Statistics{");
            sb.append("shrinkNum=").append(shrinkNum);
            sb.append(", expandNum=").append(expandNum);
            sb.append(", growNum=").append(growNum);
            sb.append(", augmentTime=").append(augmentTime);
            sb.append(", expandTime=").append(expandTime);
            sb.append(", shrinkTime=").append(shrinkTime);
            sb.append(", growTime=").append(growTime);
            sb.append('}');
            return sb.toString();
        }
    }

    /**
     * A solution to the dual linear program formulated on the  {@code graph}
     */
    public class DualSolution {
        /**
         * The graph on which both primal and dual linear programs are formulated
         */
        Graph<V, E> graph;

        /**
         * Mapping from sets of vertices of odd cardinality to their dual variables. Represents a solution
         * to the dual linear program
         */
        Map<Set<V>, Double> dualVariables;

        /**
         * Constructs a new solution for the dual linear program
         *
         * @param graph         the graph on which linear program is formulated
         * @param dualVariables the mapping from sets of vertices of odd cardinality to their dual variables
         */
        public DualSolution(Graph<V, E> graph, Map<Set<V>, Double> dualVariables) {
            this.graph = graph;
            this.dualVariables = dualVariables;
        }

        /**
         * @return the graph on which the linear program is formulated
         */
        public Graph<V, E> getGraph() {
            return graph;
        }

        /**
         * The mapping from sets of vertices of odd cardinality to their dual variables, which
         * represents a solution to the dual linear program
         *
         * @return the mapping from sets of vertices of odd cardinality to their dual variables
         */
        public Map<Set<V>, Double> getDualVariables() {
            return dualVariables;
        }
    }
}
