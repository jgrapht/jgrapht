/*
 * (C) Copyright 2018-2018, by Assaf Mizrachi and Contributors.
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
/*
 * (C) Copyright 2018-2018, by Assaf Mizrachi and Contributors.
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
package org.jgrapht.alg.shortestpath;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;

/**
 * An implementation of Suurballe algorithm for finding K edge-<em>disjoint</em> shortest paths.
 * The algorithm determines the k disjoint shortest simple paths in increasing order of
 * weight. Only directed simple graphs are allowed.
 *
 * <p>
 * The algorithm is running k sequential Dijkstra iterations to find the shortest path at each step.
 * Hence, yielding a complexity of k*O(Dijkstra).
 * 
 * <p>
 * For further reference see <a href="https://en.wikipedia.org/wiki/Suurballe%27s_algorithm">
 * Wikipedia page </a>
 * <ul>
 * <li>
 * Suurballe, J. W.; Tarjan, R. E. (1984), A quick method for finding shortest pairs of disjoint paths.
 * </ul>
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Assaf Mizrachi
 * @since March 28, 2018
 */
public class SuurballeKDisjointShortestPaths<V, E> extends BaseKDisjointShortestPathsAlgorithm<V, E> {
    
    private ShortestPathAlgorithm.SingleSourcePaths<V, E> singleSourcePaths;
    
    /**
     * Creates a new instance of the algorithm.
     *
     * @param graph graph on which shortest paths are searched.
     *
     * @throws IllegalArgumentException if the graph is null.
     * @throws IllegalArgumentException if the graph is undirected.
     * @throws IllegalArgumentException if the graph is not simple.
     */
    public SuurballeKDisjointShortestPaths(Graph<V, E> graph) {
                         
        super(graph);
    }

    @Override
    protected void transformGraph(List<E> previousPath)
    {
        for (E edge : this.workingGraph.edgeSet()) {
            V source = workingGraph.getEdgeSource(edge);
            V target = workingGraph.getEdgeTarget(edge);
            double modifiedWeight = this.workingGraph.getEdgeWeight(edge)
                - singleSourcePaths.getWeight(target) + singleSourcePaths.getWeight(source);
            
            this.workingGraph.setEdgeWeight(edge, modifiedWeight);
        }

        E reversedEdge;
        
        for (E originalEdge : previousPath) {        
            double zeroWeight = workingGraph.getEdgeWeight(originalEdge);
            if (zeroWeight != 0) {
                throw new IllegalStateException("Expected zero weight edge along the path");
            }
            
            V source = workingGraph.getEdgeSource(originalEdge);
            V target = workingGraph.getEdgeTarget(originalEdge);
            
            workingGraph.removeEdge(originalEdge);             
            workingGraph.addEdge(target, source);
            reversedEdge = workingGraph.getEdge(target, source);
            workingGraph.setEdgeWeight(reversedEdge, zeroWeight);
        }
        
    }

    @Override
    protected GraphPath<V, E> calculateShortestPath(V startVertex, V endVertex)
    {
        this.singleSourcePaths = new DijkstraShortestPath<>(this.workingGraph).getPaths(startVertex);
        return singleSourcePaths.getPath(endVertex);
    }
        
}
