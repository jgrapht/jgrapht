/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.alg.decomposition;

import org.jgrapht.*;
import org.jgrapht.alg.connectivity.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;
import org.jgrapht.util.*;
import org.junit.*;
import org.junit.experimental.categories.*;

import java.util.*;
import java.util.stream.*;

import static org.jgrapht.util.MathUtil.log2;

/**
 * Tests for {@link HeavyPathDecomposition}
 *
 * @author Alexandru Valeanu
 */
public class HeavyPathDecompositionTest
{

    // Count the maximum number of light edges on any root-to-leaf path
    public static <V,
        E> int countMaxPath(Graph<V, E> graph, HeavyPathDecomposition<V, E> decomposition)
    {
        Set<GraphPath<V, E>> paths = decomposition.getPathDecomposition().getPaths();
        Map<V, Integer> whichPath = new HashMap<>();

        int i = 0;
        for (GraphPath<V, E> path : paths) {
            List<V> vertexList = path.getVertexList();

            for (int j = 0; j < vertexList.size(); j++) {
                whichPath.put(vertexList.get(j), i);
            }

            i++;
        }

        int maxim = 0;
        HeavyPathDecomposition<V, E>.InternalState state = decomposition.getInternalState();

        for (V v : graph.vertexSet()) {
            if (whichPath.containsKey(v)) {
                int cnt = 0;

                while (true) {
                    V u = state.getParent(v);

                    if (u != null) {
                        E edge = graph.getEdge(u, v);

                        if (decomposition.getLightEdges().contains(edge)) {
                            cnt++;
                        }

                        v = u;
                    } else
                        break;
                }

                maxim = Math.max(maxim, cnt);
            }
        }

        return maxim;
    }

    public static <V, E> boolean isValidDecomposition(
        Graph<V, E> graph, Set<V> roots, HeavyPathDecomposition<V, E> decomposition)
    {
        Set<E> heavyEdges = decomposition.getHeavyEdges();
        Set<E> lightEdges = decomposition.getLightEdges();

        Set<E> allEdges = new HashSet<>(heavyEdges);
        allEdges.addAll(lightEdges);

        // Check that heavyEdges + lightEdges = allEdges

        if (!allEdges.equals(graph.edgeSet()))
            return false;

        Set<GraphPath<V, E>> paths = decomposition.getPathDecomposition().getPaths();
        Map<V, Integer> whichPath = new HashMap<>();

        int i = 0;
        for (GraphPath<V, E> path : paths) {
            List<V> vertexList = path.getVertexList();

            for (int j = 0; j < vertexList.size(); j++) {
                // Check if a vertex appear more than once in the decomposition
                if (whichPath.containsKey(vertexList.get(j)))
                    return false;

                whichPath.put(vertexList.get(j), i);

                // Check if the path is actually a valid path in the graph
                if (j > 0) {
                    if (!graph.containsEdge(vertexList.get(j - 1), vertexList.get(j)))
                        return false;
                }
            }

            i++;
        }

        ConnectivityInspector<V, E> connectivityInspector = new ConnectivityInspector<>(graph);

        // Check if every reachable vertex from a root is in a path
        for (V root : roots) {
            for (V v : connectivityInspector.connectedSetOf(root))
                if (!whichPath.containsKey(v)) {
                    return false;
                }
        }

        for (V root : roots) {
            BreadthFirstIterator<V, E> bfs = new BreadthFirstIterator<>(graph, root);

            List<V> postOrder = new ArrayList<>();

            while (bfs.hasNext()) {
                V v = bfs.next();
                postOrder.add(v);
            }

            Collections.reverse(postOrder);

            Map<V, Integer> sizeSubtree = new HashMap<>(graph.vertexSet().size());
            for (V v : postOrder) {
                sizeSubtree.put(v, 1);

                for (E edge : graph.edgesOf(v)) {
                    V u = Graphs.getOppositeVertex(graph, edge, v);

                    if (!u.equals(bfs.getParent(v))) {
                        int sizeU = sizeSubtree.get(u);
                        sizeSubtree.put(v, sizeSubtree.get(v) + sizeU);
                    }
                }

                for (E edge : graph.edgesOf(v)) {
                    if (lightEdges.contains(edge)) {
                        V u = Graphs.getOppositeVertex(graph, edge, v);

                        if (!u.equals(bfs.getParent(v))
                            && 2 * sizeSubtree.get(u) > sizeSubtree.get(v))
                        {
                            return false;
                        }
                    } else { // edge is heavy
                        V u = Graphs.getOppositeVertex(graph, edge, v);

                        if (!u.equals(bfs.getParent(v))
                            && 2 * sizeSubtree.get(u) <= sizeSubtree.get(v))
                        {
                            return false;
                        }
                    }
                }
            }
        }

        return countMaxPath(graph, decomposition) <= log2(graph.vertexSet().size());
    }

    @Test(expected = NullPointerException.class)
    public void testNullGraph()
    {
        HeavyPathDecomposition<Integer, DefaultEdge> heavyPathDecomposition =
            new HeavyPathDecomposition<>(null, 1);
    }

    @Test(expected = NullPointerException.class)
    public void testNullRoot()
    {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        String s = null;

        HeavyPathDecomposition<String, DefaultEdge> heavyPathDecomposition =
            new HeavyPathDecomposition<>(graph, s);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRootNotInTree()
    {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex("a");

        HeavyPathDecomposition<String, DefaultEdge> heavyPathDecomposition =
            new HeavyPathDecomposition<>(graph, "b");
    }

    @Test
    public void testNoHeavyEdges()
    {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex("1");
        graph.addVertex("2");
        graph.addVertex("3");
        graph.addVertex("4");

        graph.addEdge("1", "2");
        graph.addEdge("1", "3");
        graph.addEdge("1", "4");

        HeavyPathDecomposition<String, DefaultEdge> heavyPathDecomposition =
            new HeavyPathDecomposition<>(graph, "1");

        Assert.assertTrue(heavyPathDecomposition.getHeavyEdges().isEmpty());
        Assert.assertTrue(
            isValidDecomposition(graph, Collections.singleton("1"), heavyPathDecomposition));
    }

    @Test
    public void testOneVertex()
    {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex("a");

        HeavyPathDecomposition<String, DefaultEdge> heavyPathDecomposition =
            new HeavyPathDecomposition<>(graph, "a");

        Assert.assertEquals(1, heavyPathDecomposition.getPathDecomposition().numberOfPaths());
        Assert.assertTrue(
            isValidDecomposition(graph, Collections.singleton("a"), heavyPathDecomposition));
    }

    @Test
    public void testLineGraph()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 1; i <= 11; i++)
            graph.addVertex(i);

        for (int i = 1; i < 11; i++)
            graph.addEdge(i, i + 1);

        HeavyPathDecomposition<Integer, DefaultEdge> heavyPathDecomposition =
            new HeavyPathDecomposition<>(graph, 1);

        Assert.assertEquals(1, heavyPathDecomposition.getPathDecomposition().numberOfPaths());
        Assert.assertTrue(
            isValidDecomposition(graph, Collections.singleton(1), heavyPathDecomposition));
    }

    @Test
    public void testLineGraph2()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 1; i <= 11; i++)
            graph.addVertex(i);

        for (int i = 1; i < 11; i++)
            graph.addEdge(i, i + 1);

        HeavyPathDecomposition<Integer, DefaultEdge> heavyPathDecomposition =
            new HeavyPathDecomposition<>(graph, 5);

        Assert.assertEquals(2, heavyPathDecomposition.getPathDecomposition().numberOfPaths());
        Assert.assertTrue(
            isValidDecomposition(graph, Collections.singleton(5), heavyPathDecomposition));
    }

    @Test
    public void testSmallTree()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 1; i <= 11; i++)
            graph.addVertex(i);

        graph.addEdge(1, 2);
        graph.addEdge(2, 4);
        graph.addEdge(2, 5);
        graph.addEdge(2, 6);
        graph.addEdge(4, 7);
        graph.addEdge(4, 8);
        graph.addEdge(6, 9);
        graph.addEdge(1, 3);
        graph.addEdge(3, 10);
        graph.addEdge(3, 11);

        HeavyPathDecomposition<Integer, DefaultEdge> heavyPathDecomposition =
            new HeavyPathDecomposition<>(graph, 1);

        Assert.assertTrue(
            isValidDecomposition(graph, Collections.singleton(1), heavyPathDecomposition));
    }

    @Test
    public void testDisconnectedSmallGraph()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        graph.addVertex(1);
        graph.addVertex(2);
        graph.addEdge(1, 2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addEdge(3, 4);

        HeavyPathDecomposition<Integer, DefaultEdge> heavyPathDecomposition =
            new HeavyPathDecomposition<>(graph, 1);

        Assert.assertEquals(1, heavyPathDecomposition.getPathDecomposition().numberOfPaths());
        Assert.assertTrue(heavyPathDecomposition.getHeavyEdges().isEmpty());
        Assert.assertEquals(1, heavyPathDecomposition.getLightEdges().size());
    }

    @Test
    @Category(SlowTests.class)
    public void testRandomTrees()
    {
        final int NUM_TESTS = 100;
        Random random = new Random(0x2882);

        for (int test = 0; test < NUM_TESTS; test++) {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(0), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            BarabasiAlbertForestGenerator<Integer, DefaultEdge> generator =
                new BarabasiAlbertForestGenerator<>(1, 1024 + random.nextInt(1 << 12), random);

            generator.generateGraph(graph, null);

            Set<Integer> roots = Collections.singleton(graph.vertexSet().iterator().next());

            HeavyPathDecomposition<Integer, DefaultEdge> heavyPathDecomposition =
                new HeavyPathDecomposition<>(graph, roots);

            Assert.assertTrue(isValidDecomposition(graph, roots, heavyPathDecomposition));
        }
    }

    @Test
    @Category(SlowTests.class)
    public void testRandomForests()
    {
        final int NUM_TESTS = 1000;
        Random random = new Random(0x1881);

        for (int test = 0; test < NUM_TESTS; test++) {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(0), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            BarabasiAlbertForestGenerator<Integer, DefaultEdge> generator =
                new BarabasiAlbertForestGenerator<>(
                    1 + random.nextInt(20), 50 + random.nextInt(1 << 11), random);

            generator.generateGraph(graph, null);

            ConnectivityInspector<Integer, DefaultEdge> connectivityInspector =
                new ConnectivityInspector<>(graph);
            List<Set<Integer>> connectedComponents = connectivityInspector.connectedSets();
            Set<Integer> roots = connectedComponents
                .stream().map(component -> component.iterator().next()).collect(Collectors.toSet());

            HeavyPathDecomposition<Integer, DefaultEdge> heavyPathDecomposition =
                new HeavyPathDecomposition<>(graph, roots);

            Assert.assertTrue(isValidDecomposition(graph, roots, heavyPathDecomposition));
        }
    }

    @Test
    @Category(SlowTests.class)
    public void testHugeTree()
    {
        Random random = new Random(0x118811);

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(0), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        BarabasiAlbertForestGenerator<Integer, DefaultEdge> generator =
            new BarabasiAlbertForestGenerator<>(1, 1 << 19, random);

        generator.generateGraph(graph, null);

        Set<Integer> roots = Collections.singleton(graph.vertexSet().iterator().next());

        HeavyPathDecomposition<Integer, DefaultEdge> heavyPathDecomposition =
            new HeavyPathDecomposition<>(graph, roots);

        Assert.assertTrue(isValidDecomposition(graph, roots, heavyPathDecomposition));
    }
}
