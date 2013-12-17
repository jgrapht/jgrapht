/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
/* -----------------
 * AdaptiveIsomorphismInspectorFactory.java
 * -----------------
 * (C) Copyright 2005-2008, by Assaf Lehr and Contributors.
 *
 * Original Author:  Assaf Lehr
 * Contributor(s):   -
 *
 * $Id: AdaptiveIsomorphismInspectorFactory.java 485 2006-06-26 09:12:14Z
 * perfecthash $
 *
 * Changes
 * -------
 */
package org.jgrapht.experimental.isomorphism;

import org.jgrapht.*;
import org.jgrapht.experimental.equivalence.*;
import org.jgrapht.graph.*;


/**
 * This class serves as a factory for GraphIsomorphismInspector concrete
 * implementations. It can be used in two ways:
 * <li>You can can let this class to determine what is the most efficient
 * algorithm for your graph.
 * <li>You can specify the type of your graph (planar / tree / other) and save
 * this class the graph-checking time.
 *
 * <p>Note that the concrete implementations are package-private and should not
 * be created directly. If you are the maintainer of the package, you can add
 * new implementation classes, and add them to the "check-list". The current
 * algorithms do not support graphs with multiple edges (Multigraph /
 * Pseudograph)
 *
 * @author Assaf
 * @see GraphIsomorphismInspector
 * @since Jul 17, 2005
 */
public class AdaptiveIsomorphismInspectorFactory
{
    

    public static final int GRAPH_TYPE_ARBITRARY = 0;
    public static final int GRAPH_TYPE_PLANAR = 1;
    public static final int GRAPH_TYPE_TREE = 2;
    public static final int GRAPH_TYPE_MULTIGRAPH = 3;

    

    /**
     * Creates a new inspector, letting this class determine what is the most
     * efficient algorithm.
     *
     * @param graph1
     * @param graph2
     * @param vertexChecker may be null
     * @param edgeChecker may be null
     */
    public static <V, E> GraphIsomorphismInspector createIsomorphismInspector(
        Graph<V, E> graph1,
        Graph<V, E> graph2,
        EquivalenceComparator<V, Graph<V, E>> vertexChecker,
        EquivalenceComparator<E, Graph<V, E>> edgeChecker)
    {
        int graphType = checkGraphsType(graph1, graph2);
        return createAppropriateConcreteInspector(
            graphType,
            graph1,
            graph2,
            vertexChecker,
            edgeChecker);
    }

    /**
     * Creates a new inspector, letting this class determine what is the most
     * efficient algorithm and using default equivalence comparators.
     *
     * <p>same as calling createIsomorphismInspector(graph1,graph2,null,null);
     *
     * @param graph1
     * @param graph2
     */
    public static <V, E> GraphIsomorphismInspector createIsomorphismInspector(
        Graph<V, E> graph1,
        Graph<V, E> graph2)
    {
        return createIsomorphismInspector(graph1, graph2, null, null);
    }

    /**
     * Creates a new inspector for a particular graph type (planar / tree /
     * other).
     *
     * @param type - AdaptiveIsomorphismInspectorFactory.GRAPH_TYPE_XXX
     * @param graph1
     * @param graph2
     * @param vertexChecker - can be null
     * @param edgeChecker - can be null
     */
    public static <V, E> GraphIsomorphismInspector
    createIsomorphismInspectorByType(
        int type,
        Graph<V, E> graph1,
        Graph<V, E> graph2,
        EquivalenceComparator<V, Graph<V, E>> vertexChecker,
        EquivalenceComparator<E, Graph<V, E>> edgeChecker)
    {
        return createAppropriateConcreteInspector(
            type,
            graph1,
            graph2,
            vertexChecker,
            edgeChecker);
    }

    /**
     * Creates a new inspector for a particular graph type (planar / tree /
     * other) using default equivalence comparators.
     *
     * <p>same as calling
     * createAppropriateConcreteInspector(graph1,graph2,null,null);
     *
     * @param type - AdaptiveIsomorphismInspectorFactory.GRAPH_TYPE_XXX
     * @param graph1
     * @param graph2
     */
    public static <V, E> GraphIsomorphismInspector
    createIsomorphismInspectorByType(
        int type,
        Graph<V, E> graph1,
        Graph<V, E> graph2)
    {
        return createAppropriateConcreteInspector(
            type,
            graph1,
            graph2,
            null,
            null);
    }

    /**
     * Checks the graph type, and accordingly decides which type of concrete
     * inspector class to create. This implementation creates an exhaustive
     * inspector without further tests, because no other implementations are
     * available yet.
     *
     * @param graph1
     * @param graph2
     * @param vertexChecker
     * @param edgeChecker
     */
    protected static <V, E> GraphIsomorphismInspector
    createAppropriateConcreteInspector(
        int graphType,
        Graph<V, E> graph1,
        Graph<V, E> graph2,
        EquivalenceComparator<V, Graph<V, E>> vertexChecker,
        EquivalenceComparator<E, Graph<V, E>> edgeChecker)
    {
        assertUnsupportedGraphTypes(graph1);
        assertUnsupportedGraphTypes(graph2);
        GraphIsomorphismInspector currentInspector = null;

        switch (graphType) {
        case GRAPH_TYPE_PLANAR:
        case GRAPH_TYPE_TREE:
        case GRAPH_TYPE_ARBITRARY:
            currentInspector =
                createTopologicalExhaustiveInspector(
                    graph1,
                    graph2,
                    vertexChecker,
                    edgeChecker);
            break;

        default:

            throw new IllegalArgumentException(
                "The type was not one of the supported types.");
        }
        return currentInspector;
    }

    /**
     * Checks if one of the graphs is from unsupported graph type and throws
     * IllegalArgumentException if it is. The current unsupported types are
     * graphs with multiple-edges.
     *
     * @param g
     *
     * @throws IllegalArgumentException
     */
    protected static void assertUnsupportedGraphTypes(Graph g)
        throws IllegalArgumentException
    {
        if ((g instanceof Multigraph<?, ?>)
            || (g instanceof DirectedMultigraph<?, ?>)
            || (g instanceof Pseudograph<?, ?>))
        {
            throw new IllegalArgumentException(
                "graph type not supported for the graph" + g);
        }
    }

    protected static int checkGraphsType(Graph graph1, Graph graph2)
    {
        return GRAPH_TYPE_ARBITRARY;
    }

    /**
     * @return ExhaustiveInspector, where the equivalence comparator is chained
     * with a topological comparator. This implementation uses:
     * <li>vertex degree size comparator
     */
    @SuppressWarnings("unchecked")
    protected static <V, E> GraphIsomorphismInspector
    createTopologicalExhaustiveInspector(
        Graph<V, E> graph1,
        Graph<V, E> graph2,
        EquivalenceComparator<V, Graph<V, E>> vertexChecker,
        EquivalenceComparator<E, Graph<V, E>> edgeChecker)
    {
        VertexDegreeEquivalenceComparator<V, E> degreeComparator =
            new VertexDegreeEquivalenceComparator<V, E>();
        EquivalenceComparatorChain<V, Graph<V, E>> vertexChainedChecker =
            new EquivalenceComparatorChainBase<V, Graph<V, E>>(
                degreeComparator);
        vertexChainedChecker.appendComparator(vertexChecker);

        GraphIsomorphismInspector inspector =

            // FIXME hb060208 I don't understand how to generify this, yet
            new EquivalenceIsomorphismInspector(
                graph1,
                graph2,
                vertexChainedChecker,
                edgeChecker);
        return inspector;
    }
}

// End AdaptiveIsomorphismInspectorFactory.java
