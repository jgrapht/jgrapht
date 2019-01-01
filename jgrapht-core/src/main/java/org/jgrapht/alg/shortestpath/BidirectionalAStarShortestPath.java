package org.jgrapht.alg.shortestpath;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class BidirectionalAStarShortestPath<V, E>
        extends
        BaseShortestPathAlgorithm<V, E> {

    private AStarAdmissibleHeuristic<V> admissibleHeuristic;

    /**
     * Constructs a new instance of the algorithm for a given graph.
     *
     * @param graph the graph
     */
    public BidirectionalAStarShortestPath(Graph<V, E> graph, AStarAdmissibleHeuristic<V> admissibleHeuristic) {
        super(graph);
        this.admissibleHeuristic =
                Objects.requireNonNull(admissibleHeuristic, "Heuristic function cannot be null!");
    }

    @Override
    public GraphPath<V, E> getPath(V source, V sink) {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SOURCE_VERTEX);
        }
        if (!graph.containsVertex(sink)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SINK_VERTEX);
        }

        // handle special case if source equals target
        if (source.equals(sink)) {
            return createEmptyPath(source, sink);
        }

        // create frontiers
        SearchFrontier forwardFrontier = new SearchFrontier(graph);
        SearchFrontier backwardFrontier;
        if (graph.getType().isDirected()) {
            backwardFrontier = new SearchFrontier(new EdgeReversedGraph<>(graph));
        } else {
            backwardFrontier = new SearchFrontier(graph);
        }

        forwardFrontier.gScoreMap.put(source, 0.0);
        FibonacciHeapNode<V> sourceNode = new FibonacciHeapNode<>(source);
        forwardFrontier.openList.insert(sourceNode, 0.0);
        forwardFrontier.vertexToHeapNodeMap.put(source, sourceNode);

        backwardFrontier.gScoreMap.put(sink, 0.0);
        FibonacciHeapNode<V> targetNode = new FibonacciHeapNode<>(sink);
        backwardFrontier.openList.insert(targetNode, 0.0);
        backwardFrontier.vertexToHeapNodeMap.put(sink, targetNode);

        // initialize best path
        double bestPath = Double.POSITIVE_INFINITY;
        V bestPathCommonVertex = null;

        SearchFrontier frontier = forwardFrontier;
        SearchFrontier otherFrontier = backwardFrontier;
        V endVertex = source;
        V otherEndVertex = sink;

        while (true) {
            // stopping condition
            if (frontier.openList.isEmpty() || otherFrontier.openList.isEmpty()
                    || frontier.openList.min().getKey() + otherFrontier.openList.min().getKey() >= bestPath) {
                break;
            }

            // frontier scan
            FibonacciHeapNode<V> node = frontier.openList.removeMin();
            V v = node.getData();
            double vDistance = node.getKey();

            for (E edge : graph.outgoingEdgesOf(v)) {
                V successor = Graphs.getOppositeVertex(graph, edge, v);

                if (successor.equals(v)) { // Ignore self-loop
                    continue;
                }

                double gScore_current = frontier.gScoreMap.get(v);
                double tentativeGScore = gScore_current + graph.getEdgeWeight(edge);
                double fScore = tentativeGScore + admissibleHeuristic.getCostEstimate(successor, endVertex);

                frontier.updateDistance(successor, edge, tentativeGScore, fScore);

                // check path with successor's distance from the other frontier
                double pathDistance = vDistance + graph.getEdgeWeight(edge) + otherFrontier.getDistance(successor);

                if (pathDistance < bestPath) {
                    bestPath = pathDistance;
                    bestPathCommonVertex = successor;
                }
            }

            // swap frontiers
            SearchFrontier tmpFrontier = frontier;
            frontier = otherFrontier;
            otherFrontier = tmpFrontier;

            // swap end vertices
            V tmpVertex = endVertex;
            endVertex = otherEndVertex;
            otherEndVertex = tmpVertex;
        }

        // create path if found
        if (Double.isFinite(bestPath)) {
            return createPath(
                    forwardFrontier, backwardFrontier, bestPath, source, bestPathCommonVertex, sink);
        } else {
            return createEmptyPath(source, sink);
        }
    }

    private GraphPath<V, E> createPath(
            SearchFrontier forwardFrontier, SearchFrontier backwardFrontier, double weight, V source,
            V commonVertex, V sink) {
        LinkedList<E> edgeList = new LinkedList<>();
        LinkedList<V> vertexList = new LinkedList<>();

        // add common vertex
        vertexList.add(commonVertex);

        // traverse forward path
        V v = commonVertex;
        while (true) {
            E e = forwardFrontier.getTreeEdge(v);

            if (e == null) {
                break;
            }

            edgeList.addFirst(e);
            v = Graphs.getOppositeVertex(forwardFrontier.graph, e, v);
            vertexList.addFirst(v);
        }

        // traverse reverse path
        v = commonVertex;
        while (true) {
            E e = backwardFrontier.getTreeEdge(v);

            if (e == null) {
                break;
            }

            edgeList.addLast(e);
            v = Graphs.getOppositeVertex(backwardFrontier.graph, e, v);
            vertexList.addLast(v);
        }

        return new GraphWalk<>(graph, source, sink, vertexList, edgeList, weight);
    }

    /**
     * Helper class to maintain the search frontier
     */
    class SearchFrontier {
        final Graph<V, E> graph;

        final FibonacciHeap<V> openList;
        final Map<V, FibonacciHeapNode<V>> vertexToHeapNodeMap;
        final Set<V> closedList;
        final Map<V, Double> gScoreMap;
        final Map<V, E> cameFrom;

        public SearchFrontier(Graph<V, E> graph) {
            this.graph = graph;
            openList = new FibonacciHeap<>();
            vertexToHeapNodeMap = new HashMap<>();
            closedList = new HashSet<>();
            gScoreMap = new HashMap<>();
            cameFrom = new HashMap<>();
        }

        public void updateDistance(V v, E e, double tentativeGScore, double fScore) {
            if (vertexToHeapNodeMap.containsKey(v)) { // We re-encountered a vertex. It's
                // either in the open or closed list.
                if (tentativeGScore >= gScoreMap.get(v)) {// Ignore path since it is
                    // non-improving
                    return;
                }

                cameFrom.put(v, e);
                gScoreMap.put(v, tentativeGScore);

                if (closedList.contains(v)) { // it's in the closed list. Move node back to
                    // open list, since we discovered a shorter
                    // path to this node
                    closedList.remove(v);
                    openList.insert(vertexToHeapNodeMap.get(v), fScore);
                } else { // It's in the open list
                    openList.decreaseKey(vertexToHeapNodeMap.get(v), fScore);
                }
            } else { // We've encountered a new vertex.
                cameFrom.put(v, e);
                gScoreMap.put(v, tentativeGScore);
                FibonacciHeapNode<V> heapNode = new FibonacciHeapNode<>(v);
                openList.insert(heapNode, fScore);
                vertexToHeapNodeMap.put(v, heapNode);
            }
        }

        public double getDistance(V v) {
            Double distance = gScoreMap.get(v);
            if (distance == null) {
                return Double.POSITIVE_INFINITY;
            } else {
                return distance;
            }
        }

        public E getTreeEdge(V v) {
            E e = cameFrom.get(v);
            if (e == null) {
                return null;
            } else {
                return e;
            }
        }
    }
}
