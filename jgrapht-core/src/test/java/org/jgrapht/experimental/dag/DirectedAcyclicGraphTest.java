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
 * DirectedAcyclicGraphTest.java
 * -------------------
 * (C) Copyright 2008-2008, by Peter Giles and Contributors.
 *
 * Original Author:  Peter Giles
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 17-Mar-2008 : Initial revision (PG);
 *
 */
package org.jgrapht.experimental.dag;

import java.util.*;

import junit.framework.*;

import org.jgrapht.*;
import org.jgrapht.alg.*;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;


/**
 * Unit tests for the DirectedAcyclicGraph, a dynamic DAG implementation.
 *
 * @author gilesp@u.washington.edu
 */
public class DirectedAcyclicGraphTest
    extends TestCase
{
    //~ Instance fields --------------------------------------------------------

    private RandomGraphGenerator<Long, DefaultEdge> randomGraphGenerator = null;
    private Graph<Long, DefaultEdge> sourceGraph = null;

    //~ Methods ----------------------------------------------------------------

    @Override protected void setUp()
        throws Exception
    {
        super.setUp();

        setUpWithSeed(100, 5000, 2);
    }

    private void setUpWithSeed(int vertices, int edges, long seed)
    {
        randomGraphGenerator =
            new RepeatableRandomGraphGenerator<Long, DefaultEdge>(
                vertices,
                edges,
                seed);
        sourceGraph =
            new SimpleDirectedGraph<Long, DefaultEdge>(DefaultEdge.class);
        randomGraphGenerator.generateGraph(
            sourceGraph,
            new LongVertexFactory(),
            null);
    }

    /**
     * Tests the cycle detection capabilities of DirectedAcyclicGraph by
     * building a parallel SimpleDirectedGraph and using a CycleDetector to
     * check for cycles, and comparing the results.
     */
    public void testCycleDetectionInRandomGraphBuild()
    {
        for (int i = 0; i < 50; i++) { // test with 50 random graph
                                       // configurations
            setUpWithSeed(20, 200, i);

            DirectedAcyclicGraph<Long, DefaultEdge> dag =
                new DirectedAcyclicGraph<Long, DefaultEdge>(DefaultEdge.class);
            SimpleDirectedGraph<Long, DefaultEdge> compareGraph =
                new SimpleDirectedGraph<Long, DefaultEdge>(DefaultEdge.class);

            for (Long vertex : sourceGraph.vertexSet()) {
                dag.addVertex(vertex);
                compareGraph.addVertex(vertex);
            }

            for (DefaultEdge edge : sourceGraph.edgeSet()) {
                Long edgeSource = sourceGraph.getEdgeSource(edge);
                Long edgeTarget = sourceGraph.getEdgeTarget(edge);

                boolean dagRejectedEdge = false;
                try {
                    dag.addDagEdge(edgeSource, edgeTarget);
                } catch (DirectedAcyclicGraph.CycleFoundException e) {
                    // okay, it did't add that edge
                    dagRejectedEdge = true;
                }

                DefaultEdge compareEdge =
                    compareGraph.addEdge(edgeSource, edgeTarget);
                CycleDetector<Long, DefaultEdge> cycleDetector =
                    new CycleDetector<Long, DefaultEdge>(compareGraph);

                boolean cycleDetected = cycleDetector.detectCycles();

                assertTrue(dagRejectedEdge == cycleDetected);

                if (cycleDetected) {
                    // remove the edge from the compareGraph so the graphs
                    // remain in sync
                    compareGraph.removeEdge(compareEdge);
                }
            }

            // after all this, our graphs must be equal
            assertEquals(compareGraph.vertexSet(), dag.vertexSet());

            // for some reason comparing vertex sets doesn't work, so doing it
            // the hard way:
            for (Long sourceVertex : compareGraph.vertexSet()) {
                for (
                    DefaultEdge outgoingEdge
                    : compareGraph.outgoingEdgesOf(sourceVertex))
                {
                    Long targetVertex =
                        compareGraph.getEdgeTarget(outgoingEdge);
                    assertTrue(dag.containsEdge(sourceVertex, targetVertex));
                }
            }
        }
    }

    /**
     * trivial test of topological order using a linear graph
     */
    public void testTopoIterationOrderLinearGraph()
    {
        DirectedAcyclicGraph<Long, DefaultEdge> dag =
            new DirectedAcyclicGraph<Long, DefaultEdge>(DefaultEdge.class);
        LinearGraphGenerator<Long, DefaultEdge> graphGen =
            new LinearGraphGenerator<Long, DefaultEdge>(100);
        graphGen.generateGraph(dag, new LongVertexFactory(), null);

        Iterator<Long> internalTopoIter = dag.iterator();
        TopologicalOrderIterator<Long, DefaultEdge> comparTopoIter =
            new TopologicalOrderIterator<Long, DefaultEdge>(dag);

        while (comparTopoIter.hasNext()) {
            Long compareNext = comparTopoIter.next();
            Long myNext = null;

            if (internalTopoIter.hasNext()) {
                myNext = internalTopoIter.next();
            }

            assertSame(compareNext, myNext);
            assertEquals(comparTopoIter.hasNext(), internalTopoIter.hasNext());
        }
    }

    /**
     * more rigorous test of topological iteration order, by assuring that each
     * visited vertex adheres to the definition of topological order, that is
     * that it doesn't have a path leading to any of its predecessors.
     */
    public void testTopoIterationOrderComplexGraph()
    {
        for (int seed = 0; seed < 20; seed++) {
            DirectedAcyclicGraph<Long, DefaultEdge> dag =
                new DirectedAcyclicGraph<Long, DefaultEdge>(DefaultEdge.class);
            RepeatableRandomGraphGenerator<Long, DefaultEdge> graphGen =
                new RepeatableRandomGraphGenerator<Long, DefaultEdge>(
                    100,
                    500,
                    seed);
            graphGen.generateGraph(dag, new LongVertexFactory(), null);

            ConnectivityInspector<Long, DefaultEdge> connectivityInspector =
                new ConnectivityInspector<Long, DefaultEdge>(dag);

            Iterator<Long> internalTopoIter = dag.iterator();

            List<Long> previousVertices = new ArrayList<Long>();

            while (internalTopoIter.hasNext()) {
                Long vertex = internalTopoIter.next();

                for (Long previousVertex : previousVertices) {
                    connectivityInspector.pathExists(vertex, previousVertex);
                }

                previousVertices.add(vertex);
            }
        }
    }

    public void testIterationBehaviors()
    {
        int vertexCount = 100;

        DirectedAcyclicGraph<Long, DefaultEdge> dag =
            new DirectedAcyclicGraph<Long, DefaultEdge>(DefaultEdge.class);
        RepeatableRandomGraphGenerator<Long, DefaultEdge> graphGen =
            new RepeatableRandomGraphGenerator<Long, DefaultEdge>(
                vertexCount,
                500,
                2);
        graphGen.generateGraph(dag, new LongVertexFactory(), null);

        Iterator<Long> dagIter = dag.iterator();

        // Scroll through all the elements, then make sure things happen as
        // should when an iterator is all used up

        for (int i = 0; i < vertexCount; i++) {
            assertTrue(dagIter.hasNext());
            dagIter.next();
        }
        assertFalse(dagIter.hasNext());

        try {
            dagIter.next();
            fail();
        } catch (NoSuchElementException e) {
            // good, we already looked at all of the elements
        }

        assertFalse(dagIter.hasNext());

        dagIter = dag.iterator(); // replace dagIter;

        assertNotNull(dagIter.next()); // make sure it works on first element
                                       // even if hasNext() wasn't called

        // Test that ConcurrentModificationExceptionS happen as they should when
        // the topology is modified during iteration

        // remove a random vertex
        dag.removeVertex(dag.vertexSet().iterator().next());

        // now we expect exceptions since the topological order has been
        // modified (albeit trivially)
        try {
            dagIter.next();
            fail(); // fail, no exception was thrown
        } catch (ConcurrentModificationException e) {
            // good, this is expected
        }

        try {
            dagIter.hasNext();
            fail(); // fail, no exception was thrown
        } catch (ConcurrentModificationException e) {
            // good, this is expected
        }

        try {
            dagIter.remove();
            fail(); // fail, no exception was thrown
        } catch (ConcurrentModificationException e) {
            // good, this is expected
        }

        // TODO: further iterator tests
    }

    // Performance tests have underscores in the names so that they
    // they are only run explicitly (not automatically as part of
    // default JUnit runs).

    /**
     * A somewhat frivolous test of the performance difference between doing a
     * full cycle detection (non-dynamic algorithm) for each edge added versus
     * the dynamic algorithm used by DirectedAcyclicGraph.
     */
    public void _testPerformanceVersusStaticChecking()
    {
        int trialsPerConfiguration = 10;
        int maxVertices = 1024;
        int maxConnectednessFactor = 4;

        for (
            int numVertices = 1024;
            numVertices <= maxVertices;
            numVertices *= 2)
        {
            for (
                int connectednessFactor = 1;
                (connectednessFactor <= maxConnectednessFactor)
                && (connectednessFactor < (numVertices - 1));
                connectednessFactor *= 2)
            {
                long dynamicDagTime = 0;
                long staticDagTime = 0;

                for (int seed = 0; seed < trialsPerConfiguration; seed++) { // test with random graph configurations
                    setUpWithSeed(
                        numVertices,
                        numVertices * connectednessFactor,
                        seed);

                    DirectedAcyclicGraph<Long, DefaultEdge> dag =
                        new DirectedAcyclicGraph<Long, DefaultEdge>(
                            DefaultEdge.class);

                    long dynamicOpStart = System.nanoTime();

                    for (Long vertex : sourceGraph.vertexSet()) {
                        dag.addVertex(vertex);
                    }

                    for (DefaultEdge edge : sourceGraph.edgeSet()) {
                        Long edgeSource = sourceGraph.getEdgeSource(edge);
                        Long edgeTarget = sourceGraph.getEdgeTarget(edge);

                        dag.addEdge(edgeSource, edgeTarget);
                    }

                    dynamicDagTime += System.nanoTime() - dynamicOpStart;

                    SimpleDirectedGraph<Long, DefaultEdge> compareGraph =
                        new SimpleDirectedGraph<Long, DefaultEdge>(
                            DefaultEdge.class);

                    long staticOpStart = System.nanoTime();

                    for (Long vertex : sourceGraph.vertexSet()) {
                        compareGraph.addVertex(vertex);
                    }

                    for (DefaultEdge edge : sourceGraph.edgeSet()) {
                        Long edgeSource = sourceGraph.getEdgeSource(edge);
                        Long edgeTarget = sourceGraph.getEdgeTarget(edge);

                        DefaultEdge compareEdge =
                            compareGraph.addEdge(edgeSource, edgeTarget);
                        CycleDetector<Long, DefaultEdge> cycleDetector =
                            new CycleDetector<Long, DefaultEdge>(compareGraph);

                        boolean cycleDetected = cycleDetector.detectCycles();

                        if (cycleDetected) {
                            // remove the edge from the compareGraph
                            compareGraph.removeEdge(compareEdge);
                        }
                    }

                    staticDagTime += System.nanoTime() - staticOpStart;
                }

                System.out.println(
                    "vertices = " + numVertices + "  connectednessFactor = "
                    + connectednessFactor + "  trialsPerConfiguration = "
                    + trialsPerConfiguration);
                System.out.println(
                    "total static DAG time   =  " + staticDagTime + " ns");
                System.out.println(
                    "total dynamic DAG time  =  " + dynamicDagTime + " ns");
                System.out.println();
            }
        }
    }

    /**
     * A somewhat frivolous test of the performance difference between doing a
     * full cycle detection (non-dynamic algorithm) for each edge added versus
     * the dynamic algorithm used by DirectedAcyclicGraph.
     */
    public void _testVisitedImplementationPerformance()
    {
        int trialsPerConfiguration = 10;
        int maxVertices = 1024;
        int maxConnectednessFactor = 4;

        for (
            int numVertices = 64;
            numVertices <= maxVertices;
            numVertices *= 2)
        {
            for (
                int connectednessFactor = 1;
                (connectednessFactor <= maxConnectednessFactor)
                && (connectednessFactor < (numVertices - 1));
                connectednessFactor *= 2)
            {
                long arrayDagTime = 0;
                long arrayListDagTime = 0;
                long hashSetDagTime = 0;
                long bitSetDagTime = 0;

                for (int seed = 0; seed < trialsPerConfiguration; seed++) { // test with random graph configurations
                    setUpWithSeed(
                        numVertices,
                        numVertices * connectednessFactor,
                        seed);

                    DirectedAcyclicGraph<Long, DefaultEdge> arrayDag =
                        new DirectedAcyclicGraph<Long, DefaultEdge>(
                            DefaultEdge.class,
                            new DirectedAcyclicGraph.VisitedArrayImpl(),
                            null);
                    DirectedAcyclicGraph<Long, DefaultEdge> arrayListDag =
                        new DirectedAcyclicGraph<Long, DefaultEdge>(
                            DefaultEdge.class,
                            new DirectedAcyclicGraph.VisitedArrayListImpl(),
                            null);
                    DirectedAcyclicGraph<Long, DefaultEdge> hashSetDag =
                        new DirectedAcyclicGraph<Long, DefaultEdge>(
                            DefaultEdge.class,
                            new DirectedAcyclicGraph.VisitedHashSetImpl(),
                            null);
                    DirectedAcyclicGraph<Long, DefaultEdge> bitSetDag =
                        new DirectedAcyclicGraph<Long, DefaultEdge>(
                            DefaultEdge.class,
                            new DirectedAcyclicGraph.VisitedBitSetImpl(),
                            null);

                    long arrayStart = System.nanoTime();

                    for (Long vertex : sourceGraph.vertexSet()) {
                        arrayDag.addVertex(vertex);
                    }

                    for (DefaultEdge edge : sourceGraph.edgeSet()) {
                        Long edgeSource = sourceGraph.getEdgeSource(edge);
                        Long edgeTarget = sourceGraph.getEdgeTarget(edge);

                        try {
                            arrayDag.addDagEdge(edgeSource, edgeTarget);
                        } catch (DirectedAcyclicGraph.CycleFoundException e) {
                            // okay
                        }
                    }

                    arrayDagTime += System.nanoTime() - arrayStart;

                    long arrayListStart = System.nanoTime();

                    for (Long vertex : sourceGraph.vertexSet()) {
                        arrayListDag.addVertex(vertex);
                    }

                    for (DefaultEdge edge : sourceGraph.edgeSet()) {
                        Long edgeSource = sourceGraph.getEdgeSource(edge);
                        Long edgeTarget = sourceGraph.getEdgeTarget(edge);

                        try {
                            arrayListDag.addDagEdge(edgeSource, edgeTarget);
                        } catch (DirectedAcyclicGraph.CycleFoundException e) {
                            // okay
                        }
                    }

                    arrayListDagTime += System.nanoTime() - arrayListStart;

                    long hashSetStart = System.nanoTime();

                    for (Long vertex : sourceGraph.vertexSet()) {
                        hashSetDag.addVertex(vertex);
                    }

                    for (DefaultEdge edge : sourceGraph.edgeSet()) {
                        Long edgeSource = sourceGraph.getEdgeSource(edge);
                        Long edgeTarget = sourceGraph.getEdgeTarget(edge);

                        try {
                            hashSetDag.addDagEdge(edgeSource, edgeTarget);
                        } catch (DirectedAcyclicGraph.CycleFoundException e) {
                            // okay
                        }
                    }

                    hashSetDagTime += System.nanoTime() - hashSetStart;

                    long bitSetStart = System.nanoTime();

                    for (Long vertex : sourceGraph.vertexSet()) {
                        bitSetDag.addVertex(vertex);
                    }

                    for (DefaultEdge edge : sourceGraph.edgeSet()) {
                        Long edgeSource = sourceGraph.getEdgeSource(edge);
                        Long edgeTarget = sourceGraph.getEdgeTarget(edge);

                        try {
                            bitSetDag.addDagEdge(edgeSource, edgeTarget);
                        } catch (DirectedAcyclicGraph.CycleFoundException e) {
                            // okay
                        }
                    }

                    bitSetDagTime += System.nanoTime() - bitSetStart;
                }

                System.out.println(
                    "vertices = " + numVertices + "  connectednessFactor = "
                    + connectednessFactor + "  trialsPerConfiguration = "
                    + trialsPerConfiguration);
                System.out.println(
                    "total array time       =  " + arrayDagTime + " ns");
                System.out.println(
                    "total ArrayList time   =  " + arrayListDagTime + " ns");
                System.out.println(
                    "total HashSet time     =  " + hashSetDagTime + " ns");
                System.out.println(
                    "total BitSet time     =  " + bitSetDagTime + " ns");
                System.out.println();
            }
        }
    }

    
    public void testWhenVertexIsNotInGraph_Then_ThowException() {
        DirectedAcyclicGraph<Long, DefaultEdge> dag =
                new DirectedAcyclicGraph<Long, DefaultEdge>(DefaultEdge.class);
        try {
        	dag.addDagEdge(1l, 2l);
        } catch(IllegalArgumentException e) {
        	return;
        } catch (CycleFoundException e) {
			e.printStackTrace();
			fail("Unexpected 'CycleFoundException' catched");
		}
        fail("No exception 'IllegalArgumentException' catched");
    }
    
    //~ Inner Classes ----------------------------------------------------------

    private static class LongVertexFactory
        implements VertexFactory<Long>
    {
        private long nextVertex = 0;

        @Override
        public Long createVertex()
        {
            return nextVertex++;
        }
    }

    // it is nice for tests to be easily repeatable, so we use a graph generator
    // that we can seed for specific configurations
    private static class RepeatableRandomGraphGenerator<V, E>
        extends RandomGraphGenerator<V, E>
    {
        public RepeatableRandomGraphGenerator(
            int vertices,
            int edges,
            long seed)
        {
            super(vertices, edges);
            randomizer = new Random(seed);
        }

        @Override public void generateGraph(
            Graph<V, E> graph,
            VertexFactory<V> vertexFactory,
            Map<String, V> namedVerticesMap)
        {
            List<V> vertices = new ArrayList<V>(numOfVertexes);
            Set<Integer> edgeGeneratorIds = new HashSet<Integer>();

            for (int i = 0; i < numOfVertexes; i++) {
                V vertex = vertexFactory.createVertex();
                vertices.add(vertex);
                graph.addVertex(vertex);
            }

            for (int i = 0; i < numOfEdges; i++) {
                Integer edgeGeneratorId;
                do {
                    edgeGeneratorId =
                        randomizer.nextInt(numOfVertexes * (numOfVertexes - 1));
                } while (edgeGeneratorIds.contains(edgeGeneratorId));

                int fromVertexId = edgeGeneratorId / numOfVertexes;
                int toVertexId = edgeGeneratorId % (numOfVertexes - 1);
                if (toVertexId >= fromVertexId) {
                    ++toVertexId;
                }

                try {
                    graph.addEdge(
                        vertices.get(fromVertexId),
                        vertices.get(toVertexId));
                } catch (IllegalArgumentException e) {
                    // okay, that's fine; omit cycle
                }
            }
        }
    }
}

// End DirectedAcyclicGraphTest.java
