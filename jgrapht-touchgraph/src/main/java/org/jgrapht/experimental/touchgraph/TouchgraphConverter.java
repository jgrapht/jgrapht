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
 * TouchgraphConverter.java
 * -------------------
 * (C) Copyright 2006-2008, by Carl Anderson and Contributors.
 *
 * Original Author:  Carl Anderson
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 8-May-2006 : Initial revision (CA);
 *
 */
package org.jgrapht.experimental.touchgraph;

import com.touchgraph.graphlayout.*;

import java.util.*;

import org.jgrapht.*;


/**
 * A Converter class that converts a JGraphT graph to that used in the
 * TouchGraph library.
 *
 * @author canderson
 */
public class TouchgraphConverter<V, E>
{
    //~ Methods ----------------------------------------------------------------

    /**
     * Convert a JGraphT graph to the representation used in the TouchGraph
     * library. http://sourceforge.net/projects/touchgraph TouchGraph doesn't
     * have a sensible, extensible graph object class and so one has to add them
     * to a TGPanel which will store the graph components (the set of nodes and
     * edges) in its own way. The closest Touchgraph has to a graph object is a
     * GraphEltSet but Touchgraph does not provide the visibility to use it
     * easily and one can use a JGraphT graph. While JGraphT nodes can be any
     * type of objects, TouchGraph uses a set of com.touchgraph.graphlayout.Node
     * and com.touchgraph.graphlayout.Edge only. Moreover, TouchGraph edges are
     * always directed. Having said that, if you want a nice way to visualize
     * and explore a graph, especially large complex graphs, TouchGraph is very
     * nice
     *
     * @param graph: the JGraphT graph
     * @param tgPanel: the TouchGraph TGPanel
     * @param selfReferencesAllowed: do you want to include self-referenctial
     * edges, ie an edge from a node to itself? Self-referential loops do not
     * show up in the TG visualization but you may want to subclass TG's Node
     * class to show them
     *
     * @return first node of the TouchGraph graph
     *
     * @throws TGException
     */
    public Node convertToTouchGraph(
        Graph<V, E> graph,
        TGPanel tgPanel,
        boolean selfReferencesAllowed)
        throws TGException
    {
        List<V> jgtNodes = new ArrayList<V>(graph.vertexSet());
        Node [] tgNodes = new Node[jgtNodes.size()];

        // add all the nodes...
        for (int i = 0; i < jgtNodes.size(); i++) {
            Node n;
            if (jgtNodes.get(i) instanceof Node) {
                // if our JGraphT object was a touchGraph node, add it unaltered
                n = (Node) jgtNodes.get(i);
            } else {
                // create a TG Node with a "label" and "id" equals to the
                // objects toString() value
                n = new Node(jgtNodes.get(i).toString());
            }

            // store this for edge-related creation below
            tgNodes[i] = n;

            // add the node to the TG panel
            tgPanel.addNode(n);
        }

        // add the edges...
        for (int i = 0; i < tgNodes.length; i++) {
            for (int j = 0; j < tgNodes.length; j++) {
                // self-referential loops do not show up in the TG
                // visualization but you may want to
                // subclass TG's Node class to show them
                if ((i != j) || selfReferencesAllowed) {
                    if (graph.getEdge(jgtNodes.get(i), jgtNodes.get(j))
                        != null)
                    {
                        // add TG directed edge from i to j
                        tgPanel.addEdge(new Edge(tgNodes[i], tgNodes[j]));
                    }
                }
            }
        }

        // return the first node as a focal point for the TG panel
        return tgNodes[0];
    }
}

// End TouchgraphConverter.java
