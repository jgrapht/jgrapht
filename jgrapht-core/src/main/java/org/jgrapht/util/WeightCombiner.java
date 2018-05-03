/*
 * (C) Copyright 2009-2018, by Ilya Razenshteyn and Contributors.
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
 * Binary operator for edge weights. There are some prewritten operators.
 */
public interface WeightCombiner
{
    /**
     * Sum of weights.
     */
    WeightCombiner SUM = (a, b) -> a + b;

    /**
     * Multiplication of weights.
     */
    WeightCombiner MULT = (a, b) -> a * b;

    /**
     * Minimum weight.
     */
    WeightCombiner MIN = Math::min;

    /**
     * Maximum weight.
     */
    WeightCombiner MAX = Math::max;

    /**
     * First weight.
     */
    WeightCombiner FIRST = (a, b) -> a;

    /**
     * Second weight.
     */
    WeightCombiner SECOND = (a, b) -> b;

    /**
     * Combines two weights.
     *
     * @param a first weight
     * @param b second weight
     *
     * @return result of the operator
     */
    double combine(double a, double b);
}

// End WeightCombiner.java
