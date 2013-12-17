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
/* ------------------
 * CycleDetector.java
 * ------------------
 * (C) Copyright 2004-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 16-Sept-2004 : Initial revision (JVS);
 * 07-Jun-2005 : Made generic (CH);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.traverse.*;


/**
 * Performs cycle detection on a graph. The <i>inspected graph</i> is specified
 * at construction time and cannot be modified. Currently, the detector supports
 * only directed graphs.
 *
 * @author John V. Sichi
 * @since Sept 16, 2004
 */
public class CycleDetector<V, E>
{
    

    /**
     * Graph on which cycle detection is being performed.
     */
    DirectedGraph<V, E> graph;

    

    /**
     * Creates a cycle detector for the specified graph. Currently only directed
     * graphs are supported.
     *
     * @param graph the DirectedGraph in which to detect cycles
     */
    public CycleDetector(DirectedGraph<V, E> graph)
    {
        this.graph = graph;
    }

    

    /**
     * Performs yes/no cycle detection on the entire graph.
     *
     * @return true iff the graph contains at least one cycle
     */
    public boolean detectCycles()
    {
        try {
            execute(null, null);
        } catch (CycleDetectedException ex) {
            return true;
        }

        return false;
    }

    /**
     * Performs yes/no cycle detection on an individual vertex.
     *
     * @param v the vertex to test
     *
     * @return true if v is on at least one cycle
     */
    public boolean detectCyclesContainingVertex(V v)
    {
        try {
            execute(null, v);
        } catch (CycleDetectedException ex) {
            return true;
        }

        return false;
    }

    /**
     * Finds the vertex set for the subgraph of all cycles.
     *
     * @return set of all vertices which participate in at least one cycle in
     * this graph
     */
    public Set<V> findCycles()
    {
        // ProbeIterator can't be used to handle this case,
        // so use StrongConnectivityInspector instead.
        StrongConnectivityInspector<V, E> inspector =
            new StrongConnectivityInspector<V, E>(graph);
        List<Set<V>> components = inspector.stronglyConnectedSets();

        // A vertex participates in a cycle if either of the following is
        // true:  (a) it is in a component whose size is greater than 1
        // or (b) it is a self-loop

        Set<V> set = new HashSet<V>();
        for (Set<V> component : components) {
            if (component.size() > 1) {
                // cycle
                set.addAll(component);
            } else {
                V v = component.iterator().next();
                if (graph.containsEdge(v, v)) {
                    // self-loop
                    set.add(v);
                }
            }
        }

        return set;
    }

    /**
     * Finds the vertex set for the subgraph of all cycles which contain a
     * particular vertex.
     *
     * <p>REVIEW jvs 25-Aug-2006: This implementation is not guaranteed to cover
     * all cases. If you want to be absolutely certain that you report vertices
     * from all cycles containing v, it's safer (but less efficient) to use
     * StrongConnectivityInspector instead and return the strongly connected
     * component containing v.
     *
     * @param v the vertex to test
     *
     * @return set of all vertices reachable from v via at least one cycle
     */
    public Set<V> findCyclesContainingVertex(V v)
    {
        Set<V> set = new HashSet<V>();
        execute(set, v);

        return set;
    }

    private void execute(Set<V> s, V v)
    {
        ProbeIterator iter = new ProbeIterator(s, v);

        while (iter.hasNext()) {
            iter.next();
        }
    }

    

    /**
     * Exception thrown internally when a cycle is detected during a yes/no
     * cycle test. Must be caught by top-level detection method.
     */
    private static class CycleDetectedException
        extends RuntimeException
    {
        private static final long serialVersionUID = 3834305137802950712L;
    }

    /**
     * Version of DFS which maintains a backtracking path used to probe for
     * cycles.
     */
    private class ProbeIterator
        extends DepthFirstIterator<V, E>
    {
        private List<V> path;
        private Set<V> cycleSet;
        private V root;

        ProbeIterator(Set<V> cycleSet, V startVertex)
        {
            super(graph, startVertex);
            root = startVertex;
            this.cycleSet = cycleSet;
            path = new ArrayList<V>();
        }

        /**
         * {@inheritDoc}
         */
        protected void encounterVertexAgain(V vertex, E edge)
        {
            super.encounterVertexAgain(vertex, edge);

            int i;

            if (root != null) {
                // For rooted detection, the path must either
                // double back to the root, or to a node of a cycle
                // which has already been detected.
                if (vertex.equals(root)) {
                    i = 0;
                } else if ((cycleSet != null) && cycleSet.contains(vertex)) {
                    i = 0;
                } else {
                    return;
                }
            } else {
                i = path.indexOf(vertex);
            }

            if (i > -1) {
                if (cycleSet == null) {
                    // we're doing yes/no cycle detection
                    throw new CycleDetectedException();
                } else {
                    for (; i < path.size(); ++i) {
                        cycleSet.add(path.get(i));
                    }
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        protected V provideNextVertex()
        {
            V v = super.provideNextVertex();

            // backtrack
            for (int i = path.size() - 1; i >= 0; --i) {
                if (graph.containsEdge(path.get(i), v)) {
                    break;
                }

                path.remove(i);
            }

            path.add(v);

            return v;
        }
    }
}

// End CycleDetector.java
