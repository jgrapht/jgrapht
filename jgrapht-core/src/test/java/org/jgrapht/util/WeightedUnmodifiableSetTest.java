/*
 * (C) Copyright 2026-2026, by Harsh and Contributors.
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
package org.jgrapht.util;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class WeightedUnmodifiableSetTest
{
    @Test
    public void testEqualityWithOrdinarySets()
    {
        Set<Integer> plain = new HashSet<>(Set.of(1, 2));
        Set<Integer> weighted = new WeightedUnmodifiableSet<>(plain, 7.0);
        Set<Integer> sorted = new TreeSet<>(plain);

        assertEquals(plain, weighted);
        assertEquals(weighted, plain);
        assertEquals(weighted, sorted);
        assertEquals(sorted, weighted);
        assertEquals(plain.hashCode(), weighted.hashCode());
        assertEquals(new WeightedUnmodifiableSet<>(plain, 99.0), weighted);
    }

    @Test
    public void testUnequalObjects()
    {
        Set<Integer> weighted = new WeightedUnmodifiableSet<>(Set.of(1, 2));

        assertEquals(weighted, weighted);
        assertNotEquals(weighted, null);
        assertNotEquals(weighted, List.of(1, 2));
        assertNotEquals(weighted, Set.of(1));
        assertNotEquals(weighted, Set.of(1, 3));
    }

    @Test
    public void testEmptyAndNullContainingSets()
    {
        Set<Integer> empty = new WeightedUnmodifiableSet<>(Collections.emptySet());
        assertEquals(empty, Collections.emptySet());
        assertEquals(Collections.emptySet(), empty);

        Set<Integer> plain = new HashSet<>(Arrays.asList(null, 1));
        Set<Integer> weighted = new WeightedUnmodifiableSet<>(plain);
        assertEquals(weighted, plain);
        assertEquals(plain, weighted);
        assertEquals(plain.hashCode(), weighted.hashCode());
    }
}
