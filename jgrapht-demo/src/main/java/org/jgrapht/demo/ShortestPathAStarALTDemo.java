/*
 * (C) Copyright 2026-2026, by federicodeca and Contributors.
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
package org.jgrapht.demo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import org.jgrapht.alg.shortestpath.ALTAdmissibleHeuristic;
import org.jgrapht.alg.shortestpath.AStarShortestPath;
import org.jgrapht.generate.GridGraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.util.SupplierUtil;


/**
 * Demo comparing A* shortest path search with a trivial (zero) heuristic against A* guided
 * by the ALT (A*, Landmarks, Triangle inequality) heuristic.
 *
 * <p>
 * ALT precomputes shortest-path distances from a small set of landmark vertices and uses the
 * triangle inequality to derive a tight, admissible lower-bound estimate of the remaining
 * distance to the target. This lets A* skip exploring large portions of the graph without ever
 * sacrificing the guarantee of finding the true shortest path.
 *
 * <p>
 * The demo builds a weighted grid graph, computes the shortest path between two opposite
 * corners using both heuristics, and reports how many vertices each search had to expand.
 * Both approaches must agree on the path weight -- ALT only changes search efficiency, never
 * correctness.
 *
 * @author federicodeca
 */

public final class ShortestPathAStarALTDemo {

    private ShortestPathAStarALTDemo(){
    }

  
        /**
     * Main demo entry point.
     *
     * @param args command line arguments
     */
    public static void main(String[] args)
    {

    // create a weighted graph sample
    Graph<Integer, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(
    SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER);

    // we create a structure of edges 
    Map<String, Integer> corners = new HashMap<>();
    GridGraphGenerator<Integer, DefaultWeightedEdge> generator = new GridGraphGenerator<>(20,20);
    generator.generateGraph(graph, corners); 
    
    // assign weights to connections
    Random random = new Random(42); 
        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            graph.setEdgeWeight(edge, 1.0 + random.nextDouble() * 9.0);
        }


    Integer source = corners.get(GridGraphGenerator.CORNER_VERTEX + " 1");
    Integer target = corners.get(GridGraphGenerator.CORNER_VERTEX + " 4");
    Set<Integer> landmarks = new HashSet<>(corners.values());


    // launching AStar with h(x)==0 (Dikjstra)
    AStarAdmissibleHeuristic<Integer> zeroHeuristic = (u, v) -> 0.0;
    AStarShortestPath<Integer, DefaultWeightedEdge> plainAStar =
        new AStarShortestPath<>(graph, zeroHeuristic);
    GraphPath<Integer, DefaultWeightedEdge> plainPath = plainAStar.getPath(source, target);

    // launching AStar with alt heuristic applied 
    AStarAdmissibleHeuristic<Integer> altHeuristic = new ALTAdmissibleHeuristic<>(graph, landmarks);
    AStarShortestPath<Integer, DefaultWeightedEdge> altAStar =
        new AStarShortestPath<>(graph, altHeuristic);
    GraphPath<Integer, DefaultWeightedEdge> altPath = altAStar.getPath(source, target);
    
    //printing results
    System.out.println("Plain A* (zero heuristic):");
    System.out.println("  Path weight: " + plainPath.getWeight());
    System.out.println("  Vertices expanded: " + plainAStar.getNumberOfExpandedNodes());

    System.out.println("A* with ALT heuristic:");
    System.out.println("  Path weight: " + altPath.getWeight());
    System.out.println("  Vertices expanded: " + altAStar.getNumberOfExpandedNodes());

    }
}
