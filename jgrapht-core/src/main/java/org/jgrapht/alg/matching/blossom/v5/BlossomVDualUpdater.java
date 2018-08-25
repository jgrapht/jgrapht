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
package org.jgrapht.alg.matching.blossom.v5;

import org.jheaps.MergeableAddressableHeap;

import static org.jgrapht.alg.matching.blossom.v5.BlossomVOptions.DualUpdateStrategy.MULTIPLE_TREE_CONNECTED_COMPONENTS;
import static org.jgrapht.alg.matching.blossom.v5.BlossomVOptions.DualUpdateStrategy.MULTIPLE_TREE_FIXED_DELTA;
import static org.jgrapht.alg.matching.blossom.v5.KolmogorovMinimumWeightPerfectMatching.EPS;
import static org.jgrapht.alg.matching.blossom.v5.KolmogorovMinimumWeightPerfectMatching.INFINITY;

/**
 * This class is used by {@link KolmogorovMinimumWeightPerfectMatching} to perform dual updates, thus creating
 * increasing the dual objective function value and creating new tight edges.
 * <p>
 * This class currently supports three types of dual updates: single tree, multiple trees fixed delta, and
 * multiple tree variable delta. The first one is used to updates duals of a single tree, when at least on
 * of the {@link BlossomVOptions#updateDualsBefore} or {@link BlossomVOptions#updateDualsAfter} is true.
 * The later two are used to update the duals globally and are defined by the {@link BlossomVOptions}.
 * <p>
 * There are two type of constraints on a dual change of a tree: in-tree cross-tree. In-tree constraints are
 * imposed by the infinity edges, (+, +) in-tree edges and "-" blossoms. Cross-tree constraints are imposed
 * by (+, +), (+, -) and (-, +) cross-tree edges. With respect to this classification of constraints the following
 * strategies of changing the duals can be used:
 * <ul>
 * <li>Single tree strategy greedily increases the duals of the tree with respect to the in-tree and
 * cross-tree constraints. This can result in a zero-change update. If a tight (+, +) cross-tree edge
 * is encountered during this operation, an immediate augmentation is performed afterwards.</li>
 *
 * <li>Multiple tree fixed delta approach considers only in-tree constraints and constraints imposed by
 * the (+, +) cross-tree edges. Since this approach increases the trees' epsilons by the same amount,
 * it doesn't need to consider other two dual constraints. If a tight (+, +) cross-tree edge
 * is encountered during this operation, an immediate augmentation is performed afterwards.</li>
 *
 * <li>Multiple tree variable delta approach considers all types of constraints. It determines a connected
 * components in the auxiliary graph, where only tight (-, +) and (+, -) cross-tree edges are present. For
 * these connected components it computes the same dual change, therefore the constraints imposed by the
 * (-, +) and (+, -) cross-tree edges can't be violated. If a tight (+, +) cross-tree edge
 * is encountered during this operation, an immediate augmentation is performed afterwards.</li>
 * </ul>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 * @see BlossomVPrimalUpdater
 * @see KolmogorovMinimumWeightPerfectMatching
 * @since June 2018
 */
class BlossomVDualUpdater<V, E> {
    /**
     * State information needed for the algorithm
     */
    private BlossomVState<V, E> state;
    /**
     * Instance of {@link BlossomVPrimalUpdater} for performing immediate augmentations after dual
     * updates when they are applicable. These speeds the overall algorithm up.
     */
    private BlossomVPrimalUpdater primalUpdater;

    /**
     * Creates a new instance of the BlossomVDualUpdater
     *
     * @param state         the state common to {@link BlossomVPrimalUpdater}, {@link BlossomVDualUpdater} and {@link KolmogorovMinimumWeightPerfectMatching}
     * @param primalUpdater primal updater used by the algorithm
     */
    public BlossomVDualUpdater(BlossomVState<V, E> state, BlossomVPrimalUpdater primalUpdater) {
        this.state = state;
        this.primalUpdater = primalUpdater;
    }

    /**
     * Method for general dual update. It operates on the whole graph and according to the strategy
     * defined by {@link BlossomVOptions#dualUpdateStrategy} updates duals.
     *
     * @param type the strategy to use in dual update
     * @return the sum of all changes of dual variables of the trees
     */
    public double updateDuals(BlossomVOptions.DualUpdateStrategy type) {
        long start = System.nanoTime();
        BlossomVEdge augmentEdge = null;
        if (KolmogorovMinimumWeightPerfectMatching.DEBUG) {
            System.out.println("Start updating duals");
        }
        // going through all trees roots
        for (BlossomVNode root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            BlossomVTree tree = root.tree;
            double eps = getEps(tree);
            tree.accumulatedEps = eps - tree.eps;
        }
        if (type == MULTIPLE_TREE_FIXED_DELTA) {
            augmentEdge = multipleTreeFixedDelta();
        } else if (type == MULTIPLE_TREE_CONNECTED_COMPONENTS) {
            augmentEdge = updateDualsConnectedComponents();
        }

        double dualChange = 0;
        // updating trees.eps with respect to the accumulated eps
        for (BlossomVNode root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            if (root.tree.accumulatedEps > EPS) {
                dualChange += root.tree.accumulatedEps;
                root.tree.eps += root.tree.accumulatedEps;
            }
        }
        if (KolmogorovMinimumWeightPerfectMatching.DEBUG) {
            for (BlossomVNode root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
                System.out.println("Updating duals: now eps of " + root.tree + " is " + (root.tree.eps));
            }
        }
        state.statistics.dualUpdatesTime += System.nanoTime() - start;
        if (augmentEdge != null) {
            primalUpdater.augment(augmentEdge);
        }
        return dualChange;
    }

    /**
     * Computes and returns the value by which the dual variables of the "+" nodes of {@code tree} can be increased
     * and the dual variables of the "-" nodes of {@code tree} can be decreased. This value is bounded
     * by constraints on (+, +) in-tree edges, "-" blossoms and (+, inf) edges of the {@code tree}. As the result of
     * the lazy delta spreading technique, this value already contains the value of tree.eps.The computed
     * value can violate the constraints on the cross-tree edges and can be equal to
     * {@link KolmogorovMinimumWeightPerfectMatching#INFINITY}.
     *
     * @param tree the tree to process
     * @return a value which can be safely assigned to tree.eps
     */
    private double getEps(BlossomVTree tree) {
        double eps = KolmogorovMinimumWeightPerfectMatching.INFINITY;
        BlossomVEdge varEdge;
        // checking minimum slack of the plus-infinity edges
        if (!tree.plusInfinityEdges.isEmpty() && (varEdge = tree.plusInfinityEdges.findMin().getValue()).slack < eps) {
            eps = varEdge.slack;
        }
        BlossomVNode varNode;
        // checking minimum dual variable of the "-" blossoms
        if (!tree.minusBlossoms.isEmpty() && (varNode = tree.minusBlossoms.findMin().getValue()).dual < eps) {
            eps = varNode.dual;
        }
        // checking minimum slack of the (+, +) edges
        if (!tree.plusPlusEdges.isEmpty()) {
            varEdge = tree.plusPlusEdges.findMin().getValue();
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
    public boolean updateDualsSingle(BlossomVTree tree) {
        long start = System.nanoTime();

        double eps = getEps(tree);  // include only constraints on (+,+) in-tree edges, (+, inf) edges and "-' blossoms
        double epsAugment = KolmogorovMinimumWeightPerfectMatching.INFINITY; // takes into account constraints of the cross-tree edges
        BlossomVEdge augmentEdge = null; // the (+, +) cross-tree edge of minimum slack
        BlossomVEdge varEdge;
        double delta = 0;
        for (BlossomVTree.TreeEdgeIterator iterator = tree.treeEdgeIterator(); iterator.hasNext(); ) {
            BlossomVTreeEdge treeEdge = iterator.next();
            BlossomVTree opposite = treeEdge.head[iterator.getCurrentDirection()];
            if (!treeEdge.plusPlusEdges.isEmpty() && (varEdge = treeEdge.plusPlusEdges.findMin().getValue()).slack - opposite.eps < epsAugment) {
                epsAugment = varEdge.slack - opposite.eps;
                augmentEdge = varEdge;
            }
            MergeableAddressableHeap<Double, BlossomVEdge> currentPlusMinusHeap = treeEdge.getCurrentPlusMinusHeap(opposite.currentDirection);
            if (!currentPlusMinusHeap.isEmpty() && (varEdge = currentPlusMinusHeap.findMin().getValue()).slack + opposite.eps < eps) {
                eps = varEdge.slack + opposite.eps;
            }
        }
        if (eps > epsAugment) {
            eps = epsAugment;
        }
        // now eps takes into account all the constraints
        if (eps > KolmogorovMinimumWeightPerfectMatching.NO_PERFECT_MATCHING_THRESHOLD) {
            throw new IllegalArgumentException(KolmogorovMinimumWeightPerfectMatching.NO_PERFECT_MATCHING);
        }
        if (eps > tree.eps) {
            delta = eps - tree.eps;
            tree.eps = eps;
            if (KolmogorovMinimumWeightPerfectMatching.DEBUG) {
                System.out.println("Updating duals: now eps of " + tree + " is " + eps);
            }
        }

        state.statistics.dualUpdatesTime += System.nanoTime() - start;

        if (augmentEdge != null && epsAugment <= tree.eps) {
            primalUpdater.augment(augmentEdge);
            return false; // can't proceed with the same tree
        } else {
            return delta > EPS;
        }
    }

    /**
     * Updates the duals via connected components. The connect components is a set of trees which
     * are connected via tight (+, -) cross tree edges. For these components the same dual change is
     * chosen. As a result, the circular constraints are guaranteed to be avoided. This is the point where
     * the {@link BlossomVDualUpdater#updateDualsSingle} approach can fail.
     */
    private BlossomVEdge updateDualsConnectedComponents() {
        BlossomVNode root;
        BlossomVTree startTree;
        BlossomVTree currentTree;
        BlossomVTree opposite;
        BlossomVTree dummyTree = new BlossomVTree();
        BlossomVTree connectedComponentLast;
        BlossomVTreeEdge currentEdge;
        BlossomVEdge augmentEdge = null;
        double augmentEps = INFINITY;

        int dir;
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
                for (BlossomVTree.TreeEdgeIterator iterator = currentTree.treeEdgeIterator(); iterator.hasNext(); ) {
                    currentEdge = iterator.next();
                    dir = iterator.getCurrentDirection();
                    opposite = currentEdge.head[dir];
                    double plusPlusEps = KolmogorovMinimumWeightPerfectMatching.INFINITY;
                    int dirRev = 1 - dir;

                    if (!currentEdge.plusPlusEdges.isEmpty()) {
                        plusPlusEps = currentEdge.plusPlusEdges.findMin().getKey() - currentTree.eps - opposite.eps;
                        if (augmentEps > plusPlusEps) {
                            augmentEps = plusPlusEps;
                            augmentEdge = currentEdge.plusPlusEdges.findMin().getValue();
                        }
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
                    plusMinusEps[dir] = KolmogorovMinimumWeightPerfectMatching.INFINITY;
                    if (!currentEdge.getCurrentPlusMinusHeap(dir).isEmpty()) {
                        plusMinusEps[dir] = currentEdge.getCurrentPlusMinusHeap(dir).findMin().getKey() - currentTree.eps + opposite.eps;
                    }
                    plusMinusEps[dirRev] = KolmogorovMinimumWeightPerfectMatching.INFINITY;
                    if (!currentEdge.getCurrentPlusMinusHeap(dirRev).isEmpty()) {
                        plusMinusEps[dirRev] = currentEdge.getCurrentPlusMinusHeap(dirRev).findMin().getKey() - opposite.eps + currentTree.eps;
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
                if (currentTree.nextTree == currentTree) {
                    // the end of the connected component
                    break;
                }
                currentTree = currentTree.nextTree;
            }

            if (eps > KolmogorovMinimumWeightPerfectMatching.NO_PERFECT_MATCHING_THRESHOLD) {
                throw new IllegalArgumentException(KolmogorovMinimumWeightPerfectMatching.NO_PERFECT_MATCHING);
            }

            // applying dual change to all trees in the connected component
            BlossomVTree nextTree = startTree;
            do {
                currentTree = nextTree;
                nextTree = nextTree.nextTree;
                currentTree.nextTree = dummyTree;
                currentTree.accumulatedEps = eps;
            } while (currentTree != nextTree);
        }
        if (augmentEdge != null && augmentEps - augmentEdge.head[0].tree.accumulatedEps - augmentEdge.head[1].tree.accumulatedEps <= 0) {
            return augmentEdge;
        }
        return null;
    }

    /**
     * Updates duals by iterating through trees and greedily increasing their dual variables. This approach
     * can fail if there are circular constraints on (+, -) cross-tree edges.
     */
    private BlossomVEdge multipleTreeFixedDelta() {
        if (KolmogorovMinimumWeightPerfectMatching.DEBUG) {
            System.out.println("Multiple tree fixed delta approach");
        }
        BlossomVEdge varEdge;
        BlossomVEdge augmentEdge = null;
        double eps = INFINITY;
        double augmentEps = INFINITY;
        double slack;
        for (BlossomVNode root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            BlossomVTree tree = root.tree;
            double treeEps = tree.eps;
            eps = Math.min(eps, tree.accumulatedEps);
            // iterating only through outgoing tree edges so that every edge is considered only once
            for (BlossomVTreeEdge outgoingTreeEdge = tree.first[0]; outgoingTreeEdge != null; outgoingTreeEdge = outgoingTreeEdge.next[0]) {
                // since all epsilons are equal we don't have to check (+, -) cross tree edges
                if (!outgoingTreeEdge.plusPlusEdges.isEmpty()) {
                    varEdge = outgoingTreeEdge.plusPlusEdges.findMin().getValue();
                    slack = varEdge.slack - treeEps - outgoingTreeEdge.head[0].eps;
                    eps = Math.min(eps, slack / 2);
                    if (augmentEps > slack) {
                        augmentEps = slack;
                        augmentEdge = varEdge;
                    }
                }
            }
        }
        if (eps > KolmogorovMinimumWeightPerfectMatching.NO_PERFECT_MATCHING_THRESHOLD) {
            throw new IllegalArgumentException(KolmogorovMinimumWeightPerfectMatching.NO_PERFECT_MATCHING);
        }
        for (BlossomVNode root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            root.tree.accumulatedEps = eps;
        }
        if (augmentEps <= 2 * eps) {
            return augmentEdge;
        }
        return null;
    }

}
