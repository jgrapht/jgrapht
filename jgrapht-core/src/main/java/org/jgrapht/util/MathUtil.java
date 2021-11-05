/*
 * (C) Copyright 2005-2021, by Assaf Lehr and Contributors.
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

/**
 * Math Utilities.
 * 
 * @author Assaf Lehr
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
        if ((n & 0xffff0000) != 0) {
            n >>>= 16;
            log = 16;
        }
        if (n >= 256) {
            n >>>= 8;
            log += 8;
        }
        if (n >= 16) {
            n >>>= 4;
            log += 4;
        }
        if (n >= 4) {
            n >>>= 2;
            log += 2;
        }
        return log + (n >>> 1);
    }
}
