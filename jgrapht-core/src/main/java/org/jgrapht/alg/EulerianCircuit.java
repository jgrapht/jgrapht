/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* -------------------
 * EulerianCircuit.java
 * -------------------
 * (C) Copyright 2008-2008, by Andrew Newell and Contributors.
 *
 * Original Author:  Andrew Newell
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Dec-2008 : Initial revision (AN);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;


/**
 * This algorithm will check whether a graph is Eulerian (hence it contains an
 * <a href="http://mathworld.wolfram.com/EulerianCircuit.html">Eulerian
 * circuit</a>). Also, if a graph is Eulerian, the caller can obtain a list of
 * vertices making up the Eulerian circuit. An Eulerian circuit is a circuit
 * which traverses each edge exactly once.
 *
 * @author Andrew Newell
 * @since Dec 21, 2008
 */
public abstract class EulerianCircuit
{
    

    /**
     * This method will check whether the graph passed in is Eulerian or not.
     *
     * @param g The graph to be checked
     *
     * @return true for Eulerian and false for non-Eulerian
     */
    public static <V, E> boolean isEulerian(UndirectedGraph<V, E> g)
    {
        // If the graph is not connected, then no Eulerian circuit exists
        if (!(new ConnectivityInspector<V, E>(g)).isGraphConnected()) {
            return false;
        }

        // A graph is Eulerian if and only if all vertices have even degree
        // So, this code will check for that
        Iterator<V> iter = g.vertexSet().iterator();
        while (iter.hasNext()) {
            V v = iter.next();
            if ((g.degreeOf(v) % 2) == 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method will return a list of vertices which represents the Eulerian
     * circuit of the graph.
     *
     * @param g The graph to find an Eulerian circuit
     *
     * @return null if no Eulerian circuit exists, or a list of vertices
     * representing the Eulerian circuit if one does exist
     */
    public static <V, E> List<V> getEulerianCircuitVertices(
        UndirectedGraph<V, E> g)
    {
        // If the graph is not Eulerian then just return a null since no
        // Eulerian circuit exists
        if (!isEulerian(g)) {
            return null;
        }

        // The circuit will be represented by a linked list
        List<V> path = new LinkedList<V>();
        UndirectedGraph<V, E> sg = new UndirectedSubgraph<V, E>(g, null, null);
        path.add(sg.vertexSet().iterator().next());

        // Algorithm for finding an Eulerian circuit Basically this will find an
        // arbitrary circuit, then it will find another arbitrary circuit until
        // every edge has been traversed
        while (sg.edgeSet().size() > 0) {
            V v = null;

            // Find a vertex which has an edge that hasn't been traversed yet,
            // and keep its index position in the circuit list
            int index = 0;
            for (Iterator<V> iter = path.iterator(); iter.hasNext(); index++) {
                v = iter.next();
                if (sg.degreeOf(v) > 0) {
                    break;
                }
            }

            // Finds an arbitrary circuit of the current vertex and
            // appends this into the circuit list
            while (sg.degreeOf(v) > 0) {
                for (
                    Iterator<V> iter = sg.vertexSet().iterator();
                    iter.hasNext();)
                {
                    V temp = iter.next();
                    if (sg.containsEdge(v, temp)) {
                        path.add(index, temp);
                        sg.removeEdge(v, temp);
                        v = temp;
                        break;
                    }
                }
            }
        }
        return path;
    }
}

// End EulerianCircuit.java
