package org.jgrapht.alg.blossom;

import org.jgrapht.Graph;

import java.util.HashMap;
import java.util.Map;

import static org.jgrapht.alg.blossom.BlossomPerfectMatching.INFINITY;

class Initializer<V, E> {
    private final Graph<V, E> graph;
    private int nodeNum;
    private int edgeNum;
    private int treeNum;
    private Node[] nodes;
    private Edge[] edges;
    private Tree[] trees;
    private Map<V, Node> vertexMap;
    private Map<E, Edge> edgeMap;
    private State<V, E> state;

    public Initializer(Graph<V, E> graph) {
        this.graph = graph;
    }

    public State<V, E> initialize(BlossomPerfectMatching.Options options) {
        InitializationType type = options.initializationType;
        initGraph();
        state = new State<>(graph, nodes, edges, null, nodeNum, edgeNum, 0, new BlossomPerfectMatching.Statistics(), vertexMap, edgeMap, options);

        int treeNum;
        if (type == InitializationType.GREEDY) {
            treeNum = initGreedy();
        } else {
            treeNum = nodeNum;
            for (Node node : nodes) {
                node.isOuter = true;
            }
        }
        allocateTrees(treeNum);
        state.trees = trees;
        state.treeNum = treeNum;
        initAuxiliaryGraph();

        return state;
    }

    private void initGraph() {
        nodeNum = graph.vertexSet().size();
        edgeNum = graph.edgeSet().size();
        nodes = new Node[nodeNum + 1];
        nodes[nodeNum] = new Node();
        edges = new Edge[edgeNum];
        vertexMap = new HashMap<>(nodeNum);
        edgeMap = new HashMap<>(edgeNum);
        int i = 0;
        for (V vertex : graph.vertexSet()) {
            nodes[i] = new Node();
            vertexMap.put(vertex, nodes[i]);
            i++;
        }
        i = 0;
        for (E e : graph.edgeSet()) {
            Node source = vertexMap.get(graph.getEdgeSource(e));
            Node target = vertexMap.get(graph.getEdgeTarget(e));
            Edge edge = State.addEdge(source, target, graph.getEdgeWeight(e));
            edges[i] = edge;
            edgeMap.put(e, edge);
            i++;
        }
    }

    private int initGreedy() {
        int dir;
        // set all dual variables to infinity
        for (int i = 0; i < nodeNum; i++) {
            nodes[i].dual = INFINITY;
        }
        // set dual variables to the minimum weight of the incident edges
        for (Edge edge : edges) {
            if (edge.head[0].dual > edge.slack) {
                edge.head[0].dual = edge.slack;
            }
            if (edge.head[1].dual > edge.slack) {
                edge.head[1].dual = edge.slack;
            }
        }
        // divide dual variables by to, decrease edge slack accordingly
        for (Edge edge : edges) {
            Node source = edge.head[0];
            Node target = edge.head[1];
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
        for (Edge edge1 : edges) {
            if (edge1.slack < 0) {
                throw new RuntimeException();
            }
        }
        // go through all vertices, greedily increase their dual variables to the minimum slack of incident edges
        // if there exist an tight unmatched edge in the neighborhood, match it
        treeNum = nodeNum;
        int treeNum = nodeNum;
        Edge edge;
        for (Node node : nodes) {
            if (!node.isInftyNode()) {
                double minSlack = INFINITY;
                for (Node.AdjacentEdgeIterator adjacentEdgeIterator = node.adjacentEdgesIterator(); adjacentEdgeIterator.hasNext(); ) {
                    edge = adjacentEdgeIterator.next();
                    if (edge.slack < minSlack) {
                        minSlack = edge.slack;
                    }
                }
                node.dual += minSlack;
                double resultMinSlack = minSlack;
                for (Node.AdjacentEdgeIterator adjacentEdgeIterator = node.adjacentEdgesIterator(); adjacentEdgeIterator.hasNext(); ) {
                    edge = adjacentEdgeIterator.next();
                    dir = adjacentEdgeIterator.getDir();
                    if (edge.slack <= resultMinSlack && node.isPlusNode() && edge.head[dir].isPlusNode()) {
                        node.setLabel(Node.Label.INFTY);
                        edge.head[dir].setLabel(Node.Label.INFTY);
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

    private void initAuxiliaryGraph() {
        Node root, opposite;
        Tree tree;
        Edge edge;
        TreeEdge treeEdge;
        for (State.TreeRootsIterator iterator = state.treeRootsIterator(); iterator.hasNext(); ) {
            root = iterator.next();
            tree = root.tree;
            for (Node.AdjacentEdgeIterator edgeIterator = root.adjacentEdgesIterator(); edgeIterator.hasNext(); ) {
                edge = edgeIterator.next();
                opposite = edge.head[edgeIterator.getDir()];
                if (opposite.isInftyNode()) {
                    tree.addPlusInfinityEdge(edge, edge.slack);
                } else if (!opposite.isProcessed) {
                    if (opposite.tree.currentEdge == null) {
                        State.addTreeEdge(tree, opposite.tree);
                    }
                    opposite.tree.currentEdge.addPlusPlusEdge(edge, edge.slack);
                }
            }
            root.isProcessed = true;
            for (Tree.TreeEdgeIterator treeEdgeIterator = tree.treeEdgeIterator(); treeEdgeIterator.hasNext(); ) {
                treeEdge = treeEdgeIterator.next();
                treeEdge.head[treeEdgeIterator.getCurrentDirection()].currentEdge = null;
            }
        }
    }

    private void allocateTrees(int treeNumToAllocate) {
        treeNum = 0;
        trees = new Tree[treeNumToAllocate];
        Node lastRoot = nodes[nodeNum];
        for (int i = 0; i < nodeNum; i++) {
            Node node = nodes[i];
            if (node.isPlusNode()) {
                node.treeSiblingPrev = lastRoot;
                lastRoot.treeSiblingNext = node;
                lastRoot = node;

                Tree tree = new Tree(node);
                trees[treeNum++] = tree;
            }
        }
        lastRoot.treeSiblingNext = null;
    }

    enum InitializationType {
        GREEDY, NONE,
    }
}
