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
 * AsUnweightedGraphTest.java
 * --------------------------
 * (C) Copyright 2007-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 22-Sep-2007 : Initial revision (JVS);
 *
 */
package org.jgrapht.graph;

import junit.framework.TestCase;
import org.jgrapht.EnhancedTestCase;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.WeightedGraph;


/**
 * A unit test for the AsUnweighted[Directed]Graph views.
 *
 * @author John V. Sichi
 */
public class AsUnweightedGraphTest
    extends EnhancedTestCase
{
    //~ Static fields/initializers ---------------------------------------------

    private static final String v1 = "v1";
    private static final String v2 = "v2";
    private static final String v3 = "v3";

    //~ Constructors -----------------------------------------------------------

    /**
     * @see TestCase#TestCase(String)
     */
    public AsUnweightedGraphTest(final String name)
    {
        super(name);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    public void testDirected()
    {
        final DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> directed =
            new DefaultDirectedWeightedGraph<String, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);
        constructWeighted(directed);

        final AsUnweightedDirectedGraph<String, DefaultWeightedEdge> unweighted =
            new AsUnweightedDirectedGraph<String, DefaultWeightedEdge>(
                directed);
        checkView(directed, unweighted);
    }

    /**
     * .
     */
    public void testUndirected()
    {
        final WeightedGraph<String, DefaultWeightedEdge> undirected =
            new SimpleWeightedGraph<String, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);
        constructWeighted(undirected);

        final AsUnweightedGraph<String, DefaultWeightedEdge> unweighted =
            new AsUnweightedGraph<String, DefaultWeightedEdge>(
                undirected);
        checkView(undirected, unweighted);
    }

    private static void constructWeighted(
        final WeightedGraph<String, DefaultWeightedEdge> weighted)
    {
        weighted.addVertex(v1);
        weighted.addVertex(v2);
        weighted.addVertex(v3);
        Graphs.addEdge(weighted, v1, v2, 3.0);
        assertEquals(
            3.0,
            weighted.getEdgeWeight(
                weighted.getEdge(v1, v2)));
    }

    private static void checkView(
        final WeightedGraph<String, DefaultWeightedEdge> weighted,
        final Graph<String, DefaultWeightedEdge> unweighted)
    {
        assertEquals(
            WeightedGraph.DEFAULT_EDGE_WEIGHT,
            unweighted.getEdgeWeight(
                unweighted.getEdge(v1, v2)));

        Graphs.addEdge(weighted, v2, v3, 5.0);
        assertEquals(
            WeightedGraph.DEFAULT_EDGE_WEIGHT,
            unweighted.getEdgeWeight(
                unweighted.getEdge(v2, v3)));

        unweighted.addEdge(v3, v1);
        assertEquals(
            WeightedGraph.DEFAULT_EDGE_WEIGHT,
            unweighted.getEdgeWeight(
                unweighted.getEdge(v3, v1)));
        assertEquals(
            WeightedGraph.DEFAULT_EDGE_WEIGHT,
            weighted.getEdgeWeight(
                weighted.getEdge(v3, v1)));
    }
}

// End AsUnweightedGraphTest.java
