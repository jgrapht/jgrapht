/*
 * (C) Copyright 2017-2017, by Alexandru Valeanu and Contributors.
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

import org.junit.Test;

import java.util.*;

import static org.jgrapht.alg.util.UnionFindTest.testIdentical;
import static org.jgrapht.alg.util.UnionFindTest.union;
import static org.junit.Assert.*;

/**
 * Test {@link UnionFindUndo} class
 *
 * @author Alexandru Valeanu
 */
public class UnionFindUndoTest {
    private static class DisjointSets<E> extends AbstractUnionFind<E>{

        DisjointSets(){
        }

        DisjointSets(Map<E, E> map){
            map.forEach(parentMap::put);
        }

        @Override
        public void addElement(E element) {
            assert !parentMap.containsKey(element);
            parentMap.put(element, element);
            count++;
        }

        @Override
        public E find(E element) {
            E parent = parentMap.get(element);
            assert parent != null;
            return parent;
        }

        @Override
        public void union(E element1, E element2) {
            Map<E, E> newMap = new HashMap<>();
            element1 = find(element1);
            element2 = find(element2);

            for (Map.Entry<E, E> entry: parentMap.entrySet()){
                if (entry.getValue().equals(element1)){
                    newMap.put(entry.getKey(), element2);
                }
                else{
                    newMap.put(entry.getKey(), entry.getValue());
                }
            }

            parentMap.clear();
            newMap.forEach(parentMap::put);
            count = new HashSet<>(parentMap.values()).size();
        }

        @Override
        public void reset() {
            throw new UnsupportedOperationException("not implemented");
        }

        DisjointSets<E> deepCopy(){
            return new DisjointSets<>(parentMap);
        }
    }

    @Test
    public void testSimple(){
        UnionFindUndo<String> ufu = new UnionFindUndo<>();
        ufu.addElement("alex");
        ufu.addElement("ben");
        ufu.addElement("charlie");
        ufu.addElement("dan");
        assertEquals(ufu.size(), 4);
        ufu.undo();
        ufu.undo();
        assertEquals(ufu.size(), 2);
        ufu.redo();
        assertEquals(ufu.size(), 3);
        ufu.addElement("earl");
        ufu.union("alex", "ben");
        assertEquals(ufu.find("alex"), ufu.find("ben"));
        ufu.undo(1);
        assertNotEquals(ufu.find("alex"), ufu.find("ben"));
    }

    @Test
    public void testSimple2(){
        UnionFindUndo<Integer> ufu = new UnionFindUndo<>();

        for (int i = 0; i < 10; i++) {
            ufu.addElement(i);
        }

        assertEquals(ufu.size(), 10);
        assertEquals(ufu.numberOfSets(), 10);

        ufu.undo(); // nothing should happen
        ufu.redo(); // nothing should happen

        ufu.union(4, 5);
        ufu.union(1, 2);
        ufu.union(5, 7);

        assertTrue(ufu.inSameSet(4, 7));
        ufu.undo();
        assertFalse(ufu.inSameSet(4, 7));
        ufu.redo();
        assertTrue(ufu.inSameSet(4, 7));

        ufu.undo(2);
        assertTrue(ufu.inSameSet(4, 5));
        assertFalse(ufu.inSameSet(1, 2));
        assertFalse(ufu.inSameSet(4, 7));
        ufu.redo(1);
        assertTrue(ufu.inSameSet(1, 2));
        assertFalse(ufu.inSameSet(5, 7));

        ufu.union(5, 8);
        assertFalse(ufu.inSameSet(4, 7));
        assertTrue(ufu.inSameSet(4, 8));
        ufu.undo(2);
        assertEquals(ufu.numberOfSets(), 9);
        ufu.redo(1);
        assertEquals(ufu.numberOfSets(), 8);

        ufu.addElement(11);
        assertEquals(ufu.size(), 11);
        ufu.undo();
        assertEquals(ufu.size(), 10);

        ufu.undo(1000);
        assertEquals(ufu.size(), 0);
    }

    @Test
    public void testRandom(){
        Random random = new Random(121212);
        DisjointSets<Integer> dsu = new DisjointSets<>();
        UnionFindUndo<Integer> ufu = new UnionFindUndo<>();

        int N = 0;
        for (int i = 0; i < 5; i++) {
            dsu.addElement(N);
            ufu.addElement(N);
            N++;
        }

        assertEquals(dsu.numberOfSets(), ufu.numberOfSets());
        assertEquals(dsu.size(), ufu.size());

        final int NUM_OPS = 15_000;
        for (int i = 0; i < NUM_OPS; i++) {
            int t = random.nextInt(2);

            if (t == 0){
                dsu.addElement(N);
                ufu.addElement(N);
                N++;
            } else if (t == 1){
                int u = random.nextInt(N);
                int v = random.nextInt(N);
                dsu.union(u, v);
                ufu.union(u, v);
            }

            assertEquals(dsu.numberOfSets(), ufu.numberOfSets());
            assertEquals(dsu.size(), ufu.size());

            for (int j = 0; j < 100; j++) {
                int u = random.nextInt(N);
                int v = random.nextInt(N);
                assertEquals(dsu.inSameSet(u, v), ufu.inSameSet(u, v));
            }
        }
    }

    @Test
    public void testSpeed(){
        final int N = 100_000;
        UnionFindUndo<Integer> ufu = new UnionFindUndo<>();

        for (int i = 0; i < N; i++) {
            ufu.addElement(i);
        }

        for (int i = 1; i < N; i++) {
            ufu.union(0, i);
        }

        assertEquals(ufu.numberOfSets(), 1);
        ufu.undo(N - 1);
        assertEquals(ufu.numberOfSets(), N);
        ufu.redo(N - 1);
        assertEquals(ufu.numberOfSets(), 1);
    }

    @Test
    public void testUnionFind()
    {
        TreeSet<String> set = new TreeSet<String>();
        String[] strs = { "aaa", "bbb", "ccc", "ddd", "eee" };
        ArrayList<ArrayList<String>> sets = new ArrayList<>();
        for (String str : strs) {
            set.add(str);
            sets.add(new ArrayList<>());
            sets.get(sets.size() - 1).add(str);
        }
        UnionFindUndo<String> uf = new UnionFindUndo<>(set);
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
}