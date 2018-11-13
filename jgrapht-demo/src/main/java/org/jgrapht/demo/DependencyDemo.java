/*
 * (C) Copyright 2012-2018, by Rob Janes and Contributors.
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

import org.jgrapht.*;
import org.jgrapht.alg.cycle.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;

import java.util.*;

/***
 * This class is a demonstration program for creating a dependency chart, directed graph, then
 * locating and outputting any implicit loops, cycles.
 **/
public class DependencyDemo
{

    /**
     * Test creating a directed graph, checking it for cycles and either outputting cycles detected
     * or topological ordering if not.
     * 
     * @param createCycles true - create a directed graph which contains cycles. false - create a
     *        directed graph which does not contain any cycles.
     */
    public static void test(boolean createCycles)
    {
        CycleDetector<String, DefaultEdge> cycleDetector;
        Graph<String, DefaultEdge> g;

        g = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

        // Add vertices
        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("e");

        // Add edges

        g.addEdge("b", "a");
        g.addEdge("c", "b");
        if (createCycles) {
            g.addEdge("a", "c");
        }
        g.addEdge("e", "d");
        if (createCycles) {
            g.addEdge("d", "e");
        }

        // Printing the vetrices and the edges
        System.out.println(g.toString());

        // Checking for cycles in the dependencies
        cycleDetector = new CycleDetector<String, DefaultEdge>(g);

        // Cycle(s) detected.
        if (cycleDetector.detectCycles()) {
            Iterator<String> iterator;
            Set<String> cycleVertices;
            Set<String> subCycle;
            String cycle;

            System.out.println("Cycles detected.");

            // Get all vertices involved in cycles.
            cycleVertices = cycleDetector.findCycles();

            // Loop through vertices trying to find disjoint cycles.
            while (!cycleVertices.isEmpty()) {
                System.out.println("Cycle:");

                // Get a vertex involved in a cycle.
                iterator = cycleVertices.iterator();
                cycle = iterator.next();

                // Get all vertices involved with this vertex.
                subCycle = cycleDetector.findCyclesContainingVertex(cycle);
                for (String sub : subCycle) {
                    System.out.println("   " + sub);
                    // Remove vertex so that this cycle is not encountered again
                    cycleVertices.remove(sub);
                }
            }
        }

        // If no cycles are detected, output vertices topologically ordered
        else {
            String v;
            TopologicalOrderIterator<String, DefaultEdge> orderIterator;

            orderIterator = new TopologicalOrderIterator<String, DefaultEdge>(g);
            System.out.println("\nTopological Ordering:");
            while (orderIterator.hasNext()) {
                v = orderIterator.next();
                System.out.println(v);
            }
        }
    }

    /**
     * Generate two cases, one with cycles, this is dependencies and one without.
     * 
     * @param args Ignored.
     */
    public static void main(String[] args)
    {
        System.out.println("\nCase 1: There are cycles.");
        test(true);

        System.out.println("\nCase 2: There are no cycles.");
        test(false);

        System.out.println("\nAll done");
        System.exit(0);
    }
}
