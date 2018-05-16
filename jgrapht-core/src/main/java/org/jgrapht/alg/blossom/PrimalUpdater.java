package org.jgrapht.alg.blossom;

import org.jgrapht.alg.util.Pair;
import org.jgrapht.util.FibonacciHeap;

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
        Node node;
        for (int dir = 0; dir < 2; dir++) {
            node = augmentEdge.head[dir];
            augmentBranch(node);
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
        blossom.isTreeRoot = blossomRoot.isTreeRoot;
        blossom.dual = tree.eps;
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
        Tree tree = blossom.tree;
        double eps = tree.eps;

        Edge parentEdge = blossom.parentEdge;
        int dirToBlossomFromParent = parentEdge.getDirTo(blossom);  // (parentEdge + dirToBlossom) points to blossom
        Node branchesEndpoint = parentEdge.headOriginal[dirToBlossomFromParent].getPenultimateBlossom();
        //state.moveEdge(blossom, branchesEndpoint, parentEdge); // moving (blossom, blossomParent) edge

        Edge matchedEdge = blossom.matched;
        int dirToBlossomFromMatched = matchedEdge.getDirTo(blossom);
        Node blossomRoot = matchedEdge.headOriginal[dirToBlossomFromMatched];
        //state.moveEdge(blossom, blossomRoot, matchedEdge);

        // moving all edge from blossom to penultimate children
        Set<Pair<Edge, Integer>> edges = blossom.getEdges();
        for (Pair<Edge, Integer> pair : edges) {
            Edge edge = pair.getFirst();
            int dir = pair.getSecond();
            Node penultimateChild = edge.headOriginal[1 - dir].getPenultimateBlossom();
            state.moveEdge(blossom, penultimateChild, edge);
        }

        // reversing the circular blossomSibling references so that the first branch in even branch
        if (!forwardDirection(blossomRoot, branchesEndpoint)) {
            reverseBlossomSiblings(blossomRoot);
        }

        // changing the matching and the labeling on the even branch
        handleEvenBranch(blossomRoot, branchesEndpoint, blossom);

        // changing the matching and the labeling on the odd branch
        handleOddBranch(blossomRoot, branchesEndpoint);

    }

    private void handleEvenBranch(Node blossomRoot, Node branchesEndpoint, Node blossom) {
        Tree tree = blossom.tree;
        blossom.matched.getOpposite(blossomRoot).treeParent = blossomRoot;
        blossomRoot.addChild(blossom.matched.getOpposite(blossomRoot));
        blossom.removeFromChildList();
        blossom.treeParent.addChild(branchesEndpoint);

        Node current = blossomRoot;
        current.matched = blossom.matched;
        current.tree = tree;
        current.label = MINUS;
        current.isOuter = true;
        current.treeParent = blossom.treeParent;
        Edge prevMatched;
        Node prevNode = current;
        while (current != branchesEndpoint) {
            current = current.blossomSibling.getOpposite(current);
            current.label = PLUS;
            current.isOuter = true;
            current.tree = tree;
            current.matched = prevMatched = current.blossomSibling;
            current.addChild(prevNode);
            prevNode.treeParent = current;
            prevNode = current;

            current = current.blossomSibling.getOpposite(current);
            current.label = MINUS;
            current.isOuter = true;
            current.tree = tree;
            current.matched = prevMatched;
            current.addChild(prevNode);
            prevNode.treeParent = current;
            prevNode = current;
        }

    }

    private void handleOddBranch(Node blossomRoot, Node branchesEndpoint) {
        Node current = branchesEndpoint.blossomSibling.getOpposite(branchesEndpoint);
        Edge prevMatched;
        while (current != blossomRoot) {
            current.label = INFTY;
            current.isOuter = true;
            current.matched = prevMatched = current.blossomSibling;
            current = current.blossomSibling.getOpposite(current);

            current.label = INFTY;
            current.isOuter = true;
            current.matched = prevMatched;
            current = current.blossomSibling.getOpposite(current);
        }
    }

    private void recursiveGrow(Edge edge, boolean manyGrows) {
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


        handleMinusNode(minusNode);
        handlePlusNode(plusNode, manyGrows);

    }

    private void handleMinusNode(Node minusNode) {
        // maintaining heap of "-" blossoms
        double eps = minusNode.tree.eps;
        minusNode.dual += eps;
        if (minusNode.isBlossom) {
            minusNode.tree.addMinusBlossom(minusNode, minusNode.dual);
        }
        // maintaining minus-plus edges in the minus-plus heaps in the tree edges
        minusNode.forAllEdges((edge, dir) -> {
            Node opposite = edge.head[dir];
            edge.slack -= eps;
            if (opposite.isPlusNode() && opposite.tree != minusNode.tree) {
                if (opposite.tree.currentEdge == null) {
                    State.addTreeEdge(minusNode.tree, opposite.tree);
                }
                opposite.tree.removePlusInfinityEdge(edge);
                opposite.tree.currentEdge.addToCurrentMinusPlusHeap(edge, edge.slack, opposite.tree.currentDirection);
            }

        });
    }

    private void handlePlusNode(Node plusNode, boolean manyGrows) {
        double eps = plusNode.tree.eps;
        plusNode.dual -= eps;
        plusNode.forAllEdges((edge, dir) -> {
            Node opposite = edge.head[dir];
            // maintaining heap of plus-infinity edges
            edge.slack += eps;
            if (opposite.isInftyNode()) {
                if (edge.slack > eps || !manyGrows) {
                    plusNode.tree.addInfinityEdge(edge, edge.slack);
                } else {
                    recursiveGrow(edge, manyGrows);
                }

            } else {
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
                } else {
                    // this is a (+,-) edge
                    if (opposite.tree != plusNode.tree) {
                        // this is (+,-) edge to another trees
                        if (opposite.tree.currentEdge == null) {
                            State.addTreeEdge(plusNode.tree, opposite.tree);
                        }
                        opposite.tree.currentEdge.addToCurrentPlusMinusHeap(edge, edge.slack, opposite.tree.currentDirection);
                    }
                }
            }
        });
    }

    private void augmentBranch(Node firstNode) {
        Tree tree = firstNode.tree;
        double eps = tree.eps;
        Node root = tree.root;

        // setting currentEdge and currentDirection of all opposite trees connected via treeEdge
        tree.forEachTreeEdge((treeEdge, dir) -> {
            Tree opposite = treeEdge.head[dir];
            opposite.currentEdge = treeEdge;
            opposite.currentDirection = dir;
        });

        // applying tree.eps to all tree nodes and updating slacks of all incident edges
        tree.forEachTreeNode(node -> {
            if (node.isPlusNode()) {
                node.dual += eps;
            } else {
                node.dual -= eps;
            }
            node.forAllEdges((edge, dir) -> {
                Node opposite = edge.head[dir];
                Tree oppositeTree = opposite.tree;
                if (node.isPlusNode()) {
                    // if this edge is a cross-trees edge
                    if (oppositeTree != null && oppositeTree != tree) {
                        TreeEdge treeEdge = oppositeTree.currentEdge;
                        int currentDir = oppositeTree.currentDirection;
                        if (opposite.isPlusNode()) {
                            // this is a (+,+) cross-trees edge
                            treeEdge.removeFromPlusPlusHeap(edge);
                            oppositeTree.addInfinityEdge(edge, edge.slack - eps);
                        } else if (opposite.isMinusNode()) {
                            // this is a (+,-) cross-trees edge
                            treeEdge.removeFromCurrentPlusMinusHeap(edge, currentDir);
                        }
                    }
                    edge.slack -= eps;
                } else {
                    // current node is a "-" node
                    if (oppositeTree != null && oppositeTree != tree && opposite.isPlusNode()) {
                        // this is a (-,+) cross-tree edge
                        TreeEdge treeEdge = oppositeTree.currentEdge;
                        int currentDir = oppositeTree.currentDirection;
                        treeEdge.removeFromCurrentMinusPlusHeap(edge, currentDir);
                        oppositeTree.addInfinityEdge(edge, edge.slack + eps);
                    }
                    edge.slack += eps;

                }
            });
            node.setLabel(Node.Label.INFTY);
            node.tree = null;
        });

        // adding all elements from the (-,+) and (+,+) heaps to (+, inf) heaps of the opposite trees and
        // deleting tree edges
        tree.forEachTreeEdge(((treeEdge, dir) -> {
            Tree opposite = treeEdge.head[dir];
            opposite.currentEdge = null;

            opposite.plusInfinityEdges = FibonacciHeap.union(opposite.plusInfinityEdges, treeEdge.plusPlusEdges);
            opposite.plusInfinityEdges = FibonacciHeap.union(opposite.plusInfinityEdges, treeEdge.getCurrentMinusPlusHeap(dir));
            treeEdge.removeFromTreeEdgeList();
        }));

        // updating matching
        if (!firstNode.isTreeRoot) {
            Node minusNode = firstNode.treeParent;
            Node plusNode = minusNode.treeParent;
            while (minusNode != null) {
                plusNode.matched = minusNode.matched = minusNode.parentEdge;
                plusNode = minusNode.treeParent;
                minusNode = plusNode.treeParent;
            }
        }

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
                        tree.removeMinusBlossom(blossom);
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
                        tree.addInfinityEdge(edge, edge.slack);
                    }

                }
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
}
