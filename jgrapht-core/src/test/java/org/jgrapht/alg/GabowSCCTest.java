package org.jgrapht.alg;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.RingGraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedSubgraph;

public class GabowSCCTest     extends TestCase
{
    //~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";
    private static final String V4 = "v4";
   
    
    /* Create a graph with two vertexes strongly connected and two only weakly connected  */
    
	 public void testStronglyConnectedG1()
	    {
	        DirectedGraph<String, DefaultEdge> g = new DefaultDirectedGraph<String, DefaultEdge> (DefaultEdge.class);
	        g.addVertex(V1);
	        g.addVertex(V2);
	        g.addVertex(V3);
	        g.addVertex(V4);

	        g.addEdge(V1, V2);
	        g.addEdge(V2, V1); // strongly connected

	        g.addEdge(V3, V4); // only weakly connected

	        GabowSCC<String, DefaultEdge> inspector = new GabowSCC<String, DefaultEdge>(g);

	        // convert from List to Set because we need to ignore order
	        // during comparison
	        Set<Set<String>> actualSets =
	            new HashSet<Set<String>>(inspector.stronglyConnectedSets());

	        // construct the expected answer
	        Set<Set<String>> expectedSets = new HashSet<Set<String>>();
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

	        List<DirectedSubgraph<String, DefaultEdge>> subgraphs = inspector.stronglyConnectedSubgraphs();
	        for (DirectedSubgraph<String, DefaultEdge> sg : subgraphs) {
	            actualSets.add(sg.vertexSet());

	            GabowSCC<String, DefaultEdge> ci = new GabowSCC<String, DefaultEdge>(sg);
	            assertTrue(ci.isStronglyConnected());
	        }

	        assertEquals(expectedSets, actualSets);
	    }

	 	/* Create a graph with two vertexes strongly connected (v1,v2) and two only weakly connected (v3,v4). 
	 	 * Verify that v3 is not added between the strongly connected element  */

	    public void testStronglyConnectedG2()
	    {
	        DirectedGraph<String, DefaultEdge> g = new DefaultDirectedGraph<String, DefaultEdge>( DefaultEdge.class);
	        g.addVertex(V1);
	        g.addVertex(V2);
	        g.addVertex(V3);
	        g.addVertex(V4);

	        g.addEdge(V1, V2);
	        g.addEdge(V2, V1); // strongly connected

	        g.addEdge(V4, V3); // only weakly connected
	        g.addEdge(V3, V2); // only weakly connected

	        GabowSCC<String, DefaultEdge> inspector =
	            new GabowSCC<String, DefaultEdge>(g);

	        // convert from List to Set because we need to ignore order
	        // during comparison
	        Set<Set<String>> actualSets =
	            new HashSet<Set<String>>(inspector.stronglyConnectedSets());

	        // construct the expected answer
	        Set<Set<String>> expectedSets = new HashSet<Set<String>>();
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

	        List<DirectedSubgraph<String, DefaultEdge>> subgraphs = inspector.stronglyConnectedSubgraphs();
	        for (DirectedSubgraph<String, DefaultEdge> sg : subgraphs) {
	            actualSets.add(sg.vertexSet());

	            GabowSCC<String, DefaultEdge> ci = new GabowSCC<String, DefaultEdge>(sg);
	            assertTrue(ci.isStronglyConnected());
	        }

	        assertEquals(expectedSets, actualSets);
	    }


	    /* Create a graph with three vertexes strongly connected (v1,v2,v3) and one only weakly connected v4.
	     * Verify that v4 is not added between the strongly connected elements  */
	    
	    public void testStronglyConnectedG3()
	    {
	        DirectedGraph<String, DefaultEdge> g = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
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

	        GabowSCC<String, DefaultEdge> inspector = new GabowSCC<String, DefaultEdge>(g);

	        // convert from List to Set because we need to ignore order
	        // during comparison
	        Set<Set<String>> actualSets =
	            new HashSet<Set<String>>(inspector.stronglyConnectedSets());

	        // construct the expected answer
	        Set<Set<String>> expectedSets = new HashSet<Set<String>>();
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

	        List<DirectedSubgraph<String, DefaultEdge>> subgraphs =   inspector.stronglyConnectedSubgraphs();

	        for (DirectedSubgraph<String, DefaultEdge> sg : subgraphs) {
	            actualSets.add(sg.vertexSet());

	            GabowSCC<String, DefaultEdge> ci =
	                new GabowSCC<String, DefaultEdge>(sg);
	            assertTrue(ci.isStronglyConnected());
	        }

	        assertEquals(expectedSets, actualSets);
	    }

	    /* Create a RingGraph, verify that all the vertexes are strongly connected  */
	    public void testStronglyConnectedG4()
	    {
	        DefaultDirectedGraph<Integer, String> graph =
	            new DefaultDirectedGraph<Integer, String>(
	                new EdgeFactory<Integer, String>() {
	                    public String createEdge(Integer from, Integer to)
	                    {
	                        return (from + "->" + to).intern();
	                    }
	                });

	        new RingGraphGenerator<Integer, String>(3).generateGraph(
	            graph,
	            new VertexFactory<Integer>() {
	                private int i = 0;

	                public Integer createVertex()
	                {
	                    return i++;
	                }
	            },
	            null);

	        GabowSCC<Integer, String> sc = new GabowSCC<Integer, String>(graph);
	        Set<Set<Integer>> expected = new HashSet<Set<Integer>>();
	        expected.add(graph.vertexSet());
	        assertEquals(
	            expected,
	            new HashSet<Set<Integer>>(sc.stronglyConnectedSets()));
	    }
}
