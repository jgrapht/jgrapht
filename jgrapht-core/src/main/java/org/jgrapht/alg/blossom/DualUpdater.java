package org.jgrapht.alg.blossom;

import org.jgrapht.util.FibonacciHeap;

import static org.jgrapht.alg.blossom.BlossomPerfectMatching.EPS;
import static org.jgrapht.alg.blossom.BlossomPerfectMatching.INFINITY;

class DualUpdater<V, E> {
    private State<V, E> state;
    private PrimalUpdater primalUpdater;

    public DualUpdater(State<V, E> state, PrimalUpdater primalUpdater) {
        this.state = state;
        this.primalUpdater = primalUpdater;
    }

    boolean updateDuals(DualUpdateType type) {
        // going through all trees roots
        for (Node root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            Tree tree = root.tree;
            double eps = getEps(tree);
            tree.accumulatedEps = eps - tree.eps;
        }
        if(type == DualUpdateType.MULTIPLE_TREE_FIXED_DELTA){
            multipleTreeFixedDelta();
        }

        double dualChange = 0;
        // updating trees.eps with respect to the accumulated eps
        for (Node root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            if (root.tree.accumulatedEps > 0) {
                dualChange += root.tree.accumulatedEps;
                root.tree.eps += root.tree.accumulatedEps;
            }
        }
        return dualChange > 0;
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
        varEdge = null;
        // checking minimum slack of the (+, +) edges
        while (!tree.plusPlusEdges.isEmpty()) {
            varEdge = tree.plusPlusEdges.min().getData();
            // TODO: when there are contracted blossoms, need to process this (+, +) edge
            if (true) {
                break;
            }
            tree.removePlusPlusEdge(varEdge);
        }
        if (varEdge != null && 2 * eps > varEdge.slack) {
            eps = varEdge.slack / 2;
        }
        return eps;
    }

    boolean updateDualsSingle(Tree tree) {
        double eps = getEps(tree);
        double eps_augment = INFINITY;
        Edge varEdge;
        Edge augmentEdge = null;
        Node varNode;
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
            if (eps > eps_augment) {
                eps = eps_augment;
            }
            if (eps > tree.eps) {
                delta = eps - tree.eps;
                tree.eps = eps;
            }
        }
        if (augmentEdge != null && eps_augment <= tree.eps) {
            primalUpdater.augment(augmentEdge);
            return false;
        } else {
            return delta > EPS;
        }
    }

    void multipleTreeFixedDelta() {
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
                    double oppositeTreeEps = minEdge.head[0].tree.eps;
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
