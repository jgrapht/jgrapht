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
/* ---------------------------
 * TopologicalOrderIteratorTest.java
 * ---------------------------
 * (C) Copyright 2005-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 25-Apr-2005 : Initial revision (JVS);
 *
 */
package org.jgrapht.traverse;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;


/**
 * Tests for TopologicalOrderIterator.
 *
 * @author John V. Sichi
 * @since Apr 25, 2005
 */
public class TopologicalOrderIteratorTest
    extends EnhancedTestCase
{
    //~ Methods ----------------------------------------------------------------

    /**
     * Tests graph traversal in topological order on a connected DAG with a
     * total order.
     */
    public void testRecipe()
    {
        DirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

        String [] v = new String[9];

        v[0] = "preheat oven";
        v[1] = "sift dry ingredients";
        v[2] = "stir wet ingredients";
        v[3] = "mix wet and dry ingredients";
        v[4] = "spoon onto pan";
        v[5] = "bake";
        v[6] = "cool";
        v[7] = "frost";
        v[8] = "eat";

        // add in mixed up order
        graph.addVertex(v[4]);
        graph.addVertex(v[8]);
        graph.addVertex(v[1]);
        graph.addVertex(v[3]);
        graph.addVertex(v[7]);
        graph.addVertex(v[6]);
        graph.addVertex(v[0]);
        graph.addVertex(v[2]);
        graph.addVertex(v[5]);

        // specify enough edges to guarantee deterministic total order
        graph.addEdge(v[0], v[1]);
        graph.addEdge(v[1], v[2]);
        graph.addEdge(v[0], v[2]);
        graph.addEdge(v[1], v[3]);
        graph.addEdge(v[2], v[3]);
        graph.addEdge(v[3], v[4]);
        graph.addEdge(v[4], v[5]);
        graph.addEdge(v[5], v[6]);
        graph.addEdge(v[6], v[7]);
        graph.addEdge(v[7], v[8]);
        graph.addEdge(v[6], v[8]);

        Iterator<String> iter =
            new TopologicalOrderIterator<String, DefaultEdge>(graph);
        int i = 0;

        while (iter.hasNext()) {
            assertEquals(v[i], iter.next());
            ++i;
        }

        // Test with a reversed view
        DirectedGraph<String, DefaultEdge> reversed =
            new EdgeReversedGraph<String, DefaultEdge>(graph);

        iter = new TopologicalOrderIterator<String, DefaultEdge>(reversed);
        i = v.length - 1;

        while (iter.hasNext()) {
            assertEquals(v[i], iter.next());
            --i;
        }
    }

    /**
     * Tests graph traversal in topological order on an empty graph.
     */
    public void testEmptyGraph()
    {
        DirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        Iterator<String> iter =
            new TopologicalOrderIterator<String, DefaultEdge>(graph);
        assertFalse(iter.hasNext());
    }

    /**
     * Tests graph traversal in topological order on a directed graph that is
     * cyclic and contains no component with a source vertex.
     * Creating the iterator should throw an exception.
     */
    public void testCyclicDigraph()
    {
        DirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

        graph.addVertex("1");
        graph.addVertex("2");

        graph.addEdge("1", "2");
        graph.addEdge("2", "1");
        
        try {
            new TopologicalOrderIterator<String, DefaultEdge>(graph);
            fail("Expected an IllegalArgumentException to be thrown.");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    /**
     * Tests graph traversal in topological order on a disconnected DAG
     * without edges.
     */
    public void testDisconnectedDigraphWithoutEdges()
    {
        DirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

        graph.addVertex("1");
        graph.addVertex("2");

        Iterator<String> iter =
            new TopologicalOrderIterator<String, DefaultEdge>(graph);

        for (int i = 0; i < graph.vertexSet().size(); i++) {
            assertTrue(iter.hasNext());
            iter.next();
        }
    }

    /**
     * Tests graph traversal in topological order on a connected DAG with
     * two partial orders.
     */
    public void testConnectedDigraphWithTwoPartialOrders()
    {
        DirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

        graph.addVertex("1");
        graph.addVertex("2");
        graph.addVertex("3");

        graph.addEdge("1", "3");
        graph.addEdge("2", "3");

        assertPartialOrder(
           graph, 3,
           "1", "3",
           "2", "3"
        );
    }

    /**
     * Tests graph traversal in topological order on a disconnected DAG with
     * several partial orders.
     * The graph also contains a cyclic component that is not visited by the
     * traversal.
     */
    public void testDisconnectedDigraphWithSeveralPartialOrders()
    {
        DirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

        graph.addVertex("1");
        graph.addVertex("2");
        graph.addVertex("3");

        graph.addEdge("1", "3");
        graph.addEdge("2", "3");
        
        graph.addVertex("4");
        graph.addVertex("5");
        graph.addVertex("6");
        graph.addVertex("7");

        graph.addEdge("4", "6");
        graph.addEdge("5", "6");
        graph.addEdge("6", "7");

        // the cyclic component that is not visited by the traversal
        graph.addVertex("8");
        graph.addVertex("9");
        graph.addEdge("8", "9");
        graph.addEdge("9", "8");
        
        assertPartialOrder(
           graph, 7,
           "1", "3",
           "2", "3",
           "4", "6",
           "4", "7",
           "5", "6",
           "5", "7",
           "6", "7"
        );
    }
    
    /**
     * Checks the topological order of a graph's vertices as determined by a
     * {@link TopologicalOrderIterator}.
     * 
     * @param graph the graph to traverse
     * @param numberOfVisitedVertices the expected number of vertices
     * encountered in the traversal
     * @param pairsOfVertices a variable number of pairs of vertices;
     * for every pair (a,b), it is asserted that a < b according to the
     * topological order
     */
    private void assertPartialOrder(
        DirectedGraph<String, DefaultEdge> graph,
        int numberOfVisitedVertices,
        String... pairsOfVertices)
    {
        assert pairsOfVertices.length % 2 == 0;
        Iterator<String> iter =
            new TopologicalOrderIterator<String, DefaultEdge>(graph);
        
        List<String> partialOrder = new ArrayList<String>();
        while (iter.hasNext()) {
            partialOrder.add(iter.next());
        }
        
        assertEquals(numberOfVisitedVertices, partialOrder.size());
        
//        int numberOfPairs = pairsOfVertices.length / 2;
        for (int i = 0; i < pairsOfVertices.length; i+=2) {
            int indexOfFirst = partialOrder.indexOf(pairsOfVertices[i]);
            int indexOfSecond = partialOrder.indexOf(pairsOfVertices[i+1]);
            assertTrue(indexOfFirst < indexOfSecond);
        }
    }
}

// End TopologicalOrderIteratorTest.java
