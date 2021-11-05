/*
 * (C) Copyright 2018-2021, by Kirill Vishnyakov and Contributors.
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
package org.jgrapht.demo;

import org.jgrapht.alg.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WarnsdorffRuleKnightTourHeuristicTest
{

    private KnightTour container;
    private WarnsdorffRuleKnightTourHeuristic solver;

    @Before
    public void setup()
    {
        container = new KnightTour();
    }

    private boolean checkClosed(Pair<Integer, Integer> start, Pair<Integer, Integer> end)
    {
        return Math.abs(start.getFirst() - end.getFirst()) == 1
            && Math.abs(start.getSecond() - end.getSecond()) == 2
            || Math.abs(start.getFirst() - end.getFirst()) == 2
                && Math.abs(start.getSecond() - end.getSecond()) == 1;
    }

    private boolean checkOpen(Pair<Integer, Integer> start, Pair<Integer, Integer> end)
    {
        return !checkClosed(start, end);
    }

    private boolean checkMoveCorrectness(
        List<Pair<Integer, Integer>> tour, int n, int m, int shiftX, int shiftY)
    {
        boolean[][] used = new boolean[shiftX + n][shiftY + m];
        used[tour.get(0).getFirst()][tour.get(0).getSecond()] = true;

        for (int i = 1; i < tour.size(); i++) {
            if (!((Math.abs(tour.get(i - 1).getFirst() - tour.get(i).getFirst()) == 1
                && Math.abs(tour.get(i - 1).getSecond() - tour.get(i).getSecond()) == 2)
                || Math.abs(tour.get(i - 1).getFirst() - tour.get(i).getFirst()) == 2
                    && Math.abs(tour.get(i - 1).getSecond() - tour.get(i).getSecond()) == 1))
            {
                return false;
            }

            assertTrue(tour.get(i).getFirst() >= shiftX);
            assertTrue(tour.get(i).getFirst() < shiftX + n);
            assertTrue(tour.get(i).getSecond() >= shiftY);
            assertTrue(tour.get(i).getSecond() < shiftY + m);

            if (used[tour.get(i).getFirst()][tour.get(i).getSecond()]) {
                return false;
            }

            used[tour.get(i).getFirst()][tour.get(i).getSecond()] = true;
        }

        return true;
    }

    private boolean checkStructured(
        List<Pair<Integer, Integer>> tour, boolean structured, int n, int m, int shiftX, int shiftY)
    {
        HashSet<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> moves = new HashSet<>();

        for (int i = 1; i < tour.size(); i++) {
            moves.add(new Pair<>(tour.get(i - 1), tour.get(i)));
        }

        return !structured || ((moves
            .contains(new Pair<>(new Pair<>(1 + shiftX, shiftY), new Pair<>(shiftX, 2 + shiftY)))
            || moves
                .contains(
                    new Pair<>(new Pair<>(shiftX, 2 + shiftY), new Pair<>(1 + shiftX, shiftY))))
            && moves
                .contains(
                    new Pair<>(new Pair<>(2 + shiftX, shiftY), new Pair<>(shiftX, 1 + shiftY)))
            || moves
                .contains(
                    new Pair<>(new Pair<>(shiftX, 1 + shiftY), new Pair<>(2 + shiftX, shiftY)))

                &&

                moves
                    .contains(
                        new Pair<>(
                            new Pair<>(n - 3 + shiftX, shiftY),
                            new Pair<>(n - 1 + shiftX, 1 + shiftY)))
            || moves
                .contains(
                    new Pair<>(
                        new Pair<>(n - 1 + shiftX, 1 + shiftY), new Pair<>(n - 3 + shiftX, shiftY)))
                && moves
                    .contains(
                        new Pair<>(
                            new Pair<>(n - 2 + shiftX, shiftY),
                            new Pair<>(n - 1 + shiftX, 2 + shiftY)))
            || moves
                .contains(
                    new Pair<>(
                        new Pair<>(n - 1 + shiftX, 2 + shiftY), new Pair<>(n - 2 + shiftX, shiftY)))

                &&

                moves
                    .contains(
                        new Pair<>(
                            new Pair<>(shiftX, m - 3 + shiftY),
                            new Pair<>(1 + shiftX, m - 1 + shiftY)))
            || moves
                .contains(
                    new Pair<>(
                        new Pair<>(1 + shiftX, m - 1 + shiftY), new Pair<>(shiftX, m - 3 + shiftY)))
                && moves
                    .contains(
                        new Pair<>(
                            new Pair<>(shiftX, m - 2 + shiftY),
                            new Pair<>(2 + shiftX, m - 1 + shiftY)))
            || moves
                .contains(
                    new Pair<>(
                        new Pair<>(2 + shiftX, m - 1 + shiftY), new Pair<>(shiftX, m - 2 + shiftY)))

                &&

                moves
                    .contains(
                        new Pair<>(
                            new Pair<>(n - 3 + shiftX, m - 1 + shiftY),
                            new Pair<>(n - 1 + shiftX, m - 2 + shiftY)))
            || moves
                .contains(
                    new Pair<>(
                        new Pair<>(n - 1 + shiftX, m - 2 + shiftY),
                        new Pair<>(n - 3 + shiftX, m - 1 + shiftY)))
                && moves
                    .contains(
                        new Pair<>(
                            new Pair<>(n - 2 + shiftX, m - 1 + shiftY),
                            new Pair<>(n - 1 + shiftX, m - 3 + shiftY)))
            || moves
                .contains(
                    new Pair<>(
                        new Pair<>(n - 1 + shiftX, m - 3 + shiftY),
                        new Pair<>(n - 2 + shiftX, n - 2 + shiftY))));
    }

    private void checkCorrectness(
        List<Pair<Integer, Integer>> tour, TourType type, int n, int m, int shiftX, int shiftY,
        boolean structured)
    {
        if (type == TourType.CLOSED) {
            assertTrue(checkClosed(tour.get(0), tour.get(tour.size() - 1)));
        } else {
            assertTrue(checkOpen(tour.get(0), tour.get(tour.size() - 1)));
        }
        assertTrue(checkStructured(tour, structured, n, m, shiftX, shiftY));
        assertEquals(n * m, tour.size());
        assertTrue(checkMoveCorrectness(tour, n, m, shiftX, shiftY));
    }

    @Test
    public void warnsdorff8x8OpenWithShift()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(8);
        container = solver.getTour(TourType.OPEN, false, 10, 143);
        checkCorrectness(container.toList(), TourType.OPEN, 8, 8, 10, 143, false);
    }

    @Test
    public void warnsdorff10x10Open()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(10);
        container = solver.getTour(TourType.OPEN, false, 0, 0);
        checkCorrectness(container.toList(), TourType.OPEN, 10, 10, 0, 0, false);
    }

    @Test
    public void warnsdorff37x10Open()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(37, 10);
        container = solver.getTour(TourType.OPEN, false, 0, 0);
        checkCorrectness(container.toList(), TourType.OPEN, 37, 10, 0, 0, false);
    }

    @Test
    public void warnsdorff41x41Open()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(41, 41);
        container = solver.getTour(TourType.OPEN, false, 0, 0);
        checkCorrectness(container.toList(), TourType.OPEN, 41, 41, 0, 0, false);
    }

    @Test
    public void warnsdorff41x41OpenStructured()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(41, 41);
        container = solver.getTour(TourType.OPEN, true, 0, 0);
        checkCorrectness(container.toList(), TourType.OPEN, 41, 41, 0, 0, true);
    }

    @Test
    public void warnsdorff10x10Closed()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(10);
        container = solver.getTour(TourType.CLOSED, false, 0, 0);
        checkCorrectness(container.toList(), TourType.CLOSED, 10, 10, 0, 0, false);
    }

    @Test
    public void warnsdorff34x34ClosedWithShift()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(34);
        container = solver.getTour(TourType.CLOSED, false, 20, 89);
        checkCorrectness(container.toList(), TourType.CLOSED, 34, 34, 20, 89, false);
    }

    @Test
    public void warnsdorff63x23OpenStructured()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(63, 23);
        container = solver.getTour(TourType.OPEN, true, 0, 0);
        checkCorrectness(container.toList(), TourType.OPEN, 63, 23, 0, 0, true);
    }

    @Test
    public void warnsdorff49x34Closed()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(49, 34);
        container = solver.getTour(TourType.CLOSED, false, 0, 0);
        checkCorrectness(container.toList(), TourType.CLOSED, 49, 34, 0, 0, false);
    }

    @Test
    public void warnsdorff34x34ClosedStructuredWithShift()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(34);
        container = solver.getTour(TourType.CLOSED, true, 20, 89);
        checkCorrectness(container.toList(), TourType.CLOSED, 34, 34, 20, 89, true);
    }

    @Test
    public void warnsdorff18x34ClosedStructuredWithShift()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(18, 34);
        container = solver.getTour(TourType.CLOSED, true, 7, 7);
        checkCorrectness(container.toList(), TourType.CLOSED, 18, 34, 7, 7, true);
    }

    @Test
    public void warnsdorff42x42ClosedStructured()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(42, 42);
        container = solver.getTour(TourType.CLOSED, true, 0, 0);
        checkCorrectness(container.toList(), TourType.CLOSED, 42, 42, 0, 0, true);
    }

    @Test
    public void warnsdorff40x20OpenStructured()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(40, 20);
        container = solver.getTour(TourType.OPEN, true, 0, 0);
        checkCorrectness(container.toList(), TourType.OPEN, 40, 20, 0, 0, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void warnsdorffIncorrectConfigurationOpen()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(2, 200);
        container = solver.getTour(TourType.OPEN, false, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void warnsdorffIncorrectConfigurationOpen3x5()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(5, 3);
        container = solver.getTour(TourType.OPEN, false, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void warnsdorffIncorrectConfigurationOpen3x6()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(3, 6);
        container = solver.getTour(TourType.OPEN, false, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void warnsdorffIncorrectConfigurationOpen4x4()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(4, 4);
        container = solver.getTour(TourType.OPEN, false, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void warnsdorffIncorrectTourConfigurationClosedBothOdd()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(31, 31);
        container = solver.getTour(TourType.CLOSED, false, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void warnsdorffIncorrectTourConfigurationClosed4x6()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(4, 6);
        container = solver.getTour(TourType.CLOSED, false, 0, 0);
    }

    @Test
    public void warnsdorffOpenNonStructured4x6()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(4, 6);
        container = solver.getTour(TourType.OPEN, false, 0, 0);
        checkCorrectness(container.toList(), TourType.OPEN, 4, 6, 0, 0, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void warnsdorffIncorrectBoardConfiguration0x100()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(0, 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void warnsdorffIncorrectBoardConfigurationBothNegative()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(-132);
    }

    @Test(expected = IllegalArgumentException.class)
    public void warnsdorffIncorrectBoardConfigurationBothNegative2()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(-132, -140);
    }

    @Test(expected = IllegalArgumentException.class)
    public void warnsdorffIncorrectBoardConfigurationOneDimSmall()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(1, 10);
    }

    @Test
    public void warnsdorffDoubleInvocationToArrayList()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(49, 34);
        container = solver.getTour(TourType.CLOSED, false, 0, 0);
        List<Pair<Integer, Integer>> arr1 = container.toList();
        List<Pair<Integer, Integer>> arr2 = container.toList();
        assertEquals(arr1.size(), arr2.size());
    }

    @Test
    public void warnsdorffDoubleInvocationToArrayListAndgetTour()
    {
        solver = new WarnsdorffRuleKnightTourHeuristic(40, 40);
        container = solver.getTour(TourType.CLOSED, false, 0, 0);
        List<Pair<Integer, Integer>> arr1 = container.toList();
        List<Pair<Integer, Integer>> arr2 = container.toList();
        assertEquals(arr1.size(), arr2.size());
        container = solver.getTour(TourType.OPEN, true, 10, 10);
        arr1 = container.toList();
        arr2 = container.toList();
        assertEquals(arr1.size(), arr2.size());
    }
}
