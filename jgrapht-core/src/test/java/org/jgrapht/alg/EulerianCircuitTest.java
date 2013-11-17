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
 * EulerianCircuitTest.java
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

import junit.framework.*;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;


/**
 * .
 *
 * @author Andrew Newell
 */
public class EulerianCircuitTest
    extends TestCase
{
    //~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    public void testEulerianCircuit()
    {
        UndirectedGraph<Object, DefaultEdge> completeGraph1 =
            new SimpleGraph<Object, DefaultEdge>(
                DefaultEdge.class);
        CompleteGraphGenerator<Object, DefaultEdge> completeGenerator1 =
            new CompleteGraphGenerator<Object, DefaultEdge>(
                6);
        completeGenerator1.generateGraph(
            completeGraph1,
            new ClassBasedVertexFactory<Object>(Object.class),
            null);

        // A complete graph of order 6 will have all vertices with degree 5
        // which is odd, therefore this graph is not Eulerian
        assertFalse(EulerianCircuit.isEulerian(completeGraph1));
        assertTrue(
            EulerianCircuit.getEulerianCircuitVertices(completeGraph1) == null);

        UndirectedGraph<Object, DefaultEdge> completeGraph2 =
            new SimpleGraph<Object, DefaultEdge>(
                DefaultEdge.class);
        CompleteGraphGenerator<Object, DefaultEdge> completeGenerator2 =
            new CompleteGraphGenerator<Object, DefaultEdge>(
                5);
        completeGenerator2.generateGraph(
            completeGraph2,
            new ClassBasedVertexFactory<Object>(Object.class),
            null);
        assertTrue(EulerianCircuit.isEulerian(completeGraph2));

        // There are 10 edges total in this graph, so an Eulerian circuit
        // labeled by vertices should have 11 vertices
        assertEquals(
            11,
            EulerianCircuit.getEulerianCircuitVertices(completeGraph2).size());
    }
}

// End EulerianCircuitTest.java
