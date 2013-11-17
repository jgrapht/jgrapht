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
 * HamiltonianCycleTest.java
 * ----------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Andrew Newell
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 17-Feb-2008 : Initial revision (AN);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import junit.framework.*;

import org.jgrapht.generate.*;
import org.jgrapht.graph.*;


/**
 * .
 *
 * @author Andrew Newell
 */
public class HamiltonianCycleTest
    extends TestCase
{
    //~ Methods ----------------------------------------------------------------

    // ~ Methods
    // ----------------------------------------------------------------

    /**
     * .
     */
    public void testHamiltonianCycle()
    {
        SimpleWeightedGraph<Object, DefaultWeightedEdge> completeGraph =
            new SimpleWeightedGraph<Object, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);
        CompleteGraphGenerator<Object, DefaultWeightedEdge> completeGraphGenerator =
            new CompleteGraphGenerator<Object, DefaultWeightedEdge>(
                6);
        completeGraphGenerator.generateGraph(
            completeGraph,
            new ClassBasedVertexFactory<Object>(Object.class),
            null);

        assertTrue(
            HamiltonianCycle.getApproximateOptimalForCompleteGraph(
                completeGraph).size() == 6);

        List<Object> vertices =
            new LinkedList<Object>(completeGraph.vertexSet());
        completeGraph.removeEdge(
            completeGraph.getEdge(vertices.get(0),
                vertices.get(1)));

        assertTrue(
            HamiltonianCycle.getApproximateOptimalForCompleteGraph(
                completeGraph) == null);
    }
}

// End HamiltonianCycleTest.java
