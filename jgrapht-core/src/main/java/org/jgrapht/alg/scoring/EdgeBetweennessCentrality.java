package org.jgrapht.alg.scoring;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.EdgeScoringAlgorithm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Stack;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;

/**
 * Edge Betweenness centrality.
 *
 * <p>
 * The edge betweenness centrality is defined as the number of the shortest paths
 * that go through an edge in a graph or network.Each edge in the network can be associated with an edge
 * betweenness centrality value. An edge with a high edge betweenness centrality score represents a
 * bridge-like connector between two parts of a network.
 *
 * The algorithm is based on
 * <ul>
 * <li>Newman, M., & Girvan, M. (2004). Finding and evaluating community
 * structure in networksPhys. Rev. E, 69, 026113.</li>
 * </ul>
 *
 * which in essence is a modified version of the same algorithm for
 * Vertex-betweenness (see {@link BetweennessCentrality}).
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Edwin Ouwehand
 */
public class EdgeBetweennessCentrality<V, E>
    implements
    EdgeScoringAlgorithm<E, Double> {

    private final Graph<V, E> graph;
    private Map<E, Double> scores;

    /**
     * Construct a new instance.
     *
     * @param graph the input graph
     */
    public EdgeBetweennessCentrality(Graph<V, E> graph) {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        this.scores = null;
    }

    /**
     * Calculates the centrality index for all edges
     */
    private Map<E, Double> calculate() {
        Map<E, Double> betweenness = graph.edgeSet().stream().collect(toMap(e -> e, e -> 0.0));

        for (V start : graph.vertexSet()) {
            // Using a custom Dijkstra implementation, because we need to keep all the shortest
            // paths if more that one is available.
            Map<V, LinkedList<E>> shortestPath = graph.vertexSet().stream()
                .collect(toMap(v -> v, v -> new LinkedList<>()));
            Map<V, Integer> shortestPathCount = new HashMap<>();
            shortestPathCount.put(start, 1);
            Map<V, Double> pathLength = new HashMap<>();
            pathLength.put(start, 0.0);

            Stack<V> destStack = new Stack<>(); // Destinations with leaves at the top
            Queue<V> queue = new LinkedList<>();
            queue.add(start);
            for (V predecessor; (predecessor = queue.poll()) != null; ) {
                destStack.push(predecessor);

                for (E edge : graph.outgoingEdgesOf(predecessor)) {
                    V neighbor = graph.getEdgeTarget(edge);

                    if (!pathLength.containsKey(neighbor)) {
                        queue.add(neighbor);
                        pathLength.put(neighbor, pathLength.get(predecessor) + graph.getEdgeWeight(edge));
                    }

                    if (pathLength.get(neighbor) == pathLength.get(predecessor) + graph.getEdgeWeight(edge)) {
                        shortestPathCount.put(neighbor,
                            shortestPathCount.getOrDefault(neighbor, 0) + shortestPathCount.get(predecessor));
                        shortestPath.get(neighbor).add(edge);
                    }
                }
            }

            Map<E, Double> dependency = new HashMap<>();
            while (!destStack.isEmpty()) {
                V dest = destStack.pop();
                double edgeScore = graph.outgoingEdgesOf(dest).stream()
                    .mapToDouble(key -> dependency.getOrDefault(key, 0.0)).sum();

                for (E e : shortestPath.get(dest)) {
                    dependency.put(e, ((double)shortestPathCount.get(graph.getEdgeSource(e))
                        / (double)shortestPathCount.get(dest)) * (1.0 + edgeScore));
                    betweenness.computeIfPresent(e, (k, d) -> d + dependency.get(e));
                }
            }
        }
        return betweenness;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<E, Double> getScores() {
        if (scores == null) {
            scores = calculate();
        }
        return unmodifiableMap(scores);
    }

    /**
     * {@inheritDoc}
     *
     * Note that edgebetweenness is calculated for the entire graph,
     * this method merely gets a single result from the same map as {@link #getScores()}.
     */
    @Override
    public Double getEdgeScore(E e) {
        if (scores == null) {
            scores = calculate();
        }
        return scores.get(e);
    }
}
