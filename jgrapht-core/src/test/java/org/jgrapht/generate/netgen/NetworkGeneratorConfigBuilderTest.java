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

import org.junit.Test;

import static org.jgrapht.generate.netgen.NetworkGenerator.*;

/**
 * Tests for {@link NetworkGeneratorConfigBuilder}
 *
 * @author Timofey Chudakov
 */
public class NetworkGeneratorConfigBuilderTest
{

    private NetworkGeneratorConfigBuilder getBuilder(
        int nodeNum, int arcNum, int sourceNum, int sinkNum, int tSourceNum, int tSinkNum,
        int supply, int minCap, int maxCap, int minCost, int maxCost, int pCapacitated,
        int pWithInfCost)
    {
        return new NetworkGeneratorConfigBuilder()
            .setNodeNum(nodeNum).setArcNum(arcNum).setSourceNum(sourceNum).setSinkNum(sinkNum)
            .setTSourceNum(tSourceNum).setTSinkNum(tSinkNum).setTotalSupply(supply)
            .setMinCap(minCap).setMaxCap(maxCap).setMinCost(minCost).setMaxCost(maxCost)
            .setPercentCapacitated(pCapacitated).setPercentWithInfCost(pWithInfCost);
    }

    private NetworkGeneratorConfigBuilder getAssignmentBuilder()
    {
        return getBuilder(4, 4, 2, 2, 0, 0, 2, 1, 1, 0, 0, 100, 0);

    }

    private NetworkGeneratorConfigBuilder getMinCostFlowBuilder()
    {
        return getBuilder(10, 20, 2, 3, 1, 1, 50, 1, 10, 0, 10, 100, 0);

    }

    // -------------------------- node num tests --------------------------

    @Test(expected = IllegalArgumentException.class)
    public void testNodeNum_NodeNumNotSet_IllegalArgumentException()
    {
        NetworkGeneratorConfig config = new NetworkGeneratorConfigBuilder()
            .setArcNum(20).setSourceNum(2).setSinkNum(3).setTSourceNum(1).setTSinkNum(1)
            .setTotalSupply(50).setMinCap(1).setMaxCap(10).setMinCost(0).setMaxCost(10)
            .setPercentCapacitated(100).setPercentWithInfCost(0).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNodeNum_NegativeNodeNum_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setNodeNum(-1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNodeNum_TooHighNodeNum_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setNodeNum(MAX_NODE_NUM + 1).build();
    }

    // -------------------------- arc num tests --------------------------

    @Test(expected = IllegalArgumentException.class)
    public void testArcNum_ArcNumNotSet_IllegalArgumentException()
    {
        NetworkGeneratorConfig config = new NetworkGeneratorConfigBuilder()
            .setNodeNum(10).setSourceNum(2).setSinkNum(3).setTSourceNum(1).setTSinkNum(1)
            .setTotalSupply(50).setMinCap(1).setMaxCap(10).setMinCost(0).setMaxCost(10)
            .setPercentCapacitated(100).setPercentWithInfCost(0).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArcNum_NegativeArcNum_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setArcNum(-1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArcNum_TooHighArcNum_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setArcNum(MAX_ARC_NUM + 1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArcNum_TooFewArcsInAssignmentProblem_IllegalArgumentException()
    {
        getAssignmentBuilder().setArcNum(1).build();
    }

    @Test
    public void testArcNum_MinimumNumberOfArcsInAssignmentProblem_Ok()
    {
        getAssignmentBuilder().setArcNum(2).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArcNum_TooManyArcsInAssignmentProblem_IllegalArgumentException()
    {
        getAssignmentBuilder().setArcNum(5).build();
    }

    @Test
    public void testArcNum_MaximumNumberOfArcsInAssignmentProblem_Ok()
    {
        getAssignmentBuilder().setArcNum(4).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArcNum_TooFewArcsInMinCostFlowProblem_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setArcNum(7).build();
    }

    @Test
    public void testArcNum_MinimumNumberOfArcsInMinCostFlowProblem_Ok()
    {
        getMinCostFlowBuilder().setArcNum(8).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArcNum_TooManyArcsInMinCostFlowProblem_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setArcNum(9 + 8 + 40 + 8 + 1).build();
    }

    @Test
    public void testArcNum_MaximumNumberOfArcsInMinCostFlowProblem_Ok()
    {
        getMinCostFlowBuilder().setArcNum(9 + 8 + 40 + 8).build();
    }

    // -------------------------- source and sink node num tests --------------------------

    @Test(expected = IllegalArgumentException.class)
    public void testSourceNum_SourceNumNotSet_IllegalArgumentException()
    {
        NetworkGeneratorConfig config = new NetworkGeneratorConfigBuilder()
            .setNodeNum(10).setArcNum(20).setSinkNum(3).setTSourceNum(1).setTSinkNum(1)
            .setTotalSupply(50).setMinCap(1).setMaxCap(10).setMinCost(0).setMaxCost(10)
            .setPercentCapacitated(100).setPercentWithInfCost(0).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSourceNum_NegativeSourceNum_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setSourceNum(-1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSourceNum_SourceNumGreaterThanNodeNum_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setSourceNum(11).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSourceNum_SinkNumNotSet_IllegalArgumentException()
    {
        NetworkGeneratorConfig config = new NetworkGeneratorConfigBuilder()
            .setNodeNum(10).setArcNum(20).setSourceNum(2).setTSourceNum(1).setTSinkNum(1)
            .setTotalSupply(50).setMinCap(1).setMaxCap(10).setMinCost(0).setMaxCost(10)
            .setPercentCapacitated(100).setPercentWithInfCost(0).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSinkNum_NegativeSinkNum_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setSinkNum(-1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSinkNum_SinkNumGreaterThanNodeNum_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setSinkNum(11).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSourceSinkNum_SourceNumPlusSinkNumGreaterThanTheNodeNum_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setSourceNum(5).setSinkNum(6).build();
    }

    // -------------------------- transshipment source and sinks test --------------------------

    @Test(expected = IllegalArgumentException.class)
    public void testTransshipmentSourceNum_NegativeTransshipmentSourceNum_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setTSourceNum(-1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransshipmentSourceNum_TransshipmentSourceNumGreaterThanSourceNum_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setTSourceNum(3).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransshipmentSinkNum_NegativeTransshipmentSinkNum_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setTSinkNum(-1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransshipmentSinkNum_TransshipmentSinkNumGreaterThanSourceNum_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setTSinkNum(4).build();
    }

    // -------------------------- supply tests --------------------------

    @Test(expected = IllegalArgumentException.class)
    public void testSupply_SupplyNotSet_IllegalArgumentException()
    {
        NetworkGeneratorConfig config = new NetworkGeneratorConfigBuilder()
            .setNodeNum(10).setArcNum(20).setSourceNum(2).setSinkNum(3).setTSourceNum(1)
            .setTSinkNum(1).setMinCap(1).setMaxCap(10).setMinCost(0).setMaxCost(10)
            .setPercentCapacitated(100).setPercentWithInfCost(0).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSupply_NegativeSupply_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setTotalSupply(-1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSupply_TooHighSupply_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setTotalSupply(MAX_SUPPLY + 1).build();
    }

    @Test
    public void testSupply_MaximumSupply_Ok()
    {
        getMinCostFlowBuilder().setTotalSupply(MAX_SUPPLY).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSupply_SupplySmallerThanSourceNodeNum_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setTotalSupply(1).build();
    }

    // -------------------------- capacities tests --------------------------

    @Test(expected = IllegalArgumentException.class)
    public void testCapacities_NegativeMinimumCapacity_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setMinCap(-1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCapacities_NegativeMaximumCapacity_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setMaxCap(-1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCapacities_MinimumCapacityNotSet_IllegalArgumentException()
    {
        NetworkGeneratorConfig config = new NetworkGeneratorConfigBuilder()
            .setNodeNum(10).setArcNum(20).setSourceNum(2).setSinkNum(3).setTSourceNum(1)
            .setTSinkNum(1).setTotalSupply(50).setMaxCap(10).setMinCost(0).setMaxCost(10)
            .setPercentCapacitated(100).setPercentWithInfCost(0).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCapacities_MaximumCapacityNotSet_IllegalArgumentException()
    {
        NetworkGeneratorConfig config = new NetworkGeneratorConfigBuilder()
            .setNodeNum(10).setArcNum(20).setSourceNum(2).setSinkNum(3).setTSourceNum(1)
            .setTSinkNum(1).setTotalSupply(50).setMaxCap(10).setMinCost(0).setMaxCost(10)
            .setPercentCapacitated(100).setPercentWithInfCost(0).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCapacities_TooHighMinimumCapacity_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setMinCap(CAPACITY_COST_BOUND + 1);
    }

    @Test
    public void testCapacities_MaximumMinimumCapacity_Ok()
    {
        getMinCostFlowBuilder()
            .setMinCap(CAPACITY_COST_BOUND).setMaxCap(CAPACITY_COST_BOUND).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCapacities_TooHighMaximumCapacity_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setMaxCap(CAPACITY_COST_BOUND + 1);
    }

    @Test
    public void testCapacities_MaximumMaximumCapacity_Ok()
    {
        getMinCostFlowBuilder().setMaxCap(CAPACITY_COST_BOUND).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCapacities_MinimumCapacityGreaterThatMaximumCapacity_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setMinCap(10).setMaxCap(9).build();
    }

    // -------------------------- costs tests --------------------------

    @Test(expected = IllegalArgumentException.class)
    public void testCosts_TooLowMinimumCost_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setMinCost(-CAPACITY_COST_BOUND - 1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCosts_TooLowMaximumCost_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setMaxCost(-CAPACITY_COST_BOUND - 1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCosts_TooHighMinimumCost_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setMinCost(CAPACITY_COST_BOUND + 1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCosts_TooHighMaximumCost_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setMaxCost(CAPACITY_COST_BOUND + 1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCosts_MinimumCostGreaterThanMaximumCost_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setMinCost(10).setMaxCost(9).build();
    }

    // -------------------------- percent capacitated tests --------------------------

    @Test(expected = IllegalArgumentException.class)
    public void testPercentCapacitated_NegativeValue_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setPercentCapacitated(-1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPercentCapacitated_TooHighValue_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setPercentCapacitated(101).build();
    }

    // -------------------------- percent with inf cost tests --------------------------

    @Test(expected = IllegalArgumentException.class)
    public void testPercentWithInfCost_NegativeValue_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setPercentWithInfCost(-1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPercentWithInfCost_TooHighValue_IllegalArgumentException()
    {
        getMinCostFlowBuilder().setPercentWithInfCost(101).build();
    }

    // -------------------------- positive tests --------------------------

    @Test
    public void testAssignmentConfig_Ok()
    {
        getAssignmentBuilder().build();
    }

    @Test
    public void testMinCostFlowConfig_Ok()
    {
        getMinCostFlowBuilder().build();
    }

    @Test
    public void test()
    {
        NetworkGeneratorConfig config = getAssignmentBuilder().build();
        System.out.println(config.getMaximumArcNum());
    }

}
