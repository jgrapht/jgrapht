/*
 * (C) Copyright 2005-2018, by Assaf Lehr and Contributors.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PrefetchIteratorTest
{
    // ~ Methods ----------------------------------------------------------------

    @Test
    public void testIteratorInterface()
    {
        Iterator<Integer> iterator = new IterateFrom1To99();
        for (int i = 1; i < 100; i++) {
            assertEquals(true, iterator.hasNext());
            assertEquals(i, iterator.next().intValue());
        }
        assertEquals(false, iterator.hasNext());
        Exception exceptionThrown = null;
        try {
            iterator.next();
        } catch (Exception e) {
            exceptionThrown = e;
        }
        assertTrue(exceptionThrown instanceof NoSuchElementException);
    }

    @Test
    public void testEnumInterface()
    {
        Enumeration<Integer> enumuration = new IterateFrom1To99();
        for (int i = 1; i < 100; i++) {
            assertEquals(true, enumuration.hasMoreElements());
            assertEquals(i, enumuration.nextElement().intValue());
        }
        assertEquals(false, enumuration.hasMoreElements());
        Exception exceptionThrown = null;
        try {
            enumuration.nextElement();
        } catch (Exception e) {
            exceptionThrown = e;
        }
        assertTrue(exceptionThrown instanceof NoSuchElementException);
    }

    // ~ Inner Classes ----------------------------------------------------------

    // This test class supplies enumeration of integer from 1 till 100.
    public static class IterateFrom1To99
        implements
        Enumeration<Integer>,
        Iterator<Integer>
    {
        private int counter = 0;
        private PrefetchIterator<Integer> nextSupplier;

        public IterateFrom1To99()
        {
            nextSupplier = new PrefetchIterator<>(() -> {
                counter++;
                if (counter >= 100) {
                    throw new NoSuchElementException();
                } else {
                    return counter;
                }
            });
        }

        // forwarding to nextSupplier and return its returned value
        @Override
        public boolean hasMoreElements()
        {
            return this.nextSupplier.hasMoreElements();
        }

        // forwarding to nextSupplier and return its returned value
        @Override
        public Integer nextElement()
        {
            return this.nextSupplier.nextElement();
        }

        @Override
        public Integer next()
        {
            return this.nextSupplier.next();
        }

        @Override
        public boolean hasNext()
        {
            return this.nextSupplier.hasNext();
        }

        @Override
        public void remove()
        {
            this.nextSupplier.remove();
        }
    }
}
