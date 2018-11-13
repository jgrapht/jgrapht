/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.util;

import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;

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
