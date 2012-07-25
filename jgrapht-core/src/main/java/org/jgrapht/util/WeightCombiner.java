/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2009, by Barak Naveh and Contributors.
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
/* -------------------------
 * WeightCombiner.java
 * -------------------------
 * (C) Copyright 2009-2009, by Ilya Razenshteyn
 *
 * Original Author:  Ilya Razenshteyn and Contributors.
 *
 * $Id$
 *
 * Changes
 * -------
 * 02-Feb-2009 : Initial revision (IR);
 *
 */
package org.jgrapht.util;

/**
 * Binary operator for edge weights. There are some prewritten operators.
 */
public interface WeightCombiner
{
    //~ Instance fields --------------------------------------------------------

    /**
     * Sum of weights.
     */
    public WeightCombiner SUM =
        new WeightCombiner() {
            @Override
            public double combine(double a, double b)
            {
                return a + b;
            }
        };

    /**
     * Minimum weight.
     */
    public WeightCombiner MIN =
        new WeightCombiner() {
            @Override
            public double combine(double a, double b)
            {
                return Math.min(a, b);
            }
        };

    /**
     * Maximum weight.
     */
    public WeightCombiner MAX =
        new WeightCombiner() {
            @Override
            public double combine(double a, double b)
            {
                return Math.max(a, b);
            }
        };

    /**
     * First weight.
     */
    public WeightCombiner FIRST =
        new WeightCombiner() {
            @Override
            public double combine(double a, double b)
            {
                return a;
            }
        };

    /**
     * Second weight.
     */
    public WeightCombiner SECOND =
        new WeightCombiner() {
            @Override
            public double combine(double a, double b)
            {
                return b;
            }
        };

    //~ Methods ----------------------------------------------------------------

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
