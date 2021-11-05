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

/**
 * Builder class for the {@link NetworkGeneratorConfig}. This class perform all the necessary
 * parameter validation and provides meaningful error messages. For the network parameter
 * description and a complete list of parameter constrants, see {@link NetworkGeneratorConfig}. Use
 * this class to construct instances of the {@link NetworkGeneratorConfig}.
 *
 * @author Timofey Chudakov
 * @see NetworkGenerator
 * @see NetworkGeneratorConfig
 */
public class NetworkGeneratorConfigBuilder
{
    int nodeNum = 0;
    int arcNum = 0;
    int sourceNum = 0;
    int sinkNum = 0;
    int tSourceNum = 0;
    int tSinkNum = 0;
    int totalSupply = 0;
    int minCap = 0;
    int maxCap = 0;
    int minCost = 0;
    int maxCost = 0;
    int percentCapacitated = 100;
    int percentWithInfCost = 0;

    /**
     * Builds the {@link NetworkGeneratorConfig}. This method performs remaining parameter
     * validation.
     *
     * @return the constructed {@link NetworkGeneratorConfig}.
     */
    public NetworkGeneratorConfig build()
    {
        if (nodeNum <= 0) {
            invalidParam("Number of nodes must be positive");
        } else if (arcNum <= 0) {
            invalidParam("Number of arcs must be positive");
        } else if (sourceNum <= 0) {
            invalidParam("Number of sources must be positive");
        } else if (sinkNum <= 0) {
            invalidParam("Number of sinks must be positive");
        } else if (sourceNum + sinkNum > nodeNum) {
            invalidParam("Number of sources and sinks must not exceed the number of nodes");
        } else if (tSourceNum > sourceNum) {
            invalidParam(
                "Number of transhipment sources must not exceed the overall number of sources");
        } else if (tSinkNum > sinkNum) {
            invalidParam(
                "Number of transhipment sinks must not exceed the overall number of sinks");
        } else if (totalSupply < Math.max(sourceNum, sinkNum)) {
            invalidParam(
                "Total supply must not be less than the number of sources and the number of sinks");
        } else if (minCap > maxCap) {
            invalidParam("Minimum capacity must not exceed the maximum capacity");
        } else if (minCap <= 0) {
            invalidParam("Minimum capacity must be positive");
        } else if (minCost > maxCost) {
            invalidParam("Minimum cost must not exceed the maximum cost");
        }
        int tNodeNum = nodeNum - sourceNum - sinkNum;
        long minArcNum = NetworkGeneratorConfig.getMinimumArcNum(sourceNum, tNodeNum, sinkNum);
        long maxArcNum = NetworkGeneratorConfig
            .getMaximumArcNum(sourceNum, tSourceNum, tNodeNum, tSinkNum, sinkNum);

        if (arcNum < minArcNum) {
            invalidParam("Too few arcs to generate a valid problem");
        } else if (arcNum > maxArcNum) {
            invalidParam("Too many arcs to generate a valid problem");
        }
        return new NetworkGeneratorConfig(
            nodeNum, arcNum, sourceNum, sinkNum, tSourceNum, tSinkNum, totalSupply, minCap, maxCap,
            minCost, maxCost, percentCapacitated, percentWithInfCost);
    }

    /**
     * Throws {@code IllegalArgumentException} with the specified {@code message}.
     *
     * @param message a message for the exception.
     */
    private void invalidParam(String message)
    {
        throw new IllegalArgumentException(message);
    }

    /**
     * Perform node parameter validation.
     *
     * @param value the value of a node parameter.
     * @return {@code value}
     */
    private int checkNodeConstraint(int value)
    {
        if (value > NetworkGenerator.MAX_NODE_NUM) {
            invalidParam(
                String.format("Number of nodes must not exceed %d", NetworkGenerator.MAX_NODE_NUM));
        }
        return value;
    }

    /**
     * Performs capacity and cost parameter valiation.
     *
     * @param value the value of the capacity or cost parameter
     * @return {@code value}
     */
    private int checkCapacityCostConstraint(int value)
    {
        if (Math.abs(value) > NetworkGenerator.CAPACITY_COST_BOUND) {
            invalidParam(
                String
                    .format(
                        "Arcs capacities and cost must be between -%d and %d",
                        NetworkGenerator.CAPACITY_COST_BOUND,
                        NetworkGenerator.CAPACITY_COST_BOUND));
        }
        return value;
    }

    /**
     * Sets all the network parameters.
     *
     * @param nodeNum number of nodes in the network
     * @param arcNum number of arcs in the network
     * @param sourceNum number of sources in the network
     * @param sinkNum number of sinks in the network
     * @param transshipSourceNum number of transshipment sources in the network
     * @param transshipSinkNum number of transshipment sinks in the network
     * @param totalSupply total supply of the network
     * @param minCap arc capacity lower bound
     * @param maxCap arc capacity upper bound
     * @param minCost arc cost lower bound
     * @param maxCost arc cost upper bound
     * @param percentCapacitated percent of arcs to have finite capacity
     * @param percentWithInfCost percent of arcs to have infinite cost
     * @return this object
     */
    public NetworkGeneratorConfigBuilder setParams(
        int nodeNum, int arcNum, int sourceNum, int sinkNum, int transshipSourceNum,
        int transshipSinkNum, int totalSupply, int minCap, int maxCap, int minCost, int maxCost,
        int percentCapacitated, int percentWithInfCost)
    {
        setNodeNum(nodeNum);
        setArcNum(arcNum);
        setSourceNum(sourceNum);
        setSinkNum(sinkNum);
        setTSourceNum(transshipSourceNum);
        setTSinkNum(transshipSinkNum);
        setTotalSupply(totalSupply);
        setMinCap(minCap);
        setMaxCap(maxCap);
        setMinCost(minCost);
        setMaxCost(maxCost);
        setPercentCapacitated(percentCapacitated);
        setPercentWithInfCost(percentWithInfCost);
        return this;
    }

    /**
     * Sets maximum flow network parameter subset. The values of minCap and maxCap are set to 1, the
     * values of {@code sourceNum} and {@code sinkNum} are set to 1 and the value of the
     * {@code percentCapacitated} is set to 100.
     *
     * @param nodeNum number of nodes in the network
     * @param arcNum number of arcs in the network
     * @param supply total supply of the network
     * @return this object
     */
    public NetworkGeneratorConfigBuilder setMaximumFlowProblemParams(
        int nodeNum, int arcNum, int supply)
    {
        setMaximumFlowProblemParams(nodeNum, arcNum, supply, 1, 1);
        return this;
    }

    /**
     * Sets maximum flow network parameter subset. The values of {@code sourceNum} and
     * {@code sinkNum} are set to 1 and the value of the {@code percentCapacitated} is set to 100.
     *
     * @param nodeNum number of nodes in the network
     * @param arcNum number of arcs in the network
     * @param supply total supply of the network
     * @param minCap arc capacity lower bound
     * @param maxCap arc capacity upper bound
     * @return this object
     */
    public NetworkGeneratorConfigBuilder setMaximumFlowProblemParams(
        int nodeNum, int arcNum, int supply, int minCap, int maxCap)
    {
        setMaximumFlowProblemParams(nodeNum, arcNum, supply, minCap, maxCap, 1, 1);
        return this;
    }

    /**
     * Sets maximum flow network parameter subset. The value of the {@code percentCapacitated} is
     * set to 100.
     *
     * @param nodeNum number of nodes in the network
     * @param arcNum number of arcs in the network
     * @param supply total supply of the network
     * @param minCap arc capacity lower bound
     * @param maxCap arc capacity upper bound
     * @param sourceNum number of source in the network
     * @param sinkNum number of sinks in the network
     * @return this object
     */
    public NetworkGeneratorConfigBuilder setMaximumFlowProblemParams(
        int nodeNum, int arcNum, int supply, int minCap, int maxCap, int sourceNum, int sinkNum)
    {
        setMaximumFlowProblemParams(
            nodeNum, arcNum, supply, minCap, maxCap, sourceNum, sinkNum, 100);
        return this;
    }

    /**
     * Sets maximum flow network parameter subset.
     *
     * @param nodeNum number of nodes in the network
     * @param arcNum number of arcs in the network
     * @param supply total supply of the network
     * @param minCap arc capacity lower bound
     * @param maxCap arc capacity upper bound
     * @param sourceNum number of source in the network
     * @param sinkNum number of sinks in the network
     * @param percentCapacitated percent of arcs to have finite capacity
     * @return this object
     */
    public NetworkGeneratorConfigBuilder setMaximumFlowProblemParams(
        int nodeNum, int arcNum, int supply, int minCap, int maxCap, int sourceNum, int sinkNum,
        int percentCapacitated)
    {
        setParams(
            nodeNum, arcNum, sourceNum, sinkNum, 0, 0, supply, minCap, maxCap, 1, 1,
            percentCapacitated, 0);
        return this;
    }

    /**
     * Sets bipartite matching parameter subset. The values of the {@code minCost} and
     * {@code maxCost} are set to 1, the value of the {@code percentWithInfCost} is set to 0.
     *
     * @param nodeNum number of nodes in the network
     * @param arcNum number of arcs in the network
     * @return this object
     */
    public NetworkGeneratorConfigBuilder setBipartiteMatchingProblemParams(int nodeNum, int arcNum)
    {
        setBipartiteMatchingProblemParams(nodeNum, arcNum, 1, 1);
        return this;
    }

    /**
     * Sets bipartite matching parameter subset. The value of the {@code percentWithInfCost} is set
     * to 0.
     *
     * @param nodeNum number of nodes in the network
     * @param arcNum number of arcs in the network
     * @param minCost arc cost lower bound
     * @param maxCost arc cost upper bound
     * @return this object
     */
    public NetworkGeneratorConfigBuilder setBipartiteMatchingProblemParams(
        int nodeNum, int arcNum, int minCost, int maxCost)
    {
        setBipartiteMatchingProblemParams(nodeNum, arcNum, minCost, maxCost, 0);
        return this;
    }

    /**
     * Sets bipartite matching parameter subset.
     *
     * @param nodeNum number of nodes in the network
     * @param arcNum number of arcs in the network
     * @param minCost arc cost lower bound
     * @param maxCost arc cost upper bound
     * @param percentWithInfCost percent of arcs to have infinite cost
     * @return this object
     */
    public NetworkGeneratorConfigBuilder setBipartiteMatchingProblemParams(
        int nodeNum, int arcNum, int minCost, int maxCost, int percentWithInfCost)
    {
        if ((nodeNum & 1) != 0) {
            invalidParam("Assignment problem must have even number of nodes");
        }
        setParams(
            nodeNum, arcNum, nodeNum / 2, nodeNum / 2, 0, 0, nodeNum / 2, 1, 1, minCost, maxCost,
            100, percentWithInfCost);
        return this;
    }

    /**
     * Sets the number of nodes in the network.
     *
     * @param nodeNum the number of nodes in the network.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setNodeNum(int nodeNum)
    {
        if (nodeNum <= 0) {
            invalidParam("Number of nodes must be positive");
        }
        this.nodeNum = checkNodeConstraint(nodeNum);
        return this;
    }

    /**
     * Sets the number of arcs in the network.
     *
     * @param arcNum the number of arcs in the network.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setArcNum(int arcNum)
    {
        if (arcNum > NetworkGenerator.MAX_ARC_NUM) {
            invalidParam(String.format("Number of arcs must not exceed %d", arcNum));
        }
        this.arcNum = arcNum;
        return this;
    }

    /**
     * Sets the number of sources in the network.
     *
     * @param sourceNum the number of sources in the network.
     * @return this object
     */
    public NetworkGeneratorConfigBuilder setSourceNum(int sourceNum)
    {
        if (sourceNum <= 0) {
            invalidParam("Number of sources must be positive");
        }
        this.sourceNum = checkNodeConstraint(sourceNum);
        return this;
    }

    /**
     * Sets the number of sinks in the network.
     *
     * @param sinkNum the number of sinks in the network.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setSinkNum(int sinkNum)
    {
        if (sinkNum <= 0) {
            invalidParam("Number of sinks must be positive");
        }
        this.sinkNum = checkNodeConstraint(sinkNum);
        return this;
    }

    /**
     * Sets the number of transshipment sources in the network.
     *
     * @param tSourceNum the number of transshipment sources in the network.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setTSourceNum(int tSourceNum)
    {
        if (tSourceNum < 0) {
            invalidParam("Number of transshipment sources must be non-negative");
        }
        this.tSourceNum = checkNodeConstraint(tSourceNum);
        return this;
    }

    /**
     * Sets the number of transshipment sinks in the network.
     *
     * @param tSinkNum the number of transshipment sinks in the network.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setTSinkNum(int tSinkNum)
    {
        if (tSinkNum < 0) {
            invalidParam("Number of transshipment sinks must be non-negative");
        }
        this.tSinkNum = checkNodeConstraint(tSinkNum);
        return this;
    }

    /**
     * Sets the total supply of the network.
     *
     * @param totalSupply the total supply of the network.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setTotalSupply(int totalSupply)
    {
        if (totalSupply > NetworkGenerator.MAX_SUPPLY) {
            invalidParam(
                String.format("Total supply must not exceed %d", NetworkGenerator.MAX_NODE_NUM));
        }
        this.totalSupply = totalSupply;
        return this;
    }

    /**
     * Sets the arc capacity lower bound.
     *
     * @param minCap the arc capacity lower bound.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setMinCap(int minCap)
    {
        if (minCap < 0) {
            invalidParam("Minimum arc capacity must be non-negative");
        }
        this.minCap = checkCapacityCostConstraint(minCap);
        return this;
    }

    /**
     * Sets the arc capacity upper bound.
     *
     * @param maxCap the arc capacity upper bound.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setMaxCap(int maxCap)
    {
        if (maxCap < 0) {
            invalidParam("Maximum arc capacity must be non-negative");
        }
        this.maxCap = checkCapacityCostConstraint(maxCap);
        return this;
    }

    /**
     * Sets the arc cost lower bound.
     *
     * @param minCost the arc cost lower bound.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setMinCost(int minCost)
    {
        this.minCost = checkCapacityCostConstraint(minCost);
        return this;
    }

    /**
     * Sets the arc cost upper bound.
     *
     * @param maxCost the arc cost upper bound.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setMaxCost(int maxCost)
    {
        this.maxCost = checkCapacityCostConstraint(maxCost);
        return this;
    }

    /**
     * Sets the percent of arcs to have finite capacity.
     *
     * @param percentCapacitated the percent of arcs to have finite capacity.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setPercentCapacitated(int percentCapacitated)
    {
        if (percentCapacitated < 0 || percentCapacitated > 100) {
            invalidParam("Percent of capacitated arcs must be between 0 and 100 inclusive");
        }
        this.percentCapacitated = percentCapacitated;
        return this;
    }

    /**
     * Sets the percent of arcs to have infinite cost.
     *
     * @param percentWithInfCost the percent of arcs to have infinite cost.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setPercentWithInfCost(int percentWithInfCost)
    {
        if (percentWithInfCost < 0 || percentWithInfCost > 100) {
            invalidParam("Percent of arcs with infinite cost must be between 0 and 100 inclusive");
        }
        this.percentWithInfCost = percentWithInfCost;
        return this;
    }
}
