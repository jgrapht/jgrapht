/*
 * (C) Copyright 2016-2018, by Joris Kinable and Contributors.
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
package org.jgrapht.graph;

import org.jgrapht.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * A walk in a graph is an alternating sequence of vertices and edges, starting and ending at a
 * vertex, in which each edge is adjacent in the sequence to its two endpoints. More precisely, a
 * walk is a connected sequence of vertices and edges in a graph $v_0, e_0, v_1, e_1, v_2, \dotso,
 * v_{k-1}, e_{k-1}, v_{k}$, such that for $1 \leq i \leq k$, the edge $e_i$ has endpoints $v_{i-1}$
 * and $v_i$. The class makes no assumptions with respect to the shape of the walk: edges may be
 * repeated, and the start and end point of the walk may be different.
 *
 * <p>
 * See <a href="http://mathworld.wolfram.com/Walk.html">http://mathworld.wolfram.com/Walk.html</a>
 * 
 * <p>
 * GraphWalk is the default implementation of {@link GraphPath}.
 *
 * <p>
 * Two special cases exist:
 * <ol>
 * <li>A singleton GraphWalk has an empty edge list (the length of the path equals 0), the vertex
 * list contains a single vertex v, and the start and end vertex equal v.</li>
 * <li>An empty Graphwalk has empty edge and vertex lists, and the start and end vertex are both
 * null.</li>
 * </ol>
 *
 * <p>
 * This class is implemented as a light-weight data structure; this class does not verify whether
 * the sequence of edges or the sequence of vertices provided during construction forms an actual
 * walk. It is the responsibility of the invoking class to provide correct input data.
 *
 * <p>
 * Note: Serialization of a GraphWalk implies the serialization of the entire underlying graph.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Joris Kinable
 * 
 */
public class GraphWalk<V, E>
    implements
    GraphPath<V, E>,
    Serializable
{
    private static final long serialVersionUID = 7663410644865380676L;
    protected Graph<V, E> graph;

    protected List<V> vertexList;
    protected List<E> edgeList;

    protected V startVertex;

    protected V endVertex;

    protected double weight;

    /**
     * Creates a walk defined by a sequence of edges. A walk defined by its edges can be specified
     * for non-simple graphs. Edge repetition is permitted, the start and end point points ($v_0$
     * and $v_k$) can be different.
     *
     * @param graph the graph
     * @param startVertex the starting vertex
     * @param endVertex the last vertex of the path
     * @param edgeList the list of edges of the path
     * @param weight the total weight of the path
     */
    public GraphWalk(Graph<V, E> graph, V startVertex, V endVertex, List<E> edgeList, double weight)
    {
        this(graph, startVertex, endVertex, null, edgeList, weight);
    }

    /**
     * Creates a walk defined by a sequence of vertices. Note that the input graph must be simple,
     * otherwise the vertex sequence does not necessarily define a unique path. Furthermore, all
     * vertices must be pairwise adjacent.
     * 
     * @param graph the graph
     * @param vertexList the list of vertices of the path
     * @param weight the total weight of the path
     */
    public GraphWalk(Graph<V, E> graph, List<V> vertexList, double weight)
    {
        this(
            graph, (vertexList.isEmpty() ? null : vertexList.get(0)),
            (vertexList.isEmpty() ? null : vertexList.get(vertexList.size() - 1)), vertexList, null,
            weight);
    }

    /**
     * Creates a walk defined by both a sequence of edges and a sequence of vertices. Note that both
     * the sequence of edges and the sequence of vertices must describe the same path! This is not
     * verified during the construction of the walk. This constructor makes it possible to store
     * both a vertex and an edge view of the same walk, thereby saving computational overhead when
     * switching from one to the other.
     *
     * @param graph the graph
     * @param startVertex the starting vertex
     * @param endVertex the last vertex of the path
     * @param vertexList the list of vertices of the path
     * @param edgeList the list of edges of the path
     * @param weight the total weight of the path
     */
    public GraphWalk(
        Graph<V, E> graph, V startVertex, V endVertex, List<V> vertexList, List<E> edgeList,
        double weight)
    {
        // Some necessary but not sufficient conditions for valid paths
        if (vertexList == null && edgeList == null)
            throw new IllegalArgumentException("Vertex list and edge list cannot both be null!");
        if (startVertex != null && vertexList != null && edgeList != null
            && edgeList.size() + 1 != vertexList.size())
            throw new IllegalArgumentException(
                "VertexList and edgeList do not correspond to the same path (cardinality of vertexList +1 must equal the cardinality of the edgeList)");
        if (startVertex == null ^ endVertex == null)
            throw new IllegalArgumentException(
                "Either the start and end vertices must both be null, or they must both be not null (one of them is null)");

        this.graph = Objects.requireNonNull(graph);
        this.startVertex = startVertex;
        this.endVertex = endVertex;
        this.vertexList = vertexList;
        this.edgeList = edgeList;
        this.weight = weight;
    }

    @Override
    public Graph<V, E> getGraph()
    {
        return graph;
    }

    @Override
    public V getStartVertex()
    {
        return startVertex;
    }

    @Override
    public V getEndVertex()
    {
        return endVertex;
    }

    @Override
    public List<E> getEdgeList()
    {
        return (edgeList != null ? edgeList : GraphPath.super.getEdgeList());
    }

    @Override
    public List<V> getVertexList()
    {
        return (vertexList != null ? vertexList : GraphPath.super.getVertexList());
    }

    @Override
    public double getWeight()
    {
        return weight;
    }

    /**
     * Updates the weight of this walk
     * 
     * @param weight weight of the walk
     */
    public void setWeight(double weight)
    {
        this.weight = weight;
    }

    @Override
    public int getLength()
    {
        if (edgeList != null)
            return edgeList.size();
        else if (vertexList != null && !vertexList.isEmpty())
            return vertexList.size() - 1;
        else
            return 0;
    }

    @Override
    public String toString()
    {
        if (vertexList != null)
            return vertexList.toString();
        else
            return edgeList.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || !(o instanceof GraphWalk))
            return false;
        else if (this == o)
            return true;
        @SuppressWarnings("unchecked") GraphWalk<V, E> other = (GraphWalk<V, E>) o;
        if (this.isEmpty() && other.isEmpty())
            return true;
        if (!this.startVertex.equals(other.getStartVertex())
            || !this.endVertex.equals(other.getEndVertex()))
            return false;
        // If this path is expressed as a vertex list, we may get away by comparing the other path's
        // vertex list
        // This only works if its vertexList identifies a unique path in the graph
        if (this.edgeList == null && !other.getGraph().getType().isAllowingMultipleEdges())
            return this.vertexList.equals(other.getVertexList());
        else // Unlucky, we need to compare the edge lists,
            return this.getEdgeList().equals(other.getEdgeList());
    }

    @Override
    public int hashCode()
    {
        int hashCode = 1;
        if (isEmpty())
            return hashCode;

        hashCode = 31 * hashCode + startVertex.hashCode();
        hashCode = 31 * hashCode + endVertex.hashCode();

        if (edgeList != null)
            return 31 * hashCode + edgeList.hashCode();
        else
            return 31 * hashCode + vertexList.hashCode();
    }

    /**
     * Reverses the direction of the walk. In case of directed/mixed graphs, the arc directions will
     * be reversed. An exception is thrown if reversing an arc $(u,v)$ is impossible because arc
     * $(v,u)$ is not present in the graph. The weight of the resulting walk equals the sum of edge
     * weights in the walk.
     * 
     * @throws InvalidGraphWalkException if the path is invalid
     * @return a reversed GraphWalk
     */
    public GraphWalk<V, E> reverse()
    {
        return this.reverse(null);
    }

    /**
     * Reverses the direction of the walk. In case of directed/mixed graphs, the arc directions will
     * be reversed. An exception is thrown if reversing an arc $(u,v)$ is impossible because arc
     * $(v,u)$ is not present in the graph.
     * 
     * @param walkWeightCalculator Function used to calculate the weight of the reversed GraphWalk
     * @throws InvalidGraphWalkException if the path is invalid
     * @return a reversed GraphWalk
     */
    public GraphWalk<V, E> reverse(Function<GraphWalk<V, E>, Double> walkWeightCalculator)
    {
        List<V> revVertexList = null;
        List<E> revEdgeList = null;
        double revWeight = 0;

        if (vertexList != null) {
            revVertexList = new ArrayList<>(this.vertexList);
            Collections.reverse(revVertexList);
            if (graph.getType().isUndirected())
                revWeight = this.weight;

            // Check validity of the path. If the path is invalid, then calculating its weight may
            // result in an undefined exception.
            // If an edgeList is provided, then this check can be postponed to the construction of
            // the reversed edge list
            if (!graph.getType().isUndirected() && edgeList == null) {
                for (int i = 0; i < revVertexList.size() - 1; i++) {
                    V u = revVertexList.get(i);
                    V v = revVertexList.get(i + 1);
                    E edge = graph.getEdge(u, v);
                    if (edge == null)
                        throw new InvalidGraphWalkException(
                            "this walk cannot be reversed. The graph does not contain a reverse arc for arc "
                                + graph.getEdge(v, u));
                    else
                        revWeight += graph.getEdgeWeight(edge);
                }
            }
        }

        if (edgeList != null) {
            revEdgeList = new ArrayList<>(this.edgeList.size());

            if (graph.getType().isUndirected()) {
                revEdgeList.addAll(this.edgeList);
                Collections.reverse(revEdgeList);
                revWeight = this.weight;
            } else {
                ListIterator<E> listIterator = this.edgeList.listIterator(edgeList.size());
                while (listIterator.hasPrevious()) {
                    E e = listIterator.previous();
                    V u = graph.getEdgeSource(e);
                    V v = graph.getEdgeTarget(e);
                    E revEdge = graph.getEdge(v, u);
                    if (revEdge == null)
                        throw new InvalidGraphWalkException(
                            "this walk cannot be reversed. The graph does not contain a reverse arc for arc "
                                + e);
                    revEdgeList.add(revEdge);
                    revWeight += graph.getEdgeWeight(revEdge);
                }
            }
        }
        // Update weight of reversed walk
        GraphWalk<V, E> gw = new GraphWalk<>(
            this.graph, this.endVertex, this.startVertex, revVertexList, revEdgeList, 0);
        if (walkWeightCalculator == null)
            gw.weight = revWeight;
        else
            gw.weight = walkWeightCalculator.apply(gw);
        return gw;
    }

    /**
     * Concatenates the specified GraphWalk to the end of this GraphWalk. This action can only be
     * performed if the end vertex of this GraphWalk is the same as the start vertex of the
     * extending GraphWalk
     * 
     * @param extension GraphPath used for the concatenation.
     * @param walkWeightCalculator Function used to calculate the weight of the GraphWalk obtained
     *        after the concatenation.
     * @return a GraphWalk that represents the concatenation of this object's walk followed by the
     *         walk specified in the extension argument.
     */
    public GraphWalk<V, E> concat(
        GraphWalk<V, E> extension, Function<GraphWalk<V, E>, Double> walkWeightCalculator)
    {
        if (this.isEmpty())
            throw new IllegalArgumentException("An empty path cannot be extended");
        if (!this.endVertex.equals(extension.getStartVertex()))
            throw new IllegalArgumentException(
                "This path can only be extended by another path if the end vertex of the orginal path and the start vertex of the extension are equal.");

        List<V> concatVertexList = null;
        List<E> concatEdgeList = null;

        if (vertexList != null) {
            concatVertexList = new ArrayList<>(this.vertexList);
            List<V> vertexListExtension = extension.getVertexList();
            concatVertexList.addAll(vertexListExtension.subList(1, vertexListExtension.size()));
        }

        if (edgeList != null) {
            concatEdgeList = new ArrayList<>(this.edgeList);
            concatEdgeList.addAll(extension.getEdgeList());
        }

        GraphWalk<V, E> gw = new GraphWalk<>(
            this.graph, startVertex, extension.getEndVertex(), concatVertexList, concatEdgeList, 0);
        gw.setWeight(walkWeightCalculator.apply(gw));
        return gw;
    }

    /**
     * Returns true if the path is an empty path, that is, a path with startVertex=endVertex=null
     * and with an empty vertex and edge list.
     * 
     * @return Returns true if the path is an empty path.
     */
    public boolean isEmpty()
    {
        return startVertex == null;
    }

    /**
     * Convenience method which verifies whether the given path is feasible wrt the input graph and
     * forms an actual path.
     * 
     * @throws InvalidGraphWalkException if the path is invalid
     */
    public void verify()
    {

        if (isEmpty()) // Empty path
            return;

        if (vertexList != null && !vertexList.isEmpty()) {
            if (!startVertex.equals(vertexList.get(0)))
                throw new InvalidGraphWalkException(
                    "The start vertex must be the first vertex in the vertex list");
            if (!endVertex.equals(vertexList.get(vertexList.size() - 1)))
                throw new InvalidGraphWalkException(
                    "The end vertex must be the last vertex in the vertex list");
            // All vertices and edges in the path must be contained in the graph
            if (!graph.vertexSet().containsAll(vertexList))
                throw new InvalidGraphWalkException(
                    "Not all vertices in the path are contained in the graph");

            if (edgeList == null) {
                // Verify sequence
                Iterator<V> it = vertexList.iterator();
                V u = it.next();
                while (it.hasNext()) {
                    V v = it.next();
                    if (graph.getEdge(u, v) == null)
                        throw new InvalidGraphWalkException(
                            "The vertexList does not constitute to a feasible path. Edge (" + u
                                + "," + v + " does not exist in the graph.");
                    u = v;
                }
            }
        }

        if (edgeList != null && !edgeList.isEmpty()) {
            if (!Graphs.testIncidence(graph, edgeList.get(0), startVertex))
                throw new InvalidGraphWalkException(
                    "The first edge in the edge list must leave the start vertex");
            if (!graph.edgeSet().containsAll(edgeList))
                throw new InvalidGraphWalkException(
                    "Not all edges in the path are contained in the graph");

            if (vertexList == null) {
                V u = startVertex;
                for (E edge : edgeList) {
                    if (!Graphs.testIncidence(graph, edge, u))
                        throw new InvalidGraphWalkException(
                            "The edgeList does not constitute to a feasible path. Conflicting edge: "
                                + edge);
                    u = Graphs.getOppositeVertex(graph, edge, u);
                }
                if (!u.equals(endVertex))
                    throw new InvalidGraphWalkException(
                        "The path defined by the edgeList does not end in the endVertex.");
            }
        }

        if (vertexList != null && edgeList != null) {
            // Verify that the path is an actual path in the graph
            if (edgeList.size() + 1 != vertexList.size())
                throw new InvalidGraphWalkException(
                    "VertexList and edgeList do not correspond to the same path (cardinality of vertexList +1 must equal the cardinality of the edgeList)");

            for (int i = 0; i < vertexList.size() - 1; i++) {
                V u = vertexList.get(i);
                V v = vertexList.get(i + 1);
                E edge = getEdgeList().get(i);

                if (graph.getType().isDirected()) { // Directed graph
                    if (!graph.getEdgeSource(edge).equals(u)
                        || !graph.getEdgeTarget(edge).equals(v))
                        throw new InvalidGraphWalkException(
                            "VertexList and edgeList do not form a feasible path");
                } else { // Undirected or mixed
                    if (!Graphs.testIncidence(graph, edge, u)
                        || !Graphs.getOppositeVertex(graph, edge, u).equals(v))
                        throw new InvalidGraphWalkException(
                            "VertexList and edgeList do not form a feasible path");
                }
            }
        }
    }

    /**
     * Convenience method which creates an empty walk.
     * 
     * @param graph input graph
     * @param <V> vertex type
     * @param <E> edge type
     * @return an empty walk
     */
    public static <V, E> GraphWalk<V, E> emptyWalk(Graph<V, E> graph)
    {
        return new GraphWalk<>(
            graph, null, null, Collections.emptyList(), Collections.emptyList(), 0.0);
    }

    /**
     * Convenience method which creates a walk consisting of a single vertex with weight 0.0.
     * 
     * @param graph input graph
     * @param v single vertex
     * @param <V> vertex type
     * @param <E> edge type
     * @return an empty walk
     */
    public static <V, E> GraphWalk<V, E> singletonWalk(Graph<V, E> graph, V v)
    {
        return singletonWalk(graph, v, 0d);
    }

    /**
     * Convenience method which creates a walk consisting of a single vertex.
     * 
     * @param graph input graph
     * @param v single vertex
     * @param weight weight of the path
     * @param <V> vertex type
     * @param <E> edge type
     * @return an empty walk
     */
    public static <V, E> GraphWalk<V, E> singletonWalk(Graph<V, E> graph, V v, double weight)
    {
        return new GraphWalk<>(
            graph, v, v, Collections.singletonList(v), Collections.emptyList(), weight);
    }

}

/**
 * Exception thrown in the event that the path is invalid.
 */
class InvalidGraphWalkException
    extends
    RuntimeException
{
    private static final long serialVersionUID = 3811666107707436479L;

    public InvalidGraphWalkException(String message)
    {
        super(message);
    }

}
