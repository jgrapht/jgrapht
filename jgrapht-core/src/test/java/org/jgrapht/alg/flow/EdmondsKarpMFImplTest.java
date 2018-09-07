/*
 * (C) Copyright 2008-2018, by Ilya Razenshteyn and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht.alg.flow;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

public class EdmondsKarpMFImplTest
    extends
    MaximumFlowAlgorithmTest
{
    @Override
    MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> createSolver(
        Graph<Integer, DefaultWeightedEdge> network)
    {
        return new EdmondsKarpMFImpl<>(network);
    }

    // ~ Methods ----------------------------------------------------------------

    @Test
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
            new EdmondsKarpMFImpl<>(simple, -0.1);
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
                new EdmondsKarpMFImpl<>(simple);
            Map<DefaultWeightedEdge, Double> flow = solver.getMaximumFlow(0, 1).getFlow();
            flow.put(e, 25.0);
            fail();
        } catch (UnsupportedOperationException ex) {
        }
        try {
            MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> solver =
                new EdmondsKarpMFImpl<>(simple);
            solver.getMaximumFlow(2, 0);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> solver =
                new EdmondsKarpMFImpl<>(simple);
            solver.getMaximumFlow(1, 2);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> solver =
                new EdmondsKarpMFImpl<>(simple);
            solver.getMaximumFlow(0, 0);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> solver =
                new EdmondsKarpMFImpl<>(simple);
            solver.getMaximumFlow(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> solver =
                new EdmondsKarpMFImpl<>(simple);
            solver.getMaximumFlow(0, null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }
}

// End EdmondsKarpMFImplTest.java
