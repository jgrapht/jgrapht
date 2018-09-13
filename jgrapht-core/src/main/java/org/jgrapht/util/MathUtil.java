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

    /**
     * Calculate the floor of the binary logarithm of $n$.
     *
     * @param n the input number
     * @return the binary logarithm
     */
    public static int log2(int n)
    {
        // returns 0 for n=0
        int log = 0;
        if( ( n & 0xffff0000 ) != 0 ) { n >>>= 16; log = 16; }
        if( n >= 256 ) { n >>>= 8; log += 8; }
        if( n >= 16  ) { n >>>= 4; log += 4; }
        if( n >= 4   ) { n >>>= 2; log += 2; }
        return log + ( n >>> 1 );
    }
}

// End MathUtil.java
