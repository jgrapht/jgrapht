/*
 * (C) Copyright 2018-2018, by Timofey Chudakov and Contributors.
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
package org.jgrapht.alg.matching.blossom.v5;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.matching.*;
import org.jgrapht.alg.util.*;

import java.util.*;

import static org.jgrapht.alg.matching.blossom.v5.BlossomVOptions.DualUpdateStrategy.MULTIPLE_TREE_CONNECTED_COMPONENTS;

/**
 * This class computes a minimum weight perfect matching in general graphs using the Blossom V
 * algorithm.
 * <p>
 * Let $G = (V, E, c)$ be an undirected graph with a real-valued cost function defined on it. A
 * matching is an edge-disjoint subset of edges $M \subseteq E$. A matching is perfect if $2|M| =
 * |V|$. In the minimum weight perfect matching problem the goal is to minimize the weighted sum of
 * the edges in the perfect matching. This class supports pseudographs, but a problem on a
 * pseudograph can be easily reduced to a problem on a simple graph. Moreover, this reduction can
 * heavily influence the running time since only an edge with minimum weight between two vertices
 * can belong to the matching. Currently, users are responsible for doing this reduction themselves
 * before invoking the algorithm.
 * <p>
 * Note that if the graph is unweighted and dense, {@link EdmondsMaximumCardinalityMatching} may be
 * a better choice.
 * <p>
 * For more information about the algorithm see the following paper: <i>Kolmogorov, V. Math. Prog.
 * Comp. (2009) 1: 43. https://doi.org/10.1007/s12532-009-0002-8</i>, and the original
 * implementation: <i>http://pub.ist.ac.at/~vnk/software/blossom5-v2.05.src.tar.gz</i>
 * <p>
 * The algorithm can be divided into two phases: initialization and the main algorithm. The
 * initialization phase is responsible for converting the specified graph into the form convenient
 * for the algorithm and for finding an initial matching to speed up the main part. Furthermore, the
 * main part of the algorithm can be further divided into primal and dual updates. The primal phases
 * are aimed at augmenting the matching so that the value of the objective function of the primal
 * linear program increases. Dual updates are aimed at increasing the objective function of the dual
 * linear program. The algorithm iteratively performs these primal and dual operations to build
 * alternating trees of tight edges and augment the matching. Thus, at any stage of the algorithm
 * the matching consists of tight edges. This means that the resulting perfect matching meets
 * complementary slackness conditions, and is therefore optimal.
 * <p>
 * At construction time the set of options can be specified to define the strategies used by the
 * algorithm to perform initialization, dual updates, etc. This can be done with the
 * {@link BlossomVOptions}. This class supports retrieving statistics for the algorithm performance;
 * see {@link KolmogorovMinimumWeightPerfectMatching#getStatistics()}. It provides the time elapsed
 * during primal operations and dual updates, as well as the number of these primal operations
 * performed.
 * <p>
 * The solution to a minimum weight perfect matching problem instance comes with a certificate of
 * optimality, which is represented by a solution to a dual linear program; see
 * {@link DualSolution}. This class encapsulates a mapping from the node sets of odd cardinality to
 * the corresponding dual variables. This mapping doesn't contain the sets whose dual variables are
 * $0$. The computation of the dual solution is performed lazily and doesn't affect the running time
 * of finding a minimum weight perfect matching.
 * <p>
 * This class supports testing the optimality of the solution via
 * {@link KolmogorovMinimumWeightPerfectMatching#testOptimality()}. It also supports retrieval of
 * the computation error when the edge weights are real values via
 * {@link KolmogorovMinimumWeightPerfectMatching#getError()}. Both optimality test and error
 * computation are performed lazily and don't affect the running time of the main algorithm. If the
 * problem instance doesn't contain a perfect matching at all, the algorithm doesn't find a minimum
 * weight maximum matching; instead, it throws an exception.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 * @see BlossomVPrimalUpdater
 * @see BlossomVDualUpdater
 */
public class KolmogorovMinimumWeightPerfectMatching<V, E>
    implements
    MatchingAlgorithm<V, E>
{
    /**
     * Default epsilon used in the algorithm
     */
    public static final double EPS = MatchingAlgorithm.DEFAULT_EPSILON;
    /**
     * Default infinity value used in the algorithm
     */
    public static final double INFINITY = 1e100;
    /**
     * Defines the threshold for throwing an exception about no perfect matching existence
     */
    public static final double NO_PERFECT_MATCHING_THRESHOLD = 1e10;
    /**
     * When set to true, verbose debugging output will be produced
     */
    static final boolean DEBUG = false;
    /**
     * Exception message if no perfect matching is possible
     */
    static final String NO_PERFECT_MATCHING = "There is no perfect matching in the specified graph";
    /**
     * Default options
     */
    private static final BlossomVOptions DEFAULT_OPTIONS = new BlossomVOptions();
    /**
     * The graph we are matching on
     */
    private final Graph<V, E> graph;
    /**
     * Current state of the algorithm
     */
    BlossomVState<V, E> state;
    /**
     * Performs primal operations (grow, augment, shrink and expand)
     */
    private BlossomVPrimalUpdater<V, E> primalUpdater;
    /**
     * Performs dual updates using the strategy defined by the {@code options}
     */
    private BlossomVDualUpdater<V, E> dualUpdater;
    /**
     * The computed matching of the {@code graph}
     */
    private MatchingAlgorithm.Matching<V, E> matching;
    /**
     * Defines solution to the dual linear program formulated on the {@code graph}
     */
    private DualSolution<V, E> dualSolution;
    /**
     * BlossomVOptions used by the algorithm to match the problem instance
     */
    private BlossomVOptions options;

    /**
     * Constructs a new instance of the algorithm using the default options.
     *
     * @param graph the graph for which to find a minimum weight perfect matching
     */
    public KolmogorovMinimumWeightPerfectMatching(Graph<V, E> graph)
    {
        this(graph, DEFAULT_OPTIONS);
    }

    /**
     * Constructs a new instance of the algorithm with the specified {@code options}
     *
     * @param graph the graph for which to find a minimum weight perfect matching
     * @param options the options which define the strategies for the initialization and dual
     *        updates
     */
    public KolmogorovMinimumWeightPerfectMatching(Graph<V, E> graph, BlossomVOptions options)
    {
        Objects.requireNonNull(graph);
        if ((graph.vertexSet().size() & 1) == 1) {
            throw new IllegalArgumentException(NO_PERFECT_MATCHING);
        } else {
            this.graph = graph;
        }
        this.options = Objects.requireNonNull(options);
    }

    /**
     * Computes and returns a minimum weight perfect matching in the {@code graph}. See the class
     * description for the relative definitions and algorithm description.
     *
     * @return the minimum weight perfect matching for the {@code graph}
     */
    @Override
    public MatchingAlgorithm.Matching<V, E> getMatching()
    {
        if (matching == null) {
            lazyComputeMinimumWeightPerfectMatching();
        }
        return matching;
    }

    /**
     * Returns the computed solution to the dual linear program with respect to the minimum weight
     * perfect matching linear program formulation.
     *
     * @return the solution to the dual linear program formulated on the {@code graph}
     */
    public DualSolution<V, E> getDualSolution()
    {
        dualSolution = lazyComputeDualSolution();
        return dualSolution;
    }

    /**
     * Performs an optimality test after the perfect matching is computed.
     * <p>
     * More precisely, checks whether dual variables of all pseudonodes and resulting slacks of all
     * edges are non-negative and that slacks of all matched edges are exactly 0. Since the
     * algorithm uses floating point arithmetic, this check is done with precision of
     * {@link KolmogorovMinimumWeightPerfectMatching#EPS}
     * <p>
     * In general, this method should always return true unless the algorithm implementation has a
     * bug.
     *
     * @return true iff the assigned dual variables satisfy the dual linear program formulation AND
     *         complementary slackness conditions are also satisfied. The total error must not
     *         exceed EPS
     */
    public boolean testOptimality()
    {
        lazyComputeMinimumWeightPerfectMatching();
        return getError() < EPS; // getError() won't return -1 since matching != null
    }

    /**
     * Computes the error in the solution to the dual linear program. More precisely, the total
     * error equals the sum of:
     * <ul>
     * <li>Absolute value of edge slack if negative or the edge is matched</li>
     * <li>Absolute value of pseudonode variable if negative</li>
     * </ul>
     *
     * @return the total numeric error
     */
    public double getError()
    {
        lazyComputeMinimumWeightPerfectMatching();
        double error = testNonNegativity();
        Set<E> matchedEdges = matching.getEdges();

        for (int i = 0; i < state.graphEdges.size(); i++) {
            E graphEdge = state.graphEdges.get(i);
            BlossomVEdge edge = state.edges[i];
            double slack = graph.getEdgeWeight(graphEdge);
            BlossomVNode a = edge.headOriginal[0];
            BlossomVNode b = edge.headOriginal[1];

            Pair<BlossomVNode, BlossomVNode> lca = lca(a, b);
            slack -= totalDual(a, lca.getFirst());
            slack -= totalDual(b, lca.getSecond());

            if (lca.getFirst() == lca.getSecond()) {
                // if a and b have a common ancestor, its dual is subtracted from edge's slack
                slack += 2 * lca.getFirst().getTrueDual();
            }
            if (slack < 0 || matchedEdges.contains(graphEdge)) {
                error += Math.abs(slack);
            }
        }
        return error;
    }

    /**
     * Lazily runs the algorithm on the specified graph.
     */
    private void lazyComputeMinimumWeightPerfectMatching()
    {
        if (matching != null) {
            return;
        }
        BlossomVInitializer<V, E> initializer = new BlossomVInitializer<>(graph);
        this.state = initializer.initialize(options);
        this.primalUpdater = new BlossomVPrimalUpdater<>(state);
        this.dualUpdater = new BlossomVDualUpdater<>(state, primalUpdater);
        if (DEBUG) {
            printMap();
        }

        while (true) {
            int cycleTreeNum = state.treeNum;

            for (BlossomVNode currentRoot = state.nodes[state.nodeNum].treeSiblingNext;
                currentRoot != null;)
            {
                // initialize variables
                BlossomVNode nextRoot = currentRoot.treeSiblingNext;
                BlossomVNode nextNextRoot = null;
                if (nextRoot != null) {
                    nextNextRoot = nextRoot.treeSiblingNext;
                }
                BlossomVTree tree = currentRoot.tree;
                int iterationTreeNum = state.treeNum;

                if (DEBUG) {
                    printState();
                }

                // first phase
                setCurrentEdgesAndTryToAugment(tree);

                if (iterationTreeNum == state.treeNum && options.updateDualsBefore) {
                    dualUpdater.updateDualsSingle(tree);
                }

                // second phase
                // apply primal operations to the current tree while it is possible
                while (iterationTreeNum == state.treeNum) {
                    if (DEBUG) {
                        printState();
                        System.out.println(
                            "Current tree is " + tree + ", current root is " + currentRoot);
                    }

                    if (!tree.plusInfinityEdges.isEmpty()) {
                        // can grow tree
                        BlossomVEdge edge = tree.plusInfinityEdges.findMin().getValue();
                        if (edge.slack <= tree.eps) {
                            primalUpdater.grow(edge, true, true);
                            continue;
                        }
                    }
                    if (!tree.plusPlusEdges.isEmpty()) {
                        // can shrink blossom
                        BlossomVEdge edge = tree.plusPlusEdges.findMin().getValue();
                        if (edge.slack <= 2 * tree.eps) {
                            primalUpdater.shrink(edge, true);
                            continue;
                        }
                    }
                    if (!tree.minusBlossoms.isEmpty()) {
                        // can expand blossom
                        BlossomVNode node = tree.minusBlossoms.findMin().getValue();
                        if (node.dual <= tree.eps) {
                            primalUpdater.expand(node, true);
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
                    if (options.updateDualsAfter && dualUpdater.updateDualsSingle(tree)) {
                        // since some progress has been made, continue with the same trees
                        continue;
                    }
                    // clear current edge pointers
                    tree.clearCurrentEdges();
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
            if (cycleTreeNum == state.treeNum
                && dualUpdater.updateDuals(options.dualUpdateStrategy) <= 0)
            {
                dualUpdater.updateDuals(MULTIPLE_TREE_CONNECTED_COMPONENTS);
            }
        }
        finish();
    }

    /**
     * Sets the currentEdge and currentDirection variables for all trees adjacent to the
     * {@code tree}
     *
     * @param tree the tree whose adjacent trees' variables are modified
     */
    private void setCurrentEdgesAndTryToAugment(BlossomVTree tree)
    {
        for (BlossomVTree.TreeEdgeIterator iterator = tree.treeEdgeIterator();
            iterator.hasNext();)
        {
            BlossomVTreeEdge treeEdge = iterator.next();
            BlossomVTree opposite = treeEdge.head[iterator.getCurrentDirection()];

            if (!treeEdge.plusPlusEdges.isEmpty()) {
                BlossomVEdge edge = treeEdge.plusPlusEdges.findMin().getValue();
                if (edge.slack <= tree.eps + opposite.eps) {
                    if (DEBUG) {
                        System.out.println("Bingo traverse");
                    }
                    primalUpdater.augment(edge);
                    break;
                }
            }

            opposite.currentEdge = treeEdge;
            opposite.currentDirection = iterator.getCurrentDirection();
        }
    }

    /**
     * Tests whether a non-negative dual variable is assigned to every blossom
     *
     * @return true iff the condition described above holds
     */
    private double testNonNegativity()
    {
        BlossomVNode[] nodes = state.nodes;
        double error = 0;
        for (int i = 0; i < state.nodeNum; i++) {
            BlossomVNode node = nodes[i].blossomParent;
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
     * @param end the node to end with
     * @return the sum = start.dual + start.blossomParent.dual + ... + end.dual
     */
    private double totalDual(BlossomVNode start, BlossomVNode end)
    {
        if (end == start) {
            return start.getTrueDual();
        } else {
            double result = 0;
            BlossomVNode current = start;
            do {
                result += current.getTrueDual();
                current = current.blossomParent;
            } while (current != null && current != end);
            result += end.getTrueDual();
            return result;
        }
    }

    /**
     * Returns $(b, b)$ in the case where the vertices {@code a} and {@code b} have a common
     * ancestor blossom $b$. Otherwise, returns the outermost parent blossoms of nodes {@code a} and
     * {@code b}
     *
     * @param a a vertex whose lca is to be found with respect to another vertex
     * @param b the other vertex whose lca is to be found
     * @return either an lca blossom of {@code a} and {@code b} or their outermost blossoms
     */
    private Pair<BlossomVNode, BlossomVNode> lca(BlossomVNode a, BlossomVNode b)
    {
        BlossomVNode[] branches = new BlossomVNode[] { a, b };
        int dir = 0;
        Pair<BlossomVNode, BlossomVNode> result;
        while (true) {
            if (branches[dir].isMarked) {
                result = new Pair<>(branches[dir], branches[dir]);
                break;
            }
            branches[dir].isMarked = true;
            if (branches[dir].isOuter) {
                BlossomVNode jumpNode = branches[1 - dir];
                while (!jumpNode.isOuter && !jumpNode.isMarked) {
                    jumpNode = jumpNode.blossomParent;
                }
                if (jumpNode.isMarked) {
                    result = new Pair<>(jumpNode, jumpNode);
                } else {
                    result = dir == 0 ? new Pair<>(branches[dir], jumpNode)
                        : new Pair<>(jumpNode, branches[dir]);
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
     * Clears the marking of {@code node} and all its ancestors up until the first unmarked vertex
     * is encountered
     *
     * @param node the node to start from
     */
    private void clearMarked(BlossomVNode node)
    {
        do {
            node.isMarked = false;
            node = node.blossomParent;
        } while (node != null && node.isMarked);
    }

    /**
     * Clears the marking of all nodes and pseudonodes
     */
    private void clearMarked()
    {
        BlossomVNode[] nodes = state.nodes;
        for (int i = 0; i < state.nodeNum; i++) {
            BlossomVNode current = nodes[i];
            do {
                current.isMarked = false;
                current = current.blossomParent;
            } while (current != null && current.isMarked);
        }
    }

    /**
     * Finishes the algorithm after all nodes are matched. The main problem it solves is that the
     * matching after the end of primal and dual operations may not be valid in the contracted
     * blossoms.
     * <p>
     * Property: if a matching is changed in the parent blossom, the matching in all lower blossoms
     * can become invalid. Therefore, we traverse all nodes, find an unmatched node (it is
     * necessarily contracted), go up to the first blossom whose matching hasn't been fixed (we set
     * blossomGrandparent references to point to the previous nodes on the path). Then we start to
     * change the matching accordingly all the way down to the initial node.
     * <p>
     * Let's call an edge that is matched to a blossom root a "blossom edge". To make the matching
     * valid we move the blossom edge one layer down at a time so that in the end its endpoints are
     * valid initial nodes of the graph. After this transformation we can't traverse the
     * blossomSibling references any more. That is why we initially compute a mapping of every
     * pseudonode to the set of nodes that are contracted in it. This map is needed to construct a
     * dual solution after the matching in the graph becomes valid.
     */
    private void finish()
    {
        if (DEBUG) {
            System.out.println("Finishing matching");
        }

        Set<E> edges = new HashSet<>();
        double weight = 0;
        BlossomVNode[] nodes = state.nodes;
        List<BlossomVNode> processed = new LinkedList<>();

        for (int i = 0; i < state.nodeNum; i++) {
            if (nodes[i].matched == null) {
                BlossomVNode blossomPrev = null;
                BlossomVNode blossom = nodes[i];
                // traverse the path from unmatched node to the first unprocessed pseudonode
                do {
                    blossom.blossomGrandparent = blossomPrev;
                    blossomPrev = blossom;
                    blossom = blossomPrev.blossomParent;
                } while (!blossom.isOuter);
                // now node.blossomGrandparent points to the previous blossom in the hierarchy (not
                // counting the blossom node)
                while (true) {
                    // find the root of the blossom. This can be a pseudonode
                    BlossomVNode blossomRoot = blossom.matched.getCurrentOriginal(blossom);
                    if (blossomRoot == null) {
                        blossomRoot = blossom.matched.head[0].isProcessed
                            ? blossom.matched.headOriginal[1] : blossom.matched.headOriginal[0];
                    }
                    while (blossomRoot.blossomParent != blossom) {
                        blossomRoot = blossomRoot.blossomParent;
                    }
                    blossomRoot.matched = blossom.matched;
                    BlossomVNode node = blossom.getOppositeMatched();
                    if (node != null) {
                        node.isProcessed = true;
                        processed.add(node);
                    }
                    node = blossomRoot.blossomSibling.getOpposite(blossomRoot);
                    // chang the matching in the blossom
                    while (node != blossomRoot) {
                        node.matched = node.blossomSibling;
                        BlossomVNode nextNode = node.blossomSibling.getOpposite(node);
                        nextNode.matched = node.matched;
                        node = nextNode.blossomSibling.getOpposite(nextNode);
                    }
                    if (!blossomPrev.isBlossom) {
                        break;
                    }
                    blossom = blossomPrev;
                    blossomPrev = blossom.blossomGrandparent;
                }
                for (BlossomVNode processedNode : processed) {
                    processedNode.isProcessed = false;
                }
                processed.clear();
            }
        }
        // compute the final matching
        for (int i = 0; i < state.nodeNum; i++) {
            E graphEdge = state.graphEdges.get(nodes[i].matched.pos);
            if (!edges.contains(graphEdge)) {
                edges.add(graphEdge);
                weight += state.graph.getEdgeWeight(graphEdge);
            }
        }
        matching = new MatchingAlgorithm.MatchingImpl<>(state.graph, edges, weight);
    }

    /**
     * Sets the blossomGrandparent references so that from a pseudonode we can make one step down to
     * some node that belongs to that pseudonode
     */
    private void prepareForDualSolution()
    {
        BlossomVNode[] nodes = state.nodes;
        for (int i = 0; i < state.nodeNum; i++) {
            BlossomVNode current = nodes[i];
            BlossomVNode prev = null;
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
     * Computes the set of original contracted vertices in the {@code pseudonode} and puts computes
     * value into the {@code blossomNodes}. If {@code node} contains other pseudonodes which haven't
     * been processed already, recursively computes the same set for them.
     *
     * @param pseudonode the pseudonode whose contracted nodes are computed
     * @param blossomNodes the mapping from pseudonodes to the original nodes contained in them
     */
    private Set<V> getBlossomNodes(BlossomVNode pseudonode, Map<BlossomVNode, Set<V>> blossomNodes)
    {
        if (blossomNodes.containsKey(pseudonode)) {
            return blossomNodes.get(pseudonode);
        }
        Set<V> result = new HashSet<>();
        BlossomVNode endNode = pseudonode.blossomGrandparent;
        BlossomVNode current = endNode;
        do {
            if (current.isBlossom) {
                if (!blossomNodes.containsKey(current)) {
                    result.addAll(getBlossomNodes(current, blossomNodes));
                } else {
                    result.addAll(blossomNodes.get(current));
                }
            } else {
                result.add(state.graphVertices.get(current.pos));
            }
            current = current.blossomSibling.getOpposite(current);
        } while (current != endNode);
        blossomNodes.put(pseudonode, result);
        return result;
    }

    /**
     * Computes a solution to a dual linear program formulated on the initial graph.
     *
     * @return the solution to the dual linear program
     */
    private DualSolution<V, E> lazyComputeDualSolution()
    {
        lazyComputeMinimumWeightPerfectMatching();
        if (dualSolution != null) {
            return dualSolution;
        }
        Map<Set<V>, Double> dualMap = new HashMap<>();
        Map<BlossomVNode, Set<V>> nodesInBlossoms = new HashMap<>();
        BlossomVNode[] nodes = state.nodes;
        prepareForDualSolution();
        for (int i = 0; i < state.nodeNum; i++) {
            BlossomVNode current = nodes[i];
            // jump up while the first already processed node is encountered
            do {
                if (Math.abs(current.getTrueDual()) > EPS) {
                    if (current.isBlossom) {
                        dualMap
                            .put(getBlossomNodes(current, nodesInBlossoms), current.getTrueDual());
                    } else {
                        dualMap.put(
                            Collections.singleton(state.graphVertices.get(current.pos)),
                            current.getTrueDual());
                    }
                }
                current.isMarked = true;
                if (current.isOuter) {
                    break;
                }
                current = current.blossomParent;
            } while (current != null && !current.isMarked);
        }
        clearMarked();
        return new DualSolution<>(graph, dualMap);
    }

    /**
     * Prints the state of the algorithm. This is a debug method.
     */
    private void printState()
    {
        BlossomVNode[] nodes = state.nodes;
        BlossomVEdge[] edges = state.edges;
        System.out.println();
        for (int i = 0; i < 20; i++) {
            System.out.print("-");
        }
        System.out.println();
        Set<BlossomVEdge> matched = new HashSet<>();
        for (int i = 0; i < state.nodeNum; i++) {
            BlossomVNode node = nodes[i];
            if (node.matched != null) {
                BlossomVEdge matchedEdge = node.matched;
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
    private void printTrees()
    {
        System.out.println("Printing trees");
        for (BlossomVNode root = state.nodes[state.nodeNum].treeSiblingNext; root != null;
            root = root.treeSiblingNext)
        {
            BlossomVTree tree = root.tree;
            System.out.println(tree);
        }
    }

    /**
     * Debug method
     */
    private void printMap()
    {
        System.out.println(state.nodeNum + " " + state.edgeNum);
        for (int i = 0; i < state.nodeNum; i++) {
            System.out.println(state.graphVertices.get(i) + " -> " + state.nodes[i]);
        }
    }

    /**
     * Returns the statistics describing the performance characteristics of the algorithm.
     *
     * @return the statistics describing the algorithms characteristics
     */
    public Statistics getStatistics()
    {
        return state.statistics;
    }

    /**
     * Describes the performance characteristics of the algorithm and numeric data about the number
     * of performed dual operations during the main phase of the algorithm
     */
    public static class Statistics
    {
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
         * Time spent during the augment operation in nanoseconds
         */
        long augmentTime = 0;
        /**
         * Time spent during the expand operation in nanoseconds
         */
        long expandTime = 0;
        /**
         * Time spent during the shrink operation in nanoseconds
         */
        long shrinkTime = 0;
        /**
         * Time spent during the grow operation in nanoseconds
         */
        long growTime = 0;
        /**
         * Time spent during the dual update phase (either single tree or global) in nanoseconds
         */
        long dualUpdatesTime = 0;

        /**
         * @return the number of shrink operations
         */
        public int getShrinkNum()
        {
            return shrinkNum;
        }

        /**
         * @return the number of expand operations
         */
        public int getExpandNum()
        {
            return expandNum;
        }

        /**
         * @return the number of grow operations
         */
        public int getGrowNum()
        {
            return growNum;
        }

        /**
         * @return the time spent during the augment operation in nanoseconds
         */
        public long getAugmentTime()
        {
            return augmentTime;
        }

        /**
         * @return the time spent during the expand operation in nanoseconds
         */
        public long getExpandTime()
        {
            return expandTime;
        }

        /**
         * @return the time spent during the shrink operation in nanoseconds
         */
        public long getShrinkTime()
        {
            return shrinkTime;
        }

        /**
         * @return the time spent during the grow operation in nanoseconds
         */
        public long getGrowTime()
        {
            return growTime;
        }

        /**
         * @return the time spent during the dual update phase (either single tree or global) in
         *         nanoseconds
         */
        public long getDualUpdatesTime()
        {
            return dualUpdatesTime;
        }

        @Override
        public String toString()
        {
            return "Statistics{shrinkNum=" + shrinkNum + ", expandNum=" + expandNum + ", growNum="
                + growNum + ", augmentTime=" + augmentTime + ", expandTime=" + expandTime
                + ", shrinkTime=" + shrinkTime + ", growTime=" + growTime + '}';
        }
    }

    /**
     * A solution to the dual linear program formulated on the {@code graph}
     *
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    public static class DualSolution<V, E>
    {
        /**
         * The graph on which both primal and dual linear programs are formulated
         */
        Graph<V, E> graph;

        /**
         * Mapping from sets of vertices of odd cardinality to their dual variables. Represents a
         * solution to the dual linear program
         */
        Map<Set<V>, Double> dualVariables;

        /**
         * Constructs a new solution for the dual linear program
         *
         * @param graph the graph on which the linear program is formulated
         * @param dualVariables the mapping from sets of vertices of odd cardinality to their dual
         *        variables
         */
        public DualSolution(Graph<V, E> graph, Map<Set<V>, Double> dualVariables)
        {
            this.graph = graph;
            this.dualVariables = dualVariables;
        }

        /**
         * @return the graph on which the linear program is formulated
         */
        public Graph<V, E> getGraph()
        {
            return graph;
        }

        /**
         * The mapping from sets of vertices of odd cardinality to their dual variables, which
         * represents a solution to the dual linear program
         *
         * @return the mapping from sets of vertices of odd cardinality to their dual variables
         */
        public Map<Set<V>, Double> getDualVariables()
        {
            return dualVariables;
        }
    }
}
