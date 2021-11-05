/*
 * (C) Copyright 2010-2021, by Tom Conerly and Contributors.
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

import static org.junit.Assert.*;

/**
 * .
 *
 * @author Tom Conerly
 */
public class UnionFindTest
{
    // ~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    @Test
    public void testUnionFind()
    {
        TreeSet<String> set = new TreeSet<String>();
        String[] strs = { "aaa", "bbb", "ccc", "ddd", "eee" };
        ArrayList<ArrayList<String>> sets = new ArrayList<ArrayList<String>>();
        for (String str : strs) {
            set.add(str);
            sets.add(new ArrayList<String>());
            sets.get(sets.size() - 1).add(str);
        }
        UnionFind<String> uf = new UnionFind<String>(set);
        assertEquals(5, uf.size());
        assertEquals(5, uf.numberOfSets());
        testIdentical(strs, sets, uf);

        uf.union(strs[0], strs[1]);
        assertEquals(4, uf.numberOfSets());
        union(sets, strs[0], strs[1]);
        testIdentical(strs, sets, uf);
        assertTrue(uf.inSameSet("aaa", "bbb"));
        assertFalse(uf.inSameSet("bbb", "ccc"));

        uf.union(strs[2], strs[3]);
        assertEquals(3, uf.numberOfSets());
        union(sets, strs[2], strs[3]);
        testIdentical(strs, sets, uf);

        uf.union(strs[2], strs[4]);
        assertEquals(2, uf.numberOfSets());
        union(sets, strs[2], strs[4]);
        testIdentical(strs, sets, uf);

        uf.union(strs[2], strs[4]);
        assertEquals(2, uf.numberOfSets());
        union(sets, strs[2], strs[4]);
        testIdentical(strs, sets, uf);

        uf.union(strs[0], strs[4]);
        assertEquals(1, uf.numberOfSets());
        union(sets, strs[0], strs[4]);
        testIdentical(strs, sets, uf);

        uf.addElement("fff");
        assertEquals(2, uf.numberOfSets());
        assertEquals(6, uf.size());
        uf.reset();
        assertEquals(6, uf.numberOfSets());
    }

    private void union(ArrayList<ArrayList<String>> sets, String a, String b)
    {
        ArrayList<String> toAdd = new ArrayList<String>();
        for (int i = 0; i < sets.size(); i++) {
            if (sets.get(i).contains(a)) {
                toAdd.addAll(sets.get(i));
                sets.remove(i);
                break;
            }
        }
        for (int i = 0; i < sets.size(); i++) {
            if (sets.get(i).contains(b)) {
                toAdd.addAll(sets.get(i));
                sets.remove(i);
                break;
            }
        }
        sets.add(toAdd);
    }

    private boolean same(ArrayList<ArrayList<String>> sets, String a, String b)
    {
        for (ArrayList<String> set : sets) {
            if (set.contains(a) && set.contains(b)) {
                return true;
            }
        }
        return false;
    }

    private void testIdentical(
        String[] universe, ArrayList<ArrayList<String>> sets, UnionFind<String> uf)
    {
        for (String a : universe) {
            for (String b : universe) {
                boolean same1 = uf.find(a).equals(uf.find(b));
                boolean same2 = same(sets, a, b);
                assertEquals(same1, same2);
            }
        }
    }
}
