/*
 * (C) Copyright 2010-2019, by Michael Behrisch and Contributors. 
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
package org.jgrapht.nio.dimacs;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.jgrapht.Graph;
import org.jgrapht.alg.util.Triple;
import org.jgrapht.nio.BaseEventDrivenImporter;
import org.jgrapht.nio.GraphImporter;
import org.jgrapht.nio.ImportException;

/**
 * Imports a graph specified in DIMACS format.
 *
 * <p>
 * See {@link DIMACSFormat} for a description of all the supported DIMACS formats.
 *
 * <p>
 * In summary, one of the most common DIMACS formats was used in the
 * <a href="http://mat.gsia.cmu.edu/COLOR/general/ccformat.ps">2nd DIMACS challenge</a> and follows
 * the following structure:
 * 
 * <pre>
 * {@code
 * DIMACS G {
 *    c <comments> ignored during parsing of the graph
 *    p edge <number of nodes> <number of edges>
 *    e <edge source 1> <edge target 1>
 *    e <edge source 2> <edge target 2>
 *    e <edge source 3> <edge target 3>
 *    e <edge source 4> <edge target 4>
 *    ...
 * }
 * }
 * </pre>
 * 
 * Although not specified directly in the DIMACS format documentation, this implementation also
 * allows for the a weighted variant:
 * 
 * <pre>
 * {@code 
 * e <edge source 1> <edge target 1> <edge_weight> 
 * }
 * </pre>
 * 
 * Note: the current implementation does not fully implement the DIMACS specifications! Special
 * (rarely used) fields specified as 'Optional Descriptors' are currently not supported (ignored).
 *
 * @author Michael Behrisch (adaptation of GraphReader class)
 * @author Joris Kinable
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class DIMACSImporter<V, E>
    extends
    BaseEventDrivenImporter<V, E>
    implements
    GraphImporter<V, E>
{
    private final double defaultWeight;

    /**
     * Construct a new DIMACSImporter
     * 
     * @param defaultWeight default edge weight
     */
    public DIMACSImporter(double defaultWeight)
    {
        super();
        this.defaultWeight = defaultWeight;
    }

    /**
     * Construct a new DIMACSImporter
     */
    public DIMACSImporter()
    {
        this(Graph.DEFAULT_EDGE_WEIGHT);
    }

    /**
     * Import a graph.
     * 
     * <p>
     * The provided graph must be able to support the features of the graph that is read. For
     * example if the file contains self-loops then the graph provided must also support self-loops.
     * The same for multiple edges.
     * 
     * <p>
     * If the provided graph is a weighted graph, the importer also reads edge weights. Otherwise
     * edge weights are ignored.
     * 
     * @param graph the output graph
     * @param input the input reader
     * @throws ImportException in case an error occurs, such as I/O or parse error
     */
    @Override
    public void importGraph(Graph<V, E> graph, Reader input)
        throws ImportException
    {
        DIMACSEventDrivenImporter genericImporter = new DIMACSEventDrivenImporter().renumberVertices(false);
        Consumers consumers = new Consumers(graph);
        genericImporter.addVertexCountConsumer(consumers.nodeCountConsumer);
        genericImporter.addEdgeConsumer(consumers.edgeConsumer);
        genericImporter.importInput(input);
    }

    private class Consumers
    {
        private Graph<V, E> graph;
        private Integer nodeCount;
        private Map<Integer, V> map;

        public Consumers(Graph<V, E> graph)
        {
            this.graph = graph;
            this.nodeCount = null;
            this.map = new HashMap<Integer, V>();
        }

        public final Consumer<Integer> nodeCountConsumer = (n) -> {
            this.nodeCount = n;
            for (int i = 0; i < nodeCount; i++) {
                map.put(Integer.valueOf(i), graph.addVertex());
            }
        };

        public final Consumer<Triple<Integer, Integer, Double>> edgeConsumer = (t) -> {
            int source = t.getFirst();
            V from = map.get(t.getFirst());
            if (from == null) {
                throw new ImportException("Node " + source + " does not exist");
            }

            int target = t.getSecond();
            V to = map.get(target);
            if (to == null) {
                throw new ImportException("Node " + target + " does not exist");
            }

            E e = graph.addEdge(from, to);
            if (graph.getType().isWeighted()) {
                double weight = t.getThird() == null ? defaultWeight : t.getThird();
                graph.setEdgeWeight(e, weight);
            }
        };

    }

}
