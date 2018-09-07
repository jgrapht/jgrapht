/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.util;

import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Test {@link AliasMethodSampler}.
 * 
 * @author Dimitrios Michail
 */
public class AliasMethodSamplerTest
{

    @Test
    public void test1()
    {
        final long seed = 5;
        double[] prob = { 0.1, 0.2, 0.3, 0.4 };
        AliasMethodSampler am = new AliasMethodSampler(prob, new Random(seed));

        int[] counts = new int[4];
        for (int i = 0; i < 1000000; i++) {
            counts[am.next()]++;
        }

        assertEquals(100033, counts[0]);
        assertEquals(200069, counts[1]);
        assertEquals(299535, counts[2]);
        assertEquals(400363, counts[3]);
    }

    @Test
    public void test2()
    {
        final long seed = 17;
        double[] prob = { 0.05, 0.05, 0.05, 0.05, 0.8 };
        AliasMethodSampler am = new AliasMethodSampler(prob, new Random(seed));

        int[] counts = new int[5];
        for (int i = 0; i < 1000000; i++) {
            counts[am.next()]++;
        }

        assertEquals(49949, counts[0]);
        assertEquals(49726, counts[1]);
        assertEquals(50441, counts[2]);
        assertEquals(49894, counts[3]);
        assertEquals(799990, counts[4]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonValid()
    {
        double[] prob = { 0.5, 0.6 };
        new AliasMethodSampler(prob, new Random(15));
    }

}
