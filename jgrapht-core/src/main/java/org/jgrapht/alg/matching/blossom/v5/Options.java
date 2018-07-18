package org.jgrapht.alg.matching.blossom.v5;

import static org.jgrapht.alg.matching.blossom.v5.Options.DualUpdateStrategy.MULTIPLE_TREE_CONNECTED_COMPONENTS;
import static org.jgrapht.alg.matching.blossom.v5.Options.DualUpdateStrategy.MULTIPLE_TREE_FIXED_DELTA;
import static org.jgrapht.alg.matching.blossom.v5.Options.InitializationType.GREEDY;
import static org.jgrapht.alg.matching.blossom.v5.Options.InitializationType.NONE;

/**
 * Options that define the strategies to use during the algorithm for updating duals and initializing the matching
 */
public class Options {
    /**
     * All possible options
     */
    public static final Options[] ALL_OPTIONS = new Options[]{
            new Options(NONE, MULTIPLE_TREE_CONNECTED_COMPONENTS, true, true), //[0]
            new Options(NONE, MULTIPLE_TREE_CONNECTED_COMPONENTS, true, false), //[1]
            new Options(NONE, MULTIPLE_TREE_CONNECTED_COMPONENTS, false, true), //[2]
            new Options(NONE, MULTIPLE_TREE_CONNECTED_COMPONENTS, false, false), //[3]
            new Options(NONE, MULTIPLE_TREE_FIXED_DELTA, true, true), //[4]
            new Options(NONE, MULTIPLE_TREE_FIXED_DELTA, true, false), //[5]
            new Options(NONE, MULTIPLE_TREE_FIXED_DELTA, false, true), //[6]
            new Options(NONE, MULTIPLE_TREE_FIXED_DELTA, false, false), //[7]
            new Options(GREEDY, MULTIPLE_TREE_CONNECTED_COMPONENTS, true, true), //[8]
            new Options(GREEDY, MULTIPLE_TREE_CONNECTED_COMPONENTS, true, false), //[9]
            new Options(GREEDY, MULTIPLE_TREE_CONNECTED_COMPONENTS, false, true), //[10]
            new Options(GREEDY, MULTIPLE_TREE_CONNECTED_COMPONENTS, false, false), //[11]
            new Options(GREEDY, MULTIPLE_TREE_FIXED_DELTA, true, true), //[12]
            new Options(GREEDY, MULTIPLE_TREE_FIXED_DELTA, true, false), //[13]
            new Options(GREEDY, MULTIPLE_TREE_FIXED_DELTA, false, true), //[14]
            new Options(GREEDY, MULTIPLE_TREE_FIXED_DELTA, false, true), //[15]
    };
    /**
     * Default algorithm initialization type
     */
    private static final InitializationType DEFAULT_INITIALIZATION_TYPE = GREEDY;
    /**
     * Default dual updates strategy
     */
    private static final DualUpdateStrategy DEFAULT_DUAL_UPDATE_TYPE = MULTIPLE_TREE_FIXED_DELTA;
    /**
     * Default value for the flag {@link Options#updateDualsBefore}
     */
    private static final boolean DEFAULT_UPDATE_DUALS_BEFORE = true;
    /**
     * Default value for the flag {@link Options#updateDualsAfter}
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
     * Constructs a custom options for the algorithm
     *
     * @param dualUpdateStrategy greedy strategy to update dual variables globally
     * @param initializationType strategy for initializing the matching
     * @param updateDualsBefore  whether to update duals of the tree before growth
     * @param updateDualsAfter   whether to update duals of the tree after growth
     */
    public Options(InitializationType initializationType, DualUpdateStrategy dualUpdateStrategy, boolean updateDualsBefore, boolean updateDualsAfter) {
        this.dualUpdateStrategy = dualUpdateStrategy;
        this.initializationType = initializationType;
        this.updateDualsBefore = updateDualsBefore;
        this.updateDualsAfter = updateDualsAfter;
    }

    /**
     * Construct a new options instance with a {@code initializationType}
     *
     * @param initializationType defines a strategy to use to initialize the matching
     */
    public Options(InitializationType initializationType) {
        this(initializationType, DEFAULT_DUAL_UPDATE_TYPE, DEFAULT_UPDATE_DUALS_BEFORE, DEFAULT_UPDATE_DUALS_AFTER);
    }

    /**
     * Construct a default options for the algorithm
     */

    public Options() {
        this(DEFAULT_INITIALIZATION_TYPE, DEFAULT_DUAL_UPDATE_TYPE, DEFAULT_UPDATE_DUALS_BEFORE, DEFAULT_UPDATE_DUALS_AFTER);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Options{");
        sb.append("initializationType=").append(initializationType);
        sb.append(", dualUpdateStrategy=").append(dualUpdateStrategy);
        sb.append(",updateDualsBefore=").append(updateDualsBefore);
        sb.append(", updateDualsAfter=").append(updateDualsAfter);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Getter for {@link Options#updateDualsBefore} flag
     *
     * @return the flag {@link Options#updateDualsBefore}
     */
    public boolean isUpdateDualsBefore() {
        return updateDualsBefore;
    }

    /**
     * Getter for {@link Options#updateDualsAfter} flag
     *
     * @return the flag {@link Options#updateDualsAfter}
     */
    public boolean isUpdateDualsAfter() {
        return updateDualsAfter;
    }

    /**
     * Returns dual updates strategy
     *
     * @return dual updates strategy
     */
    public DualUpdateStrategy getDualUpdateStrategy() {
        return dualUpdateStrategy;
    }

    /**
     * Returns initialization type
     *
     * @return initialization type
     */
    public InitializationType getInitializationType() {
        return initializationType;
    }

    /**
     * Enum for choosing dual updates strategy
     */
    public enum DualUpdateStrategy {
        MULTIPLE_TREE_FIXED_DELTA {
            @Override
            public String toString() {
                return "Multiple tree fixed delta";
            }
        },
        MULTIPLE_TREE_CONNECTED_COMPONENTS {
            @Override
            public String toString() {
                return "Multiple tree connected components";
            }
        };

        /**
         * Returns the name of the dual updates strategy
         *
         * @return the name of the dual updates strategy
         */
        public abstract String toString();
    }

    /**
     * Enum for types of matching initialization
     */
    public enum InitializationType {
        GREEDY {
            @Override
            public String toString() {
                return "Greedy initialization";
            }
        }, NONE {
            @Override
            public String toString() {
                return "None";
            }
        };

        /**
         * Returns the name of the initialization type
         *
         * @return the name of the initialization type
         */
        public abstract String toString();
    }
}
