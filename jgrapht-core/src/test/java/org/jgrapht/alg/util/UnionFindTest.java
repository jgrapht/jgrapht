/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2010, by Barak Naveh and Contributors.
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
/* ------------------------------
 * UnionFindTest.java
 * ------------------------------
 * (C) Copyright 2010-2010, by Tom Conerly and Contributors.
 *
 * Original Author:  Tom Conerly
 * Contributor(s):   -
 *
 * Changes
 * -------
 * 02-Feb-2010 : Initial revision (TC);
 *
 */
package org.jgrapht.alg.util;

import java.util.*;

import junit.framework.*;


/**
 * .
 *
 * @author Tom Conerly
 */
public class UnionFindTest
    extends TestCase
{
    //~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    public void testUnionFind()
    {
        TreeSet<String> set = new TreeSet<String>();
        String [] strs = { "aaa", "bbb", "ccc", "ddd", "eee" };
        ArrayList<ArrayList<String>> sets = new ArrayList<ArrayList<String>>();
        for (String str : strs) {
            set.add(str);
            sets.add(new ArrayList<String>());
            sets.get(sets.size() - 1).add(str);
        }
        UnionFind<String> uf = new UnionFind<String>(set);
        testIdentical(strs, sets, uf);

        uf.union(strs[0], strs[1]);
        union(sets, strs[0], strs[1]);
        testIdentical(strs, sets, uf);

        uf.union(strs[2], strs[3]);
        union(sets, strs[2], strs[3]);
        testIdentical(strs, sets, uf);

        uf.union(strs[2], strs[4]);
        union(sets, strs[2], strs[4]);
        testIdentical(strs, sets, uf);

        uf.union(strs[2], strs[4]);
        union(sets, strs[2], strs[4]);
        testIdentical(strs, sets, uf);

        uf.union(strs[0], strs[4]);
        union(sets, strs[0], strs[4]);
        testIdentical(strs, sets, uf);
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
        String [] universe,
        ArrayList<ArrayList<String>> sets,
        UnionFind<String> uf)
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

// End UnionFindTest.java
