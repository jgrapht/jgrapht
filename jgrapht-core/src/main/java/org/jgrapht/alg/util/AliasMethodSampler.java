/*
 * (C) Copyright 2017-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.util;

import java.util.*;

/**
 * The alias method for sampling from a discrete probability distribution.
 * 
 * <p>
 * The implementation is described in the paper: Michael D. Vose. A Linear Algorithm for Generating
 * Random Numbers with a Given Distribution. IEEE Transactions on Software Engineering,
 * 17(9):972--975, 1991.
 * 
 * <p>
 * Initialization takes $O(n)$ where $n$ is the number of items. Sampling takes $O(1)$.
 *
 * @author Dimitrios Michail
 */
public class AliasMethodSampler
{
    private final Random rng;
    private Comparator<Double> comparator;

    private final double[] prob;
    private final int[] alias;

    /**
     * Constructor
     * 
     * @param p the probability distribution where position i of the array is $Prob(X=i)$
     * @throws IllegalArgumentException in case of a non-valid probability distribution
     */
    public AliasMethodSampler(double[] p)
    {
        this(p, new Random(), ToleranceDoubleComparator.DEFAULT_EPSILON);
    }

    /**
     * Constructor
     * 
     * @param p the probability distribution where position $i$ of the array is $Prob(X=i)$
     * @param seed seed to use for the random number generator
     */
    public AliasMethodSampler(double[] p, long seed)
    {
        this(p, new Random(seed), ToleranceDoubleComparator.DEFAULT_EPSILON);
    }

    /**
     * Constructor
     * 
     * @param p the probability distribution where position $i$ of the array is $Prob(X=i)$
     * @param rng the random number generator
     * @throws IllegalArgumentException in case of a non-valid probability distribution
     */
    public AliasMethodSampler(double[] p, Random rng)
    {
        this(p, rng, ToleranceDoubleComparator.DEFAULT_EPSILON);
    }

    /**
     * Constructor
     * 
     * @param p the probability distribution where position $i$ of the array is $Prob(X=i)$
     * @param rng the random number generator
     * @param epsilon tolerance used when comparing floating-point values
     * @throws IllegalArgumentException in case of a non-valid probability distribution
     */
    public AliasMethodSampler(double[] p, Random rng, double epsilon)
    {
        this.rng = Objects.requireNonNull(rng, "Random number generator cannot be null");
        this.comparator = new ToleranceDoubleComparator(epsilon);

        if (p == null || p.length < 1) {
            throw new IllegalArgumentException("Probabilities cannot be empty");
        }
        double sum = 0d;
        for (int i = 0; i < p.length; i++) {
            if (comparator.compare(p[i], 0d) < 0) {
                throw new IllegalArgumentException("Non valid probability distribution");
            }
            sum += p[i];
        }
        if (comparator.compare(sum, 1d) != 0) {
            throw new IllegalArgumentException("Non valid probability distribution");
        }

        /*
         * Initialize large and small
         */
        int n = p.length;
        int[] large = new int[n];
        int[] small = new int[n];
        double threshold = 1d / n;

        int l = 0, s = 0;
        for (int j = 0; j < n; j++) {
            if (comparator.compare(p[j], threshold) > 0) {
                large[l++] = j;
            } else {
                small[s++] = j;
            }
        }

        /*
         * Compute probability and alias
         */
        this.prob = new double[n];
        this.alias = new int[n];
        while (s != 0 && l != 0) {
            int j = small[--s];
            int k = large[--l];

            prob[j] = n * p[j];
            alias[j] = k;
            p[k] += p[j] - threshold;
            if (comparator.compare(p[k], threshold) > 0) {
                large[l++] = k;
            } else {
                small[s++] = k;
            }
        }

        while (s > 0) {
            prob[small[--s]] = 1d;
        }

        while (l > 0) {
            prob[large[--l]] = 1d;
        }
    }

    /**
     * Sample a value from the distribution.
     * 
     * @return a sample from the distribution
     */
    public int next()
    {
        double u = rng.nextDouble() * prob.length;
        int j = (int) Math.floor(u);
        if (comparator.compare(u - j, prob[j]) <= 0) {
            return j;
        } else {
            return alias[j];
        }
    }

}
