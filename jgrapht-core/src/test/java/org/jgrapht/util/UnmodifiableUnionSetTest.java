/*
 * (C) Copyright 2018-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.util;

import org.junit.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static org.junit.Assert.*;

/**
 * Tests for {@link UnmodifiableUnionSet}.
 * 
 * @author Dimitrios Michail
 */
public class UnmodifiableUnionSetTest
{

    @Test
    public void test1()
    {
        UnmodifiableUnionSet<Integer> union = new UnmodifiableUnionSet<>(
            new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)),
            new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)));
        assertEquals(5, union.size());
        IntStream.rangeClosed(1, 5).forEach(x -> assertTrue(union.contains(x)));
        IntStream.rangeClosed(6, 15).forEach(x -> assertFalse(union.contains(x)));
    }

    @Test
    public void test2()
    {
        UnmodifiableUnionSet<Integer> union = new UnmodifiableUnionSet<>(
            new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)),
            new HashSet<>(Arrays.asList(6, 7, 8, 9, 10, 11, 12, 13, 14, 15)));
        assertEquals(15, union.size());
        IntStream.rangeClosed(1, 15).forEach(x -> assertTrue(union.contains(x)));
        IntStream.rangeClosed(16, 20).forEach(x -> assertFalse(union.contains(x)));
    }
    
    @Test
    public void test3()
    {
        UnmodifiableUnionSet<Integer> union = new UnmodifiableUnionSet<>(
            new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)),
            new HashSet<>(Arrays.asList(3, 4, 5, 6, 7, 8, 9, 10, 20)));
        assertEquals(11, union.size());
        IntStream.rangeClosed(1, 10).forEach(x -> assertTrue(union.contains(x)));
        IntStream.rangeClosed(11, 19).forEach(x -> assertFalse(union.contains(x)));
        IntStream.of(20).forEach(x -> assertTrue(union.contains(x)));
    }
    
    @Test
    public void test4()
    {
        UnmodifiableUnionSet<Integer> union = new UnmodifiableUnionSet<>(
            new HashSet<>(),
            new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)));
        assertEquals(5, union.size());
        IntStream.rangeClosed(1, 5).forEach(x -> assertTrue(union.contains(x)));
        IntStream.of(6).forEach(x -> assertFalse(union.contains(x)));
    }
    
    @Test
    public void test5()
    {
        UnmodifiableUnionSet<Integer> union = new UnmodifiableUnionSet<>(
            new HashSet<>(),
            new HashSet<>());
        assertEquals(0, union.size());
        IntStream.rangeClosed(1, 5).forEach(x -> assertFalse(union.contains(x)));
    }

    @Test
    public void testIteratorDisjoint()
    {
        UnmodifiableUnionSet<Integer> union = new UnmodifiableUnionSet<>(
            new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)),
            new HashSet<>(Arrays.asList(6, 7, 8, 9, 10, 11, 12, 13, 14, 15)));
        assertEquals(15, union.size());

        List<Integer> collectedElementsAsList = StreamSupport
            .stream(union.spliterator(), false).collect(Collectors.toCollection(ArrayList::new));
        assertEquals(15, collectedElementsAsList.size());
        
        Set<Integer> collectedElementsAsSet = StreamSupport
            .stream(union.spliterator(), false).collect(Collectors.toCollection(HashSet::new));
        assertEquals(15, collectedElementsAsSet.size());
        
        IntStream.rangeClosed(1, 15).forEach(x->assertTrue(collectedElementsAsList.contains(x)));
        IntStream.rangeClosed(1, 15).forEach(x->assertTrue(collectedElementsAsSet.contains(x)));
    }
    
    @Test
    public void testIteratorCommonElements()
    {
        UnmodifiableUnionSet<Integer> union = new UnmodifiableUnionSet<>(
            new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)),
            new HashSet<>(Arrays.asList(3, 4, 5, 6, 7, 8, 9, 10)));
        assertEquals(10, union.size());

        List<Integer> collectedElementsAsList = StreamSupport
            .stream(union.spliterator(), false).collect(Collectors.toCollection(ArrayList::new));
        assertEquals(10, collectedElementsAsList.size());
        
        Set<Integer> collectedElementsAsSet = StreamSupport
            .stream(union.spliterator(), false).collect(Collectors.toCollection(HashSet::new));
        assertEquals(10, collectedElementsAsSet.size());
        
        IntStream.rangeClosed(1, 10).forEach(x->assertTrue(collectedElementsAsList.contains(x)));
        IntStream.rangeClosed(1, 10).forEach(x->assertTrue(collectedElementsAsSet.contains(x)));
    }

}
