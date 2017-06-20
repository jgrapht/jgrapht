/*
 * (C) Copyright 2017-2017, by Karolina Rezkova and Contributors.
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
package org.jgrapht.alg.planarity;

import java.util.Iterator;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.alg.BiconnectivityInspector;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.*;

/**
 * Hopcroft Tarjan graph planarity testing algorithm.
 * 
 * This is an implementation of
 * <a href="https://www.cs.princeton.edu/courses/archive/fall05/cos528/handouts/Efficient%20Planarity.pdf">
 * Hopcroft Tarjan algorithm</a>, which tests if given graph is 
 * <a href="http://mathworld.wolfram.com/PlanarGraph.html">
 * planar</a>. This implementation has a runtime complexity of $O(|V|)$, where $V$ is 
 * the set of graph vertices. 
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Karolina Rezkova
 */
public class HopcroftTarjanPlanarityInspector<V, E> {
 
    private final Graph<V, E> graph;

    /**
     * constructor for HopcroftTarjanPlanarityInspector
     *
     * @param g the input graph
     */
    public HopcroftTarjanPlanarityInspector(Graph<V, E> g) {
        GraphTests.requireDirectedOrUndirected(g);
        if(g.getType().isUndirected())
          this.graph=g;
        else
            this.graph = new AsUndirectedGraph<>(g);
        }

    /**
     * Runs a test if graph is planar
     * tests eulerian formula
     *
     * @return true if graph is planar, false otherwise
     */
    public boolean isPlanar() {
        
        if (this.graph.vertexSet().size()<=4){
            return true;
        }

        if ((this.graph.edgeSet().size()) > (this.graph.vertexSet().size() * 3 - 6)) {
            return false;
        }
        
        ConnectivityInspector<V, E> ci = new ConnectivityInspector(this.graph);
        Iterator<Set<V>> cIter = ci.connectedSets().iterator();
        
        while (cIter.hasNext()) {
            
            Graph<V, E> subGraph = new AsSubgraph(this.graph, cIter.next());
            BiconnectivityInspector bi = new BiconnectivityInspector(subGraph);
            
            Set<Set<V>> biComponents = bi.getBiconnectedVertexComponents();
            Iterator<Set<V>> bc = biComponents.iterator();
            Set<V> component;
            HopcroftTarjanStrongPlanarityInspector spi;

            while (bc.hasNext()) {
                component = bc.next();
                spi = new HopcroftTarjanStrongPlanarityInspector(new AsSubgraph(this.graph, component));
                
                if (!spi.isComponentPlanar()) {
                    return false;
                }
            }
        }
        return true;
    }
}
