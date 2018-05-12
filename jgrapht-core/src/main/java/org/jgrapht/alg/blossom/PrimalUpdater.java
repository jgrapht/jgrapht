package org.jgrapht.alg.blossom;

import org.jgrapht.alg.util.Pair;
import org.jgrapht.util.FibonacciHeap;

import java.util.Set;

class PrimalUpdater<V, E> {
    private State<V, E> state;

    public PrimalUpdater(State<V, E> state) {
        this.state = state;
    }

    void grow(Edge edge, boolean manyGrows) {
        int dirToMinusNode = edge.head[0].isInftyNode() ? 0 : 1;
        Node nodeInTheTree = edge.head[1 - dirToMinusNode];
        nodeInTheTree.tree.removePlusInfinityEdge(edge);
        recursiveGrow(edge, manyGrows);
    }

    void recursiveGrow(Edge edge, boolean manyGrows) {
        int dirToMinusNode = edge.head[0].isInftyNode() ? 0 : 1;
        Node nodeInTheTree = edge.head[1 - dirToMinusNode];

        Node minusNode = edge.head[dirToMinusNode];
        Node plusNode = minusNode.matched.getOpposite(minusNode);

        minusNode.setLabel(Node.Label.MINUS);
        plusNode.setLabel(Node.Label.PLUS);

        minusNode.treeParent = nodeInTheTree;
        plusNode.treeParent = minusNode;
        minusNode.parentEdge = edge;
        plusNode.parentEdge = plusNode.matched;

        nodeInTheTree.addChild(minusNode);
        minusNode.addChild(plusNode);

        handleMinusNode(minusNode);
        handlePlusNode(plusNode, manyGrows);

    }

    void handleMinusNode(Node minusNode) {
        // maintaining heap of "-" blossoms
        if (minusNode.isBlossom) {
            minusNode.tree.addMinusBlossom(minusNode, minusNode.dual);
        }
        // maintaining minus-plus edges in the minus-plus heaps in the tree edges
        minusNode.forAllEdges((edge, dir) -> {
            Node opposite = edge.head[dir];
            if (opposite.isPlusNode() && opposite.tree != minusNode.tree) {
                if (opposite.tree.currentEdge == null) {
                    State.addTreeEdge(minusNode.tree, opposite.tree);
                }
                opposite.tree.removePlusInfinityEdge(edge);
                opposite.tree.currentEdge.addToCurrentMinusPlusHeap(edge, edge.slack, opposite.tree.currentDirection);
            }

        });
    }

    void handlePlusNode(Node plusNode, boolean manyGrows) {
        plusNode.forAllEdges((edge, dir) -> {
            Node opposite = edge.head[dir];
            // maintaining heap of plus-infinity edges
            if (opposite.isInftyNode()) {
                if (edge.slack > 0 || !manyGrows) {
                    plusNode.tree.addInfinityEdge(edge, edge.slack);
                } else {
                    recursiveGrow(edge, manyGrows);
                }

            } else {
                if (opposite.isPlusNode()) {
                    // this is a (+,+) edge
                    if (opposite.tree == plusNode.tree) {
                        // this is blossom-forming edge
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

    void clearIsMarkedAndSetIsOuter(Node root, Node start) {
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

    Node shrink(Edge blossomFormingEdge) {
        Node root = findBlossomRoot(blossomFormingEdge);
        Tree tree = root.tree;
        Node blossom = new Node();
        // initializing blossom
        blossom.isBlossom = true;
        blossom.isTreeRoot = root.isTreeRoot;
        blossom.dual -= tree.eps;
        if (blossom.isTreeRoot) {
            tree.root = blossom;
        } else {
            blossom.matched = root.matched;
        }

        // mark all blossom nodes
        for (State.BlossomNodesIterator iterator = state.blossomNodesIterator(root, blossomFormingEdge); iterator.hasNext(); ) {
            iterator.next().isMarked = true;
        }

        updateTreeStructure(root, blossomFormingEdge, blossom);

        substituteRootWithBlossom(root, blossom);

        setBlossomSiblings(root, blossomFormingEdge);

        return blossom;
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

    private void setBlossomSiblings(Node root, Edge blossomFormingEdge) {
        // setting blossom sibling nodes
        Node prevNode = blossomFormingEdge.head[1];
        for (State.BlossomNodesIterator iterator = state.blossomNodesIterator(root, blossomFormingEdge); iterator.hasNext(); ) {
            Node current = iterator.next();
            if (iterator.getCurrentDirection() == 0) {
                current.blossomSibling = prevNode;
                prevNode = current;
            } else {
                current.blossomSibling = current.treeParent;
            }
        }
    }

    private void updateTreeStructure(Node root, Edge blossomFormingEdge, Node blossom) {
        // going through every vertex in the blossom and moving its child list to
        // blossom child list
        Node varNode;
        Tree tree = root.tree;
        double eps = tree.eps;
        for (State.BlossomNodesIterator iterator = state.blossomNodesIterator(root, blossomFormingEdge); iterator.hasNext(); ) {
            varNode = iterator.next();
            varNode.blossomEps = eps;
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
            }
            varNode.blossomGrandparent = varNode.blossomParent = blossom;
        }
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
                state.moveEdge(node, blossom, edge, direction);
                if (opposite.tree == tree) {
                    // edge to the node from the same tree
                } else if (opposite.tree != null) {
                    // cross-tree edge

                }
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
                state.moveEdge(node, blossom, edge, direction);
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

    void expand(Node pseudonode) {

    }

    void augment(Edge augmentEdge) {
        Node node;
        for (int dir = 0; dir < 2; dir++) {
            node = augmentEdge.head[dir];
            augmentBranch(node);
            node.matched = augmentEdge;
        }

    }

    void augmentBranch(Node firstNode) {
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
        });

        // since this tree is
        tree.forEachTreeEdge(((treeEdge, dir) -> {
            Tree opposite = treeEdge.head[dir];
            opposite.currentEdge = null;

            opposite.plusInfinityEdges = FibonacciHeap.union(opposite.plusInfinityEdges, treeEdge.plusPlusEdges);
            opposite.plusInfinityEdges = FibonacciHeap.union(opposite.plusInfinityEdges, treeEdge.getCurrentMinusPlusHeap(dir));
            treeEdge.removeFromTreeEdgeList();
        }));

        // updating labels and dual variables by applying trees.eps to each node
        // according to its label and changing its label to infinity
        for (Tree.TreeNodeIterator iterator = tree.treeNodeIterator(); iterator.hasNext(); ) {
            Node current = iterator.next();
            if (current.isPlusNode()) {
                current.dual += eps;
            } else {
                current.dual -= eps;
            }
            current.setLabel(Node.Label.INFTY);
            current.tree = null;
        }

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
}
