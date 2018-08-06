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

import static org.jgrapht.alg.matching.blossom.v5.BlossomVNode.Label.*;
import static org.jgrapht.alg.matching.blossom.v5.KolmogorovMinimumWeightPerfectMatching.DEBUG;

/**
 * This class is used by {@link KolmogorovMinimumWeightPerfectMatching} for performing primal operations: grow,
 * augment, shrink and expand. This class operates on alternating trees, blossom structures, and node states.
 * It changes them after applying any primal operation. Also, this class can add and subtract some values from
 * nodes' dual variables, it never changes their actual dual variables.
 * <p>
 * The augment operation is used to increase the cardinality of the matching. It is applied to a tight (+, +)
 * cross-tree edge. Its main purpose is alter the matching on the simple path between tree roots through
 * the tight edge, destroy the previous tree structures, update the state of the node, and change the presence
 * of edges in the priority queues. Now this operation doesn't destroy the tree structure, this technique is
 * called <i>lazy tree structure destroying</i>. The information of the nodes from the tree structure block
 * is overwritten when a node is being added to another tree. This operation doesn't change the matching in the
 * contracted blossoms.
 * <p>
 * The grow operation is used to add new nodes to a given tree. This operation is applied only to tight infinity
 * edges. It always adds even number of nodes. This operation can grow the tree recursively in the depth-first order.
 * If it encounters a tight (+, +) cross-tree edge, it stops growing and performs immediate augmentation.
 * <p>
 * The shrink operation contracts an odd node circuit and introduces a new pseudonode. It is applied to tight
 * (+, +) in-tree edges. It changes the state so than the contracted nodes don't appear in the surface graph.
 * If during the changing of the endpoints of boundary edge a tight (+, +) cross-tree edge is encountered, an
 * immediate augmentation is performed.
 * <p>
 * The expand operation brings the contracted in a blossom nodes to the surface graph. It is applied only to a
 * "-" blossom with zero dual variable. The operation determines the two branched of a blossom: an even and an
 * odd one. The first one contains even number of edges and can be empty, the later - odd number of edges an
 * necessarily contains at least one edge. An even branch is inserted into the tree. The state of the algorithm
 * is changes respectively (node duals, tree structure, etc.). If some boundary edge in a tight (+, +) cross-tree
 * edge, an immediate augmentation is performed.
 * <p>
 * The immediate augmentations are used to speed up the algorithm. More detailed description of the primal
 * operations can be found in their Javadoc.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 * @see KolmogorovMinimumWeightPerfectMatching
 * @see BlossomVDualUpdater
 * @since June 2018
 */
class BlossomVPrimalUpdater<V, E> {
    /**
     * Store of the information needed for the algorithm
     */
    private BlossomVState<V, E> state;

    /**
     * Constructs a new instance of BlossomVPrimalUpdater
     *
     * @param state contains the graph and associated information
     */
    public BlossomVPrimalUpdater(BlossomVState<V, E> state) {
        this.state = state;
    }

    /**
     * One of the four primal operations. Is invoked on the plus-infinity {@code growEdge}, which connects
     * a "+" node in the tree and an infinity matched node. The {@code growEdge} and the matched free edge
     * are added to the tree structure. Two new nodes are added to the tree: minus and plus node. Let's call
     * the node incident to the {@code growEdge} and opposite to the minusNode the "tree node".
     * <p>
     * As the result, following actions are performed:
     * <ul>
     * <li>Adding new child to the children of tree node and minus node</li>
     * <li>Setting parent edges of minus and plus nodes</li>
     * <li>If minus node is a blossom, add it to the heap of "-" blossoms</li>
     * <li>Remove growEdge from the heap of infinity edges</li>
     * <li>Remove former infinity edges and add new (+, +) in-tree and cross-tree edges, (+, -) cross tree edges
     * to the appropriate heaps (due to the changes of the labels of the minus and plus nodes)</li>
     * <li>Add new infinity edge from the plus node</li>
     * <li>Add new tree edges is necessary</li>
     * <li>Subtract tree.eps from the slacks of all edges incident to the minus node</li>
     * <li>Add tree.eps to the slacks of all edges incident to the plus node</li>
     * </ul>
     * <p>
     * If the {@code manyGrows} flag is true, performs recursive growing of the tree.
     *
     * @param growEdge         the tight edge between node in the tree and minus node
     * @param recursiveGrow    specifies whether to perform recursive growing
     * @param immediateAugment a flag that indicates whether to perform immediate augmentation if a
     *                         tight (+, +) cross-tree edge is encountered
     */
    public void grow(BlossomVEdge growEdge, boolean recursiveGrow, boolean immediateAugment) {
        if (DEBUG) {
            System.out.println("Growing edge " + growEdge);
        }
        int initialTreeNum = state.treeNum;
        long start = System.nanoTime();
        int dirToMinusNode = growEdge.head[0].isInfinityNode() ? 0 : 1;

        BlossomVNode nodeInTheTree = growEdge.head[1 - dirToMinusNode];
        BlossomVNode minusNode = growEdge.head[dirToMinusNode];
        BlossomVNode plusNode = minusNode.getOppositeMatched();

        nodeInTheTree.addChild(minusNode, growEdge, true);
        minusNode.addChild(plusNode, minusNode.matched, true);

        BlossomVNode stop = plusNode;

        while (true) {
            minusNode.label = MINUS;
            plusNode.label = PLUS;
            minusNode.isMarked = plusNode.isMarked = false;
            processMinusNodeGrow(minusNode);
            processPlusNodeGrow(plusNode, recursiveGrow, immediateAugment);
            if (initialTreeNum != state.treeNum) {
                break;
            }

            if (plusNode.firstTreeChild != null) {
                minusNode = plusNode.firstTreeChild;
                plusNode = minusNode.getOppositeMatched();
            } else {
                while (plusNode != stop && plusNode.treeSiblingNext == null) {
                    plusNode = plusNode.getTreeParent();
                }
                if (plusNode.isMinusNode()) {
                    minusNode = plusNode.treeSiblingNext;
                    plusNode = minusNode.getOppositeMatched();
                } else {
                    break;
                }
            }
        }
        state.statistics.growTime += System.nanoTime() - start;
    }

    /**
     * One of the four primal operations. Is invoked on a tight (+, +) cross-tree edge.
     * Increases the matching by 1. Converts the trees on both sides into the set of
     * free matched edges. Applies lazy delta spreading.
     * <p>
     * For each tree the following actions are performed:
     * <ul>
     * <li>Labels of all nodes change to INFINITY</li>
     * <li>tree.eps is subtracted from "-" nodes' duals and added to the "+" nodes' duals</li>
     * <li>tree.eps is subtracted from all edges incident to "+" nodes and added to all edges incident to "-" nodes.
     * Consecutively, the slacks of the (+, -) in-tree edges stay unchanged</li>
     * <li>Former (-, +) and (+, +) are substituted with the (+, inf) edges (removed and added to appropriate heaps).</li>
     * <li>The cardinality of the matching is increased by 1</li>
     * <li>Tree structure references are set to null</li>
     * <li>Tree roots are removed from the linked list of tree roots</li>
     * </ul>
     * <p>
     * These actions change only the surface graph. They don't change the nodes and edges in the pseudonodes.
     *
     * @param augmentEdge the edge to augment
     */
    public void augment(BlossomVEdge augmentEdge) {
        long start = System.nanoTime();

        if (DEBUG) {
            System.out.println("Augmenting edge " + augmentEdge);
        }
        BlossomVNode node;
        // augmenting trees on both sides
        for (int dir = 0; dir < 2; dir++) {
            node = augmentEdge.head[dir];
            augmentBranch(node, augmentEdge);
            node.matched = augmentEdge;
        }

        state.statistics.augmentTime += System.nanoTime() - start;
    }

    /**
     * One of the four primal operations. Is invoked on a tight (+, +) in-tree edge.
     * The result of this operation is the substitution of an odd circuit with a single
     * node. This means that we consider the set of nodes of odd cardinality as a single
     * node.
     * <p>
     * In the shrink operation the following main actions are performed:
     * <ul>
     * <li>Lazy dual updates are applied to all inner edges and nodes on the circuit. Thus, the inner
     * edges and nodes in the pseudonodes have valid slacks and dual variables</li>
     * <li>The endpoints of the boundary edges are moved to the new blossom node, which
     * has label {@link BlossomVNode.Label#PLUS}
     * <li>Lazy dual updates are applied to boundary edges and newly created blossom</li>
     * <li>Children of blossom nodes are moved to the blossom, their parent edges are changed respectively</li>
     * <li>The blossomSibling references are set so that they form a circular linked list</li>
     * <li>If the blossom becomes a tree root, it substitutes the previous tree's root in the linked list of tree roots</li>
     * <li>Since the newly created blossom with "+" label can change the classification of edges, their
     * presence in heaps is updated</li>
     * </ul>
     *
     * @param blossomFormingEdge the tight (+, +) in-tree edge
     * @param immediateAugment   a flag that indicates whether to perform immediate augmentation if a
     *                           tight (+, +) cross-tree edge is encountered
     * @return the newly created blossom
     */
    public BlossomVNode shrink(BlossomVEdge blossomFormingEdge, boolean immediateAugment) {
        long start = System.nanoTime();
        if (DEBUG) {
            System.out.println("Shrinking edge " + blossomFormingEdge);
        }
        BlossomVEdge zeroSlackEdge;
        BlossomVNode blossomRoot = findBlossomRoot(blossomFormingEdge);
        BlossomVTree tree = blossomRoot.tree;
        /* We don't actually need position of the blossom node since blossom nodes aren't stored in
         * the state.nodes array. We use blossom's position as its id for debug purposes.
         */
        BlossomVNode blossom = new BlossomVNode(state.nodeNum + state.blossomNum);
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
        for (BlossomVState.BlossomNodesIterator iterator = state.blossomNodesIterator(blossomRoot, blossomFormingEdge); iterator.hasNext(); ) {
            iterator.next().isMarked = true;
        }

        // move edges and children, change slacks if necessary
        zeroSlackEdge = updateTreeStructure(blossomRoot, blossomFormingEdge, blossom);

        // create circular linked list of circuit nodes
        setBlossomSiblings(blossomRoot, blossomFormingEdge);

        // resetting marks of blossom nodes
        blossomRoot.isMarked = false;
        blossomRoot.isProcessed = false;
        for (BlossomVNode current = blossomRoot.blossomSibling.getOpposite(blossomRoot); current != blossomRoot; current = current.blossomSibling.getOpposite(current)) {
            current.isMarked = false;
            current.isProcessed = false;
        }
        blossomRoot.matched = null; // now new blossom is matched (used when finishing the matching

        state.statistics.shrinkNum++;
        state.blossomNum++;

        state.statistics.shrinkTime += System.nanoTime() - start;
        if (zeroSlackEdge != null && immediateAugment) {
            if (DEBUG) {
                System.out.println("Bingo shrink");
            }
            augment(zeroSlackEdge);
        }
        return blossom;
    }

    /**
     * One of the four primal operations. Is invoked on a previously contracted pseudonode.
     * The result of this operation is bringing the nodes in the blossom to the surface graph.
     * An even branch of the blossom is inserted into the tree structure. Endpoints of the edges
     * incident to the blossom are moved one layer down. The slack of the inner and boundary edges
     * are update according to the lazy delta spreading technique.
     * <p>
     * <b>Note: </b> only "-" blossoms can be expanded. At that moment their dual variables are always zero.
     * This is the reason why they don't need to be stored to compute the dual solution.
     * <p>
     * In the expand operation the following actions are performed:
     * <ul>
     * <li>Endpoints of the boundary edges are updated</li>
     * <li>The matching in the blossom is changed. <b>Note:</b> the resulting matching doesn't depend on the
     * previous matching</li>
     * <li>isOuter flags are updated</li>
     * <li>node.tree are updated</li>
     * <li>Tree structure is updated including parent edges and tree children of the nodes on the even branch</li>
     * <li>The endpoints of some edges change their labels to "+" => their slacks are changed according to the
     * lazy delta spreading and their presence in heaps also changes</li>
     * </ul>
     *
     * @param blossom          the blossom to expand
     * @param immediateAugment a flag that indicates whether to perform immediate augmentation if a
     *                         tight (+, +) cross-tree edge is encountered
     */
    public void expand(BlossomVNode blossom, boolean immediateAugment) {
        BlossomVEdge zeroSlackEdge;
        BlossomVEdge edge;
        long start = System.nanoTime();
        if (DEBUG) {
            System.out.println("Expanding blossom " + blossom);
        }
        BlossomVTree tree = blossom.tree;
        double eps = tree.eps;
        blossom.dual -= eps;
        blossom.tree.removeMinusBlossom(blossom);  // it doesn't belong to the tree no more

        BlossomVNode branchesEndpoint = blossom.parentEdge.getCurrentOriginal(blossom).getPenultimateBlossom();

        if (DEBUG) {
            BlossomVState.printBlossomNodes(branchesEndpoint);
        }

        // the node which is matched to the node from outside
        BlossomVNode blossomRoot = blossom.matched.getCurrentOriginal(blossom).getPenultimateBlossom();

        // marking blossom nodes
        BlossomVNode current = blossomRoot;
        do {
            current.isMarked = true;
            current = current.blossomSibling.getOpposite(current);
        } while (current != blossomRoot);

        // moving all edge from blossom to penultimate children
        blossom.removeFromChildList();
        for (BlossomVNode.IncidentEdgeIterator iterator = blossom.incidentEdgesIterator(); iterator.hasNext(); ) {
            edge = iterator.next();
            BlossomVNode penultimateChild = edge.headOriginal[1 - iterator.getDir()].getPenultimateBlossomAndFixBlossomGrandparent();
            state.moveEdgeTail(blossom, penultimateChild, edge);
        }

        // reversing the circular blossomSibling references so that the first branch in even branch
        if (!forwardDirection(blossomRoot, branchesEndpoint)) {
            reverseBlossomSiblings(blossomRoot);
        }

        // changing the matching, the labeling and the dual information on the odd branch
        expandOddBranch(blossomRoot, branchesEndpoint, tree);

        // changing the matching, the labeling and dual information on the even branch
        zeroSlackEdge = expandEvenBranch(blossomRoot, branchesEndpoint, blossom);

        // resetting marks of blossom nodes
        current = blossomRoot;
        do {
            current.isMarked = false;
            current.isProcessed = false;
            current = current.blossomSibling.getOpposite(current);
        } while (current != blossomRoot);
        state.statistics.expandNum++;
        state.removedNum++;
        if (DEBUG) {
            BlossomVState.printTreeNodes(tree);
        }
        state.statistics.expandTime += System.nanoTime() - start;

        if (immediateAugment && zeroSlackEdge != null) {
            if (DEBUG) {
                System.out.println("Bingo expand");
            }
            augment(zeroSlackEdge);
        }

    }

    /**
     * Processes a minus node in the grow operation. Applies lazy delta spreading, adds new (-,+) cross-tree edges,
     * removes former (+, inf) edges.
     *
     * @param minusNode a minus endpoint of the matched edge that is being appended to the tree
     */
    private void processMinusNodeGrow(BlossomVNode minusNode) {
        BlossomVEdge edge;
        double eps = minusNode.tree.eps;
        minusNode.dual += eps;
        // maintaining heap of "-" blossoms
        if (minusNode.isBlossom) {
            minusNode.tree.addMinusBlossom(minusNode);
        }
        // maintaining minus-plus edges in the minus-plus heaps in the tree edges
        for (BlossomVNode.IncidentEdgeIterator iterator = minusNode.incidentEdgesIterator(); iterator.hasNext(); ) {
            edge = iterator.next();
            BlossomVNode opposite = edge.head[iterator.getDir()];
            edge.slack -= eps;
            if (opposite.isPlusNode()) {
                if (opposite.tree != minusNode.tree) {
                    // encountered (-,+) cross-tree edge
                    if (opposite.tree.currentEdge == null) {
                        BlossomVState.addTreeEdge(minusNode.tree, opposite.tree);
                    }
                    opposite.tree.removePlusInfinityEdge(edge);
                    opposite.tree.currentEdge.addToCurrentMinusPlusHeap(edge, opposite.tree.currentDirection);
                } else if (opposite != minusNode.getOppositeMatched()) {
                    // encountered a former (+, inf) edge
                    minusNode.tree.removePlusInfinityEdge(edge);
                }
            }
        }
    }

    /**
     * Processes a plus node during the grow operation. Applies lazy delta spreading, removes
     * former (+, inf) edges, adds new (+, +) in-tree and cross-tree edges, new (+, -) cross-tree
     * edges. When the {@code manyGrows} flag is on, collects the tight (+, inf) edges on grows them
     * as well.
     * <p>
     * <b>Note:</b> the recursive grows must be done ofter the gro operation on the current edge is over.
     * This ensures correct state of the heaps and the edges' slacks.
     *
     * @param node             a plus endpoint of the matched edge that is being appended to the tree
     * @param recursiveGrow    a flag that indicates whether to grow the tree recursively
     * @param immediateAugment a flag that indicates whether to perform immediate augmentation if a
     *                         tight (+, +) cross-tree edge is encountered
     */
    private void processPlusNodeGrow(BlossomVNode node, boolean recursiveGrow, boolean immediateAugment) {
        BlossomVNode minusNode, plusNode;
        BlossomVEdge edge;
        BlossomVEdge zeroSlackEdge = null;
        double eps = node.tree.eps;
        node.dual -= eps;
        for (BlossomVNode.IncidentEdgeIterator iterator = node.incidentEdgesIterator(); iterator.hasNext(); ) {
            edge = iterator.next();
            BlossomVNode opposite = edge.head[iterator.getDir()];
            // maintaining heap of plus-infinity edges
            edge.slack += eps;
            if (opposite.isPlusNode()) {
                // this is a (+,+) edge
                if (opposite.tree == node.tree) {
                    // this is blossom-forming edge
                    node.tree.removePlusInfinityEdge(edge);
                    node.tree.addPlusPlusEdge(edge);
                } else {
                    // this is plus-plus edge to another trees
                    if (opposite.tree.currentEdge == null) {
                        BlossomVState.addTreeEdge(node.tree, opposite.tree);
                    }
                    opposite.tree.removePlusInfinityEdge(edge);
                    opposite.tree.currentEdge.addPlusPlusEdge(edge);
                    if (edge.slack <= node.tree.eps + opposite.tree.eps) {
                        zeroSlackEdge = edge;
                    }
                }
            } else if (opposite.isMinusNode()) {
                // this is a (+,-) edge
                if (opposite.tree != node.tree) {
                    // this is (+,-) edge to another trees
                    if (opposite.tree.currentEdge == null) {
                        BlossomVState.addTreeEdge(node.tree, opposite.tree);
                    }
                    opposite.tree.currentEdge.addToCurrentPlusMinusHeap(edge, opposite.tree.currentDirection);
                }
            } else if (opposite.isInfinityNode()) {
                node.tree.addPlusInfinityEdge(edge);
                // this edge can be grown as well
                // it can be the case when this edge can't be grown because opposite vertex is already added
                // to this tree via some other grow operation
                if (recursiveGrow && edge.slack <= eps && !edge.getOpposite(node).isMarked) {
                    if (DEBUG) {
                        System.out.println("Growing edge " + edge);
                    }
                    minusNode = edge.getOpposite(node);
                    plusNode = minusNode.getOppositeMatched();
                    minusNode.isMarked = plusNode.isMarked = true;
                    node.addChild(minusNode, edge, true);
                    minusNode.addChild(plusNode, minusNode.matched, true);
                }
            }
        }
        if (immediateAugment && zeroSlackEdge != null) {
            if (DEBUG) {
                System.out.println("Bingo grow");
            }
            augment(zeroSlackEdge);
        }
        state.statistics.growNum++;
    }

    /**
     * Helper method for expanding an even branch of the blossom. Here it is assumed that the
     * blossomSiblings are directed in the way that the even branch goes from {@code blossomRoot}
     * to {@code branchesEndpoint}.
     * <p>
     * The method traverses the nodes twice: firstly it changes the tree
     * structure, updates the labeling and flags, adds children, and changes the matching. After that it
     * changes the slacks of the edges according to the lazy delta spreading and their presence in
     * heaps. This operation is done in two step cause the later requires correct labeling of the nodes on
     * the branch.
     * <p>
     * <b>Note:</b> this branch can consist only of one node. In this case {@code blossomRoot} and
     * {@code branchesEndpoint} are the same nodes
     *
     * @param blossomRoot      the node of the blossom which is matched from the outside
     * @param branchesEndpoint the common endpoint of the even and odd branches
     * @param blossom          the node that is being expanded
     * @return a tight (+, +) cross-tree edge if it is encountered, null otherwise
     */
    private BlossomVEdge expandEvenBranch(BlossomVNode blossomRoot, BlossomVNode branchesEndpoint, BlossomVNode blossom) {
        BlossomVEdge edge, zeroSlackEdge = null;
        BlossomVTree tree = blossom.tree;
        blossomRoot.matched = blossom.matched;
        blossomRoot.tree = tree;
        blossomRoot.addChild(blossom.matched.getOpposite(blossomRoot), blossomRoot.matched, false);

        BlossomVNode current = blossomRoot;
        current.label = MINUS;
        current.isOuter = true;
        current.parentEdge = blossom.parentEdge;
        BlossomVEdge prevMatched;
        BlossomVNode prevNode = current;
        // first traversal. It is done from blossomRoot to branchesEndpoint, i.e. from higher
        // layers of the tree to the lower
        while (current != branchesEndpoint) {
            // processing "+" node
            current = current.blossomSibling.getOpposite(current);
            current.label = PLUS;
            current.isOuter = true;
            current.tree = tree;
            current.matched = prevMatched = current.blossomSibling;
            current.addChild(prevNode, prevNode.blossomSibling, false);
            prevNode = current;

            // processing "-" node
            current = current.blossomSibling.getOpposite(current);
            current.label = MINUS;
            current.isOuter = true;
            current.tree = tree;
            current.matched = prevMatched;
            current.addChild(prevNode, prevNode.blossomSibling, false);
            prevNode = current;
        }
        blossom.parentEdge.getOpposite(branchesEndpoint).addChild(branchesEndpoint, blossom.parentEdge, false);

        // second traversal, updating edges' slacks and their presence in heaps
        current = blossomRoot;
        expandMinusNode(current);
        while (current != branchesEndpoint) {
            current = current.blossomSibling.getOpposite(current);
            edge = expandPlusNode(current);
            if (edge != null) {
                zeroSlackEdge = edge;
            }
            current.isProcessed = true; // this is needed for correct processing of (+, +) edges connecting two node on the branch

            current = current.blossomSibling.getOpposite(current);
            expandMinusNode(current);
        }
        return zeroSlackEdge;
    }

    /**
     * Helper method for expanding the nodes on an odd branch. Here it is assumed that the
     * blossomSiblings are directed in the way the odd branch goes from {@code branchesEndpoint}
     * to {@code blossomRoot}.
     * <p>
     * The method traverses the nodes only once setting the labels,
     * flags, updating the matching, removing former (+, -) edges and creating new (+, inf)
     * edges in the corresponding heaps. The method doesn't process the {@code blossomRoot}
     * and {@code branchesEndpoint} as they belong to the even branch.
     *
     * @param blossomRoot      the node that is matched from the outside
     * @param branchesEndpoint the common node of the even and odd branches
     * @param tree             the tree the blossom was previously in
     */
    private void expandOddBranch(BlossomVNode blossomRoot, BlossomVNode branchesEndpoint, BlossomVTree tree) {
        BlossomVNode current = branchesEndpoint.blossomSibling.getOpposite(branchesEndpoint);
        BlossomVEdge prevMatched;
        // the traversal if done from branchesEndpoint to blossomRoot, i.e. from
        // lower layers to higher
        while (current != blossomRoot) {
            current.label = BlossomVNode.Label.INFINITY;
            current.isOuter = true;
            current.tree = null;
            current.matched = prevMatched = current.blossomSibling;
            expandInfinityNode(current, tree);
            current = current.blossomSibling.getOpposite(current);

            current.label = BlossomVNode.Label.INFINITY;
            current.isOuter = true;
            current.tree = null;
            current.matched = prevMatched;
            expandInfinityNode(current, tree);
            current = current.blossomSibling.getOpposite(current);
        }
    }

    /**
     * Helper method for changing dual information of the {@code plusNode} and edge incident to it.
     * This method relies on the labeling produced by the first traversal of the
     * {@link BlossomVPrimalUpdater#expandEvenBranch(BlossomVNode, BlossomVNode, BlossomVNode)} and on the isProcessed flags of the
     * nodes on the even branch that have been traversed already. It also assumes that all blossom nodes
     * are marked.
     * <p>
     * Since one of endpoints of the edges previously incident to the blossom changes its label,
     * we have to update the slacks of the boundary edges incindent to the {@code plusNode}.
     *
     * @param plusNode the "+" node from the even branch
     * @return a tight (+, +) cross-tree edge if it is encountered, null otherwise
     */
    private BlossomVEdge expandPlusNode(BlossomVNode plusNode) {
        BlossomVEdge zeroSlackEdge = null;
        double eps = plusNode.tree.eps; // the plusNode.tree is assumed to be correct
        plusNode.dual -= eps; // applying lazy delta spreading
        BlossomVEdge edge;
        for (BlossomVNode.IncidentEdgeIterator iterator = plusNode.incidentEdgesIterator(); iterator.hasNext(); ) {
            edge = iterator.next();
            BlossomVNode opposite = edge.head[iterator.getDir()];
            // update slack of the edge
            if (opposite.isMarked && opposite.isPlusNode()) {
                // this is an inner (+, +) edge
                if (!opposite.isProcessed) {
                    // we encounter this edge for the first time
                    edge.slack += 2 * eps;
                }
            } else if (!opposite.isMarked) {
                // this is boundary edge
                edge.slack += 2 * eps; // the endpoint changes its label to "+"
            } else if (!opposite.isMinusNode()) {
                // this edge is inner edge between even and odd branches or it is an inner (+, +) edge
                edge.slack += eps;
            }
            // update its presence in the heap of edges
            if (opposite.isPlusNode()) {
                if (opposite.tree == plusNode.tree) {
                    // this edge becomes a (+, +) in-tree edge
                    if (!opposite.isProcessed) {
                        // if opposite.isProcessed = true => this is an inner (+, +) edge => its slack has been
                        // updated already and it has been added to the plus-plus edges heap already
                        plusNode.tree.addPlusPlusEdge(edge);
                    }
                } else {
                    // opposite is from another tree since it's label is "+"
                    opposite.tree.currentEdge.removeFromCurrentMinusPlusHeap(edge);
                    opposite.tree.currentEdge.addPlusPlusEdge(edge);
                    if (edge.slack <= eps + opposite.tree.eps) {
                        zeroSlackEdge = edge;
                    }
                }
            } else if (opposite.isMinusNode()) {
                if (opposite.tree != plusNode.tree) {
                    // this edge becomes a (+, -) cross-tree edge
                    if (opposite.tree.currentEdge == null) {
                        BlossomVState.addTreeEdge(plusNode.tree, opposite.tree);
                    }
                    opposite.tree.currentEdge.addToCurrentPlusMinusHeap(edge, opposite.tree.currentDirection);
                }
            } else {
                // this is either an inner edge, that becomes a (+, inf) edge, or it is a former (-, +) edge,
                // that also becomes a (+, inf) edge
                plusNode.tree.addPlusInfinityEdge(edge); // updating edge's key
            }
        }
        return zeroSlackEdge;
    }

    /**
     * Helper method for expanding a minus node from the odd branch. It changes the slacks of inner
     * (-,-) and (-, inf) edges.
     *
     * @param minusNode a "-" node from the even branch
     */
    private void expandMinusNode(BlossomVNode minusNode) {
        double eps = minusNode.tree.eps; // the minusNode.tree is assumed to be correct
        minusNode.dual += eps;
        if (minusNode.isBlossom) {
            minusNode.tree.addMinusBlossom(minusNode);
        }
        BlossomVEdge edge;
        BlossomVNode opposite;
        for (BlossomVNode.IncidentEdgeIterator iterator = minusNode.incidentEdgesIterator(); iterator.hasNext(); ) {
            edge = iterator.next();
            opposite = edge.head[iterator.getDir()];
            if (opposite.isMarked && !opposite.isPlusNode()) {
                // this is a (-, inf) or (-, -) inner edge
                edge.slack -= eps;
            }
        }
    }

    /**
     * Helper method for expanding an infinity node from the odd branch
     *
     * @param infinityNode a node from the odd branch
     * @param tree         the tree the blossom was previously in
     */
    private void expandInfinityNode(BlossomVNode infinityNode, BlossomVTree tree) {
        double eps = tree.eps;
        BlossomVEdge edge;
        for (BlossomVNode.IncidentEdgeIterator iterator = infinityNode.incidentEdgesIterator(); iterator.hasNext(); ) {
            edge = iterator.next();
            BlossomVNode opposite = edge.head[iterator.getDir()];
            if (!opposite.isMarked) {
                edge.slack += eps; // since edge's label changes to inf and this is a boundary edge
                if (opposite.isPlusNode()) {
                    // if this node is marked => it's a blossom node => this edge has been processed already
                    if (opposite.tree != tree) {
                        opposite.tree.currentEdge.removeFromCurrentMinusPlusHeap(edge);
                    }
                    opposite.tree.addPlusInfinityEdge(edge);
                }
            }
        }
    }

    /**
     * Method for converting a tree into a set of free matched edges. It changes the matching starting
     * from {@code firstNode} all the way up to the firstNode.tree.root. It changes the labeling of the nodes,
     * applies lazy delta spreading, updates edges' presence in the heaps. This method also deletes unnecessary
     * tree edges.
     * <p>
     * This method doesn't change the nodes and edge contracted in the blossoms.
     *
     * @param firstNode   an endpoint of the {@code augmentEdge} which belongs to the tree to augment
     * @param augmentEdge a tight (+, +) cross tree edge
     */
    private void augmentBranch(BlossomVNode firstNode, BlossomVEdge augmentEdge) {
        BlossomVTree tree = firstNode.tree;
        double eps = tree.eps;
        BlossomVNode root = tree.root;
        BlossomVTreeEdge treeEdge;
        BlossomVEdge edge;
        BlossomVNode node;
        int dir;

        // setting currentEdge and currentDirection of all opposite trees connected via treeEdge
        state.setCurrentEdges(tree);

        // applying tree.eps to all tree nodes and updating slacks of all incident edges
        for (BlossomVTree.TreeNodeIterator treeNodeIterator = tree.treeNodeIterator(); treeNodeIterator.hasNext(); ) {
            node = treeNodeIterator.next();
            if (!node.isMarked) {
                // applying lazy delta spreading
                if (node.isPlusNode()) {
                    node.dual += eps;
                } else {
                    node.dual -= eps;
                }
                for (BlossomVNode.IncidentEdgeIterator incidentEdgeIterator = node.incidentEdgesIterator(); incidentEdgeIterator.hasNext(); ) {
                    edge = incidentEdgeIterator.next();
                    dir = incidentEdgeIterator.getDir();
                    BlossomVNode opposite = edge.head[dir];
                    BlossomVTree oppositeTree = opposite.tree;
                    if (node.isPlusNode()) {
                        edge.slack -= eps;
                        if (oppositeTree != null && oppositeTree != tree) {
                            // if this edge is a cross-tree edge
                            treeEdge = oppositeTree.currentEdge;
                            if (opposite.isPlusNode()) {
                                // this is a (+,+) cross-tree edge
                                treeEdge.removeFromPlusPlusHeap(edge);
                                oppositeTree.addPlusInfinityEdge(edge);
                            } else if (opposite.isMinusNode()) {
                                // this is a (+,-) cross-tree edge
                                treeEdge.removeFromCurrentPlusMinusHeap(edge);
                            }
                        }
                    } else {
                        // current node is a "-" node
                        edge.slack += eps;
                        if (oppositeTree != null && oppositeTree != tree && opposite.isPlusNode()) {
                            // this is a (-,+) cross-tree edge
                            treeEdge = oppositeTree.currentEdge;
                            treeEdge.removeFromCurrentMinusPlusHeap(edge);
                            oppositeTree.addPlusInfinityEdge(edge);
                        }


                    }
                }
                node.label = INFINITY;
            } else {
                // this node was added to the tree by the grow operation,
                // but it hasn't been processed, so we don't need to process it here
                node.isMarked = false;
            }
        }

        // adding all elements from the (-,+) and (+,+) heaps to (+, inf) heaps of the opposite trees and
        // deleting tree edges
        for (BlossomVTree.TreeEdgeIterator treeEdgeIterator = tree.treeEdgeIterator(); treeEdgeIterator.hasNext(); ) {
            treeEdge = treeEdgeIterator.next();
            dir = treeEdgeIterator.getCurrentDirection();
            BlossomVTree opposite = treeEdge.head[dir];
            opposite.currentEdge = null;

            opposite.plusPlusEdges.meld(treeEdge.plusPlusEdges);
            opposite.plusPlusEdges.meld(treeEdge.getCurrentMinusPlusHeap(dir));
            treeEdge.removeFromTreeEdgeList();
        }

        // updating matching
        BlossomVEdge matchedEdge = augmentEdge;
        BlossomVNode plusNode = firstNode;
        BlossomVNode minusNode = plusNode.getTreeParent();
        while (minusNode != null) {
            plusNode.matched = matchedEdge;
            matchedEdge = minusNode.parentEdge;
            minusNode.matched = matchedEdge;
            plusNode = minusNode.getTreeParent();
            minusNode = plusNode.getTreeParent();
        }
        root.matched = matchedEdge;

        // removing root from the linked list of roots;
        root.removeFromChildList();
        root.isTreeRoot = false;

        state.treeNum--;
    }

    /**
     * Helper method for updating the tree structure in the shrink operation. Move the endpoints of the boundary edges
     * to the {@code blossom}, moves the children of the nodes on the circuit to the blossom, updates edges's slacks
     * and presence in heaps accordingly.
     *
     * @param blossomRoot        the node that is matched from the outside or is a tree root
     * @param blossomFormingEdge a tight (+, +) edge
     * @param blossom            the node that is being inserted into the tree structure
     * @return a tight (+, +) cross-tree edge if it is encountered, null otherwise
     */
    private BlossomVEdge updateTreeStructure(BlossomVNode blossomRoot, BlossomVEdge blossomFormingEdge, BlossomVNode blossom) {
        BlossomVNode varNode;
        BlossomVEdge zeroSlackEdge = null;
        BlossomVEdge edge;
        BlossomVTree tree = blossomRoot.tree;
        // going through every vertex in the blossom and moving its child list to
        // blossom child list
        // handling all blossom nodes except for the blossom root
        // the reason is we can't move root's correctly to the blossom
        // until both children from the circuit are removed from the its children list
        for (BlossomVState.BlossomNodesIterator iterator = state.blossomNodesIterator(blossomRoot, blossomFormingEdge); iterator.hasNext(); ) {
            varNode = iterator.next();
            if (varNode != blossomRoot) {
                if (varNode.isPlusNode()) {
                    // substituting varNode with the blossom in the tree structure
                    varNode.removeFromChildList();
                    varNode.moveChildrenTo(blossom);
                    edge = shrinkPlusNode(varNode, blossom);
                    if (edge != null) {
                        zeroSlackEdge = edge;
                    }
                    varNode.isProcessed = true;
                } else {
                    if (varNode.isBlossom) {
                        tree.removeMinusBlossom(varNode);
                    }
                    varNode.removeFromChildList(); // minus node have only one child and this child belongs to the circuit
                    shrinkMinusNode(varNode, blossom);
                }
            }
            varNode.blossomGrandparent = varNode.blossomParent = blossom;
        }
        // substituting varNode with the blossom in the tree structure
        blossomRoot.removeFromChildList();
        if (!blossomRoot.isTreeRoot) {
            blossomRoot.getTreeParent().addChild(blossom, blossomRoot.parentEdge, false);
        } else {
            // substituting blossomRoot with blossom in the linked list of tree roots
            blossom.treeSiblingNext = blossomRoot.treeSiblingNext;
            blossom.treeSiblingPrev = blossomRoot.treeSiblingPrev;
            blossomRoot.treeSiblingPrev.treeSiblingNext = blossom;
            if (blossomRoot.treeSiblingNext != null) {
                blossomRoot.treeSiblingNext.treeSiblingPrev = blossom;
            }
        }
        // finally process blossomRoot
        blossomRoot.moveChildrenTo(blossom);
        edge = shrinkPlusNode(blossomRoot, blossom);
        if (edge != null) {
            zeroSlackEdge = edge;
        }
        blossomRoot.isTreeRoot = false;

        return zeroSlackEdge;
    }

    /**
     * Processes a plus node on an odd circuit in the shrink operation. Moves
     * endpoints of the boundary edges, updates slacks of incident edges.
     *
     * @param plusNode a plus node from an odd circuit
     * @param blossom  a newly created pseudonode
     * @return a tight (+, +) cross-tree edge if it is encountered, null otherwise
     */
    private BlossomVEdge shrinkPlusNode(BlossomVNode plusNode, BlossomVNode blossom) {
        BlossomVEdge edge;
        BlossomVEdge zeroSlackEdge = null;
        BlossomVNode opposite;
        BlossomVTree tree = plusNode.tree;
        double eps = tree.eps;
        plusNode.dual += eps;

        for (BlossomVNode.IncidentEdgeIterator iterator = plusNode.incidentEdgesIterator(); iterator.hasNext(); ) {
            edge = iterator.next();
            opposite = edge.head[iterator.getDir()];

            if (!opposite.isMarked) {
                // opposite isn't a node inside the blossom
                state.moveEdgeTail(plusNode, blossom, edge);
                if (opposite.tree != tree && opposite.isPlusNode() && edge.slack <= eps + opposite.tree.eps) {
                    zeroSlackEdge = edge;
                }
            } else if (opposite.isPlusNode()) {
                // inner edge, subtract eps only in the case the opposite node is a "+" node
                if (!opposite.isProcessed) { // here we rely on the proper setting of the isProcessed flag
                    // remove this edge when it is encountered for the first time
                    tree.removePlusPlusEdge(edge);
                }
                edge.slack -= eps;
            }
        }
        return zeroSlackEdge;
    }

    /**
     * Processes a minus node from an odd circuit in the shrink operation. Moves
     * the endpoints of the boundary edges, updates their slacks
     *
     * @param minusNode a minus node from an odd circuit
     * @param blossom   a newly create pseudonode
     */
    private void shrinkMinusNode(BlossomVNode minusNode, BlossomVNode blossom) {
        BlossomVEdge edge;
        BlossomVNode opposite;
        BlossomVTree oppositeTree;
        BlossomVTree tree = minusNode.tree;
        double eps = tree.eps;
        minusNode.dual -= eps;

        for (BlossomVNode.IncidentEdgeIterator iterator = minusNode.incidentEdgesIterator(); iterator.hasNext(); ) {
            edge = iterator.next();
            opposite = edge.head[iterator.getDir()];
            oppositeTree = opposite.tree;

            if (!opposite.isMarked) {
                // opposite isn't a node inside the blossom
                state.moveEdgeTail(minusNode, blossom, edge);
                edge.slack += 2 * eps;
                if (opposite.tree == tree) {
                    // edge to the node from the same tree, need only to add it to "++" heap if opposite is "+" node
                    if (opposite.isPlusNode()) {
                        tree.addPlusPlusEdge(edge);
                    }
                } else {
                    // cross-tree edge or infinity edge
                    if (opposite.isPlusNode()) {
                        oppositeTree.currentEdge.removeFromCurrentMinusPlusHeap(edge);
                        oppositeTree.currentEdge.addPlusPlusEdge(edge);
                    } else if (opposite.isMinusNode()) {
                        if (oppositeTree.currentEdge == null) {
                            BlossomVState.addTreeEdge(tree, oppositeTree);
                        }
                        oppositeTree.currentEdge.addToCurrentPlusMinusHeap(edge, oppositeTree.currentDirection);
                    } else {
                        tree.addPlusInfinityEdge(edge);
                    }

                }
            } else if (opposite.isMinusNode()) {
                // this is an inner edge
                edge.slack += eps;
            }
        }
    }

    /**
     * Creates a circular linked list of blossom nodes.
     * <p>
     * <b>BlossomVNode:</b> this method heavily relies on the property of the
     * {@link BlossomVState.BlossomNodesIterator} that it returns the blossomRoot
     * while processing the first branch (with direction 0).
     *
     * @param blossomRoot        the common endpoint of two branches
     * @param blossomFormingEdge a tight (+, +) in-tree edge
     */
    private void setBlossomSiblings(BlossomVNode blossomRoot, BlossomVEdge blossomFormingEdge) {
        // setting blossom sibling nodes
        BlossomVEdge prevEdge = blossomFormingEdge;
        for (BlossomVState.BlossomNodesIterator iterator = state.blossomNodesIterator(blossomRoot, blossomFormingEdge); iterator.hasNext(); ) {
            BlossomVNode current = iterator.next();
            if (iterator.getCurrentDirection() == 0) {
                current.blossomSibling = prevEdge;
                prevEdge = current.parentEdge;
            } else {
                current.blossomSibling = current.parentEdge;
            }
        }
    }

    /**
     * Finds a blossom root of the circuit created by the {@code edge}.
     * More precisely, finds an lca of edge.head[0] and edge.head[1].
     *
     * @param blossomFormingEdge a tight (+, +) in-tree edge
     * @return the lca of edge.head[0] and edge.head[1]
     */
    BlossomVNode findBlossomRoot(BlossomVEdge blossomFormingEdge) {
        BlossomVNode root;
        BlossomVNode upperBound;
        BlossomVNode varNode;
        BlossomVNode[] endPoints = new BlossomVNode[2];
        endPoints[0] = blossomFormingEdge.head[0];
        endPoints[1] = blossomFormingEdge.head[1];
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
                    varNode = varNode.getTreeGrandparent();
                }
                root = varNode;
                break;
            }
            endPoints[branch] = endPoints[branch].getTreeGrandparent();
            branch = 1 - branch;
        }
        varNode = root;
        while (varNode != upperBound) {
            varNode = varNode.getTreeGrandparent();
            varNode.isMarked = false;
        }
        clearIsMarkedAndSetIsOuter(root, blossomFormingEdge.head[0]);
        clearIsMarkedAndSetIsOuter(root, blossomFormingEdge.head[1]);

        return root;
    }

    /**
     * Helper method for {@link BlossomVPrimalUpdater#findBlossomRoot(BlossomVEdge)}, traverses the nodes in the tree
     * from {@code start} to {@code root} and sets isMarked and isOuter to false
     */
    private void clearIsMarkedAndSetIsOuter(BlossomVNode root, BlossomVNode start) {
        while (start != root) {
            start.isMarked = false;
            start.isOuter = false;
            start = start.getTreeParent();
            start.isOuter = false;
            start = start.getTreeParent();
        }
        root.isOuter = false;
        root.isMarked = false;
    }

    /**
     * Auxiliary method for reversing the direction of blossomSibling references
     *
     * @param blossomNode some node on an off circuit
     */
    private void reverseBlossomSiblings(BlossomVNode blossomNode) {
        BlossomVEdge prevEdge = blossomNode.blossomSibling;
        BlossomVNode current = blossomNode;
        BlossomVEdge tmpEdge;
        do {
            current = prevEdge.getOpposite(current);
            tmpEdge = prevEdge;
            prevEdge = current.blossomSibling;
            current.blossomSibling = tmpEdge;
        } while (current != blossomNode);
    }

    /**
     * Checks whether the direction of blossomSibling references is suitable for the expand
     * operation, i.e. an even branch goes from {@code blossomRoot} to {@code branchesEndpoint}.
     *
     * @param blossomRoot      a node on an odd circuit that is matched from the outside
     * @param branchesEndpoint a node common to both branches
     * @return true if the condition described above holds, false otherwise
     */
    private boolean forwardDirection(BlossomVNode blossomRoot, BlossomVNode branchesEndpoint) {
        int hops = 0;
        BlossomVNode current = blossomRoot;
        while (current != branchesEndpoint) {
            ++hops;
            current = current.blossomSibling.getOpposite(current);
        }
        return (hops & 1) == 0;
    }
}
