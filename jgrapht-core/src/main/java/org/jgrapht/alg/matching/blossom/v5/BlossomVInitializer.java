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

import org.jgrapht.Graph;
import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;
import org.jheaps.AddressableHeap;
import org.jheaps.tree.PairingHeap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.jgrapht.alg.matching.blossom.v5.BlossomVInitializer.Action.AUGMENT;
import static org.jgrapht.alg.matching.blossom.v5.BlossomVInitializer.Action.NONE;
import static org.jgrapht.alg.matching.blossom.v5.BlossomVInitializer.Action.SHRINK;
import static org.jgrapht.alg.matching.blossom.v5.BlossomVNode.Label.MINUS;
import static org.jgrapht.alg.matching.blossom.v5.BlossomVNode.Label.PLUS;
import static org.jgrapht.alg.matching.blossom.v5.KolmogorovMinimumWeightPerfectMatching.*;
import static org.jgrapht.alg.matching.blossom.v5.KolmogorovMinimumWeightPerfectMatching.INFINITY;

/**
 * Is used to start the Kolmogorov's Blossom V algorithm. Performs initialization of the algorithm's internal
 * data structures and finds an initial matching according to the strategy specified in {@code options}.
 * <p>
 * The initialization process necessarily goes through converting the graph into internal representation, allocating
 * trees for unmatched vertices, and creating auxiliary graph whose nodes correspond to alternating trees. The only
 * part that differs is the strategy to find an initial matching to speed up the main part of the algorithm.
 * <p>
 * The simple initialization (option {@link BlossomVOptions.InitializationType#NONE}) doesn't find any matching
 * and initializes the data structures by allocating $|V|$ single vertex trees. This is the fastest initialization
 * strategy, also it slows the main algorithm down.
 * <p>
 * The greedy initialization runs in two phases. Firstly, for every node it determines an edge of minimum weight
 * an assigns the half of that weight to the node's dual variable. This ensures that the slacks of all edges are
 * non-negative. After that it goes through all nodes again, greedily increases its dual variable and chooses an
 * incident matching edge if it is possible. After that every node is incident to at least one tight edge. The
 * resulting matching is an output of this initialization strategy.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 * @see KolmogorovMinimumWeightPerfectMatching
 * @since June 2018
 */
class BlossomVInitializer<V, E> {
    /**
     * The graph to search matching in
     */
    private final Graph<V, E> graph;
    /**
     * Number of nodes in the graph
     */
    private int nodeNum = 0;
    /**
     * Number of edges in the graph
     */
    private int edgeNum = 0;
    /**
     * An array of nodes that will be passes to the resulting state object
     */
    private BlossomVNode[] nodes;
    /**
     * An array of edges that will be passes to the resulting state object
     */
    private BlossomVEdge[] edges;
    /**
     * Generic vertices of the {@code graph} in the same order as internal nodes in the array {@code nodes}.
     * Since for each node in the {@code nodes} we know its position in the {@code nodes}, we can in constant
     * time determine its generic counterpart.
     */
    private List<V> graphVertices;
    /**
     * Generic edge of the {@code graph} in the same order as internal edges in the array {@code edges}.
     * Since for each edge in the {@code edges} we know its position in the {@code edges}, we can in constant
     * time determine its generic counterpart.
     */
    private List<E> graphEdges;

    /**
     * Creates a new BlossomVInitializer instance
     *
     * @param graph the graph to search matching in
     */
    public BlossomVInitializer(Graph<V, E> graph) {
        this.graph = graph;
        nodeNum = graph.vertexSet().size();
    }

    /**
     * Converts the generic graph representation into the data structure form convenient for the algorithm
     * and initializes the matching according to the strategy specified in {@code options}.
     *
     * @param options the options of the algorithm
     * @return the state object with all necessary for the algorithm information
     */
    public BlossomVState<V, E> initialize(BlossomVOptions options) {
        switch (options.initializationType) {
            case NONE:
                return simpleInitialization(options);
            case GREEDY:
                return greedyInitialization(options);
            case FRACTIONAL:
                return fractionalMatchingInitialization(options);
            default:
                return null;
        }
    }

    /**
     * Performs simple initialization of the matching by allocating $|V|$ trees. The result of
     * this type of initialization is an empty matching. That is why this is the most basic type
     * of initialization.
     *
     * @param options the options of the algorithm
     * @return the state object with all necessary for the algorithm information
     */
    private BlossomVState<V, E> simpleInitialization(BlossomVOptions options) {
        initGraph();
        for (BlossomVNode node : nodes) {
            node.isOuter = true;
        }
        allocateTrees();
        initAuxiliaryGraph();
        return new BlossomVState<>(graph, nodes, edges, nodeNum, edgeNum, nodeNum,graphVertices, graphEdges, options);
    }

    /**
     * Performs greedy initialization of the algorithm. For the description of this initialization strategy
     * see the class description.
     *
     * @param options the options of the algorithm
     * @return the state object with all necessary for the algorithm information
     */
    private BlossomVState<V, E> greedyInitialization(BlossomVOptions options) {
        initGraph();
        int treeNum = initGreedy();
        allocateTrees();
        initAuxiliaryGraph();
        return new BlossomVState<>(graph, nodes, edges, nodeNum, edgeNum, treeNum, graphVertices, graphEdges, options);
    }

    /**
     * Performs fractional matching initialization, {@link BlossomVInitializer#initFractional()} ()} for the description.
     *
     * @param options the options of the algorithm
     * @return the state object with all necessary for the algorithm information
     */
    private BlossomVState<V, E> fractionalMatchingInitialization(BlossomVOptions options) {
        initGraph();
        initGreedy();
        allocateTrees();
        int treeNum = initFractional();
        initAuxiliaryGraph();
        return new BlossomVState<>(graph, nodes, edges, nodeNum, edgeNum, treeNum, graphVertices, graphEdges, options);
    }

    /**
     * Helper method to convert the generic graph representation into the form convenient for the algorithm
     */
    private void initGraph() {
        int expectedEdgeNum = graph.edgeSet().size();
        nodes = new BlossomVNode[nodeNum + 1];
        edges = new BlossomVEdge[expectedEdgeNum];
        graphVertices = new ArrayList<>(nodeNum);
        graphEdges = new ArrayList<>(expectedEdgeNum);
        HashMap<V, BlossomVNode> vertexMap = new HashMap<>(nodeNum);
        int i = 0;
        // mapping nodes
        for (V vertex : graph.vertexSet()) {
            nodes[i] = new BlossomVNode(i);
            graphVertices.add(vertex);
            vertexMap.put(vertex, nodes[i]);
            i++;
        }
        nodes[nodeNum] = new BlossomVNode(nodeNum);  // auxiliary node to keep track of the first item in the linked list of tree roots
        i = 0;
        // mapping edges
        for (E e : graph.edgeSet()) {
            BlossomVNode source = vertexMap.get(graph.getEdgeSource(e));
            BlossomVNode target = vertexMap.get(graph.getEdgeTarget(e));
            if (source != target) { // we avoid self-loops in order to support pseudographs
                edgeNum++;
                BlossomVEdge edge = addEdge(source, target, graph.getEdgeWeight(e), i);
                edges[i] = edge;
                graphEdges.add(e);
                i++;
            }
        }
    }

    /**
     * Adds a new edge between {@code from} and {@code to}. The resulting edge points from {@code from} \
     * to {@code to}
     *
     * @param from  the tail of this edge
     * @param to    the head of this edge
     * @param slack the slack of the resulting edge
     * @param pos   position of the resulting edge in the array {@code edges}
     * @return the newly added edge
     */
    public BlossomVEdge addEdge(BlossomVNode from, BlossomVNode to, double slack, int pos) {
        BlossomVEdge edge = new BlossomVEdge(pos);
        edge.slack = slack;
        edge.headOriginal[0] = to;
        edge.headOriginal[1] = from;
        // the call to the BlossomVNode#addEdge implies setting head[dir] reference
        from.addEdge(edge, 0);
        to.addEdge(edge, 1);
        return edge;
    }

    /**
     * Method for greedy matching initialization.
     * <p>
     * For every node we choose an incident edge of minimum slack and set its dual to the half of this slack.
     * This maintains the nonnegativity of edge slacks. After that we go through all nodes again, greedily
     * increase their dual variables and match them if it is possible.
     *
     * @return the number of unmatched nodes, which equals to the number of trees
     */
    private int initGreedy() {
        int dir;
        BlossomVEdge edge;
        // set all dual variables to infinity
        for (int i = 0; i < nodeNum; i++) {
            nodes[i].dual = INFINITY;
        }
        // set dual variables to the half of the minimum weight of the incident edges
        for (int i = 0; i < edgeNum; i++) {
            edge = edges[i];
            if (edge.head[0].dual > edge.slack) {
                edge.head[0].dual = edge.slack;
            }
            if (edge.head[1].dual > edge.slack) {
                edge.head[1].dual = edge.slack;
            }
        }
        // divide dual variables by to, this ensures nonnegativity of all slacks
        // decrease edge slacks accordingly
        for (int i = 0; i < edgeNum; i++) {
            edge = edges[i];
            BlossomVNode source = edge.head[0];
            BlossomVNode target = edge.head[1];
            if (!source.isOuter) {
                source.isOuter = true;
                source.dual /= 2;
            }
            edge.slack -= source.dual;
            if (!target.isOuter) {
                target.isOuter = true;
                target.dual /= 2;
            }
            edge.slack -= target.dual;
        }
        // go through all vertices, greedily increase their dual variables to the minimum slack of incident edges
        // if there exist a tight unmatched edge in the neighborhood, match it
        int treeNum = nodeNum;
        BlossomVNode node;
        for (int i = 0; i < nodeNum; i++) {
            node = nodes[i];
            if (!node.isInfinityNode()) {
                double minSlack = INFINITY;
                // find the minimum slack of incident edges
                for (BlossomVNode.IncidentEdgeIterator incidentEdgeIterator = node.incidentEdgesIterator(); incidentEdgeIterator.hasNext(); ) {
                    edge = incidentEdgeIterator.next();
                    if (edge.slack < minSlack) {
                        minSlack = edge.slack;
                    }
                }
                node.dual += minSlack;
                double resultMinSlack = minSlack;
                // subtract minimum slack from the slacks of all incident edges
                for (BlossomVNode.IncidentEdgeIterator incidentEdgeIterator = node.incidentEdgesIterator(); incidentEdgeIterator.hasNext(); ) {
                    edge = incidentEdgeIterator.next();
                    dir = incidentEdgeIterator.getDir();
                    if (edge.slack <= resultMinSlack && node.isPlusNode() && edge.head[dir].isPlusNode()) {
                        node.label = BlossomVNode.Label.INFINITY;
                        edge.head[dir].label = BlossomVNode.Label.INFINITY;
                        node.matched = edge;
                        edge.head[dir].matched = edge;
                        treeNum -= 2;
                    }
                    edge.slack -= resultMinSlack;
                }
            }
        }

        return treeNum;
    }

    /**
     * Initializes an auxiliary graph by adding tree edges between trees and adding (+, +) cross-tree edges
     * and (+, inf) edges to the appropriate heaps
     */
    private void initAuxiliaryGraph() {
        BlossomVNode opposite;
        BlossomVTree tree;
        BlossomVEdge edge;
        BlossomVTreeEdge treeEdge;
        // go through all tree roots and visit all incident edges of those roots.
        // if a (+, inf) edge is encountered => add it to the infinity heap
        // if a (+, +) edge is encountered and the opposite node hasn't been processed yet =>
        // add this edge to the heap of (+, +) cross-tree edges
        for (BlossomVNode root = nodes[nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            tree = root.tree;
            for (BlossomVNode.IncidentEdgeIterator edgeIterator = root.incidentEdgesIterator(); edgeIterator.hasNext(); ) {
                edge = edgeIterator.next();
                opposite = edge.head[edgeIterator.getDir()];
                if (opposite.isInfinityNode()) {
                    tree.addPlusInfinityEdge(edge);
                } else if (!opposite.isProcessed) {
                    if (opposite.tree.currentEdge == null) {
                        BlossomVState.addTreeEdge(tree, opposite.tree);
                    }
                    opposite.tree.currentEdge.addPlusPlusEdge(edge);
                }
            }
            root.isProcessed = true;
            for (BlossomVTree.TreeEdgeIterator treeEdgeIterator = tree.treeEdgeIterator(); treeEdgeIterator.hasNext(); ) {
                treeEdge = treeEdgeIterator.next();
                treeEdge.head[treeEdgeIterator.getCurrentDirection()].currentEdge = null;
            }
        }
        // clearing isProcessed flags
        for (BlossomVNode root = nodes[nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            root.isProcessed = false;
        }
    }

    /**
     * Helper method for allocating trees. Initializes the doubly linked list of tree roots
     * via treeSiblingPrev and treeSiblingNext. The same mechanism is used for keeping track
     * of the children of a node in the tree. The node nodes[nodeNum] is used to quichly find
     * the first root in the linked list
     */
    private void allocateTrees() {
        BlossomVNode lastRoot = nodes[nodeNum];
        for (int i = 0; i < nodeNum; i++) {
            BlossomVNode node = nodes[i];
            if (node.isPlusNode()) {
                node.treeSiblingPrev = lastRoot;
                lastRoot.treeSiblingNext = node;
                lastRoot = node;
                new BlossomVTree(node);
            }
        }
        lastRoot.treeSiblingNext = null;
    }

    /**
     * Method for finishing the fractional matching initialization. Goes through all nodes and expands half-loops.
     * The total number or trees equals to the number of half-loops. Tree roots are being chosen arbitrarily.
     *
     * @return the number of trees in the resulting state object, which equals to the number of unmatched nodes
     */
    private int finish() {
        if (DEBUG) {
            System.out.println("Finishing fractional matching initialization");
        }
        BlossomVNode varNode;
        BlossomVNode prevRoot = nodes[nodeNum];
        int treeNum = 0;
        for (int i = 0; i < nodeNum; i++) {
            varNode = nodes[i];
            varNode.firstTreeChild = varNode.treeSiblingNext = varNode.treeSiblingPrev = null;
            if (!varNode.isOuter) {
                expandInit(varNode, null); // this node becomes unmatched
                varNode.parentEdge = null;
                varNode.label = PLUS;
                new BlossomVTree(varNode);

                prevRoot.treeSiblingNext = varNode;
                varNode.treeSiblingPrev = prevRoot;
                prevRoot = varNode;
                treeNum++;
            }
        }
        return treeNum;
    }

    /**
     * Method for performing lazy delta spreading during the fractional matching initialization.
     * <p>
     * Goes through all nodes in the tree rooted at {@code root} and adds {@code eps} to the "+" nodes and
     * subtracts {@code eps} from "-" nodes. Updates incident edges respectively.
     *
     * @param fibHeap the heap for storing best edges
     * @param root    the root of the current tree
     * @param eps     the accumulated dual change of the tree
     */
    private void updateDuals(AddressableHeap<Double, BlossomVEdge> fibHeap, BlossomVNode root, double eps) {
        BlossomVNode varNode;
        BlossomVNode minusNode;
        BlossomVEdge varEdge;
        for (BlossomVTree.TreeNodeIterator treeNodeIterator = new BlossomVTree.TreeNodeIterator(root); treeNodeIterator.hasNext(); ) {
            varNode = treeNodeIterator.next();
            if (varNode.isProcessed) {
                varNode.dual += eps;
                if (!varNode.isTreeRoot) {
                    minusNode = varNode.getOppositeMatched();
                    minusNode.dual -= eps;
                    double delta = eps - varNode.matched.slack;
                    for (BlossomVNode.IncidentEdgeIterator iterator = minusNode.incidentEdgesIterator(); iterator.hasNext(); ) {
                        iterator.next().slack += delta;
                    }
                }
                for (BlossomVNode.IncidentEdgeIterator iterator = varNode.incidentEdgesIterator(); iterator.hasNext(); ) {
                    iterator.next().slack -= eps;
                }
                varNode.isProcessed = false;
            }
        }
        // clearing bestEdge after dual update
        while (!fibHeap.isEmpty()) {
            varEdge = fibHeap.findMin().getValue();
            varNode = varEdge.head[0].isInfinityNode() ? varEdge.head[0] : varEdge.head[1];
            removeFromHeap(varNode);
        }
    }

    /**
     * Method for correct adding of "best edges" to the {@code fibHeap}
     *
     * @param heap     the heap for storing best edges
     * @param node     infinity node {@code bestEdge} is incident to
     * @param bestEdge current best edge of the {@code node}
     */
    private void addToHead(AddressableHeap<Double, BlossomVEdge> heap, BlossomVNode node, BlossomVEdge bestEdge) {
        bestEdge.handle = heap.insert(bestEdge.slack, bestEdge);
        node.bestEdge = bestEdge;
    }

    /**
     * Method for correct removing of best edges from {@code fibHeap}
     *
     * @param node the node which best edge should be removed from {@code fibHeap}
     */
    private void removeFromHeap(BlossomVNode node) {
        node.bestEdge.handle.delete();
        node.bestEdge.handle = null;
        node.bestEdge = null;
    }

    /**
     * Finds blossom root during the fractional matching initialization
     *
     * @param blossomFormingEdge a tight (+, +) in-tree edge
     * @return the root of the blossom formed by the {@code blossomFormingEdge}
     */
    private BlossomVNode findBlossomRootInit(BlossomVEdge blossomFormingEdge) {
        BlossomVNode[] branches = new BlossomVNode[]{blossomFormingEdge.head[0], blossomFormingEdge.head[1]};
        BlossomVNode varNode;
        BlossomVNode root;
        BlossomVNode upperBound;
        int dir = 0;
        while (true) {
            if (!branches[dir].isOuter) {
                root = branches[dir];
                upperBound = branches[1 - dir];
                break;
            }
            branches[dir].isOuter = false;
            if (branches[dir].isTreeRoot) {
                upperBound = branches[dir];
                varNode = branches[1 - dir];
                while (varNode.isOuter) {
                    varNode.isOuter = false;
                    varNode = varNode.getTreeParent();
                    varNode.isOuter = false;
                    varNode = varNode.getTreeParent();
                }
                root = varNode;
                break;
            }
            varNode = branches[dir].getTreeParent();
            varNode.isOuter = false;
            branches[dir] = varNode.getTreeParent();
            dir = 1 - dir;
        }
        varNode = root;
        while (varNode != upperBound) {
            varNode = varNode.getTreeParent();
            varNode.isOuter = true;
            varNode = varNode.getTreeParent();
            varNode.isOuter = true;
        }
        return root;
    }

    private void handleInfinityEdgeInit(AddressableHeap<Double, BlossomVEdge> fibHeap, BlossomVEdge varEdge, int dir, double eps, double criticalEps) {
        BlossomVNode varNode = varEdge.head[1 - dir];
        BlossomVNode oppositeNode = varEdge.head[dir];
        if (varEdge.slack > eps) {
            // this edge isn't tight, but this edge can become a best edge
            if (varEdge.slack < criticalEps) {
                if (oppositeNode.bestEdge == null) {
                    addToHead(fibHeap, oppositeNode, varEdge);
                } else {
                    if (varEdge.slack < oppositeNode.bestEdge.slack) {
                        removeFromHeap(oppositeNode);
                        addToHead(fibHeap, oppositeNode, varEdge);
                    }
                }
            }
        } else {
            if (DEBUG) {
                System.out.println("Growing an edge " + varEdge);
            }
            // this is a tight edge, can grow it
            if (oppositeNode.bestEdge != null) {
                removeFromHeap(oppositeNode);
            }
            oppositeNode.label = MINUS;
            varNode.addChild(oppositeNode, varEdge, true);

            BlossomVNode plusNode = oppositeNode.matched.getOpposite(oppositeNode);
            if (plusNode.bestEdge != null) {
                removeFromHeap(plusNode);
            }
            plusNode.label = PLUS;
            oppositeNode.addChild(plusNode, plusNode.matched, true);
        }
    }

    /**
     * Augments the tree rooted at {@code root} via {@code augmentEdge}. The augmenting branch starts at {@code branchStart}
     *
     * @param root        the root of the tree to augment
     * @param branchStart the endpoint of the {@code augmentEdge} which belongs to the currentTree
     * @param augmentEdge a tight (+, +) cross-tree edge
     */
    private void augmentBranchInit(BlossomVNode root, BlossomVNode branchStart, BlossomVEdge augmentEdge) {
        if (DEBUG) {
            System.out.println("Augmenting an edge " + augmentEdge);
        }
        for (BlossomVTree.TreeNodeIterator iterator = new BlossomVTree.TreeNodeIterator(root); iterator.hasNext(); ) {
            iterator.next().label = BlossomVNode.Label.INFINITY;
        }

        BlossomVNode plusNode = branchStart;
        BlossomVNode minusNode = branchStart.getTreeParent();
        BlossomVEdge matchedEdge = augmentEdge;
        while (minusNode != null) {
            plusNode.matched = matchedEdge;
            minusNode.matched = matchedEdge = minusNode.parentEdge;
            plusNode = minusNode.getTreeParent();
            minusNode = plusNode.getTreeParent();
        }
        root.matched = matchedEdge;

        root.removeFromChildList();
        root.isTreeRoot = false;
    }

    /**
     * Forms a 1/2-valued odd circuit. Nodes from the odd circuit aren't actually contracted into a single
     * pseudonode. The blossomSibling references are set so that the nodes form a circular linked list.
     * The matching is updated respectively.
     * <p>
     * <b>Note: </b> each node of the circuit can be expanded in the future and become a new tree root.
     *
     * @param blossomFormingEdge a tight (+, +) in-tree edge that forms an odd circuit
     * @param treeRoot           the root of the tree odd circuit belongs to
     */
    private void shrinkInit(BlossomVEdge blossomFormingEdge, BlossomVNode treeRoot) {
        if (DEBUG) {
            System.out.println("Shrinking an edge " + blossomFormingEdge);
        }
        for (BlossomVTree.TreeNodeIterator iterator = new BlossomVTree.TreeNodeIterator(treeRoot); iterator.hasNext(); ) {
            iterator.next().label = BlossomVNode.Label.INFINITY;
        }
        BlossomVNode blossomRoot = findBlossomRootInit(blossomFormingEdge);

        if (!blossomRoot.isTreeRoot) {
            BlossomVNode minusNode = blossomRoot.getTreeParent();
            BlossomVEdge prevEdge = minusNode.parentEdge;
            minusNode.matched = minusNode.parentEdge;
            BlossomVNode plusNode = minusNode.getTreeParent();
            while (plusNode != treeRoot) {
                minusNode = plusNode.getTreeParent();
                plusNode.matched = prevEdge;
                minusNode.matched = prevEdge = minusNode.parentEdge;
                plusNode = minusNode.getTreeParent();
            }
            plusNode.matched = prevEdge;
        }

        BlossomVEdge prevEdge = blossomFormingEdge;
        for (BlossomVState.BlossomNodesIterator iterator = new BlossomVState.BlossomNodesIterator(blossomRoot, blossomFormingEdge); iterator.hasNext(); ) {
            BlossomVNode current = iterator.next();
            current.label = PLUS;
            if (iterator.getCurrentDirection() == 0) {
                current.blossomSibling = prevEdge;
                prevEdge = current.parentEdge;
            } else {
                current.blossomSibling = current.parentEdge;
            }
        }
        treeRoot.removeFromChildList();
        treeRoot.isTreeRoot = false;

    }

    /**
     * Expands a 1/2-valued odd circuit. Essentially, changes the matching of the circuit so that
     * {@code blossomNode} becomes an unmatched node. Sets the labels of the matched nodes of the
     * circuit to {@link org.jgrapht.alg.matching.blossom.v5.BlossomVNode.Label#INFINITY}
     *
     * @param blossomNode        some node that belongs to the "contracted" odd circuit
     * @param blossomNodeMatched a matched edge of the {@code blossomNode}, which doesn't belong to the
     *                           circuit. Note: this value can be {@code null}
     */
    private void expandInit(BlossomVNode blossomNode, BlossomVEdge blossomNodeMatched) {
        if (DEBUG) {
            System.out.println("Expanding node " + blossomNode);
        }
        BlossomVNode currentNode = blossomNode.blossomSibling.getOpposite(blossomNode);
        BlossomVEdge prevEdge;

        blossomNode.isOuter = true;
        blossomNode.label = BlossomVNode.Label.INFINITY;
        blossomNode.matched = blossomNodeMatched;
        do {
            currentNode.matched = prevEdge = currentNode.blossomSibling;
            currentNode.isOuter = true;
            currentNode.label = BlossomVNode.Label.INFINITY;
            currentNode = currentNode.blossomSibling.getOpposite(currentNode);

            currentNode.matched = prevEdge;
            currentNode.isOuter = true;
            currentNode.label = BlossomVNode.Label.INFINITY;
            currentNode = currentNode.blossomSibling.getOpposite(currentNode);
        } while (currentNode != blossomNode);
    }

    /**
     * Solves the fractional matching problem formulated on the initial graph. The linear programming
     * formulation of the fractional matching problem is identical to the one used for bipartite graphs.
     * More precisely:
     * <oi>
     * <li>Minimize the $sum_{e\in E}x_e\times c_e$ subject to:</li>
     * <li>For all nodes: $\sum_{e is incident to v}x_e = 1$</li>
     * <li>For all edges: $x_e \ge 0$</li>
     * </oi>
     *
     * @return the number of trees in the resulting state object, which equals to the number of unmatched nodes.
     */
    private int initFractional() {
        BlossomVNode root;
        BlossomVNode root2;
        BlossomVNode root3 = null;
        BlossomVNode varNode;
        int varDir;
        BlossomVEdge varEdge;
        BlossomVNode oppositeNode;
        BlossomVNode.IncidentEdgeIterator iterator;

        /*
         * For every free node u, which is adjacent to at least one "+" node in the current tree, we keep track
         * of an edge, that has minimum slack and connects node u and some "+" node in the current tree.
         */
        AddressableHeap<Double, BlossomVEdge> fibHeap = new PairingHeap<>();

        for (root = nodes[nodeNum].treeSiblingNext; root != null; ) {
            root2 = root.treeSiblingNext;
            if (root2 != null) {
                root3 = root2.treeSiblingNext;
            }
            varNode = root;

            fibHeap.clear();

            double eps = 0;
            Action flag = NONE;
            BlossomVNode branchRoot = varNode;
            BlossomVEdge criticalEdge = null;
            double criticalEps = INFINITY;
            int criticalDir = -1;
            boolean primalOperation = false;

            /*
             * Growing a tree while is it possible. Main goal is to apply a primal operation. Therefore,
             * If we encounter a tight (+, +) cross-tree or in-tree edge => we won't be able to increase
             * dual objective function anymore (can't increase eps of the current tree)
             * => we go out of the loop, apply lazy dual changes to the current branch and perform an
             * augment or shrink operation.
             *
             * Tree is being grown in phases. Each phase starts with a new "branch", the reason to
             * start a new branch is that the tree can't be grown any further without dual changes and there
             * no primal operation can be applied. Therefore, we choose an edge of minimum slack from fibHeap,
             * set the eps of the branch so that this edge becomes tight
             */
            while (true) {
                varNode.isProcessed = true;
                varNode.dual -= eps; // applying lazy delta spreading

                if (!varNode.isTreeRoot) {
                    // applying lazy delta spreading to the matched "-" node
                    varNode.matched.getOpposite(varNode).dual += eps;
                }

                /*
                 * Processing edges incident to the current node.
                 */
                for (iterator = varNode.incidentEdgesIterator(); iterator.hasNext(); ) {
                    varEdge = iterator.next();
                    varDir = iterator.getDir();

                    varEdge.slack += eps; // applying lazy delta spreading
                    oppositeNode = varEdge.head[varDir];

                    if (oppositeNode.tree == root.tree) {
                        // opposite node is in the same tree
                        if (oppositeNode.isPlusNode()) {
                            double slack = varEdge.slack;
                            if (!oppositeNode.isProcessed) {
                                slack += eps;
                            }
                            if (2 * criticalEps > slack || criticalEdge == null) {
                                flag = SHRINK;
                                criticalEps = slack / 2;
                                criticalEdge = varEdge;
                                criticalDir = varDir;
                                if (criticalEps <= eps) {
                                    // found a tight (+, +) in-tree edge to shrink => go out of the loop
                                    primalOperation = true;
                                    break;
                                }
                            }
                        }

                    } else if (oppositeNode.isPlusNode()) {
                        // varEdge is a (+, +) cross-tree edge
                        if (criticalEps >= varEdge.slack || criticalEdge == null) {
                            //
                            flag = AUGMENT;
                            criticalEps = varEdge.slack;
                            criticalEdge = varEdge;
                            criticalDir = varDir;
                            if (criticalEps <= eps) {
                                // found a tight (+, +) cross-tree edge to augment
                                primalOperation = true;
                                break;
                            }
                        }

                    } else {
                        // opposite node is an infinity node
                        handleInfinityEdgeInit(fibHeap, varEdge, varDir, eps, criticalEps);
                    }
                }
                if (primalOperation) {
                    // finish processing incident edges
                    while (iterator.hasNext()) {
                        iterator.next().slack += eps;
                    }
                    // exit the loop since we can perform shrink or augment operation
                    break;
                } else {
                    /*
                     * Moving currentNode to the next unprocessed "+" node in the tree
                     * growing the tree if it is possible. Starting a new branch if all nodes have
                     * been processed. Exit the loop, if the slack of fibHeap.min().getData() is >=
                     * than the slack of critical edge (in this case we can perform primal operation
                     * after updating the duals).
                     */
                    if (varNode.firstTreeChild != null) {
                        // moving to the next grandchild
                        varNode = varNode.firstTreeChild.getOppositeMatched();
                    } else {
                        // trying to find another unprocessed node
                        while (varNode != branchRoot && varNode.treeSiblingNext == null) {
                            varNode = varNode.getTreeParent();
                        }
                        if (varNode.isMinusNode()) {
                            // found an unprocessed node
                            varNode = varNode.treeSiblingNext.getOppositeMatched();
                        } else if (varNode == branchRoot) {
                            // we've processed all nodes in the current branch
                            BlossomVEdge minSlackEdge = fibHeap.isEmpty() ? null : fibHeap.findMin().getValue();
                            if (minSlackEdge == null || minSlackEdge.slack >= criticalEps) {
                                // can perform primal operation after updating duals
                                if (DEBUG) {
                                    System.out.println("Now current eps = " + criticalEps);
                                }
                                if (criticalEps > NO_PERFECT_MATCHING_THRESHOLD) {
                                    throw new IllegalArgumentException(NO_PERFECT_MATCHING);
                                }
                                eps = criticalEps;
                                break;
                            } else {
                                // growing minimum slack edge
                                if (DEBUG) {
                                    System.out.println("Growing an edge " + minSlackEdge);
                                }
                                int dirToFreeNode = minSlackEdge.head[0].isInfinityNode() ? 0 : 1;
                                varNode = minSlackEdge.head[1 - dirToFreeNode];
                                BlossomVNode minusNode = minSlackEdge.head[dirToFreeNode];
                                removeFromHeap(minusNode);
                                minusNode.label = MINUS;
                                varNode.addChild(minusNode, minSlackEdge, true);
                                eps = minSlackEdge.slack; // setting new eps of the tree

                                BlossomVNode plusNode = minusNode.getOppositeMatched();
                                if (plusNode.bestEdge != null) {
                                    removeFromHeap(plusNode);
                                }
                                plusNode.label = PLUS;
                                minusNode.addChild(plusNode, minusNode.matched, true);

                                if (DEBUG) {
                                    System.out.println("New branch root is " + plusNode + ", eps = " + eps);
                                }
                                //Starting a new branch
                                varNode = branchRoot = plusNode;
                            }
                        }
                    }
                }
            }

            // updating duals
            updateDuals(fibHeap, root, eps);

            // applying primal operation
            BlossomVNode from = criticalEdge.head[1 - criticalDir];
            BlossomVNode to = criticalEdge.head[criticalDir];
            if (flag == SHRINK) {
                shrinkInit(criticalEdge, root);
            } else {
                augmentBranchInit(root, from, criticalEdge);
                if (to.isOuter) {
                    // node to doesn't belong to a 1/2-values odd circuit
                    augmentBranchInit(to, to, criticalEdge); // to is the root of the opposite tree
                } else {
                    // node to belons to a 1/2-values odd circuit
                    expandInit(to, criticalEdge);
                }
            }


            root = root2;
            if (root != null && !root.isTreeRoot) {
                root = root3;
            }
        }

        return finish();
    }

    /**
     * Enum for specifying the primal operation to perform with critical edge during fractional matching
     * initialization
     */
    enum Action {
        NONE, SHRINK, AUGMENT,
    }
}
