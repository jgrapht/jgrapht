package org.jgrapht.alg.matching.blossom.v5;

import static org.jgrapht.alg.matching.blossom.v5.Options.DualUpdateStrategy.MULTIPLE_TREE_CONNECTED_COMPONENTS;
import static org.jgrapht.alg.matching.blossom.v5.Options.DualUpdateStrategy.MULTIPLE_TREE_FIXED_DELTA;
import static org.jgrapht.alg.matching.blossom.v5.Options.InitializationType.GREEDY;
import static org.jgrapht.alg.matching.blossom.v5.Options.InitializationType.NONE;

/**
 * Options that define the strategies to use during the algorithm for updating duals and initializing the matching
 */
public class Options {
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
    private static final boolean DEFAULT_UPDATE_DUALS_BEFORE = false;
    private static final boolean DEFAULT_UPDATE_DUALS_AFTER = false;
    private static final DualUpdateStrategy DEFAULT_DUAL_UPDATE_TYPE = MULTIPLE_TREE_FIXED_DELTA;
    private static final InitializationType DEFAULT_INITIALIZATION_TYPE = GREEDY;
    boolean updateDualsBefore;
    boolean updateDualsAfter;
    /**
     * What greedy strategy to use to perform a global dual update
     */
    DualUpdateStrategy dualUpdateStrategy;
    /**
     * What strategy to choose to initialize the matching before the main phase of the algorithm
     */
    InitializationType initializationType;

    /**
     * Constructs a custom options for the algorithm
     *
     * @param dualUpdateStrategy greedy strategy to update dual variables globally
     * @param initializationType strategy for initializing the matching
     * @param updateDualsBefore
     * @param updateDualsAfter
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

    public boolean isUpdateDualsBefore() {
        return updateDualsBefore;
    }

    public boolean isUpdateDualsAfter() {
        return updateDualsAfter;
    }

    public DualUpdateStrategy getDualUpdateStrategy() {
        return dualUpdateStrategy;
    }

    public InitializationType getInitializationType() {
        return initializationType;
    }

    /**
     * Enum for choosing dual update strategy
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

        public abstract String toString();
    }
}
