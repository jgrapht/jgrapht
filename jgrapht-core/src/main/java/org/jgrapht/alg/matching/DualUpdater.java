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
package org.jgrapht.alg.matching;

import org.jgrapht.util.FibonacciHeap;

import static java.lang.Math.abs;
import static org.jgrapht.alg.matching.KolmogorovMinimumWeightPerfectMatching.*;

/**
 * This class is used by {@link KolmogorovMinimumWeightPerfectMatching} to perform dual updates, thus creating
 * increasing the dual objective function value and creating new tight edges.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 * @see PrimalUpdater
 * @see KolmogorovMinimumWeightPerfectMatching
 * @since June 2018
 */
class DualUpdater<V, E> {
    /**
     * Store of the information needed for the algorithm
     */
    private State<V, E> state;
    /**
     * Instance of {@link PrimalUpdater} for performing immediate augmentations after dual
     * updates when they are applicable. These speeds the overall algorithm up.
     */
    private PrimalUpdater primalUpdater;

    /**
     * Creates a new instance of the DualUpdater
     *
     * @param state         the state common to {@link PrimalUpdater}, {@link DualUpdater} and {@link KolmogorovMinimumWeightPerfectMatching}
     * @param primalUpdater primal updater used by the algorithm
     */
    public DualUpdater(State<V, E> state, PrimalUpdater primalUpdater) {
        this.state = state;
        this.primalUpdater = primalUpdater;
    }

    /**
     * Method for general dual update. It operates on the whole graph and according to the strategy
     * defined by {@code strategy} performs dual update
     *
     * @param type the strategy to use in dual update
     * @return the sum of all changes of dual variables of the trees
     */
    public double updateDuals(DualUpdateStrategy type) {
        long start = System.nanoTime();

        if (DEBUG) {
            System.out.println("Start updating duals");
        }
        // going through all trees roots
        for (Node root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            Tree tree = root.tree;
            double eps = getEps(tree);
            tree.accumulatedEps = eps - tree.eps;
        }
        if (type == DualUpdateStrategy.MULTIPLE_TREE_FIXED_DELTA) {
            multipleTreeFixedDelta();
        } else if (type == DualUpdateStrategy.MULTIPLE_TREE_CONNECTED_COMPONENTS) {
            updateDualsConnectedComponents();
        }

        double dualChange = 0;
        // updating trees.eps with respect to the accumulated eps
        for (Node root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            if (abs(root.tree.accumulatedEps) > EPS) {
                dualChange += root.tree.accumulatedEps;
                root.tree.eps += root.tree.accumulatedEps;
            }
        }
        if (DEBUG) {
            for (Node root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
                System.out.println("Updating duals: now eps of " + root.tree + " is " + (root.tree.eps));
            }
        }

        state.statistics.dualUpdatesTime += System.nanoTime() - start;
        return dualChange;
    }

    /**
     * Computes and returns the value by which the dual variables of the "+" nodes of {@code tree} can be increased
     * and the dual variables of the "-" nodes of {@code tree} can be decreased. This value is bounded
     * by constraints on (+, +) in-tree edges, "-" blossoms and (+, inf) edges of the {@code tree}. As the result of
     * the lazy delta spreading technique, this value already contains the value of tree.eps.The computed
     * value can violate the constrains on the cross-tree edges and can be equal to
     * {@link KolmogorovMinimumWeightPerfectMatching#INFINITY}.
     *
     * @param tree the tree to process
     * @return a value which can be safely assigned to tree.eps
     */
    private double getEps(Tree tree) {
        double eps = INFINITY;
        Edge varEdge;
        // checking minimum slack of the plus-infinity edges
        if (!tree.plusInfinityEdges.isEmpty() && (varEdge = tree.plusInfinityEdges.min().getData()).slack < eps) {
            eps = varEdge.slack;
        }
        Node varNode;
        // checking minimum dual variable of the "-" blossoms
        if (!tree.minusBlossoms.isEmpty() && (varNode = tree.minusBlossoms.min().getData()).dual < eps) {
            eps = varNode.dual;
        }
        // checking minimum slack of the (+, +) edges
        if (!tree.plusPlusEdges.isEmpty()) {
            varEdge = tree.plusPlusEdges.min().getData();
            if (2 * eps > varEdge.slack) {
                eps = varEdge.slack / 2;
            }
        }
        return eps;
    }

    /**
     * Updates the duals of the single tree. This method operates locally on a single tree. It also finds
     * a cross-tree (+, +) edge of minimum slack and performs an augmentation if it is possible.
     *
     * @param tree the tree to update duals of
     * @return true iff some progress was made and there was no augmentation performed, false otherwise
     */
    public boolean updateDualsSingle(Tree tree) {
        long start = System.nanoTime();

        double eps = getEps(tree);  // include only constraints on (+,+) in-tree edges, (+, inf) edges and "-' blossoms
        double eps_augment = INFINITY; // takes into account constraints of the cross-tree edges
        Edge augmentEdge = null; // the (+, +) cross-tree edge of minimum slack
        Edge varEdge;
        double delta = 0;
        for (Tree.TreeEdgeIterator iterator = tree.treeEdgeIterator(); iterator.hasNext(); ) {
            TreeEdge treeEdge = iterator.next();
            Tree opposite = treeEdge.head[iterator.getCurrentDirection()];
            if (!treeEdge.plusPlusEdges.isEmpty() && (varEdge = treeEdge.plusPlusEdges.min().getData()).slack - opposite.eps < eps_augment) {
                eps_augment = varEdge.slack - opposite.eps;
                augmentEdge = varEdge;
            }
            FibonacciHeap<Edge> currentPlusMinusHeap = treeEdge.getCurrentPlusMinusHeap(opposite.currentDirection);
            if (!currentPlusMinusHeap.isEmpty() && (varEdge = currentPlusMinusHeap.min().getData()).slack + opposite.eps < eps) {
                eps = varEdge.slack + opposite.eps;
            }
        }
        if (eps > eps_augment) {
            eps = eps_augment;
        }
        // now eps takes into account all the constraints
        if (eps > NO_PERFECT_MATCHING_THRESHOLD) {
            throw new IllegalArgumentException(NO_PERFECT_MATCHING);
        }
        if (eps > tree.eps) {
            delta = eps - tree.eps;
            tree.eps = eps;
            if (DEBUG) {
                System.out.println("Updating duals: now eps of " + tree + " is " + eps);
            }
        }

        state.statistics.dualUpdatesTime += System.nanoTime() - start;

        if (augmentEdge != null && eps_augment <= tree.eps) {
            primalUpdater.augment(augmentEdge);
            return false; // can't proceed with the same tree
        } else {
            return delta > EPS;
        }
    }

    /**
     * Updates the duals via connected components. The connect components is a set of trees which
     * are connected via tight (+, -) cross tree edges. For these components the same dual change is
     * chosen. As a result, the circular constrains are avoided for sure. This is the point where
     * the {@link DualUpdater#multipleTreeFixedDelta()} approach can fail.
     */
    private void updateDualsConnectedComponents() {
        Node root;
        Tree startTree;
        Tree currentTree;
        Tree opposite;
        Tree dummyTree = new Tree();
        Tree connectedComponentLast;
        TreeEdge currentEdge;

        double eps;
        double oppositeEps;
        for (root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            root.tree.nextTree = null;
        }
        for (root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            startTree = root.tree;
            if (startTree.nextTree != null) {
                // this tree is present in some connected component and has been processed already
                continue;
            }
            eps = startTree.accumulatedEps;

            startTree.nextTree = connectedComponentLast = currentTree = startTree;

            while (true) {
                for (int dir = 0; dir < 2; ++dir) {
                    for (currentEdge = currentTree.first[dir]; currentEdge != null; currentEdge = currentEdge.next[dir]) {
                        opposite = currentEdge.head[dir];
                        double plusPlusEps = INFINITY;
                        int dirRev = 1 - dir;

                        if (!currentEdge.plusPlusEdges.isEmpty()) {
                            plusPlusEps = currentEdge.plusPlusEdges.min().getKey() - currentTree.eps - opposite.eps;
                        }
                        if (opposite.nextTree != null && opposite.nextTree != dummyTree) {
                            // opposite tree is in the same connected component
                            // since the trees in the same connected component have the same dual change
                            // we don't have to check (-, +) edges in this tree edge
                            if (2 * eps > plusPlusEps) {
                                eps = plusPlusEps / 2;
                            }
                            continue;
                        }

                        double[] plusMinusEps = new double[2];
                        plusMinusEps[dir] = INFINITY;
                        if (!currentEdge.getCurrentPlusMinusHeap(dir).isEmpty()) {
                            plusMinusEps[dir] = currentEdge.getCurrentPlusMinusHeap(dir).min().getKey() - currentTree.eps + opposite.eps;
                        }
                        plusMinusEps[dirRev] = INFINITY;
                        if (!currentEdge.getCurrentPlusMinusHeap(dirRev).isEmpty()) {
                            plusMinusEps[dirRev] = currentEdge.getCurrentPlusMinusHeap(dirRev).min().getKey() - opposite.eps + currentTree.eps;
                        }
                        if (opposite.nextTree == dummyTree) {
                            // opposite tree is in another connected component and has valid accumulated eps
                            oppositeEps = opposite.accumulatedEps;
                        } else if (plusMinusEps[0] > 0 && plusMinusEps[1] > 0) {
                            // this tree edge doesn't contain any tight (-, +) cross-tree edge and opposite tree
                            // hasn't been processed yet.
                            oppositeEps = 0;
                        } else {
                            // opposite hasn't been processed and there is a tight (-, +) cross-tree edge between
                            // current tree and opposite tree => we add opposite to the current connected component
                            connectedComponentLast.nextTree = opposite;
                            connectedComponentLast = opposite.nextTree = opposite;
                            if (eps > opposite.accumulatedEps) {
                                // eps of the connected component can't be greater than the minimum
                                // accumulated eps among trees in the connected component
                                eps = opposite.accumulatedEps;
                            }
                            continue;
                        }
                        if (eps > plusPlusEps - oppositeEps) {
                            // bounded by the resulting slack of a (+, +) cross-tree edge
                            eps = plusPlusEps - oppositeEps;
                        }
                        if (eps > plusMinusEps[dir] + oppositeEps) {
                            // bounded by the resulting slack of a (+, -) cross-tree edge in the current direction
                            eps = plusMinusEps[dir] + oppositeEps;
                        }

                    }
                }
                if (currentTree.nextTree == currentTree) {
                    // the end of the connected component
                    break;
                }
                currentTree = currentTree.nextTree;
            }

            if (eps > NO_PERFECT_MATCHING_THRESHOLD) {
                throw new IllegalArgumentException(NO_PERFECT_MATCHING);
            }

            // applying dual change to all trees in the connected component
            Tree nextTree = startTree;
            do {
                currentTree = nextTree;
                nextTree = nextTree.nextTree;
                currentTree.nextTree = dummyTree;
                currentTree.accumulatedEps = eps;
            } while (currentTree != nextTree);
        }
    }

    /**
     * Updates duals by iterating through trees and greedily increasing their dual variables. This approach
     * can fail if there are circular constraints on (+, -) cross-tree edges.
     */
    private void multipleTreeFixedDelta() {
        if (DEBUG) {
            System.out.println("Multiple tree fixed delta approach");
        }
        Edge varEdge;
        double eps = INFINITY;
        for (Node root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            Tree tree = root.tree;
            double treeEps = tree.eps;
            if (eps > tree.accumulatedEps) {
                eps = tree.accumulatedEps;
            }
            // iterating only through outgoing tree edges so that every edge is considered only once
            for (TreeEdge outgoingTreeEdge = tree.first[0]; outgoingTreeEdge != null; outgoingTreeEdge = outgoingTreeEdge.next[0]) {
                // since all epsilons are equal we don't have to check (+, -) cross tree edges
                if (!outgoingTreeEdge.plusPlusEdges.isEmpty()) {
                    varEdge = outgoingTreeEdge.plusPlusEdges.min().getData();
                    double oppositeTreeEps = outgoingTreeEdge.head[0].eps;
                    if (2 * eps > varEdge.slack - treeEps - oppositeTreeEps) {
                        eps = (varEdge.slack - treeEps - oppositeTreeEps) / 2;
                    }
                }
            }
        }
        if (eps > NO_PERFECT_MATCHING_THRESHOLD) {
            throw new IllegalArgumentException(NO_PERFECT_MATCHING);
        }
        for (Node root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            root.tree.accumulatedEps = eps;
        }
    }

    /**
     * Enum for choosing dual update strategy
     */
    enum DualUpdateStrategy {
        MULTIPLE_TREE_FIXED_DELTA,
        MULTIPLE_TREE_CONNECTED_COMPONENTS,
    }
}
