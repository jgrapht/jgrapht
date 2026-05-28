/*
 * (C) Copyright 2026-2026, by Shai Eilat and Contributors.
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
package org.jgrapht.osm;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link HaversineHeuristic}.
 *
 * @author Shai Eilat
 */
class HaversineHeuristicTest
{

    @Test
    void zeroDistanceForIdenticalPoints()
    {
        Map<Integer, double[]> coords = Map.of(0, new double[] { 40.0, -74.0 });
        HaversineHeuristic<Integer> h = HaversineHeuristic.ofMap(coords);
        assertEquals(0.0, h.getCostEstimate(0, 0), 1e-9);
    }

    @Test
    void symmetric()
    {
        Map<Integer, double[]> coords = Map.of(
            0, new double[] { 42.5, 1.5 },
            1, new double[] { 42.51, 1.51 });
        HaversineHeuristic<Integer> h = HaversineHeuristic.ofMap(coords);
        assertEquals(h.getCostEstimate(0, 1), h.getCostEstimate(1, 0), 1e-9);
    }

    @Test
    void newYorkToLondonReferenceDistance()
    {
        Map<Integer, double[]> coords = Map.of(
            0, new double[] { 40.7128, -74.0060 }, // New York
            1, new double[] { 51.5074, -0.1278 }); // London
        HaversineHeuristic<Integer> h = HaversineHeuristic.ofMap(coords);
        double meters = h.getCostEstimate(0, 1);
        assertTrue(
            Math.abs(meters - 5_570_000) < 30_000,
            "expected ~5,570 km, got " + meters + " m");
    }

    @Test
    void honoursCustomRadius()
    {
        Map<Integer, double[]> coords = Map.of(
            0, new double[] { 0.0, 0.0 },
            1, new double[] { 0.0, 90.0 });
        HaversineHeuristic<Integer> earth = new HaversineHeuristic<>(coords::get);
        HaversineHeuristic<Integer> mars =
            new HaversineHeuristic<>(coords::get, 3_389_500.0);

        assertEquals(
            Math.PI / 2 * HaversineHeuristic.EARTH_RADIUS_M,
            earth.getCostEstimate(0, 1),
            1e-3);
        assertEquals(
            Math.PI / 2 * 3_389_500.0, mars.getCostEstimate(0, 1), 1e-3);
    }

    @Test
    void returnsZeroWhenCoordinateUnknown()
    {
        Map<Integer, double[]> coords = Map.of(0, new double[] { 0.0, 0.0 });
        HaversineHeuristic<Integer> h = HaversineHeuristic.ofMap(coords);
        assertEquals(0.0, h.getCostEstimate(0, 99), 0.0);
        assertEquals(0.0, h.getCostEstimate(99, 0), 0.0);
    }

    @Test
    void nullCoordinatesRejected()
    {
        assertThrows(NullPointerException.class, () -> new HaversineHeuristic<Integer>(null));
    }

    @Test
    void nonPositiveRadiusRejected()
    {
        Map<Integer, double[]> coords = Map.of(0, new double[] { 0.0, 0.0 });
        assertThrows(
            IllegalArgumentException.class,
            () -> new HaversineHeuristic<>(coords::get, 0.0));
        assertThrows(
            IllegalArgumentException.class,
            () -> new HaversineHeuristic<>(coords::get, -1.0));
    }
}
