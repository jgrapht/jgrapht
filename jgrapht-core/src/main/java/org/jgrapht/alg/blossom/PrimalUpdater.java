package org.jgrapht.alg.blossom;

import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.util.FibonacciHeap;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.jgrapht.alg.blossom.Node.Label.*;

class PrimalUpdater<V, E> {
    private State<V, E> state;

    public PrimalUpdater(State<V, E> state) {
        this.state = state;
    }

    public void grow(Edge edge, boolean manyGrows) {
        int dirToMinusNode = edge.head[0].isInftyNode() ? 0 : 1;
        Node nodeInTheTree = edge.head[1 - dirToMinusNode];
        nodeInTheTree.tree.removePlusInfinityEdge(edge);
        recursiveGrow(edge, manyGrows);
    }

    public void augment(Edge augmentEdge) {
        System.out.println("Augmenting edge " + augmentEdge);
        Node node;
        // augmenting trees on both sides
        for (int dir = 0; dir < 2; dir++) {
            node = augmentEdge.head[dir];
            augmentBranch(node, augmentEdge);
            node.matched = augmentEdge;
        }

    }

    public Node shrink(Edge blossomFormingEdge) {
        Node blossomRoot = findBlossomRoot(blossomFormingEdge);
        Tree tree = blossomRoot.tree;
        Node blossom = new Node();
        // initializing blossom
        blossom.tree = tree;
        blossom.isBlossom = true;
        blossom.isOuter = true;
        blossom.isTreeRoot = blossomRoot.isTreeRoot;
        blossom.dual = -tree.eps;
        if (blossom.isTreeRoot) {
            tree.root = blossom;
        } else {
            blossom.matched = blossomRoot.matched;
        }

        // mark all blossom nodes
        for (State.BlossomNodesIterator iterator = state.blossomNodesIterator(blossomRoot, blossomFormingEdge); iterator.hasNext(); ) {
            iterator.next().isMarked = true;
        }

        updateTreeStructure(blossomRoot, blossomFormingEdge, blossom);

        substituteRootWithBlossom(blossomRoot, blossom);

        setBlossomSiblings(blossomRoot, blossomFormingEdge);

        // resetting marks of blossom nodes
        blossomRoot.isMarked = false;
        for (Node current = blossomRoot.blossomSibling.getOpposite(blossomRoot); current != blossomRoot; current = current.blossomSibling.getOpposite(current)) {
            current.isMarked = false;
        }

        return blossom;
    }

    public void expand(Node blossom) {
        System.out.println("Expanding blossom " + blossom);
        Tree tree = blossom.tree;
        double eps = tree.eps;
        blossom.dual -= eps;
        blossom.tree.removeMinusBlossom(blossom);

        Edge parentEdge = blossom.parentEdge;
        int dirToBlossomFromParent = parentEdge.getDirTo(blossom);  // (parentEdge + dirToBlossom) points to blossom
        Node branchesEndpoint = parentEdge.headOriginal[dirToBlossomFromParent].getPenultimateBlossom();

        Edge matchedEdge = blossom.matched;
        int dirToBlossomFromMatched = matchedEdge.getDirTo(blossom);
        Node blossomRoot = matchedEdge.headOriginal[dirToBlossomFromMatched];

        // marking blossom nodes
        Node current = blossomRoot;
        do {
            current.isMarked = true;
            current = current.blossomSibling.getOpposite(current);
        } while (current != blossomRoot);

        // moving all edge from blossom to penultimate children
        Set<Pair<Edge, Integer>> edges = blossom.getEdges();
        Edge edge;
        for (Pair<Edge, Integer> pair : edges) {
            edge = pair.getFirst();
            int dir = pair.getSecond();
            Node penultimateChild = edge.headOriginal[1 - dir].getPenultimateBlossom();
            state.moveEdge(blossom, penultimateChild, edge);
        }

        // reversing the circular blossomSibling references so that the first branch in even branch
        if (!forwardDirection(blossomRoot, branchesEndpoint)) {
            reverseBlossomSiblings(blossomRoot);
        }

        // changing the matching, the labeling and the dual information on the odd branch
        processOddBranchExpand(blossomRoot, branchesEndpoint, eps, tree);

        // changing the matching, the labeling and dual information on the even branch
        processEvenBranchExpand(blossomRoot, branchesEndpoint, blossom);

        // resetting marks of blossom nodes
        current = blossomRoot;
        do {
            current.isMarked = true;
            current = current.blossomSibling.getOpposite(current);
        } while (current != blossomRoot);

        state.statistics.expandNum++;
        state.removedNum++;
        if (state.removedNum > 4 * state.nodeNum) {
            freeRemoved();
        }
    }

    public MatchingAlgorithm.Matching<V, E> finish() {
        Set<E> edges = new HashSet<>();
        double weight = 0;
        Node[] nodes = state.nodes;
        Node blossomRoot;
        Node node;
        Node nextNode;
        Node blossomPrev;
        Node blossom;
        E edge;

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
                    node = blossomRoot.blossomSibling.getOpposite(blossomRoot);
                    // changing the matching in the blossom
                    while (node != blossomRoot) {
                        node.matched = node.blossomSibling;
                        nextNode = node.blossomSibling.getOpposite(node);
                        nextNode.matched = node.matched;
                        node = nextNode.blossomSibling.getOpposite(nextNode);
                    }

                    blossom = blossomPrev;
                    if (!blossom.isBlossom) {
                        break;
                    }
                    blossomPrev = blossom.blossomGrandparent;
                }
            }
        }
        for (int i = 0; i < state.nodeNum; i++) {
            edge = state.backEdgeMap.get(nodes[i].matched);
            if (edge == null) {
                System.out.println("Node " + i + " is unmatched");
                throw new RuntimeException();
            } else {
                if (!edges.contains(edge)) {
                    edges.add(edge);
                    weight += state.graph.getEdgeWeight(edge);
                }
            }
        }
        return new MatchingAlgorithm.MatchingImpl<>(state.graph, edges, weight);
    }

    private void processEvenBranchExpand(Node blossomRoot, Node branchesEndpoint, Node blossom) {
        Tree tree = blossom.tree;
        double eps = tree.eps;
        blossom.matched.getOpposite(blossomRoot).treeParent = blossomRoot;
        blossomRoot.tree = tree;
        blossomRoot.addChild(blossom.matched.getOpposite(blossomRoot));
        blossom.removeFromChildList();
        blossom.treeParent.addChild(branchesEndpoint);

        Node current = blossomRoot;
        current.matched = blossom.matched;
        current.label = MINUS;
        current.isOuter = true;
        current.treeParent = blossom.treeParent;
        processMinusNodeExpand(current, eps);
        Edge prevMatched;
        Node prevNode = current;
        while (current != branchesEndpoint) {
            // processing "+" node
            current = current.blossomSibling.getOpposite(current);
            current.label = PLUS;
            current.isOuter = true;
            current.tree = tree;
            current.matched = prevMatched = current.blossomSibling;
            current.addChild(prevNode);
            prevNode.treeParent = current;
            prevNode = current;
            processPlusNodeExpand(current, eps);

            // processing "-" node
            current = current.blossomSibling.getOpposite(current);
            if (current.isBlossom) {
                tree.addMinusBlossom(current, current.dual);
            }
            current.label = MINUS;
            current.isOuter = true;
            current.tree = tree;
            current.matched = prevMatched;
            current.addChild(prevNode);
            prevNode.treeParent = current;
            prevNode = current;
            processMinusNodeExpand(current, eps);
        }

    }

    private void processOddBranchExpand(Node blossomRoot, Node branchesEndpoint, double eps, Tree tree) {
        Node current = branchesEndpoint.blossomSibling.getOpposite(branchesEndpoint);
        Edge prevMatched;
        while (current != blossomRoot) {
            current.label = INFTY;
            current.isOuter = true;
            current.matched = prevMatched = current.blossomSibling;
            processInfinityNodeExpand(current, eps, tree);
            current = current.blossomSibling.getOpposite(current);

            current.label = INFTY;
            current.isOuter = true;
            current.matched = prevMatched;
            processInfinityNodeExpand(current, eps, tree);
            current = current.blossomSibling.getOpposite(current);
        }
    }

    private void processPlusNodeExpand(Node plusNode, double eps) {
        plusNode.dual -= eps; // applying lazy delta spreading
        Edge edge;
        for (Node.AdjacentEdgeIterator iterator = plusNode.adjacentEdgesIterator(); iterator.hasNext(); ) {
            edge = iterator.next();
            Node opposite = edge.head[iterator.getDir()];
            if (!opposite.isMarked) {
                // this is boundary edge
                edge.slack += 2 * eps; // the endpoint changes its label to "+"
            } else if (opposite.isInftyNode()) {
                // this edge is inner edge between even and odd branches
                edge.slack += eps;
            }
            if (opposite.isPlusNode()) {
                if (opposite.tree == plusNode.tree) {
                    // this edge becomes a (+, +) in-tree edge
                    plusNode.tree.addPlusPlusEdge(edge, edge.slack);
                } else {
                    // opposite is from another tree since it's label is "+"
                    opposite.tree.currentEdge.removeFromCurrentMinusPlusHeap(edge, opposite.tree.currentDirection);
                    opposite.tree.currentEdge.addPlusPlusEdge(edge, edge.slack);
                }
            } else if (opposite.isMinusNode()) {
                if (opposite.tree != plusNode.tree) {
                    // this edge becomes a (+, -) cross-tree edge
                    if (opposite.tree.currentEdge == null) {
                        State.addTreeEdge(plusNode.tree, opposite.tree);
                    }
                    opposite.tree.currentEdge.addToCurrentPlusMinusHeap(edge, edge.slack, opposite.tree.currentDirection);
                }
            } else {
                if (!opposite.isMarked) {
                    // this is not an inner blossom edge
                    plusNode.tree.removePlusInfinityEdge(edge); // edge's key is invalid
                }
                plusNode.tree.addPlusInfinityEdge(edge, edge.slack); // updating edge's key
            }
        }
    }

    private void processMinusNodeExpand(Node minusNode, double eps) {
        minusNode.dual += eps;
        Edge edge;
        Node opposite;
        for (Node.AdjacentEdgeIterator iterator = minusNode.adjacentEdgesIterator(); iterator.hasNext(); ) {
            edge = iterator.next();
            opposite = edge.head[iterator.getDir()];
            if (opposite.isMarked && opposite.isInftyNode()) {
                // this is an (-, inf) inner edge
                edge.slack -= eps;
            }
        }
    }

    private void processInfinityNodeExpand(Node infinityNode, double eps, Tree tree) {
        Edge edge;
        for (Node.AdjacentEdgeIterator iterator = infinityNode.adjacentEdgesIterator(); iterator.hasNext(); ) {
            edge = iterator.next();
            Node opposite = edge.head[iterator.getDir()];
            if (!opposite.isMarked) {
                edge.slack += eps; // since edge's label changes to inf and this is a boundary edge
                if (opposite.isPlusNode()) {
                    // if this node is marked => it's a blossom node => this edge has been processed already
                    if (opposite.tree != tree) {
                        opposite.tree.currentEdge.removeFromCurrentMinusPlusHeap(edge, opposite.tree.currentDirection);
                    }
                    opposite.tree.addPlusInfinityEdge(edge, edge.slack);
                }
            }
        }
    }

    private void recursiveGrow(Edge edge, boolean manyGrows) {
        System.out.println("Growing edge " + edge);
        int dirToMinusNode = edge.head[0].isInftyNode() ? 0 : 1;
        Node nodeInTheTree = edge.head[1 - dirToMinusNode];

        Node minusNode = edge.head[dirToMinusNode];
        Node plusNode = minusNode.matched.getOpposite(minusNode);

        minusNode.setLabel(MINUS);
        plusNode.setLabel(PLUS);

        minusNode.treeParent = nodeInTheTree;
        plusNode.treeParent = minusNode;
        minusNode.parentEdge = edge;
        plusNode.parentEdge = plusNode.matched;

        nodeInTheTree.addChild(minusNode);
        minusNode.addChild(plusNode);


        processMinusNodeGrow(minusNode);
        processPlusNodeGrow(plusNode, manyGrows);

    }

    private void processMinusNodeGrow(Node minusNode) {
        // maintaining heap of "-" blossoms
        double eps = minusNode.tree.eps;
        minusNode.dual += eps;
        Edge edge;
        int dir;
        // maintaining heap of "-" blossoms
        if (minusNode.isBlossom) {
            minusNode.tree.addMinusBlossom(minusNode, minusNode.dual);
        }
        // maintaining minus-plus edges in the minus-plus heaps in the tree edges
        for (Node.AdjacentEdgeIterator iterator = minusNode.adjacentEdgesIterator(); iterator.hasNext(); ) {
            edge = iterator.next();
            dir = iterator.getDir();
            Node opposite = edge.head[dir];
            edge.slack -= eps;
            if (opposite.isPlusNode() && opposite.tree != minusNode.tree) {
                // encountered (-,+) cross-tree edge
                if (opposite.tree.currentEdge == null) {
                    State.addTreeEdge(minusNode.tree, opposite.tree);
                }
                opposite.tree.removePlusInfinityEdge(edge);
                opposite.tree.currentEdge.addToCurrentMinusPlusHeap(edge, edge.slack, opposite.tree.currentDirection);
            }
        }
    }

    private void processPlusNodeGrow(Node plusNode, boolean manyGrows) {
        double eps = plusNode.tree.eps;
        plusNode.dual -= eps;
        Edge edge;
        int dir;
        List<Edge> growEdges = new LinkedList<>();
        for (Node.AdjacentEdgeIterator iterator = plusNode.adjacentEdgesIterator(); iterator.hasNext(); ) {
            edge = iterator.next();
            dir = iterator.getDir();
            Node opposite = edge.head[dir];
            // maintaining heap of plus-infinity edges
            edge.slack += eps;
            if (opposite.isPlusNode()) {
                // this is a (+,+) edge
                if (opposite.tree == plusNode.tree) {
                    // this is blossom-forming edge
                    plusNode.tree.removePlusInfinityEdge(edge);
                    plusNode.tree.addPlusPlusEdge(edge, edge.slack);
                } else {
                    // this is plus-plus edge to another trees
                    if (opposite.tree.currentEdge == null) {
                        State.addTreeEdge(plusNode.tree, opposite.tree);
                    }
                    opposite.tree.removePlusInfinityEdge(edge);
                    opposite.tree.currentEdge.addPlusPlusEdge(edge, edge.slack);
                }
            } else if (opposite.isMinusNode()) {
                // this is a (+,-) edge
                if (opposite.tree != plusNode.tree) {
                    // this is (+,-) edge to another trees
                    if (opposite.tree.currentEdge == null) {
                        State.addTreeEdge(plusNode.tree, opposite.tree);
                    }
                    opposite.tree.currentEdge.addToCurrentPlusMinusHeap(edge, edge.slack, opposite.tree.currentDirection);
                }
            } else if (opposite.isInftyNode()) {
                if (edge.slack > eps || !manyGrows) {
                    plusNode.tree.addPlusInfinityEdge(edge, edge.slack);
                } else {
                    growEdges.add(edge);
                }
            }
        }
        for (Edge growEdge : growEdges) {
            recursiveGrow(growEdge, false);
        }
        state.statistics.growNum++;
    }

    private void augmentBranch(Node firstNode, Edge augmentEdge) {
        Tree tree = firstNode.tree;
        double eps = tree.eps;
        Node root = tree.root;
        TreeEdge treeEdge;
        Edge edge;
        Node node;
        int dir;

        // setting currentEdge and currentDirection of all opposite trees connected via treeEdge
        state.setCurrentEdges(tree);

        // applying tree.eps to all tree nodes and updating slacks of all incident edges
        for (Tree.TreeNodeIterator treeNodeIterator = tree.treeNodeIterator(); treeNodeIterator.hasNext(); ) {
            node = treeNodeIterator.next();
            // applying lazy delta spreading
            if (node.isPlusNode()) {
                node.dual += eps;
            } else {
                node.dual -= eps;
            }
            for (Node.AdjacentEdgeIterator adjacentEdgeIterator = node.adjacentEdgesIterator(); adjacentEdgeIterator.hasNext(); ) {
                edge = adjacentEdgeIterator.next();
                dir = adjacentEdgeIterator.getDir();
                Node opposite = edge.head[dir];
                Tree oppositeTree = opposite.tree;
                if (node.isPlusNode()) {
                    edge.slack -= eps;
                    if (oppositeTree != null && oppositeTree != tree) {
                        // if this edge is a cross-tree edge
                        treeEdge = oppositeTree.currentEdge;
                        int currentDir = oppositeTree.currentDirection;
                        if (opposite.isPlusNode()) {
                            // this is a (+,+) cross-tree edge
                            treeEdge.removeFromPlusPlusHeap(edge);
                            oppositeTree.addPlusInfinityEdge(edge, edge.slack);
                        } else if (opposite.isMinusNode()) {
                            // this is a (+,-) cross-tree edge
                            treeEdge.removeFromCurrentPlusMinusHeap(edge, currentDir);
                        }
                    }
                } else {
                    // current node is a "-" node
                    if (oppositeTree != null && oppositeTree != tree && opposite.isPlusNode()) {
                        // this is a (-,+) cross-tree edge
                        treeEdge = oppositeTree.currentEdge;
                        int currentDir = oppositeTree.currentDirection;
                        treeEdge.removeFromCurrentMinusPlusHeap(edge, currentDir);
                        oppositeTree.addPlusInfinityEdge(edge, edge.slack + eps);
                    }
                    edge.slack += eps;

                }
            }
            node.setLabel(Node.Label.INFTY);
            node.tree = null;
        }

        // adding all elements from the (-,+) and (+,+) heaps to (+, inf) heaps of the opposite trees and
        // deleting tree edges
        for (Tree.TreeEdgeIterator treeEdgeIterator = tree.treeEdgeIterator(); treeEdgeIterator.hasNext(); ) {
            treeEdge = treeEdgeIterator.next();
            dir = treeEdgeIterator.getCurrentDirection();
            Tree opposite = treeEdge.head[dir];
            opposite.currentEdge = null;

            opposite.plusInfinityEdges = FibonacciHeap.union(opposite.plusInfinityEdges, treeEdge.plusPlusEdges);
            opposite.plusInfinityEdges = FibonacciHeap.union(opposite.plusInfinityEdges, treeEdge.getCurrentMinusPlusHeap(dir));
            treeEdge.removeFromTreeEdgeList();
        }

        // updating matching

        Edge matchedEdge = augmentEdge;
        Node plusNode = firstNode;
        Node minusNode = plusNode.treeParent;
        while (minusNode != null) {
            plusNode.matched = matchedEdge;
            matchedEdge = minusNode.parentEdge;
            minusNode.matched = matchedEdge;
            plusNode = minusNode.treeParent;
            minusNode = plusNode.treeParent;
        }
        root.matched = matchedEdge;

        // removing root from the linked list of roots;
        root.treeSiblingPrev.treeSiblingNext = root.treeSiblingNext;
        if (root.treeSiblingNext != null) {
            root.treeSiblingNext.treeSiblingPrev = root.treeSiblingPrev;
        }
        root.isTreeRoot = false;

        state.treeNum--;
    }

    Node findBlossomRoot(Edge edge) {
        Node root;
        Node upperBound;
        Node varNode;
        Node[] endPoints = new Node[2];
        endPoints[0] = edge.head[0];
        endPoints[1] = edge.head[1];
        int branch = 0;
        while (true) {
            if (endPoints[branch].isMarked) {
                root = endPoints[branch];
                upperBound = endPoints[1 - branch];
                break;
            }
            endPoints[branch].isMarked = true;
            if (endPoints[branch].isTreeRoot) {
                upperBound = endPoints[branch];
                varNode = endPoints[1 - branch];
                while (!varNode.isMarked) {
                    varNode = varNode.treeParent.treeParent;
                }
                root = varNode;
                break;
            }
            endPoints[branch] = endPoints[branch].treeParent.treeParent;
        }
        varNode = root;
        while (varNode != upperBound) {
            varNode = varNode.treeParent.treeParent;
            varNode.isMarked = false;
        }
        clearIsMarkedAndSetIsOuter(root, edge.head[0]);
        clearIsMarkedAndSetIsOuter(root, edge.head[1]);

        return root;
    }

    private void clearIsMarkedAndSetIsOuter(Node root, Node start) {
        while (start != root) {
            start.isMarked = false;
            start.isOuter = false;
            start = start.treeParent;
            start.isOuter = false;
            start = start.treeParent;
        }
        root.isOuter = false;
        root.isMarked = false;
    }

    private void substituteRootWithBlossom(Node root, Node blossom) {
        // inserting blossom in the child list of root.treeParent
        blossom.treeSiblingNext = root.treeSiblingNext;
        blossom.treeSiblingPrev = root.treeSiblingPrev;
        if (blossom.treeSiblingPrev != null) {
            // blossom is not the first vertex in the child list
            blossom.treeSiblingPrev.treeSiblingNext = blossom;
        } else {
            // blossom is the first vertex in the child list
            root.treeParent.firstTreeChild = blossom;
        }
        if (blossom.treeSiblingNext != null) {
            // blossom isn't the last vertex in the child list
            blossom.treeSiblingNext.treeSiblingPrev = blossom;
        } else if (root.treeParent != null) {
            root.treeParent.firstTreeChild.treeSiblingPrev = blossom;
        }
    }

    private void setBlossomSiblings(Node blossomRoot, Edge blossomFormingEdge) {
        // setting blossom sibling nodes
        Edge prevEdge = blossomFormingEdge;
        for (State.BlossomNodesIterator iterator = state.blossomNodesIterator(blossomRoot, blossomFormingEdge); iterator.hasNext(); ) {
            Node current = iterator.next();
            if (iterator.getCurrentDirection() == 0) {
                current.blossomSibling = prevEdge;
                prevEdge = current.parentEdge;
            } else {
                current.blossomSibling = current.parentEdge;
            }
        }
    }

    private void updateTreeStructure(Node blossomRoot, Edge blossomFormingEdge, Node blossom) {
        // going through every vertex in the blossom and moving its child list to
        // blossom child list
        Node varNode;
        Tree tree = blossomRoot.tree;
        double eps = tree.eps;
        // handle all blossom nodes except for the blossom root
        for (State.BlossomNodesIterator iterator = state.blossomNodesIterator(blossomRoot, blossomFormingEdge); iterator.hasNext(); ) {
            varNode = iterator.next();
            varNode.blossomEps = eps;
            if (varNode != blossomRoot) {
                if (varNode.isPlusNode()) {
                    // updating tree parents of the tree nodes that don't belong to the blossom
                    for (Node child = varNode.firstTreeChild; child != null; child = child.treeSiblingNext) {
                        if (!child.isMarked) {
                            child.treeParent = blossom;
                        }
                    }
                    // substituting varNode with the blossom in the tree structure
                    shrinkPlusNode(varNode, blossom);
                    varNode.removeFromChildList();
                    varNode.moveChildrenTo(blossom);
                } else {
                    if (varNode.isBlossom) {
                        // TODO: update slack of the edge and dual variable of the blossom
                        tree.removeMinusBlossom(varNode);
                    }
                    shrinkMinusNode(varNode, blossom);
                    varNode.removeFromChildList();
                }
            }
            varNode.blossomGrandparent = varNode.blossomParent = blossom;
        }
        // handle blossom root
        for (Node child = blossomRoot.firstTreeChild; child != null; child = child.treeSiblingNext) {
            if (!child.isMarked) {
                child.treeParent = blossom;
            }
        }
        // substituting varNode with the blossom in the tree structure
        shrinkPlusNode(blossomRoot, blossom);
        blossomRoot.removeFromChildList();
        blossomRoot.moveChildrenTo(blossom);
    }

    private void shrinkPlusNode(Node node, Node blossom) {
        Tree tree = node.tree;
        double eps = tree.eps;
        node.dual += eps;

        Set<Pair<Edge, Integer>> edges = node.getEdges();
        for (Pair<Edge, Integer> pair : edges) {
            Edge edge = pair.getFirst();
            int direction = pair.getSecond();
            Node opposite = edge.head[direction];

            if (!opposite.isMarked) {
                // opposite isn't a node inside the blossom
                state.moveEdge(node, blossom, edge);
                if (opposite.tree == tree) {
                    // edge to the node from the same tree
                } else if (opposite.tree != null) {
                    // cross-tree edge

                }
            } else if (opposite.isPlusNode()) {
                // inner edge, subtract eps only in the case the opposite node is a "+" node
                if (edge.fibNode != null) {
                    // remove this edge when it is encountered for the first time
                    tree.removePlusPlusEdge(edge);
                }
                edge.slack -= eps;
            }
        }

    }

    private void shrinkMinusNode(Node node, Node blossom) {
        Tree tree = node.tree;
        double eps = tree.eps;
        node.dual -= eps;

        Set<Pair<Edge, Integer>> edges = node.getEdges();
        for (Pair<Edge, Integer> pair : edges) {
            Edge edge = pair.getFirst();
            int direction = pair.getSecond();
            Node opposite = edge.head[direction];
            Tree oppositeTree = opposite.tree;

            if (!opposite.isMarked) {
                // opposite isn't a node inside the blossom
                state.moveEdge(node, blossom, edge);
                edge.slack += 2 * eps;
                if (opposite.tree == tree) {
                    // edge to the node from the same tree, need only to add it to "++" heap if opposite is "+" node
                    if (opposite.isPlusNode()) {
                        tree.addPlusPlusEdge(edge, edge.slack);
                    }
                } else {
                    // cross-tree edge or infinity edge
                    if (opposite.isPlusNode()) {
                        oppositeTree.currentEdge.removeFromCurrentMinusPlusHeap(edge, oppositeTree.currentDirection);
                        oppositeTree.currentEdge.addPlusPlusEdge(edge, edge.slack);
                    } else if (opposite.isMinusNode()) {
                        oppositeTree.currentEdge.addToCurrentPlusMinusHeap(edge, edge.slack, oppositeTree.currentDirection);
                    } else {
                        tree.addPlusInfinityEdge(edge, edge.slack);
                    }

                }
            } else if (opposite.isMinusNode()) {
                // this is an inner edge
                edge.slack += eps;
            }
        }
    }

    private void reverseBlossomSiblings(Node blossomNode) {
        Edge prevEdge = blossomNode.blossomSibling;
        Node current = blossomNode;
        Node tmp;
        Edge tmpEdge;
        do {
            current = prevEdge.getOpposite(current);
            tmpEdge = prevEdge;
            prevEdge = current.blossomSibling;
            current.blossomSibling = tmpEdge;
        } while (current != blossomNode);
    }

    private boolean forwardDirection(Node blossomRoot, Node stopNode) {
        int hops = 0;
        Node current = blossomRoot;
        while (current != stopNode) {
            ++hops;
            current = current.blossomSibling.getOpposite(current);
        }
        return (hops & 1) == 0;
    }

    private void freeRemoved() {
        Node[] nodes = state.nodes;
        Node iterNode;
        Node jumpNode;
        for (int i = 0; i < state.nodeNum; i++) {
            iterNode = nodes[i];
            for (jumpNode = iterNode; !jumpNode.isOuter && !jumpNode.isMarked; jumpNode = jumpNode.blossomParent) {
                jumpNode.isMarked = true;
                if (jumpNode.blossomGrandparent.isRemoved) {
                    jumpNode.blossomGrandparent = jumpNode.blossomParent;
                }
            }
        }
        for (int i = 0; i < state.nodeNum; i++) {
            iterNode = nodes[i];
            for (jumpNode = iterNode; !jumpNode.isOuter && jumpNode.isMarked; jumpNode = jumpNode.blossomParent) {
                jumpNode.isMarked = false;
            }
        }
    }
}
