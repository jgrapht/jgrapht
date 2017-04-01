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
/* -------------------------
 * StrongConnectivityInspectorTest.java
 * -------------------------
 * (C) Copyright 2007-2008, by France Telecom
 *
 * Original Author:  Ronald Chen.
 *
 * $Id$
 *
 * Changes
 * -------
 * 23-Jan-2013 : Initial revision (RC);
 *
 */
package org.jgrapht.alg;

import junit.framework.TestCase;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedSubgraph;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author Ronald Chen
 * @since January 23, 2013
 */
@SuppressWarnings("unchecked")
public class StrongConnectivityInspectorTest
    extends TestCase
{
    //~ Methods ----------------------------------------------------------------

    public void testOneStronglyOneWeaklyConnected()
    {
        DirectedGraph<Object, Object> graph = new DefaultDirectedGraph<Object, Object>(DefaultEdge.class);

        graph.addVertex("strong A");
        graph.addVertex("strong B");
        graph.addEdge("strong A", "strong B");
        graph.addEdge("strong B", "strong A");

        graph.addVertex("weak A");
        graph.addVertex("weak B");
        graph.addEdge("weak A", "weak B");

        StrongConnectivityInspector<Object, Object> inspector = new StrongConnectivityInspector<Object, Object>(graph);

        assertFalse(inspector.isStronglyConnected());

        Set<DirectedSubgraph<Object,Object>> expected = new HashSet<DirectedSubgraph<Object, Object>>();
        expected.add(createSubgraph(graph, createSet("strong A", "strong B"),
                createSet(graph.getEdge("strong A", "strong B"), graph.getEdge("strong B", "strong A"))));
        expected.add(createSubgraph(graph, createSet("weak A"), Collections.emptySet()));
        expected.add(createSubgraph(graph, createSet("weak B"), Collections.emptySet()));

        List<DirectedSubgraph<Object,Object>> result = inspector.stronglyConnectedSubgraphs();

        assertEquals(expected, asSet(result));
    }

    private static DirectedSubgraph<Object, Object> createSubgraph(DirectedGraph<Object, Object> graph, Set<Object> vertices, Set<Object> edges)
    {
        return new DirectedSubgraph<Object, Object>(graph, vertices, edges);
    }

    private static Set<Object> createSet(Object... members)
    {
        return new HashSet<Object>(Arrays.asList(members));
    }

    private static Set<DirectedSubgraph<Object,Object>> asSet(List<DirectedSubgraph<Object, Object>> list)
    {
        return new HashSet<DirectedSubgraph<Object, Object>>(list);
    }
}

// End StrongConnectivityInspectorTest.java
