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
        final UndirectedGraph<Object, DefaultEdge> completeGraph =
            new SimpleGraph<Object, DefaultEdge>(
                DefaultEdge.class);
        final CompleteGraphGenerator<Object, DefaultEdge> completeGenerator =
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
        final Map<Integer, Set<Object>> coloring =
            ChromaticNumber.findGreedyColoredGroups(completeGraph);
        assertEquals(
            7,
            coloring.keySet().size());

        final UndirectedGraph<Object, DefaultEdge> linearGraph =
            new SimpleGraph<Object, DefaultEdge>(
                DefaultEdge.class);
        final LinearGraphGenerator<Object, DefaultEdge> linearGenerator =
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
