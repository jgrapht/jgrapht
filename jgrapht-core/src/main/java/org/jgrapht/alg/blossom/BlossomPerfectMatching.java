package org.jgrapht.alg.blossom;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.graph.AsUndirectedGraph;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.jgrapht.alg.blossom.DualUpdater.DualUpdateType.MULTIPLE_TREE_CONNECTED_COMPONENTS;
import static org.jgrapht.alg.blossom.DualUpdater.DualUpdateType.MULTIPLE_TREE_FIXED_DELTA;

public class BlossomPerfectMatching<V, E> {
    public static final Options DEFAULT_OPTIONS = new Options();
    public static final double EPS = 10e-12;
    public static final int INFINITY = Integer.MAX_VALUE;

    private final Graph<V, E> graph;
    private State<V, E> state;
    private MatchingAlgorithm.Matching<V, E> matching;
    private int n;
    private int m;
    private PrimalUpdater<V, E> primalUpdater;
    private DualUpdater<V, E> dualUpdater;
    private Options options;


    public BlossomPerfectMatching(Graph<V, E> graph) {
        this(graph, DEFAULT_OPTIONS);
    }

    public BlossomPerfectMatching(Graph<V, E> graph, Options options) {
        Objects.requireNonNull(graph);
        if (graph.getType().isDirected()) {
            this.graph = new AsUndirectedGraph<>(graph);
        } else {
            this.graph = graph;
        }
        this.n = graph.vertexSet().size();
        this.m = graph.edgeSet().size();
        this.options = Objects.requireNonNull(options);
    }

    public MatchingAlgorithm.Matching<V, E> solve() {
        Initializer<V, E> initializer = new Initializer<>(graph);
        this.state = initializer.initialize(options);
        this.primalUpdater = new PrimalUpdater<>(state);
        this.dualUpdater = new DualUpdater<>(state, primalUpdater);
        printMap();

        Node currentRoot;
        Node nextRoot;
        Node nextNextRoot = null;
        Tree tree;

        while (true) {
            int cycleTreeNum = state.treeNum;

            for (currentRoot = state.nodes[n].treeSiblingNext; currentRoot != null; ) {
                // initializing variables
                nextRoot = currentRoot.treeSiblingNext;
                if (nextRoot != null) {
                    nextNextRoot = nextRoot.treeSiblingNext;
                }
                tree = currentRoot.tree;
                int iterationTreeNum = state.treeNum;

                printState();

                // first phase
                state.setCurrentEdges(tree);

                if (options.singleTreeDualUpdatePhase == SingleTreeDualUpdatePhase.UPDATE_DUAL_BEFORE) {
                    dualUpdater.updateDualsSingle(tree);
                }

                // second phase
                // applying primal operations to the current tree while it is possible
                while (iterationTreeNum == state.treeNum) {
                    printState();
                    System.out.println("Current tree is " + tree + ", current root is " + currentRoot);
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
                    System.out.println("Can't do anything");
                    break;
                }
                printState();

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
                if (nextRoot != null && nextRoot.isInftyNode()) {
                    currentRoot = nextNextRoot;
                }
            }

            printTrees();
            printState();

            if (state.treeNum == 0) {
                // we are done
                break;
            }

            if (cycleTreeNum == state.treeNum) {
                if (dualUpdater.updateDuals(options.dualUpdateType) <= 0) {
                    dualUpdater.updateDuals(MULTIPLE_TREE_FIXED_DELTA);
                }
            }
        }

        return matching = primalUpdater.finish();
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
            System.out.println(edges[i] + (matched.contains(edges[i]) ? ", matched" : "") + (edges[i].slack == 0 ? ", tight" : ""));
        }
    }

    private void printTrees() {
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
        public static final SingleTreeDualUpdatePhase DEFAULT_PHASE = SingleTreeDualUpdatePhase.UPDATE_DUAL_BEFORE;
        public static final DualUpdater.DualUpdateType DEfAULT_DUAL_UPDATE_TYPE = MULTIPLE_TREE_CONNECTED_COMPONENTS;
        public static final Initializer.InitializationType DEFAULT_INITIALIZATION_TYPE = Initializer.InitializationType.GREEDY;
        public static final boolean DEFAULT_VERBOSE = true;

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
            this(DEFAULT_PHASE, DEfAULT_DUAL_UPDATE_TYPE, DEFAULT_INITIALIZATION_TYPE);
        }

        public Options(SingleTreeDualUpdatePhase singleTreeDualUpdatePhase) {
            this(singleTreeDualUpdatePhase, DEfAULT_DUAL_UPDATE_TYPE, DEFAULT_INITIALIZATION_TYPE, DEFAULT_VERBOSE);
        }

        public Options(Initializer.InitializationType initializationType) {
            this(DEFAULT_PHASE, DEfAULT_DUAL_UPDATE_TYPE, initializationType, DEFAULT_VERBOSE);
        }

        public Options(boolean verbose) {
            this(DEFAULT_PHASE, DEfAULT_DUAL_UPDATE_TYPE, DEFAULT_INITIALIZATION_TYPE, verbose);
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
}
