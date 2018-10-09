/*
 * (C) Copyright 2016-2018, by Assaf Mizrachi and Contributors.
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
package org.jgrapht.traverse;

import org.jgrapht.*;

import java.util.*;

/**
 * A random walk iterator for a directed or undirected graph.
 * 
 * <p>
 * At each step the iterator selects a random (uniformly distributed) edge out of the current vertex
 * and follows it to the next vertex. In case of directed graphs the outgoing edge set is used. See
 * <a href="https://en.wikipedia.org/wiki/Random_walk#Random_walk_on_graphs">wikipedia</a> for more
 * details.
 * 
 * <p>
 * In case a weighted walk is desired, edges are selected with probability respective to its weight
 * (out of the total weight of the edges). The walk can be bounded by number of steps (default
 * {@code Long#MAX_VALUE} . When the bound is reached the iterator is considered exhausted. Calling
 * {@code next()} on exhausted iterator will throw {@code NoSuchElementException}.
 * 
 * In case a sink (i.e. no edges) vertex is reached, any consecutive calls to {@code next()} will
 * throw {@code NoSuchElementException}.
 * 
 * <p>
 * For this iterator to work correctly the graph must not be modified during iteration. Currently
 * there are no means to ensure that, nor to fail-fast. The results of such modifications are
 * undefined.
 * 
 * @author Assaf Mizrachi
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class RandomWalkIterator<V, E>
    extends
    AbstractGraphIterator<V, E>
{
    private V currentVertex;
    private final boolean isWeighted;
    private boolean sinkReached;
    private long maxSteps;
    private Random random;

    /**
     * Creates a new iterator for the specified graph. Iteration will start at arbitrary vertex.
     * Walk is un-weighted and bounded by {@code Long#MAX_VALUE} steps.
     *
     * @param graph the graph to be iterated.
     *
     * @throws IllegalArgumentException if <code>graph==null</code> or does not contain
     *         <code>startVertex</code>
     */
    public RandomWalkIterator(Graph<V, E> graph)
    {
        this(graph, null);
    }

    /**
     * Creates a new iterator for the specified graph. Iteration will start at the specified start
     * vertex. If the specified start vertex is <code>
     * null</code>, Iteration will start at an arbitrary graph vertex. Walk is un-weighted and
     * bounded by {@code Long#MAX_VALUE} steps.
     *
     * @param graph the graph to be iterated.
     * @param startVertex the vertex iteration to be started.
     *
     * @throws IllegalArgumentException if <code>graph==null</code> or does not contain
     *         <code>startVertex</code>
     */
    public RandomWalkIterator(Graph<V, E> graph, V startVertex)
    {
        this(graph, startVertex, true);
    }

    /**
     * Creates a new iterator for the specified graph. Iteration will start at the specified start
     * vertex. If the specified start vertex is <code>
     * null</code>, Iteration will start at an arbitrary graph vertex. Walk is bounded by
     * {@code Long#MAX_VALUE} steps.
     *
     * @param graph the graph to be iterated.
     * @param startVertex the vertex iteration to be started.
     * @param isWeighted set to <code>true</code> if a weighted walk is desired.
     *
     * @throws IllegalArgumentException if <code>graph==null</code> or does not contain
     *         <code>startVertex</code>
     */
    public RandomWalkIterator(Graph<V, E> graph, V startVertex, boolean isWeighted)
    {
        this(graph, startVertex, isWeighted, Long.MAX_VALUE);
    }

    /**
     * Creates a new iterator for the specified graph. Iteration will start at the specified start
     * vertex. If the specified start vertex is <code>
     * null</code>, Iteration will start at an arbitrary graph vertex. Walk is bounded by the
     * provided number steps.
     *
     * @param graph the graph to be iterated.
     * @param startVertex the vertex iteration to be started.
     * @param isWeighted set to <code>true</code> if a weighted walk is desired.
     * @param maxSteps number of steps before walk is exhausted.
     *
     * @throws IllegalArgumentException if <code>graph==null</code> or does not contain
     *         <code>startVertex</code>
     */
    public RandomWalkIterator(Graph<V, E> graph, V startVertex, boolean isWeighted, long maxSteps)
    {
        this(graph, startVertex, isWeighted, maxSteps, new Random());
    }

    /**
     * Creates a new iterator for the specified graph. Iteration will start at the specified start
     * vertex. If the specified start vertex is <code>
     * null</code>, Iteration will start at an arbitrary graph vertex. Walk is bounded by the
     * provided number steps.
     *
     * @param graph the graph to be iterated.
     * @param startVertex the vertex iteration to be started.
     * @param isWeighted set to <code>true</code> if a weighted walk is desired.
     * @param maxSteps number of steps before walk is exhausted.
     * @param rng the random number generator to use
     *
     * @throws IllegalArgumentException if <code>graph==null</code> or does not contain
     *         <code>startVertex</code>
     */
    public RandomWalkIterator(
        Graph<V, E> graph, V startVertex, boolean isWeighted, long maxSteps, Random rng)
    {
        super(graph);

        // do not cross components.
        this.crossComponentTraversal = false;
        this.isWeighted = isWeighted;
        this.maxSteps = maxSteps;

        // select a random start vertex in case not provided.
        if (startVertex == null) {
            if (graph.vertexSet().size() > 0) {
                currentVertex = graph.vertexSet().iterator().next();
            }
        } else if (graph.containsVertex(startVertex)) {
            currentVertex = startVertex;
        } else {
            throw new IllegalArgumentException("graph must contain the start vertex");
        }

        this.sinkReached = false;
        this.random = Objects.requireNonNull(rng, "Random number generator cannot be null");
    }

    /**
     * Check if this walk is exhausted. Calling {@link #next()} on exhausted iterator will throw
     * {@link NoSuchElementException}.
     * 
     * @return <code>true</code>if this iterator is exhausted, <code>false</code> otherwise.
     */
    protected boolean isExhausted()
    {
        return maxSteps == 0;
    }

    /**
     * Update data structures every time we see a vertex.
     *
     * @param vertex the vertex encountered
     * @param edge the edge via which the vertex was encountered, or null if the vertex is a
     *        starting point
     */
    protected void encounterVertex(V vertex, E edge)
    {
        maxSteps--;
    }

    @Override
    public boolean hasNext()
    {
        return currentVertex != null && !isExhausted() && !sinkReached;
    }

    @Override
    public V next()
    {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        Set<? extends E> potentialEdges = graph.outgoingEdgesOf(currentVertex);

        // randomly select an edge from the set of potential edges.
        E nextEdge = drawEdge(potentialEdges);
        if (nextEdge != null) {
            V nextVertex;
            nextVertex = Graphs.getOppositeVertex(graph, nextEdge, currentVertex);
            encounterVertex(nextVertex, nextEdge);
            fireEdgeTraversed(createEdgeTraversalEvent(nextEdge));
            fireVertexTraversed(createVertexTraversalEvent(nextVertex));
            currentVertex = nextVertex;
            return nextVertex;
        } else {
            sinkReached = true;
            return currentVertex;
        }
    }

    /**
     * Randomly draws an edges out of the provided set. In case of un-weighted walk, edge will be
     * selected with uniform distribution across all outgoing edges. In case of a weighted walk,
     * edge will be selected with probability respective to its weight across all outgoing edges.
     * 
     * @param edges the set to select the edge from
     * @return the drawn edges or null if set is empty.
     */
    private E drawEdge(Set<? extends E> edges)
    {
        if (edges.isEmpty()) {
            return null;
        }

        int drawn;
        List<E> list = new ArrayList<E>(edges);
        if (isWeighted) {
            Iterator<E> safeIter = list.iterator();
            double border = random.nextDouble() * getTotalWeight(list);
            double d = 0;
            drawn = -1;
            do {
                d += graph.getEdgeWeight(safeIter.next());
                drawn++;
            } while (d < border);
        } else {
            drawn = random.nextInt(list.size());
        }
        return list.get(drawn);
    }

    private double getTotalWeight(Collection<E> edges)
    {
        double total = 0;
        for (E e : edges) {
            total += graph.getEdgeWeight(e);
        }
        return total;
    }
}
