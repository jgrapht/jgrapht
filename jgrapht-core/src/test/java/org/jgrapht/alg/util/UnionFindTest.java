/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2010, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
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
        final TreeSet<String> set = new TreeSet<String>();
        final String [] strs = { "aaa", "bbb", "ccc", "ddd", "eee" };
        final ArrayList<ArrayList<String>> sets = new ArrayList<ArrayList<String>>();
        for (final String str : strs) {
            set.add(str);
            sets.add(new ArrayList<String>());
            sets.get(sets.size() - 1).add(str);
        }
        final UnionFind<String> uf = new UnionFind<String>(set);
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

    private void union(final ArrayList<ArrayList<String>> sets, final String a, final String b)
    {
        final ArrayList<String> toAdd = new ArrayList<String>();
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

    private boolean same(final ArrayList<ArrayList<String>> sets, final String a, final String b)
    {
        for (final ArrayList<String> set : sets) {
            if (set.contains(a) && set.contains(b)) {
                return true;
            }
        }
        return false;
    }

    private void testIdentical(
        final String [] universe,
        final ArrayList<ArrayList<String>> sets,
        final UnionFind<String> uf)
    {
        for (final String a : universe) {
            for (final String b : universe) {
                final boolean same1 = uf.find(a).equals(uf.find(b));
                final boolean same2 = same(sets, a, b);
                assertEquals(same1, same2);
            }
        }
    }
}

// End UnionFindTest.java
