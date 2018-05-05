package org.jgrapht.alg.blossom;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.graph.AsUndirectedGraph;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class BlossomPerfectMatching<V, E> {
    public static final Options DEFAULT_OPTIONS = new Options(SingleTreeDualUpdatePhase.UPDATE_DUAL_BEFORE,
            DualUpdater.DualUpdateType.MULTIPLE_TREE_FIXED_DELTA, BlossomInitializer.InitializationType.GREEDY);
    public static final double EPS = 10e-12;
    public static final int INFTY = Integer.MAX_VALUE;

    private final Graph<V, E> graph;
    State<V, E> state;
    private int n;
    private int m;
    private PrimalUpdater primalUpdater;
    private DualUpdater dualUpdater;
    private Statistics statistics;
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
        BlossomInitializer<V, E> initializer = new BlossomInitializer<>(graph);
        this.state = initializer.initialize(options.initializationType);
        this.primalUpdater = new PrimalUpdater(state);
        this.dualUpdater = new DualUpdater(state, primalUpdater);

        Node root1 = null;
        Node root2 = null;
        Node root3 = null;
        Tree tree;

        while (true) {

            for (root1 = state.nodes[n].treeSiblingNext; root1 != null; ) {
                // initializing variables
                root2 = root1.treeSiblingNext;
                if (root2 != null) {
                    root3 = root2.treeSiblingNext;
                }
                tree = root1.tree;

                ////////////////////////////////////////////////////////////
                /////////////////////// first phase ////////////////////////
                ////////////////////////////////////////////////////////////
                // going through all adjacent trees via tree edges directing to them and
                // setting tree.currentEdge = treeEdge
                tree.forEachTreeEdge((treeEdge, dir) -> {
                    Tree tree2 = treeEdge.head[1 - dir];
                    tree2.currentEdge = treeEdge;
                    tree2.currentDirection = dir;
                });

                if (options.singleTreeDualUpdatePhase == SingleTreeDualUpdatePhase.UPDATE_DUAL_BEFORE) {
                    dualUpdater.updateDualsSingle(tree);
                }

                /////////////////////////////////////////////////////////////
                ////////////////////// second phase /////////////////////////
                /////////////////////////////////////////////////////////////
                int treeNum1 = state.treeNum;
                while (treeNum1 == state.treeNum) {
                    Edge edge;
                    Node node;
                    // can grow tree
                    if ((edge = tree.plusInftyEdges.min().getData()) != null && edge.slack <= 0) {
                        tree.plusInftyEdges.removeMin();
                        primalUpdater.grow(edge);
                    }
                    // can shrink blossom
                    else if ((edge = tree.plusPlusEdges.min().getData()) != null && edge.slack <= 0) {
                        primalUpdater.shrink(edge);
                    }
                    //
                    else if ((node = tree.minusBlossoms.min().getData()) != null && node.dual <= 0) {

                    }

                }


                //////////////////////////////////////////////////////////////
                ///////////////////////// third phase ////////////////////////
                //////////////////////////////////////////////////////////////
                if (options.singleTreeDualUpdatePhase == SingleTreeDualUpdatePhase.UPDATE_DUAL_AFTER) {
                    boolean progress = dualUpdater.updateDualsSingle(tree);
                    if (progress) {
                        // continue with the same tree
                        continue;
                    }
                }
                // clearing current edge pointers
                tree.forEachTreeEdge((treeEdge, dir) -> treeEdge.head[1 - dir] = null);

            }
            root1 = root2;
            if (root1 != null && !root1.isTreeRoot) {
                root1 = root3;
            }

            if (state.nodeNum == 0) break;
            if (!dualUpdater.updateDuals(options.dualUpdateType)) {
                dualUpdater.updateDuals(DualUpdater.DualUpdateType.MULTIPLE_TREE_FIXED_DELTA);
            }
        }


        return null;
    }

    Node getNode(V vertex) {
        return (Node) state.vertexMap.get(vertex);
    }

    enum SingleTreeDualUpdatePhase {
        UPDATE_DUAL_BEFORE,
        UPDATE_DUAL_AFTER
    }

    public static class Options {
        SingleTreeDualUpdatePhase singleTreeDualUpdatePhase;
        DualUpdater.DualUpdateType dualUpdateType;
        BlossomInitializer.InitializationType initializationType;

        public Options(SingleTreeDualUpdatePhase singleTreeDualUpdatePhase, DualUpdater.DualUpdateType dualUpdateType, BlossomInitializer.InitializationType initializationType) {
            this.singleTreeDualUpdatePhase = singleTreeDualUpdatePhase;
            this.dualUpdateType = dualUpdateType;
            this.initializationType = initializationType;
        }
    }

    class NodeIterator implements Iterator<Node> {
        int pos = 0;
        Node current = state.nodes[0];

        @Override
        public boolean hasNext() {
            if (current != null) {
                return true;
            }
            current = advance();
            return current != null;
        }

        @Override
        public Node next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Node result = current;
            current = null;
            return result;
        }

        private Node advance() {
            return ++pos < state.nodeNum ? (current = state.nodes[pos]) : null;
        }
    }

    public class Statistics {
        int shrinkNum;
        int expandNum;
        int growNum;

        public int getShrinkNum() {
            return shrinkNum;
        }

        public int getExpandNum() {
            return expandNum;
        }

        public int getGrowNum() {
            return growNum;
        }
    }
}
