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
 * Configuration class to specify network parameters for the {@link NetworkGenerator}. Any valid
 * configuration specifies a minimum cost flow network to generate. Under additional constraints the
 * minimum cost flow networks can be interpreted as maximum flow problems or bipartite matching
 * problems.
 * <p>
 * In the following parameter definition the term <it>transshipment</it> is used for nodes that have
 * both incoming and outgoing arcs. This config is used to configure the following parameters:
 * <ul>
 * <li>nodeNum - number of all nodes in the network;</li>
 * <li>arcNum - number of all arcs in the network;</li>
 * <li>sourceNum - number of source nodes in the network. Source node is node that has positive
 * supply value;</li>
 * <li>sinkNum - number of sink nodes in the network. Sink node is a node that has negative supply
 * value (i.e. it has demand);</li>
 * <li>transshipSourceNum - number of transshipment sources. These source nodes compose a subtype of
 * all source nodes, which means that the number of these nodes must not exceed the number of
 * sources. This parameter can be called tSourceNum as well, the transshipment sources can be called
 * t-sources;</li>
 * <li>transshipSinkNum - number of transshipment sinks. As with transshipment sources, these sinks
 * are a subtype of all sinks and thus their number must not exceed the number of all sinks. This
 * parameter can be called tSinkNum as well, the transshipment sinks can be called t-sinks;</li>
 * <li>totalSupply - the sum of supplies od all source nodes. This value is distributed among source
 * nodes. The same amount is distributed among sink nodes with negative sign;</li>
 * <li>minCap - a lower bound on the arc capacities;</li>
 * <li>maxCap - an upper bound on the arc capacities;</li>
 * <li>minCost - a lower bound on the arc costs;</li>
 * <li>maxCost - an upper bound on the arc costs;</li>
 * <li>percentCapacitated - a value between 0 and 100 which specifies an approximate ratio of arcs
 * which have finite capacity. Other arcs will have infinite capacity;</li>
 * <li>percentWithInfCost - a value between 0 and 100 which specifies an approximate ratio of arcs
 * which have infinite cost. All other arcs will have finite cost.</li>
 * </ul>
 * <p>
 * This parameter set specifies certain amount of implicit parameters:
 * <ul>
 * <li>pureSourceNum - number of sources, which are guaranteed to have no incoming arcs. This value
 * is equal to the sourceNum - transshipSourceNum;</li>
 * <li>pureSinkNum - number of sinks, which are guaranteed to have no outcoming arcs. This value is
 * equal to the sinkNum - transshipSinkNum;</li>
 * <li>transshipNodeNum - number of nodes in the network which are neither sources now sinks. These
 * nodes can have both incoming and outcoming arcs and their supply values are equal to 0. The
 * number of these nodes is equal to the nodeNum - sourceNum - sinkNum. This parameter can be called
 * tNodeNum as well, transshipment nodes can be called t-nodes.</li>
 * </ul>
 * <p>
 * Not every parameter combination specifies a valid config for a network generator. The following
 * are existing parameter constraints:
 * <ul>
 * <li>transshipSourceNum $\leq$ sourceNum;</li>
 * <li>transshipSinkNum $\leq$ sinkNum;</li>
 * <li>sourceNum $+$ sinkNum $\leq$ nodeNum;</li>
 * <li>max(sourceNum, sinkNum) $\leq$ totalSupply;</li>
 * <li>minArcNum $\leq$ arcNum $\leq$ maxArcNum;</li>
 * <li>minCap $\leq$ maxCap<;/li>
 * <li>minCost $\leq$ maxCost;</li>
 * <li>0 $\leq$ percentCapacitated $\leq$ 100;</li>
 * <li>0 $\leq$ percentWithInfCost $\leq$ 100;</li>
 * <li>all parameters are non-negative except for minCost and maxCost (the are costs may be
 * negative).</li>
 * </ul>
 * <p>
 * MinArcNum is a number of arcs that is needed to make every node connected to at least one source
 * and one sink. This value is equal to transshipNodeNum + max(sourceNum, sinkNum). This value can
 * be computed using {@link NetworkGeneratorConfig#getMinimumArcNum()} for a specific network. This
 * value can be computes using {@link NetworkGeneratorConfig#getMinimumArcNum(long, long, long)} as
 * well. MaxArcNum is a number of arcs that makes it impossible to add more arcs to the network
 * without violating the constraints. This value consists of 3 quantities:
 * <ul>
 * <li>sourceArcs = pureSourceNum*tSourceNum + tSourceNum*(tSourceNum - 1) + sourceNum * (tNodeNum +
 * sinkNum)</li>
 * <li>tNodeArcs = tNodeNum*(tSourceNum + (tNodeNum - 1) + sinkNum)</li>
 * <li>tSinkArcs = tSinkNum*(tSourceNum + tNodeNum + (tSinkNum - 1))</li>
 * </ul>
 * <p>
 * The maximum number of arcs is therefore equal to sourceArcs + tNodeArcs + tSinkArcs. This values
 * can be computed for a specific network configuration using
 * {@link NetworkGeneratorConfig#getMaximumArcNum()}, or for specified node quantity parameters
 * using {@link NetworkGeneratorConfig#getMaximumArcNum(long, long, long, long, long)}.
 * <p>
 * The general purpose of this config is to specify parameters for the minimum cost flow network. At
 * the same time, this config can specify parameters for the max flow network or bipartite matching
 * problems if additional parameter constraints are imposed. If minCost = maxCost, then the network
 * is called unweighted. An unweighted network specifies a maximum flow problem, it the supply
 * values are additionally removed. To specify a bipartite matching problem, the parameters must
 * satisfy:
 * <ul>
 * <li>tSourceNum = tSinkNum = 0;</li>
 * <li>sourceNum = sinkNum = nodeNum/2 (nodeNum must be even);</li>
 * <li>totalSupply = sourceNum;</li>
 * <li>minCap = maxCap = 1.</li>
 * </ul>
 * <p>
 * Note that bipartite matching problem can be both weighted and unweighted.
 * <p>
 * To construct instances of the {@link NetworkGeneratorConfig}, use
 * {@link NetworkGeneratorConfigBuilder}. It performs all the parameter validation and provides
 * meaningful error messages in the cases something is going wrong.
 *
 * @author Timofey Chudakov
 * @see NetworkGenerator
 * @see NetworkGeneratorConfigBuilder
 * @see org.jgrapht.alg.flow.mincost.MinimumCostFlowProblem
 * @see MaximumFlowProblem
 * @see BipartiteMatchingProblem
 */
public class NetworkGeneratorConfig
{
    private final int nodeNum;
    private final int arcNum;
    private final int sourceNum;
    private final int sinkNum;
    private final int transshipSourceNum;
    private final int transshipSinkNum;
    private final int totalSupply;
    private final int minCap;
    private final int maxCap;
    private final int minCost;
    private final int maxCost;
    private final int percentCapacitated;
    private final int percentWithInfCost;

    /**
     * Constructs a new {@link NetworkGeneratorConfig}
     *
     * @param nodeNum number of nodes
     * @param arcNum number of arcs
     * @param sourceNum number of network sources
     * @param sinkNum number of network sinks
     * @param transshipSourceNum number of transshipment sources
     * @param transshipSinkNum number of transshipment sinks
     * @param totalSupply total supply of all network sources
     * @param minCap arc capacity lower bound
     * @param maxCap arc capacity upper bound
     * @param minCost arc cost lower bound
     * @param maxCost arc cost upper bound
     * @param percentCapacitated percent of arcs to have finite capacity
     * @param percentWithInfCost percent of arcs to have infinite cost
     */
    NetworkGeneratorConfig(
        int nodeNum, int arcNum, int sourceNum, int sinkNum, int transshipSourceNum,
        int transshipSinkNum, int totalSupply, int minCap, int maxCap, int minCost, int maxCost,
        int percentCapacitated, int percentWithInfCost)
    {
        this.nodeNum = nodeNum;
        this.arcNum = arcNum;
        this.sourceNum = sourceNum;
        this.sinkNum = sinkNum;
        this.transshipSourceNum = transshipSourceNum;
        this.transshipSinkNum = transshipSinkNum;
        this.totalSupply = totalSupply;
        this.minCap = minCap;
        this.maxCap = maxCap;
        this.minCost = minCost;
        this.maxCost = maxCost;
        this.percentCapacitated = percentCapacitated;
        this.percentWithInfCost = percentWithInfCost;
    }

    /**
     * Returns maximum possible number of arcs this network can contain between the source nodes.
     * This number is 0 if network doesn't contain transshipment sources.
     *
     * @return maximum number of arcs between network sources.
     */
    public long getMaxSource2TSourceArcNum()
    {
        return (long) getPureSourceNum() * transshipSourceNum
            + (long) transshipSourceNum * (transshipSourceNum - 1);
    }

    /**
     * Returns maximum number of arcs this network can contain between network sources and
     * transshipment nodes.
     *
     * @return maximum number of arcs between network sources and transshipment nodes.
     */
    public long getMaxSource2TNodeArcNum()
    {
        return (long) sourceNum * getTransshipNodeNum();
    }

    /**
     * Returns maximum number of arcs between network sources and network sinks.
     *
     * @return maximum number of arcs between network sources and network sinks.
     */
    public long getMaxSource2SinkArcNum()
    {
        return (long) sourceNum * sinkNum;
    }

    /**
     * Returns maximum number of arcs between transshipment nodes and network sources.
     *
     * @return maximum number of arcs between transshipment nodes and network sources.
     */
    public long getMaxTNode2TSourceArcNum()
    {
        return (long) getTransshipNodeNum() * transshipSourceNum;
    }

    /**
     * Returns maximum number of arcs between transshipment nodes of this network
     *
     * @return maximum number of arcs between transshipment nodes of this network
     */
    public long getMaxTNode2TNodeArcNum()
    {
        return (long) getTransshipNodeNum() * (getTransshipNodeNum() - 1);
    }

    /**
     * Returns maximum number of arcs between transshipment nodes and network sinks.
     *
     * @return maximum number of arcs between transshipment nodes and network sinks.
     */
    public long getMaxTNode2SinkArcNum()
    {
        return (long) getTransshipNodeNum() * sinkNum;
    }

    /**
     * Returns maximum number of arcs between network sinks and network sources.
     *
     * @return maximum number of arcs between network sinks and network sources.
     */
    public long getMaxTSink2TSourceArcNum()
    {
        return (long) transshipSinkNum * transshipSourceNum;
    }

    /**
     * Returns maximum number of arcs between network sinks and transshipment nodes.
     *
     * @return maximum number of arcs between network sinks and transshipment nodes.
     */
    public long getMaxTSink2TNodeArcNum()
    {
        return (long) transshipSinkNum * getTransshipNodeNum();
    }

    /**
     * Returns maximum number of arcs between network sinks.
     *
     * @return maximum number of arcs between network sinks.
     */
    public long getMaxTSink2SinkArcNum()
    {
        return (long) transshipSinkNum * (transshipSinkNum - 1)
            + getPureSinkNum() * transshipSinkNum;
    }

    /**
     * Returns maximum number of arcs between network sources and all other nodes.
     *
     * @return maximum number of arcs between network sources and all other nodes.
     */
    public long getMaxSource2AllArcNum()
    {
        return getMaxSource2TSourceArcNum() + getMaxSource2TNodeArcNum()
            + getMaxSource2SinkArcNum();
    }

    /**
     * Returns maximum number of arcs between transshipment nodes and all other nodes.
     *
     * @return maximum number of arcs between transshipment nodes and all other nodes.
     */
    public long getMaxTransshipNode2AllArcNum()
    {
        return getMaxTNode2TSourceArcNum() + getMaxTNode2TNodeArcNum() + getMaxTNode2SinkArcNum();
    }

    /**
     * Returns maximum number of arcs between network sinks and all other nodes.
     *
     * @return maximum number of arcs between network sinks and all other nodes.
     */
    public long getMaxSink2ALlArcNum()
    {
        return getMaxTSink2TSourceArcNum() + getMaxTSink2TNodeArcNum() + getMaxTSink2SinkArcNum();
    }

    /**
     * Returns minimum number of nodes this network can contain.
     *
     * @return minimum number of nodes this network can contain.
     */
    public long getMinimumArcNum()
    {
        return getTransshipNodeNum() + Math.max(getSourceNum(), getSinkNum());
    }

    /**
     * Returns maximum number of nodes this network can contain.
     *
     * @return maximum number of nodes this network can contain.
     */
    public long getMaximumArcNum()
    {
        return getMaxSource2AllArcNum() + getMaxTransshipNode2AllArcNum() + getMaxSink2ALlArcNum();
    }

    /**
     * Returns minimum number of arcs a network with specifies node parameters can contain. Note,
     * that the number of transshipment sources and sinks doesn't affect this quantity.
     *
     * @param sourceNum number of sources in the network
     * @param tNodeNum number of transshipment nodes in the network
     * @param sinkNum number of sinks in the network
     * @return minimum number of arcs a network with specifies nodes parameters can contain.
     */
    public static long getMinimumArcNum(long sourceNum, long tNodeNum, long sinkNum)
    {
        return tNodeNum + Math.max(sourceNum, sinkNum);
    }

    /**
     * Returns maximum number of arcs a network with specified node parameters can contain. Use this
     * network in situation when number of transshipment sources and sinks is zero.
     *
     * @param sourceNum number of sources in the network
     * @param tNodeNum number of transshipment nodes in the network
     * @param sinkNum number of sinks in the network
     * @return maximum number of arcs a network with specified node parameters can contain.
     */
    public static long getMaximumArcNum(long sourceNum, long tNodeNum, long sinkNum)
    {
        return getMaximumArcNum(sourceNum, 0, tNodeNum, 0, sinkNum);
    }

    /**
     * Returns maximum number of arcs a network with specified node parameters can contain.
     *
     * @param sourceNum number of sources in the network
     * @param tSourceNum number of transshipment sources in the network
     * @param tNodeNum number of transshipment nodes in the network
     * @param tSinkNum number of transshipment sinks in the network
     * @param sinkNum number of sinks in the network
     * @return maximum number of arcs a network with specified node parameters can contain.
     */
    public static long getMaximumArcNum(
        long sourceNum, long tSourceNum, long tNodeNum, long tSinkNum, long sinkNum)
    {
        long pureSourceNum = sourceNum - tSourceNum;

        long sourceArcs = pureSourceNum * tSourceNum + tSourceNum * (tSourceNum - 1)
            + sourceNum * (tNodeNum + sinkNum);
        long tNodeArcs = tNodeNum * (tSourceNum + (tNodeNum - 1) + sinkNum);
        long sinkArcs = tSinkNum * (tSourceNum + tNodeNum + (sinkNum - 1));

        return sourceArcs + tNodeArcs + sinkArcs;
    }

    /**
     * Returns number of pure sources in the network. Pure sources are network sources which can't
     * have incoming arcs.
     *
     * @return number of pure sources in the network.
     */
    public int getPureSourceNum()
    {
        return sourceNum - transshipSourceNum;
    }

    /**
     * Returns number of pure sinks in the network. Pure sinks are network sinks which can't have
     * outgoing arcs. which can't have outgoing arcs.
     *
     * @return number of pure sinks in the network.
     */
    public int getPureSinkNum()
    {
        return sinkNum - transshipSinkNum;
    }

    /**
     * Checks if the network allows different arc costs.
     *
     * @return {@code true} if the network allows different arc costs, {@code false} otherwise.
     */
    public boolean isCostWeighted()
    {
        return minCost != maxCost;
    }

    /**
     * Returns the number of transshipment nodes in the network.
     *
     * @return the number of transshipment nodes in the network.
     */
    public int getTransshipNodeNum()
    {
        return nodeNum - sourceNum - sinkNum;
    }

    /**
     * Checks if the network satisfies the transportation problem conditions.
     * <p>
     * In transportation problem the sum of network sources and network sinks equals to the number
     * of nodes (no transshipment nodes) and the network doesn't contain transshipment sources and
     * sinks. In essence, the network is a bipartite graph.
     *
     * @return {@code true} if the network specifies a transportation problem, {@code false}
     *         otherwise.
     */
    private boolean transportationProblemCondition()
    {
        return sourceNum + sinkNum == nodeNum && transshipSourceNum == 0 && transshipSinkNum == 0;
    }

    /**
     * Checks if the <it>transportation</it> network is a bipartite matching problem.
     * <p>
     * A transportation problem is a bipartite matching problem, if the bipartite graph partitions
     * are of equal size, every source supply is equal to 1 (thus the demand of every sink is equal
     * to 1 as well), and the capacity of every arc is 1.
     *
     * @return {@code true} if the transportation problem is a bipartite matching problem,
     *         {@code false} otherwise.
     */
    private boolean assignmentProblemCondition()
    {
        return sourceNum == sinkNum && totalSupply == sourceNum && minCap == 1 && maxCap == 1;
    }

    /**
     * Checks if a network can be interpreted as a maximum flow problem.
     * <p>
     * The only condition for a minimum cost flow to be interpreted as a maximum flow problem is
     * that the arc costs are constant for all arcs.
     *
     * @return {@code true} if the network can be interpreted as a max flow problem, {@code false}
     *         otherwise.
     */
    public boolean isMaxFlowProblem()
    {
        return !isCostWeighted();
    }

    /**
     * Checks if the network is a bipartite matching problem (assignment problem). The problem can
     * we both weighted and unweighted.
     *
     * @return {@code true} if the network specifies a bipartite matching problem, {@code false}
     *         otherwise.
     */
    public boolean isAssignmentProblem()
    {
        return transportationProblemCondition() && assignmentProblemCondition();
    }

    /**
     * Returns the number of nodes in the network.
     *
     * @return the number of nodes in the network.
     */
    public int getNodeNum()
    {
        return nodeNum;
    }

    /**
     * Returns the number of arcs in the network.
     *
     * @return the number of arcs in the network.
     */
    public int getArcNum()
    {
        return arcNum;
    }

    /**
     * Returns the number of sources in the network.
     *
     * @return the number of sources in the network.
     */
    public int getSourceNum()
    {
        return sourceNum;
    }

    /**
     * Returns the number of sinks in the network.
     *
     * @return the number of sinks in the network.
     */
    public int getSinkNum()
    {
        return sinkNum;
    }

    /**
     * Returns the number of transshipment sources in the network.
     *
     * @return the number of transshipment sources in the network.
     */
    public int getTransshipSourceNum()
    {
        return transshipSourceNum;
    }

    /**
     * Returns the number of transshipment sinks in the network.
     *
     * @return the number of transshipment sinks in the network.
     */
    public int getTransshipSinkNum()
    {
        return transshipSinkNum;
    }

    /**
     * Returns the total supply of the network.
     *
     * @return the total supply of the network.
     */
    public int getTotalSupply()
    {
        return totalSupply;
    }

    /**
     * Returns arc capacity lower bound.
     *
     * @return arc capacity lower bound.
     */
    public int getMinCap()
    {
        return minCap;
    }

    /**
     * Returns arc capacity upper bound.
     *
     * @return arc capacity upper bound.
     */
    public int getMaxCap()
    {
        return maxCap;
    }

    /**
     * Returns arc cost lower bound.
     *
     * @return arc cost lower bound.
     */
    public int getMinCost()
    {
        return minCost;
    }

    /**
     * Returns arc cost upper bound.
     *
     * @return arc cost upper bound.
     */
    public int getMaxCost()
    {
        return maxCost;
    }

    /**
     * Returns percent of arcs that have finite capacity.
     *
     * @return percent of arcs that have finite capacity.
     */
    public int getPercentCapacitated()
    {
        return percentCapacitated;
    }

    /**
     * Returns percent of arcs that have infinite cost.
     *
     * @return percent of arcs that have infinite cost.
     */
    public int getPercentWithInfCost()
    {
        return percentWithInfCost;
    }

}
