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

import junit.framework.TestCase;
import org.jgrapht.Graph;
import org.jgrapht.experimental.alg.color.BrownBacktrackColoring;
import org.jgrapht.experimental.alg.color.GreedyColoring;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.graph.ClassBasedVertexFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;


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
        final Graph<Object, DefaultEdge> completeGraph =
            new SimpleGraph<Object, DefaultEdge>(
                DefaultEdge.class);
        final CompleteGraphGenerator<Object, DefaultEdge> completeGraphGenerator =
            new CompleteGraphGenerator<Object, DefaultEdge>(
                6);
        completeGraphGenerator.generateGraph(
            completeGraph,
            new ClassBasedVertexFactory<Object>(Object.class),
            null);
        final GreedyColoring<Object, DefaultEdge> colorer =
            new GreedyColoring<Object, DefaultEdge>(completeGraph);
        assertEquals(new Integer(6), colorer.getUpperBound(null));
    }

    /**
     * .
     */
    public void testBacktrackColoring()
    {
        final Graph<Object, DefaultEdge> completeGraph =
            new SimpleGraph<Object, DefaultEdge>(
                DefaultEdge.class);
        final CompleteGraphGenerator<Object, DefaultEdge> completeGraphGenerator =
            new CompleteGraphGenerator<Object, DefaultEdge>(
                6);
        completeGraphGenerator.generateGraph(
            completeGraph,
            new ClassBasedVertexFactory<Object>(Object.class),
            null);
        final BrownBacktrackColoring<Object, DefaultEdge> colorer =
            new BrownBacktrackColoring<Object, DefaultEdge>(completeGraph);
        assertEquals(new Integer(6), colorer.getResult(null));
    }
}

// End ColoringTest.java
