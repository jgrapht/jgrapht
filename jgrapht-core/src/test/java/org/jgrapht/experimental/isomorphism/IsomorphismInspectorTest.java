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
/* -----------------
 * IsomorphismInspectorTest.java
 * -----------------
 * (C) Copyright 2005-2008, by Assaf Lehr and Contributors.
 *
 * Original Author:  Assaf Lehr
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 */
package org.jgrapht.experimental.isomorphism;

import junit.framework.TestCase;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.experimental.equivalence.EquivalenceComparator;
import org.jgrapht.experimental.isomorphism.comparators.DirectedEdgeWeightOddEvenComparator;
import org.jgrapht.experimental.isomorphism.comparators.OddEvenGroupComparator;
import org.jgrapht.generate.LinearGraphGenerator;
import org.jgrapht.generate.RandomGraphGenerator;
import org.jgrapht.generate.WheelGraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Iterator;
import java.util.Set;


/**
 * @author Assaf
 * @since May 27, 2005
 */
public class IsomorphismInspectorTest
    extends TestCase
{
    //~ Constructors -----------------------------------------------------------

    /**
     * Constructor for IsomorphismInspectorTest.
     *
     * @param arg0
     */
    public IsomorphismInspectorTest(final String arg0)
    {
        super(arg0);
    }

    //~ Methods ----------------------------------------------------------------

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
    }

    /**
     * Calls the same method with different (default) parameters
     * EqualityGroupChecker vertexChecker = null EqualityGroupChecker
     * edgeChecker = null
     */
    private void assertIsomorphic(
        final Graph<Integer, DefaultEdge> [] graphs,
        final boolean shouldTheyBeIsomorphic)
    {
        assertIsomorphic(graphs, shouldTheyBeIsomorphic, null, null);
    }

    @SuppressWarnings("unchecked")
    private void assertIsomorphic(
        final Graph<Integer, DefaultEdge> [] graphs,
        final boolean shouldTheyBeIsomorphic,
        final EquivalenceComparator vertexChecker,
        final EquivalenceComparator edgeChecker)
    {
        // System.out.println("\nassertIsomorphic:"+shouldTheyBeIsomorphic);
        final Graph<Integer, DefaultEdge> g1 = graphs[0];
        final Graph<Integer, DefaultEdge> g2 = graphs[1];

        // System.out.println("g1:"+g1);
        // System.out.println("g2:"+g2);
        // long beforeTime=System.currentTimeMillis();
        final GraphIsomorphismInspector iso =
            AdaptiveIsomorphismInspectorFactory.createIsomorphismInspector(
                g1,
                g2,
                vertexChecker,
                edgeChecker);
        int counter = 0;
        while (iso.hasNext()) {
            final IsomorphismRelation isioResult = (IsomorphismRelation) iso.next();

            if (false) {
                if (counter == 0) {
                    System.out.println(
                        "Graphs are isomorphic. Iterating over all options:");
                }
                System.out.println(counter + " : " + isioResult);
            }
            counter++;
        }

        // if (counter==0)
        // {
        // System.out.println("Graphs are NOT isomorphic.");
        // }
        // long deltaTime=System.currentTimeMillis()-beforeTime;
        // String timeDesc;
        // timeDesc= deltaTime<=10 ?  "<10ms [less than minimum measurement
        // time]": String.valueOf(deltaTime);
        // System.out.println("# Performence: Isomorphism check in
        // MiliSeconds:"+timeDesc);
        if (shouldTheyBeIsomorphic) {
            assertTrue(counter != 0);
        } else {
            assertTrue(counter == 0);
        }
    }

    @SuppressWarnings("unchecked")
    private void checkRelation(
        final Graph<Integer, DefaultEdge> [] graphs,
        final EquivalenceComparator vertexChecker,
        final EquivalenceComparator edgeChecker)
    {
        final Graph<Integer, DefaultEdge> g1 = graphs[0];
        final Graph<Integer, DefaultEdge> g2 = graphs[1];

        final GraphIsomorphismInspector iso =
            AdaptiveIsomorphismInspectorFactory.createIsomorphismInspector(
                g1,
                g2,
                vertexChecker,
                edgeChecker);
        final IsomorphismRelation<Integer, DefaultEdge> isoResult;
        if (iso.hasNext()) {
            isoResult = (IsomorphismRelation) iso.next();

            final Set<Integer> vertexSet = g1.vertexSet();
            for (
                Iterator<Integer> iter = vertexSet.iterator();
                iter.hasNext();)
            {
                final Integer v1 = iter.next();
                final Integer v2 = isoResult.getVertexCorrespondence(v1, true);
                if (false) {
                    System.out.println("Vertex relation " + v1 + " to " + v2);
                }
            }
            final Set<DefaultEdge> edgeSet = g1.edgeSet();
            for (
                Iterator<DefaultEdge> iter = edgeSet.iterator();
                iter.hasNext();)
            {
                final DefaultEdge e1 = iter.next();
                final DefaultEdge e2 = isoResult.getEdgeCorrespondence(e1, true);
                if (false) {
                    System.out.println("Vertex relation " + e1 + " to " + e2);
                }
            }

            // if (counter==0)
            // {
            // System.out.println("Graphs are isomorphic. Iterating over all
            // options:");
            // }
            // System.out.println(counter+" : "+isioResult);

        }
    }

    @SuppressWarnings("unchecked")
    public void testWheelGraphAddRemoveParts()
    {
        final int NUM_OF_VERTEXES_IN_WHEEL = 6;
        final int FIRST_INTEGER_FOR_G2 = 13;

        final Graph<Integer, DefaultEdge> g1 =
            new DefaultDirectedGraph<Integer, DefaultEdge>(
                DefaultEdge.class);
        final Graph<Integer, DefaultEdge> g2 =
            new DefaultDirectedGraph<Integer, DefaultEdge>(
                DefaultEdge.class);
        final WheelGraphGenerator<Integer, DefaultEdge> gen1 =
            new WheelGraphGenerator<Integer, DefaultEdge>(
                NUM_OF_VERTEXES_IN_WHEEL);
        gen1.generateGraph(g1, new IntegerVertexFactory(), null);

        // FIRST_INTEGER_FOR_G2-1 , cause first integer is always the next one.
        gen1.generateGraph(
            g2,
            new IntegerVertexFactory(FIRST_INTEGER_FOR_G2),
            null);
        assertIsomorphic(
            new Graph[] {
                g1,
                g2
            },
            true);

        // In a wheel , the last vertex is the wheel center!
        g1.removeVertex(new Integer(NUM_OF_VERTEXES_IN_WHEEL)); // delete one vertex from g1
        assertIsomorphic(
            new Graph[] {
                g1,
                g2
            },
            false);

        // for example: 10+4
        g2.removeVertex(
            new Integer(FIRST_INTEGER_FOR_G2
                + NUM_OF_VERTEXES_IN_WHEEL));
        assertIsomorphic(
            new Graph[] {
                g1,
                g2
            },
            true);

        g1.removeEdge(new Integer(1), new Integer(2));
        assertIsomorphic(
            new Graph[] {
                g1,
                g2
            },
            false);
    }

    @SuppressWarnings("unchecked")
    public void testLinear4vertexIsomorphicGraph()
    {
        final Graph<Integer, DefaultEdge> g1 =
            new DefaultDirectedGraph<Integer, DefaultEdge>(
                DefaultEdge.class);
        final LinearGraphGenerator gen1 = new LinearGraphGenerator(4);
        gen1.generateGraph(g1, new IntegerVertexFactory(), null);

        final Graph<Integer, DefaultEdge> g2 =
            new DefaultDirectedGraph<Integer, DefaultEdge>(
                DefaultEdge.class);
        final LinearGraphGenerator gen2 = new LinearGraphGenerator(4);
        gen2.generateGraph(g2, new IntegerVertexFactory(5), null); // start vertex from number 6
        assertIsomorphic(
            new Graph[] {
                g1,
                g2
            },
            true);

        checkRelation(
            new Graph[] {
                g1,
                g2
            },
            null,
            null);
    }

    /**
     * Create two graphs which are topologically the same (same number of
     * vertexes and same edges connection), but the contents of the vertexes
     * belong to different eq. set. g1: 1-->2-->3-->4 g2: 2-->3-->4-->5 g3:
     * 3-->4-->5-->6 The eq-group-check is if the number is even or odd. So, g1
     * and g3 are isomorphic. g2 is not isomorphic to either of them.
     */
    @SuppressWarnings("unchecked")
    public void testLinear4vertexNonIsomorphicCauseOfVertexEqGroup()
    {
        final LinearGraphGenerator<Integer, DefaultEdge> gen4 =
            new LinearGraphGenerator<Integer, DefaultEdge>(4);

        final Graph<Integer, DefaultEdge> g1 =
            new DefaultDirectedGraph<Integer, DefaultEdge>(
                DefaultEdge.class);
        gen4.generateGraph(g1, new IntegerVertexFactory(), null);

        final Graph<Integer, DefaultEdge> g2 =
            new DefaultDirectedGraph<Integer, DefaultEdge>(
                DefaultEdge.class);
        gen4.generateGraph(g2, new IntegerVertexFactory(1), null); // start vertex from number 2

        final Graph<Integer, DefaultEdge> g3 =
            new DefaultDirectedGraph<Integer, DefaultEdge>(
                DefaultEdge.class);
        gen4.generateGraph(g3, new IntegerVertexFactory(2), null); // start vertex from number 3

        // first assert all are isomorphic (if vertexChecker is not used)
        assertIsomorphic(
            new Graph[] {
                g1,
                g2
            },
            true);
        assertIsomorphic(
            new Graph[] {
                g2,
                g3
            },
            true);
        assertIsomorphic(
            new Graph[] {
                g1,
                g3
            },
            true);

        // create a functor according to odd even
        final EquivalenceComparator vertexEqChecker = new OddEvenGroupComparator();
        assertIsomorphic(
            new Graph[] {
                g1,
                g2
            },
            false,
            vertexEqChecker,
            null);
        assertIsomorphic(
            new Graph[] {
                g2,
                g3
            },
            false,
            vertexEqChecker,
            null);
        assertIsomorphic(
            new Graph[] {
                g1,
                g3
            },
            true,
            vertexEqChecker,
            null);
    }

    /**
     * Passes an EdgeComparator, which compares according to odd-even edge
     * weight. The generated graphs are: A-(12)->B-(10)->C-(5)->D
     * A-(10)->B-(18)->C-(3)->D A-(11)->B-(10)->C-(5)->D (the first here is odd)
     *
     * @author Assaf
     * @since Aug 12, 2005
     */
    @SuppressWarnings("unchecked")
    public void testEdgeComparator()
    {
        final int LINEAR_GRAPH_VERTEX_NUM = 4;
        final Graph [] graphsArray = new DirectedGraph[3];
        final Character [] charArray = {
            new Character('A'),
            new Character('B'),
            new Character('C'),
            new Character('D')
        };
        final int [][] weigthsArray = {
            {
                12,
                10,
                5
            },
            {
                10,
                18,
                3
            },
            {
                11,
                10,
                5
            }
        };

        for (int i = 0; i < graphsArray.length; i++) {
            final Graph<Character, DefaultEdge> currGraph =
                graphsArray[i] =
                    new DefaultDirectedWeightedGraph<Character,
                        DefaultWeightedEdge>(
                        DefaultWeightedEdge.class);
            for (int j = 0; j < LINEAR_GRAPH_VERTEX_NUM; j++) {
                currGraph.addVertex(charArray[j]);
            }

            // create the 3 edges with weights
            for (int j = 0; j < 3; j++) {
                Graphs.addEdge(
                    currGraph,
                    charArray[j],
                    charArray[j + 1],
                    weigthsArray[i][j]);
            }
        }

        // first assert all are isomorphic (if vertexChecker is not used)
        assertIsomorphic(new Graph[] { graphsArray[0], graphsArray[1] },
            true);
        assertIsomorphic(new Graph[] { graphsArray[0], graphsArray[2] },
            true);
        assertIsomorphic(new Graph[] { graphsArray[1], graphsArray[2] },
            true);

        // create a functor according to odd even
        final EquivalenceComparator edgeEqChecker =
            new DirectedEdgeWeightOddEvenComparator(graphsArray[0]);
        assertIsomorphic(
            new Graph[] { graphsArray[0], graphsArray[1] },
            true,
            null,
            edgeEqChecker);
        assertIsomorphic(
            new Graph[] { graphsArray[0], graphsArray[2] },
            false,
            null,
            edgeEqChecker);
        assertIsomorphic(
            new Graph[] { graphsArray[1], graphsArray[2] },
            false,
            null,
            edgeEqChecker);
    }

    @SuppressWarnings("unchecked")
    private void assertIsomorphicStopAfterFirstMatch(
        final Graph [] graphs,
        final boolean assertActive,
        final boolean shouldTheyBeIsomorphic,
        final EquivalenceComparator vertexChecker,
        final EquivalenceComparator edgeChecker)
    {
        if (assertActive) {
            System.out.println("\nassertIsomorphic:"
                + shouldTheyBeIsomorphic);
        }
        final Graph g1 = graphs[0];
        final Graph g2 = graphs[1];
        System.out.println("g1:" + g1);
        System.out.println("g2:" + g2);
        final long beforeTime = System.currentTimeMillis();
        final GraphIsomorphismInspector iso =
            AdaptiveIsomorphismInspectorFactory.createIsomorphismInspector(
                g1,
                g2,
                vertexChecker,
                edgeChecker);
        final boolean isoResult = iso.isIsomorphic();
        if (isoResult) {
            System.out.println("Graphs are isomorphic. ");
        } else {
            System.out.println("Graphs are NOT isomorphic.");
        }

        final long deltaTime = System.currentTimeMillis() - beforeTime;
        final String timeDesc;
        timeDesc =
            deltaTime <= 10 ? "<10ms [less than minumun measurement time]"
            : String.valueOf(deltaTime);
        System.out.println(
            "# Performence: Isomorphism check in MiliSeconds:" + timeDesc);
        if (assertActive) {
            assertEquals(shouldTheyBeIsomorphic, isoResult);
        }
    }

    /**
     * Performance test with different number of vertex, edges. For each number
     * pair, 3 graphs are generated. The first two, using the same generator,
     * the third using a different generator. Note: the 1st and 2nd must be
     * isomorphic. The 3rd will most likely not be isomorphic , but on special
     * occasaions may be, so do not assert it. (example: empty graph, full mesh
     * , rare case that they are not real random). NOTE: RENAME TO testXXX to
     * make it work. It shows output and not assertions, so it cannot be used by
     * automatic tests.
     */
    @SuppressWarnings("unchecked")
    public void performanceTestOnRandomGraphs()
        throws Exception
    {
        final int [] numOfVertexesArray = {
            6,
            6,
            6,
            8,
            8,
            8,
            10,
            10,
            10,
            12,
            14,
            20,
            30,
            99
        };
        final int [] numOfEdgesArray = {
            0,
            4,
            12,
            1,
            15,
            54,
            0,
            40,
            90,
            130,
            50,
            79,
            30,
            234
        };

        // there will be two different generators. The first will be used for
        // 1st,2nd graph
        // the other for the3rd graph
        final int NUM_OF_GENERATORS = 2;
        final RandomGraphGenerator [] genArray =
            new RandomGraphGenerator[NUM_OF_GENERATORS];

        final String [] graphConctereClassesFullName = { // "org.jgrapht.graph.SimpleGraph" ,
            "org.jgrapht.graph.SimpleDirectedGraph",
            "org.jgrapht.graph.DefaultDirectedGraph",
            // "org.jgrapht.graph.Multigraph",
            // "org.jgrapht.graph.Pseudograph"
        };

        // 3 graphs. 1st,2nd must be isomorphic .3rd probably not iso.
        final int SIZE_OF_GENERATED_GRAPH_ARRAY = 3;

        // graphsArray rows are different graph types. Columns are few
        // instances of that type
        final Graph [][] graphsArray =
            new Graph[graphConctereClassesFullName.length][SIZE_OF_GENERATED_GRAPH_ARRAY];

        final Graph [] currIsoTestArray = new Graph[2];
        for (int testNum = 0; testNum < numOfVertexesArray.length; testNum++) {
            // recreate the graphs (empty)
            try {
                for (int i = 0; i < graphConctereClassesFullName.length; i++) {
                    for (int j = 0; j < SIZE_OF_GENERATED_GRAPH_ARRAY; j++) {
                        graphsArray[i][j] =
                            (Graph) Class.forName(
                                graphConctereClassesFullName[i]).newInstance();
                    }
                }
            } catch (Exception e) {
                throw new Exception("failed to initilized the graphs", e);
            }

            // create generators for the new vertex/edges number
            for (int i = 0; i < genArray.length; i++) {
                genArray[i] =
                    new RandomGraphGenerator(
                        numOfVertexesArray[testNum],
                        numOfEdgesArray[testNum]);
            }

            for (
                int graphType = 0;
                graphType < graphConctereClassesFullName.length;
                graphType++)
            {
                System.out.println(
                    "### numOfVertexes= " + numOfVertexesArray[testNum]);
                System.out.println(
                    "### numOfEdges= " + numOfEdgesArray[testNum]);
                System.out.println(
                    "######### Graph Type:"
                    + graphConctereClassesFullName[graphType]);
                System.out.println(
                    "# # # # # # # # # # # # # # # # # # # # # # # # # # # #");

                // 1st and 2nd from genArray[0]
                genArray[0].generateGraph(
                    graphsArray[graphType][0],
                    new IntegerVertexFactory(),
                    null);
                genArray[0].generateGraph(
                    graphsArray[graphType][1],
                    new IntegerVertexFactory(),
                    null);

                // 3rd from genArray[1]
                genArray[1].generateGraph(
                    graphsArray[graphType][2],
                    new IntegerVertexFactory(),
                    null);

                // now start testing
                currIsoTestArray[0] = graphsArray[graphType][0];
                currIsoTestArray[1] = graphsArray[graphType][1];

                assertIsomorphicStopAfterFirstMatch(
                    currIsoTestArray,
                    true,
                    true,
                    null,
                    null);

                // remember it is not a MUST - it can be true . DEGUG REASON
                // ONLY , and with care
                currIsoTestArray[1] = graphsArray[graphType][2];
                assertIsomorphicStopAfterFirstMatch(
                    currIsoTestArray,
                    false,
                    false,
                    null,
                    null);
            }
        }
    }
}

// End IsomorphismInspectorTest.java
