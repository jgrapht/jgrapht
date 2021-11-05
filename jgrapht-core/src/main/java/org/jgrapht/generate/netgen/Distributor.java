/*
 * (C) Copyright 2020-2021, by Timofey Chudakov and Contributors.
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
package org.jgrapht.generate.netgen;

import org.jgrapht.alg.util.Pair;

import java.util.*;
import java.util.function.Function;

/**
 * Distributes value units among keys given lower and upper bound constraints.
 * <p>
 * Let's define a set of elements $\{k_1, k_2, \dots, k_n\}$. For every element a set of lower
 * bounds $\{l_1, l_2, \dots, l_t\}$ and upper bounds $\{u_1, u_2, \dots, u_p\}$ is specified. The
 * problem is to randomly distribute a number of abstract value units $V$ among keys such that the
 * lower bound and upper bound constraints are satisfied. This class solves this problem.
 *
 * @param <K> the element type.
 * @author Timofey Chudakov
 * @see NetworkGenerator
 */
public class Distributor<K>
{
    /**
     * Random number generator used by this distributor.
     */
    private final Random rng;
    /**
     * Lower bounds.
     */
    private final List<Function<K, Integer>> lowerBounds;
    /**
     * Upper bounds.
     */
    private final List<Function<K, Integer>> upperBounds;

    /**
     * Creates a Distributor using random seed.
     */
    public Distributor()
    {
        this(System.nanoTime());
    }

    /**
     * Creates a distributor using the specified {@code seed}.
     *
     * @param seed the seed for the random number generator.
     */
    public Distributor(long seed)
    {
        this(new Random(seed));
    }

    /**
     * Creates a distributor which uses the random number generatow {@code rng}.
     *
     * @param rng a random number generator to use.
     */
    public Distributor(Random rng)
    {
        this.rng = rng;
        this.lowerBounds = new ArrayList<>();
        this.upperBounds = new ArrayList<>();
    }

    /**
     * Adds an upper bounding function. This function must be defined for all keys.
     *
     * @param upperBound an upper bound function.
     */
    public void addUpperBound(Function<K, Integer> upperBound)
    {
        this.upperBounds.add(upperBound);
    }

    /**
     * Adds a lower bound function. This function must be defined for all keys.
     *
     * @param lowerBound a lower bound function.
     */
    public void addLowerBound(Function<K, Integer> lowerBound)
    {
        this.lowerBounds.add(lowerBound);
    }

    /**
     * Finds a maximum lower bound for every key.
     *
     * @param keys list of keys.
     * @return the computed key lower bounds.
     */
    private List<Integer> computeLowerBounds(List<K> keys)
    {
        List<Integer> keyLowerBounds = new ArrayList<>(keys.size());
        for (K key : keys) {
            int lowerBound = 0;
            for (Function<K, Integer> lowerBoundFunction : lowerBounds) {
                lowerBound = Math.max(lowerBound, lowerBoundFunction.apply(key));
            }
            keyLowerBounds.add(lowerBound);
        }

        return keyLowerBounds;
    }

    /**
     * Finds a minimum lower bound for every key.
     *
     * @param keys a list of keys.
     * @return the computed key upper bound.
     */
    private List<Integer> computeUpperBounds(List<K> keys)
    {
        List<Integer> keyUpperBounds = new ArrayList<>(keys.size());
        for (K key : keys) {
            int upperBound = Integer.MAX_VALUE;
            for (Function<K, Integer> upperBoundFunction : upperBounds) {
                upperBound = Math.min(upperBound, upperBoundFunction.apply(key));
            }
            keyUpperBounds.add(upperBound);
        }

        return keyUpperBounds;
    }

    /**
     * Computes a suffix sum of the {@code bounds}. Returns computed suffix sum and the sum of all
     * elements in the {@code bounds list}.
     *
     * @param bounds list of integers.
     * @return computed pair of suffix sum list and a sum of all elements.
     */
    private Pair<List<Integer>, Long> computeSuffixSum(List<Integer> bounds)
    {
        List<Integer> suffixSum = new ArrayList<>(Collections.nCopies(bounds.size(), 0));
        long sum = 0;
        for (int i = bounds.size() - 1; i >= 0; i--) {
            suffixSum.set(i, (int) Math.min(Integer.MAX_VALUE, sum));
            sum += bounds.get(i);
        }

        return Pair.of(suffixSum, sum);
    }

    /**
     * Computes and returns a value distribution for the list of keys. The resulting distribution
     * will satisfy the (possibly empty) sets of lower and upper bound constraints. Distributed
     * values will be in the same order as the keys in the key list.
     *
     * @param keys the list of keys.
     * @param valueNum the number of abstract value units to distribute.
     * @return the computed value distribution.
     */
    public List<Integer> getDistribution(List<K> keys, final int valueNum)
    {
        List<Integer> keyLowerBounds = computeLowerBounds(keys);
        List<Integer> keyUpperBounds = computeUpperBounds(keys);

        Pair<List<Integer>, Long> lbSufSumP = computeSuffixSum(keyLowerBounds);
        Pair<List<Integer>, Long> ubSufSumP = computeSuffixSum(keyUpperBounds);

        List<Integer> lbSufSum = lbSufSumP.getFirst();
        List<Integer> ubSufSum = ubSufSumP.getFirst();

        long lbSum = lbSufSumP.getSecond();
        long ubSum = ubSufSumP.getSecond();

        if (lbSum > valueNum) {
            throw new IllegalArgumentException(
                "Can't distribute values among keys: the sum of lower bounds is greater than the number of values");
        } else if (ubSum < valueNum) {
            throw new IllegalArgumentException(
                "Can't distribute values among keys: the sum of upper bounds is smaller than the number of values");
        }

        int remainingValues = valueNum;
        List<Integer> resultingDistribution = new ArrayList<>();
        for (int i = 0; i < keyLowerBounds.size(); i++) {
            int lowerBound = keyLowerBounds.get(i);
            int upperBound = keyUpperBounds.get(i);

            int valueNumUpperBound = remainingValues - lbSufSum.get(i);
            int valueNumLowerBound = remainingValues - ubSufSum.get(i);

            lowerBound = Math.max(lowerBound, valueNumLowerBound);
            upperBound = Math.min(upperBound, valueNumUpperBound);

            if (lowerBound > upperBound) {
                throw new IllegalArgumentException(
                    "Infeasible bound specified for the key: " + keys.get(i));
            }

            int allocatedValues = rng.nextInt(upperBound - lowerBound + 1) + lowerBound;
            resultingDistribution.add(allocatedValues);
            remainingValues -= allocatedValues;
        }

        return resultingDistribution;
    }

}
