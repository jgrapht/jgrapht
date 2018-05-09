package org.jgrapht.alg.blossom;

import org.jgrapht.util.FibonacciHeap;

class PrimalUpdater<V, E> {
    private State<V, E> state;

    public PrimalUpdater(State<V, E> state) {
        this.state = state;
    }

    void grow(Edge edge){
        int dirToMinusNode = edge.head[0].isInftyNode() ? 0 : 1;
        Node nodeInTheTree = edge.head[1 - dirToMinusNode];
        nodeInTheTree.tree.removePlusInfinityEdge(edge);
        recursiveGrow(edge);
    }

    void recursiveGrow(Edge edge) {
        int dirToMinusNode = edge.head[0].isInftyNode() ? 0 : 1;
        Node nodeInTheTree = edge.head[1 - dirToMinusNode];

        Node minusNode = edge.head[dirToMinusNode];
        Node plusNode = minusNode.matched.getOpposite(minusNode);

        minusNode.setLabel(Node.Label.MINUS);
        plusNode.setLabel(Node.Label.PLUS);

        minusNode.parent = nodeInTheTree;
        plusNode.parent = minusNode;
        minusNode.parentEdge = edge;
        plusNode.parentEdge = plusNode.matched;

        nodeInTheTree.addChild(minusNode);
        minusNode.addChild(plusNode);

        handleMinusNode(minusNode);
        handlePlusNode(plusNode);

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

    void handlePlusNode(Node plusNode) {
        plusNode.forAllEdges((edge, dir) -> {
            Node opposite = edge.head[dir];
            // maintaining heap of plus-infinity edges
            if (opposite.isInftyNode()) {
                if (edge.slack > 0) {
                    plusNode.tree.addInfinityEdge(edge, edge.slack);
                } else {
                    recursiveGrow(edge);
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
                    varNode = varNode.parent.parent;
                }
                root = varNode;
                break;
            }
            endPoints[branch] = endPoints[branch].parent.parent;
        }
        varNode = root;
        while (varNode != upperBound) {
            varNode = varNode.parent.parent;
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
            start = start.parent;
            start.isOuter = false;
            start = start.parent;
        }
        root.isOuter = false;
        root.isMarked = false;
    }

    void moveChildren(Node node, Node blossom) {
        if (node.firstTreeChild != null) {
            if (blossom.firstTreeChild == null) {
                blossom.firstTreeChild = node.firstTreeChild;
            } else {
                Node first = node.firstTreeChild;
                first.treeSiblingPrev.treeSiblingNext = blossom.firstTreeChild;
                first.treeSiblingPrev = blossom.firstTreeChild.treeSiblingPrev;
                blossom.firstTreeChild.treeSiblingPrev = first.treeSiblingPrev;
                blossom.firstTreeChild = first;
            }
        }
    }

    void shrink(Edge blossomFormingEdge) {
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

        // going through every vertex in the blossom and moving its child list to
        // blossom child list
        Node varNode;
        for (State.BlossomNodesIterator iterator = state.blossomNodesIterator(root, blossomFormingEdge); iterator.hasNext(); ) {
            varNode = iterator.next();
            if (varNode.isPlusNode()) {
                varNode.removeFromChildList();
                moveChildren(varNode, blossom);
            } else {
                if (varNode.isBlossom) {
                    // TODO: update slack of the edge and dual variable of the blossom
                    tree.removeMinusBlossom(blossom);
                }
            }
            varNode.blossomGrandparent = varNode.blossomParent = blossom;
        }

        // inserting blossom in the child list of root.parent
        blossom.treeSiblingNext = root.treeSiblingNext;
        blossom.treeSiblingPrev = root.treeSiblingNext;
        if (blossom.treeSiblingPrev.treeSiblingNext == null) {
            // blossom is the first vertex in the child list
            root.parent.firstTreeChild = blossom;
        } else {
            // blossom is not the first vertex in the child list
            blossom.treeSiblingPrev.treeSiblingNext = blossom;
        }
        if (blossom.treeSiblingNext != null) {
            // blossom isn't the last vertex in the child list
            blossom.treeSiblingNext.treeSiblingPrev = blossom;
        } else if (root.parent != null) {
            root.parent.firstTreeChild.treeSiblingPrev = blossom;
        }
        // moving all edges between some nodes and nodes on the blossom to point to the blossom
        for (State.BlossomNodesIterator iterator = state.blossomNodesIterator(root, blossomFormingEdge); iterator.hasNext(); ) {
            varNode = iterator.next();
            for (Node.AdjacentEdgeIterator edgeIterator = varNode.adjacentEdgesIterator(); edgeIterator.hasNext(); ) {
                Edge edge = edgeIterator.next();
                Node opposite = edge.head[edgeIterator.getDir()];
                if (opposite.tree != tree) {
                    // opposite if an infinity node or a node from another trees
                } else if (opposite.blossomParent != blossom) {
                    // opposite is a node from the same trees but doesn't belong to the blossom
                }
                state.moveEdge(varNode, blossom, edge, edgeIterator.getDir());
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
            Node minusNode = firstNode.parent;
            Node plusNode = minusNode.parent;
            while (minusNode != null) {
                plusNode.matched = minusNode.matched = minusNode.parentEdge;
                plusNode = minusNode.parent;
                minusNode = plusNode.parent;
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
