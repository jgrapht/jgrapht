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

import org.jgrapht.*;
import org.jheaps.*;
import org.jheaps.tree.*;

import java.util.*;

import static org.jgrapht.alg.matching.blossom.v5.BlossomVInitializer.Action.*;
import static org.jgrapht.alg.matching.blossom.v5.BlossomVNode.Label.MINUS;
import static org.jgrapht.alg.matching.blossom.v5.BlossomVNode.Label.PLUS;
import static org.jgrapht.alg.matching.blossom.v5.KolmogorovMinimumWeightPerfectMatching.*;

/**
 * Is used to start the Kolmogorov's Blossom V algorithm. Performs initialization of the algorithm's
 * internal data structures and finds an initial matching according to the strategy specified in
 * {@code options}.
 * <p>
 * The initialization process involves converting the graph into internal representation, allocating
 * trees for unmatched vertices, and creating an auxiliary graph whose nodes correspond to
 * alternating trees. The only part that varies is the strategy to find an initial matching to speed
 * up the main part of the algorithm.
 * <p>
 * The simple initialization (option {@link BlossomVOptions.InitializationType#NONE}) doesn't find
 * any matching and initializes the data structures by allocating $|V|$ single vertex trees. This is
 * the fastest initialization strategy; however, it slows the main algorithm down.
 * <p>
 * The greedy initialization (option {@link BlossomVOptions.InitializationType#GREEDY} runs in two
 * phases. First, for every node it determines an edge of minimum weight and assigns half of that
 * weight to the node's dual variable. This ensures that the slacks of all edges are non-negative.
 * After that it goes through all nodes again, greedily increases its dual variable and chooses an
 * incident matching edge if it is possible. After that every node is incident to at least one tight
 * edge. The resulting matching is an output of this initialization strategy.
 * <p>
 * The fractional matching initialization (option
 * {@link BlossomVOptions.InitializationType#FRACTIONAL}) is both the most complicated and the most
 * efficient type of initialization. The linear programming formulation of the fractional matching
 * problem is identical to the one used for bipartite graphs. More precisely: <oi>
 * <li>Minimize the $sum_{e\in E}x_e\times c_e$ subject to:</li>
 * <li>For all nodes: $\sum_{e is incident to v}x_e = 1$</li>
 * <li>For all edges: $x_e \ge 0$</li> </oi> <b>Note:</b> for an optimal solution in general graphs
 * we have to require the variables $x_e$ to be $0$ or $1$. For more information on this type of
 * initialization, see: <i>David Applegate and William J. Cook. \Solving Large-Scale Matching
 * Problems". In: Network Flows And Matching. 1991.</i>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 * @see KolmogorovMinimumWeightPerfectMatching
 */
class BlossomVInitializer<V, E>
{
    /**
     * The graph for which to find a matching
     */
    private final Graph<V, E> graph;
    /**
     * Number of nodes in the graph
     */
    private int nodeNum;
    /**
     * Number of edges in the graph
     */
    private int edgeNum = 0;
    /**
     * An array of nodes that will be passed to the resulting state object
     */
    private BlossomVNode[] nodes;
    /**
     * An array of edges that will be passed to the resulting state object
     */
    private BlossomVEdge[] edges;
    /**
     * Generic vertices of the {@code graph} in the same order as internal nodes in the array
     * {@code nodes}. Since for each node in the {@code nodes} we know its position in the
     * {@code nodes}, we can determine its generic counterpart in constant time
     */
    private List<V> graphVertices;
    /**
     * Generic edges of the {@code graph} in the same order as internal edges in the array
     * {@code edges}. Since for each edge in the {@code edges} we know its position in the
     * {@code edges}, we can determine its generic counterpart in constant time
     */
    private List<E> graphEdges;

    /**
     * Creates a new BlossomVInitializer instance
     *
     * @param graph the graph to search matching in
     */
    public BlossomVInitializer(Graph<V, E> graph)
    {
        this.graph = graph;
        nodeNum = graph.vertexSet().size();
    }

    /**
     * Converts the generic graph representation into the data structure form convenient for the
     * algorithm, and initializes the matching according to the strategy specified in
     * {@code options}.
     *
     * @param options the options of the algorithm
     * @return the state object with all necessary information for the algorithm
     */
    public BlossomVState<V, E> initialize(BlossomVOptions options)
    {
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
     * Performs simple initialization of the matching by allocating $|V|$ trees. The result of this
     * type of initialization is an empty matching. That is why this is the most basic type of
     * initialization.
     *
     * @param options the options of the algorithm
     * @return the state object with all necessary information for the algorithm
     */
    private BlossomVState<V, E> simpleInitialization(BlossomVOptions options)
    {
        initGraph();
        for (BlossomVNode node : nodes) {
            node.isOuter = true;
        }
        allocateTrees();
        initAuxiliaryGraph();
        return new BlossomVState<>(
            graph, nodes, edges, nodeNum, edgeNum, nodeNum, graphVertices, graphEdges, options);
    }

    /**
     * Performs greedy initialization of the algorithm. For the description of this initialization
     * strategy see the class description.
     *
     * @param options the options of the algorithm
     * @return the state object with all necessary information for the algorithm
     */
    private BlossomVState<V, E> greedyInitialization(BlossomVOptions options)
    {
        initGraph();
        int treeNum = initGreedy();
        allocateTrees();
        initAuxiliaryGraph();
        return new BlossomVState<>(
            graph, nodes, edges, nodeNum, edgeNum, treeNum, graphVertices, graphEdges, options);
    }

    /**
     * Performs fractional matching initialization, see {@link BlossomVInitializer#initFractional()}
     * for the description.
     *
     * @param options the options of the algorithm
     * @return the state object with all necessary information for the algorithm
     */
    private BlossomVState<V, E> fractionalMatchingInitialization(BlossomVOptions options)
    {
        initGraph();
        initGreedy();
        allocateTrees();
        int treeNum = initFractional();
        initAuxiliaryGraph();
        return new BlossomVState<>(
            graph, nodes, edges, nodeNum, edgeNum, treeNum, graphVertices, graphEdges, options);
    }

    /**
     * Converts the generic graph representation into the form convenient for the algorithm
     */
    private void initGraph()
    {
        int expectedEdgeNum = graph.edgeSet().size();
        nodes = new BlossomVNode[nodeNum + 1];
        edges = new BlossomVEdge[expectedEdgeNum];
        graphVertices = new ArrayList<>(nodeNum);
        graphEdges = new ArrayList<>(expectedEdgeNum);
        HashMap<V, BlossomVNode> vertexMap = new HashMap<>(nodeNum);
        int i = 0;
        // maps nodes
        for (V vertex : graph.vertexSet()) {
            nodes[i] = new BlossomVNode(i);
            graphVertices.add(vertex);
            vertexMap.put(vertex, nodes[i]);
            i++;
        }
        nodes[nodeNum] = new BlossomVNode(nodeNum); // auxiliary node to keep track of the first
                                                    // item in the linked list of tree roots
        i = 0;
        // maps edges
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
     * Adds a new edge between {@code from} and {@code to}. The resulting edge points from
     * {@code from} to {@code to}
     *
     * @param from the tail of this edge
     * @param to the head of this edge
     * @param slack the slack of the resulting edge
     * @param pos position of the resulting edge in the array {@code edges}
     * @return the newly added edge
     */
    public BlossomVEdge addEdge(BlossomVNode from, BlossomVNode to, double slack, int pos)
    {
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
     * Performs greedy matching initialization.
     * <p>
     * For every node we choose an incident edge of minimum slack and set its dual to half of this
     * slack. This maintains the nonnegativity of edge slacks. After that we go through all nodes
     * again, greedily increase their dual variables, and match them if it is possible.
     *
     * @return the number of unmatched nodes, which equals the number of trees
     */
    private int initGreedy()
    {
        // set all dual variables to infinity
        for (int i = 0; i < nodeNum; i++) {
            nodes[i].dual = INFINITY;
        }
        // set dual variables to half of the minimum weight of the incident edges
        for (int i = 0; i < edgeNum; i++) {
            BlossomVEdge edge = edges[i];
            if (edge.head[0].dual > edge.slack) {
                edge.head[0].dual = edge.slack;
            }
            if (edge.head[1].dual > edge.slack) {
                edge.head[1].dual = edge.slack;
            }
        }
        // divide dual variables by two; this ensures nonnegativity of all slacks;
        // decrease edge slacks accordingly
        for (int i = 0; i < edgeNum; i++) {
            BlossomVEdge edge = edges[i];
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
        // go through all vertices, greedily increase their dual variables to the minimum slack of
        // incident edges;
        // if there exists a tight unmatched edge in the neighborhood, match it
        int treeNum = nodeNum;
        for (int i = 0; i < nodeNum; i++) {
            BlossomVNode node = nodes[i];
            if (!node.isInfinityNode()) {
                double minSlack = INFINITY;
                // find the minimum slack of incident edges
                for (BlossomVNode.IncidentEdgeIterator incidentEdgeIterator =
                    node.incidentEdgesIterator(); incidentEdgeIterator.hasNext();)
                {
                    BlossomVEdge edge = incidentEdgeIterator.next();
                    if (edge.slack < minSlack) {
                        minSlack = edge.slack;
                    }
                }
                node.dual += minSlack;
                double resultMinSlack = minSlack;
                // subtract minimum slack from the slacks of all incident edges
                for (BlossomVNode.IncidentEdgeIterator incidentEdgeIterator =
                    node.incidentEdgesIterator(); incidentEdgeIterator.hasNext();)
                {
                    BlossomVEdge edge = incidentEdgeIterator.next();
                    int dir = incidentEdgeIterator.getDir();
                    if (edge.slack <= resultMinSlack && node.isPlusNode()
                        && edge.head[dir].isPlusNode())
                    {
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
     * Initializes an auxiliary graph by adding tree edges between trees and adding (+, +)
     * cross-tree edges and (+, inf) edges to the appropriate heaps
     */
    private void initAuxiliaryGraph()
    {
        // go through all tree roots and visit all incident edges of those roots.
        // if a (+, inf) edge is encountered => add it to the infinity heap
        // if a (+, +) edge is encountered and the opposite node hasn't been processed yet =>
        // add this edge to the heap of (+, +) cross-tree edges
        for (BlossomVNode root = nodes[nodeNum].treeSiblingNext; root != null;
            root = root.treeSiblingNext)
        {
            BlossomVTree tree = root.tree;
            for (BlossomVNode.IncidentEdgeIterator edgeIterator = root.incidentEdgesIterator();
                edgeIterator.hasNext();)
            {
                BlossomVEdge edge = edgeIterator.next();
                BlossomVNode opposite = edge.head[edgeIterator.getDir()];
                if (opposite.isInfinityNode()) {
                    tree.addPlusInfinityEdge(edge);
                } else if (!opposite.isProcessed) {
                    if (opposite.tree.currentEdge == null) {
                        BlossomVTree.addTreeEdge(tree, opposite.tree);
                    }
                    opposite.tree.currentEdge.addPlusPlusEdge(edge);
                }
            }
            root.isProcessed = true;
            for (BlossomVTree.TreeEdgeIterator treeEdgeIterator = tree.treeEdgeIterator();
                treeEdgeIterator.hasNext();)
            {
                BlossomVTreeEdge treeEdge = treeEdgeIterator.next();
                treeEdge.head[treeEdgeIterator.getCurrentDirection()].currentEdge = null;
            }
        }
        // clear isProcessed flags
        for (BlossomVNode root = nodes[nodeNum].treeSiblingNext; root != null;
            root = root.treeSiblingNext)
        {
            root.isProcessed = false;
        }
    }

    /**
     * Allocates trees. Initializes the doubly linked list of tree roots via treeSiblingPrev and
     * treeSiblingNext. The same mechanism is used for keeping track of the children of a node in
     * the tree. The lookup {@code nodes[nodeNum] } is used to quickly find the first root in the
     * linked list
     */
    private void allocateTrees()
    {
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
     * Finishes the fractional matching initialization. Goes through all nodes and expands
     * half-loops. The total number or trees equals to the number of half-loops. Tree roots are
     * chosen arbitrarily.
     *
     * @return the number of trees in the resulting state object, which equals the number of
     *         unmatched nodes
     */
    private int finish()
    {
        if (DEBUG) {
            System.out.println("Finishing fractional matching initialization");
        }
        BlossomVNode prevRoot = nodes[nodeNum];
        int treeNum = 0;
        for (int i = 0; i < nodeNum; i++) {
            BlossomVNode node = nodes[i];
            node.firstTreeChild = node.treeSiblingNext = node.treeSiblingPrev = null;
            if (!node.isOuter) {
                expandInit(node, null); // this node becomes unmatched
                node.parentEdge = null;
                node.label = PLUS;
                new BlossomVTree(node);

                prevRoot.treeSiblingNext = node;
                node.treeSiblingPrev = prevRoot;
                prevRoot = node;
                treeNum++;
            }
        }
        return treeNum;
    }

    /**
     * Performs lazy delta spreading during the fractional matching initialization.
     * <p>
     * Goes through all nodes in the tree rooted at {@code root} and adds {@code eps} to the "+"
     * nodes and subtracts {@code eps} from "-" nodes. Updates incident edges respectively.
     *
     * @param heap the heap for storing best edges
     * @param root the root of the current tree
     * @param eps the accumulated dual change of the tree
     */
    private void updateDuals(
        AddressableHeap<Double, BlossomVEdge> heap, BlossomVNode root, double eps)
    {
        for (BlossomVTree.TreeNodeIterator treeNodeIterator =
            new BlossomVTree.TreeNodeIterator(root); treeNodeIterator.hasNext();)
        {
            BlossomVNode treeNode = treeNodeIterator.next();
            if (treeNode.isProcessed) {
                treeNode.dual += eps;
                if (!treeNode.isTreeRoot) {
                    BlossomVNode minusNode = treeNode.getOppositeMatched();
                    minusNode.dual -= eps;
                    double delta = eps - treeNode.matched.slack;
                    for (BlossomVNode.IncidentEdgeIterator iterator =
                        minusNode.incidentEdgesIterator(); iterator.hasNext();)
                    {
                        iterator.next().slack += delta;
                    }
                }
                for (BlossomVNode.IncidentEdgeIterator iterator = treeNode.incidentEdgesIterator();
                    iterator.hasNext();)
                {
                    iterator.next().slack -= eps;
                }
                treeNode.isProcessed = false;
            }
        }
        // clear bestEdge after dual update
        while (!heap.isEmpty()) {
            BlossomVEdge edge = heap.findMin().getValue();
            BlossomVNode node = edge.head[0].isInfinityNode() ? edge.head[0] : edge.head[1];
            removeFromHeap(node);
        }
    }

    /**
     * Adds "best edges" to the {@code heap}
     *
     * @param heap the heap for storing best edges
     * @param node infinity node {@code bestEdge} is incident to
     * @param bestEdge current best edge of the {@code node}
     */
    private void addToHead(
        AddressableHeap<Double, BlossomVEdge> heap, BlossomVNode node, BlossomVEdge bestEdge)
    {
        bestEdge.handle = heap.insert(bestEdge.slack, bestEdge);
        node.bestEdge = bestEdge;
    }

    /**
     * Removes "best edge" from {@code heap}
     *
     * @param node the node which best edge should be removed from the heap it is stored in
     */
    private void removeFromHeap(BlossomVNode node)
    {
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
    private BlossomVNode findBlossomRootInit(BlossomVEdge blossomFormingEdge)
    {
        BlossomVNode[] branches =
            new BlossomVNode[] { blossomFormingEdge.head[0], blossomFormingEdge.head[1] };
        BlossomVNode root, upperBound; // need to be scoped outside of the loop
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
                BlossomVNode jumpNode = branches[1 - dir];
                while (jumpNode.isOuter) {
                    jumpNode.isOuter = false;
                    jumpNode = jumpNode.getTreeParent();
                    jumpNode.isOuter = false;
                    jumpNode = jumpNode.getTreeParent();
                }
                root = jumpNode;
                break;
            }
            BlossomVNode node = branches[dir].getTreeParent();
            node.isOuter = false;
            branches[dir] = node.getTreeParent();
            dir = 1 - dir;
        }
        BlossomVNode jumpNode = root;
        while (jumpNode != upperBound) {
            jumpNode = jumpNode.getTreeParent();
            jumpNode.isOuter = true;
            jumpNode = jumpNode.getTreeParent();
            jumpNode.isOuter = true;
        }
        return root;
    }

    /**
     * Handles encountered infinity edges incident to "+" nodes of the alternating tree. This method
     * determines whether the {@code infinityEdge} is tight. If so, it applies grow operation to it.
     * Otherwise, it determines whether it has smaller slack than {@code criticalEps}. If so, this
     * edge becomes the best edge of the "+" node in the tree.
     *
     * @param heap the heap of infinity edges incident to the currently processed tree
     * @param infinityEdge encountered infinity edge
     * @param dir direction of the infinityEdge to the infinity node
     * @param eps the eps of the current branch
     * @param criticalEps the value by which the epsilon of the current tree can be increased so
     *        that the slacks of (+, +) cross-tree and in-tree edges don't become negative
     */
    private void handleInfinityEdgeInit(
        AddressableHeap<Double, BlossomVEdge> heap, BlossomVEdge infinityEdge, int dir, double eps,
        double criticalEps)
    {
        BlossomVNode inTreeNode = infinityEdge.head[1 - dir];
        BlossomVNode oppositeNode = infinityEdge.head[dir];
        if (infinityEdge.slack > eps) { // this edge isn't tight, but this edge can become a best
                                        // edge
            if (infinityEdge.slack < criticalEps) { // this edge can become a best edge
                if (oppositeNode.bestEdge == null) { // inTreeNode hadn't had any best edge before
                    addToHead(heap, oppositeNode, infinityEdge);
                } else {
                    if (infinityEdge.slack < oppositeNode.bestEdge.slack) {
                        removeFromHeap(oppositeNode);
                        addToHead(heap, oppositeNode, infinityEdge);
                    }
                }
            }
        } else {
            if (DEBUG) {
                System.out.println("Growing an edge " + infinityEdge);
            }
            // this is a tight edge, can grow it
            if (oppositeNode.bestEdge != null) {
                removeFromHeap(oppositeNode);
            }
            oppositeNode.label = MINUS;
            inTreeNode.addChild(oppositeNode, infinityEdge, true);

            BlossomVNode plusNode = oppositeNode.matched.getOpposite(oppositeNode);
            if (plusNode.bestEdge != null) {
                removeFromHeap(plusNode);
            }
            plusNode.label = PLUS;
            oppositeNode.addChild(plusNode, plusNode.matched, true);
        }
    }

    /**
     * Augments the tree rooted at {@code treeRoot} via {@code augmentEdge}. The augmenting branch
     * starts at {@code branchStart}
     *
     * @param treeRoot the root of the tree to augment
     * @param branchStart the endpoint of the {@code augmentEdge} which belongs to the currentTree
     * @param augmentEdge a tight (+, +) cross-tree edge
     */
    private void augmentBranchInit(
        BlossomVNode treeRoot, BlossomVNode branchStart, BlossomVEdge augmentEdge)
    {
        if (DEBUG) {
            System.out.println("Augmenting an edge " + augmentEdge);
        }
        for (BlossomVTree.TreeNodeIterator iterator = new BlossomVTree.TreeNodeIterator(treeRoot);
            iterator.hasNext();)
        {
            iterator.next().label = BlossomVNode.Label.INFINITY;
        }

        BlossomVNode plusNode = branchStart;
        BlossomVNode minusNode = branchStart.getTreeParent();
        BlossomVEdge matchedEdge = augmentEdge;
        // alternate the matching from branch start up to the tree root
        while (minusNode != null) {
            plusNode.matched = matchedEdge;
            minusNode.matched = matchedEdge = minusNode.parentEdge;
            plusNode = minusNode.getTreeParent();
            minusNode = plusNode.getTreeParent();
        }
        treeRoot.matched = matchedEdge;

        treeRoot.removeFromChildList();
        treeRoot.isTreeRoot = false;
    }

    /**
     * Forms a 1/2-valued odd circuit. Nodes from the odd circuit aren't actually contracted into a
     * single pseudonode. The blossomSibling references are set so that the nodes form a circular
     * linked list. The matching is updated respectively.
     * <p>
     * <b>Note:</b> each node of the circuit can be expanded in the future and become a new tree
     * root.
     *
     * @param blossomFormingEdge a tight (+, +) in-tree edge that forms an odd circuit
     * @param treeRoot the root of the tree odd circuit belongs to
     */
    private void shrinkInit(BlossomVEdge blossomFormingEdge, BlossomVNode treeRoot)
    {
        if (DEBUG) {
            System.out.println("Shrinking an edge " + blossomFormingEdge);
        }
        for (BlossomVTree.TreeNodeIterator iterator = new BlossomVTree.TreeNodeIterator(treeRoot);
            iterator.hasNext();)
        {
            iterator.next().label = BlossomVNode.Label.INFINITY;
        }
        BlossomVNode blossomRoot = findBlossomRootInit(blossomFormingEdge);

        // alternate the matching from blossom root up to the tree root
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

        // set the circular blossomSibling references
        BlossomVEdge prevEdge = blossomFormingEdge;
        for (BlossomVEdge.BlossomNodesIterator iterator =
            blossomFormingEdge.blossomNodesIterator(blossomRoot); iterator.hasNext();)
        {
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
     * the {@code blossomNode} becomes matched to the {@code blossomNodeMatched} edge and all other
     * nodes become matched. Sets the labels of the matched nodes of the circuit to
     * {@link org.jgrapht.alg.matching.blossom.v5.BlossomVNode.Label#INFINITY}
     *
     * @param blossomNode some node that belongs to the "contracted" odd circuit
     * @param blossomNodeMatched a matched edge of the {@code blossomNode}, which doesn't belong to
     *        the circuit. <b>Note:</b> this value can be {@code null}
     */
    private void expandInit(BlossomVNode blossomNode, BlossomVEdge blossomNodeMatched)
    {
        if (DEBUG) {
            System.out.println("Expanding node " + blossomNode);
        }
        BlossomVNode currentNode = blossomNode.blossomSibling.getOpposite(blossomNode);

        blossomNode.isOuter = true;
        blossomNode.label = BlossomVNode.Label.INFINITY;
        blossomNode.matched = blossomNodeMatched;
        // change the matching in the blossom
        do {
            currentNode.matched = currentNode.blossomSibling;
            BlossomVEdge prevEdge = currentNode.blossomSibling;
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
     * Solves the fractional matching problem formulated on the initial graph. See the class
     * description for more information about fractional matching initialization.
     *
     * @return the number of trees in the resulting state object, which equals to the number of
     *         unmatched nodes.
     */
    private int initFractional()
    {
        /*
         * For every free node u, which is adjacent to at least one "+" node in the current tree, we
         * keep track of an edge that has minimum slack and connects node u and some "+" node in the
         * current tree. This edge is called a "best edge".
         */
        AddressableHeap<Double, BlossomVEdge> heap = new PairingHeap<>();

        for (BlossomVNode root = nodes[nodeNum].treeSiblingNext; root != null;) {
            BlossomVNode root2 = root.treeSiblingNext;
            BlossomVNode root3 = null;
            if (root2 != null) {
                root3 = root2.treeSiblingNext;
            }
            BlossomVNode currentNode = root;

            heap.clear();

            double branchEps = 0;
            Action flag = NONE;
            BlossomVNode branchRoot = currentNode;
            BlossomVEdge criticalEdge = null;
            /*
             * Let's denote the minimum slack of (+, inf) edges incident to nodes of this tree as
             * infSlack. Critical eps is the minimum dual value which can be chosen as the branchEps
             * so that it doesn't violate the dual constraints on (+, +) in-tree and cross-tree
             * edges. It is always greater than or equal to the branchEps. If it is equal to the
             * branchEps, a shrink or augment operation can be applied immediately. If it is greater
             * than branchEps, we have to compare it with infSlack. If criticalEps is greater than
             * infSlack, we have to do a grow operation after we increase the branchEps by infSlack
             * - branchEps. Otherwise, we can apply shrink or augment operations after we increase
             * the branchEps by criticalEps - branchEps.
             */
            double criticalEps = INFINITY;
            int criticalDir = -1;
            boolean primalOperation = false;

            /*
             * Grow a tree as much as possible. Main goal is to apply a primal operation. Therefore,
             * if we encounter a tight (+, +) cross-tree or in-tree edge => we won't be able to
             * increase dual objective function anymore (can't increase branchEps) => we go out of
             * the loop, apply lazy dual changes to the current branch and perform an augment or
             * shrink operation.
             *
             * A tree is grown in phases. Each phase starts with a new "branch"; the reason to start
             * a new branch is that the tree can't be grown any further without dual changes and
             * therefore no primal operation can be applied. That is why we choose an edge of
             * minimum slack from heap, and set the eps of the branch so that this edge becomes
             * tight
             */
            while (true) {
                currentNode.isProcessed = true;
                currentNode.dual -= branchEps; // apply lazy delta spreading

                if (!currentNode.isTreeRoot) {
                    // apply lazy delta spreading to the matched "-" node
                    currentNode.getOppositeMatched().dual += branchEps;
                }

                // Process edges incident to the current node
                BlossomVNode.IncidentEdgeIterator iterator;
                for (iterator = currentNode.incidentEdgesIterator(); iterator.hasNext();) {
                    BlossomVEdge currentEdge = iterator.next();
                    int dir = iterator.getDir();

                    currentEdge.slack += branchEps; // apply lazy delta spreading
                    BlossomVNode oppositeNode = currentEdge.head[dir];

                    if (oppositeNode.tree == root.tree) {
                        // opposite node is in the same tree
                        if (oppositeNode.isPlusNode()) {
                            double slack = currentEdge.slack;
                            if (!oppositeNode.isProcessed) {
                                slack += branchEps;
                            }
                            if (2 * criticalEps > slack || criticalEdge == null) {
                                flag = SHRINK;
                                criticalEps = slack / 2;
                                criticalEdge = currentEdge;
                                criticalDir = dir;
                                if (criticalEps <= branchEps) {
                                    // found a tight (+, +) in-tree edge to shrink => go out of the
                                    // loop
                                    primalOperation = true;
                                    break;
                                }
                            }
                        }

                    } else if (oppositeNode.isPlusNode()) {
                        // current edge is a (+, +) cross-tree edge
                        if (criticalEps >= currentEdge.slack || criticalEdge == null) {
                            //
                            flag = AUGMENT;
                            criticalEps = currentEdge.slack;
                            criticalEdge = currentEdge;
                            criticalDir = dir;
                            if (criticalEps <= branchEps) {
                                // found a tight (+, +) cross-tree edge to augment
                                primalOperation = true;
                                break;
                            }
                        }

                    } else {
                        // opposite node is an infinity node since all other trees contain only one
                        // "+" node
                        handleInfinityEdgeInit(heap, currentEdge, dir, branchEps, criticalEps);
                    }
                }
                if (primalOperation) {
                    // finish processing incident edges
                    while (iterator.hasNext()) {
                        iterator.next().slack += branchEps;
                    }
                    // exit the loop since we can perform shrink or augment operation
                    break;
                } else {
                    /*
                     * Move currentNode to the next unprocessed "+" node in the tree, growing the
                     * tree if it is possible. Start a new branch if all nodes have been processed.
                     * Exit the loop if the slack of fibHeap.min().getData() is >= than the slack of
                     * critical edge (in this case we can perform primal operation after updating
                     * the duals).
                     */
                    if (currentNode.firstTreeChild != null) {
                        // move to the next grandchild
                        currentNode = currentNode.firstTreeChild.getOppositeMatched();
                    } else {
                        // try to find another unprocessed node
                        while (currentNode != branchRoot && currentNode.treeSiblingNext == null) {
                            currentNode = currentNode.getTreeParent();
                        }
                        if (currentNode.isMinusNode()) {
                            // found an unprocessed node
                            currentNode = currentNode.treeSiblingNext.getOppositeMatched();
                        } else if (currentNode == branchRoot) {
                            // we've processed all nodes in the current branch
                            BlossomVEdge minSlackEdge =
                                heap.isEmpty() ? null : heap.findMin().getValue();
                            if (minSlackEdge == null || minSlackEdge.slack >= criticalEps) {
                                // can perform primal operation after updating duals
                                if (DEBUG) {
                                    System.out.println("Now current eps = " + criticalEps);
                                }
                                if (criticalEps > NO_PERFECT_MATCHING_THRESHOLD) {
                                    throw new IllegalArgumentException(NO_PERFECT_MATCHING);
                                }
                                branchEps = criticalEps;
                                break;
                            } else {
                                // grow minimum slack edge
                                if (DEBUG) {
                                    System.out.println("Growing an edge " + minSlackEdge);
                                }
                                int dirToFreeNode = minSlackEdge.head[0].isInfinityNode() ? 0 : 1;
                                currentNode = minSlackEdge.head[1 - dirToFreeNode];

                                BlossomVNode minusNode = minSlackEdge.head[dirToFreeNode];
                                removeFromHeap(minusNode);
                                minusNode.label = MINUS;
                                currentNode.addChild(minusNode, minSlackEdge, true);
                                branchEps = minSlackEdge.slack; // set new eps of the tree

                                BlossomVNode plusNode = minusNode.getOppositeMatched();
                                if (plusNode.bestEdge != null) {
                                    removeFromHeap(plusNode);
                                }
                                plusNode.label = PLUS;
                                minusNode.addChild(plusNode, minusNode.matched, true);

                                if (DEBUG) {
                                    System.out.println(
                                        "New branch root is " + plusNode + ", eps = " + branchEps);
                                }
                                // Start a new branch
                                currentNode = branchRoot = plusNode;
                            }
                        }
                    }
                }
            }

            // update duals
            updateDuals(heap, root, branchEps);

            // apply primal operation
            BlossomVNode from = criticalEdge.head[1 - criticalDir];
            BlossomVNode to = criticalEdge.head[criticalDir];
            if (flag == SHRINK) {
                shrinkInit(criticalEdge, root);
            } else {
                augmentBranchInit(root, from, criticalEdge);
                if (to.isOuter) {
                    // this node doesn't belong to a 1/2-values odd circuit
                    augmentBranchInit(to, to, criticalEdge); // to is the root of the opposite tree
                } else {
                    // this node belongs to a 1/2-values odd circuit
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
     * Enum for specifying the primal operation to perform with critical edge during fractional
     * matching initialization
     */
    enum Action
    {
        NONE,
        SHRINK,
        AUGMENT,
    }
}
