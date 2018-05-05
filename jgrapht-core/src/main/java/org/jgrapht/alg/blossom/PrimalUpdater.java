package org.jgrapht.alg.blossom;

class PrimalUpdater<V, E> {
    private State<V, E> state;

    public PrimalUpdater(State<V, E> state) {
        this.state = state;
    }

    void grow(Edge edge) {
        int dir = edge.head[0].isInftyNode() ? 0 : 1;
        Node treeNode = edge.head[1 - dir];
        Node minusNode = edge.head[dir];
        Node plusNode = minusNode.matched.head[0] == minusNode ? minusNode.matched.head[1] : minusNode.matched.head[0];

        minusNode.setLabel(Node.Label.MINUS);
        plusNode.setLabel(Node.Label.PLUS);

        plusNode.parent = minusNode;
        minusNode.parent = treeNode;

        treeNode.addChild(minusNode);
        minusNode.addChild(plusNode);

        handleMinusNode(minusNode);
        handlePlusNode(plusNode);

    }

    void handleMinusNode(Node minusNode) {
        if (minusNode.isBlossom) {
            minusNode.tree.addMinusBlossom(minusNode);
        }
        minusNode.forAllEdges((edge, dir) -> {
            Node opposite = edge.head[dir];
            if (opposite.isPlusNode() && opposite.tree != minusNode.tree) {
                if (opposite.tree.currentEdge == null) {
                    Tree.addTreeEdge(minusNode.tree, opposite.tree);
                }
                opposite.tree.plusInftyEdges.delete(edge.fibNode);
                opposite.tree.currentEdge.addToCurrentMinusPlusHeap(edge, opposite.tree.currentDirection);
            }

        });
    }

    void handlePlusNode(Node plusNode) {
        plusNode.forAllEdges((edge, dir) -> {
            Node opposite = edge.head[dir];
            if (opposite.isInftyNode()) {
                // found infinity edge
                if (edge.slack > 0) {
                    plusNode.tree.addInftyEdge(edge);
                } else {
                    grow(edge);
                }
            } else {
                if (opposite.isPlusNode()) {
                    //found plus-plus edge
                    if (opposite.tree == plusNode.tree) {
                        // this is blossom-forming edge
                        plusNode.tree.addPlusPlusEdge(edge);
                    } else {
                        // this is plus-plus edge to another tree
                        if (opposite.tree.currentEdge == null) {
                            Tree.addTreeEdge(plusNode.tree, opposite.tree);
                        }
                        opposite.tree.plusInftyEdges.delete(edge.fibNode);
                        opposite.tree.currentEdge.addPlusPlusEdge(edge);
                    }
                } else {
                    if (opposite.tree != plusNode.tree) {
                        // this is plus-minus edge to another tree
                        if (opposite.tree.currentEdge == null) {
                            Tree.addTreeEdge(plusNode.tree, opposite.tree);
                        }
                        opposite.tree.currentEdge.addToCurrentPlusMinusHeap(edge, opposite.tree.currentDirection);
                    }
                }
            }
        });
    }

    void shrink(Edge blossomFormingEdge) {

    }

    void expand(Node pseudonode) {
    }

    // not finished
    void augment(Edge augmentEdge) {
        Node node;
        for (int dir = 0; dir < 2; dir++) {
            node = augmentEdge.head[dir];
            augmentBranch(augmentEdge, node);
        }

    }

    // not finished
    void augmentBranch(Edge augmentEdge, Node firstNode) {

        Node currentNode = firstNode;
        Edge prevEdge = augmentEdge;
        while (currentNode != firstNode.tree.root) {

            prevEdge = currentNode.first[0];
            currentNode = currentNode.parent;
        }

        Tree tree = firstNode.tree;
        for (Node node : tree) {
            node.label = Node.Label.INFTY;
            node.forAllEdges((edge, dir) -> {
                Node opposite = edge.head[dir];
                if (opposite.tree != null && opposite.tree != tree) {
                    // found edge from this node to another tree
                    if (opposite.isPlusNode() && node.isPlusNode()) {
                        if (edge.fibNode != null) {
                        }
                    } else {

                    }
                }
            });
        }

        state.treeNum--;
    }
}
