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

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KnightTourParberryTest {

    private KnightTourParberry par;
    
    private boolean checkMove(int x1, int y1, int x2, int y2) {
        return (Math.abs(x1 - x2) == 1 && Math.abs(y1 - y2) == 2) || (Math.abs(x1  - x2) == 2 && Math.abs(y1 - y2) == 1);
    }

    private boolean checkCorrectnessParberry(ArrayList<ChessBoardCell<Integer, Integer>> list, int n, int m) {
        if (n * m != list.size()) {
            return false;
        }

        if (!(Math.abs(list.get(0).first - list.get(list.size() - 1).first) == 1 && Math.abs(list.get(0).second - list.get(list.size() - 1).second) == 2
                || Math.abs(list.get(0).first - list.get(list.size() - 1).first) == 2 && Math.abs(list.get(0).second - list.get(list.size() - 1).second) == 1)) {
            return false;
        }

        boolean[][] used = new boolean[n][m];
        used[list.get(0).first][list.get(0).second] = true;

        for (int i = 1; i < list.size(); i++) {
            if (!checkMove(list.get(i).first, list.get(i).second, list.get(i - 1).first, list.get(i - 1).second) || used[list.get(i).first][list.get(i).second]) {
                return false;
            }
            used[list.get(i).first][list.get(i).second] = true;
        }
        return true;
    }

    @Test
    public void testParberry64x64() {
        par = new KnightTourParberry(64, 64);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 64, 64));
    }

    @Test
    public void testParberry128x128() {
        par = new KnightTourParberry(128, 128);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 128, 128));
    }

    @Test
    public void testParberry12x12() {
        par = new KnightTourParberry(12, 12);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 12, 12));
    }

    @Test
    public void testParberry8x8() {
        par = new KnightTourParberry(8, 8);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 8, 8));
    }

    @Test
    public void testParberry14x14() {
        par = new KnightTourParberry(14, 14);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 14, 14));
    }

    @Test
    public void testParberry38x38() {
        par = new KnightTourParberry(38, 38);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 38, 38));
    }

    @Test
    public void testParberry70x72() {
        par = new KnightTourParberry(70, 72);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 70, 72));
    }

    @Test
    public void testParberry14x16() {
        par = new KnightTourParberry(14, 16);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 14, 16));
    }

    @Test
    public void testParberry24x26() {
        par = new KnightTourParberry(24, 26);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 24, 26));
    }

    @Test
    public void testParberry78x80() {
        par = new KnightTourParberry(78, 80);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 78, 80));
    }

    @Test
    public void testParberry140x142() {
        par = new KnightTourParberry(140, 142);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 140, 142));
    }

    @Test
    public void testParberry282x284() {
        par = new KnightTourParberry(282, 284);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 282, 284));
    }

    @Test
    public void testParberry696x698() {
        par = new KnightTourParberry(696, 698);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 696, 698));
    }

    @Test
    public void testParberry150x150() {
        par = new KnightTourParberry(150, 150);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 150, 150));
    }

    @Test
    public void testParberry76x76() {
        par = new KnightTourParberry(76, 76);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 76, 76));
    }

    @Test
    public void testParberry34x36() {
        par = new KnightTourParberry(34, 36);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 34, 36));
    }

    @Test
    public void testParberry340x342() {
        par = new KnightTourParberry(340, 342);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 340, 342));
    }

    @Test
    public void testParberry800x800() {
        par = new KnightTourParberry(800, 800);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 800, 800));
    }

    @Test
    public void testParberry700x700() {
        par = new KnightTourParberry(700, 700);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 700, 700));
    }

    @Test
    public void testParberry1340x1342() {
        par = new KnightTourParberry(1340, 1342);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 1340, 1342));
    }

    @Test
    public void testParberry1344x1344() {
        par = new KnightTourParberry(1344, 1344);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 1344, 1344));
    }

    @Test
    public void testParberry6x6() {
        par = new KnightTourParberry(6, 6);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 6, 6));
    }

    @Test
    public void testParberry6x8() {
        par = new KnightTourParberry(6, 8);
        assertTrue(checkCorrectnessParberry(par.buildTour().toArrayList(), 6, 8));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testParberryIncorrectBoardConf1() {
        par = new KnightTourParberry(2, 2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testParberryIncorrectBoardConf2() {
        par = new KnightTourParberry(21, 22);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testParberryIncorrectBoardConf3() {
        par = new KnightTourParberry(73, 73);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testParberryIncorrectBoardConf4() {
        par = new KnightTourParberry(-20, 20);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testParberryIncorrectBoardConf5() {
        par = new KnightTourParberry(40, 44);
    }

    @Test
    public void parberryDoubleInvocationToArrayList() {
        par = new KnightTourParberry(48, 50);
        KnightTourContainer cont = par.buildTour();
        ArrayList<ChessBoardCell<Integer, Integer>> arr1 = cont.toArrayList();
        ArrayList<ChessBoardCell<Integer, Integer>> arr2 = cont.toArrayList();
        assertEquals(arr1.size(), arr2.size());
    }

    @Test
    public void parberryDoubleInvocationToArrayListAndGenerateTour() {
        par = new KnightTourParberry(40, 40);
        KnightTourContainer cont = par.buildTour();
        ArrayList<ChessBoardCell<Integer, Integer>> arr1 = cont.toArrayList();
        ArrayList<ChessBoardCell<Integer, Integer>> arr2 = cont.toArrayList();
        assertEquals(arr1.size(), arr2.size());
        cont = par.buildTour();
        arr1 = cont.toArrayList();
        arr2 = cont.toArrayList();
        assertEquals(arr1.size(), arr2.size());
    }
}
