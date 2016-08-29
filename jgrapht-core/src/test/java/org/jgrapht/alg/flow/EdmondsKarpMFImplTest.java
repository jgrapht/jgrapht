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
/* -----------------
 * EdmondsKarpMFImplTest.java
 * -----------------
 * (C) Copyright 2008-2008, by Ilya Razenshteyn and Contributors.
 *
 * Original Author:  Ilya Razenshteyn
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 */
package org.jgrapht.alg.flow;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.Map;


public class EdmondsKarpMFImplTest extends MaximumFlowAlgorithmTest
{
    @Override
    MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> createSolver(Graph<Integer, DefaultWeightedEdge> network) {
        return new EdmondsKarpMFImpl<>(network);
    }

    //~ Methods ----------------------------------------------------------------


    public void testCornerCases()
    {
        DirectedWeightedMultigraph<Integer, DefaultWeightedEdge> simple =
            new DirectedWeightedMultigraph<>(DefaultWeightedEdge.class);
        simple.addVertex(0);
        simple.addVertex(1);
        DefaultWeightedEdge e = simple.addEdge(0, 1);
        try {
            new EdmondsKarpMFImpl<Integer, DefaultWeightedEdge>(null);
            fail();
        } catch (NullPointerException ex) {
        }
        try {
            new EdmondsKarpMFImpl<>(
                simple,
                -0.1);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            simple.setEdgeWeight(e, -1.0);
            new EdmondsKarpMFImpl<>(simple);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            simple.setEdgeWeight(e, 1.0);
            MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> solver =
                new EdmondsKarpMFImpl<>(
                    simple);
            Map<DefaultWeightedEdge, Double> flow = solver.buildMaximumFlow(0, 1).getFlow();
            flow.put(e, 25.0);
            fail();
        } catch (UnsupportedOperationException ex) {
        }
        try {
            MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> solver =
                new EdmondsKarpMFImpl<>(
                    simple);
            solver.buildMaximumFlow(2, 0);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> solver =
                    new EdmondsKarpMFImpl<>(
                            simple);
            solver.buildMaximumFlow(1, 2);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> solver =
                    new EdmondsKarpMFImpl<>(
                            simple);
            solver.buildMaximumFlow(0, 0);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> solver =
                    new EdmondsKarpMFImpl<>(
                            simple);
            solver.buildMaximumFlow(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> solver =
                    new EdmondsKarpMFImpl<>(
                            simple);
            solver.buildMaximumFlow(0, null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }
}

// End EdmondsKarpMFImplTest.java
