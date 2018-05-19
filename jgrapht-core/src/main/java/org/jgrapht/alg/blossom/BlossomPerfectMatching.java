package org.jgrapht.alg.blossom;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.graph.AsUndirectedGraph;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class BlossomPerfectMatching<V, E> {
    public static final Options DEFAULT_OPTIONS = new Options(SingleTreeDualUpdatePhase.UPDATE_DUAL_BEFORE,
            DualUpdater.DualUpdateType.MULTIPLE_TREE_FIXED_DELTA, Initializer.InitializationType.GREEDY);
    public static final double EPS = 10e-12;
    public static final int INFINITY = Integer.MAX_VALUE;

    private final Graph<V, E> graph;
    State<V, E> state;
    private int n;
    private int m;
    private PrimalUpdater primalUpdater;
    private DualUpdater dualUpdater;
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
        this.state = initializer.initialize(options.initializationType);
        this.primalUpdater = new PrimalUpdater<>(state);
        this.dualUpdater = new DualUpdater<>(state, primalUpdater);

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
                // going through all adjacent trees via trees edges directing to them and
                // setting trees.currentEdge = treeEdge
                state.setCurrentEdges(tree);

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
                    // can grow trees
                    if ((edge = tree.plusInfinityEdges.min().getData()) != null && edge.slack <= 0) {
                        tree.removePlusInfinityEdge(edge);
                        primalUpdater.grow(edge, true);
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
                    if (dualUpdater.updateDualsSingle(tree)) {
                        // continue with the same trees
                        continue;
                    }
                }
                // clearing current edge pointers
                for(Tree.TreeEdgeIterator iterator = tree.treeEdgeIterator(); iterator.hasNext();){
                    iterator.next().head[iterator.getCurrentDirection()].currentEdge = null;
                }

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

    public Statistics getStatistics() {
        return state.statistics;
    }

    enum SingleTreeDualUpdatePhase {
        UPDATE_DUAL_BEFORE,
        UPDATE_DUAL_AFTER
    }

    public static class Options {
        SingleTreeDualUpdatePhase singleTreeDualUpdatePhase;
        DualUpdater.DualUpdateType dualUpdateType;
        Initializer.InitializationType initializationType;

        public Options(SingleTreeDualUpdatePhase singleTreeDualUpdatePhase, DualUpdater.DualUpdateType dualUpdateType, Initializer.InitializationType initializationType) {
            this.singleTreeDualUpdatePhase = singleTreeDualUpdatePhase;
            this.dualUpdateType = dualUpdateType;
            this.initializationType = initializationType;
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
