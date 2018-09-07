/*
 * (C) Copyright 2015-2018, by Fabian Späh and Contributors.
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

import java.util.*;

/**
 * A default implementation for a check on equality (that always holds)
 * 
 * @param <T> type of elements to be compared
 * 
 */
public class AlwaysEqualComparator<T>
    implements
    Comparator<T>
{
    @Override
    public int compare(T arg0, T arg1)
    {
        return 0;
    }
}

// End AlwaysEqualComparator.java
