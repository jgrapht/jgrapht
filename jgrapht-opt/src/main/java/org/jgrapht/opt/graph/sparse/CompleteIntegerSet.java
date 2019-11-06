/*
 * (C) Copyright 2019-2019, by Dimitrios Michail and Contributors.
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
package org.jgrapht.opt.graph.sparse;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An integer set containing all numbers from 0 to n-1.
 * 
 * @author Dimitrios Michail
 */
class CompleteIntegerSet
    extends
    AbstractSet<Integer>
{
    private int n;

    /**
     * Create an integer set from 0 to n-1.
     * 
     * @param n the number n
     */
    public CompleteIntegerSet(int n)
    {
        this.n = n;
    }

    @Override
    public Iterator<Integer> iterator()
    {
        return new CompleteIntegerSetIterator(n);
    }

    @Override
    public int size()
    {
        return n;
    }

    private class CompleteIntegerSetIterator
        implements
        Iterator<Integer>
    {
        private int n;
        private int current;

        public CompleteIntegerSetIterator(int n)
        {
            this.n = n;
            this.current = 0;
        }

        @Override
        public boolean hasNext()
        {
            return current < n;
        }

        @Override
        public Integer next()
        {
            if (current >= n) {
                throw new NoSuchElementException();
            }
            return current++;
        }

    }

}
