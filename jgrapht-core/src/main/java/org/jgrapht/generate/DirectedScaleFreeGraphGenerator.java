/*
 * (C) Copyright 2019-2021, by Amr ALHOSSARY and Contributors.
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
package org.jgrapht.generate;

import org.jgrapht.*;

import java.util.*;

/**
 * A generator for directed scale-free graphs.
 * <p>
 * This generator creates a directed scale-free graph according to a power law, as described in
 * <a href="https://dl.acm.org/citation.cfm?id=644133">Bollobás et al.</a> The paper can be cited as
 * Béla Bollobás, Christian Borgs, Jennifer Chayes, and Oliver Riordan. "Directed scale-free
 * graphs." Proceedings of the fourteenth annual ACM-SIAM symposium on Discrete algorithms. Society
 * for Industrial and Applied Mathematics, 2003.
 * <p>
 * In This generator, the graph continues to grow one edge per step, according to the probabilities
 * alpha, beta, and gamma (which sum up to 1).<br>
 * <ul>
 * <li><b>alpha</b> is the probability that the new edge is from a new vertex v to an existing
 * vertex w, where w is chosen according to d_in + delta_in.
 * <li><b>beta</b> is the probability that the new edge is from an existing vertex v to an existing
 * vertex w, where v and w are chosen independently, v according to d_out + delta_out, and w
 * according to d_in + delta_in.
 * <li><b>gamma</b> is the probability that the new edge is from an existing vertex v to a new
 * vertex w, where v is chosen according to d_out + delta_out.
 * </ul>
 * 
 * <p>
 * In their original paper, the graph continues to grow according to a certain power law until a
 * certain number of edges is reached irrespective to the number of nodes.<br>
 * However, because the target number of edges is not known beforehand, in this implementation, we
 * added another feature that enables the user to grow the curve according to that power law until
 * the target number of edges or target number of nodes is reached.
 * 
 * @author Amr ALHOSSARY
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class DirectedScaleFreeGraphGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{
    private final Random rng;

    /**
     * probability that the new edge is from a new vertex v to an existing vertex w, where w is
     * chosen according to d_in + delta_in criterion.
     */
    private final float alpha;
    /**
     * The probability that the new edge is (from a new vertex v to an existing vertex w) plus the
     * probability that the new edge is (from an existing vertex v to an existing vertex w), where v
     * and w are chosen independently, v according to d_out + delta_out, and w according to d_in +
     * delta_in criteria. This equals 1 - gamma. Gamma refers to the probability that the new edge
     * is from an existing vertex v to a new vertex w.
     */
    private final float alphaPlusBeta;

    /** In-degree bias used for Alpha and Beta */
    private final float deltaIn;
    /** Out-degree bias used for Beta and Gamma */
    private final float deltaOut;
    /**
     * Target total number of edges to reach. It has a higher priority than {@link #targetNodes}.
     * Zero is a valid value.<br>
     * If negative number, the user does not care about the total number of edges and is interested
     * only in the number of nodes, therefore, {@link #targetNodes} will be considered instead.
     * Otherwise, {@link #targetEdges} will be considered and {@link #targetEdges} will be ignored.
     */
    private final int targetEdges;
    /**
     * Target total number of targetNodes to reach.<br>
     * This has lower priority than {@link #targetEdges}. It will not be used unless
     * {@link #targetEdges} given is a <i>negative</i> number.
     */
    private final int targetNodes;

    /**
     * An enum to indicate the vertex selection using its inDegree or outDegree
     */
    private enum Direction
    {
        IN,
        OUT
    }

    /**
     * Maximum number of consecutive failed attempts to add an edge.
     */
    private int maxFailures = 1000;

    /**
     * Control whether the generated graph may contain loops.
     */
    private boolean allowingMultipleEdges = true;

    /**
     * Control whether the generated graph many contain multiple (parallel) edges between the same
     * two vertices
     */
    private boolean allowingSelfLoops = true;

    /**
     * Constructs a Generator.
     * 
     * @param alpha The probability that the new edge is from a new vertex v to an existing vertex
     *        w, where w is chosen according to d_in + delta_in.
     * @param gamma The probability that the new edge is from an existing vertex v to a new vertex
     *        w, where v is chosen according to d_out + delta_out.
     * @param deltaIn The in-degree bias used for Alpha and Beta.
     * @param deltaOut The out-degree bias used for Beta and Gamma.
     * @param targetEdges Target total number of edges to reach. It has a higher priority than
     *        {@link #targetNodes}. Zero is a valid value.<br>
     *        If negative number, the user does not care about the total number of edges and is
     *        interested only in the number of nodes, therefore, {@link #targetNodes} will be
     *        considered instead. Otherwise, {@link #targetEdges} will be considered and
     *        {@link #targetEdges} will be ignored.
     * @param targetNodes Target number of nodes to reach. Zero is a valid value.<br>
     *        This parameter has lower priority than {@link #targetEdges} and will be used only if
     *        {@link #targetEdges} given is a <i>negative</i> number.
     */
    public DirectedScaleFreeGraphGenerator(
        float alpha, float gamma, float deltaIn, float deltaOut, int targetEdges, int targetNodes)
    {
        this(alpha, gamma, deltaIn, deltaOut, targetEdges, targetNodes, new Random());
    }

    /**
     * Constructs a Generator using a seed for the random number generator.
     * 
     * @param alpha The probability that the new edge is from a new vertex v to an existing vertex
     *        w, where w is chosen according to d_in + delta_in.
     * @param gamma The probability that the new edge is from an existing vertex v to a new vertex
     *        w, where v is chosen according to d_out + delta_out.
     * @param deltaIn The in-degree bias used for Alpha and Beta.
     * @param deltaOut The out-degree bias used for Beta and Gamma.
     * @param targetEdges Target total number of edges to reach. It has a higher priority than
     *        {@link #targetNodes}. Zero is a valid value.<br>
     *        If negative number, the user does not care about the total number of edges and is
     *        interested only in the number of nodes, therefore, {@link #targetNodes} will be
     *        considered instead. Otherwise, {@link #targetEdges} will be considered and
     *        {@link #targetEdges} will be ignored.
     * @param targetNodes Target number of nodes to reach. Zero is a valid value.<br>
     *        This parameter has lower priority than {@link #targetEdges} and will be used only if
     *        {@link #targetEdges} given is a <i>negative</i> number.
     * @param seed The seed to feed to the random number generator.
     */
    public DirectedScaleFreeGraphGenerator(
        float alpha, float gamma, float deltaIn, float deltaOut, int targetEdges, int targetNodes,
        long seed)
    {
        this(alpha, gamma, deltaIn, deltaOut, targetEdges, targetNodes, new Random(seed));
    }

    /**
     * Constructs a Generator using a seed for the random number generator and sets the two
     * relaxation options <code>allowingMultipleEdges</code> and <code>allowingSelfLoops</code>.
     * 
     * @param alpha The probability that the new edge is from a new vertex v to an existing vertex
     *        w, where w is chosen according to d_in + delta_in.
     * @param gamma The probability that the new edge is from an existing vertex v to a new vertex
     *        w, where v is chosen according to d_out + delta_out.
     * @param deltaIn The in-degree bias used for Alpha and Beta.
     * @param deltaOut The out-degree bias used for Beta and Gamma.
     * @param targetEdges Target total number of edges to reach. It has a higher priority than
     *        {@link #targetNodes}. Zero is a valid value.<br>
     *        If negative number, the user does not care about the total number of edges and is
     *        interested only in the number of nodes, therefore, {@link #targetNodes} will be
     *        considered instead. Otherwise, {@link #targetEdges} will be considered and
     *        {@link #targetEdges} will be ignored.
     * @param targetNodes Target number of nodes to reach. Zero is a valid value.<br>
     *        This parameter has lower priority than {@link #targetEdges} and will be used only if
     *        {@link #targetEdges} given is a <i>negative</i> number.
     * @param seed The seed to feed to the random number generator.
     * @param allowingMultipleEdges whether the generator allows multiple parallel edges between the
     *        same two vertices (v, w).
     * @param allowingSelfLoops whether the generator allows self loops from the a vertex to itself.
     */
    public DirectedScaleFreeGraphGenerator(
        float alpha, float gamma, float deltaIn, float deltaOut, int targetEdges, int targetNodes,
        long seed, boolean allowingMultipleEdges, boolean allowingSelfLoops)
    {
        this(alpha, gamma, deltaIn, deltaOut, targetEdges, targetNodes, seed);
        this.allowingMultipleEdges = allowingMultipleEdges;
        this.allowingSelfLoops = allowingSelfLoops;
    }

    /**
     * Construct a new generator using the provided random number generator.
     * 
     * @param alpha The probability that the new edge is from a new vertex v to an existing vertex
     *        w, where w is chosen according to d_in + delta_in.
     * @param gamma The probability that the new edge is from an existing vertex v to a new vertex
     *        w, where v is chosen according to d_out + delta_out.
     * @param deltaIn The in-degree bias used for Alpha and Beta.
     * @param deltaOut The out-degree bias used for Beta and Gamma.
     * @param targetEdges Target total number of edges to reach. It has a higher priority than
     *        {@link #targetNodes}. Zero is a valid value.<br>
     *        If negative number, the user does not care about the total number of edges and is
     *        interested only in the number of nodes, therefore, {@link #targetNodes} will be
     *        considered instead. Otherwise, {@link #targetEdges} will be considered and
     *        {@link #targetEdges} will be ignored.
     * @param targetNodes Target number of nodes to reach. Zero is a valid value.<br>
     *        This parameter has lower priority than {@link #targetEdges} and will be used only if
     *        {@link #targetEdges} given is a <i>negative</i> number.
     * @param rng The {@link Random} object to use.
     */
    public DirectedScaleFreeGraphGenerator(
        float alpha, float gamma, float deltaIn, float deltaOut, int targetEdges, int targetNodes,
        Random rng)
    {
        this.alpha = alpha;
        this.alphaPlusBeta = 1.0f - gamma;
        this.deltaIn = deltaIn;
        this.deltaOut = deltaOut;
        this.targetEdges = targetEdges;
        this.targetNodes = targetNodes;
        this.rng = Objects.requireNonNull(rng, "Random number generator cannot be null");

        // Do several checks on the parameters
        if (alpha < 0 || gamma < 0 || alpha + gamma > 1) {
            throw new IllegalArgumentException(
                String.format("alpha and gamma values of (%f, %f) are invalid", alpha, gamma));
        }
        if (deltaIn < 0 || deltaOut < 0) {
            throw new IllegalArgumentException(
                String
                    .format(
                        "deltaIn and deltaOut values of (%f, %f) are invalid", deltaIn, deltaOut));
        }
        if (targetEdges < 0 && targetNodes < 0) {
            throw new IllegalArgumentException(
                "can not have both targetEdges and targetNodes not set.");
        }

    }

    /**
     * Construct a new generator using the provided random number generator and sets the two
     * relaxation options <code>allowingMultipleEdges</code> and <code>allowingSelfLoops</code>.
     * 
     * @param alpha The probability that the new edge is from a new vertex v to an existing vertex
     *        w, where w is chosen according to d_in + delta_in.
     * @param gamma The probability that the new edge is from an existing vertex v to a new vertex
     *        w, where v is chosen according to d_out + delta_out.
     * @param deltaIn The in-degree bias used for Alpha and Beta.
     * @param deltaOut The out-degree bias used for Beta and Gamma.
     * @param targetEdges Target total number of edges to reach. It has a higher priority than
     *        {@link #targetNodes}. Zero is a valid value.<br>
     *        If negative number, the user does not care about the total number of edges and is
     *        interested only in the number of nodes, therefore, {@link #targetNodes} will be
     *        considered instead. Otherwise, {@link #targetEdges} will be considered and
     *        {@link #targetEdges} will be ignored.
     * @param targetNodes Target number of nodes to reach. Zero is a valid value.<br>
     *        This parameter has lower priority than {@link #targetEdges} and will be used only if
     *        {@link #targetEdges} given is a <i>negative</i> number.
     * @param rng The {@link Random} object to use.
     * @param allowingMultipleEdges whether the generator allows multiple parallel edges between the
     *        same two vertices (v, w).
     * @param allowingSelfLoops whether the generator allows self loops from the a vertex to itself.
     */
    public DirectedScaleFreeGraphGenerator(
        float alpha, float gamma, float deltaIn, float deltaOut, int targetEdges, int targetNodes,
        Random rng, boolean allowingMultipleEdges, boolean allowingSelfLoops)
    {
        this(alpha, gamma, deltaIn, deltaOut, targetEdges, targetNodes, rng);
        this.allowingMultipleEdges = allowingMultipleEdges;
        this.allowingSelfLoops = allowingSelfLoops;
    }

    /**
     * Generates an instance of the {@link Graph}.
     * 
     * @param target the target graph
     * @param resultMap not used by this generator, can be null
     * 
     * @throws TooManyFailuresException When the method fails {@link #maxFailures} times to add a
     *         new edge to the growing graph.
     * @throws IllegalArgumentException When the graph does not support Multiple edges or self loop
     *         while the generator does.
     */
    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        if (this.allowingMultipleEdges && !target.getType().isAllowingMultipleEdges()) {
            throw new IllegalArgumentException(
                "Generator allows Multiple Edges while graph does not. Consider changing this generator parameters or the target graph type.");
        }
        if (this.allowingSelfLoops && !target.getType().isAllowingSelfLoops()) {
            throw new IllegalArgumentException(
                "Generator allows Self loops while graph does not. Consider changing this generator parameters or the target graph type.");
        }

        Set<V> newNodesSet = new HashSet<>();
        Set<E> newEdgesSet = new HashSet<>();

        if (targetEdges == 0 || (targetEdges < 0 && targetNodes == 0))
            return;
        V initV = target.addVertex();
        newNodesSet.add(initV);

        int failuresCounter = 0;
        // grow network now, edge by edge. If the number of edges is unlocked, continue growing not
        // only until targetNodes is reached, but until adding any more edges would add another
        // node. i.e. allow more beta growth but not alpha nor gamma.
        while (targetEdges >= 0 ? targetEdges > newEdgesSet.size()
            : targetNodes >= newNodesSet.size())
        {

            if (failuresCounter >= maxFailures) {
                throw new TooManyFailuresException(
                    failuresCounter + " consecutive failures is more than maximum allowed number ("
                        + maxFailures + ").");
            }

            V v = null, w = null;
            boolean newV = false, newW = false;
            E e;
            float tributaries = rng.nextFloat();
            if (tributaries <= alpha) {
                // stop adding nodes if you will exceed the target
                if (targetEdges < 0 && newNodesSet.size() == targetNodes)
                    break;
                newV = true;
                w = pickAVertex(target, newNodesSet, newEdgesSet, Direction.IN, deltaIn);
            } else if (tributaries <= alphaPlusBeta) {
                v = pickAVertex(target, newNodesSet, newEdgesSet, Direction.OUT, deltaOut);
                w = pickAVertex(target, newNodesSet, newEdgesSet, Direction.IN, deltaIn);
            } else {// gamma
                    // stop adding nodes if you will exceed the target
                if (targetEdges < 0 && newNodesSet.size() == targetNodes)
                    break;
                v = pickAVertex(target, newNodesSet, newEdgesSet, Direction.OUT, deltaOut);
                newW = true;
            }

            if ((newV && w == null) || (newW && v == null)) {
                failuresCounter++;
                continue;
            }

            // check for self loops
            if (!allowingSelfLoops && v == w) {
                failuresCounter++;
                continue;
            }

            // check for multiple parallel targetEdges
            if (!allowingMultipleEdges && target.containsEdge(v, w)) {
                failuresCounter++;
                continue;
            }

            if (newV) {
                v = target.addVertex();
            }
            if (newW) {
                w = target.addVertex();
            }

            e = target.addEdge(v, w);
            failuresCounter = 0;

            newNodesSet.add(v);
            newNodesSet.add(w);
            newEdgesSet.add(e);
        }
    }

    /**
     * Select a vertex from the currently available vertices, using the passed bias.
     * 
     * @param target The target graph
     * @param allNewNodes All (new) nodes in the target graph
     * @param allNewEdgesSet All (new) edges in the target graph
     * @param direction {@link Direction#IN} for inDegree or {@link Direction#IN} for outDegree
     * @param bias deltaIn or deltaOut value according to #directioIn
     * @return the selected node.
     */
    private V pickAVertex(
        Graph<V, E> target, Set<V> allNewNodes, Set<E> allNewEdgesSet, Direction direction,
        float bias)
    {
        final int allNewNodesSize = allNewNodes.size();
        if (allNewNodesSize == 0) {
            return null;
        } else if (allNewNodesSize == 1) {
            return allNewNodes.iterator().next();
        }

        float indicatorAccumulator = 0;
        V ret;
        float denominator = allNewEdgesSet.size() + allNewNodesSize * bias;
        float numerator;

        float r = rng.nextFloat();
        // multiply r by denominator instead of dividing all individual values by it.
        r *= denominator;
        Iterator<V> verticesIterator = allNewNodes.iterator();
        do {
            ret = verticesIterator.next();
            numerator = (direction == Direction.IN) ? (target.inDegreeOf(ret) + bias)
                : (target.outDegreeOf(ret) + bias);
            indicatorAccumulator += numerator;
        } while (verticesIterator.hasNext() && indicatorAccumulator < r);

        return ret;
    }

    /**
     * Returns the maximum allowed number of consecutive failed attempts to add an edge.
     * 
     * @return maxFailure field.
     */
    public int getMaxFailures()
    {
        return maxFailures;
    }

    /**
     * Sets the maximum allowed number of consecutive failed attempts to add an edge (must be non
     * negative).
     * 
     * @param maxFailures Maximum allowed (non negative) number of consecutive failed attempts to
     *        add an edge.
     */
    public void setMaxFailures(int maxFailures)
    {
        if (maxFailures < 0) {
            throw new IllegalArgumentException("value must be non negative");
        }
        this.maxFailures = maxFailures;
    }

    /**
     * Returns whether the generated graph may contain multiple (parallel) edges between the same
     * two vertices.
     * 
     * @return whether the generated graph may contain multiple (parallel) edges between the same
     *         two vertices
     */
    public boolean isAllowingMultipleEdges()
    {
        return allowingMultipleEdges;
    }

    /**
     * Sets whether the generated graph may contain multiple (parallel) edges between the same two
     * vertices
     * 
     * @param allowingMultipleEdges whether the generated graph may contain multiple (parallel)
     *        edges between the same two vertices
     */
    public void setAllowingMultipleEdges(boolean allowingMultipleEdges)
    {
        this.allowingMultipleEdges = allowingMultipleEdges;
    }

    /**
     * Returns whether the generated graph may contain multiple (parallel) edges between the same
     * two vertices
     * 
     * @return whether the generated graph many contain multiple (parallel) edges between the same
     *         two vertices
     */
    public boolean isAllowingSelfLoops()
    {
        return allowingSelfLoops;
    }

    /**
     * Sets whether the generated graph may contain multiple (parallel) edges between the same two
     * vertices
     * 
     * @param allowingSelfLoops whether the generated graph many contain multiple (parallel) edges
     *        between the same two vertices
     */
    public void setAllowingSelfLoops(boolean allowingSelfLoops)
    {
        this.allowingSelfLoops = allowingSelfLoops;
    }
}
