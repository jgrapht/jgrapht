/*
 * (C) Copyright 2020-2021, by Timofey Chudakov and Contributors.
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
package org.jgrapht.generate.netgen;

import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Tests for {@link Distributor}.
 *
 * @author Timofey Chudakov
 */
public class DistributorTest
{
    private static final long SEED = 1;

    private final Random rng = new Random(SEED);

    @Test
    public void testDistributor_NoUpperBounds_OneValidDistribution()
    {
        Distributor<String> distributor = new Distributor<>(rng);
        distributor.addLowerBound(element -> {
            switch (element) {
            case "a":
                return 3;
            case "b":
                return 4;
            case "c":
                return 2;
            default:
                return 0;
            }
        });
        List<Integer> distribution = distributor.getDistribution(List.of("a", "b", "c"), 9);

        assertEquals(List.of(3, 4, 2), distribution);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDistributor_NoUpperBounds_NoValidDistribution()
    {
        Distributor<Integer> distributor = new Distributor<>(rng);
        distributor.addLowerBound(element -> {
            switch (element) {
            case 1:
                return 5;
            case 2:
                return 6;
            case 3:
                return 2;
            default:
                return 0;
            }
        });
        List<Integer> distribution = distributor.getDistribution(List.of(1, 2, 3), 12);
    }

    @Test
    public void testDistributor_NoLowerBounds_OneValidDistribution()
    {
        Distributor<Integer> distributor = new Distributor<>(rng);
        distributor.addLowerBound(element -> {
            switch (element) {
            case 1:
                return 3;
            case 2:
                return 5;
            case 3:
                return 2;
            default:
                return 0;
            }
        });

        List<Integer> distribution = distributor.getDistribution(List.of(1, 2, 3), 10);

        assertEquals(List.of(3, 5, 2), distribution);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDistributor_NoLowerBounds_NoValidDistribution()
    {
        Distributor<Integer> distributor = new Distributor<>(rng);
        distributor.addLowerBound(element -> {
            switch (element) {
            case 1:
                return 3;
            case 2:
                return 4;
            case 3:
                return 2;
            default:
                return 0;
            }
        });

        List<Integer> distribution = distributor.getDistribution(List.of(1, 2, 3), 8);
    }

    @Test
    public void testDistributor_AllBounds1()
    {
        Distributor<Integer> distributor = new Distributor<>(rng);
        distributor.addLowerBound(element -> 5);
        distributor.addUpperBound(element -> 10);

        int elementNum = 10;
        int valueNum = 5 * elementNum;
        List<Integer> dist = distributor
            .getDistribution(
                IntStream.range(0, elementNum).boxed().collect(Collectors.toList()), valueNum);

        int sum = dist.stream().mapToInt(i -> i).sum();
        assertEquals(sum, valueNum);

        for (int assignedValues : dist) {
            assertEquals(5, assignedValues);
        }
    }

    @Test
    public void testDistributor_AllBounds2()
    {
        Distributor<Integer> distributor = new Distributor<>(rng);
        distributor.addLowerBound(element -> 5);
        distributor.addUpperBound(element -> 10);

        int elementNum = 10;
        int valueNum = 10 * elementNum;
        List<Integer> dist = distributor
            .getDistribution(
                IntStream.range(0, elementNum).boxed().collect(Collectors.toList()), valueNum);

        int sum = dist.stream().mapToInt(i -> i).sum();
        assertEquals(sum, valueNum);

        for (int assignedValues : dist) {
            assertEquals(10, assignedValues);
        }
    }

    @Test
    public void testDistributor_AllBounds3()
    {
        Distributor<Integer> distributor = new Distributor<>(rng);
        distributor.addLowerBound(element -> 5);
        distributor.addUpperBound(element -> 10);

        int elementNum = 10;
        int valueNum = 8 * elementNum;
        List<Integer> dist = distributor
            .getDistribution(
                IntStream.range(0, elementNum).boxed().collect(Collectors.toList()), valueNum);

        int sum = dist.stream().mapToInt(i -> i).sum();
        assertEquals(sum, valueNum);

        for (int assignedValues : dist) {
            assertTrue(assignedValues >= 5);
            assertTrue(assignedValues <= 10);
        }
    }

    @Test
    public void testDistributor_AllBounds_LargeBounds()
    {
        Distributor<Integer> distributor = new Distributor<>(rng);
        int lb = 1000 * 1000;
        int ub = 2 * 1000 * 1000;
        distributor.addLowerBound(element -> lb);
        distributor.addUpperBound(element -> ub);

        int elementNum = 1000;
        int valueNum = ((lb + ub) / 2) * elementNum;
        List<Integer> dist = distributor
            .getDistribution(
                IntStream.range(0, elementNum).boxed().collect(Collectors.toList()), valueNum);

        int sum = dist.stream().mapToInt(i -> i).sum();
        assertEquals(sum, valueNum);

        for (int assignedValues : dist) {
            assertTrue(assignedValues >= lb);
            assertTrue(assignedValues <= ub);
        }
    }
}
