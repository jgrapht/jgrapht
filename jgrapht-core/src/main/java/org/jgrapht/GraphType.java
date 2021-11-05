/*
 * (C) Copyright 2017-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht;

/**
 * A graph type.
 * 
 * <p>
 * The graph type describes various properties of a graph such as whether it is directed, undirected
 * or mixed, whether it contain self-loops (a self-loop is an edge where the source vertex is the
 * same as the target vertex), whether it contain multiple (parallel) edges (multiple edges which
 * connect the same pair of vertices) and whether it is weighted or not.
 * 
 * <p>
 * The type of a graph can be queried on runtime using method {@link Graph#getType()}. This way, for
 * example, an algorithm can have different behavior based on whether the input graph is directed or
 * undirected, etc.
 * 
 * @author Dimitrios Michail
 */
public interface GraphType
{
    /**
     * Returns true if all edges of the graph are directed, false otherwise.
     * 
     * @return true if all edges of the graph are directed, false otherwise
     */
    boolean isDirected();

    /**
     * Returns true if all edges of the graph are undirected, false otherwise.
     * 
     * @return true if all edges of the graph are undirected, false otherwise
     */
    boolean isUndirected();

    /**
     * Returns true if the graph contain both directed and undirected edges, false otherwise.
     * 
     * @return true if the graph contain both directed and undirected edges, false otherwise
     */
    boolean isMixed();

    /**
     * Returns <code>true</code> if and only if multiple (parallel) edges are allowed in this graph.
     * The meaning of multiple edges is that there can be many edges going from vertex v1 to vertex
     * v2.
     *
     * @return <code>true</code> if and only if multiple (parallel) edges are allowed.
     */
    boolean isAllowingMultipleEdges();

    /**
     * Returns <code>true</code> if and only if self-loops are allowed in this graph. A self loop is
     * an edge that its source and target vertices are the same.
     *
     * @return <code>true</code> if and only if graph self-loops are allowed.
     */
    boolean isAllowingSelfLoops();

    /**
     * Returns <code>true</code> if and only if cycles are allowed in this graph.
     *
     * @return <code>true</code> if and only if graph cycles are allowed.
     */
    boolean isAllowingCycles();

    /**
     * Returns <code>true</code> if and only if the graph supports edge weights.
     *
     * @return <code>true</code> if the graph supports edge weights, <code>false</code> otherwise.
     */
    boolean isWeighted();

    /**
     * Returns <code>true</code> if the graph is simple, <code>false</code> otherwise.
     * 
     * @return <code>true</code> if the graph is simple, <code>false</code> otherwise
     */
    boolean isSimple();

    /**
     * Returns <code>true</code> if the graph is a pseudograph, <code>false</code> otherwise.
     * 
     * @return <code>true</code> if the graph is a pseudograph, <code>false</code> otherwise
     */
    boolean isPseudograph();

    /**
     * Returns <code>true</code> if the graph is a multigraph, <code>false</code> otherwise.
     * 
     * @return <code>true</code> if the graph is a multigraph, <code>false</code> otherwise
     */
    boolean isMultigraph();

    /**
     * Returns <code>true</code> if the graph is modifiable, <code>false</code> otherwise.
     * 
     * @return <code>true</code> if the graph is modifiable, <code>false</code> otherwise
     */
    boolean isModifiable();

    /**
     * Create a directed variant of the current graph type.
     * 
     * @return a directed variant of the current graph type
     */
    GraphType asDirected();

    /**
     * Create an undirected variant of the current graph type.
     * 
     * @return an undirected variant of the current graph type
     */
    GraphType asUndirected();

    /**
     * Create a mixed variant of the current graph type.
     * 
     * @return a mixed variant of the current graph type
     */
    GraphType asMixed();

    /**
     * Create an unweighted variant of the current graph type.
     * 
     * @return an unweighted variant of the current graph type
     */
    GraphType asUnweighted();

    /**
     * Create a weighted variant of the current graph type.
     * 
     * @return a weighted variant of the current graph type
     */
    GraphType asWeighted();

    /**
     * Create a modifiable variant of the current graph type.
     * 
     * @return a modifiable variant of the current graph type
     */
    GraphType asModifiable();

    /**
     * Create an unmodifiable variant of the current graph type.
     * 
     * @return a unmodifiable variant of the current graph type
     */
    GraphType asUnmodifiable();
}
