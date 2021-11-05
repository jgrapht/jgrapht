/*
 * (C) Copyright 2015-2021, by Florian Buenzli and Contributors.
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
package org.jgrapht.alg.clique;

import org.jgrapht.*;
import org.jgrapht.alg.connectivity.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the Clique Minimal Separator Decomposition.
 * 
 * @author Florian Buenzli {@literal <fbuenzli@student.ethz.ch>}
 */
public class CliqueMinimalSeparatorDecompositionTest
{

    /**
     * Test graph:<br>
     * <code>o-o<br>
     * |/|<br>
     * o-o<br></code>
     */
    @Test
    public void testSimpleGraph1()
    {
        SimpleGraph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(1, 3);
        g.addEdge(2, 4);
        // validate graph
        assertEquals(4, g.vertexSet().size());
        assertEquals(5, g.edgeSet().size());

        CliqueMinimalSeparatorDecomposition<Integer, DefaultEdge> cmsd =
            new CliqueMinimalSeparatorDecomposition<>(g);
        // check triangulation
        assertEquals(4, cmsd.getMinimalTriangulation().vertexSet().size());
        assertEquals(5, cmsd.getMinimalTriangulation().edgeSet().size());

        // check atoms
        boolean atom1found = false, atom2found = false;
        for (Set<Integer> atom : cmsd.getAtoms()) {
            if (atom.equals(Set.of(1, 2, 3))) {
                atom1found = true;
            } else if (atom.equals(Set.of(2, 3, 4))) {
                atom2found = true;
            }
        }
        assertEquals(2, cmsd.getAtoms().size());
        assertTrue(atom1found);
        assertTrue(atom2found);

        // check seprators
        boolean separator1found = false;
        for (Set<Integer> separator : cmsd.getSeparators()) {
            if (separator.equals(Set.of(2, 3))) {
                separator1found = true;
            }
        }
        assertEquals(1, cmsd.getSeparators().size());
        assertTrue(separator1found);
    }

    /**
     * Test pseudo graph based on:<br>
     * <code>o-o<br>
     * |/|<br>
     * o-o<br></code>
     */
    @Test
    public void testPseudograph1()
    {
        Pseudograph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addEdge(1, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 2);
        g.addEdge(2, 3);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(1, 3);
        g.addEdge(2, 4);
        g.addEdge(2, 4);
        g.addEdge(2, 4);
        // validate graph
        assertEquals(4, g.vertexSet().size());
        assertEquals(10, g.edgeSet().size());

        CliqueMinimalSeparatorDecomposition<Integer, DefaultEdge> cmsd =
            new CliqueMinimalSeparatorDecomposition<>(g);
        // check triangulation
        assertEquals(4, cmsd.getMinimalTriangulation().vertexSet().size());
        assertEquals(5, cmsd.getMinimalTriangulation().edgeSet().size());

        // check atoms
        boolean atom1found = false, atom2found = false;
        for (Set<Integer> atom : cmsd.getAtoms()) {
            if (atom.equals(Set.of(1, 2, 3))) {
                atom1found = true;
            } else if (atom.equals(Set.of(2, 3, 4))) {
                atom2found = true;
            }
        }
        assertEquals(2, cmsd.getAtoms().size());
        assertTrue(atom1found);
        assertTrue(atom2found);

        // check seprators
        boolean separator1found = false;
        for (Set<Integer> separator : cmsd.getSeparators()) {
            if (separator.equals(Set.of(2, 3))) {
                separator1found = true;
            }
        }
        assertEquals(1, cmsd.getSeparators().size());
        assertTrue(separator1found);
    }

    /**
     * Test graph:<br>
     * <code>o-o<br>
     * | |<br>
     * o-o<br></code>
     */
    @Test
    public void testSimpleGraph2()
    {
        SimpleGraph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(4, 1);
        // validate graph
        assertEquals(4, g.vertexSet().size());
        assertEquals(4, g.edgeSet().size());

        CliqueMinimalSeparatorDecomposition<Integer, DefaultEdge> cmsd =
            new CliqueMinimalSeparatorDecomposition<>(g);
        // check triangulation
        assertEquals(4, cmsd.getMinimalTriangulation().vertexSet().size());
        assertEquals(5, cmsd.getMinimalTriangulation().edgeSet().size());

        // check atoms
        boolean atom1found = false;
        for (Set<Integer> atom : cmsd.getAtoms()) {
            if (atom.equals(Set.of(1, 2, 3, 4))) {
                atom1found = true;
            }
        }
        assertEquals(1, cmsd.getAtoms().size());
        assertTrue(atom1found);

        // check seprators
        assertEquals(0, cmsd.getSeparators().size());
    }

    /**
     * Test graph: An Introduction to Clique Minimal Separator Decomposition, Berry et al. 2010,
     * Figure 1, DOI:10.3390/a3020197 <a href="http://www.mdpi.com/1999-4893/3/2/197">
     * http://www.mdpi.com/1999-4893/3/2/197</a>
     */
    @Test
    public void testBerry2010()
    {
        SimpleGraph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("e");
        g.addVertex("f");
        g.addVertex("g");
        g.addVertex("h");
        g.addVertex("i");
        g.addVertex("j");
        g.addVertex("k");
        g.addEdge("a", "b");
        g.addEdge("b", "c");
        g.addEdge("c", "d");
        g.addEdge("d", "e");
        g.addEdge("e", "g");
        g.addEdge("f", "g");
        g.addEdge("d", "f");
        g.addEdge("a", "k");
        g.addEdge("k", "j");
        g.addEdge("c", "k");
        g.addEdge("d", "j");
        g.addEdge("c", "j");
        g.addEdge("d", "k");
        g.addEdge("f", "k");
        g.addEdge("f", "j");
        g.addEdge("g", "j");
        g.addEdge("g", "k");
        g.addEdge("h", "j");
        g.addEdge("h", "i");
        g.addEdge("i", "k");
        // validate graph
        assertEquals(11, g.vertexSet().size());
        assertEquals(20, g.edgeSet().size());

        CliqueMinimalSeparatorDecomposition<String, DefaultEdge> cmsd =
            new CliqueMinimalSeparatorDecomposition<>(g);
        // check triangulation
        assertEquals(11, cmsd.getMinimalTriangulation().vertexSet().size());
        assertEquals(20 + 3, cmsd.getMinimalTriangulation().edgeSet().size());

        // check atoms
        boolean atom1found = false, atom2found = false, atom3found = false, atom4found = false;
        for (Set<String> atom : cmsd.getAtoms()) {
            if (atom.equals(Set.of("a", "b", "c", "k"))) {
                atom1found = true;
            } else if (atom.equals(Set.of("c", "d", "j", "k"))) {
                atom2found = true;
            } else if (atom.equals(Set.of("h", "i", "j", "k"))) {
                atom3found = true;
            } else if (atom.equals(Set.of("d", "e", "f", "g", "j", "k"))) {
                atom4found = true;
            }
        }
        assertEquals(4, cmsd.getAtoms().size());
        assertTrue(atom1found);
        assertTrue(atom2found);
        assertTrue(atom3found);
        assertTrue(atom4found);

        // check seprators
        boolean separator1found = false, separator2found = false, separator3found = false;
        for (Set<String> separator : cmsd.getSeparators()) {
            if (separator.equals(Set.of("c", "k"))) {
                separator1found = true;
            } else if (separator.equals(Set.of("j", "k"))) {
                separator2found = true;
            } else if (separator.equals(Set.of("d", "j", "k"))) {
                separator3found = true;
            }
        }
        assertEquals(3, cmsd.getSeparators().size());
        assertTrue(separator1found);
        assertTrue(separator2found);
        assertTrue(separator3found);
    }

    /**
     * Test graph: Decomposition by clique separators, Tarjan, 1985, Figure 1, DOI:
     * 10.1016/0012-365X(85)90051-2
     * <a href="http://www.sciencedirect.com/science/article/pii/0012365X85900512">
     * http://www.sciencedirect.com/science/article/pii/0012365X85900512</a>
     */
    @Test
    public void testTarjan1985()
    {
        SimpleGraph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("e");
        g.addVertex("f");
        g.addVertex("g");
        g.addVertex("h");
        g.addVertex("i");
        g.addVertex("j");
        g.addVertex("k");
        g.addEdge("a", "c");
        g.addEdge("a", "d");
        g.addEdge("a", "f");
        g.addEdge("b", "c");
        g.addEdge("b", "g");
        g.addEdge("c", "d");
        g.addEdge("c", "f");
        g.addEdge("c", "h");
        g.addEdge("d", "e");
        g.addEdge("d", "f");
        g.addEdge("d", "i");
        g.addEdge("e", "j");
        g.addEdge("f", "k");
        g.addEdge("g", "h");
        g.addEdge("h", "k");
        g.addEdge("i", "j");
        g.addEdge("i", "k");
        // validate graph
        assertEquals(11, g.vertexSet().size());
        assertEquals(17, g.edgeSet().size());

        CliqueMinimalSeparatorDecomposition<String, DefaultEdge> cmsd =
            new CliqueMinimalSeparatorDecomposition<>(g);
        // check triangulation
        assertEquals(11, cmsd.getMinimalTriangulation().vertexSet().size());

        // disabled: this currently returns 23 instead of 21
        /*
         * assertEquals(17 + 4, cmsd.getMinimalTriangulation().edgeSet().size());
         */

        // check atoms
        boolean atom1found = false, atom2found = false, atom3found = false, atom4found = false;
        for (Set<String> atom : cmsd.getAtoms()) {
            if (atom.equals(Set.of("a", "c", "d", "f"))) {
                atom1found = true;
            } else if (atom.equals(Set.of("b", "c", "g", "h"))) {
                atom2found = true;
            } else if (atom.equals(Set.of("d", "e", "i", "j"))) {
                atom3found = true;
            } else if (atom.equals(Set.of("c", "d", "f", "h", "i", "k"))) {
                atom4found = true;
            }
        }
        assertEquals(4, cmsd.getAtoms().size());
        assertTrue(atom1found);
        assertTrue(atom2found);
        assertTrue(atom3found);
        assertTrue(atom4found);

        // check seprators
        boolean separator1found = false, separator2found = false, separator3found = false;
        for (Set<String> separator : cmsd.getSeparators()) {
            if (separator.equals(Set.of("c", "d", "f"))) {
                separator1found = true;
            } else if (separator.equals(Set.of("c", "h"))) {
                separator2found = true;
            } else if (separator.equals(Set.of("d", "i"))) {
                separator3found = true;
            }
        }
        assertEquals(3, cmsd.getSeparators().size());
        assertTrue(separator1found);
        assertTrue(separator2found);
        assertTrue(separator3found);
    }

    /**
     * Test graph: CliqueMinimalSeparatorDecomposition1.jpg
     * <p>
     * <img src="CliqueMinimalSeparatorDecomposition1.jpg" />
     */
    @Test
    public void testGraph1()
    {
        SimpleGraph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("e");
        g.addVertex("f");
        g.addVertex("g");
        g.addVertex("h");
        g.addVertex("i");
        g.addVertex("j");
        g.addVertex("k");
        g.addVertex("l");
        g.addVertex("m");
        g.addVertex("n");
        g.addEdge("a", "b");
        g.addEdge("a", "d");
        g.addEdge("b", "e");
        g.addEdge("c", "e");
        g.addEdge("d", "e");
        g.addEdge("e", "f");
        g.addEdge("d", "g");
        g.addEdge("d", "h");
        g.addEdge("f", "k");
        g.addEdge("g", "i");
        g.addEdge("h", "i");
        g.addEdge("d", "i");
        g.addEdge("e", "j");
        g.addEdge("i", "j");
        g.addEdge("j", "k");
        g.addEdge("i", "l");
        g.addEdge("i", "m");
        g.addEdge("i", "n");
        g.addEdge("j", "m");
        g.addEdge("j", "n");
        // validate graph
        assertEquals(14, g.vertexSet().size());
        assertEquals(20, g.edgeSet().size());

        CliqueMinimalSeparatorDecomposition<String, DefaultEdge> cmsd =
            new CliqueMinimalSeparatorDecomposition<>(g);
        // check triangulation
        assertEquals(14, cmsd.getMinimalTriangulation().vertexSet().size());
        assertEquals(20 + 3, cmsd.getMinimalTriangulation().edgeSet().size());

        // check atoms
        assertEquals(9, cmsd.getAtoms().size());
        boolean[] atomsFound = new boolean[cmsd.getAtoms().size()];
        for (Set<String> atom : cmsd.getAtoms()) {
            if (atom.equals(Set.of("a", "b", "d", "e"))) {
                atomsFound[0] = true;
            } else if (atom.equals(Set.of("c", "e"))) {
                atomsFound[1] = true;
            } else if (atom.equals(Set.of("d", "g", "i"))) {
                atomsFound[2] = true;
            } else if (atom.equals(Set.of("d", "h", "i"))) {
                atomsFound[3] = true;
            } else if (atom.equals(Set.of("d", "e", "i", "j"))) {
                atomsFound[4] = true;
            } else if (atom.equals(Set.of("e", "f", "j", "k"))) {
                atomsFound[5] = true;
            } else if (atom.equals(Set.of("i", "l"))) {
                atomsFound[6] = true;
            } else if (atom.equals(Set.of("i", "j", "m"))) {
                atomsFound[7] = true;
            } else if (atom.equals(Set.of("i", "j", "n"))) {
                atomsFound[8] = true;
            }
        }
        for (int i = 0; i < atomsFound.length; ++i)
            assertTrue("atom " + i, atomsFound[i]);

        // check seprators
        assertEquals(6, cmsd.getSeparators().size());
        boolean[] separatorsFound = new boolean[cmsd.getSeparators().size()];
        for (Set<String> separator : cmsd.getSeparators()) {
            if (separator.equals(Set.of("d", "e"))) {
                separatorsFound[0] = true;
            } else if (separator.equals(Set.of("e"))) {
                separatorsFound[1] = true;
            } else if (separator.equals(Set.of("d", "i"))) {
                separatorsFound[2] = true;
            } else if (separator.equals(Set.of("i"))) {
                separatorsFound[3] = true;
            } else if (separator.equals(Set.of("e", "j"))) {
                separatorsFound[4] = true;
            } else if (separator.equals(Set.of("i", "j"))) {
                separatorsFound[5] = true;
            }
        }
        for (int i = 0; i < separatorsFound.length; ++i)
            assertTrue("separator " + i, separatorsFound[i]);

        // check component counts
        assertEquals(6, cmsd.getFullComponentCount().size());

        assertEquals(2, cmsd.getFullComponentCount().get(Set.of("d", "e")).intValue());

        assertEquals(2, cmsd.getFullComponentCount().get(Set.of("e")).intValue());

        assertEquals(3, cmsd.getFullComponentCount().get(Set.of("d", "i")).intValue());

        assertEquals(2, cmsd.getFullComponentCount().get(Set.of("i")).intValue());

        assertEquals(2, cmsd.getFullComponentCount().get(Set.of("e", "j")).intValue());

        assertEquals(3, cmsd.getFullComponentCount().get(Set.of("i", "j")).intValue());
    }

    /**
     * Test random graphs. You may change the number of vertices and edges.
     */
    @Test
    public void testRandomGraphs()
    {
        int rounds = 42;
        while (rounds-- > 0) {

            // number of vertices
            final int n = 80 + rounds;

            // number of edges, sparse but enough to get a connected graph,
            // 10% more than phase transition from disconnected to conntected.
            final int m = (int) Math.ceil(1.1 * Math.log(n) / n * (n * (n - 1) / 2));

            // generate a connected random graph with n vertices and m edges
            GraphGenerator<Integer, DefaultEdge, Integer> generator;
            SimpleGraph<Integer, DefaultEdge> g;
            ConnectivityInspector<Integer, DefaultEdge> inspector;
            do {
                g = new SimpleGraph<>(
                    SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER,
                    false);
                generator = new GnmRandomGraphGenerator<>(n, m);
                generator.generateGraph(g);

                inspector = new ConnectivityInspector<>(g);
            } while (!inspector.isConnected());

            // decompose graph
            CliqueMinimalSeparatorDecomposition<Integer, DefaultEdge> cmsd =
                new CliqueMinimalSeparatorDecomposition<>(g);

            // check triangulation
            assertEquals(
                cmsd.getMinimalTriangulation().edgeSet().size(),
                g.edgeSet().size() + cmsd.getFillEdges().size());

            // check seprators
            for (Set<Integer> separator : cmsd.getSeparators()) {
                assertTrue(separator.size() >= 1);
                assertTrue(isClique(g, separator));
            }
            assertTrue(cmsd.getSeparators().size() < cmsd.getAtoms().size());

            // check component count
            assertEquals(cmsd.getSeparators().size(), cmsd.getFullComponentCount().size());

            for (Set<Integer> separator : cmsd.getSeparators()) {
                assertTrue(cmsd.getFullComponentCount().get(separator) >= 2);
            }
        }
    }

    /**
     * Check whether the subgraph of <code>graph</code> induced by the given <code>vertices</code>
     * is complete, i.e. a clique.
     * 
     * @param graph the graph.
     * @param vertices the vertices to induce the subgraph from.
     * @return true if the induced subgraph is a clique.
     */
    private static <V, E> boolean isClique(Graph<V, E> graph, Set<V> vertices)
    {
        for (V v1 : vertices) {
            for (V v2 : vertices) {
                if (v1 != v2 && graph.getEdge(v1, v2) == null)
                    return false;
            }
        }
        return true;
    }

}
