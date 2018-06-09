package org.jgrapht.alg.blossom;

import org.jgrapht.util.FibonacciHeap;

import static org.jgrapht.alg.blossom.KolmogorovMinimumWeightPerfectMatching.EPS;
import static org.jgrapht.alg.blossom.KolmogorovMinimumWeightPerfectMatching.INFINITY;

class DualUpdater<V, E> {
    private State<V, E> state;
    private PrimalUpdater primalUpdater;

    public DualUpdater(State<V, E> state, PrimalUpdater primalUpdater) {
        this.state = state;
        this.primalUpdater = primalUpdater;
    }

    double updateDuals(DualUpdateType type) {
        if (state.options.verbose) {
            System.out.println("Start updating duals");
        }
        // going through all trees roots
        for (Node root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            Tree tree = root.tree;
            double eps = getEps(tree);
            tree.accumulatedEps = eps - tree.eps;
        }
        if (type == DualUpdateType.MULTIPLE_TREE_FIXED_DELTA) {
            multipleTreeFixedDelta();
        } else if (type == DualUpdateType.MULTIPLE_TREE_CONNECTED_COMPONENTS) {
            updateDualsConnectedComponents();
        }

        double dualChange = 0;
        // updating trees.eps with respect to the accumulated eps
        for (Node root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            if (root.tree.accumulatedEps > 0) {
                dualChange += root.tree.accumulatedEps;
                root.tree.eps += root.tree.accumulatedEps;
            }
        }
        if (state.options.verbose) {
            for (Node root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
                System.out.println("Updating duals: now eps of " + root.tree + " is " + (root.tree.eps));
            }
        }
        return dualChange;
    }

    double getEps(Tree tree) {
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

    boolean updateDualsSingle(Tree tree) {
        double eps = getEps(tree);
        double eps_augment = INFINITY;
        Edge varEdge;
        Edge augmentEdge = null;
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
        if (eps > tree.eps) {
            delta = eps - tree.eps;
            tree.eps = eps;
            if (state.options.verbose) {
                System.out.println("Updating duals: now eps of " + tree + " is " + eps);
            }
        }
        if (augmentEdge != null && eps_augment <= tree.eps) {
            primalUpdater.augment(augmentEdge);
            return false;
        } else {
            return delta > EPS;
        }
    }

    boolean updateDualsConnectedComponents() {
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

            Tree nextTree = startTree;
            do {
                currentTree = nextTree;
                nextTree = nextTree.nextTree;
                currentTree.nextTree = dummyTree;
                currentTree.accumulatedEps = eps;
            } while (currentTree != nextTree);
        }
        return false;
    }

    void multipleTreeFixedDelta() {
        if (state.options.verbose) {
            System.out.println("Multiple tree fixed delta approach");
        }
        double eps = INFINITY;
        for (Node root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            Tree tree = root.tree;
            double treeEps = tree.eps;
            if (eps > tree.accumulatedEps) {
                eps = tree.accumulatedEps;
            }
            for (TreeEdge outgoingTreeEdge = tree.first[0]; outgoingTreeEdge != null; outgoingTreeEdge = outgoingTreeEdge.next[0]) {
                if (!outgoingTreeEdge.plusPlusEdges.isEmpty()) {
                    Edge minEdge = outgoingTreeEdge.plusPlusEdges.min().getData();
                    double oppositeTreeEps = outgoingTreeEdge.head[0].eps;
                    if (2 * eps > minEdge.slack - treeEps - oppositeTreeEps) {
                        eps = (minEdge.slack - treeEps - oppositeTreeEps) / 2;
                    }
                }
            }
        }
        for (Node root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            root.tree.accumulatedEps = eps;
        }
    }

    enum DualUpdateType {
        MULTIPLE_TREE_FIXED_DELTA,
        MULTIPLE_TREE_CONNECTED_COMPONENTS,
        MULTIPLE_TREE_STRONGLY_CONNECTED_COMPONENTS,
    }
}
