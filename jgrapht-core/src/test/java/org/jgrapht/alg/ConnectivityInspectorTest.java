/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/* ------------------------------
 * ConnectivityInspectorTest.java
 * ------------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   John V. Sichi
 *
 * $Id$
 *
 * Changes
 * -------
 * 07-Aug-2003 : Initial revision (BN);
 * 20-Apr-2005 : Added StrongConnectivityInspector test (JVS);
 *
 */
package org.jgrapht.alg;

import junit.framework.TestCase;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.RingGraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedSubgraph;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.Pseudograph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * .
 *
 * @author Barak Naveh
 */
public class ConnectivityInspectorTest
    extends TestCase
{
    //~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";
    private static final String V4 = "v4";

    //~ Instance fields --------------------------------------------------------

    //
    DefaultEdge e1;
    DefaultEdge e2;
    DefaultEdge e3;
    DefaultEdge e3_b;
    DefaultEdge u;

    //~ Methods ----------------------------------------------------------------

    /**
     * .
     *
     * @return a graph
     */
    public Pseudograph<String, DefaultEdge> create()
    {
        final Pseudograph<String, DefaultEdge> g =
            new Pseudograph<String, DefaultEdge>(DefaultEdge.class);

        assertEquals(0, g.vertexSet().size());
        g.addVertex(V1);
        assertEquals(1, g.vertexSet().size());
        g.addVertex(V2);
        assertEquals(2, g.vertexSet().size());
        g.addVertex(V3);
        assertEquals(3, g.vertexSet().size());
        g.addVertex(V4);
        assertEquals(4, g.vertexSet().size());

        assertEquals(0, g.edgeSet().size());

        e1 = g.addEdge(V1, V2);
        assertEquals(1, g.edgeSet().size());

        e2 = g.addEdge(V2, V3);
        assertEquals(2, g.edgeSet().size());

        e3 = g.addEdge(V3, V1);
        assertEquals(3, g.edgeSet().size());

        e3_b = g.addEdge(V3, V1);
        assertEquals(4, g.edgeSet().size());
        assertNotNull(e3_b);

        u = g.addEdge(V1, V1);
        assertEquals(5, g.edgeSet().size());
        u = g.addEdge(V1, V1);
        assertEquals(6, g.edgeSet().size());

        return g;
    }

    /**
     * .
     */
    public void testDirectedGraph()
    {
        final ListenableDirectedGraph<String, DefaultEdge> g =
            new ListenableDirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);

        g.addEdge(V1, V2);

        final ConnectivityInspector<String, DefaultEdge> inspector =
            new ConnectivityInspector<String, DefaultEdge>(g);
        g.addGraphListener(inspector);

        assertEquals(false, inspector.isGraphConnected());

        g.addEdge(V1, V3);

        assertEquals(true, inspector.isGraphConnected());
    }

    /**
     * .
     */
    public void testIsGraphConnected()
    {
        final Pseudograph<String, DefaultEdge> g = create();
        ConnectivityInspector<String, DefaultEdge> inspector =
            new ConnectivityInspector<String, DefaultEdge>(g);

        assertEquals(false, inspector.isGraphConnected());

        g.removeVertex(V4);
        inspector = new ConnectivityInspector<String, DefaultEdge>(g);
        assertEquals(true, inspector.isGraphConnected());

        g.removeVertex(V1);
        assertEquals(1, g.edgeSet().size());

        g.removeEdge(e2);
        g.addEdge(V2, V2);
        assertEquals(1, g.edgeSet().size());

        inspector = new ConnectivityInspector<String, DefaultEdge>(g);
        assertEquals(false, inspector.isGraphConnected());
    }

    /**
     * .
     */
    public void testStronglyConnected1()
    {
        final DirectedGraph<String, DefaultEdge> g =
            new DefaultDirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);

        g.addEdge(V1, V2);
        g.addEdge(V2, V1); // strongly connected

        g.addEdge(V3, V4); // only weakly connected

        final StrongConnectivityInspector<String, DefaultEdge> inspector =
            new StrongConnectivityInspector<String, DefaultEdge>(g);

        // convert from List to Set because we need to ignore order
        // during comparison
        final Set<Set<String>> actualSets =
            new HashSet<Set<String>>(inspector.stronglyConnectedSets());

        // construct the expected answer
        final Set<Set<String>> expectedSets = new HashSet<Set<String>>();
        Set<String> set = new HashSet<String>();
        set.add(V1);
        set.add(V2);
        expectedSets.add(set);
        set = new HashSet<String>();
        set.add(V3);
        expectedSets.add(set);
        set = new HashSet<String>();
        set.add(V4);
        expectedSets.add(set);

        assertEquals(expectedSets, actualSets);

        actualSets.clear();

        final List<DirectedSubgraph<String, DefaultEdge>> subgraphs =
            inspector.stronglyConnectedSubgraphs();
        for (final DirectedSubgraph<String, DefaultEdge> sg : subgraphs) {
            actualSets.add(sg.vertexSet());

            final StrongConnectivityInspector<String, DefaultEdge> ci =
                new StrongConnectivityInspector<String, DefaultEdge>(sg);
            assertTrue(ci.isStronglyConnected());
        }

        assertEquals(expectedSets, actualSets);
    }

    /**
     * .
     */
    public void testStronglyConnected2()
    {
        final DirectedGraph<String, DefaultEdge> g =
            new DefaultDirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);

        g.addEdge(V1, V2);
        g.addEdge(V2, V1); // strongly connected

        g.addEdge(V4, V3); // only weakly connected
        g.addEdge(V3, V2); // only weakly connected

        final StrongConnectivityInspector<String, DefaultEdge> inspector =
            new StrongConnectivityInspector<String, DefaultEdge>(g);

        // convert from List to Set because we need to ignore order
        // during comparison
        final Set<Set<String>> actualSets =
            new HashSet<Set<String>>(inspector.stronglyConnectedSets());

        // construct the expected answer
        final Set<Set<String>> expectedSets = new HashSet<Set<String>>();
        Set<String> set = new HashSet<String>();
        set.add(V1);
        set.add(V2);
        expectedSets.add(set);
        set = new HashSet<String>();
        set.add(V3);
        expectedSets.add(set);
        set = new HashSet<String>();
        set.add(V4);
        expectedSets.add(set);

        assertEquals(expectedSets, actualSets);

        actualSets.clear();

        final List<DirectedSubgraph<String, DefaultEdge>> subgraphs =
            inspector.stronglyConnectedSubgraphs();
        for (final DirectedSubgraph<String, DefaultEdge> sg : subgraphs) {
            actualSets.add(sg.vertexSet());

            final StrongConnectivityInspector<String, DefaultEdge> ci =
                new StrongConnectivityInspector<String, DefaultEdge>(sg);
            assertTrue(ci.isStronglyConnected());
        }

        assertEquals(expectedSets, actualSets);
    }

    /**
     * .
     */
    public void testStronglyConnected3()
    {
        final DirectedGraph<String, DefaultEdge> g =
            new DefaultDirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);

        g.addEdge(V1, V2);
        g.addEdge(V2, V3);
        g.addEdge(V3, V1); // strongly connected

        g.addEdge(V1, V4);
        g.addEdge(V2, V4);
        g.addEdge(V3, V4); // weakly connected

        final StrongConnectivityInspector<String, DefaultEdge> inspector =
            new StrongConnectivityInspector<String, DefaultEdge>(g);

        // convert from List to Set because we need to ignore order
        // during comparison
        final Set<Set<String>> actualSets =
            new HashSet<Set<String>>(inspector.stronglyConnectedSets());

        // construct the expected answer
        final Set<Set<String>> expectedSets = new HashSet<Set<String>>();
        Set<String> set = new HashSet<String>();
        set.add(V1);
        set.add(V2);
        set.add(V3);
        expectedSets.add(set);
        set = new HashSet<String>();
        set.add(V4);
        expectedSets.add(set);

        assertEquals(expectedSets, actualSets);

        actualSets.clear();

        final List<DirectedSubgraph<String, DefaultEdge>> subgraphs =
            inspector.stronglyConnectedSubgraphs();

        for (final DirectedSubgraph<String, DefaultEdge> sg : subgraphs) {
            actualSets.add(sg.vertexSet());

            final StrongConnectivityInspector<String, DefaultEdge> ci =
                new StrongConnectivityInspector<String, DefaultEdge>(sg);
            assertTrue(ci.isStronglyConnected());
        }

        assertEquals(expectedSets, actualSets);
    }

    public void testStronglyConnected4()
    {
        final DefaultDirectedGraph<Integer, String> graph =
            new DefaultDirectedGraph<Integer, String>(
                new EdgeFactory<Integer, String>() {
                    @Override
                    public String createEdge(final Integer from, final Integer to)
                    {
                        return (from + "->" + to).intern();
                    }
                });

        new RingGraphGenerator<Integer, String>(3).generateGraph(
            graph,
            new VertexFactory<Integer>() {
                private int i = 0;

                @Override
                public Integer createVertex()
                {
                    return i++;
                }
            },
            null);

        final StrongConnectivityInspector<Integer, String> sc =
            new StrongConnectivityInspector<Integer, String>(
                graph);
        final Set<Set<Integer>> expected = new HashSet<Set<Integer>>();
        expected.add(graph.vertexSet());
        assertEquals(
            expected,
            new HashSet<Set<Integer>>(sc.stronglyConnectedSets()));
    }
}

// End ConnectivityInspectorTest.java
