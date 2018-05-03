/*
 * (C) Copyright 2005-2018, by Assaf Lehr and Contributors.
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

/**
 * Math Utilities.
 * 
 * @author Assaf Lehr
 * @since May 30, 2005
 */
public class MathUtil
{

    /**
     * Calculate the factorial of $n$.
     * 
     * @param n the input number
     * @return the factorial
     */
    public static long factorial(int n)
    {
        long multi = 1;
        for (int i = 1; i <= n; i++) {
            multi = multi * i;
        }
        return multi;
    }
}

// End MathUtil.java
