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
/* -----------------------
 * GraphGeneratorTest.java
 * -----------------------
 * (C) Copyright 2003-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 17-Sep-2003 : Initial revision (JVS);
 * 07-May-2006 : Changed from List<Edge> to Set<Edge> (JVS);
 *
 */
package org.jgrapht.generate;

import com.google.common.collect.Maps;
import junit.framework.TestCase;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.ClassBasedVertexFactory;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * .
 *
 * @author John V. Sichi
 * @since Sep 17, 2003
 */
public class GraphGeneratorTest
    extends TestCase
{
    //~ Static fields/initializers ---------------------------------------------

    private static final int SIZE = 10;

    //~ Instance fields --------------------------------------------------------

    private final VertexFactory<Object> vertexFactory =
        new VertexFactory<Object>() {
            private int i;

            @Override
            public Object createVertex()
            {
                return new Integer(++i);
            }
        };

    //~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    public void testEmptyGraphGenerator()
    {
        final GraphGenerator<Object, DefaultEdge, Object> gen =
            new EmptyGraphGenerator<Object, DefaultEdge>(SIZE);
        final DirectedGraph<Object, DefaultEdge> g =
            new DefaultDirectedGraph<Object, DefaultEdge>(DefaultEdge.class);
        final Map<String, Object> resultMap = Maps.newHashMap();
        gen.generateGraph(g, vertexFactory, resultMap);
        assertEquals(SIZE, g.vertexSet().size());
        assertEquals(0, g.edgeSet().size());
        assertTrue(resultMap.isEmpty());
    }

    /**
     * .
     */
    public void testLinearGraphGenerator()
    {
        final GraphGenerator<Object, DefaultEdge, Object> gen =
            new LinearGraphGenerator<Object, DefaultEdge>(SIZE);
        final DirectedGraph<Object, DefaultEdge> g =
            new DefaultDirectedGraph<Object, DefaultEdge>(DefaultEdge.class);
        final Map<String, Object> resultMap = Maps.newHashMap();
        gen.generateGraph(g, vertexFactory, resultMap);
        assertEquals(SIZE, g.vertexSet().size());
        assertEquals(SIZE - 1, g.edgeSet().size());

        final Object startVertex = resultMap.get(LinearGraphGenerator.START_VERTEX);
        final Object endVertex = resultMap.get(LinearGraphGenerator.END_VERTEX);
        final Iterator vertexIter = g.vertexSet().iterator();

        while (vertexIter.hasNext()) {
            final Object vertex = vertexIter.next();

            if (vertex == startVertex) {
                assertEquals(0, g.inDegreeOf(vertex));
                assertEquals(1, g.outDegreeOf(vertex));

                continue;
            }

            if (vertex == endVertex) {
                assertEquals(1, g.inDegreeOf(vertex));
                assertEquals(0, g.outDegreeOf(vertex));

                continue;
            }

            assertEquals(1, g.inDegreeOf(vertex));
            assertEquals(1, g.outDegreeOf(vertex));
        }
    }

    /**
     * .
     */
    public void testRingGraphGenerator()
    {
        final GraphGenerator<Object, DefaultEdge, Object> gen =
            new RingGraphGenerator<Object, DefaultEdge>(SIZE);
        final DirectedGraph<Object, DefaultEdge> g =
            new DefaultDirectedGraph<Object, DefaultEdge>(DefaultEdge.class);
        final Map<String, Object> resultMap = Maps.newHashMap();
        gen.generateGraph(g, vertexFactory, resultMap);
        assertEquals(SIZE, g.vertexSet().size());
        assertEquals(SIZE, g.edgeSet().size());

        final Object startVertex = g.vertexSet().iterator().next();
        assertEquals(1, g.outDegreeOf(startVertex));

        Object nextVertex = startVertex;
        final Set<Object> seen = new HashSet<Object>();

        for (int i = 0; i < SIZE; ++i) {
            final DefaultEdge nextEdge =
                g.outgoingEdgesOf(nextVertex).iterator().next();
            nextVertex = g.getEdgeTarget(nextEdge);
            assertEquals(1, g.inDegreeOf(nextVertex));
            assertEquals(1, g.outDegreeOf(nextVertex));
            assertTrue(!seen.contains(nextVertex));
            seen.add(nextVertex);
        }

        // do you ever get the feeling you're going in circles?
        assertTrue(nextVertex == startVertex);
        assertTrue(resultMap.isEmpty());
    }

    /**
     * .
     */
    public void testCompleteGraphGenerator()
    {
        final Graph<Object, DefaultEdge> completeGraph =
            new SimpleGraph<Object, DefaultEdge>(DefaultEdge.class);
        final CompleteGraphGenerator<Object, DefaultEdge> completeGenerator =
            new CompleteGraphGenerator<Object, DefaultEdge>(10);
        completeGenerator.generateGraph(
            completeGraph,
            new ClassBasedVertexFactory<Object>(Object.class),
            null);

        // complete graph with 10 vertices has 10*(10-1)/2 = 45 edges
        assertEquals(45, completeGraph.edgeSet().size());
    }

    /**
     * .
     */
    public void testScaleFreeGraphGenerator()
    {
        final DirectedGraph<Object, DefaultEdge> graph =
            new DefaultDirectedGraph<Object, DefaultEdge>(DefaultEdge.class);
        ScaleFreeGraphGenerator<Object, DefaultEdge> generator =
            new ScaleFreeGraphGenerator<Object, DefaultEdge>(500);
        generator.generateGraph(graph, vertexFactory, null);
        final ConnectivityInspector<Object, DefaultEdge> inspector =
            new ConnectivityInspector<Object, DefaultEdge>(graph);
        assertTrue(
            "generated graph is not connected",
            inspector.isGraphConnected());

        try {
            generator = new ScaleFreeGraphGenerator<Object, DefaultEdge>(-50);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }

        try {
            generator =
                new ScaleFreeGraphGenerator<Object, DefaultEdge>(-50, 31337);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }

        generator = new ScaleFreeGraphGenerator<Object, DefaultEdge>(0);
        final DirectedGraph<Object, DefaultEdge> empty =
            new DefaultDirectedGraph<Object, DefaultEdge>(DefaultEdge.class);
        generator.generateGraph(empty, vertexFactory, null);
        assertTrue("non-empty graph generated", empty.vertexSet().isEmpty());
    }

    /**
     * .
     */
    public void testCompleteBipartiteGraphGenerator()
    {
        final Graph<Object, DefaultEdge> completeBipartiteGraph =
            new SimpleGraph<Object, DefaultEdge>(
                DefaultEdge.class);
        final CompleteBipartiteGraphGenerator<Object, DefaultEdge> completeBipartiteGenerator =
            new CompleteBipartiteGraphGenerator<Object, DefaultEdge>(
                10,
                4);
        completeBipartiteGenerator.generateGraph(
            completeBipartiteGraph,
            new ClassBasedVertexFactory<Object>(Object.class),
            null);

        // Complete bipartite graph with 10 and 4 vertices should have 14
        // total vertices and 4*10=40 total edges
        assertEquals(14, completeBipartiteGraph.vertexSet().size());
        assertEquals(40, completeBipartiteGraph.edgeSet().size());
    }

    /**
     * .
     */
    public void testHyperCubeGraphGenerator()
    {
        final Graph<Object, DefaultEdge> hyperCubeGraph =
            new SimpleGraph<Object, DefaultEdge>(
                DefaultEdge.class);
        final HyperCubeGraphGenerator<Object, DefaultEdge> hyperCubeGenerator =
            new HyperCubeGraphGenerator<Object, DefaultEdge>(
                4);
        hyperCubeGenerator.generateGraph(
            hyperCubeGraph,
            new ClassBasedVertexFactory<Object>(Object.class),
            null);

        // Hypercube of 4 dimensions should have 2^4=16 vertices and
        // 4*2^(4-1)=32 total edges
        assertEquals(16, hyperCubeGraph.vertexSet().size());
        assertEquals(32, hyperCubeGraph.edgeSet().size());
    }

    /**
     * .
     */
    public void testStarGraphGenerator()
    {
        final Map<String, Object> map = Maps.newHashMap();
        final Graph<Object, DefaultEdge> starGraph =
            new SimpleGraph<Object, DefaultEdge>(
                DefaultEdge.class);
        final StarGraphGenerator<Object, DefaultEdge> starGenerator =
            new StarGraphGenerator<Object, DefaultEdge>(
                10);
        starGenerator.generateGraph(
            starGraph,
            new ClassBasedVertexFactory<Object>(Object.class),
            map);

        // Star graph of order 10 should have 10 vertices and 9 edges
        assertEquals(9, starGraph.edgeSet().size());
        assertEquals(10, starGraph.vertexSet().size());
        assertTrue(map.get(StarGraphGenerator.CENTER_VERTEX) != null);
    }

    /**
     * .
     */
    public void testGridGraphGenerator()
    {
        final int rows = 3;
        final int cols = 4;

        //the form of these two classes helps debugging
        class StringVertexFactory
            implements VertexFactory<String>
        {
            int index = 1;

            @Override public String createVertex()
            {
                return String.valueOf(index++);
            }
        }

        class StringEdgeFactory
            implements EdgeFactory<String, String>
        {
            @Override public String createEdge(
                final String sourceVertex,
                final String targetVertex)
            {
                return sourceVertex + '-' + targetVertex;
            }
        }

        final GridGraphGenerator<String, String> generator =
            new GridGraphGenerator<String, String>(rows, cols);
        final Map<String, String> resultMap = Maps.newHashMap();

        //validating a directed and undirected graph
        final Graph<String, String> directedGridGraph =
            new DefaultDirectedGraph<String, String>(new StringEdgeFactory());
        generator.generateGraph(
            directedGridGraph,
            new StringVertexFactory(),
            resultMap);
        validateGridGraphGenerator(rows, cols, directedGridGraph, resultMap);

        resultMap.clear();
        final Graph<String, String> undirectedGridGraph =
            new SimpleGraph<String, String>(new StringEdgeFactory());
        generator.generateGraph(
            undirectedGridGraph,
            new StringVertexFactory(),
            resultMap);
        validateGridGraphGenerator(rows, cols, undirectedGridGraph, resultMap);
    }

    public static void validateGridGraphGenerator(final int rows,
        final int cols, final Graph<String, String> gridGraph,
        final Map<String, String> resultMap)
    {
        // graph structure validations
        final int expectedVerticeNum = rows * cols;
        assertEquals(
            "number of vertices is wrong (" + gridGraph
            .vertexSet().size()
            + "), should be " + expectedVerticeNum,
            expectedVerticeNum,
            gridGraph.vertexSet().size());
        final int expectedEdgesNum =
            ((rows - 1) * cols + (cols - 1) * rows)
            * (gridGraph instanceof UndirectedGraph ? 1 : 2);
        assertEquals(
            "number of edges is wrong (" + gridGraph
            .edgeSet().size()
            + "), should be " + expectedEdgesNum,
            expectedEdgesNum,
            gridGraph.edgeSet().size());

        int cornerVertices = 0, borderVertices = 0, innerVertices = 0,
            neighborsSize;
        final int expCornerVertices = 4;
        final int expBorderVertices =
            Math.max((rows - 2) * 2 + (cols - 2) * 2, 0);
        final int expInnerVertices = Math.max((rows - 2) * (cols - 2), 0);
        final Set<String> neighbors = new HashSet<String>();

        for (final String v : gridGraph.vertexSet()) {
            neighbors.clear();
            neighbors.addAll(Graphs.neighborListOf(gridGraph, v));
            neighborsSize = neighbors.size();
            assertTrue(
                "vertex with illegal number of neighbors (" + neighborsSize
                + ").",
                neighborsSize == 2
                || neighborsSize == 3
                || neighborsSize == 4);
            if (neighborsSize == 2) {
                cornerVertices++;
            } else if (neighborsSize == 3) {
                borderVertices++;
            } else if (neighborsSize == 4) {
                innerVertices++;
            }
        }
        assertEquals(
            "there should be exactly " + expCornerVertices
            + " corner (with two neighbors) vertices. "
            + " actual number is " + cornerVertices + ".",
            expCornerVertices,
            cornerVertices);
        assertEquals(
            "there should be exactly " + expBorderVertices
            + " border (with three neighbors) vertices. "
            + " actual number is " + borderVertices + ".",
            expBorderVertices,
            borderVertices);
        assertEquals(
            "there should be exactly " + expInnerVertices
            + " inner (with four neighbors) vertices. "
            + " actual number is " + innerVertices + ".",
            expInnerVertices,
            innerVertices);

        // result map validations
        final Set<String> keys = resultMap.keySet();
        assertEquals(
            "result map contains should contains exactly 4 corner verices",
            4,
            keys.size());

        for (final String key : keys) {
            neighbors.clear();
            neighbors.addAll(
                Graphs.neighborListOf(gridGraph, resultMap.get(key)));
            neighborsSize = neighbors.size();
            assertEquals(
                "corner vertex should have exactly 2 neighbors",
                2,
                neighborsSize);
        }
    }
}

// End GraphGeneratorTest.java
