/*
 * (C) Copyright 2018-2018, by Timofey Chudakov and Contributors.
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
package org.jgrapht.alg.matching.blossom.v5;

import static org.jgrapht.alg.matching.blossom.v5.BlossomVOptions.DualUpdateStrategy.MULTIPLE_TREE_CONNECTED_COMPONENTS;
import static org.jgrapht.alg.matching.blossom.v5.BlossomVOptions.DualUpdateStrategy.MULTIPLE_TREE_FIXED_DELTA;
import static org.jgrapht.alg.matching.blossom.v5.BlossomVOptions.InitializationType.*;

/**
 * BlossomVOptions that define the strategies to use during the algorithm for updating duals and
 * initializing the matching
 * <p>
 * According to the experimental results, the greedy initialization substantially speeds up the
 * algorithm.
 */
public class BlossomVOptions
{
    /**
     * All possible options
     */
    public static final BlossomVOptions[] ALL_OPTIONS = new BlossomVOptions[] {
        new BlossomVOptions(NONE, MULTIPLE_TREE_CONNECTED_COMPONENTS, true, true), // [0]
        new BlossomVOptions(NONE, MULTIPLE_TREE_CONNECTED_COMPONENTS, true, false), // [1]
        new BlossomVOptions(NONE, MULTIPLE_TREE_CONNECTED_COMPONENTS, false, true), // [2]
        new BlossomVOptions(NONE, MULTIPLE_TREE_CONNECTED_COMPONENTS, false, false), // [3]
        new BlossomVOptions(NONE, MULTIPLE_TREE_FIXED_DELTA, true, true), // [4]
        new BlossomVOptions(NONE, MULTIPLE_TREE_FIXED_DELTA, true, false), // [5]
        new BlossomVOptions(NONE, MULTIPLE_TREE_FIXED_DELTA, false, true), // [6]
        new BlossomVOptions(NONE, MULTIPLE_TREE_FIXED_DELTA, false, false), // [7]
        new BlossomVOptions(GREEDY, MULTIPLE_TREE_CONNECTED_COMPONENTS, true, true), // [8]
        new BlossomVOptions(GREEDY, MULTIPLE_TREE_CONNECTED_COMPONENTS, true, false), // [9]
        new BlossomVOptions(GREEDY, MULTIPLE_TREE_CONNECTED_COMPONENTS, false, true), // [10]
        new BlossomVOptions(GREEDY, MULTIPLE_TREE_CONNECTED_COMPONENTS, false, false), // [11]
        new BlossomVOptions(GREEDY, MULTIPLE_TREE_FIXED_DELTA, true, true), // [12]
        new BlossomVOptions(GREEDY, MULTIPLE_TREE_FIXED_DELTA, true, false), // [13]
        new BlossomVOptions(GREEDY, MULTIPLE_TREE_FIXED_DELTA, false, true), // [14]
        new BlossomVOptions(GREEDY, MULTIPLE_TREE_FIXED_DELTA, false, true), // [15]
        new BlossomVOptions(FRACTIONAL, MULTIPLE_TREE_CONNECTED_COMPONENTS, true, true), // [16]
        new BlossomVOptions(FRACTIONAL, MULTIPLE_TREE_CONNECTED_COMPONENTS, true, false), // [17]
        new BlossomVOptions(FRACTIONAL, MULTIPLE_TREE_CONNECTED_COMPONENTS, false, true), // [18]
        new BlossomVOptions(FRACTIONAL, MULTIPLE_TREE_CONNECTED_COMPONENTS, false, false), // [19]
        new BlossomVOptions(FRACTIONAL, MULTIPLE_TREE_FIXED_DELTA, true, true), // [20]
        new BlossomVOptions(FRACTIONAL, MULTIPLE_TREE_FIXED_DELTA, true, false), // [21]
        new BlossomVOptions(FRACTIONAL, MULTIPLE_TREE_FIXED_DELTA, false, true), // [22]
        new BlossomVOptions(FRACTIONAL, MULTIPLE_TREE_FIXED_DELTA, false, true), // [23]
    };
    /**
     * Default algorithm initialization type
     */
    private static final InitializationType DEFAULT_INITIALIZATION_TYPE = FRACTIONAL;
    /**
     * Default dual updates strategy
     */
    private static final DualUpdateStrategy DEFAULT_DUAL_UPDATE_TYPE = MULTIPLE_TREE_FIXED_DELTA;
    /**
     * Default value for the flag {@link BlossomVOptions#updateDualsBefore}
     */
    private static final boolean DEFAULT_UPDATE_DUALS_BEFORE = true;
    /**
     * Default value for the flag {@link BlossomVOptions#updateDualsAfter}
     */
    private static final boolean DEFAULT_UPDATE_DUALS_AFTER = false;
    /**
     * What greedy strategy to use to perform a global dual update
     */
    DualUpdateStrategy dualUpdateStrategy;
    /**
     * What strategy to choose to initialize the matching before the main phase of the algorithm
     */
    InitializationType initializationType;
    /**
     * Whether to update duals of the tree before growth
     */
    boolean updateDualsBefore;
    /**
     * Whether to update duals of the tree after growth
     */
    boolean updateDualsAfter;

    /**
     * Constructs a custom set of options for the algorithm
     *
     * @param dualUpdateStrategy greedy strategy to update dual variables globally
     * @param initializationType strategy for initializing the matching
     * @param updateDualsBefore whether to update duals of the tree before growth
     * @param updateDualsAfter whether to update duals of the tree after growth
     */
    public BlossomVOptions(
        InitializationType initializationType, DualUpdateStrategy dualUpdateStrategy,
        boolean updateDualsBefore, boolean updateDualsAfter)
    {
        this.dualUpdateStrategy = dualUpdateStrategy;
        this.initializationType = initializationType;
        this.updateDualsBefore = updateDualsBefore;
        this.updateDualsAfter = updateDualsAfter;
    }

    /**
     * Constructs a new options instance with a {@code initializationType}
     *
     * @param initializationType defines a strategy to use to initialize the matching
     */
    public BlossomVOptions(InitializationType initializationType)
    {
        this(
            initializationType, DEFAULT_DUAL_UPDATE_TYPE, DEFAULT_UPDATE_DUALS_BEFORE,
            DEFAULT_UPDATE_DUALS_AFTER);
    }

    /**
     * Constructs a default set of options for the algorithm
     */
    public BlossomVOptions()
    {
        this(
            DEFAULT_INITIALIZATION_TYPE, DEFAULT_DUAL_UPDATE_TYPE, DEFAULT_UPDATE_DUALS_BEFORE,
            DEFAULT_UPDATE_DUALS_AFTER);
    }

    @Override
    public String toString()
    {
        return "BlossomVOptions{initializationType=" + initializationType + ", dualUpdateStrategy="
            + dualUpdateStrategy + ", updateDualsBefore=" + updateDualsBefore
            + ", updateDualsAfter=" + updateDualsAfter + '}';
    }

    /**
     * Returns the {@link BlossomVOptions#updateDualsBefore} flag
     *
     * @return the flag {@link BlossomVOptions#updateDualsBefore}
     */
    public boolean isUpdateDualsBefore()
    {
        return updateDualsBefore;
    }

    /**
     * Returns the {@link BlossomVOptions#updateDualsAfter} flag
     *
     * @return the flag {@link BlossomVOptions#updateDualsAfter}
     */
    public boolean isUpdateDualsAfter()
    {
        return updateDualsAfter;
    }

    /**
     * Returns dual updates strategy
     *
     * @return dual updates strategy
     */
    public DualUpdateStrategy getDualUpdateStrategy()
    {
        return dualUpdateStrategy;
    }

    /**
     * Returns initialization type
     *
     * @return initialization type
     */
    public InitializationType getInitializationType()
    {
        return initializationType;
    }

    /**
     * Enum for choosing dual updates strategy
     */
    public enum DualUpdateStrategy
    {
        MULTIPLE_TREE_FIXED_DELTA
        {
            @Override
            public String toString()
            {
                return "Multiple tree fixed delta";
            }
        },
        MULTIPLE_TREE_CONNECTED_COMPONENTS
        {
            @Override
            public String toString()
            {
                return "Multiple tree connected components";
            }
        };

        /**
         * Returns the name of the dual updates strategy
         *
         * @return the name of the dual updates strategy
         */
        @Override
        public abstract String toString();
    }

    /**
     * Enum for types of matching initialization
     */
    public enum InitializationType
    {
        GREEDY
        {
            @Override
            public String toString()
            {
                return "Greedy initialization";
            }
        },
        NONE
        {
            @Override
            public String toString()
            {
                return "None";
            }
        },
        FRACTIONAL
        {
            @Override
            public String toString()
            {
                return "Fractional matching initializations";
            }
        };

        /**
         * Returns the name of the initialization type
         *
         * @return the name of the initialization type
         */
        @Override
        public abstract String toString();
    }
}
