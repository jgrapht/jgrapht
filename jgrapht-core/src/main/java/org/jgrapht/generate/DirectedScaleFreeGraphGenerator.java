/*
 * (C) Copyright 2019-2019, by Amr ALHOSSARY and Contributors.
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.util.SupplierUtil;

/**
 * This generator implements Bollobás, Béla, et al. "Directed scale-free graphs." Proceedings of the
 * fourteenth annual ACM-SIAM symposium on Discrete algorithms. Society for Industrial and Applied
 * Mathematics, 2003.<br>
 * 
 * <pre>
 * The authors are:
 *  <b>Béla Bollobás</b>   University of Memphis, Memphis TN and Trinity College, Cambridge, UK
 *  <b>Christian Borgs</b> Microsoft Research, Redmond, WA
 *  <b>Jennifer Chayes</b> Microsoft Research, Redmond, WA
 *  <b>Oliver Riordan</b>  Trinity College, Cambridge, UK
 * </pre>
 * <p>
 * In their original paper, the graph continues to grow according to a certain power low until a
 * certain number of edges is reached irrespective to the number of nodes.<br>
 * However, because the target number of edges is not known beforehand, in this implementation, I
 * added another feature that enables the user to grow the curve according to that power low until
 * the target number of edges or target number of nodes is reached.
 * 
 * @author Amr ALHOSSARY
 *
 * @param <V> The Vertex class
 * @param <E> The Edge class
 */
public class DirectedScaleFreeGraphGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{
    private final Random rng;

    /**
     * probability that the new edge is from a new vertex v to an existing vertex w, where w is
     * chosen according to d_in + delta_in
     */
    private final float alpha;
    // /**probability that the new edge is from an existing vertex v to an
    // * existing vertex w, where v and w are chosen independently, v
    // * according to d_out + delta_out, and w according to d_in + delta_in.*/
    // private final float beta;
    // /**probability that the new edge is from an existing vertex v to a new vertex w,
    // * where v is chosen according to d_out + delta_out.*/
    // private final float gamma;
    /**
     * probability that the new edge is (from a new vertex v to an existing vertex w) plus the
     * probability that the new edge is (from an existing vertex v to an existing vertex w), where v
     * and w are chosen independently, v according to d_out + delta_out, and w according to d_in +
     * delta_in.<br>
     * This equals 1 - gamma. Gamma refers to the probability that the new edge is from an existing
     * vertex v to a new vertex w.
     */
    final float alphaPlusBeta;

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
    private static final boolean IN_DEGREE = true;
    private static final boolean OUT_DEGREE = false;

    /**
     * Constructs a Generator with a Random Number Generator using a random seed.
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
     * Constructs a Generator with a Random Number Generator using the given seed.
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
        // this.gamma = gamma;
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
        if (targetEdges < 0) {
            // priority is for targetNodes
            if (targetNodes < 0) {
                throw new IllegalArgumentException(
                    "can not have both targetEdges and targetNodes not set.");
            }
        }

    }

    /**
     * Generates an instance.
     * 
     * @param target the target graph
     * @param resultMap not used by this generator, can be null
     */
    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        Set<V> newNodesSet = new HashSet<>();
        Set<E> newEdgesSet = new HashSet<>();
        final int MAX_VERTEX_FAILURES = 1000;

        V initV = target.addVertex();
        newNodesSet.add(initV);

        int failuresCounter = 0;
        // grow network now
        while (targetEdges >= 0 ? targetEdges > newEdgesSet.size()
            : newNodesSet.size() <= targetNodes)
        {

            if (failuresCounter >= MAX_VERTEX_FAILURES) {
                final String errMsg =
                    failuresCounter + " successive failures more than maximum allowed number.";
                System.err.println(errMsg);
                throw new RuntimeException(errMsg);
            }

            V v, w, newV = null, newW = null;
            E e;
            float tributaries = rng.nextFloat();
            if (tributaries <= alpha) {
                // stop adding nodes if you will exceed the target
                if (targetEdges < 0 && newNodesSet.size() == targetNodes)
                    break;
                v = newV = target.addVertex();
                w = picAVertix(target, newNodesSet, newEdgesSet, IN_DEGREE, deltaIn);
            } else if (tributaries <= alphaPlusBeta) {
                v = picAVertix(target, newNodesSet, newEdgesSet, OUT_DEGREE, deltaOut);
                w = picAVertix(target, newNodesSet, newEdgesSet, IN_DEGREE, deltaIn);
            } else {// gamma
                    // stop adding nodes if you will exceed the target
                if (targetEdges < 0 && newNodesSet.size() == targetNodes)
                    break;
                v = picAVertix(target, newNodesSet, newEdgesSet, OUT_DEGREE, deltaOut);
                w = newW = target.addVertex();
            }

            if (v == null || w == null) {
                if (v == null) {
                    target.removeVertex(newW);
                }
                if (w == null) {
                    target.removeVertex(newV);
                }
                failuresCounter++;
                System.err
                    .println("WARNING: failed to pick a suitable node: Trial # " + failuresCounter);
                continue;
            } else {
                failuresCounter = 0;
            }

            final GraphType graphType = target.getType();
            // check for self loops
            if (!graphType.isAllowingSelfLoops() && v == w) {
                failuresCounter++;
                continue;
            }
            // check for multiple parallel targetEdges
            if (!graphType.isAllowingMultipleEdges() && target.containsEdge(v, w)) {
                failuresCounter++;
                continue;
            }

            e = target.addEdge(v, w);
            if (e == null) {
                // some other unknown internal error
                throw new RuntimeException("unexpected failure");
            }

            newNodesSet.add(v);
            newNodesSet.add(w);
            newEdgesSet.add(e);
        }
    }

    /**
     * @param target The target graph
     * @param allNewNodes All (new) nodes in the target graph
     * @param allNewEdgesSet All (new) edges in the target graph
     * @param directionIn <code>true</code> for inDegree or <code>false</code> for outDegree
     * @param bias deltaIn or deltaOut value according to #directioIn
     * @return the selected node.
     */
    V picAVertix(
        Graph<V, E> target, Set<V> allNewNodes, Set<E> allNewEdgesSet, boolean directionIn,
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
        float nominator;

        float r = rng.nextFloat();
        // multiply r by denominator instead of dividing all individual values by it.
        r *= denominator;
        Iterator<V> verticesIterator = allNewNodes.iterator();
        do {
            ret = verticesIterator.next();
            nominator = directionIn == IN_DEGREE ? (target.inDegreeOf(ret) + bias)
                : (target.outDegreeOf(ret) + bias);
            indicatorAccumulator += nominator;
        } while (verticesIterator.hasNext() && indicatorAccumulator < r);

        return ret;
    }

    /**A main function to be removed later
     * @param args Passed-in arguments
     */
    public static void main(String[] args)
    {
        Graph<Integer, DefaultEdge> testGraph = new DefaultDirectedGraph<Integer, DefaultEdge>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
        DirectedScaleFreeGraphGenerator<Integer, DefaultEdge> testGraphGenerator =
            new DirectedScaleFreeGraphGenerator<Integer, DefaultEdge>(0.41f, 0.05f, 0.12f, 0.12f, -1, 100);
        testGraphGenerator.generateGraph(testGraph);
        System.out.println(testGraph);
        // GraphsIO.export(testGraph, System.out);
        System.out.println("==============");

        Set<Integer> vertexSet = testGraph.vertexSet();
        System.out.println("Id\tInDegree");
        vertexSet
            .stream().sorted((v1, v2) -> testGraph.inDegreeOf(v1) - testGraph.inDegreeOf(v2))
            .forEachOrdered(v -> System.out.println("" + v + "\t" + testGraph.inDegreeOf(v)));
        System.out.println("==============");
        System.out.println("Id\tOutDegree");
        vertexSet
            .stream().sorted((v1, v2) -> testGraph.outDegreeOf(v1) - testGraph.outDegreeOf(v2))
            .forEachOrdered(v -> System.out.println("" + v + "\t" + testGraph.outDegreeOf(v)));
        System.out.println("==============");
        // distribution of outdegree where indegree == 0
        System.out.println("Id\tOutDegree [where InDegree == 0]");
        vertexSet
            .stream().filter(v -> testGraph.inDegreeOf(v) == 0)
            .sorted((v1, v2) -> testGraph.inDegreeOf(v1) - testGraph.inDegreeOf(v2))
            .forEachOrdered(v -> System.out.println("" + v + "\t" + testGraph.inDegreeOf(v)));
        System.out.println("==============");

        long inDegreeZero = vertexSet.stream().filter(v -> testGraph.inDegreeOf(v) == 0).count();
        long outDegreeZero = vertexSet.stream().filter(v -> testGraph.outDegreeOf(v) == 0).count();
        System.out.format("inDegreeZero = %d, outDegreeZero = %d\n", inDegreeZero, outDegreeZero);
    }

}
