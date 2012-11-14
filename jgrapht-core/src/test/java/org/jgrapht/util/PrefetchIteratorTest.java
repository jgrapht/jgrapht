/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* -----------------
 * PrefetchIteratorTest.java
 * -----------------
 * (C) Copyright 2005-2008, by Assaf Lehr and Contributors.
 *
 * Original Author:  Assaf Lehr
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 */
package org.jgrapht.util;

import java.util.*;

import junit.framework.*;


public class PrefetchIteratorTest
    extends TestCase
{
    //~ Methods ----------------------------------------------------------------

    public void testIteratorInterface()
    {
        final Iterator iterator = new IterateFrom1To99();
        for (int i = 1; i < 100; i++) {
            assertEquals(true, iterator.hasNext());
            assertEquals(i, iterator.next());
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

    public void testEnumInterface()
    {
        final Enumeration enumuration = new IterateFrom1To99();
        for (int i = 1; i < 100; i++) {
            assertEquals(true, enumuration.hasMoreElements());
            assertEquals(i, enumuration.nextElement());
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

    //~ Inner Classes ----------------------------------------------------------

    // This test class supplies enumeration of integer from 1 till 100.
    public static class IterateFrom1To99
        implements Enumeration,
            Iterator
    {
        private int counter = 0;
        private final PrefetchIterator nextSupplier;

        public IterateFrom1To99()
        {
            nextSupplier =
                new PrefetchIterator<Integer>(
                    new PrefetchIterator.NextElementFunctor<Integer>() {
                        @Override
                        public Integer nextElement()
                            throws NoSuchElementException
                        {
                            counter++;
                            if (counter >= 100) {
                                throw new NoSuchElementException();
                            } else {
                                return new Integer(counter);
                            }
                        }
                    });
        }

        // forwarding to nextSupplier and return its returned value
        @Override
        public boolean hasMoreElements()
        {
            return nextSupplier.hasMoreElements();
        }

        // forwarding to nextSupplier and return its returned value
        @Override
        public Object nextElement()
        {
            return nextSupplier.nextElement();
        }

        @Override
        public Object next()
        {
            return nextSupplier.next();
        }

        @Override
        public boolean hasNext()
        {
            return nextSupplier.hasNext();
        }

        @Override
        public void remove()
        {
            nextSupplier.remove();
        }
    }
}

// End PrefetchIteratorTest.java
