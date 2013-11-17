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
/* ----------------
 * ColoringTest.java
 * ----------------
 * (C) Copyright 2010, by Michael Behrisch and Contributors.
 *
 * Original Author:  Michael Behrisch
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 17-Feb-2008 : Initial revision (MB);
 *
 */
package org.jgrapht.experimental.alg;

import junit.framework.*;

import org.jgrapht.*;
import org.jgrapht.experimental.alg.color.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;


/**
 * .
 *
 * @author Michael Behrisch
 */
public class ColoringTest
    extends TestCase
{
    //~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    public void testGreedyColoring()
    {
        Graph<Object, DefaultEdge> completeGraph =
            new SimpleGraph<Object, DefaultEdge>(
                DefaultEdge.class);
        CompleteGraphGenerator<Object, DefaultEdge> completeGraphGenerator =
            new CompleteGraphGenerator<Object, DefaultEdge>(
                6);
        completeGraphGenerator.generateGraph(
            completeGraph,
            new ClassBasedVertexFactory<Object>(Object.class),
            null);
        GreedyColoring<Object, DefaultEdge> colorer =
            new GreedyColoring<Object, DefaultEdge>(completeGraph);
        assertEquals(new Integer(6), colorer.getUpperBound(null));
    }

    /**
     * .
     */
    public void testBacktrackColoring()
    {
        Graph<Object, DefaultEdge> completeGraph =
            new SimpleGraph<Object, DefaultEdge>(
                DefaultEdge.class);
        CompleteGraphGenerator<Object, DefaultEdge> completeGraphGenerator =
            new CompleteGraphGenerator<Object, DefaultEdge>(
                6);
        completeGraphGenerator.generateGraph(
            completeGraph,
            new ClassBasedVertexFactory<Object>(Object.class),
            null);
        BrownBacktrackColoring<Object, DefaultEdge> colorer =
            new BrownBacktrackColoring<Object, DefaultEdge>(completeGraph);
        assertEquals(new Integer(6), colorer.getResult(null));
    }
}

// End ColoringTest.java
