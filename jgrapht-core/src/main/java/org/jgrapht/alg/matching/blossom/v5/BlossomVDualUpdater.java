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

import org.jheaps.*;

import static org.jgrapht.alg.matching.blossom.v5.BlossomVOptions.DualUpdateStrategy.MULTIPLE_TREE_CONNECTED_COMPONENTS;
import static org.jgrapht.alg.matching.blossom.v5.BlossomVOptions.DualUpdateStrategy.MULTIPLE_TREE_FIXED_DELTA;
import static org.jgrapht.alg.matching.blossom.v5.KolmogorovMinimumWeightPerfectMatching.EPS;
import static org.jgrapht.alg.matching.blossom.v5.KolmogorovMinimumWeightPerfectMatching.INFINITY;

/**
 * This class is used by {@link KolmogorovMinimumWeightPerfectMatching} to perform dual updates,
 * thus increasing the dual objective function value and creating new tight edges.
 * <p>
 * This class currently supports three types of dual updates: single tree, multiple trees fixed
 * delta, and multiple tree variable delta. The first one is used to updates duals of a single tree,
 * when at least one of the {@link BlossomVOptions#updateDualsBefore} or
 * {@link BlossomVOptions#updateDualsAfter} is true. The latter two are used to update the duals
 * globally and are defined by the {@link BlossomVOptions}.
 * <p>
 * There are two type of constraints on a dual change of a tree: in-tree and cross-tree. In-tree
 * constraints are imposed by the infinity edges, (+, +) in-tree edges and "-" blossoms. Cross-tree
 * constraints are imposed by (+, +), (+, -) and (-, +) cross-tree edges. With respect to this
 * classification of constraints the following strategies of changing the duals can be used:
 * <ul>
 * <li>Single tree strategy greedily increases the duals of the tree with respect to the in-tree and
 * cross-tree constraints. This can result in a zero-change update. If a tight (+, +) cross-tree
 * edge is encountered during this operation, an immediate augmentation is performed
 * afterwards.</li>
 *
 * <li>Multiple tree fixed delta approach considers only in-tree constraints and constraints imposed
 * by the (+, +) cross-tree edges. Since this approach increases the trees' epsilons by the same
 * amount, it doesn't need to consider other two dual constraints. If a tight (+, +) cross-tree edge
 * is encountered during this operation, an immediate augmentation is performed afterwards.</li>
 *
 * <li>Multiple tree variable delta approach considers all types of constraints. It determines a
 * connected components in the auxiliary graph, where only tight (-, +) and (+, -) cross-tree edges
 * are present. For these connected components it computes the same dual change, therefore the
 * constraints imposed by the (-, +) and (+, -) cross-tree edges can't be violated. If a tight (+,
 * +) cross-tree edge is encountered during this operation, an immediate augmentation is performed
 * afterwards.</li>
 * </ul>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 * @see BlossomVPrimalUpdater
 * @see KolmogorovMinimumWeightPerfectMatching
 */
class BlossomVDualUpdater<V, E>
{
    /**
     * State information needed for the algorithm
     */
    private BlossomVState<V, E> state;
    /**
     * Instance of {@link BlossomVPrimalUpdater} for performing immediate augmentations after dual
     * updates when they are applicable. These speed up the overall algorithm.
     */
    private BlossomVPrimalUpdater<V, E> primalUpdater;

    /**
     * Creates a new instance of the BlossomVDualUpdater
     *
     * @param state the state common to {@link BlossomVPrimalUpdater}, {@link BlossomVDualUpdater}
     *        and {@link KolmogorovMinimumWeightPerfectMatching}
     * @param primalUpdater primal updater used by the algorithm
     */
    public BlossomVDualUpdater(BlossomVState<V, E> state, BlossomVPrimalUpdater<V, E> primalUpdater)
    {
        this.state = state;
        this.primalUpdater = primalUpdater;
    }

    /**
     * Performs global dual update. Operates on the whole graph and updates duals according to the
     * strategy defined by {@link BlossomVOptions#dualUpdateStrategy}.
     *
     * @param type the strategy to use for updating the duals
     * @return the sum of all changes of dual variables of the trees
     */
    public double updateDuals(BlossomVOptions.DualUpdateStrategy type)
    {
        long start = System.nanoTime();

        BlossomVEdge augmentEdge = null;
        if (KolmogorovMinimumWeightPerfectMatching.DEBUG) {
            System.out.println("Start updating duals");
        }
        // go through all tree roots and determine the initial tree dual change wrt. in-tree
        // constraints
        // the cross-tree constraints are handles wrt. dual update strategy
        for (BlossomVNode root = state.nodes[state.nodeNum].treeSiblingNext; root != null;
            root = root.treeSiblingNext)
        {
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
        // add tree.accumulatedEps to the tree.eps
        for (BlossomVNode root = state.nodes[state.nodeNum].treeSiblingNext; root != null;
            root = root.treeSiblingNext)
        {
            if (root.tree.accumulatedEps > EPS) {
                dualChange += root.tree.accumulatedEps;
                root.tree.eps += root.tree.accumulatedEps;
            }
        }
        if (KolmogorovMinimumWeightPerfectMatching.DEBUG) {
            for (BlossomVNode root = state.nodes[state.nodeNum].treeSiblingNext; root != null;
                root = root.treeSiblingNext)
            {
                System.out
                    .println("Updating duals: now eps of " + root.tree + " is " + (root.tree.eps));
            }
        }
        state.statistics.dualUpdatesTime += System.nanoTime() - start;
        if (augmentEdge != null) {
            primalUpdater.augment(augmentEdge);
        }
        return dualChange;
    }

    /**
     * Computes and returns the value which can be assigned to the {@code tree.eps} so that it
     * doesn't violate in-tree constraints. In other words, {@code getEps(tree) - tree.eps} is the
     * resulting dual change wrt. in-tree constraints. The computed value is always greater than or
     * equal to the {@code tree.eps}, can violate the cross-tree constraints, and can be equal to
     * {@link KolmogorovMinimumWeightPerfectMatching#INFINITY}.
     *
     * @param tree the tree to process
     * @return a value which can be safely assigned to tree.eps
     */
    private double getEps(BlossomVTree tree)
    {
        double eps = KolmogorovMinimumWeightPerfectMatching.INFINITY;
        // check minimum slack of the plus-infinity edges
        if (!tree.plusInfinityEdges.isEmpty()) {
            BlossomVEdge edge = tree.plusInfinityEdges.findMin().getValue();
            if (edge.slack < eps) {
                eps = edge.slack;
            }
        }
        // check minimum dual variable of the "-" blossoms
        if (!tree.minusBlossoms.isEmpty()) {
            BlossomVNode node = tree.minusBlossoms.findMin().getValue();
            if (node.dual < eps) {
                eps = node.dual;

            }
        }
        // check minimum slack of the (+, +) edges
        if (!tree.plusPlusEdges.isEmpty()) {
            BlossomVEdge edge = tree.plusPlusEdges.findMin().getValue();
            if (2 * eps > edge.slack) {
                eps = edge.slack / 2;
            }
        }
        return eps;
    }

    /**
     * Updates the duals of the single tree. This method takes into account both in-tree and
     * cross-tree constraints. If possible, it also finds a cross-tree (+, +) edge of minimum slack
     * and performs an augmentation.
     *
     * @param tree the tree to update duals of
     * @return true iff some progress was made and there was no augmentation performed, false
     *         otherwise
     */
    public boolean updateDualsSingle(BlossomVTree tree)
    {
        long start = System.nanoTime();

        double eps = getEps(tree); // include only constraints on (+,+) in-tree edges, (+, inf)
                                   // edges and "-' blossoms
        double epsAugment = KolmogorovMinimumWeightPerfectMatching.INFINITY; // takes into account
                                                                             // constraints of the
                                                                             // cross-tree edges
        BlossomVEdge augmentEdge = null; // the (+, +) cross-tree edge of minimum slack
        double delta = 0;
        for (BlossomVTree.TreeEdgeIterator iterator = tree.treeEdgeIterator();
            iterator.hasNext();)
        {
            BlossomVTreeEdge treeEdge = iterator.next();
            BlossomVTree opposite = treeEdge.head[iterator.getCurrentDirection()];
            if (!treeEdge.plusPlusEdges.isEmpty()) {
                BlossomVEdge plusPlusEdge = treeEdge.plusPlusEdges.findMin().getValue();
                if (plusPlusEdge.slack - opposite.eps < epsAugment) {
                    epsAugment = plusPlusEdge.slack - opposite.eps;
                    augmentEdge = plusPlusEdge;
                }
            }
            MergeableAddressableHeap<Double, BlossomVEdge> currentPlusMinusHeap =
                treeEdge.getCurrentPlusMinusHeap(opposite.currentDirection);
            if (!currentPlusMinusHeap.isEmpty()) {
                BlossomVEdge edge = currentPlusMinusHeap.findMin().getValue();
                if (edge.slack + opposite.eps < eps) {
                    eps = edge.slack + opposite.eps;

                }
            }
        }
        if (eps > epsAugment) {
            eps = epsAugment;
        }
        // now eps takes into account all the constraints
        if (eps > KolmogorovMinimumWeightPerfectMatching.NO_PERFECT_MATCHING_THRESHOLD) {
            throw new IllegalArgumentException(
                KolmogorovMinimumWeightPerfectMatching.NO_PERFECT_MATCHING);
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
     * Updates the duals via connected components. The connected components are a set of trees which
     * are connected via tight (+, -) cross tree edges. For these components the same dual change is
     * chosen. As a result, the circular constraints are guaranteed to be avoided. This is the point
     * where the {@link BlossomVDualUpdater#updateDualsSingle} approach can fail.
     */
    private BlossomVEdge updateDualsConnectedComponents()
    {
        BlossomVTree dummyTree = new BlossomVTree();
        BlossomVEdge augmentEdge = null;
        double augmentEps = INFINITY;

        double oppositeEps;
        for (BlossomVNode root = state.nodes[state.nodeNum].treeSiblingNext; root != null;
            root = root.treeSiblingNext)
        {
            root.tree.nextTree = null;
        }
        for (BlossomVNode root = state.nodes[state.nodeNum].treeSiblingNext; root != null;
            root = root.treeSiblingNext)
        {
            BlossomVTree startTree = root.tree;
            if (startTree.nextTree != null) {
                // this tree is present in some connected component and has been processed already
                continue;
            }
            double eps = startTree.accumulatedEps;

            startTree.nextTree = startTree;
            BlossomVTree connectedComponentLast = startTree;

            BlossomVTree currentTree = startTree;
            while (true) {
                for (BlossomVTree.TreeEdgeIterator iterator = currentTree.treeEdgeIterator();
                    iterator.hasNext();)
                {
                    BlossomVTreeEdge currentEdge = iterator.next();
                    int dir = iterator.getCurrentDirection();
                    BlossomVTree opposite = currentEdge.head[dir];
                    double plusPlusEps = KolmogorovMinimumWeightPerfectMatching.INFINITY;
                    int dirRev = 1 - dir;

                    if (!currentEdge.plusPlusEdges.isEmpty()) {
                        plusPlusEps = currentEdge.plusPlusEdges.findMin().getKey() - currentTree.eps
                            - opposite.eps;
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
                        plusMinusEps[dir] =
                            currentEdge.getCurrentPlusMinusHeap(dir).findMin().getKey()
                                - currentTree.eps + opposite.eps;
                    }
                    plusMinusEps[dirRev] = KolmogorovMinimumWeightPerfectMatching.INFINITY;
                    if (!currentEdge.getCurrentPlusMinusHeap(dirRev).isEmpty()) {
                        plusMinusEps[dirRev] =
                            currentEdge.getCurrentPlusMinusHeap(dirRev).findMin().getKey()
                                - opposite.eps + currentTree.eps;
                    }
                    if (opposite.nextTree == dummyTree) {
                        // opposite tree is in another connected component and has valid accumulated
                        // eps
                        oppositeEps = opposite.accumulatedEps;
                    } else if (plusMinusEps[0] > 0 && plusMinusEps[1] > 0) {
                        // this tree edge doesn't contain any tight (-, +) cross-tree edge and
                        // opposite tree
                        // hasn't been processed yet.
                        oppositeEps = 0;
                    } else {
                        // opposite hasn't been processed and there is a tight (-, +) cross-tree
                        // edge between
                        // current tree and opposite tree => we add opposite to the current
                        // connected component
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
                        // eps is bounded by the resulting slack of a (+, +) cross-tree edge
                        eps = plusPlusEps - oppositeEps;
                    }
                    if (eps > plusMinusEps[dir] + oppositeEps) {
                        // eps is bounded by the resulting slack of a (+, -) cross-tree edge in the
                        // current direction
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
                throw new IllegalArgumentException(
                    KolmogorovMinimumWeightPerfectMatching.NO_PERFECT_MATCHING);
            }

            // apply dual change to all trees in the connected component
            BlossomVTree nextTree = startTree;
            do {
                currentTree = nextTree;
                nextTree = nextTree.nextTree;
                currentTree.nextTree = dummyTree;
                currentTree.accumulatedEps = eps;
            } while (currentTree != nextTree);
        }
        if (augmentEdge != null && augmentEps - augmentEdge.head[0].tree.accumulatedEps
            - augmentEdge.head[1].tree.accumulatedEps <= 0)
        {
            return augmentEdge;
        }
        return null;
    }

    /**
     * Updates duals by iterating through trees and greedily increasing their dual variables.
     */
    private BlossomVEdge multipleTreeFixedDelta()
    {
        if (KolmogorovMinimumWeightPerfectMatching.DEBUG) {
            System.out.println("Multiple tree fixed delta approach");
        }
        BlossomVEdge varEdge;
        BlossomVEdge augmentEdge = null;
        double eps = INFINITY;
        double augmentEps = INFINITY;
        double slack;
        for (BlossomVNode root = state.nodes[state.nodeNum].treeSiblingNext; root != null;
            root = root.treeSiblingNext)
        {
            BlossomVTree tree = root.tree;
            double treeEps = tree.eps;
            eps = Math.min(eps, tree.accumulatedEps);
            // iterate only through outgoing tree edges so that every edge is considered only once
            for (BlossomVTreeEdge outgoingTreeEdge = tree.first[0]; outgoingTreeEdge != null;
                outgoingTreeEdge = outgoingTreeEdge.next[0])
            {
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
            throw new IllegalArgumentException(
                KolmogorovMinimumWeightPerfectMatching.NO_PERFECT_MATCHING);
        }
        for (BlossomVNode root = state.nodes[state.nodeNum].treeSiblingNext; root != null;
            root = root.treeSiblingNext)
        {
            root.tree.accumulatedEps = eps;
        }
        if (augmentEps <= 2 * eps) {
            return augmentEdge;
        }
        return null;
    }

}
