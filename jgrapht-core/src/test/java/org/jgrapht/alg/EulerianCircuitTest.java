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

import junit.framework.TestCase;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.graph.ClassBasedVertexFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;


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
        final UndirectedGraph<Object, DefaultEdge> completeGraph1 =
            new SimpleGraph<Object, DefaultEdge>(
                DefaultEdge.class);
        final CompleteGraphGenerator<Object, DefaultEdge> completeGenerator1 =
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

        final UndirectedGraph<Object, DefaultEdge> completeGraph2 =
            new SimpleGraph<Object, DefaultEdge>(
                DefaultEdge.class);
        final CompleteGraphGenerator<Object, DefaultEdge> completeGenerator2 =
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
