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
/* --------------------------
 * AsWeightedGraphTest.java
 * --------------------------
 * (C) Copyright 2007, by Lucas J. Scharenbroich and Contributors.
 *
 * Original Author:  Lucas J. Scharenbroich
 * Contributor(s):   John V. Sichi
 *
 * $Id$
 *
 * Changes
 * -------
 * 22-Sep-2007 : Initial revision (JVS);
 *
 */
package org.jgrapht.graph;

import com.google.common.collect.Maps;
import org.jgrapht.EnhancedTestCase;
import org.jgrapht.WeightedGraph;

import java.util.Map;


/**
 * A unit test for the AsWeightedGraph view.
 *
 * @author Lucas J. Scharenbroich
 */
public class AsWeightedGraphTest
    extends EnhancedTestCase
{
    //~ Instance fields --------------------------------------------------------

    public SimpleWeightedGraph<String, DefaultWeightedEdge> weightedGraph;
    public SimpleGraph<String, DefaultEdge> unweightedGraph;

    //~ Methods ----------------------------------------------------------------

    @Override
    public void setUp()
    {
        weightedGraph =
            new SimpleWeightedGraph<String, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);
        unweightedGraph =
            new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

        // Create a very simple graph
        weightedGraph.addVertex("v1");
        weightedGraph.addVertex("v2");
        weightedGraph.addVertex("v3");

        unweightedGraph.addVertex("v1");
        unweightedGraph.addVertex("v2");
        unweightedGraph.addVertex("v3");

        weightedGraph.setEdgeWeight(weightedGraph.addEdge("v1", "v2"), 1.);
        weightedGraph.setEdgeWeight(weightedGraph.addEdge("v2", "v3"), 2.);
        weightedGraph.setEdgeWeight(weightedGraph.addEdge("v3", "v1"), 3.);

        unweightedGraph.addEdge("v1", "v2");
        unweightedGraph.addEdge("v2", "v3");
        unweightedGraph.addEdge("v3", "v1");
    }

    @Override
    public void tearDown()
    {
    }

    public void test1()
    {
        Map<DefaultEdge, Double> weightMap1 = Maps.newHashMap();
        Map<DefaultWeightedEdge, Double> weightMap2 = Maps.newHashMap();

        DefaultEdge e1 = unweightedGraph.getEdge("v1", "v2");
        DefaultEdge e2 = unweightedGraph.getEdge("v2", "v3");
        DefaultEdge e3 = unweightedGraph.getEdge("v3", "v1");

        DefaultWeightedEdge e4 = weightedGraph.getEdge("v1", "v2");
        DefaultWeightedEdge e5 = weightedGraph.getEdge("v2", "v3");
        DefaultWeightedEdge e6 = weightedGraph.getEdge("v3", "v1");

        weightMap1.put(e1, 9.0);

        weightMap2.put(e4, 9.0);
        weightMap2.put(e6, 8.0);

        assertEquals(
            unweightedGraph.getEdgeWeight(e1),
            WeightedGraph.DEFAULT_EDGE_WEIGHT);

        WeightedGraph<String, DefaultEdge> g1 =
            new AsWeightedGraph<String, DefaultEdge>(
                unweightedGraph,
                weightMap1);
        WeightedGraph<String, DefaultWeightedEdge> g2 =
            new AsWeightedGraph<String, DefaultWeightedEdge>(
                weightedGraph,
                weightMap2);

        assertEquals(g1.getEdgeWeight(e1), 9.0);
        assertEquals(g1.getEdgeWeight(e2), WeightedGraph.DEFAULT_EDGE_WEIGHT);
        assertEquals(g1.getEdgeWeight(e3), WeightedGraph.DEFAULT_EDGE_WEIGHT);

        assertEquals(g2.getEdgeWeight(e4), 9.0);
        assertEquals(g2.getEdgeWeight(e5), 2.0);
        assertEquals(g2.getEdgeWeight(e6), 8.0);

        g1.setEdgeWeight(e2, 5.0);
        g2.setEdgeWeight(e5, 5.0);

        assertEquals(g1.getEdgeWeight(e2), 5.0);
        assertEquals(
            unweightedGraph.getEdgeWeight(e2),
            WeightedGraph.DEFAULT_EDGE_WEIGHT);

        assertEquals(g2.getEdgeWeight(e5), 5.0);
        assertEquals(weightedGraph.getEdgeWeight(e5), 5.0);
    }
}

// End AsWeightedGraphTest.java
