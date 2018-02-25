/*
 * (C) Copyright 2017-2017, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.color;


import java.util.*;

import org.jgrapht.util.*;
import org.jgrapht.*;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.traverse.CrossComponentIterator;


/**
 * A 2-coloring algorithm for bipartite graphs using a DFS approach.
 * The running time of the algorithm is O(V+E).
 * 
 * @author Meghana M Reddy
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class BipartiteColoring<V, E> extends CrossComponentIterator<V, E, BipartiteColoring.VisitColor>
{
    /**
     * Sentinel object. Unfortunately, we can't use null, because ArrayDeque won't accept those. And
     * we don't want to rely on the caller to provide a sentinel object for us. So we have to play
     * typecasting games.
     */
    public static final Object SENTINEL = new Object();
    
    /**
     * Stores the final colors of the vertices after finding a 2-coloring.
     */
    Map<V, Integer> bipartiteColors = new HashMap<>();
    
    /**
     * Stores the color given to the predecessor/parents of the vertex.
     */
    private Map<V, BipartiteColoring.VisitColor> parentColor = new HashMap<>();
    
    /**
     * A variable which stores if the graph is bipartite or not.
     */
    boolean isGraphBipartite = true;
    
    /**
     * Temporary variable used to store the color of previously colored vertex.
     */
    protected VisitColor prevColor = VisitColor.GRAY1;

    /**
     * Modified vertex visit state enumeration.
     */
    protected static enum VisitColor
    {
        /**
         * Vertex has not been returned via iterator yet.
         */
        WHITE,

        /**
         * Vertex has been returned via iterator, but we're not done with all of its out-edges yet.
         * Depending on the color of the predecessor/parent, either color 1 or 2 is assigned.
         */
        GRAY1,
        GRAY2,

        /**
         * Vertex has been returned via iterator, and we're done with all of its out-edges.
         * We store the final color.
         */
        BLACK1,
        BLACK2
    }
    

    private Deque<Object> stack = new ArrayDeque<>();

    /**
     * Creates a new depth-first iterator (for bipartite coloring) for the specified graph.
     *
     * @param g the graph to be iterated.
     */
    public BipartiteColoring(Graph<V, E> g)
    {
        this(g, (V) null);
    }

    /**
     * Creates a new depth-first iterator for the specified graph. Iteration will start at the
     * specified start vertex and will be limited to the connected component that includes that
     * vertex. If the specified start vertex is <code>null</code>, iteration will start at an
     * arbitrary vertex and will not be limited, that is, will be able to traverse all the graph.
     *
     * @param g the graph to be iterated.
     * @param startVertex the vertex iteration to be started.
     */
    public BipartiteColoring(Graph<V, E> g, V startVertex)
    {
        super(g, startVertex);
    }

    /**
     * Creates a new depth-first iterator for the specified graph. Iteration will start at the
     * specified start vertices and will be limited to the connected component that includes those
     * vertices. If the specified start vertices is <code>null</code>, iteration will start at an
     * arbitrary vertex and will not be limited, that is, will be able to traverse all the graph.
     *
     * @param g the graph to be iterated.
     * @param startVertices the vertices iteration to be started.
     */
    public BipartiteColoring(Graph<V, E> g, Iterable<V> startVertices)
    {
        super(g, startVertices);
    }

    @Override
    protected boolean isConnectedComponentExhausted()
    {
        for (;;) {
            if (stack.isEmpty()) {
                return true;
            }
            if (stack.getLast() != SENTINEL) {
                // Found a non-sentinel.
                return false;
            }

            // Found a sentinel: pop it, record the finish time,
            // and then loop to check the rest of the stack.

            // Pop null we peeked at above.
            stack.removeLast();

            // This will pop corresponding vertex to be recorded as finished.
            recordFinish();
        }
    }

    @Override
    protected void encounterVertex(V vertex, E edge)
    {
        putSeenData(vertex, VisitColor.WHITE);
        parentColor.put(vertex, prevColor);
        stack.addLast(vertex);
    }

    @Override
    protected void encounterVertexAgain(V vertex, E edge)
    {
        VisitColor color = getSeenData(vertex);
        if (color != VisitColor.WHITE) {
            // We've already visited this vertex and given it a color;
	    // If it has the same color as its neighbor (through which we visited this vertex)
            // The graph is not bipartite
        	if (color == prevColor) {
        		isGraphBipartite = false;
        	}
            return;
        }

        // Since we've encountered it before, and it's still WHITE, it
        // *must* be on the stack. Use removeLastOccurrence on the
        // assumption that for typical topologies and traversals,
        // it's likely to be nearer the top of the stack than
        // the bottom of the stack.
        boolean found = stack.removeLastOccurrence(vertex);
        assert (found);
        stack.addLast(vertex);
    }

    @Override
    protected V provideNextVertex()
    {
        V v;
        for (;;) {
            Object o = stack.removeLast();
            if (o == SENTINEL) {
                // This is a finish-time sentinel we previously pushed.
                recordFinish();
                // Now carry on with another pop until we find a non-sentinel
            } else {
                // Got a real vertex to start working on
                v = TypeUtil.uncheckedCast(o, null);
                break;
            }
        }

        // Push a sentinel for v onto the stack so that we'll know
        // when we're done with it.
        stack.addLast(v);
        stack.addLast(SENTINEL);
        
        // We give a color to the vertex that is different from its parent/predecessor
        // We also update the variable prevColor.
        BipartiteColoring.VisitColor pc = parentColor.get(v);
        
        putSeenData(v, (pc == VisitColor.GRAY1 ? VisitColor.GRAY2 : VisitColor.GRAY1));
        prevColor = (pc == VisitColor.GRAY2 ? VisitColor.GRAY1 : VisitColor.GRAY2);
        
        return v;
    }

    private void recordFinish()
    {
        V v = TypeUtil.uncheckedCast(stack.removeLast(), null);
        if (getSeenData(v) == VisitColor.GRAY1) {
        	putSeenData(v, VisitColor.BLACK1);
            bipartiteColors.put(v, 1);
        }
        else {
        	putSeenData(v, VisitColor.BLACK2);
            bipartiteColors.put(v, 2);
        }
        finishVertex(v);
    }

    /**
     * Retrieves the LIFO stack of vertices which have been encountered but not yet visited (WHITE).
     * This stack also contains <em>sentinel</em> entries representing vertices which have been
     * visited but are still GRAY. A sentinel entry is a sequence (v, SENTINEL), whereas a
     * non-sentinel entry is just (v).
     *
     * @return stack
     */
    public Deque<Object> getStack()
    {
        return stack;
    }
    
    /**
     * @return true if graph is bipartite, false otherwise
     */
    public boolean isGraphBipartite() {
    	return isGraphBipartite;
    }
    
    /**
     * 
     * @return a Map - with the vertices and their corresponding colors.
     */
    public Map<V, Integer> getColors() {
    	return bipartiteColors;
    }
    
    /**
     * 
     * @param graph - for which a 2-coloring is required
     * @return a map - with the vertices and their corresponding colors.
     */
    public Map<V, Integer> findTwoColoring(Graph<V, E> graph) {
    	if (((AbstractBaseGraph<V, E>) graph).isDirected() == true) {
			throw new IllegalArgumentException("Input graph needs to be undirected");
		}
		
	    while (hasNext()) {
	        next();
	        if (isGraphBipartite() == false) {
	        	throw new IllegalArgumentException("Input graph is not bipartite");
	        }
	    }
	    return getColors();
    }

}
