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
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class VF2SubgraphIsomorphismInspectorTest
{

    /**
     * Tests graph types: In case of invalid graph types or invalid combination of graph arguments
     * UnsupportedOperationException or InvalidArgumentException is expected
     */
    @Test
    public void testGraphTypes()
    {

        Graph<Integer, DefaultEdge> dg1 = new DefaultDirectedGraph<>(DefaultEdge.class);

        dg1.addVertex(1);
        dg1.addVertex(2);

        dg1.addEdge(1, 2);

        SimpleGraph<Integer, DefaultEdge> sg1 = new SimpleGraph<>(DefaultEdge.class);

        sg1.addVertex(1);
        sg1.addVertex(2);

        sg1.addEdge(1, 2);

        Multigraph<Integer, DefaultEdge> mg1 = new Multigraph<>(DefaultEdge.class);

        mg1.addVertex(1);
        mg1.addVertex(2);

        mg1.addEdge(1, 2);

        Pseudograph<Integer, DefaultEdge> pg1 = new Pseudograph<>(DefaultEdge.class);

        pg1.addVertex(1);
        pg1.addVertex(2);

        pg1.addEdge(1, 2);

        /* GT-0 test graph=null */
        try {
            new VF2SubgraphIsomorphismInspector<>(null, sg1);
            Assert.fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
        }

        /* GT-1: multigraphs */
        try {
            new VF2SubgraphIsomorphismInspector<>(mg1, mg1);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }

        /* GT-2: pseudographs */
        try {
            new VF2SubgraphIsomorphismInspector<>(pg1, pg1);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }

        /* GT-3: simple graphs */
        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> gt3 =
            new VF2SubgraphIsomorphismInspector<>(sg1, sg1);
        assertEquals(true, gt3.getMappings().hasNext());

        /* GT-4: directed graphs */
        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> gt4 =
            new VF2SubgraphIsomorphismInspector<>(dg1, dg1);
        assertEquals("[1=1 2=2]", gt4.getMappings().next().toString());

        /* GT-5: simple graph + multigraph */

        try {
            new VF2SubgraphIsomorphismInspector<>(sg1, mg1);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }

        /* GT-6: simple graph + pseudograph */
        try {
            new VF2SubgraphIsomorphismInspector<>(sg1, pg1);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }

        /* GT-7: directed graph + multigraph */
        try {
            new VF2SubgraphIsomorphismInspector<>(dg1, mg1);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }

        /* GT-8: directed graph + pseudograph */
        try {
            new VF2SubgraphIsomorphismInspector<>(dg1, pg1);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }

        /* GT-9: pseudograph + multigraph */
        try {
            new VF2SubgraphIsomorphismInspector<>(pg1, mg1);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }

        /* GT-10: simple graph + directed graph */
        try {
            new VF2SubgraphIsomorphismInspector<>(sg1, dg1);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }
    }

    /**
     * Tests edge cases on simple graphs
     */
    @Test
    public void testEdgeCasesSimpleGraph()
    {

        /* ECS-1: graph and subgraph empty */

        SimpleGraph<Integer, DefaultEdge> sg0v = new SimpleGraph<>(DefaultEdge.class),
            sg0v2 = new SimpleGraph<>(DefaultEdge.class);

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vfs1 =
            new VF2SubgraphIsomorphismInspector<>(sg0v, sg0v2);

        assertEquals("[]", vfs1.getMappings().next().toString());

        /* ECS-2: graph non-empty, subgraph empty */

        SimpleGraph<Integer, DefaultEdge> sg4v3e = new SimpleGraph<>(DefaultEdge.class);

        sg4v3e.addVertex(1);
        sg4v3e.addVertex(2);
        sg4v3e.addVertex(3);
        sg4v3e.addVertex(4);

        sg4v3e.addEdge(1, 2);
        sg4v3e.addEdge(3, 2);
        sg4v3e.addEdge(3, 4);

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vfs2 =
            new VF2SubgraphIsomorphismInspector<>(sg4v3e, sg0v);

        assertEquals("[1=~~ 2=~~ 3=~~ 4=~~]", vfs2.getMappings().next().toString());

        /* ECS-3: graph empty, subgraph non-empty */

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vfs3 =
            new VF2SubgraphIsomorphismInspector<>(sg0v, sg4v3e);

        assertEquals(false, vfs3.isomorphismExists());

        /* ECS-4: graph non-empty, subgraph single vertex */

        SimpleGraph<Integer, DefaultEdge> sg1v = new SimpleGraph<>(DefaultEdge.class);

        sg1v.addVertex(5);

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vfs4 =
            new VF2SubgraphIsomorphismInspector<>(sg4v3e, sg1v);

        Iterator<GraphMapping<Integer, DefaultEdge>> iter = vfs4.getMappings();

        Set<String> mappings = new HashSet<>(
            Arrays.asList(
                "[1=5 2=~~ 3=~~ 4=~~]", "[1=~~ 2=5 3=~~ 4=~~]", "[1=~~ 2=~~ 3=5 4=~~]",
                "[1=~~ 2=~~ 3=~~ 4=5]"));
        assertEquals(true, mappings.remove(iter.next().toString()));
        assertEquals(true, mappings.remove(iter.next().toString()));
        assertEquals(true, mappings.remove(iter.next().toString()));
        assertEquals(true, mappings.remove(iter.next().toString()));
        assertEquals(false, iter.hasNext());

        /* ECS-5: graph empty, subgraph single vertex */

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vfs5 =
            new VF2SubgraphIsomorphismInspector<>(sg0v, sg1v);

        assertEquals(false, vfs5.isomorphismExists());

        /* ECS-6: subgraph with vertices, but no edges */

        SimpleGraph<Integer, DefaultEdge> sg3v0e = new SimpleGraph<>(DefaultEdge.class);

        sg3v0e.addVertex(5);
        sg3v0e.addVertex(6);
        sg3v0e.addVertex(7);

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vfs6 =
            new VF2SubgraphIsomorphismInspector<>(sg4v3e, sg3v0e);

        assertEquals(false, vfs6.isomorphismExists());

        /* ECS-7: graph and subgraph with vertices, but no edges */

        SimpleGraph<Integer, DefaultEdge> sg2v0e = new SimpleGraph<>(DefaultEdge.class);

        sg2v0e.addVertex(1);
        sg2v0e.addVertex(2);

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vfs7 =
            new VF2SubgraphIsomorphismInspector<>(sg3v0e, sg2v0e);

        Iterator<GraphMapping<Integer, DefaultEdge>> iter7 = vfs7.getMappings();

        Set<String> mappings7 = new HashSet<>(
            Arrays.asList(
                "[5=1 6=2 7=~~]", "[5=1 6=~~ 7=2]", "[5=2 6=1 7=~~]", "[5=~~ 6=1 7=2]",
                "[5=2 6=~~ 7=1]", "[5=~~ 6=2 7=1]"));
        assertEquals(true, mappings7.remove(iter7.next().toString()));
        assertEquals(true, mappings7.remove(iter7.next().toString()));
        assertEquals(true, mappings7.remove(iter7.next().toString()));
        assertEquals(true, mappings7.remove(iter7.next().toString()));
        assertEquals(true, mappings7.remove(iter7.next().toString()));
        assertEquals(true, mappings7.remove(iter7.next().toString()));
        assertEquals(false, iter7.hasNext());

        /* ECS-8: graph no edges, subgraph contains single edge */

        SimpleGraph<Integer, DefaultEdge> sg2v1e = new SimpleGraph<>(DefaultEdge.class);

        sg2v1e.addVertex(5);
        sg2v1e.addVertex(6);

        sg2v1e.addEdge(5, 6);

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vfs8 =
            new VF2SubgraphIsomorphismInspector<>(sg3v0e, sg2v1e);

        assertEquals(false, vfs8.isomorphismExists());

        /*
         * ECS-9: complete graphs of different size, graph smaller than subgraph
         */

        SimpleGraph<Integer, DefaultEdge> sg5k = new SimpleGraph<>(DefaultEdge.class);

        sg5k.addVertex(0);
        sg5k.addVertex(1);
        sg5k.addVertex(2);
        sg5k.addVertex(3);
        sg5k.addVertex(4);

        sg5k.addEdge(0, 1);
        sg5k.addEdge(0, 2);
        sg5k.addEdge(0, 3);
        sg5k.addEdge(0, 4);
        sg5k.addEdge(1, 2);
        sg5k.addEdge(1, 3);
        sg5k.addEdge(1, 4);
        sg5k.addEdge(2, 3);
        sg5k.addEdge(2, 4);
        sg5k.addEdge(3, 4);

        SimpleGraph<Integer, DefaultEdge> sg4k = new SimpleGraph<>(DefaultEdge.class);

        sg4k.addVertex(0);
        sg4k.addVertex(1);
        sg4k.addVertex(2);
        sg4k.addVertex(3);

        sg4k.addEdge(0, 1);
        sg4k.addEdge(0, 2);
        sg4k.addEdge(0, 3);
        sg4k.addEdge(1, 2);
        sg4k.addEdge(1, 3);
        sg4k.addEdge(2, 3);

        SimpleGraph<Integer, DefaultEdge> sg3k = new SimpleGraph<>(DefaultEdge.class);

        sg3k.addVertex(0);
        sg3k.addVertex(1);
        sg3k.addVertex(2);

        sg3k.addEdge(0, 1);
        sg3k.addEdge(0, 2);
        sg3k.addEdge(1, 2);

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vfs9 =
            new VF2SubgraphIsomorphismInspector<>(sg4k, sg5k);

        assertEquals(false, vfs9.isomorphismExists());

        /*
         * ECS-10: complete graphs of different size, graph bigger than subgraph
         */

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vfs10 =
            new VF2SubgraphIsomorphismInspector<>(sg4k, sg3k);
        Iterator<GraphMapping<Integer, DefaultEdge>> iter10 = vfs10.getMappings();

        Set<String> mappings10 = new HashSet<>(
            Arrays.asList(
                "[0=0 1=1 2=2 3=~~]", "[0=0 1=1 2=~~ 3=2]", "[0=0 1=~~ 2=1 3=2]",
                "[0=~~ 1=0 2=1 3=2]", "[0=1 1=0 2=2 3=~~]", "[0=1 1=0 2=~~ 3=2]",
                "[0=1 1=~~ 2=0 3=2]", "[0=~~ 1=1 2=0 3=2]", "[0=2 1=1 2=0 3=~~]",
                "[0=2 1=1 2=~~ 3=0]", "[0=2 1=~~ 2=1 3=0]", "[0=~~ 1=2 2=1 3=0]",
                "[0=0 1=2 2=1 3=~~]", "[0=0 1=2 2=~~ 3=1]", "[0=0 1=~~ 2=2 3=1]",
                "[0=~~ 1=0 2=2 3=1]", "[0=1 1=2 2=0 3=~~]", "[0=1 1=2 2=~~ 3=0]",
                "[0=1 1=~~ 2=2 3=0]", "[0=~~ 1=1 2=2 3=0]", "[0=2 1=0 2=1 3=~~]",
                "[0=2 1=0 2=~~ 3=1]", "[0=2 1=~~ 2=0 3=1]", "[0=~~ 1=2 2=0 3=1]"));
        for (int i = 0; i < 24; i++)
            assertEquals(true, mappings10.remove(iter10.next().toString()));
        assertEquals(false, iter10.hasNext());

        /* ECS-11: isomorphic graphs */

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vfs11 =
            new VF2SubgraphIsomorphismInspector<>(sg4v3e, sg4v3e);

        Iterator<GraphMapping<Integer, DefaultEdge>> iter11 = vfs11.getMappings();

        Set<String> mappings11 =
            new HashSet<>(Arrays.asList("[1=1 2=2 3=3 4=4]", "[1=4 2=3 3=2 4=1]"));
        assertEquals(true, mappings11.remove(iter11.next().toString()));
        assertEquals(true, mappings11.remove(iter11.next().toString()));
        assertEquals(false, iter11.hasNext());

        /* ECS-12: not connected graphs of different size */

        SimpleGraph<Integer, DefaultEdge> sg6v4enc = new SimpleGraph<>(DefaultEdge.class);

        sg6v4enc.addVertex(0);
        sg6v4enc.addVertex(1);
        sg6v4enc.addVertex(2);
        sg6v4enc.addVertex(3);
        sg6v4enc.addVertex(4);
        sg6v4enc.addVertex(5);

        sg6v4enc.addEdge(1, 2);
        sg6v4enc.addEdge(2, 3);
        sg6v4enc.addEdge(3, 1);
        sg6v4enc.addEdge(4, 5);

        SimpleGraph<Integer, DefaultEdge> sg5v4enc = new SimpleGraph<>(DefaultEdge.class);

        sg5v4enc.addVertex(6);
        sg5v4enc.addVertex(7);
        sg5v4enc.addVertex(8);
        sg5v4enc.addVertex(9);
        sg5v4enc.addVertex(10);

        sg5v4enc.addEdge(7, 6);
        sg5v4enc.addEdge(9, 8);
        sg5v4enc.addEdge(10, 9);
        sg5v4enc.addEdge(8, 10);

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vfs12 =
            new VF2SubgraphIsomorphismInspector<>(sg6v4enc, sg5v4enc);

        Iterator<GraphMapping<Integer, DefaultEdge>> iter12 = vfs12.getMappings();

        Set<String> mappings12 = new HashSet<>(
            Arrays.asList(
                "[0=~~ 1=8 2=10 3=9 4=7 5=6]", "[0=~~ 1=9 2=8 3=10 4=7 5=6]",
                "[0=~~ 1=10 2=9 3=8 4=7 5=6]", "[0=~~ 1=8 2=10 3=9 4=6 5=7]",
                "[0=~~ 1=9 2=8 3=10 4=6 5=7]", "[0=~~ 1=10 2=9 3=8 4=6 5=7]",
                "[0=~~ 1=10 2=8 3=9 4=7 5=6]", "[0=~~ 1=8 2=9 3=10 4=7 5=6]",
                "[0=~~ 1=9 2=10 3=8 4=7 5=6]", "[0=~~ 1=10 2=8 3=9 4=6 5=7]",
                "[0=~~ 1=8 2=9 3=10 4=6 5=7]", "[0=~~ 1=9 2=10 3=8 4=6 5=7]"));
        for (int i = 0; i < 12; i++)
            assertEquals(true, mappings12.remove(iter12.next().toString()));
        assertEquals(false, iter12.hasNext());
    }

    /**
     * Tests edge cases on directed graphs
     */
    @Test
    public void testEdgeCasesDirectedGraph()
    {

        /* ECD-1: graph and subgraph empty */

        Graph<Integer, DefaultEdge> dg0v = new DefaultDirectedGraph<>(DefaultEdge.class),
            dg0v2 = new DefaultDirectedGraph<>(DefaultEdge.class);

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vf1 =
            new VF2SubgraphIsomorphismInspector<>(dg0v, dg0v2);

        assertEquals("[]", vf1.getMappings().next().toString());

        /* ECD-2: graph non-empty, subgraph empty */

        Graph<Integer, DefaultEdge> dg4v3e = new DefaultDirectedGraph<>(DefaultEdge.class);

        dg4v3e.addVertex(1);
        dg4v3e.addVertex(2);
        dg4v3e.addVertex(3);
        dg4v3e.addVertex(4);

        dg4v3e.addEdge(1, 2);
        dg4v3e.addEdge(3, 2);
        dg4v3e.addEdge(3, 4);

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vf2 =
            new VF2SubgraphIsomorphismInspector<>(dg4v3e, dg0v);

        assertEquals("[1=~~ 2=~~ 3=~~ 4=~~]", vf2.getMappings().next().toString());

        /* ECD-3: graph empty, subgraph non-empty */

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vf3 =
            new VF2SubgraphIsomorphismInspector<>(dg0v, dg4v3e);

        assertEquals(false, vf3.isomorphismExists());

        /* ECD-4: graph non-empty, subgraph single vertex */

        Graph<Integer, DefaultEdge> dg1v = new DefaultDirectedGraph<>(DefaultEdge.class);

        dg1v.addVertex(5);

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vf4 =
            new VF2SubgraphIsomorphismInspector<>(dg4v3e, dg1v);

        Iterator<GraphMapping<Integer, DefaultEdge>> iter4 = vf4.getMappings();

        Set<String> mappings = new HashSet<>(
            Arrays.asList(
                "[1=5 2=~~ 3=~~ 4=~~]", "[1=~~ 2=5 3=~~ 4=~~]", "[1=~~ 2=~~ 3=5 4=~~]",
                "[1=~~ 2=~~ 3=~~ 4=5]"));
        assertEquals(true, mappings.remove(iter4.next().toString()));
        assertEquals(true, mappings.remove(iter4.next().toString()));
        assertEquals(true, mappings.remove(iter4.next().toString()));
        assertEquals(true, mappings.remove(iter4.next().toString()));
        assertEquals(false, iter4.hasNext());

        /* ECD-5: graph empty, subgraph single vertex */

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vf5 =
            new VF2SubgraphIsomorphismInspector<>(dg0v, dg1v);

        assertEquals(false, vf5.isomorphismExists());

        /* ECD-6: subgraph with vertices, but no edges */

        Graph<Integer, DefaultEdge> dg3v0e = new DefaultDirectedGraph<>(DefaultEdge.class);

        dg3v0e.addVertex(5);
        dg3v0e.addVertex(6);
        dg3v0e.addVertex(7);

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vf6 =
            new VF2SubgraphIsomorphismInspector<>(dg4v3e, dg3v0e);

        assertEquals(false, vf6.isomorphismExists());

        /* ECD-7: graph and subgraph with vertices, but no edges */

        Graph<Integer, DefaultEdge> dg2v0e = new DefaultDirectedGraph<>(DefaultEdge.class);

        dg2v0e.addVertex(1);
        dg2v0e.addVertex(2);

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vf7 =
            new VF2SubgraphIsomorphismInspector<>(dg3v0e, dg2v0e);

        Iterator<GraphMapping<Integer, DefaultEdge>> iter7 = vf7.getMappings();

        Set<String> mappings7 = new HashSet<>(
            Arrays.asList(
                "[5=1 6=2 7=~~]", "[5=1 6=~~ 7=2]", "[5=2 6=1 7=~~]", "[5=~~ 6=1 7=2]",
                "[5=2 6=~~ 7=1]", "[5=~~ 6=2 7=1]"));
        assertEquals(true, mappings7.remove(iter7.next().toString()));
        assertEquals(true, mappings7.remove(iter7.next().toString()));
        assertEquals(true, mappings7.remove(iter7.next().toString()));
        assertEquals(true, mappings7.remove(iter7.next().toString()));
        assertEquals(true, mappings7.remove(iter7.next().toString()));
        assertEquals(true, mappings7.remove(iter7.next().toString()));
        assertEquals(false, iter7.hasNext());

        /* ECD-8: graph no edges, subgraph contains single edge */

        Graph<Integer, DefaultEdge> dg2v1e = new DefaultDirectedGraph<>(DefaultEdge.class);

        dg2v1e.addVertex(5);
        dg2v1e.addVertex(6);

        dg2v1e.addEdge(5, 6);

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vf8 =
            new VF2SubgraphIsomorphismInspector<>(dg3v0e, dg2v1e);

        assertEquals(false, vf8.isomorphismExists());

        /*
         * ECD-9: complete graphs of different size, graph smaller than subgraph
         */

        Graph<Integer, DefaultEdge> dg5c = new DefaultDirectedGraph<>(DefaultEdge.class);

        dg5c.addVertex(0);
        dg5c.addVertex(1);
        dg5c.addVertex(2);
        dg5c.addVertex(3);
        dg5c.addVertex(4);

        dg5c.addEdge(0, 1);
        dg5c.addEdge(0, 2);
        dg5c.addEdge(0, 3);
        dg5c.addEdge(0, 4);
        dg5c.addEdge(1, 2);
        dg5c.addEdge(1, 3);
        dg5c.addEdge(1, 4);
        dg5c.addEdge(2, 3);
        dg5c.addEdge(2, 4);
        dg5c.addEdge(3, 4);

        Graph<Integer, DefaultEdge> dg4c = new DefaultDirectedGraph<>(DefaultEdge.class);

        dg4c.addVertex(0);
        dg4c.addVertex(1);
        dg4c.addVertex(2);
        dg4c.addVertex(3);

        dg4c.addEdge(0, 1);
        dg4c.addEdge(0, 2);
        dg4c.addEdge(0, 3);
        dg4c.addEdge(1, 2);
        dg4c.addEdge(1, 3);
        dg4c.addEdge(2, 3);

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vf9 =
            new VF2SubgraphIsomorphismInspector<>(dg4c, dg5c);

        assertEquals(false, vf9.isomorphismExists());

        /*
         * ECD-10: complete graphs of different size, graph bigger than subgraph
         */

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vf10 =
            new VF2SubgraphIsomorphismInspector<>(dg5c, dg4c);

        Iterator<GraphMapping<Integer, DefaultEdge>> iter10 = vf10.getMappings();

        Set<String> mappings10 = new HashSet<>(
            Arrays.asList(
                "[0=0 1=1 2=2 3=3 4=~~]", "[0=0 1=1 2=2 3=~~ 4=3]", "[0=0 1=1 2=~~ 3=2 4=3]",
                "[0=0 1=~~ 2=1 3=2 4=3]", "[0=~~ 1=0 2=1 3=2 4=3]"));
        assertEquals(true, mappings10.remove(iter10.next().toString()));
        assertEquals(true, mappings10.remove(iter10.next().toString()));
        assertEquals(true, mappings10.remove(iter10.next().toString()));
        assertEquals(true, mappings10.remove(iter10.next().toString()));
        assertEquals(true, mappings10.remove(iter10.next().toString()));
        assertEquals(false, iter10.hasNext());

        /* ECD-11: isomorphic graphs */

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vf11 =
            new VF2SubgraphIsomorphismInspector<>(dg4v3e, dg4v3e);

        Iterator<GraphMapping<Integer, DefaultEdge>> iter11 = vf11.getMappings();

        assertEquals("[1=1 2=2 3=3 4=4]", iter11.next().toString());
        assertEquals(false, iter11.hasNext());

        /* ECD-12: not connected graphs of different size */

        Graph<Integer, DefaultEdge> dg6v4enc = new DefaultDirectedGraph<>(DefaultEdge.class);

        dg6v4enc.addVertex(0);
        dg6v4enc.addVertex(1);
        dg6v4enc.addVertex(2);
        dg6v4enc.addVertex(3);
        dg6v4enc.addVertex(4);
        dg6v4enc.addVertex(5);

        dg6v4enc.addEdge(1, 2);
        dg6v4enc.addEdge(2, 3);
        dg6v4enc.addEdge(3, 1);
        dg6v4enc.addEdge(4, 5);

        Graph<Integer, DefaultEdge> dg5v4enc = new DefaultDirectedGraph<>(DefaultEdge.class);

        dg5v4enc.addVertex(6);
        dg5v4enc.addVertex(7);
        dg5v4enc.addVertex(8);
        dg5v4enc.addVertex(9);
        dg5v4enc.addVertex(10);

        dg5v4enc.addEdge(7, 6);
        dg5v4enc.addEdge(9, 8);
        dg5v4enc.addEdge(10, 9);
        dg5v4enc.addEdge(8, 10);

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vf12 =
            new VF2SubgraphIsomorphismInspector<>(dg6v4enc, dg5v4enc);

        Iterator<GraphMapping<Integer, DefaultEdge>> iter12 = vf12.getMappings();

        Set<String> mappings12 = new HashSet<>(
            Arrays.asList(
                "[0=~~ 1=8 2=10 3=9 4=7 5=6]", "[0=~~ 1=9 2=8 3=10 4=7 5=6]",
                "[0=~~ 1=10 2=9 3=8 4=7 5=6]"));
        assertEquals(true, mappings12.remove(iter12.next().toString()));
        assertEquals(true, mappings12.remove(iter12.next().toString()));
        assertEquals(true, mappings12.remove(iter12.next().toString()));
        assertEquals(false, iter12.hasNext());
    }

    @Test
    public void testExhaustive()
    {

        /*
         * DET-1:
         *
         * 0 3 | /| 0 2 g1 = | 2 | g2 = |/ |/ | 1 1 4
         */

        SimpleGraph<Integer, DefaultEdge> g1 = new SimpleGraph<>(DefaultEdge.class),
            g2 = new SimpleGraph<>(DefaultEdge.class);

        g1.addVertex(0);
        g1.addVertex(1);
        g1.addVertex(2);
        g1.addVertex(3);
        g1.addVertex(4);

        g2.addVertex(0);
        g2.addVertex(1);
        g2.addVertex(2);

        g1.addEdge(0, 1);
        g1.addEdge(1, 2);
        g1.addEdge(2, 3);
        g1.addEdge(3, 4);

        g2.addEdge(0, 1);
        g2.addEdge(1, 2);

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vf2 =
            new VF2SubgraphIsomorphismInspector<>(g1, g2);

        assertEquals(true, SubgraphIsomorphismTestUtils.containsAllMatchings(vf2, g1, g2));

        /*
         * DET-2:
         *
         * g3 = ... g4 = ...
         *
         */

        Graph<Integer, DefaultEdge> g3 = new DefaultDirectedGraph<>(DefaultEdge.class),
            g4 = new DefaultDirectedGraph<>(DefaultEdge.class);

        g3.addVertex(0);
        g3.addVertex(1);
        g3.addVertex(2);
        g3.addVertex(3);
        g3.addVertex(4);
        g3.addVertex(5);

        g4.addVertex(0);
        g4.addVertex(1);
        g4.addVertex(2);
        g4.addVertex(3);

        g3.addEdge(0, 1);
        g3.addEdge(0, 5);
        g3.addEdge(1, 4);
        g3.addEdge(2, 1);
        g3.addEdge(2, 4);
        g3.addEdge(3, 1);
        g3.addEdge(4, 0);
        g3.addEdge(5, 2);
        g3.addEdge(5, 4);

        g4.addEdge(0, 3);
        g4.addEdge(1, 2);
        g4.addEdge(1, 3);
        g4.addEdge(2, 3);
        g4.addEdge(2, 0);

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vf3 =
            new VF2SubgraphIsomorphismInspector<>(g3, g4);

        assertEquals(true, SubgraphIsomorphismTestUtils.containsAllMatchings(vf3, g3, g4));

        /*
         * DET-3:
         *
         * 1----0 0---2 | | / g5 = | g6 = | / | |/ 2----3 1
         */

        SimpleGraph<Integer, DefaultEdge> g5 = new SimpleGraph<>(DefaultEdge.class),
            g6 = new SimpleGraph<>(DefaultEdge.class);

        g5.addVertex(0);
        g5.addVertex(1);
        g5.addVertex(2);
        g5.addVertex(3);

        g6.addVertex(0);
        g6.addVertex(1);
        g6.addVertex(2);

        g5.addEdge(0, 1);
        g5.addEdge(1, 2);
        g5.addEdge(2, 3);

        g6.addEdge(0, 1);
        g6.addEdge(1, 2);
        g6.addEdge(2, 0);

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vf4 =
            new VF2SubgraphIsomorphismInspector<>(g5, g6);

        assertEquals(true, SubgraphIsomorphismTestUtils.containsAllMatchings(vf4, g5, g6));
    }

    /**
     * RG-1: Tests if a all matchings are correct (on some random graphs).
     */
    @Test
    public void testRandomGraphs()
    {
        Random rnd = new Random();
        rnd.setSeed(54321);

        for (int i = 1; i < 50; i++) {
            int vertexCount = 2 + rnd.nextInt(i),
                edgeCount = vertexCount + rnd.nextInt(vertexCount * (vertexCount - 1)) / 2,
                subVertexCount = 1 + rnd.nextInt(vertexCount);

            Graph<Integer, DefaultEdge> g1 =
                SubgraphIsomorphismTestUtils.randomGraph(vertexCount, edgeCount, i),
                g2 = SubgraphIsomorphismTestUtils.randomSubgraph(g1, subVertexCount, i);

            VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vf2 =
                new VF2SubgraphIsomorphismInspector<>(g1, g2);

            SubgraphIsomorphismTestUtils.showLog(i + ": " + vertexCount + "v, " + edgeCount + "e ");

            for (Iterator<GraphMapping<Integer, DefaultEdge>> mappings = vf2.getMappings();
                mappings.hasNext();)
            {
                assertEquals(
                    true, SubgraphIsomorphismTestUtils.isCorrectMatching(mappings.next(), g1, g2));
                SubgraphIsomorphismTestUtils.showLog(".");
            }
            SubgraphIsomorphismTestUtils.showLog("\n");
        }
    }

    /**
     * RG-2: Tests if all matchings are correct and if every matching is found (on random graphs).
     */
    @Test
    public void testRandomGraphsExhaustive()
    {
        Random rnd = new Random();
        rnd.setSeed(12345);

        for (int i = 1; i < 100; i++) {
            int vertexCount = 3 + rnd.nextInt(5),
                edgeCount = rnd.nextInt(vertexCount * (vertexCount - 1)),
                subVertexCount = 2 + rnd.nextInt(vertexCount),
                subEdgeCount = rnd.nextInt(subVertexCount * (subVertexCount - 1));

            Graph<Integer, DefaultEdge> g1 =
                SubgraphIsomorphismTestUtils.randomGraph(vertexCount, edgeCount, i),
                g2 = SubgraphIsomorphismTestUtils.randomGraph(subVertexCount, subEdgeCount, i);

            VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vf2 =
                new VF2SubgraphIsomorphismInspector<>(g1, g2);

            SubgraphIsomorphismTestUtils
                .showLog(i + ": " + vertexCount + "v, " + edgeCount + "e ....\n");

            assertEquals(true, SubgraphIsomorphismTestUtils.containsAllMatchings(vf2, g1, g2));
        }
    }

    /**
     * SEM Tests the edge- and vertex-comparator
     */
    @Test
    public void testSemanticCheck()
    {

        /*
         * a---<3>---b | | g1 = <4> <1> g2 = A---<6>---b---<5>---B | | A---<2>---B
         */

        SimpleGraph<String, Integer> g1 = new SimpleGraph<>(Integer.class),
            g2 = new SimpleGraph<>(Integer.class);

        g1.addVertex("a");
        g1.addVertex("b");
        g1.addVertex("A");
        g1.addVertex("B");

        g1.addEdge("a", "b", 3);
        g1.addEdge("b", "B", 1);
        g1.addEdge("B", "A", 2);
        g1.addEdge("A", "a", 4);

        g2.addVertex("A");
        g2.addVertex("b");
        g2.addVertex("B");

        g2.addEdge("A", "b", 6);
        g2.addEdge("b", "B", 5);

        /*
         * SEM-1 test vertex and edge comparator
         */

        VF2SubgraphIsomorphismInspector<String, Integer> vf2 =
            new VF2SubgraphIsomorphismInspector<>(g1, g2, new VertexComp(), new EdgeComp());

        Iterator<GraphMapping<String, Integer>> iter = vf2.getMappings();

        assertEquals("[A=A B=b a=~~ b=B]", iter.next().toString());
        assertEquals(false, iter.hasNext());

        /*
         * SEM-2 test vertex comparator
         */

        VF2SubgraphIsomorphismInspector<String, Integer> vf3 =
            new VF2SubgraphIsomorphismInspector<>(
                g1, g2, new VertexComp(), new AlwaysEqualComparator<>());

        Iterator<GraphMapping<String, Integer>> iter2 = vf3.getMappings();

        Set<String> mappings =
            new HashSet<>(Arrays.asList("[A=A B=b a=~~ b=B]", "[A=~~ B=B a=A b=b]"));
        assertEquals(true, mappings.remove(iter2.next().toString()));
        assertEquals(true, mappings.remove(iter2.next().toString()));
        assertEquals(false, iter2.hasNext());

        /*
         * SEM-3 test edge comparator
         */

        VF2SubgraphIsomorphismInspector<String, Integer> vf4 =
            new VF2SubgraphIsomorphismInspector<>(
                g1, g2, new AlwaysEqualComparator<>(), new EdgeComp());

        Iterator<GraphMapping<String, Integer>> iter3 = vf4.getMappings();

        Set<String> mappings2 =
            new HashSet<>(Arrays.asList("[A=A B=b a=~~ b=B]", "[A=A B=~~ a=b b=B]"));
        assertEquals(true, mappings2.remove(iter3.next().toString()));
        assertEquals(true, mappings2.remove(iter3.next().toString()));
        assertEquals(false, iter3.hasNext());
    }

    private class VertexComp
        implements
        Comparator<String>
    {
        @Override
        public int compare(String o1, String o2)
        {
            if (o1.toLowerCase().equals(o2.toLowerCase()))
                return 0;
            else
                return 1;
        }
    }

    private class EdgeComp
        implements
        Comparator<Integer>
    {
        @Override
        public int compare(Integer o1, Integer o2)
        {
            return (o1 % 2) - (o2 % 2);
        }
    }

    /**
     * HG: measures time needed to check a pair of huge random graphs
     */
    @Test
    public void testHugeGraph()
    {
        int n = 700;
        long time = System.currentTimeMillis();

        Graph<Integer, DefaultEdge> g1 =
            SubgraphIsomorphismTestUtils.randomGraph(n, n * n / 50, 12345),
            g2 = SubgraphIsomorphismTestUtils.randomSubgraph(g1, n / 2, 54321);

        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vf2 =
            new VF2SubgraphIsomorphismInspector<>(g1, g2);

        assertEquals(true, vf2.isomorphismExists());

        SubgraphIsomorphismTestUtils.showLog(
            "|V1| = " + g1.vertexSet().size() + ", |E1| = " + g1.edgeSet().size() + ", |V2| = "
                + g2.vertexSet().size() + ", |E2| = " + g2.edgeSet().size() + " - "
                + (System.currentTimeMillis() - time) + "ms");
    }

}
