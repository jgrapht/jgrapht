/*
 * (C) Copyright 2017-2018, by Joris Kinable and Contributors.
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
package org.jgrapht;

import java.util.*;

import org.jgrapht.alg.shortestpath.*;
import org.jgrapht.alg.util.*;

/**
 * Collection of methods which provide numerical graph information.
 *
 * @author Joris Kinable
 */
public abstract class GraphMetrics
{

    /**
     * Compute the <a href="http://mathworld.wolfram.com/GraphDiameter.html">diameter</a> of the
     * graph. The diameter of a graph is defined as $\max_{v\in V}\epsilon(v)$, where $\epsilon(v)$
     * is the eccentricity of vertex $v$. In other words, this method computes the 'longest shortest
     * path'. Two special cases exist. If the graph has no vertices, the diameter is 0. If the graph
     * is disconnected, the diameter is {@link Double#POSITIVE_INFINITY}.
     * <p>
     * For more fine-grained control over this method, or if you need additional distance metrics
     * such as the graph radius, consider using {@link org.jgrapht.alg.shortestpath.GraphMeasurer}
     * instead.
     *
     * @param graph input graph
     * @param <V> graph vertex type
     * @param <E> graph edge type
     * @return the diameter of the graph.
     */
    public static <V, E> double getDiameter(Graph<V, E> graph)
    {
        return new GraphMeasurer<>(graph).getDiameter();
    }

    /**
     * Compute the <a href="http://mathworld.wolfram.com/GraphRadius.html">radius</a> of the graph.
     * The radius of a graph is defined as $\min_{v\in V}\epsilon(v)$, where $\epsilon(v)$ is the
     * eccentricity of vertex $v$. Two special cases exist. If the graph has no vertices, the radius
     * is 0. If the graph is disconnected, the diameter is {@link Double#POSITIVE_INFINITY}.
     * <p>
     * For more fine-grained control over this method, or if you need additional distance metrics
     * such as the graph diameter, consider using {@link org.jgrapht.alg.shortestpath.GraphMeasurer}
     * instead.
     *
     * @param graph input graph
     * @param <V> graph vertex type
     * @param <E> graph edge type
     * @return the diameter of the graph.
     */
    public static <V, E> double getRadius(Graph<V, E> graph)
    {
        return new GraphMeasurer<>(graph).getRadius();
    }

    /**
     * Compute the <a href="http://mathworld.wolfram.com/Girth.html">girth</a> of the graph. The
     * girth of a graph is the length (number of edges) of the smallest cycle in the graph. Acyclic
     * graphs are considered to have infinite girth. For directed graphs, the length of the shortest
     * directed cycle is returned (see Bang-Jensen, J., Gutin, G., Digraphs: Theory, Algorithms and
     * Applications, Springer Monographs in Mathematics, ch 1, ch 8.4.). Simple undirected graphs
     * have a girth of at least 3 (triangle cycle). Directed graphs and Multigraphs have a girth of
     * at least 2 (parallel edges/arcs), and in Pseudo graphs have a girth of at least 1
     * (self-loop).
     * <p>
     * This implementation is loosely based on these <a href=
     * "http://webcourse.cs.technion.ac.il/234247/Winter2003-2004/ho/WCFiles/Girth.pdf">notes</a>.
     * In essence, this method invokes a Breadth-First search from every vertex in the graph. A
     * single Breadth-First search takes $O(n+m)$ time, where $n$ is the number of vertices in the
     * graph, and $m$ the number of edges. Consequently, the runtime complexity of this method is
     * $O(n(n+m))=O(mn)$.
     * <p>
     * An algorithm with the same worst case runtime complexity, but a potentially better average
     * runtime complexity of $O(n^2)$ is described in: Itai, A. Rodeh, M. Finding a minimum circuit
     * in a graph. SIAM J. Comput. Vol 7, No 4, 1987.
     * 
     * @param graph input graph
     * @param <V> graph vertex type
     * @param <E> graph edge type
     * @return girth of the graph, or {@link Integer#MAX_VALUE} if the graph is acyclic.
     */
    public static <V, E> int getGirth(Graph<V, E> graph)
    {
        final int NIL = -1;
        final boolean isAllowingMultipleEdges = graph.getType().isAllowingMultipleEdges();

        // Ordered sequence of vertices
        List<V> vertices = new ArrayList<>(graph.vertexSet());
        // Index map of vertices in ordered sequence
        Map<V, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < vertices.size(); i++)
            indexMap.put(vertices.get(i), i);

        // Objective
        int girth = Integer.MAX_VALUE;
        // Array storing the depth of each vertex in the search tree
        int[] depth = new int[vertices.size()];
        // Queue for BFS
        Queue<V> queue = new LinkedList<>();

        // Check whether the graph has self-loops
        if (graph.getType().isAllowingSelfLoops())
            for (V v : vertices)
                if (graph.containsEdge(v, v))
                    return 1;

        NeighborCache<V, E> neighborIndex = new NeighborCache<>(graph);

        if (graph.getType().isUndirected()) {

            // Array which keeps track of the search tree structure to prevent revisiting parent
            // nodes
            int[] parent = new int[vertices.size()];

            // Start a BFS search tree from each vertex. The search stops when a triangle (smallest
            // possible cycle) is found.
            // The last two vertices can be ignored.
            for (int i = 0; i < vertices.size() - 2 && girth > 3; i++) {

                // Reset data structures
                Arrays.fill(depth, NIL);
                Arrays.fill(parent, NIL);
                queue.clear();

                depth[i] = 0;
                queue.add(vertices.get(i));
                int depthU;

                do {
                    V u = queue.poll();
                    int indexU = indexMap.get(u);
                    depthU = depth[indexU];

                    // Visit all neighbors of vertex u
                    for (V v : neighborIndex.neighborsOf(u)) {
                        int indexV = indexMap.get(v);

                        if (parent[indexU] == indexV) { // Skip the parent of vertex u, unless there
                                                        // are multiple edges between u and v
                            if (!isAllowingMultipleEdges || graph.getAllEdges(u, v).size() == 1)
                                continue;
                        }

                        int depthV = depth[indexV];
                        if (depthV == NIL) { // New neighbor discovered
                            queue.add(v);
                            depth[indexV] = depthU + 1;
                            parent[indexV] = indexU;
                        } else { // Rediscover neighbor: found cycle.
                            girth = Math.min(girth, depthU + depthV + 1);
                        }
                    }
                } while (!queue.isEmpty() && 2 * (depthU + 1) - 1 < girth);
            }
        } else { // Directed case
            for (int i = 0; i < vertices.size() - 1 && girth > 2; i++) {

                // Reset data structures
                Arrays.fill(depth, NIL);
                queue.clear();

                depth[i] = 0;
                queue.add(vertices.get(i));
                int depthU;

                do {
                    V u = queue.poll();
                    int indexU = indexMap.get(u);
                    depthU = depth[indexU];

                    // Visit all neighbors of vertex u
                    for (V v : neighborIndex.successorsOf(u)) {
                        int indexV = indexMap.get(v);

                        int depthV = depth[indexV];
                        if (depthV == NIL) { // New neighbor discovered
                            queue.add(v);
                            depth[indexV] = depthU + 1;
                        } else if (depthV == 0) { // Rediscover root: found cycle.
                            girth = Math.min(girth, depthU + depthV + 1);
                        }
                    }
                } while (!queue.isEmpty() && depthU + 1 < girth);
            }
        }

        assert graph.getType().isUndirected() && graph.getType().isSimple() && girth >= 3
            || graph.getType().isAllowingSelfLoops() && girth >= 1 || girth >= 2
                && (graph.getType().isDirected() || graph.getType().isAllowingMultipleEdges());
        return girth;
    }
}

//End GraphMetrics.java
