/*
 * (C) Copyright 2019-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.opt.graph.sparse.specifics;

import java.util.*;
import java.util.stream.*;

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
        return IntStream.range(0, n).iterator();
    }

    @Override
    public boolean contains(Object o)
    {
        if (o instanceof Integer) {
            Integer x = (Integer) o;
            return x >= 0 && x < n;
        }
        return false;
    }

    @Override
    public int size()
    {
        return n;
    }

}
