package org.jgrapht.alg.blossom;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.AsUndirectedGraph;

import java.util.*;

import static org.jgrapht.alg.blossom.DualUpdater.DualUpdateType.MULTIPLE_TREE_CONNECTED_COMPONENTS;
import static org.jgrapht.alg.blossom.Initializer.InitializationType.GREEDY;
import static org.jgrapht.alg.blossom.KolmogorovMinimumWeightPerfectMatching.SingleTreeDualUpdatePhase.UPDATE_DUAL_BEFORE;

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
    public static final int INFINITY = Integer.MAX_VALUE;
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
        if (graph.getType().isDirected()) {
            this.graph = new AsUndirectedGraph<>(graph);
        } else {
            this.graph = graph;
        }
        this.options = Objects.requireNonNull(options);
    }

    /**
     * Computes and returns a minimum weight perfect matching in the {@code graph}.
     *
     * @return the perfect matching in the {@code graph} of minimum weight
     */
    @Override
    public MatchingAlgorithm.Matching<V, E> getMatching() {
        Initializer<V, E> initializer = new Initializer<>(graph);
        this.state = initializer.initialize(options);
        this.primalUpdater = new PrimalUpdater<>(state);
        this.dualUpdater = new DualUpdater<>(state, primalUpdater);
        if (options.verbose)
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

                if (options.verbose)
                    printState();

                // first phase
                state.setCurrentEdges(tree);

                if (options.singleTreeDualUpdatePhase == UPDATE_DUAL_BEFORE) {
                    dualUpdater.updateDualsSingle(tree);
                }

                // second phase
                // applying primal operations to the current tree while it is possible
                while (iterationTreeNum == state.treeNum) {
                    if (options.verbose) {
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
                    if (options.verbose) {
                        System.out.println("Can't do anything");
                    }
                    break;
                }
                if (options.verbose) {
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

            if (options.verbose) {
                printTrees();
                printState();
            }

            if (state.treeNum == 0) {
                // we are done
                break;
            }
            if (cycleTreeNum == state.treeNum) {
                if (dualUpdater.updateDuals(options.dualUpdateType) <= 0) {
                    // don't understand why MULTIPLE_TREE_FIXED_DELTA is used in blossom V code in this case
                    dualUpdater.updateDuals(MULTIPLE_TREE_CONNECTED_COMPONENTS);
                }
            }
        }
        finish();
        return matching;
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
     * checks whether dual variables of all pseudonodes are non-negative and whether the resulting
     * slacks of all edges are non-negative
     *
     * @return true iff the assigned dual variable satisfy the dual linear program formulation and
     * complementary slackness conditions are satisfied
     */
    public boolean testOptimality() {
        if (matching != null) {
            if (!testNonNegativity()) {
                return false;
            }
            double error = 0;
            Set<E> matched = matching.getEdges();
            for (E e : graph.edgeSet()) {
                Edge edge = state.edgeMap.get(e);
                double slack = graph.getEdgeWeight(e);
                Node a = edge.headOriginal[0];
                Node b = edge.headOriginal[1];
                Pair<Node, Node> lca = lca(a, b);
                slack -= totalDual(a, lca.getFirst());
                slack -= totalDual(b, lca.getSecond());
                if (lca.getFirst() == lca.getSecond()) {
                    slack += 2 * lca.getFirst().getTrueDual();
                }
                if (slack + EPS < 0) {
                    return false;
                }
                if (matched.contains(e)) {
                    error += Math.abs(slack);
                }
            }
            return error < EPS;
        } else {
            return false;
        }
    }

    /**
     * Tests whether a non-negative dual variable is assigned to every blossom
     *
     * @return true iff the condition described above holds
     */
    private boolean testNonNegativity() {
        Node[] nodes = state.nodes;
        Node node;
        boolean nonNegative = true;
        for (int i = 0; i < state.nodeNum && nonNegative; i++) {
            node = nodes[i].blossomParent;
            while (node != null && !node.isMarked) {
                if (node.dual + EPS < 0) {
                    nonNegative = false;
                    break;
                }
                node.isMarked = true;
                node = node.blossomParent;
            }
        }
        clearMarked();
        return nonNegative;
    }

    /**
     * The sum of all duals from {@code start} inclusive to {@code end} inclusive
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

    private void finish() {
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

        if (state.options.verbose) {
            System.out.println("Finishing matching");
        }

        for (int i = 0; i < state.nodeNum; i++) {
            if (nodes[i].matched == null) {
                // changing the matching in the blossoms
                blossomPrev = null;
                blossom = nodes[i];
                do {
                    blossom.isMarked = true;
                    blossom.blossomGrandparent = blossomPrev;
                    blossomPrev = blossom;
                    blossom = blossomPrev.blossomParent;
                } while (!blossom.isOuter && !blossom.blossomParent.isMarked);
                blossom.isMarked = true;
                // now node.blossomGrandparent points to the previous blossom in the hierarchy except for the blossom node
                while (true) {
                    for (blossomRoot = blossom.matched.getCurrentOriginal(blossom); blossomRoot.blossomParent != blossom; blossomRoot = blossomRoot.blossomParent) {
                    }
                    blossomRoot.matched = blossom.matched;
                    state.moveEdge(blossom, blossomRoot, blossom.matched);
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

    private DualSolution computeDualSolution(Map<Node, Set<V>> nodesInBlossoms) {
        Map<Set<V>, Double> dualMap = new HashMap<>();
        Node[] nodes = state.nodes;
        Node current;
        for (int i = 0; i < state.nodeNum; i++) {
            current = nodes[i];
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

    private void printTrees() {
        System.out.println("Printing trees");
        for (Node root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            Tree tree = root.tree;
            System.out.println(tree);
        }
    }

    private void printMap() {
        System.out.println(state.nodeNum + " " + state.edgeNum);
        for (Map.Entry<V, Node> entry : state.vertexMap.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }

    public Statistics getStatistics() {
        return state.statistics;
    }

    enum SingleTreeDualUpdatePhase {
        UPDATE_DUAL_BEFORE,
        UPDATE_DUAL_AFTER
    }

    public static class Options {
        private static final SingleTreeDualUpdatePhase DEFAULT_PHASE = UPDATE_DUAL_BEFORE;
        private static final DualUpdater.DualUpdateType DEFAULT_DUAL_UPDATE_TYPE = MULTIPLE_TREE_CONNECTED_COMPONENTS;
        private static final Initializer.InitializationType DEFAULT_INITIALIZATION_TYPE = GREEDY;
        private static final boolean DEFAULT_VERBOSE = true;

        SingleTreeDualUpdatePhase singleTreeDualUpdatePhase;
        DualUpdater.DualUpdateType dualUpdateType;
        Initializer.InitializationType initializationType;
        boolean verbose;

        public Options(SingleTreeDualUpdatePhase singleTreeDualUpdatePhase, DualUpdater.DualUpdateType dualUpdateType, Initializer.InitializationType initializationType, boolean verbose) {
            this.singleTreeDualUpdatePhase = singleTreeDualUpdatePhase;
            this.dualUpdateType = dualUpdateType;
            this.initializationType = initializationType;
            this.verbose = verbose;
        }

        public Options(SingleTreeDualUpdatePhase singleTreeDualUpdatePhase, DualUpdater.DualUpdateType dualUpdateType, Initializer.InitializationType initializationType) {
            this(singleTreeDualUpdatePhase, dualUpdateType, initializationType, DEFAULT_VERBOSE);
        }

        public Options(DualUpdater.DualUpdateType updateType) {
            this(DEFAULT_PHASE, updateType, DEFAULT_INITIALIZATION_TYPE);
        }

        public Options() {
            this(DEFAULT_PHASE, DEFAULT_DUAL_UPDATE_TYPE, DEFAULT_INITIALIZATION_TYPE);
        }

        public Options(SingleTreeDualUpdatePhase singleTreeDualUpdatePhase) {
            this(singleTreeDualUpdatePhase, DEFAULT_DUAL_UPDATE_TYPE, DEFAULT_INITIALIZATION_TYPE, DEFAULT_VERBOSE);
        }

        public Options(Initializer.InitializationType initializationType) {
            this(DEFAULT_PHASE, DEFAULT_DUAL_UPDATE_TYPE, initializationType, DEFAULT_VERBOSE);
        }

        public Options(boolean verbose) {
            this(DEFAULT_PHASE, DEFAULT_DUAL_UPDATE_TYPE, DEFAULT_INITIALIZATION_TYPE, verbose);
        }
    }

    public static class Statistics {
        int shrinkNum = 0;
        int expandNum = 0;
        int growNum = 0;

        double augmentTime = 0;
        double expandTime = 0;
        double shrinkTime = 0;
        double growTime = 0;

        public int getShrinkNum() {
            return shrinkNum;
        }

        public int getExpandNum() {
            return expandNum;
        }

        public int getGrowNum() {
            return growNum;
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

    public class DualSolution {
        Graph<V, E> graph;

        Map<Set<V>, Double> dualVariables;

        public DualSolution(Graph<V, E> graph, Map<Set<V>, Double> dualVariables) {
            this.graph = graph;
            this.dualVariables = dualVariables;
        }

        public Graph<V, E> getGraph() {
            return graph;
        }

        public Map<Set<V>, Double> getDualVariables() {
            return dualVariables;
        }
    }
}
