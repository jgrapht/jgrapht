/*
 * (C) Copyright 2018-2021, by Timofey Chudakov and Contributors.
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
package org.jgrapht.alg.planar;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

/**
 * An implementation of the Boyer-Myrvold planarity testing algorithm. This class determines whether
 * an input graph is planar or not. If the graph is planar, the algorithm provides a
 * <a href="https://en.wikipedia.org/wiki/Graph_embedding#Combinatorial_embedding">combinatorial
 * embedding</a> of the graph, which is represented as a clockwise ordering of the edges of the
 * graph. Otherwise, the algorithm provides a
 * <a href="https://en.wikipedia.org/wiki/Kuratowski%27s_theorem#Kuratowski_subgraphs"> Kuratowski
 * subgraph</a> as a certificate. Both embedding of the graph and Kuratowski subdivision are
 * computed lazily, meaning that the call to the {@link BoyerMyrvoldPlanarityInspector#isPlanar()}
 * does spend time only on the planarity testing. All of the operations of this algorithm (testing,
 * embedding and Kuratowski subgraph extraction) run in linear time.
 * <p>
 * A <a href="https://en.wikipedia.org/wiki/Planar_graph">planar graph</a> is a graph, which can be
 * drawn in two-dimensional space without any of its edges crossing. According to the
 * <a href="https://en.wikipedia.org/wiki/Kuratowski%27s_theorem">Kuratowski theorem</a>, a graph is
 * planar if and only if it doesn't contain a subdivision of the $K_{3,3}$ or $K_{5}$ graphs.
 * <p>
 * The Boyer-Myrvold planarity testing algorithm was originally described in: <i>Boyer, John amp;
 * Myrvold, Wendy. (2004). On the Cutting Edge: Simplified O(n) Planarity by Edge Addition. J. Graph
 * Algorithms Appl.. 8. 241-273. 10.7155/jgaa.00091. </i>. We refer to this paper for the complete
 * description of the Boyer-Myrvold algorithm
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 */
public class BoyerMyrvoldPlanarityInspector<V, E>
    implements
    PlanarityTestingAlgorithm<V, E>
{
    /**
     * Whether to print debug messages
     */
    private static final boolean DEBUG = false;
    /**
     * Whether to print Kuratowski case distinction messages
     */
    private static final boolean PRINT_CASES = false;
    /**
     * The graph we're testing planarity of
     */
    private Graph<V, E> graph;
    /**
     * The number of vertices in the {@code graph}
     */
    private int n;
    /**
     * The resulting combinatorial embedding. This value is computed only after the first call to
     * the {@link BoyerMyrvoldPlanarityInspector#getEmbedding()}
     */
    private Embedding<V, E> embedding;
    /**
     * The subgraph of the {@code graph}, which is a Kuratowski subdivision. This subgraph certifies
     * the nonplanarity of the graph.
     */
    private Graph<V, E> kuratowskiSubdivision;
    /**
     * List of the vertices of the {@code graph} in their internal representation. After the
     * orientation of the {@code graph} and edge sorting, nodes in this list are sorted according to
     * their dfs indexes
     */
    private List<Node> nodes;
    /**
     * List of the dfs tree roots of the {@code graph}. This list has length more than 1 if the
     * input {@code graph} isn't connected
     */
    private List<Node> dfsTreeRoots;
    /**
     * List of the virtual biconnected component roots. Initially, a virtual biconnected component
     * root is created for every node in the {@code graph}, except for the dfs roots. These
     * component roots don't belong to the {@code graph}. At each step of the algorithm, every
     * biconnected component has its own unique component root.
     */
    private List<Node> componentRoots;
    /**
     * The stack containing merge information for every consecutive pair of biconnected components
     * on the path to the back edge source. After all the biconnected components are merged, this
     * stack is cleared
     */
    private List<MergeInfo> stack;
    /**
     * The node $v$, which has an unembedded back edge incident to it.
     */
    private Node failedV;
    /**
     * Whether the planarity of the {@code graph} has been tested already
     */
    private boolean tested;
    /**
     * Whether the graph is planar or not. Is valid, if {@code tested} is {@code true}
     */
    private boolean planar;

    /**
     * Creates new instance of the planarity testing algorithm for the {@code graph}. The input
     * graph can't be null.
     *
     * @param graph the graph to test the planarity of
     */
    public BoyerMyrvoldPlanarityInspector(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph, "Graph can't be null");
        this.n = graph.vertexSet().size();
        this.nodes = new ArrayList<>(n);
        this.dfsTreeRoots = new ArrayList<>();
        this.componentRoots = new ArrayList<>(n);
        this.stack = new ArrayList<>();
    }

    /**
     * Creates a new node by converting the {@code graphVertex} to the internal node representation.
     *
     * @param vertexMap the map from vertices of the {@code graph} to their internal representation
     * @param graphVertex the vertex of the {@code graph} we're processing
     * @param edge the parent edge of the {@code graphVertex}, is {@code null} for dfs tree roots
     * @param parent the parent node of the {@code graphVertex}
     * @param dfsIndex the dfs index of the {@code graphVertex}
     * @return the newly created node
     */
    private Node createNewNode(
        Map<V, Node> vertexMap, V graphVertex, E edge, Node parent, int dfsIndex)
    {
        Node child;
        if (parent == null) {
            child = new Node(graphVertex, dfsIndex, 0, null, null);
            child.outerFaceNeighbors[0] = child.outerFaceNeighbors[1] = child;
            dfsTreeRoots.add(child);
        } else {
            Edge treeEdge = new Edge(edge, parent);
            Node componentRoot = new Node(parent.dfsIndex, treeEdge);
            child = new Node(graphVertex, dfsIndex, parent.height + 1, componentRoot, treeEdge);
            treeEdge.target = child;

            componentRoots.add(componentRoot);

            parent.treeEdges.add(treeEdge);

            child.outerFaceNeighbors[0] = child.outerFaceNeighbors[1] = componentRoot;
            componentRoot.outerFaceNeighbors[0] = componentRoot.outerFaceNeighbors[1] = child;
        }
        nodes.add(child);
        vertexMap.put(graphVertex, child);
        return child;
    }

    /**
     * Orients the input graph according to its dfs traversal by creating a dfs tree. Computes the
     * least ancestors and lowpoints of the nodes
     *
     * @param vertexMap the map from {@code graph} vertices to their internal representatives
     * @param startGraphVertex the node to start the traversal from (this is a dfs tree root).
     * @param currentDfsIndex the dfs index of the {@code startGraphVertex}
     * @return the {@code currentDfsIndex} + number of nodes in the traversed subtree
     */
    private int orientDfs(Map<V, Node> vertexMap, V startGraphVertex, int currentDfsIndex)
    {
        List<OrientDfsStackInfo> stack = new ArrayList<>();
        stack.add(new OrientDfsStackInfo(startGraphVertex, null, null, false));
        while (!stack.isEmpty()) {
            OrientDfsStackInfo info = stack.remove(stack.size() - 1);

            if (info.backtrack) {
                Node current = vertexMap.get(info.current);
                current.leastAncestor = current.lowpoint = current.dfsIndex;
                for (Edge backEdge : current.backEdges) {
                    current.leastAncestor =
                        Math.min(current.leastAncestor, backEdge.target.dfsIndex);
                }
                for (Edge treeEdge : current.treeEdges) {
                    current.lowpoint = Math.min(current.lowpoint, treeEdge.target.lowpoint);
                }
                current.lowpoint = Math.min(current.lowpoint, current.leastAncestor);
            } else {
                if (vertexMap.containsKey(info.current)) {
                    // other dfs branch has reached this vertex earlier
                    continue;
                }
                stack.add(new OrientDfsStackInfo(info.current, info.parent, info.parentEdge, true));
                Node current = createNewNode(
                    vertexMap, info.current, info.parentEdge, vertexMap.get(info.parent),
                    currentDfsIndex);
                ++currentDfsIndex;
                for (E e : graph.edgesOf(info.current)) {
                    V opposite = Graphs.getOppositeVertex(graph, e, info.current);
                    if (vertexMap.containsKey(opposite)) {
                        // back edge or parent edge
                        Node oppositeNode = vertexMap.get(opposite);
                        if (opposite.equals(info.parent)) {
                            continue;
                        }
                        Edge backEdge = new Edge(e, current, oppositeNode);
                        oppositeNode.downEdges.add(backEdge);
                        current.backEdges.add(backEdge);
                    } else {
                        // possible tree edge
                        stack.add(new OrientDfsStackInfo(opposite, current.graphVertex, e, false));
                    }
                }
            }
        }
        return currentDfsIndex;
    }

    /**
     * Iteratively start an orienting dfs from every {@code graph} vertex that hasn't been visited
     * yet. After orienting the graph, sorts the nodes by their lowpoints and adds them to the
     * {@code separatedDfsChildList}
     */
    private void orient()
    {
        Map<V, Node> visited = new HashMap<>();
        int currentDfsIndex = 0;
        for (V vertex : graph.vertexSet()) {
            if (!visited.containsKey(vertex)) {
                currentDfsIndex = orientDfs(visited, vertex, currentDfsIndex);
            }
        }
        sortVertices();
    }

    /**
     * Performs sorting of the vertices by their lowpoints and adding them to the
     * {@code separatedDfsChildList}
     */
    private void sortVertices()
    {
        List<List<Node>> sorted = new ArrayList<>(Collections.nCopies(n, null));
        for (Node node : nodes) {
            int lowpoint = node.lowpoint;
            if (sorted.get(lowpoint) == null) {
                sorted.set(lowpoint, new ArrayList<>());
            }
            sorted.get(lowpoint).add(node);
        }
        int i = 0;
        for (List<Node> list : sorted) {
            if (i >= n) {
                break;
            }
            if (list != null) {
                for (Node node : list) {
                    nodes.set(i++, node);
                    if (node.parentEdge != null) {
                        node.listNode =
                            node.parentEdge.source.separatedDfsChildList.addElementLast(node);
                    }
                }
            }
        }
    }

    /**
     * Lazily tests the planarity of the graph. The implementation below is close to the code
     * presented in the original paper
     *
     * @return true if the graph is planar, false otherwise
     */
    private boolean lazyTestPlanarity()
    {
        if (!tested) {
            tested = true;

            orient();
            if (DEBUG) {
                printState();
                System.out.println("Start testing planarity");
            }
            for (int currentNode = n - 1; currentNode >= 0; currentNode--) {
                Node current = nodes.get(currentNode);
                if (DEBUG) {
                    System.out.printf("Current vertex is %s\n", current.toString(false));
                }
                for (Edge downEdge : current.downEdges) {
                    walkUp(downEdge.source, current, downEdge);
                }
                for (Edge treeEdge : current.treeEdges) {
                    walkDown(treeEdge.target.initialComponentRoot);
                }

                for (Edge downEdge : current.downEdges) {
                    if (!downEdge.embedded) {
                        failedV = current;
                        return planar = false;
                    }
                }
            }
            planar = true;
        }
        return planar;
    }

    /**
     * Merges the last two biconnected components using the info stored on top of the stack. The key
     * goal of this method is to merge the outer faces of the two components and to merge the
     * embedded edges of the child component root with the embedded edges of the component parent
     * node.
     */
    private void mergeBiconnectedComponent()
    {
        MergeInfo info = stack.get(stack.size() - 1);
        stack.remove(stack.size() - 1);
        if (DEBUG) {
            System.out.printf("\nMerging biconnected component, info: %s\n", info.toString());
        }
        Node virtualRoot = info.child;
        if (info.isInverted()) {
            virtualRoot.swapNeighbors();
        }
        Node root = info.parent;
        Node virtualRootChild = virtualRoot.parentEdge.target;

        root.pertinentRoots.removeNode(virtualRoot.listNode);
        root.separatedDfsChildList.removeNode(virtualRootChild.listNode);

        root
            .mergeChildEdges(
                virtualRoot.embedded, info.vIn, info.vOut, info.parentNext, virtualRoot.parentEdge);

        root.substituteAnother(info.parentNext, info.childPrev);
        info.childPrev.substitute(virtualRoot, root);
        virtualRoot.outerFaceNeighbors[0] = virtualRoot.outerFaceNeighbors[1] = null;
    }

    /**
     * Embeds the back edge {@code edge} into the list of embedded edges of the source and the
     * virtual target of the edge such that the {@code childPrev} belongs to the new inner face.
     * This method also takes care of modifying the boundary of the outer face accordingly
     *
     * @param root the component root
     * @param entryDir the component entry direction
     * @param edge the edge to embed
     * @param childPrev the neighbor of the source of the edge that should belong to the inner face
     * @return a circulator starting from the edge's source
     */
    private OuterFaceCirculator embedBackEdge(Node root, int entryDir, Edge edge, Node childPrev)
    {
        if (DEBUG) {
            System.out.printf("Embedding back edge %s\n", edge.toString());
        }
        assert !edge.embedded;
        if (entryDir == 0) {
            root.embedded.addLast(edge);
        } else {
            root.embedded.addFirst(edge);
        }
        Node child = edge.source;
        child.embedBackEdge(edge, childPrev);
        child.edgeToEmbed = null;
        child.backEdgeFlag = n;
        edge.embedded = true;

        child.substitute(childPrev, root);
        root.outerFaceNeighbors[entryDir] = child;
        Node next = child.nextOnOuterFace(root);
        return new OuterFaceCirculator(next, child);
    }

    /**
     * Embeds a short-circuit edge from the {@code componentRoot} to the current node of the
     * {@code circulator}. Changes the outer face accordingly
     *
     * @param componentRoot the component root
     * @param entryDir the direction used to enter the component
     * @param circulator a circulator to the source of the new edge
     */
    private void embedShortCircuit(Node componentRoot, int entryDir, OuterFaceCirculator circulator)
    {
        Node current = circulator.getCurrent(), prev = circulator.getPrev();
        Edge shortCircuit = new Edge(current, componentRoot.getParent());
        if (entryDir == 0) {
            componentRoot.embedded.addLast(shortCircuit);
            componentRoot.outerFaceNeighbors[0] = current;
        } else {
            componentRoot.embedded.addFirst(shortCircuit);
            componentRoot.outerFaceNeighbors[1] = current;
        }
        current.embedBackEdge(shortCircuit, prev);
        current.substitute(prev, componentRoot);
        if (DEBUG) {
            System.out.printf("Embedding short circuit edge: %s\n", shortCircuit.toString());
            printState();
        }
    }

    /**
     * The walkdown procedure from the original paper. Either embeds all of the back edges in the
     * subtree rooted at the child of the {@code componentRoot} or identifies the back edges which
     * can be used to extract a Kuratowski subdivision. Iteratively traverses the tree of the
     * biconnected component and descends only to the pertinent components. This procedure is also
     * responsible for embedding short-circuit edges to make the algorithm run in linear time in the
     * worst case.
     *
     * @param componentRoot the root of the component to start the walkdown from
     */
    private void walkDown(Node componentRoot)
    {
        if (DEBUG) {
            System.out.printf("\nStart walk down on node %s\n", componentRoot.toString(true));
        }
        for (int componentEntryDir = 0; componentEntryDir < 2 && stack.isEmpty();
            componentEntryDir++)
        {
            if (DEBUG) {
                System.out.println("\nNew traversal direction = " + componentEntryDir);
            }
            int currentComponentEntryDir = componentEntryDir;
            OuterFaceCirculator circulator = componentRoot.iterator(currentComponentEntryDir);
            for (Node current = circulator.next(); current != componentRoot;) {
                if (DEBUG) {
                    System.out.printf("Current = %s\n", current.toString());
                }
                if (current.hasBackEdgeWrtTo(componentRoot)) {
                    Node childPrev = circulator.getPrev();
                    while (!stack.isEmpty()) {
                        mergeBiconnectedComponent();
                        if (DEBUG) {
                            printState();
                        }
                    }
                    circulator = embedBackEdge(
                        componentRoot, componentEntryDir, current.edgeToEmbed, childPrev);
                    if (DEBUG) {
                        printState();
                        printBiconnectedComponent(current);
                    }
                }
                if (!current.pertinentRoots.isEmpty()) {
                    int parentComponentEntryDir = currentComponentEntryDir;
                    Node root = current.pertinentRoots.getFirst();

                    if (DEBUG) {
                        System.out.printf("Descending to the root = %s\n", root.toString());
                    }
                    OuterFaceCirculator ccwCirculator =
                        getActiveSuccessorOnOuterFace(root, componentRoot, 0);
                    Node ccwActiveNode = ccwCirculator.getCurrent();
                    OuterFaceCirculator cwCirculator =
                        getActiveSuccessorOnOuterFace(root, componentRoot, 1);
                    Node cwActiveNode = cwCirculator.getCurrent();

                    if (ccwActiveNode.isInternallyActiveWrtTo(componentRoot)) {
                        currentComponentEntryDir = 0;
                    } else if (cwActiveNode.isInternallyActiveWrtTo(componentRoot)) {
                        currentComponentEntryDir = 1;
                    } else if (ccwActiveNode.isPertinentWrtTo(componentRoot)) {
                        currentComponentEntryDir = 0;
                    } else {
                        currentComponentEntryDir = 1;
                    }

                    if (currentComponentEntryDir == 0) {
                        stack
                            .add(
                                new MergeInfo(
                                    current, circulator.next(), root, root.outerFaceNeighbors[1],
                                    parentComponentEntryDir, currentComponentEntryDir));
                        current = ccwActiveNode;
                        circulator = ccwCirculator;

                        if (!cwActiveNode.hasRootNeighbor()) {
                            embedShortCircuit(root, 1, cwCirculator);
                        }
                    } else {
                        stack
                            .add(
                                new MergeInfo(
                                    current, circulator.next(), root, root.outerFaceNeighbors[0],
                                    parentComponentEntryDir, currentComponentEntryDir));
                        current = cwActiveNode;
                        circulator = cwCirculator;

                        if (!ccwActiveNode.hasRootNeighbor()) {
                            embedShortCircuit(root, 0, ccwCirculator);
                        }
                    }

                } else if (current.isInactiveWrtTo(componentRoot)) {
                    current = circulator.next();
                } else {
                    // current vertex is externally active
                    if (DEBUG) {
                        System.out.println("Current vertex is externally active, stop\n");
                    }
                    if (!current.hasRootNeighbor() && stack.isEmpty()) {
                        embedShortCircuit(componentRoot, componentEntryDir, circulator);
                    }
                    break;
                }
            }
        }
    }

    /**
     * The walkup procedure from the original paper. Identifies the pertinent subgraph of the graph
     * by going up the dfs tree from the {@code start} node to the {@code end} node using the edge
     * {@code edge}
     *
     * @param start the node to start the walkup from
     * @param end the node currently processed by the main loop of the algorithm
     * @param edge a back edge to embed
     */
    private void walkUp(Node start, Node end, Edge edge)
    {
        if (DEBUG) {
            System.out.printf("\nStart walk up on edge = %s\n", edge.toString());
        }
        int visited = end.dfsIndex;

        start.backEdgeFlag = visited;
        start.edgeToEmbed = edge;

        Node x = start.outerFaceNeighbors[0], y = start.outerFaceNeighbors[1], xPrev = start,
            yPrev = start;
        start.visited = visited;
        while (x != end && !x.isVisitedWrtTo(end) && !y.isVisitedWrtTo(end)) {
            if (DEBUG) {
                System.out.printf("Current x = %s\nCurrent y = %s\n", x.toString(), y.toString());
            }
            x.visited = y.visited = visited;

            Node root = null;
            if (x.isRootVertex()) {
                root = x;
            } else if (y.isRootVertex()) {
                root = y;
            }
            if (root != null) {
                if (DEBUG) {
                    System.out.printf("Found root = %s\n", root.toString());
                }
                Node rootChild = root.parentEdge.target;
                Node newStart = root.parentEdge.source;
                if (newStart != end) {
                    if (rootChild.lowpoint < end.dfsIndex) {
                        // the component in externally active
                        root.listNode = newStart.pertinentRoots.addElementLast(root);
                    } else {
                        // the component is internally active
                        root.listNode = newStart.pertinentRoots.addElementFirst(root);
                    }
                } else {
                    break;
                }
                newStart.visited = visited;
                xPrev = yPrev = newStart;
                x = newStart.outerFaceNeighbors[0];
                y = newStart.outerFaceNeighbors[1];

            } else {
                Node t = x;
                x = x.nextOnOuterFace(xPrev);
                xPrev = t;

                t = y;
                y = y.nextOnOuterFace(yPrev);
                yPrev = t;
            }
        }
    }

    /**
     * Lazily computes a combinatorial embedding of the {@code graph} by removing all the
     * short-circuit edges and properly orienting the edges around the nodes.
     *
     * @return a combinatorial embedding of the {@code graph}
     */
    private Embedding<V, E> lazyComputeEmbedding()
    {
        lazyTestPlanarity();
        if (!planar) {
            throw new IllegalArgumentException(
                "Input graph is not planar, can't compute graph embedding");
        }
        if (embedding == null) {
            for (Node dfsTreeRoot : dfsTreeRoots) {
                cleanUpDfs(dfsTreeRoot);
            }
            Map<V, List<E>> embeddingMap = new HashMap<>();
            for (Node node : nodes) {
                for (Node child : node.separatedDfsChildList) {
                    Node virtualRoot = child.initialComponentRoot;
                    node.embedded.append(virtualRoot.embedded);
                }
                List<E> embeddedEdges = new ArrayList<>(node.embedded.size());
                for (Edge edge : node.embedded) {
                    embeddedEdges.add(edge.graphEdge);
                }
                embeddingMap.put(node.graphVertex, embeddedEdges);
            }
            embedding = new EmbeddingImpl<>(graph, embeddingMap);
        }
        return embedding;
    }

    /**
     * Method for debug purposes, prints the boundary the {@code node} belongs to
     *
     * @param node a node on the outer face
     */
    private void printBiconnectedComponent(Node node)
    {
        StringBuilder builder = new StringBuilder(node.toString(false));
        OuterFaceCirculator circulator = node.iterator(0);
        Node current = circulator.next();
        Node stop = current;
        do {
            builder.append(" -> ").append(current.toString(false));
            current = circulator.next();
        } while (current != stop);
        System.out.println("Biconnected component after merge: " + builder.toString());
    }

    /**
     * Method for debug purposes, prints the state of the algorithm
     */
    private void printState()
    {
        System.out.println("\nPrinting state:");
        System.out.println("Dfs roots: " + dfsTreeRoots);
        System.out.println("Nodes:");
        for (Node node : nodes) {
            System.out.println(node.toString(true));
        }
        System.out.println("Virtual nodes:");
        for (Node node : componentRoots) {
            System.out.println(node.toString(true));
        }
        List<Edge> inverted = new ArrayList<>();
        for (Node node : nodes) {
            for (Edge edge : node.treeEdges) {
                if (edge.sign < 0) {
                    inverted.add(edge);
                }
            }
        }
        System.out.println("Inverted edges = " + inverted);
    }

    /**
     * Either finds and returns a circulator to the node on the boundary of the component, which
     * satisfies the {@code predicate} or returns a circulator to the {@code stop} node.
     *
     * @param predicate the condition the desired node should satisfy
     * @param start the node to start the search from
     * @param stop the node to end the search with
     * @param dir the direction to start the traversal in
     * @return a circulator to the node satisfying the {@code predicate} or to the {@code stop} node
     */
    private OuterFaceCirculator selectOnOuterFace(
        Predicate<Node> predicate, Node start, Node stop, int dir)
    {
        OuterFaceCirculator circulator = start.iterator(dir);
        Node current = circulator.next();
        while (current != stop && !predicate.test(current)) {
            current = circulator.next();
        }
        return circulator;
    }

    /**
     * Returns an active node on the outer face in the direction {@code dir} starting from the
     * {@code start} node
     *
     * @param start the node to start the search from
     * @param v an ancestor of the {@code start}
     * @param dir the direction of the search
     * @return a circulator to the found node
     */
    private OuterFaceCirculator getActiveSuccessorOnOuterFace(Node start, Node v, int dir)
    {
        return selectOnOuterFace(n -> n.isActiveWrtTo(v), start, start, dir);
    }

    /**
     * Returns acirculator to the externally active node on the outer face between the {@code start}
     * and {@code end} nodes in the direction {@code dir}.
     *
     * @param start the node to start the search from
     * @param stop the node to end the search with
     * @param v an ancestor of the {@code start} and the {@code end}
     * @param dir the direction of the search
     * @return a circulator to the found node
     */
    private OuterFaceCirculator getExternallyActiveSuccessorOnOuterFace(
        Node start, Node stop, Node v, int dir)
    {
        return selectOnOuterFace(n -> n.isExternallyActiveWrtTo(v), start, stop, dir);
    }

    /**
     * Returns a component root of component the {@code node} is contained in.
     *
     * @param node a node in the partially embedded graph
     * @return a component root of the component the {@code node} belongs to
     */
    private Node getComponentRoot(Node node)
    {
        return selectOnOuterFace(Node::isRootVertex, node, node, 0).getCurrent();
    }

    /**
     * Adds the edges on the path from the {@code startEdge} up to the node {@code stop} to the set
     * {@code edges}
     *
     * @param edges the set to add the path edges to
     * @param startEdge the edge to start from
     * @param stop the last node on the path
     */
    private void addPathEdges(Set<Edge> edges, Edge startEdge, Node stop)
    {
        edges.add(startEdge);
        Node current = startEdge.source;
        while (current != stop) {
            edges.add(current.parentEdge);
            current = current.getParent();
        }
    }

    /**
     * Adds the edges between the {@code start} and the {@code end} to the set {@code edges}
     *
     * @param edges the set to add the path edges to to
     * @param start the node to start from
     * @param stop the node to end with
     */
    private void addPathEdges(Set<Edge> edges, Node start, Node stop)
    {
        if (start != stop) {
            addPathEdges(edges, start.parentEdge, stop);
        }
    }

    /**
     * Searches a back edge which target has a height smaller than {@code heightMax}
     *
     * @param current the node to start from
     * @param heightMax an upper bound on the height of the desired back edge
     * @return the desired back edge or null, if no such edge exist
     */
    private Edge searchEdge(Node current, int heightMax)
    {
        return searchEdge(current, heightMax, null);
    }

    /**
     * Searches a back edge which target has a height smaller than {@code heightMax}
     *
     * @param current the node to start from
     * @param heightMax an upper bound on the height of the desired back edge
     * @param forbiddenEdge an edge the desired edge should not be equal to
     * @return the desired back edge or null, if no such edge exist
     */
    private Edge searchEdge(Node current, int heightMax, Edge forbiddenEdge)
    {
        Predicate<Edge> isNeeded = e -> {
            if (forbiddenEdge == e) {
                return false;
            }
            return e.target.height < heightMax;
        };
        return searchEdge(current, isNeeded);
    }

    /**
     * Generically searches an edge in the subtree rooted at the {@code current}, which doesn't
     * include the children of the {@code current} that have beem merged to the parent biconnected
     * component.
     *
     * @param current the node to start the searh from
     * @param isNeeded the predicate which the desired edge should satisfy
     * @return an edge which satisfies the {@code predicate}, or null if such an edge doesn't exist
     */
    private Edge searchEdge(Node current, Predicate<Edge> isNeeded)
    {
        for (Node node : current.separatedDfsChildList) {
            Edge result = searchSubtreeDfs(node, isNeeded);
            if (result != null) {
                return result;
            }
        }
        for (Edge edge : current.backEdges) {
            if (isNeeded.test(edge)) {
                return edge;
            }
        }
        return null;
    }

    /**
     * Recursively searches all the subtree root at the node {@code start} to find an edge
     * satisfying the {@code predicate}.
     *
     * @param start the node to start the search from.
     * @param isNeeded a predicate, which the desired edge should satisfy
     * @return a desired edge, or null if no such edge exist.
     */
    private Edge searchSubtreeDfs(Node start, Predicate<Edge> isNeeded)
    {
        List<Node> stack = new ArrayList<>();
        stack.add(start);
        while (!stack.isEmpty()) {
            Node current = stack.remove(stack.size() - 1);

            for (Edge edge : current.backEdges) {
                if (isNeeded.test(edge)) {
                    return edge;
                }
            }
            for (Edge edge : current.treeEdges) {
                stack.add(edge.target);
            }
        }
        return null;
    }

    /**
     * Returns the highest of the two input node, i.e. the node with the greater height
     *
     * @param a a node in the dfs tree
     * @param b a node in the dfs tree
     * @return the highest of the two nodes
     */
    private Node highest(Node a, Node b)
    {
        return a.height > b.height ? a : b;
    }

    /**
     * Returns the lowest of the two input node, i.e. the node with the least height.
     *
     * @param a a node in the dfs tree
     * @param b a node in the dfs tree
     * @return the lowest of the two nodes
     */
    private Node lowest(Node a, Node b)
    {
        return a.height < b.height ? a : b;
    }

    /**
     * Iteratively sets a boundary height for the nodes on the outer face branch ending at the node
     * {@code w}.
     *
     * @param componentRoot the root of the component
     * @param w the end of the outer face branch
     * @param dir the direction to start the traversal in
     * @param delta a value in $\{+1, -1\}$ to set either positive or negative boundary height
     */
    private void setBoundaryDepth(Node componentRoot, Node w, int dir, int delta)
    {
        OuterFaceCirculator circulator = componentRoot.iterator(dir);
        Node current = circulator.next();
        int currentHeight = delta;
        while (current != w) {
            current.boundaryHeight = currentHeight;
            currentHeight += delta;
            current = circulator.next();
        }
    }

    /**
     * Clears the visited variable of all the nodes and component roots
     */
    private void clearVisited()
    {
        nodes.forEach(n -> n.visited = 0);
        componentRoots.forEach(n -> n.visited = 0);
    }

    /**
     * Generically searches a path from the {@code current} node to the first node satisfying the
     * {@code isFinish} predicate consisting of all the nodes satisfying the {@code canGo}
     * predicate. The key property of this method is that it searches the next edge on the path in
     * the clockwise order starting from the previous edge. The edges of the resulting path are
     * added to the {@code edges}.
     *
     * @param start the start node of the traversal
     * @param startPrev the previous edge of the start node
     * @param canGo specifies where the search can go
     * @param isFinish specifies what nodes are finish nodes
     * @param edges the list containing the resulting path
     * @return true if the search was successful, false otherwise
     */
    private boolean findPathDfs(
        Node start, Edge startPrev, Predicate<Node> canGo, Predicate<Node> isFinish,
        List<Edge> edges)
    {
        List<SearchInfo> stack = new ArrayList<>();
        stack.add(new SearchInfo(start, startPrev, false));
        while (!stack.isEmpty()) {
            SearchInfo info = stack.remove(stack.size() - 1);
            if (isFinish.test(info.current)) {
                edges.add(info.prevEdge);
                edges.remove(0);
                return true;
            }
            if (info.backtrack) {
                edges.remove(edges.size() - 1);
            } else {
                if (info.current.visited != 0) {
                    continue;
                }
                info.current.visited = 1;
                stack.add(new SearchInfo(info.current, info.prevEdge, true));
                edges.add(info.prevEdge);
                /*
                 * The iteration is performed in the reverse order since the infos are pushed on the
                 * stack and therefore will be processed in the again reverse order
                 */
                Iterator<Edge> iterator =
                    info.current.embedded.reverseCircularIterator(info.prevEdge);
                while (iterator.hasNext()) {
                    Edge currentEdge = iterator.next();
                    Node opposite = currentEdge.getOpposite(info.current);
                    if ((!canGo.test(opposite) || opposite.visited != 0)
                        && !isFinish.test(opposite))
                    {
                        continue;
                    }
                    stack.add(new SearchInfo(opposite, currentEdge, false));
                }
            }
        }
        return false;
    }

    /**
     * Finds the highest obstructing path in the component rooted at {@code componentRoot}. See the
     * original paper for the definition of the obstructing path. This method heavily relies on the
     * fact that the method
     * {@link BoyerMyrvoldPlanarityInspector#findPathDfs(Node, Edge, Predicate, Predicate, List)}
     * chooses the edges in the clockwise order.
     *
     * @param componentRoot the root of the component
     * @param w the node called {@code w} in the Kuratowski subdivision extraction phase.
     * @return the edges of the desired path as a list
     */
    private List<Edge> findHighestObstructingPath(Node componentRoot, Node w)
    {
        clearVisited();
        List<Edge> result = new ArrayList<>();
        OuterFaceCirculator circulator = componentRoot.iterator(0);
        Node current = circulator.next();
        while (current != w) {
            if (findPathDfs(
                current, current.embedded.getFirst(), n -> !n.marked, n -> n.boundaryHeight < 0,
                result))
            {
                return result;
            }
            current = circulator.next();
        }
        return result;
    }

    /**
     * Finishes the Kuratowski subdivision extraction by constructing the desired subgraph
     *
     * @param subdivision the edges in the Kuratowski subdivision
     * @return the Kuratowski subgraph of the {@code graph}
     */
    private Graph<V, E> finish(Set<Edge> subdivision)
    {
        Set<E> edgeSubset = new HashSet<>();
        Set<V> vertexSubset = new HashSet<>();
        subdivision.forEach(e -> {
            edgeSubset.add(e.graphEdge);
            vertexSubset.add(e.target.graphVertex);
            vertexSubset.add(e.source.graphVertex);
        });
        kuratowskiSubdivision = new AsSubgraph<>(graph, vertexSubset, edgeSubset);
        return kuratowskiSubdivision;
    }

    /**
     * Adds the edges on the outer face of the component rooted at {@code componentRoot} to the set
     * {@code edges}
     *
     * @param edges the set to add the edges to
     * @param componentRoot the root of the biconnected component
     */
    private void addBoundaryEdges(Set<Edge> edges, Node componentRoot)
    {
        OuterFaceCirculator circulator = componentRoot.iterator(0);
        Node current;
        do {
            Edge edge = circulator.edgeToNext();
            edge.source.marked = edge.target.marked = true;
            edges.add(edge);
            current = circulator.next();
        } while (current != componentRoot);
    }

    /**
     * Cleans up the dfs trees before the Kuratowski subdivision extraction phase
     */
    private void kuratowskiCleanUp()
    {
        for (Node dfsTreeRoot : dfsTreeRoots) {
            cleanUpDfs(dfsTreeRoot);
        }
        for (Node node : componentRoots) {
            if (node.outerFaceNeighbors[0] != null) {
                node.removeShortCircuitEdges();
                fixBoundaryOrder(node);
            }
        }
    }

    /**
     * Recursively cleans up the dfs tree rooted at the {@code dfsTreeRoot} my removing all the
     * short-circuit edges and properly orienting other embedded edges
     *
     * @param dfsTreeRoot the root of the dfs tree to clean up
     */
    private void cleanUpDfs(Node dfsTreeRoot)
    {
        List<Pair<Node, Integer>> stack = new ArrayList<>();
        stack.add(Pair.of(dfsTreeRoot, 1));
        while (!stack.isEmpty()) {
            Pair<Node, Integer> entry = stack.remove(stack.size() - 1);
            Node current = entry.getFirst();
            int sign = entry.getSecond();
            if (sign < 0) {
                current.embedded.invert();
            }
            current.removeShortCircuitEdges();
            for (Node node : current.separatedDfsChildList) {
                // all the components, that aren't merged, aren't inverted
                node.parentEdge.sign = sign;
            }

            for (Edge treeEdge : current.treeEdges) {
                stack.add(Pair.of(treeEdge.target, sign * treeEdge.sign));
            }
        }

    }

    /**
     * Orient the connections on the outer face of the component rooted at {@code componentRoot}
     * such that they are ordered
     *
     * @param componentRoot the root of the component to process
     */
    private void fixBoundaryOrder(Node componentRoot)
    {
        if (componentRoot.embedded.size() < 2) {
            return;
        }
        Node componentParent = componentRoot.getParent();
        Edge edgeToNext = componentRoot.embedded.getLast(),
            edgeToPrev = componentRoot.embedded.getFirst();
        Node next = edgeToNext.getOpposite(componentParent),
            prev = edgeToPrev.getOpposite(componentParent);

        componentRoot.outerFaceNeighbors[0] = next;
        componentRoot.outerFaceNeighbors[1] = prev;
        next.outerFaceNeighbors[1] = componentRoot;
        prev.outerFaceNeighbors[0] = componentRoot;
        Node current = componentRoot.outerFaceNeighbors[0];
        do {
            edgeToNext = current.embedded.getLast();
            edgeToPrev = current.embedded.getFirst();
            next = edgeToNext.getOpposite(current);
            prev = edgeToPrev.getOpposite(current);
            if (prev != componentParent) {
                current.outerFaceNeighbors[1] = prev;
            }
            if (next != componentParent) {
                current.outerFaceNeighbors[0] = next;
            }
            current = next;
        } while (current != componentParent);
    }

    /**
     * Removes the edges from the outer face from the {@code start} node to the {@code end} node in
     * the direction {@code dir} from the set {@code edges}
     *
     * @param start the start of the boundary path
     * @param end the end of the boundary path
     * @param dir the direction to take from the {@code start} node
     * @param edges the set of edges to modify
     */
    private void removeUp(Node start, Node end, int dir, Set<Edge> edges)
    {
        if (start == end) {
            return;
        }
        OuterFaceCirculator circulator = start.iterator(dir);
        Node next;
        do {
            Edge edge = circulator.edgeToNext();
            edges.remove(edge);
            next = circulator.next();
        } while (next != end);
    }

    /**
     * Effectively is a method for finding node {@code z} in the notations of the original paper.
     * The search proceeds in the reverse order of the path from the {@code backEdge} to the node
     * {@code w}
     *
     * @param w the start of the path down
     * @param backEdge the last edge on the path
     * @return the desired node {@code z} or null if the source of the {@code backEdge} is equal to
     *         {@code w}
     */
    private Node getNextOnPath(Node w, Edge backEdge)
    {
        if (backEdge.source == w) {
            return null;
        }
        Node prev = backEdge.source, current = backEdge.source.getParent();
        while (current != w) {
            prev = current;
            current = current.getParent();
        }
        return prev;
    }

    /**
     * Finds a path from some intermediate nodes on the path represented by the list {@code path} to
     * the node {@code v}. The path to {@code v} certainly doesn't exist if the list {@code path}
     * has size 1, because we're looking for a path from some intermediate node
     *
     * @param path the path between left and right outer face branches
     * @param v the parent of the biconnected component
     * @return the path edges in a list, which can be empty
     */
    private List<Edge> findPathToV(List<Edge> path, Node v)
    {
        clearVisited();
        int i = 0;
        Edge currentEdge = path.get(i);
        Node current =
            currentEdge.source.boundaryHeight != 0 ? currentEdge.target : currentEdge.source;
        List<Edge> result = new ArrayList<>();
        while (i < path.size() - 1) {
            if (findPathDfs(current, currentEdge, n -> !n.marked, n -> n == v, result)) {
                return result;
            }
            ++i;
            currentEdge = path.get(i);
            current = currentEdge.getOpposite(current);
        }
        return result;
    }

    /**
     * Checks whether the first node {@code a} is strictly higher than nodes {@code b} and {@code c}
     *
     * @param a a node in the dfs tree
     * @param b a node in the dfs tree
     * @param c a node in the dfs tree
     * @return true if the first node in strictly higher that other node, false otherwise
     */
    private boolean firstStrictlyHigher(Node a, Node b, Node c)
    {
        return a.height > b.height && a.height > c.height;
    }

    /**
     * Checks whether the biconnected component rooted at {@code componentRoot} can be used to
     * extract a Kuratowski subdivision. It can be used in the case there is one externally active
     * node on each branch of the outer face and there is a pertinent node on the lower part of the
     * outer face between these two externally active nodes.
     *
     * @param componentRoot the root of the biconnected component
     * @param v an ancestor of the nodes in the biconnected component
     * @return an unembedded back edge, which target is {@code v} and which can be used to extract a
     *         Kuratowski subdivision, or {@code null} is no such edge exist for this biconnected
     *         component
     */
    private Edge checkComponentForFailedEdge(Node componentRoot, Node v)
    {
        OuterFaceCirculator firstDir =
            getExternallyActiveSuccessorOnOuterFace(componentRoot, componentRoot, v, 0);
        Node firstDirNode = firstDir.getCurrent();
        OuterFaceCirculator secondDir =
            getExternallyActiveSuccessorOnOuterFace(componentRoot, componentRoot, v, 1);
        Node secondDirNode = secondDir.getCurrent();
        if (firstDirNode != componentRoot && firstDirNode != secondDirNode) {
            Node current = firstDir.next();
            while (current != secondDirNode) {
                if (current.isPertinentWrtTo(v)) {
                    return searchEdge(current, e -> e.target == v && !e.embedded);
                }
                current = firstDir.next();
            }
        }
        return null;
    }

    /**
     * Finds an unembedded back edge to {@code v}, which can be used to extract the Kuratowski
     * subdivision. If the merge stack isn't empty, the last biconnected component processed by the
     * walkdown can be used to find such an edge, because walkdown descended to that component
     * (which means that component is pertinent) and couldn't reach a pertinent node. This can only
     * happen by encountering externally active nodes on both branches of the traversal. Otherwise,
     * be have look in all the child biconnected components to find an unembedded back edge. We're
     * guided by the fact that an edge can not be embedded only in the case both traversals of the
     * walkdown could reach all off the pertinent nodes. This in turn can happen only if both
     * traversals get stuck on externally active nodes.
     * <p>
     * <b>Note:</b> not every unembedded back edge can be used to extract a Kuratowski subdivision
     *
     * @param v the vertex which has an unembedded back edge incident to it
     * @return the found unembedded back edge which can be used to extract a Kuratowski subdivision
     */
    private Edge findFailedEdge(Node v)
    {
        if (stack.isEmpty()) {
            for (Node child : v.separatedDfsChildList) {
                Node componentRoot = child.initialComponentRoot;
                Edge result = checkComponentForFailedEdge(componentRoot, v);
                if (result != null) {
                    return result;
                }
            }
            return null; // should not happen in case node v has an incident unembedded back edge
        } else {
            MergeInfo info = stack.get(stack.size() - 1);
            return checkComponentForFailedEdge(info.child, v);
        }
    }

    /**
     * Lazily extracts a Kuratowski subdivision from the {@code graph}. The process of adding the
     * edges of the subdivision to the resulting set of edges had been made explicit for every case.
     *
     * @return a Kuratowski subgraph of the {@code graph}
     */
    private Graph<V, E> lazyExtractKuratowskiSubdivision()
    {
        if (kuratowskiSubdivision == null) {
            // remove short-circuit edges and orient all embedded lists clockwise
            kuratowskiCleanUp();
            if (DEBUG) {
                printState();
            }
            Set<Edge> subdivision = new HashSet<>();
            // find the needed unembedded back edge which can be used to find Kuratowski subgraph
            Edge failedEdge = findFailedEdge(failedV);
            assert failedEdge != null;
            /*
             * We're iteratively moving up traversing the outer faces of the biconnected components
             * to find externally active nodes x and y, which are on different branches of the outer
             * face. The way we're finding the nodes x and y helps us eliminate the case E_1
             * described in the original paper. This can be done because we always find the closest
             * to the node w externally active nodes x and y.
             */
            Node x, y, v = failedEdge.target, w = failedEdge.source, componentRoot;
            while (true) {
                componentRoot = getComponentRoot(w);
                x = getExternallyActiveSuccessorOnOuterFace(w, componentRoot, v, 1).getCurrent();
                y = getExternallyActiveSuccessorOnOuterFace(w, componentRoot, v, 0).getCurrent();
                if (x.isRootVertex()) {
                    w = x.getParent();
                } else if (y.isRootVertex()) {
                    w = y.getParent();
                } else {
                    componentRoot = getComponentRoot(w);
                    break;
                }
            }
            Edge xBackEdge = searchEdge(x, v.height);
            Edge yBackEdge = searchEdge(y, v.height);
            if (DEBUG) {
                System.out
                    .printf(
                        "Failed v = %s, failed edge = %s\n", failedV.toString(false),
                        failedEdge.toString());
                System.out.printf("x = %s, y = %s\n", x.toString(false), y.toString(false));
                System.out
                    .printf(
                        "xBackEdge = %s, yBackEdge = %s\n", xBackEdge.toString(),
                        yBackEdge.toString());
            }
            Node backLower = lowest(xBackEdge.target, yBackEdge.target);
            Node backHigher = highest(xBackEdge.target, yBackEdge.target);
            addPathEdges(subdivision, xBackEdge, x);
            addPathEdges(subdivision, yBackEdge, y);
            addBoundaryEdges(subdivision, componentRoot);

            if (componentRoot.getParent() != v) {
                // case A, v isn't a parent of the component we've found
                if (PRINT_CASES) {
                    System.out.println("Case A");
                }
                addPathEdges(subdivision, componentRoot.getParent(), backLower);
                addPathEdges(subdivision, failedEdge, w);
                return finish(subdivision);
            }
            // node z will be null only if the tree path from w to failedEdge is failedEdge itself
            Node z = getNextOnPath(w, failedEdge);
            Edge backEdge = null;
            if (z != null) {
                backEdge = searchSubtreeDfs(z, e -> e.target.height < v.height && e != failedEdge);
            }
            if (backEdge != null) {
                // case B
                if (PRINT_CASES) {
                    System.out.println("Case B");
                }
                addPathEdges(subdivision, backEdge, w);
                addPathEdges(subdivision, failedEdge, w);
                Node highest =
                    highest(xBackEdge.target, highest(yBackEdge.target, backEdge.target));
                Node lowest = lowest(xBackEdge.target, lowest(yBackEdge.target, backEdge.target));
                addPathEdges(subdivision, highest, lowest);
                return finish(subdivision);
            }
            /*
             * If we failed to either case A or B, we have to find a highest obstructing path and
             * then deal with cases C - E
             */
            setBoundaryDepth(componentRoot, w, 0, 1);
            setBoundaryDepth(componentRoot, w, 1, -1);

            assert x.boundaryHeight > 0;
            List<Edge> path = findHighestObstructingPath(componentRoot, w);
            assert !path.isEmpty();
            if (DEBUG) {
                System.out.println("Path = " + path);
            }

            Edge firstEdge = path.get(0);
            Edge lastEdge = path.get(path.size() - 1);
            Node firstNode =
                firstEdge.source.boundaryHeight > 0 ? firstEdge.source : firstEdge.target;
            Node lastNode = lastEdge.source.boundaryHeight < 0 ? lastEdge.source : lastEdge.target;
            if (firstNode.boundaryHeight < x.boundaryHeight
                || lastNode.boundaryHeight > y.boundaryHeight)
            {
                // case C, either the first node or the last node of the path is higher than x or y
                // respectively
                if (PRINT_CASES) {
                    System.out.println("Case C");
                }
                Node removeStart;
                if (lastNode.boundaryHeight > y.boundaryHeight) {
                    removeStart = firstNode.boundaryHeight < x.boundaryHeight ? firstNode : x;
                    removeUp(removeStart, componentRoot, 1, subdivision);
                } else {
                    removeUp(y, componentRoot, 0, subdivision);
                }
                addPathEdges(subdivision, failedEdge, w);
                subdivision.addAll(path);
                addPathEdges(subdivision, v, backLower);
                return finish(subdivision);
            }
            path.forEach(e -> e.source.marked = e.target.marked = true);
            List<Edge> pathToV = findPathToV(path, v);
            if (!pathToV.isEmpty()) {
                // case D, we have a path to the node v
                if (PRINT_CASES) {
                    System.out.println("Case D");
                }
                removeUp(x, componentRoot, 1, subdivision);
                removeUp(y, componentRoot, 0, subdivision);
                subdivision.addAll(path);
                subdivision.addAll(pathToV);
                addPathEdges(subdivision, v, backLower);
                addPathEdges(subdivision, failedEdge, w);
                return finish(subdivision);
            }
            Edge externallyActive = searchEdge(w, v.height, failedEdge);
            assert externallyActive != null;
            if (DEBUG) {
                System.out.printf("Externally active edge = %s\n", externallyActive.toString());
            }
            addPathEdges(subdivision, externallyActive, w);
            if (firstStrictlyHigher(externallyActive.target, xBackEdge.target, yBackEdge.target)) {
                // case E_2, equivalent to A
                if (PRINT_CASES) {
                    System.out.println("Case E_2");
                }
                addPathEdges(subdivision, componentRoot.getParent(), backLower);
            } else if (firstStrictlyHigher(
                xBackEdge.target, yBackEdge.target, externallyActive.target))
            {
                // case E_2, u_x is higher
                if (PRINT_CASES) {
                    System.out.println("Case E_2, u_x is higher");
                }
                removeUp(componentRoot, x, 0, subdivision);
                removeUp(w, lastNode, 0, subdivision);
                subdivision.addAll(path);
                addPathEdges(subdivision, failedEdge, w);
                addPathEdges(subdivision, v, lowest(backLower, externallyActive.target));
            } else if (firstStrictlyHigher(
                yBackEdge.target, xBackEdge.target, externallyActive.target))
            {
                // case E_2, u_y is higher
                if (PRINT_CASES) {
                    System.out.println("Case E_2, u_y is higher");
                }
                removeUp(y, componentRoot, 0, subdivision);
                removeUp(firstNode, w, 0, subdivision);
                subdivision.addAll(path);
                addPathEdges(subdivision, failedEdge, w);
                addPathEdges(subdivision, v, lowest(backLower, externallyActive.target));
            } else if (firstNode.boundaryHeight > x.boundaryHeight) {
                // case E_4, p_x is lower than x
                if (PRINT_CASES) {
                    System.out.println("Case E_3, p_x is lower than x");
                }
                removeUp(w, lastNode, 0, subdivision);
                subdivision.addAll(path);
                addPathEdges(subdivision, failedEdge, w);
                addPathEdges(
                    subdivision, highest(backHigher, externallyActive.target),
                    lowest(backLower, externallyActive.target));
            } else if (lastNode.boundaryHeight < y.boundaryHeight) {
                // case E_4, p_y is lower than y
                if (PRINT_CASES) {
                    System.out.println("Case E_3, p_y is lower than y");
                }
                removeUp(firstNode, w, 0, subdivision);
                subdivision.addAll(path);
                addPathEdges(subdivision, failedEdge, w);
                addPathEdges(
                    subdivision, highest(backHigher, externallyActive.target),
                    lowest(backLower, externallyActive.target));
            } else {
                // case E, extracting K_5
                if (PRINT_CASES) {
                    System.out.println("Case E, extracting K_5");
                }
                subdivision.addAll(path);
                addPathEdges(subdivision, v, lowest(backLower, externallyActive.target));
                addPathEdges(subdivision, failedEdge, w);
            }
            return finish(subdivision);
        }
        return kuratowskiSubdivision;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Only the first call to this method does the actual computation, all subsequent calls only
     * return the previously computed value.
     */
    @Override
    public boolean isPlanar()
    {
        return lazyTestPlanarity();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Only the first call to this method does the actual computation, all subsequent calls only
     * return the previously computed value.
     */
    @Override
    public Embedding<V, E> getEmbedding()
    {
        if (isPlanar()) {
            return lazyComputeEmbedding();
        } else {
            throw new IllegalArgumentException("Graph is not planar");
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Only the first call to this method does the actual computation, all subsequent calls only
     * return the previously computed value.
     */
    @Override
    public Graph<V, E> getKuratowskiSubdivision()
    {
        if (isPlanar()) {
            throw new IllegalArgumentException("Graph is planar");
        } else {
            return lazyExtractKuratowskiSubdivision();
        }
    }

    /**
     * Represents information needed to search a path within a biconnected component
     */
    private class SearchInfo
    {
        /**
         * The current node of the dfs traversal
         */
        Node current;
        /**
         * The edge used to go to the {@code current} vertex
         */
        Edge prevEdge;
        /**
         * Whether dfs is in a forward or a backtracking phase
         */
        boolean backtrack;

        /**
         * Creates a new search info
         *
         * @param current the current node of the traversal
         * @param prevEdge the edge used to go to the {@code current} vertex
         * @param backtrack whether dfs is in a forward or a backtracking phase
         */
        SearchInfo(Node current, Edge prevEdge, boolean backtrack)
        {
            this.current = current;
            this.prevEdge = prevEdge;
            this.backtrack = backtrack;
        }
    }

    /**
     * Represents information needed to store in the stack during the input {@code graph}
     * orientation.
     */
    private class OrientDfsStackInfo
    {
        /**
         * The current vertex of the dfs traversal
         */
        V current;
        /**
         * The parent vertex of the {@code current} vertex, which is null for dfs tree roots
         */
        V parent;
        /**
         * The edge connecting {@code parent} and {@code current} vertices
         */
        E parentEdge;
        /**
         * Whether dfs is moving forward or backtracking on the {@code current} node
         */
        boolean backtrack;

        /**
         * Creates new instance of the information stored on the stack during the orientation of the
         * {@code graph}
         *
         * @param current the vertex dfs is currently processing
         * @param parent the parent of the {@code current} vertex
         * @param parentEdge the edge between {@code current} and {@code parent} vertices
         * @param backtrack whether dfs is moving forward or backtracking on the {@code current}
         *        vertex
         */
        OrientDfsStackInfo(V current, V parent, E parentEdge, boolean backtrack)
        {
            this.current = current;
            this.parent = parent;
            this.parentEdge = parentEdge;
            this.backtrack = backtrack;
        }
    }

    /**
     * The information needed to merge two consecutive biconnected components
     */
    private class MergeInfo
    {
        /**
         * The node current traversal descended from. This node belongs to the parent biconnected
         * component
         */
        Node parent;
        /**
         * The next node along the traversal of the parent biconnected component
         */
        Node parentNext;
        /**
         * The virtual root of the child biconnected component
         */
        Node child;
        /**
         * The previous node along the traversal of the child biconnected component
         */
        Node childPrev;
        /**
         * The direction used to enter the parent biconnected component.
         * <p>
         * <b>Note:</b> this value doesn't specify the direction from {@code parent} node to the
         * {@code parentNext} node, i.e. {@code parent.outerFaceNeighbors[vIn]} may not be equal to
         * the {@code parentNext}. Instead, this value specifies the direction used to start the
         * traversal from the parent's biconnected component virtual root.
         */
        int vIn;
        /**
         * The direction used to start the traversal of the child biconnected component. Since the
         * {@code child} is the component root, {@code child.outerFaceNeighbors[|1-vOut|]} is equal
         * to the {@code childPrev}
         */
        int vOut;

        /**
         * Creates new instance of the infromation needed to merge to biconnected components
         *
         * @param parent the node current traversal descended from
         * @param parentNext the next node along the traversal of the parent component
         * @param child the virtual root of the child biconnected component
         * @param childPrev the previous node along the traversal of the child component
         * @param vIn the direction used to enter the parent biconnected component
         * @param vOut the direction used to enter the child biconnected component
         */
        MergeInfo(Node parent, Node parentNext, Node child, Node childPrev, int vIn, int vOut)
        {
            this.parent = parent;
            this.parentNext = parentNext;
            this.child = child;
            this.childPrev = childPrev;
            this.vIn = vIn;
            this.vOut = vOut;
        }

        /**
         * Returns true if the traversal was inverted when descending to the child biconnected
         * component, false otherwise
         *
         * @return true if the traversal was inverted when descending to the child biconnected
         *         component, false otherwise
         */
        boolean isInverted()
        {
            return vIn != vOut;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return String
                .format(
                    "Parent dir = {%s -> %s}, child_dir = {%s -> %s}, inverted = %b, vIn = %d, vOut = %d",
                    parent.toString(false), parentNext.toString(false), childPrev.toString(false),
                    child.toString(false), isInverted(), vIn, vOut);
        }
    }

    /**
     * A circulator over the nodes on the boundary of the biconnected component. Traverses the nodes
     * in the cyclic manner, i.e. it doesn't stop when all the nodes are traversed
     */
    private class OuterFaceCirculator
        implements
        Iterator<Node>
    {
        /**
         * The node this circulator will return next
         */
        private Node current;
        /**
         * The previous node along the traversal of the component boundary. This node is needed
         * because the component boundary nodes aren't connected in an ordered way.
         */
        private Node prev;

        /**
         * Creates a new instance of the circulator over the biconnected component boundary nodes.
         * The {@code prev} node is considered to be just traversed
         *
         * @param current the node this circulator will return next
         * @param prev the previous node along the traversal
         */
        OuterFaceCirculator(Node current, Node prev)
        {
            this.current = current;
            this.prev = prev;
        }

        /**
         * {@inheritDoc}
         * <p>
         * Always returns true since this is a circulator
         */
        @Override
        public boolean hasNext()
        {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Node next()
        {
            Node t = current;
            current = current.nextOnOuterFace(prev);
            prev = t;
            return prev;
        }

        /**
         * Returns an edge connecting previously returned node with node, which will be returned
         * next. If either of the mentioned nodes is virtual, the edge will be incident to its real
         * counterpart.
         *
         * @return an edge from the current node to the next node
         */
        Edge edgeToNext()
        {
            Edge edge = prev.embedded.getFirst();
            Node target = toExistingNode(current);
            Node source = toExistingNode(prev);
            if (edge.getOpposite(source) == target) {
                return edge;
            } else {
                return prev.embedded.getLast();
            }
        }

        /**
         * Returns the node this circulator has just traversed
         *
         * @return the node this circulator has just traversed
         */
        Node getCurrent()
        {
            return prev;
        }

        /**
         * Returns a node adjacent to the current node along the boundary, which is not equal to the
         * next node along the traversal. If the component consist of just one real node, returns
         * the only neighbor the the current node.
         *
         * @return node before the current node along the traversal
         */
        Node getPrev()
        {
            return prev.nextOnOuterFace(current);
        }

        /**
         * Returns either {@code node} or its real counterpart in the case the {@code node} is a
         * component root.
         *
         * @param node the input argument
         * @return the real counterpath of the {@code node}
         */
        private Node toExistingNode(Node node)
        {
            return node.isRootVertex() ? node.getParent() : node;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return String.format("%s -> %s", prev.toString(false), current.toString(false));
        }
    }

    /**
     * Internal representation of the edges of the input {@code graph}.
     */
    private class Edge
    {
        /**
         * The counterpart of this edge in the {@code graph}. This value can be null if the edge was
         * created as a short-circuit edge.
         */
        E graphEdge;
        /**
         * The source node of this edge. For tree edges the {@code source} is lower than the
         * {@code target}, for back edges the {@code target} is lower (having smaller height)
         */
        Node source;
        /**
         * The target of this edge
         */
        Node target;
        /**
         * Either $+1$ or $-1$ for regular and inverted edges respectively. This value is set to
         * $-1$ to flip a biconnected component in $\mathcal{O}(1)$ time. <b>Note:</b> this
         * operation doesn't flip any of the child biconnected components of this biconnected
         * component
         */
        int sign;
        /**
         * Whether the edge is embedded or not. This value is important for
         */
        boolean embedded;
        /**
         * Whether the edge is real or short-circuit. See the original paper for the definition of
         * the short-circuit edges.
         */
        boolean shortCircuit;

        /**
         * Creates a new short-circuit edge with no counterpart in {@code graph}. The {@code source}
         * of this edge is always a real node on the boundary of some biconnected component, and the
         * {@code target} node is the parent node of the biconnected component the source node
         * belongs to, so the edge resembles a regular back edge except for that it doesn't have a
         * counterpart in the {@code graph}
         *
         * @param source the source of the short-circuit edge
         * @param target the target of the short-circuit edge
         */
        Edge(Node source, Node target)
        {
            this(null, source, target);
            this.shortCircuit = true;
            this.embedded = true;
        }

        /**
         * Creates a new tree edge.
         *
         * @param graphEdge the counterpart of this edge in the {@code graph}
         * @param source the source node of this edge
         */
        Edge(E graphEdge, Node source)
        {
            this(graphEdge, source, null);
        }

        /**
         * Creates a new edge. This constructor is used directly for the creation of the back edges
         *
         * @param graphEdge the counterpart of this edge in the {@code graph}
         * @param source the source node of this edge
         * @param target the target node of this edge
         */
        Edge(E graphEdge, Node source, Node target)
        {
            this.graphEdge = graphEdge;
            this.source = source;
            this.target = target;
            this.sign = 1;
        }

        /**
         * True if this edge is incident to the node {@code node}, false otherwise
         *
         * @param node the node to test
         * @return true if this edge is incident to the node {@code node}, false otherwise
         */
        boolean isIncidentTo(Node node)
        {
            return source == node || target == node;
        }

        /**
         * Returns the opposite node of the {@code node}
         *
         * @param node an endpoint of this edge
         * @return the other endpoint of this edge
         */
        Node getOpposite(Node node)
        {
            assert isIncidentTo(node); // debug purpose assertion
            return source == node ? target : source;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            String formatString = "%s -> %s";
            if (shortCircuit) {
                formatString = "%s ~ %s";
            }
            return String.format(formatString, source.toString(false), target.toString(false));
        }
    }

    /**
     * The internal representation of the vertices of the graph. Contains necessary information to
     * perform dfs traversals, biconnected component boundary traversals, to embed edges, etc.
     */
    private class Node
    {
        /**
         * The counterpart of this node in the {@code graph}. For the component roots this value is
         * {@code null}.
         */
        V graphVertex;
        /**
         * Whether this node is a root of the biconnected component or not.
         */
        boolean rootVertex;
        /**
         * The dfs index of this node in the graph. Every node has a unique dfs index in the range
         * $[0,|V| - 1]$. This value is used to order the nodes for the embedding of the edges
         * incident to them. The index of the first dfs root is $1$
         */
        int dfsIndex;
        /**
         * The height of the node in the created dfs tree. The root of the tree has height $0$. The
         * smaller this value is, the lower the node is considered to be.
         */
        int height;
        /**
         * The least <b>dfs index</b> of the nodes' least ancestors in the subtree rooted at this
         * node. If the subtree doesn't contain a node with a back edge, that ends lower that this
         * node, this value is equal to the dfs index of this node.
         */
        int lowpoint;
        /**
         * The least dfs index of the nodes adjacent to this node. If the node doesn't have incident
         * back edges, this value is equal to the dfs index of the node itself.
         */
        int leastAncestor;
        /**
         * Stores some value to indicate whether this node is visited in the current context or not.
         * During the testing phase, if this value if equal to the dfs index of the currently
         * processed node $v$, then this node is visited, otherwise not. During the Kuratowski
         * subdivision extraction phase, the value of $0$ indicates that the node isn't visited,
         * otherwise, the node is considered to be visited.
         */
        int visited;
        /**
         * During the process of embedding of the down edges of the node $v$, this variable is set
         * to the dfs index of $v$ to indicate that this node has a back edge incident to $v$, which
         * needs to be embedded.
         */
        int backEdgeFlag;
        /**
         * The height of this node on the boundary of the biconnected component, which is used to
         * extract the Kuratowski subdivision. The height of the component root is $0$, the heights
         * on the left side are positive, on the right side - negative. This value is used to
         * quickly determine for two nodes on the same boundary branch which one is higher (closer
         * to the component root).
         */
        int boundaryHeight;
        /**
         * Used to mark the boundary nodes of the biconnected
         */
        boolean marked;
        /**
         * Edge to the parent node of this node in the dfs tree. For tree roots this value is
         * {@code null}
         */
        Edge parentEdge;
        /**
         * If this node has a back edge incident to the currently processed node $v$, then this
         * variable stores this edge
         */
        Edge edgeToEmbed;
        /**
         * The component root the node is created in. For dfs tree roots this value is {@code null}
         */
        Node initialComponentRoot;
        /**
         * Two neighbors on the outer face of the biconnected component. Their order is completely
         * irrelevant for the algorithm to traverse the outer face.
         */
        Node[] outerFaceNeighbors;
        /**
         * The list containing the dfs children of this node, which are in the different child
         * biconnected component, i.e. their weren't merged in the parent component.
         */
        DoublyLinkedList<Node> separatedDfsChildList;
        /**
         * The roots of the pertinent components during the processing of the node $v$. These are
         * the components that have pertinent descendant nodes, i.e. nodes incident to the node $v$
         * via a back edge
         */
        DoublyLinkedList<Node> pertinentRoots;
        /**
         * The list of tree edges incident to this node in the dfs tree. This list doesn't contain a
         * parent edge of this node
         */
        List<Edge> treeEdges;
        /**
         * The list containing the edges from descendants of this node in the dfs tree. For each
         * such descendant the corresponding edge is a back edge.
         */
        List<Edge> downEdges;
        /**
         * The list of back edges incident to this node.
         */
        List<Edge> backEdges;
        /**
         * Stores the list node from the {@code separatedDfsChildList} of the parent node this node
         * is stored in. This enable deleting of this node from the {@code separatedDfsChildList}
         * list in $\mathcal{O}(1)$ time
         */
        DoublyLinkedList.ListNode<Node> listNode;
        /**
         * The list of the embedded edges incident to this node in a clockwise or a counterclockwise
         * order. The order is counterclockwise if the product of the signs of the parent edges
         * along the path from the root of the component this node is contained in to this node is
         * equal to $-1$. Otherwise, the order is clockwise.
         */
        DoublyLinkedList<Edge> embedded;

        /**
         * Creates a new real node with the specified parameters.
         *
         * @param graphVertex the counterpart of this node in the {@code graph}
         * @param dfsIndex the dfs index of this node
         * @param height the height of this node in the dfs tree
         * @param initialComponentRoot the component root of this node.
         * @param parentEdge the parent edge of this node
         */
        Node(V graphVertex, int dfsIndex, int height, Node initialComponentRoot, Edge parentEdge)
        {
            this(graphVertex, dfsIndex, parentEdge, false);
            this.height = height;
            this.initialComponentRoot = initialComponentRoot;
        }

        /**
         * Creates a new component root. Dfs index of the component root is equal to dfs index of
         * its parent.
         *
         * @param dfsIndex the dfs index of this node
         * @param parentEdge the parent edge of this component root
         */
        Node(int dfsIndex, Edge parentEdge)
        {
            this(null, dfsIndex, parentEdge, true);
        }

        /**
         * Creates a new node with the specified parameters
         *
         * @param graphVertex the vertex in the {@code graph} corresponding to this node
         * @param dfsIndex the dfs index of this node
         * @param parentEdge the parent edge of this node
         * @param rootVertex whether this is real or virtual node
         */
        Node(V graphVertex, int dfsIndex, Edge parentEdge, boolean rootVertex)
        {
            this.graphVertex = graphVertex;
            this.dfsIndex = dfsIndex;
            this.parentEdge = parentEdge;
            this.rootVertex = rootVertex;
            this.outerFaceNeighbors = TypeUtil.uncheckedCast(Array.newInstance(Node.class, 2));
            this.embedded = new DoublyLinkedList<>();
            if (parentEdge != null) {
                embedded.add(parentEdge);
            }
            this.visited = this.backEdgeFlag = n;
            if (!rootVertex) {
                separatedDfsChildList = new DoublyLinkedList<>();
                pertinentRoots = new DoublyLinkedList<>();
                treeEdges = new ArrayList<>();
                downEdges = new ArrayList<>();
                backEdges = new ArrayList<>();
            }
        }

        /**
         * Checks whether this node is visited in the context of processing the node {@code node}
         *
         * @param node the node that is currently been processed
         * @return true if this node is visited, false otherwise
         */
        boolean isVisitedWrtTo(Node node)
        {
            return node.dfsIndex == visited;
        }

        /**
         * Checks whether this node is pertinent in the context of processing the node {@code node}.
         * During the processing of the node {@code node}, a node is pertinent if it has a back edge
         * to {@code node} or it has a child biconnected component, which has a pertinent node.
         *
         * @param node the node that is currently been processed
         * @return true if this node is pertinent, false otherwise
         */
        boolean isPertinentWrtTo(Node node)
        {
            return backEdgeFlag == node.dfsIndex || !pertinentRoots.isEmpty();
        }

        /**
         * Checks whether this node has a back edge to the {@code node}.
         *
         * @param node the other endpoint of the edge we're looking for
         * @return true if the edge between this node and {@code node} exists, false otherwise
         */
        boolean hasBackEdgeWrtTo(Node node)
        {
            return backEdgeFlag == node.dfsIndex;
        }

        /**
         * Checks whether this node is externally active with respect to the {@code node}. A node is
         * externally active, if it is incident to the edge ending higher than {@code node}, or it
         * has a child biconnected component with a pertinent node
         *
         * @param node an ancestor of this node
         * @return true if this node is externally active with respect to the {@code node}, false
         *         otherwise
         */
        boolean isExternallyActiveWrtTo(Node node)
        {
            return leastAncestor < node.dfsIndex || (!separatedDfsChildList.isEmpty()
                && separatedDfsChildList.getFirst().lowpoint < node.dfsIndex);
        }

        /**
         * Returns true if the node is a component root, false otherwise
         *
         * @return true if the node is a component root, false otherwise
         */
        boolean isRootVertex()
        {
            return rootVertex;
        }

        /**
         * Check whether this node is internally active. A node is internally active if it's
         * pertinent and not externally active
         *
         * @param node an ancestor of this node
         * @return true if this node is internally active, false otherwise
         */
        boolean isInternallyActiveWrtTo(Node node)
        {
            return isPertinentWrtTo(node) && !isExternallyActiveWrtTo(node);
        }

        /**
         * Check whether this node is inactive. A node is inactive it is neither pertinent nor
         * externally active
         *
         * @param node an ancestor of this node
         * @return true if this node is inactive, false otherwise
         */
        boolean isInactiveWrtTo(Node node)
        {
            return !isExternallyActiveWrtTo(node) && !isPertinentWrtTo(node);
        }

        /**
         * Check whether this node is active. A node is active it is either pertinent or externally
         * active
         *
         * @param node an ancestor of this node
         * @return true if this node is active, false otherwise
         */
        boolean isActiveWrtTo(Node node)
        {
            return !isInactiveWrtTo(node);
        }

        /**
         * Returns a circulator, that moves in the direction {@code direction}. The next node along
         * the traversal will be {@code this.outerFaceNeighbor[direction]}.
         *
         * @param direction the direction to move in
         * @return an iterator over the boundary nodes in the direction {@code direction}
         */
        OuterFaceCirculator iterator(int direction)
        {
            return new OuterFaceCirculator(outerFaceNeighbors[direction], this);
        }

        void removeShortCircuitEdges()
        {
            embedded.removeIf(e -> e.shortCircuit);
        }

        /**
         * Returns the parent of this node in the dfs tree. Tree parent of the dfs root node is
         * {@code null}
         *
         * @return the parent of this node in the dfs tree
         */
        Node getParent()
        {
            return parentEdge == null ? null : parentEdge.source;
        }

        /**
         * Checks whether this node has a neighbor {@code node} on the boundary of the biconnected
         * component
         *
         * @param node a possible neighbor of this node
         */
        void checkIsAdjacent(Node node)
        {
            assert node == outerFaceNeighbors[0] || node == outerFaceNeighbors[1];
        }

        /**
         * Swaps the outer face neighbors of this node
         */
        void swapNeighbors()
        {
            Node t = outerFaceNeighbors[0];
            outerFaceNeighbors[0] = outerFaceNeighbors[1];
            outerFaceNeighbors[1] = t;
        }

        /**
         * Substitutes the neighbor {@code node} with a {@code newNeighbor}
         *
         * @param node an old neighbor
         * @param newNeighbor a new neighbor
         */
        void substitute(Node node, Node newNeighbor)
        {
            checkIsAdjacent(node);
            if (outerFaceNeighbors[0] == node) {
                outerFaceNeighbors[0] = newNeighbor;
            } else {
                outerFaceNeighbors[1] = newNeighbor;
            }
        }

        /**
         * Substitutes a neighbor of this node, which is not equal to the {@code node}, with the
         * {@code newNeighbor}
         *
         * @param node the remaining neighbor
         * @param newNeighbor a new neighbor
         */
        void substituteAnother(Node node, Node newNeighbor)
        {
            checkIsAdjacent(node);
            if (outerFaceNeighbors[0] == node) {
                outerFaceNeighbors[1] = newNeighbor;
            } else {
                outerFaceNeighbors[0] = newNeighbor;
            }
        }

        /**
         * Checks whether this node has a neighbor, which is a root of a biconnected component
         *
         * @return true, if this node has a root node neighbor, false otherwise
         */
        boolean hasRootNeighbor()
        {
            return outerFaceNeighbors[0].isRootVertex() || outerFaceNeighbors[1].isRootVertex();
        }

        /**
         * Returns a neighbor of this node which is not equal to the {@code prev}
         *
         * @param prev a neighbor of this node
         * @return a neightbor, which is not equal to the {@code prev}
         */
        Node nextOnOuterFace(Node prev)
        {
            checkIsAdjacent(prev);
            if (outerFaceNeighbors[0] == prev) {
                return outerFaceNeighbors[1];
            } else {
                return outerFaceNeighbors[0];
            }
        }

        /**
         * Adds {@code edge} to the list of the embedded edges such that the {@code prev} node
         * becomes an inner node.
         *
         * @param edge an edge to embed
         * @param prev the node which should be on the new inner face
         */
        void embedBackEdge(Edge edge, Node prev)
        {
            assert !embedded.isEmpty();
            if (prev.isRootVertex()) {
                prev = prev.getParent();
            }
            Edge firstEdge = embedded.getFirst();
            if (firstEdge.getOpposite(this) == prev) {
                // edge on the new inner face is at the beginning of the list
                embedded.addFirst(edge);
            } else {
                embedded.addLast(edge);
            }
        }

        /**
         * Merges the embedded edges of the child component root into this node's embedded edges.
         * Note, that the edges in the {@code edges} list are always in the clockwise order. There
         * are 3 parameters which determine how the {@code edges} list is merged: the {@code vIn}
         * direction, the {@code vOut} direction and the orientation of the edges around this node
         * (clockwise or counterclockwise). The edges in the {@code edges} list should have the same
         * orientation. If this list is inverted, the sign of the {@code parentEdge} is set to $-1$.
         *
         * @param edges the edges from the child component root
         * @param vIn the direction used to enter the parent component
         * @param vOut the direction used to enter the child component
         * @param parentNext the next node along the traversal of the parent biconnected component
         * @param parentEdge the parent edge if the child component
         */
        void mergeChildEdges(
            DoublyLinkedList<Edge> edges, int vIn, int vOut, Node parentNext, Edge parentEdge)
        {
            assert !embedded.isEmpty();
            Node firstOpposite = embedded.getFirst().getOpposite(this);
            boolean alongParentTraversal = firstOpposite != parentNext;
            boolean actionAppend = false, invert = false;
            if (vIn == 0) {
                if (vOut == 0) {
                    if (!alongParentTraversal) {
                        invert = actionAppend = true;
                    }
                } else {
                    if (alongParentTraversal) {
                        invert = true;
                    } else {
                        actionAppend = true;
                    }
                }
            } else {
                if (vOut == 0) {
                    if (!alongParentTraversal) {
                        invert = actionAppend = true;
                    }
                } else {
                    if (alongParentTraversal) {
                        invert = true;
                    } else {
                        actionAppend = true;
                    }
                }
            }
            if (invert) {
                parentEdge.sign = -1;
                edges.invert();
            }
            if (actionAppend) {
                embedded.append(edges);
            } else {
                embedded.prepend(edges);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            String neighbor1 =
                outerFaceNeighbors[0] == null ? "null" : outerFaceNeighbors[0].toString(false);
            String neighbor2 =
                outerFaceNeighbors[1] == null ? "null" : outerFaceNeighbors[1].toString(false);
            String childListString = "null";
            if (separatedDfsChildList != null) {
                StringBuilder builder = new StringBuilder("{");
                separatedDfsChildList.forEach(n -> builder.append(n.toString(false)).append(", "));
                childListString = builder.append("}").toString();
            }
            if (rootVertex) {
                return String
                    .format(
                        "R {%s}: neighbors = [%s, %s], embedded = %s, visited = %d, back_edge_flag = %d, dfs_index = %d",
                        toString(false), neighbor1, neighbor2, embedded.toString(), visited,
                        backEdgeFlag, dfsIndex);
            } else {
                return String
                    .format(
                        "{%s}:  neighbors = [%s, %s], embedded = %s, visited = %d, back_edge_flag = %d, dfs_index = %d, separated = %s, tree_edges = %s, down_edges = %s, back_edges = %s, parent = %s, lowpoint = %d, least_ancestor = %d",
                        toString(false), neighbor1, neighbor2, embedded.toString(), visited,
                        backEdgeFlag, dfsIndex, childListString, treeEdges.toString(),
                        downEdges.toString(), backEdges.toString(),
                        parentEdge == null ? "null" : parentEdge.source.toString(false), lowpoint,
                        leastAncestor);
            }
        }

        /**
         * Returns a full or a partial string representation of this node
         *
         * @param full whether to return full or partial string representation of this node
         * @return either full or partial string representation of this node
         */
        public String toString(boolean full)
        {
            if (!full) {
                if (rootVertex) {
                    return String
                        .format(
                            "%s^%s", parentEdge.source.graphVertex.toString(),
                            parentEdge.target.graphVertex.toString());
                } else {
                    return graphVertex.toString();
                }
            } else {
                return toString();
            }
        }
    }
}
