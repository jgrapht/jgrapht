/*
 * (C) Copyright 2008-2021, by Ilya Razenshteyn and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.alg.flow;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.fail;

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
            Map<DefaultWeightedEdge, Double> flow = solver.getMaximumFlow(0, 1).getFlowMap();
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
