/*
 * (C) Copyright 2018-2018, by Kirill Vishnyakov and Contributors.
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

package org.jgrapht.alg.tour;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WarnsdorffRuleTest {

    private KnightTourContainer container;
    private WarnsdorffRule solver;

    @Before
    public void setup() {
        container = new KnightTourContainer();
    }

    private boolean checkClosed(ChessBoardCell<Integer, Integer> start, ChessBoardCell<Integer, Integer> end) {
        return Math.abs(start.first - end.first) == 1 && Math.abs(start.second - end.second) == 2
                || Math.abs(start.first - end.first) == 2 && Math.abs(start.second - end.second) == 1;
    }

    private boolean checkOpen(ChessBoardCell<Integer, Integer> start, ChessBoardCell<Integer, Integer> end) {
        return !checkClosed(start, end);
    }

    private boolean checkMoveCorrectness(ArrayList<ChessBoardCell<Integer, Integer>> tour, int n, int m, int shiftX, int shiftY) {
        boolean[][] used = new boolean[shiftX + n][shiftY + m];
        used[tour.get(0).first][tour.get(0).second] = true;

        for (int i = 1; i < tour.size(); i++) {
            if (!((Math.abs(tour.get(i - 1).first - tour.get(i).first) == 1 && Math.abs(tour.get(i - 1).second - tour.get(i).second) == 2)
                ||
                Math.abs(tour.get(i - 1).first - tour.get(i).first) == 2 && Math.abs(tour.get(i - 1).second - tour.get(i).second) == 1)) {
                return false;
            }

            assertTrue(tour.get(i).first >= shiftX);
            assertTrue(tour.get(i).first < shiftX + n);
            assertTrue(tour.get(i).second >= shiftY);
            assertTrue(tour.get(i).second < shiftY + m);

            if (used[tour.get(i).first][tour.get(i).second]) {
                return false;
            }

            used[tour.get(i).first][tour.get(i).second] = true;
        }

        return true;
    }

    private boolean checkStructured(ArrayList<ChessBoardCell<Integer, Integer>> tour, boolean structured, int n, int m, int shiftX, int shiftY) {
        HashSet<Move> moves = new HashSet<>();

        for(int i = 1; i < tour.size(); i++) {
            moves.add(new Move(tour.get(i - 1), tour.get(i)));
        }

        return !structured || (
            (moves.contains(new Move(new ChessBoardCell<>(1 + shiftX, shiftY), new ChessBoardCell<>(shiftX, 2 + shiftY)))
                || moves.contains(new Move(new ChessBoardCell<>(shiftX, 2 + shiftY), new ChessBoardCell<>(1 + shiftX, shiftY))))
                &&
                moves.contains(new Move(new ChessBoardCell<>(2 + shiftX, shiftY), new ChessBoardCell<>(shiftX, 1 + shiftY)))
                || moves.contains(new Move(new ChessBoardCell<>(shiftX, 1 + shiftY), new ChessBoardCell<>(2 + shiftX, shiftY)))

                &&

                moves.contains(new Move(new ChessBoardCell<>(n - 3 + shiftX, shiftY), new ChessBoardCell<>(n - 1 + shiftX, 1 + shiftY)))
                || moves.contains(new Move(new ChessBoardCell<>(n - 1 + shiftX, 1 + shiftY), new ChessBoardCell<>(n - 3 + shiftX, shiftY)))
                &&
                moves.contains(new Move(new ChessBoardCell<>(n - 2 + shiftX, shiftY), new ChessBoardCell<>(n - 1 + shiftX, 2 + shiftY)))
                || moves.contains(new Move(new ChessBoardCell<>(n - 1 + shiftX, 2 + shiftY), new ChessBoardCell<>(n - 2 + shiftX, shiftY)))

                &&

                moves.contains(new Move(new ChessBoardCell<>(shiftX, m - 3 + shiftY), new ChessBoardCell<>(1 + shiftX, m - 1 + shiftY)))
                || moves.contains(new Move(new ChessBoardCell<>(1 + shiftX, m - 1 + shiftY), new ChessBoardCell<>(shiftX, m - 3 + shiftY)))
                &&
                moves.contains(new Move(new ChessBoardCell<>(shiftX, m - 2 + shiftY), new ChessBoardCell<>(2 + shiftX, m - 1 + shiftY)))
                || moves.contains(new Move(new ChessBoardCell<>(2 + shiftX, m - 1 + shiftY), new ChessBoardCell<>(shiftX, m - 2 + shiftY)))

                &&

                moves.contains(new Move(new ChessBoardCell<>(n - 3 + shiftX, m - 1 + shiftY), new ChessBoardCell<>(n - 1 + shiftX, m - 2 + shiftY)))
                || moves.contains(new Move(new ChessBoardCell<>(n - 1 + shiftX, m - 2 + shiftY), new ChessBoardCell<>(n - 3 + shiftX, m - 1 + shiftY)))
                &&
                moves.contains(new Move(new ChessBoardCell<>(n - 2 + shiftX, m - 1 + shiftY), new ChessBoardCell<>(n - 1 + shiftX, m - 3 + shiftY)))
                || moves.contains(new Move(new ChessBoardCell<>(n - 1 + shiftX, m - 3 + shiftY), new ChessBoardCell<>(n - 2 + shiftX, n - 2 + shiftY))));
    }

    private void checkCorrectness(ArrayList<ChessBoardCell<Integer, Integer>> tour, TourType type, int n, int m, int shiftX, int shiftY, boolean structured) {
        if (type == TourType.CLOSED) {
            assertTrue(checkClosed(tour.get(0), tour.get(tour.size() - 1)));
        }
        else {
            assertTrue(checkOpen(tour.get(0), tour.get(tour.size() - 1)));
        }
        assertTrue(checkStructured(tour, structured, n, m, shiftX, shiftY));
        assertEquals(n * m, tour.size());
        assertTrue(checkMoveCorrectness(tour, n, m, shiftX, shiftY));
    }

    @Test
    public void warnsdorff8x8OpenWithShift() {
        solver = new WarnsdorffRule(8);
        container = solver.generateTour(TourType.OPEN, false, 10, 143);
        checkCorrectness(container.toArrayList(), TourType.OPEN, 8, 8, 10, 143, false);
    }

    @Test
    public void warnsdorff10x10Open() {
        solver = new WarnsdorffRule(10);
        container = solver.generateTour(TourType.OPEN, false, 0, 0);
        checkCorrectness(container.toArrayList(), TourType.OPEN, 10, 10, 0, 0, false);
    }

    @Test
    public void warnsdorff37x10Open() {
        solver = new WarnsdorffRule(37, 10);
        container = solver.generateTour(TourType.OPEN, false, 0, 0);
        checkCorrectness(container.toArrayList(), TourType.OPEN, 37, 10, 0, 0, false);
    }

    @Test
    public void warnsdorff41x41Open() {
        solver = new WarnsdorffRule(41, 41);
        container = solver.generateTour(TourType.OPEN, false, 0, 0);
        checkCorrectness(container.toArrayList(), TourType.OPEN, 41, 41, 0, 0, false);
    }

    @Test
    public void warnsdorff41x41OpenStructured() {
        solver = new WarnsdorffRule(41, 41);
        container = solver.generateTour(TourType.OPEN, true, 0, 0);
        checkCorrectness(container.toArrayList(), TourType.OPEN, 41, 41, 0, 0, true);
    }

    @Test
    public void warnsdorff10x10Closed() {
        solver = new WarnsdorffRule(10);
        container = solver.generateTour(TourType.CLOSED, false, 0, 0);
        checkCorrectness(container.toArrayList(), TourType.CLOSED,10, 10, 0, 0, false);
    }

    @Test
    public void warnsdorff34x34ClosedWithShift() {
        solver = new WarnsdorffRule(34);
        container = solver.generateTour(TourType.CLOSED, false, 20, 89);
        checkCorrectness(container.toArrayList(), TourType.CLOSED,34, 34, 20, 89, false);
    }

    @Test
    public void warnsdorff63x23OpenStructured() {
        solver = new WarnsdorffRule(63, 23);
        container = solver.generateTour(TourType.OPEN, true, 0, 0);
        checkCorrectness(container.toArrayList(), TourType.OPEN,63, 23, 0, 0, true);
    }

    @Test
    public void warnsdorff49x34Closed() {
        solver = new WarnsdorffRule(49, 34);
        container = solver.generateTour(TourType.CLOSED, false, 0, 0);
        checkCorrectness(container.toArrayList(), TourType.CLOSED,49, 34, 0, 0, false);
    }

    @Test
    public void warnsdorff34x34ClosedStructuredWithShift() {
        solver = new WarnsdorffRule(34);
        container = solver.generateTour(TourType.CLOSED, true, 20, 89);
        checkCorrectness(container.toArrayList(), TourType.CLOSED,34, 34, 20, 89, true);
    }

    @Test
    public void warnsdorff18x34ClosedStructuredWithShift() {
        solver = new WarnsdorffRule(18, 34);
        container = solver.generateTour(TourType.CLOSED, true,  7, 7);
        checkCorrectness(container.toArrayList(), TourType.CLOSED,18, 34, 7, 7, true);
    }

    @Test
    public void warnsdorff42x42ClosedStructured() {
        solver = new WarnsdorffRule(42, 42);
        container = solver.generateTour(TourType.CLOSED, true,  0, 0);
        checkCorrectness(container.toArrayList(), TourType.CLOSED,42, 42, 0, 0, true);
    }

    @Test
    public void warnsdorff40x20OpenStructured() {
        solver = new WarnsdorffRule(40, 20);
        container = solver.generateTour(TourType.OPEN, true,  0, 0);
        checkCorrectness(container.toArrayList(), TourType.OPEN,40, 20, 0, 0, true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void warnsdorffIncorrectConfigurationOpen() {
        solver = new WarnsdorffRule(2, 200);
        container = solver.generateTour(TourType.OPEN, false,  0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void warnsdorffIncorrectConfigurationOpen3x5() {
        solver = new WarnsdorffRule(5, 3);
        container = solver.generateTour(TourType.OPEN, false,  0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void warnsdorffIncorrectConfigurationOpen3x6() {
        solver = new WarnsdorffRule(3, 6);
        container = solver.generateTour(TourType.OPEN, false,  0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void warnsdorffIncorrectConfigurationOpen4x4() {
        solver = new WarnsdorffRule(4, 4);
        container = solver.generateTour(TourType.OPEN, false,  0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void warnsdorffIncorrectTourConfigurationClosedBothOdd() {
        solver = new WarnsdorffRule(31, 31);
        container = solver.generateTour(TourType.CLOSED, false, 0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void warnsdorffIncorrectTourConfigurationClosed4x6() {
        solver = new WarnsdorffRule(4, 6);
        container = solver.generateTour(TourType.CLOSED, false, 0, 0);
    }

    @Test
    public void warnsdorffOpenNonStructured4x6() {
        solver = new WarnsdorffRule(4, 6);
        container = solver.generateTour(TourType.OPEN, false, 0, 0);
        checkCorrectness(container.toArrayList(), TourType.OPEN, 4, 6,0, 0, false);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void warnsdorffIncorrectBoardConfiguration0x100() {
        solver = new WarnsdorffRule(0, 100);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void warnsdorffIncorrectBoardConfigurationBothNegative() {
        solver = new WarnsdorffRule(-132);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void warnsdorffIncorrectBoardConfigurationBothNegative2() {
        solver = new WarnsdorffRule(-132, -140);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void warnsdorffIncorrectBoardConfigurationOneDimSmall() {
        solver = new WarnsdorffRule(1, 10);
    }

    @Test
    public void warnsdorffDoubleInvocationToArrayList() {
        solver = new WarnsdorffRule(49, 34);
        container = solver.generateTour(TourType.CLOSED, false, 0, 0);
        ArrayList<ChessBoardCell<Integer, Integer>> arr1 = container.toArrayList();
        ArrayList<ChessBoardCell<Integer, Integer>> arr2 = container.toArrayList();
        assertEquals(arr1.size(), arr2.size());
    }

    @Test
    public void warnsdorffDoubleInvocationToArrayListAndGenerateTour() {
        solver = new WarnsdorffRule(40, 40);
        container = solver.generateTour(TourType.CLOSED, false, 0, 0);
        ArrayList<ChessBoardCell<Integer, Integer>> arr1 = container.toArrayList();
        ArrayList<ChessBoardCell<Integer, Integer>> arr2 = container.toArrayList();
        assertEquals(arr1.size(), arr2.size());
        container = solver.generateTour(TourType.OPEN, true, 10, 10);
        arr1 = container.toArrayList();
        arr2 = container.toArrayList();
        assertEquals(arr1.size(), arr2.size());
    }
}