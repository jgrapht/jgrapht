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
/* ------------------------------
 * BronKerboschCliqueFinderTest.java
 * ------------------------------
 * (C) Copyright 2005-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 26-July-2005 : Initial revision (JVS);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import junit.framework.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;


/**
 * .
 *
 * @author John V. Sichi
 */
public class BronKerboschCliqueFinderTest
    extends TestCase
{
    //~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";
    private static final String V4 = "v4";
    private static final String V5 = "v5";
    private static final String V6 = "v6";
    private static final String V7 = "v7";
    private static final String V8 = "v8";

    //~ Methods ----------------------------------------------------------------

    /**
     * .
     *
     * @param g
     */
    public void createGraph(Graph<String, DefaultEdge> g)
    {
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);
        g.addVertex(V5);
        g.addVertex(V6);
        g.addVertex(V7);
        g.addVertex(V8);

        // biggest clique:  { V1, V2, V3, V4 }
        g.addEdge(V1, V2);
        g.addEdge(V1, V3);
        g.addEdge(V1, V4);
        g.addEdge(V2, V3);
        g.addEdge(V2, V4);
        g.addEdge(V3, V4);

        // smaller clique:  { V5, V6, V7 }
        g.addEdge(V5, V6);
        g.addEdge(V5, V7);
        g.addEdge(V6, V7);

        // for fun, add an overlapping clique { V3, V4, V5 }
        g.addEdge(V3, V5);
        g.addEdge(V4, V5);

        // make V8 less lonely
        g.addEdge(V7, V8);
    }

    public void testFindBiggest()
    {
        SimpleGraph<String, DefaultEdge> g =
            new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        createGraph(g);

        BronKerboschCliqueFinder<String, DefaultEdge> finder =
            new BronKerboschCliqueFinder<String, DefaultEdge>(g);

        Collection<Set<String>> cliques = finder.getBiggestMaximalCliques();

        assertEquals(1, cliques.size());

        Set<String> expected = new HashSet<String>();
        expected.add(V1);
        expected.add(V2);
        expected.add(V3);
        expected.add(V4);

        Set<String> actual = cliques.iterator().next();

        assertEquals(expected, actual);
    }

    public void testFindAll()
    {
        SimpleGraph<String, DefaultEdge> g =
            new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        createGraph(g);

        BronKerboschCliqueFinder<String, DefaultEdge> finder =
            new BronKerboschCliqueFinder<String, DefaultEdge>(g);

        Collection<Set<String>> cliques = finder.getAllMaximalCliques();

        assertEquals(4, cliques.size());

        Set<Set<String>> expected = new HashSet<Set<String>>();

        Set<String> set = new HashSet<String>();
        set.add(V1);
        set.add(V2);
        set.add(V3);
        set.add(V4);
        expected.add(set);

        set = new HashSet<String>();
        set.add(V5);
        set.add(V6);
        set.add(V7);
        expected.add(set);

        set = new HashSet<String>();
        set.add(V3);
        set.add(V4);
        set.add(V5);
        expected.add(set);

        set = new HashSet<String>();
        set.add(V7);
        set.add(V8);
        expected.add(set);

        // convert result from Collection to Set because we don't want
        // order to be significant
        Set<Set<String>> actual = new HashSet<Set<String>>(cliques);

        assertEquals(expected, actual);
    }
}

// End BronKerboschCliqueFinderTest.java
