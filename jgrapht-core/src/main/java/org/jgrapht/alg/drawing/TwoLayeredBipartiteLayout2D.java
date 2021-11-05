/*
 * (C) Copyright 2020-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.drawing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.alg.drawing.model.Box2D;
import org.jgrapht.alg.drawing.model.LayoutModel2D;
import org.jgrapht.alg.drawing.model.Point2D;
import org.jgrapht.alg.interfaces.PartitioningAlgorithm.Partitioning;
import org.jgrapht.alg.partition.BipartitePartitioning;
import org.jgrapht.alg.util.Pair;

/**
 * A two layered bipartite layout.
 * 
 * The algorithm draws a bipartite graph using straight edges. Vertices are arranged along two
 * vertical or horizontal lines. No attempt is made to minimize edge crossings.
 * 
 * The order of the vertices can be adjusted by providing a vertex comparator. Similarly the user
 * can also determine the two partitions or can let the algorithm compute them.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class TwoLayeredBipartiteLayout2D<V, E>
    implements
    LayoutAlgorithm2D<V, E>
{
    protected Comparator<V> vertexComparator;
    protected boolean vertical;
    protected Set<V> partition;

    /**
     * Create a new layout
     */
    public TwoLayeredBipartiteLayout2D()
    {
        this(null, null, true);
    }

    /**
     * Create a new layout
     * 
     * @param partition one of the two partitions, can be null
     * @param vertexComparator vertex order, can be null
     * @param vertical draw on two vertical or horizontal lines
     */
    public TwoLayeredBipartiteLayout2D(
        Set<V> partition, Comparator<V> vertexComparator, boolean vertical)
    {
        this.partition = partition;
        this.vertexComparator = vertexComparator;
        this.vertical = vertical;
    }

    /**
     * Adjust the vertex comparator which specifies the vertex order.
     * 
     * @param vertexComparator the vertex comparator, or null in order to use the graph ordering
     * @return the layout algorithm instance
     */
    public TwoLayeredBipartiteLayout2D<V, E> withVertexComparator(Comparator<V> vertexComparator)
    {
        this.vertexComparator = vertexComparator;
        return this;
    }

    /**
     * Adjust whether the layout will be vertical or horizontal.
     * 
     * @param vertical if true vertical, otherwize horizontal
     * @return the layout algorithm instance
     */
    public TwoLayeredBipartiteLayout2D<V, E> withVertical(boolean vertical)
    {
        this.vertical = vertical;
        return this;
    }

    /**
     * Specify the first of the two bipartite partitions. If not provided, the algorithm computes a
     * partitioning.
     * 
     * @param partition the partition
     * @return the layout algorithm instance
     */
    public TwoLayeredBipartiteLayout2D<V, E> withFirstPartition(Set<V> partition)
    {
        this.partition = partition;
        return this;
    }

    @Override
    public void layout(Graph<V, E> graph, LayoutModel2D<V> model)
    {
        Pair<List<V>, List<V>> partitions = computePartitions(graph);

        drawFirstPartition(graph, partitions.getFirst(), model);
        drawSecondPartition(graph, partitions.getSecond(), model);
    }

    protected void drawFirstPartition(Graph<V, E> graph, List<V> partition, LayoutModel2D<V> model)
    {
        if (partition.isEmpty()) {
            throw new IllegalArgumentException("Partition cannot be empty");
        }

        Box2D drawableArea = model.getDrawableArea();
        double height = drawableArea.getHeight();
        double width = drawableArea.getWidth();
        double minX = drawableArea.getMinX();
        double minY = drawableArea.getMinY();

        int n = partition.size();
        double step = 0d;
        if (n > 1) {
            step = (vertical ? height : width) / (n - 1);
        }

        if (vertical) {
            double y = minY;
            for (V v : partition) {
                model.put(v, Point2D.of(minX, y));
                y += step;
            }
        } else {
            double x = minX;
            for (V v : partition) {
                model.put(v, Point2D.of(x, minY));
                x += step;
            }
        }
    }

    protected void drawSecondPartition(Graph<V, E> graph, List<V> partition, LayoutModel2D<V> model)
    {
        if (partition.isEmpty()) {
            throw new IllegalArgumentException("Partition cannot be empty");
        }

        Box2D drawableArea = model.getDrawableArea();
        double height = drawableArea.getHeight();
        double width = drawableArea.getWidth();
        double minX = drawableArea.getMinX();
        double minY = drawableArea.getMinY();

        int n = partition.size();
        double step = 0d;
        if (n > 1) {
            step = (vertical ? height : width) / (n - 1);
        }

        if (vertical) {
            double y = minY;
            for (V v : partition) {
                model.put(v, Point2D.of(minX + width, y));
                y += step;
            }
        } else {
            double x = minX;
            for (V v : partition) {
                model.put(v, Point2D.of(x, minY + height));
                x += step;
            }
        }

    }

    /**
     * Compute the vertex partitions.
     * 
     * @param graph the input graph
     * @return a pair of two vertex lists
     */
    protected Pair<List<V>, List<V>> computePartitions(Graph<V, E> graph)
    {
        List<V> left = new ArrayList<>();
        List<V> right = new ArrayList<>();

        if (partition != null) {
            // partition is given
            for (V v : graph.vertexSet()) {
                if (partition.contains(v)) {
                    left.add(v);
                } else {
                    right.add(v);
                }
            }
            for (E e : graph.edgeSet()) {
                V v = graph.getEdgeSource(e);
                V u = graph.getEdgeTarget(e);

                if (!(partition.contains(v) ^ partition.contains(u))) {
                    throw new IllegalArgumentException("Invalid provided bipartite partition.");
                }
            }
        } else {
            // compute partition
            Partitioning<V> partitioning = new BipartitePartitioning<V, E>(graph).getPartitioning();
            if (partitioning == null) {
                throw new IllegalArgumentException("Graph is not bipartite.");
            }
            left.addAll(partitioning.getPartition(0));
            right.addAll(partitioning.getPartition(1));
        }

        // sort by comparator
        if (vertexComparator != null) {
            left.sort(vertexComparator);
            right.sort(vertexComparator);
        }

        return Pair.of(left, right);
    }

}
