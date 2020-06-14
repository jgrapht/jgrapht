/*
 * (C) Copyright 2018-2020, by Dimitrios Michail and Contributors.
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

import org.junit.*;

import java.util.*;
import java.util.stream.*;

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
            new HashSet<>(), new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)));
        assertEquals(5, union.size());
        IntStream.rangeClosed(1, 5).forEach(x -> assertTrue(union.contains(x)));
        IntStream.of(6).forEach(x -> assertFalse(union.contains(x)));
    }

    @Test
    public void test5()
    {
        UnmodifiableUnionSet<Integer> union =
            new UnmodifiableUnionSet<>(new HashSet<>(), new HashSet<>());
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

        IntStream.rangeClosed(1, 15).forEach(x -> assertTrue(collectedElementsAsList.contains(x)));
        IntStream.rangeClosed(1, 15).forEach(x -> assertTrue(collectedElementsAsSet.contains(x)));
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

        IntStream.rangeClosed(1, 10).forEach(x -> assertTrue(collectedElementsAsList.contains(x)));
        IntStream.rangeClosed(1, 10).forEach(x -> assertTrue(collectedElementsAsSet.contains(x)));
    }

    @Test
    public void testOptimizations()
    {
        // per https://github.com/jgrapht/jgrapht/issues/757, verify
        // that constructor and contains do not require call to size on
        // underlying sets, and that size/iterator calls are optimized
        // based on the relative sizes of the underlying sets

        Set<Integer> smallerHash = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
        ProfilingSet<Integer> smaller = new ProfilingSet<>(smallerHash);
        Set<Integer> biggerHash = new HashSet<>(Arrays.asList(3, 4, 5, 6, 7, 8, 9, 10));
        ProfilingSet<Integer> bigger = new ProfilingSet<>(biggerHash);

        UnmodifiableUnionSet<Integer> union = new UnmodifiableUnionSet<>(smaller, bigger);
        verifyOptimizations(smaller, bigger, union);

        // repeat with smaller/bigger constructor parameters swapped
        UnmodifiableUnionSet<Integer> swapped = new UnmodifiableUnionSet<>(bigger, smaller);
        verifyOptimizations(smaller, bigger, swapped);

        // now verify that if we dynamically resize the underlying data on
        // existing unions, the optimizations are still performed correctly
        ProfilingSet<Integer> shrunk = bigger;
        ProfilingSet<Integer> grown = smaller;
        shrunk.setDelegate(smallerHash);
        grown.setDelegate(biggerHash);
        verifyOptimizations(shrunk, grown, union);
        verifyOptimizations(shrunk, grown, swapped);
    }

    private void verifyOptimizations(
        ProfilingSet<Integer> smaller, ProfilingSet<Integer> bigger,
        UnmodifiableUnionSet<Integer> union)
    {
        // verify that constructor did not make calls on
        // underlying sets (or that reused input sets were properly
        // precleared)
        verifyNoCalls(smaller, bigger);

        // likewise for contains
        assertTrue(union.contains(3));
        assertFalse(union.contains(11));
        verifyNoCalls(smaller, bigger);

        // verify optimizations for calls to size()
        verifySizeOptimizations(smaller, bigger, union);
        // repeat to verify that size checks are performed each time
        verifySizeOptimizations(smaller, bigger, union);

        // verify optimizations for iteration
        verifyIterationOptimizations(smaller, bigger, union);
        // and repeat
        verifyIterationOptimizations(smaller, bigger, union);

        smaller.clearCallCounts();
        bigger.clearCallCounts();
    }

    private void verifyNoCalls(ProfilingSet<Integer> smaller, ProfilingSet<Integer> bigger)
    {
        assertEquals(0, smaller.getSizeCallCount());
        assertEquals(0, bigger.getSizeCallCount());
        assertEquals(0, smaller.getIteratorCallCount());
        assertEquals(0, bigger.getIteratorCallCount());
    }

    private void verifySizeOptimizations(
        ProfilingSet<Integer> smaller, ProfilingSet<Integer> bigger,
        UnmodifiableUnionSet<Integer> union)
    {
        smaller.clearCallCounts();
        bigger.clearCallCounts();

        assertEquals(10, union.size());
        assertEquals(1, smaller.getSizeCallCount());
        assertEquals(1, bigger.getSizeCallCount());
        assertEquals(1, smaller.getIteratorCallCount());
        assertEquals(0, bigger.getIteratorCallCount());
        assertEquals(5, smaller.getIteratorNextCallCount());
        assertEquals(0, bigger.getIteratorNextCallCount());
    }

    private void verifyIterationOptimizations(
        ProfilingSet<Integer> smaller, ProfilingSet<Integer> bigger,
        UnmodifiableUnionSet<Integer> union)
    {
        smaller.clearCallCounts();
        bigger.clearCallCounts();

        int count = 0;
        for (Integer i : union) {
            count++;
        }
        assertEquals(10, count);
        assertEquals(1, smaller.getSizeCallCount());
        assertEquals(1, bigger.getSizeCallCount());
        assertEquals(1, smaller.getIteratorCallCount());
        assertEquals(1, bigger.getIteratorCallCount());
        assertEquals(5, smaller.getIteratorNextCallCount());
        assertEquals(8, bigger.getIteratorNextCallCount());
    }

    /**
     * Set wrapper for counting calls to individual methods.
     */
    private static class ProfilingSet<E>
        extends
        AbstractSet<E>
    {
        private Set<E> delegate;

        private int iteratorCalls = 0;

        private int iteratorNextCalls = 0;

        private int sizeCalls = 0;

        ProfilingSet(Set<E> delegate)
        {
            setDelegate(delegate);
        }

        void setDelegate(Set<E> delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public boolean contains(Object o)
        {
            return delegate.contains(o);
        }

        @Override
        public Iterator<E> iterator()
        {
            iteratorCalls++;
            return new Iterator<E>()
            {
                private Iterator<E> delegateIterator = delegate.iterator();

                public boolean hasNext()
                {
                    return delegateIterator.hasNext();
                }

                public E next()
                {
                    iteratorNextCalls++;
                    return delegateIterator.next();
                }
            };
        }

        @Override
        public int size()
        {
            sizeCalls++;
            return delegate.size();
        }

        int getSizeCallCount()
        {
            return sizeCalls;
        }

        int getIteratorCallCount()
        {
            return iteratorCalls;
        }

        int getIteratorNextCallCount()
        {
            return iteratorNextCalls;
        }

        void clearCallCounts()
        {
            sizeCalls = 0;
            iteratorCalls = 0;
            iteratorNextCalls = 0;
        }
    }
}
