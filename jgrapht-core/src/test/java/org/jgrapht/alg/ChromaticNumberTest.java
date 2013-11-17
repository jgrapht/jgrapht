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
 * ChromaticNumberTest.java
 * -------------------
 * (C) Copyright 2008-2008, by Andrew Newell and Contributors.
 *
 * Original Author:  Andrew Newell
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Dec-2008 : Initial revision (AN);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import junit.framework.*;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;


/**
 * .
 *
 * @author Andrew Newell
 */
public class ChromaticNumberTest
    extends TestCase
{
    //~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    public void testChromaticNumber()
    {
        UndirectedGraph<Object, DefaultEdge> completeGraph =
            new SimpleGraph<Object, DefaultEdge>(
                DefaultEdge.class);
        CompleteGraphGenerator<Object, DefaultEdge> completeGenerator =
            new CompleteGraphGenerator<Object, DefaultEdge>(
                7);
        completeGenerator.generateGraph(
            completeGraph,
            new ClassBasedVertexFactory<Object>(Object.class),
            null);

        // A complete graph has a chromatic number equal to its order
        assertEquals(
            7,
            ChromaticNumber.findGreedyChromaticNumber(completeGraph));
        Map<Integer, Set<Object>> coloring =
            ChromaticNumber.findGreedyColoredGroups(completeGraph);
        assertEquals(
            7,
            coloring.keySet().size());

        UndirectedGraph<Object, DefaultEdge> linearGraph =
            new SimpleGraph<Object, DefaultEdge>(
                DefaultEdge.class);
        LinearGraphGenerator<Object, DefaultEdge> linearGenerator =
            new LinearGraphGenerator<Object, DefaultEdge>(
                50);
        linearGenerator.generateGraph(
            linearGraph,
            new ClassBasedVertexFactory<Object>(Object.class),
            null);

        // A linear graph is a tree, and a greedy algorithm for chromatic number
        // can always find a 2-coloring
        assertEquals(2, ChromaticNumber.findGreedyChromaticNumber(linearGraph));
    }
}

// End ChromaticNumberTest.java
