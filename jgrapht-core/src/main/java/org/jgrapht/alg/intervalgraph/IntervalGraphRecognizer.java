package org.jgrapht.alg.intervalgraph;

import static org.jgrapht.alg.intervalgraph.LexBreadthFirstSearch.*;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.intervalgraph.*;
import org.jgrapht.intervalgraph.interval.*;

public final class IntervalGraphRecognizer<V, E>
{

    /**
     * Stores whether or not the graph is an interval graph.
     */
    private boolean isIntervalGraph;

    /**
     * Stores the computed interval graph representation (or <tt>null</tt> if no such representation
     * exists) of the graph.
     */
    private Graph<V, E> intervalRepresentation; // TODO: change to work with an actual interval
                                                // graph after merge

    /**
     * Creates (and runs) a new interval graph recognizer for the given graph.
     * 
     * @param graph the graph to be tested.
     */
    public IntervalGraphRecognizer(Graph<V, E> graph)
    {
        isIntervalGraph(graph);
    }

    /**
     * check if the graph is an interval graph
     *
     * @param <V> the generic type representing vertices
     * @param <E> the generic type representing edges
     * @return
     */
    public boolean isIntervalGraph(Graph<V, E> graph)
    {

        // An empty graph is an interval graph.
        if (graph.vertexSet().isEmpty()) {
            return true;
        }

        // Step 1 - LBFS from an arbitrary vertex
        // Input - random vertex r
        // Output - the result of current sweep alpha, further last vertex a visited by current
        // sweep
        HashMap<V, Integer> sweepAlpha =
            lexBreadthFirstSearch(graph, randomElementOf(graph.vertexSet()));
        V vertexA = lastElementOf(sweepAlpha);

        // Step 2 - LBFS+ from the last vertex of the previous sweep
        // Input - the result of previous sweep alpha, vertex a
        // Output - the result of current sweep beta, further last vertex b visited by current sweep
        HashMap<V, Integer> sweepBeta = lexBreadthFirstSearchPlus(graph, vertexA, sweepAlpha);
        V vertexB = lastElementOf(sweepBeta);

        // Step 3 - LBFS+ from the last vertex of the previous sweep
        // Input - the result of previous sweep beta, vertex b
        // Output - the result of current sweep gamma, further last vertex c visited by current
        // sweep
        HashMap<V, Integer> sweepGamma = lexBreadthFirstSearchPlus(graph, vertexB, sweepBeta);
        V vertexC = lastElementOf(sweepGamma);

        // Step 4 - LBFS+ from the last vertex of the previous sweep
        // Input - the result of previous sweep gamma, vertex c
        // Output - the result of current sweep delta, further last vertex d visited by current
        // sweep
        HashMap<V, Integer> sweepDelta = lexBreadthFirstSearchPlus(graph, vertexC, sweepGamma);
        V vertexD = lastElementOf(sweepDelta);

        // Additionally, calculate the index and the corresponding A set for each vertex

        // Step 5 - LBFS+ from the last vertex of the previous sweep
        // Input - the result of previous sweep delta, vertex d
        // Output - the result of current sweep epsilon, further last vertex e visited by current
        // sweep
        HashMap<V, Integer> sweepEpsilon = lexBreadthFirstSearchPlus(graph, vertexD, sweepDelta);
        // V vertexE = lastElementOf(sweepEpsilon); TODO: not used?

        // Additionally, calculate the index and the corresponding B set for each vertex

        // Step 6 - LBFS* with the resulting sweeps
        // Input - the result of sweep gamma and sweep epsilon
        // Output - the result of current sweep zeta
        HashMap<V, Integer> sweepZeta =
            lexBreadthFirstSearchStar(graph, vertexD, sweepDelta, sweepEpsilon);

        // if sweepZeta is umbrella-free, then the graph is interval.
        // otherwise, the graph is not interval

        if (isIOrdering(sweepZeta, graph)) {
            this.isIntervalGraph = true;

            // Compute interval representation -- TODO: complete after merge
            HashMap<V, Integer> neighborIndex = new HashMap<>();
            for (V vertex : graph.vertexSet()) {
                int maxNeighbor = 0;

                List<V> neighbors = Graphs.neighborListOf(graph, vertex);
                neighbors.add(vertex);

                for (V neighbor : neighbors) {
                    maxNeighbor = Math.max(maxNeighbor, sweepZeta.get(neighbor));
                }

                neighborIndex.put(vertex, maxNeighbor);
            }

            HashMap<Integer, Interval<Integer>> intervals = new HashMap<>(graph.vertexSet().size());
            ArrayList<Interval<Integer>> sortedIntervals =
                new ArrayList<>(graph.vertexSet().size());

            // Compute intervals and store them associated by their starting point ...
            for (V vertex : graph.vertexSet()) {
                Interval<Integer> vertexInterval =
                    new Interval<>(sweepZeta.get(vertex), neighborIndex.get(vertex));

                intervals.put(sweepZeta.get(vertex), vertexInterval);
            }

            // ... and produce a list sorted by the starting points for an efficient construction of
            // the graph
            for (int i = 0; i < graph.vertexSet().size(); i++) {
                sortedIntervals.add(intervals.get(i));
            }

            // TODO: build the actual interval graph
            this.intervalRepresentation = null;
        } else {
            // set values negatively
            this.isIntervalGraph = false;
            this.intervalRepresentation = null;
        }

        return isIOrdering(sweepZeta, graph);
    }

    /**
     * Calculates if the given sweep is an I-Ordering (according to the Graph graph)
     * 
     * @param <E>
     *
     * @param sweep the order we want to check if its an I-Order
     * @param graph the graph we want to check if its an I-Order
     * @return true, if sweep is an I-Order according to graph
     */
    private static <V, E> boolean isIOrdering(HashMap<V, Integer> sweep, Graph<V, E> graph)
    {
        HashMap<V, Integer> last = new HashMap<>();
        HashMap<Integer, V> inverseSweep = new HashMap<>();

        for (V vertex : graph.vertexSet()) {
            int index = sweep.get(vertex);
            inverseSweep.put(index, vertex);
        }

        for (int i = 0; i < sweep.size() - 2; i++) {
            for (int j = i + 1; j < sweep.size() - 1; j++) {
                for (int k = j + 1; k < sweep.size(); k++) {
                    boolean edgeIJ = graph.containsEdge(inverseSweep.get(i), inverseSweep.get(j));
                    boolean edgeIK = graph.containsEdge(inverseSweep.get(i), inverseSweep.get(k));
                    if (edgeIK) {
                        if (edgeIJ) {
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * return the last element of the given map
     *
     * @param map
     * @param <V> the generic type representing vertices
     * @return
     */
    private static <V> V lastElementOf(HashMap<V, Integer> map)
    {
        return Collections.max(map.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    /**
     * return a random element of the given set
     *
     * @param set
     * @param <V> the generic type representing vertices
     * @return
     */
    private static <V> V randomElementOf(Set<V> set)
    {
        if (set == null) {
            throw new IllegalArgumentException("Set parameter cannot be null.");
        }

        int index = new Random().nextInt(set.size());
        Iterator<V> iterator = set.iterator();
        for (int i = 0; i < index; i++) {
            iterator.next();
        }
        return iterator.next();
    }

    /**
     * Returns whether or not the graph is an interval graph.
     *
     * @return <tt>true</tt> if the graph is an interval graph, otherwise false.
     */
    public boolean isIntervalGraph()
    {
        return isIntervalGraph;
    }

    /**
     * Returns an interval graph representation of the graph.
     *
     * @return an interval graph representation of the graph or <tt>null</tt> if the graph is not an
     *         interval graph.
     */
    public Graph<V, E> getIntervalGraphRepresentation()
    {
        return intervalRepresentation;
    }
}
