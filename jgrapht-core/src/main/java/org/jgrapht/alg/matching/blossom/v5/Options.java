package org.jgrapht.alg.matching.blossom.v5;

import static org.jgrapht.alg.matching.blossom.v5.Options.DualUpdateStrategy.MULTIPLE_TREE_FIXED_DELTA;
import static org.jgrapht.alg.matching.blossom.v5.Options.InitializationType.GREEDY;

/**
 * Options that define the strategies to use during the algorithm for updating duals and initializing the matching
 */
public class Options {
    private static final boolean DEFAULT_UPDATE_DUALS_BEFORE = false;
    private static final boolean DEFAULT_UPDATE_DUALS_AFTER = false;
    private static final DualUpdateStrategy DEFAULT_DUAL_UPDATE_TYPE = MULTIPLE_TREE_FIXED_DELTA;
    private static final InitializationType DEFAULT_INITIALIZATION_TYPE = GREEDY;
    boolean updateDualsBefore;
    boolean updateDualsAfter;

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
    enum DualUpdateStrategy {
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
    enum InitializationType {
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
