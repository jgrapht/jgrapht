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
/* ------------------------
 * SubgraphTest.java
 * ------------------------
 * (C) Copyright 2003-2008, by Michael Behrisch and Contributors.
 *
 * Original Author:  Michael Behrisch
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 21-Sep-2004 : Initial revision (MB);
 *
 */
package org.jgrapht.graph;

import java.util.*;

import junit.framework.*;

import org.jgrapht.*;


/**
 * Unit test for {@link Subgraph} class.
 *
 * @author Michael Behrisch
 * @since Sep 21, 2004
 */
public class SubgraphTest
    extends TestCase
{
    //~ Instance fields --------------------------------------------------------

    private String v1 = "v1";
    private String v2 = "v2";
    private String v3 = "v3";
    private String v4 = "v4";

    //~ Constructors -----------------------------------------------------------

    /**
     * @see junit.framework.TestCase#TestCase(java.lang.String)
     */
    public SubgraphTest(String name)
    {
        super(name);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    public void testInducedSubgraphListener()
    {
        UndirectedGraph<String, DefaultEdge> g = init(true);
        UndirectedSubgraph<String, DefaultEdge> sub =
            new UndirectedSubgraph<String, DefaultEdge>(g, null, null);

        assertEquals(g.vertexSet(), sub.vertexSet());
        assertEquals(g.edgeSet(), sub.edgeSet());

        g.addEdge(v3, v4);

        assertEquals(g.vertexSet(), sub.vertexSet());
        assertEquals(g.edgeSet(), sub.edgeSet());
    }

    /**
     * Tests Subgraph.
     */
    public void testSubgraph()
    {
        UndirectedGraph<String, DefaultEdge> g = init(false);
        UndirectedSubgraph<String, DefaultEdge> sub =
            new UndirectedSubgraph<String, DefaultEdge>(g, null, null);

        assertEquals(g.vertexSet(), sub.vertexSet());
        assertEquals(g.edgeSet(), sub.edgeSet());

        Set<String> vset = new HashSet<String>(g.vertexSet());
        g.removeVertex(v1);
        assertEquals(vset, sub.vertexSet()); // losing track

        g = init(false);
        vset = new HashSet<String>();
        vset.add(v1);
        sub = new UndirectedSubgraph<String, DefaultEdge>(g, vset, null);
        assertEquals(vset, sub.vertexSet());
        assertEquals(0, sub.degreeOf(v1));
        assertEquals(Collections.EMPTY_SET, sub.edgeSet());

        vset.add(v2);
        vset.add(v3);
        sub =
            new UndirectedSubgraph<String, DefaultEdge>(
                g,
                vset,
                new HashSet<DefaultEdge>(g.getAllEdges(v1, v2)));
        assertEquals(vset, sub.vertexSet());
        assertEquals(1, sub.edgeSet().size());
    }

    /**
     * .
     */
    public void testSubgraphListener()
    {
        UndirectedGraph<String, DefaultEdge> g = init(true);
        UndirectedSubgraph<String, DefaultEdge> sub =
            new UndirectedSubgraph<String, DefaultEdge>(g, null, null);

        assertEquals(g.vertexSet(), sub.vertexSet());
        assertEquals(g.edgeSet(), sub.edgeSet());

        Set<String> vset = new HashSet<String>(g.vertexSet());
        g.removeVertex(v1);
        vset.remove(v1);
        assertEquals(vset, sub.vertexSet()); // not losing track
        assertEquals(g.edgeSet(), sub.edgeSet());
    }

    private UndirectedGraph<String, DefaultEdge> init(boolean listenable)
    {
        UndirectedGraph<String, DefaultEdge> g;

        if (listenable) {
            g = new ListenableUndirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);
        } else {
            g = new SimpleGraph<String, DefaultEdge>(
                DefaultEdge.class);
        }

        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addEdge(v1, v2);
        g.addEdge(v2, v3);
        g.addEdge(v3, v1);
        g.addEdge(v1, v4);

        return g;
    }

    public void testInducedSubgraphUnderlyingEdgeAddition()
    {
        ListenableGraph<Object, DefaultEdge> baseGraph =
            new ListenableUndirectedGraph<Object, DefaultEdge>(
                DefaultEdge.class);

        baseGraph.addVertex(v1);
        baseGraph.addVertex(v2);

        Set<Object> initialVertexes = new LinkedHashSet<Object>();
        initialVertexes.add(v1);
        Subgraph<Object, DefaultEdge, ListenableGraph<Object, DefaultEdge>> subgraph =
            new Subgraph<Object,
                DefaultEdge, ListenableGraph<Object, DefaultEdge>>(
                baseGraph,
                initialVertexes,
                null);
        baseGraph.addEdge(v1, v2);

        assertFalse(subgraph.containsEdge(v1, v2));
    }
}

// End SubgraphTest.java
