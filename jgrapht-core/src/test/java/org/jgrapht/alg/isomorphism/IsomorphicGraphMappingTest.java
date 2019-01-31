/*
 * (C) Copyright 2015-2018, by Fabian Späh and Contributors.
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
package org.jgrapht.alg.isomorphism;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.jgrapht.alg.isomorphism.IsomorphismTestUtil.*;

/**
 * Tests for {@link IsomorphicGraphMapping}
 *
 * @author Alexandru Valeanu
 */
public class IsomorphicGraphMappingTest
{

    @Test
    public void testIdentity()
    {
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);

        for (char c = 'A'; c <= 'E'; c++) {
            tree1.addVertex(String.valueOf(c));
        }

        tree1.addEdge("A", "B");
        tree1.addEdge("A", "C");
        tree1.addEdge("C", "D");
        tree1.addEdge("C", "E");

        IsomorphicGraphMapping<String, DefaultEdge> identity =
            IsomorphicGraphMapping.identity(tree1);

        Graph<String, DefaultEdge> tree2 = generateMappedGraph(tree1, identity.getForwardMapping());

        AHURootedTreeIsomorphismInspector<String, DefaultEdge> isomorphism =
            new AHURootedTreeIsomorphismInspector<>(
                tree1, "A", tree2, identity.getVertexCorrespondence("A", true));

        Assert.assertTrue(isomorphism.isomorphismExists());
        Assert.assertTrue(areIsomorphic(tree1, tree2, identity));
    }

    @Test
    public void testCompositionOfMappings()
    {
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        tree1.addVertex("1");
        tree1.addVertex("2");
        tree1.addEdge("1", "2");

        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(DefaultEdge.class);
        tree2.addVertex("a");
        tree2.addVertex("b");
        tree2.addEdge("a", "b");

        Graph<String, DefaultEdge> tree3 = new SimpleGraph<>(DefaultEdge.class);
        tree3.addVertex("A");
        tree3.addVertex("B");
        tree3.addEdge("A", "B");

        AHUUnrootedTreeIsomorphismInspector<String, DefaultEdge> isomorphism =
            new AHUUnrootedTreeIsomorphismInspector<>(tree1, tree2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<String, DefaultEdge> mapping12 = isomorphism.getMapping();

        isomorphism = new AHUUnrootedTreeIsomorphismInspector<>(tree2, tree3);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<String, DefaultEdge> mapping23 = isomorphism.getMapping();

        IsomorphicGraphMapping<String, DefaultEdge> mapping13 = mapping12.compose(mapping23);

        Assert.assertTrue(areIsomorphic(tree1, tree3, mapping13));
    }

    @Test
    public void testCompositionOfRandomMappings()
    {
        final int NUM_TESTS = 1000;
        Random random = new Random(0x11_88_11);

        for (int test = 0; test < NUM_TESTS; test++) {
            final int N = 10 + random.nextInt(150);

            Graph<Integer, DefaultEdge> tree1 = generateTree(N, random);
            Graph<Integer, DefaultEdge> tree2 = generateIsomorphicGraph(tree1, random).getFirst();
            Graph<Integer, DefaultEdge> tree3 = generateIsomorphicGraph(tree2, random).getFirst();

            AHUUnrootedTreeIsomorphismInspector<Integer, DefaultEdge> isomorphism =
                new AHUUnrootedTreeIsomorphismInspector<>(tree1, tree2);

            IsomorphicGraphMapping<Integer, DefaultEdge> mapping12 = isomorphism.getMapping();

            isomorphism = new AHUUnrootedTreeIsomorphismInspector<>(tree2, tree3);
            IsomorphicGraphMapping<Integer, DefaultEdge> mapping23 = isomorphism.getMapping();

            IsomorphicGraphMapping<Integer, DefaultEdge> mapping13 = mapping12.compose(mapping23);

            Assert.assertTrue(areIsomorphic(tree1, tree3, mapping13));
        }
    }

}
