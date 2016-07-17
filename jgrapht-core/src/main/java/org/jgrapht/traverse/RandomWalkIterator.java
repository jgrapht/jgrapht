/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2016, by Barak Naveh and Contributors.
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
/* ----------------
 * RandomWalkIterator.java
 * ----------------
 * (C) Copyright 2016-, by Assaf Mizrachi and Contributors.
 *
 * Original Author:  Assaf Mizrachi
 * Contributor(s):   
 *
 * $Id$
 *
 * Changes
 * -------
 *
 */
package org.jgrapht.traverse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.VertexTraversalEvent;

/**
 * 
 * A <a href="https://en.wikipedia.org/wiki/Random_walk#Random_walk_on_graphs">random-walk</a> iterator
 * for a directed and an undirected graph. At each step selected a randomly (uniformly distributed) 
 * edge out of the current vertex edges (in case of directed graph - from the outgoing edges),
 * and follows it to the next vertex.
 * 
 * In case a weighted walk is desired (and in case the graph is weighted), edges are selected with
 * probability respective to its weight (out of the total weight of the edges).
 * 
 * Walk can be bounded by number of steps (default {@code Long#MAX_VALUE} . When the bound
 * is reached the iterator is considered exhausted. Calling {@code next()} on exhausted iterator will
 * throw {@code NoSuchElementException}.
 * 
 * In case a sink (i.e. no edges) vertex is reached, iterator will return null and consecutive calls
 * to {@code next()} will throw {@code NoSuchElementException}.
 * 
 * For this iterator to work correctly the graph must not be
 * modified during iteration. Currently there are no means to ensure that, nor to fail-fast. 
 * The results of such modifications are undefined.
 * 
 * @author Assaf Mizrachi
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class RandomWalkIterator<V, E> extends AbstractGraphIterator<V, E> {
    
    private V currentVertex;
    private final Graph<V, E> graph;
    private final boolean isWeighted;
    private boolean sinkReached;
    private long maxSteps;
    private Random random;
    
    /**
     * Creates a new iterator for the specified graph. Iteration will start at
     * arbitrary vertex.
     * Walk is un-weighted and bounded by {@code Long#MAX_VALUE} steps.
     *
     * @param graph the graph to be iterated.
     *
     * @throws IllegalArgumentException if <code>graph==null</code> or does not
     * contain <code>startVertex</code>
     */
    public RandomWalkIterator(Graph<V, E> graph) {
        this(graph, null);
    }

    /**
     * Creates a new iterator for the specified graph. Iteration will start at
     * the specified start vertex. If the specified start vertex is <code>
     * null</code>, Iteration will start at an arbitrary graph vertex.
     * Walk is un-weighted and bounded by {@code Long#MAX_VALUE} steps.
     *
     * @param graph the graph to be iterated.
     * @param startVertex the vertex iteration to be started.
     *
     * @throws IllegalArgumentException if <code>graph==null</code> or does not
     * contain <code>startVertex</code>
     */
    public RandomWalkIterator(Graph<V, E> graph, V startVertex) {
        this(graph, startVertex, true);
    }
    
    /**
     * Creates a new iterator for the specified graph. Iteration will start at
     * the specified start vertex. If the specified start vertex is <code>
     * null</code>, Iteration will start at an arbitrary graph vertex.
     * Walk is bounded by {@code Long#MAX_VALUE} steps.
     *
     * @param graph the graph to be iterated.
     * @param startVertex the vertex iteration to be started.
     * @param isWeighted set to <code>true</code> if a weighted walk is desired.
     *
     * @throws IllegalArgumentException if <code>graph==null</code> or does not
     * contain <code>startVertex</code>
     */
    public RandomWalkIterator(Graph<V, E> graph, V startVertex, boolean isWeighted) {
        this(graph, startVertex, isWeighted, Long.MAX_VALUE);
    }
    
    /**
     * Creates a new iterator for the specified graph. Iteration will start at
     * the specified start vertex. If the specified start vertex is <code>
     * null</code>, Iteration will start at an arbitrary graph vertex.
     * Walk is bounded by the provided number steps.
     *
     * @param graph the graph to be iterated.
     * @param startVertex the vertex iteration to be started.
     * @param isWeighted set to <code>true</code> if a weighted walk is desired.
     * @param maxSteps number of steps before walk is exhausted.
     *
     * @throws IllegalArgumentException if <code>graph==null</code> or does not
     * contain <code>startVertex</code>
     */
    public RandomWalkIterator(Graph<V, E> graph, V startVertex, boolean isWeighted, long maxSteps) {        
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null");
        }
        //do not cross components.
        setCrossComponentTraversal(false);
        this.graph = graph;
        this.isWeighted = isWeighted;
        this.maxSteps = maxSteps;
        this.specifics = createGraphSpecifics(graph);
        //select a random start vertex in case not provided.
        if (startVertex == null) {
            if (graph.vertexSet().size() > 0) {
                currentVertex = graph.vertexSet().iterator().next();
            }
        } else if (graph.containsVertex(startVertex)){
            currentVertex = startVertex;            
        } else {
            throw new IllegalArgumentException("graph must contain the start vertex");
        }
        this.sinkReached = false;
        this.random = new Random();
    }

    /**
     * Check if this walk is exhausted. Calling {@link #next()} on
     * exhausted iterator will throw {@link NoSuchElementException}.
     * 
     * @return <code>true</code>if this iterator is exhausted,
     * <code>false</code> otherwise.
     */
    protected boolean isExhausted() {
        return maxSteps == 0;
    }
    
    /**
     * Update data structures every time we see a vertex.
     *
     * @param vertex the vertex encountered
     * @param edge the edge via which the vertex was encountered, or null if the
     * vertex is a starting point
     */
    protected void encounterVertex(V vertex, E edge) {
        maxSteps--;
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        return currentVertex != null && !isExhausted() &&
                !sinkReached;
    }

    /**
     * @see java.util.Iterator#next()
     */
    @Override
    public V next() {
        
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        
        Set<? extends E> potentialEdges = specifics.edgesOf(currentVertex);
        
        //randomly select an edge from the set of potential edges.
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
     * Randomly draws an edges out of the provided set. In case of un-weighted walk,
     * edge will be selected with uniform distribution across all outgoing edges.
     * In case of a weighted walk, edge will be selected with probability respective
     * to its weight across all outgoing edges.
     *   
     * @param edges the set to select the edge from
     * @return the drawn edges or null if set is empty.
     */
    private E drawEdge(Set<? extends E> edges) {
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

    private EdgeTraversalEvent<E> createEdgeTraversalEvent(E edge) {
        if (isReuseEvents()) {
            reusableEdgeEvent.setEdge(edge);

            return reusableEdgeEvent;
        } else {
            return new EdgeTraversalEvent<E>(this, edge);
        }
    }

    private VertexTraversalEvent<V> createVertexTraversalEvent(V vertex) {
        if (isReuseEvents()) {
            reusableVertexEvent.setVertex(vertex);

            return reusableVertexEvent;
        } else {
            return new VertexTraversalEvent<V>(this, vertex);
        }
    }    
    
    private double getTotalWeight(Collection<E> edges) {
        double total = 0;
        for (E e : edges) {
            total += graph.getEdgeWeight(e);
        }
        return total;
    }
}