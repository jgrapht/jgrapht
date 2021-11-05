/*
 * (C) Copyright 2019-2021, by Joris Kinable, Jon Robison, Thomas Breitbart and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;

/**
 * Base test class for the heuristic search algorithms.
 *
 * @author Joris Kinable
 * @author Jon Robison
 * @author Thomas Breitbart
 */
public class BaseHeuristicSearchTest
{
    protected final String[] labyrinth1 =
        { ". . . . . . . . . . . . . . . . . . . . . ####. . . . . . .",
            ". . . . . . . . . . . . . . . . . . . . . ####. . . . . . .",
            ". . . . . . . . . . . . . . . . . . . . . ####. . . . . . .",
            ". . . ####. . . . . . . . . . . . . . . . ####. . . . . . .",
            ". . . ####. . . . . . . . ####. . . . . . ####T . . . . . .",
            ". . . ####. . . . . . . . ####. . . . . . ##########. . . .",
            ". . . ####. . . . . . . . ####. . . . . . ##########. . . .",
            ". . . ####. . . . . . . . ####. . . . . . . . . . . . . . .",
            ". . . ####. . . . . . . . ####. . . . . . . . . . . . . . .",
            ". . . ####. . . . . . . . ####. . . . . . . . . . . . . . .",
            ". . . ####. . . . . . . . ####. . . . . . . . . . . . . . .",
            ". . . ####. . . . . . . . ####. . . . . . . . . . . . . . .",
            ". . . . . . . . . . . . . ####. . . . . . . . . . . . . . .",
            ". . . . . . . . . . . . . ####. . . . . . . . . . . . . . .",
            "S . . . . . . . . . . . . ####. . . . . . . . . . . . . . ." };

    protected final String[] labyrinth2 = { // Target node is unreachable
        ". . . . . . . . . . . . . . . . . . . . . ####. . . . . . .",
        ". . . . . . . . . . . . . . . . . . . . . ####. . . . . . .",
        ". . . . . . . . . . . . . . . . . . . . . ####. . . . . . .",
        ". . . ####. . . . . . . . . . . . . . . . ####### . . . . .",
        ". . . ####. . . . . . . . ####. . . . . . ####T## . . . . .",
        ". . . ####. . . . . . . . ####. . . . . . ##########. . . .",
        ". . . ####. . . . . . . . ####. . . . . . ##########. . . .",
        ". . . ####. . . . . . . . ####. . . . . . . . . . . . . . .",
        ". . . ####. . . . . . . . ####. . . . . . . . . . . . . . .",
        ". . . ####. . . . . . . . ####. . . . . . . . . . . . . . .",
        ". . . ####. . . . . . . . ####. . . . . . . . . . . . . . .",
        ". . . ####. . . . . . . . ####. . . . . . . . . . . . . . .",
        ". . . . . . . . . . . . . ####. . . . . . . . . . . . . . .",
        ". . . . . . . . . . . . . ####. . . . . . . . . . . . . . .",
        "S . . . . . . . . . . . . ####. . . . . . . . . . . . . . ." };
    protected Graph<Node, DefaultWeightedEdge> graph;
    protected Node sourceNode;
    protected Node targetNode;
    protected Node n1;
    protected Node n3;

    protected void readLabyrinth(String[] labyrinth)
    {
        graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        // Create the nodes
        Node[][] nodes = new Node[labyrinth.length][labyrinth[0].length()];
        for (int i = 0; i < labyrinth.length; i++) {
            for (int j = 0; j < labyrinth[0].length(); j++) {
                if (labyrinth[i].charAt(j) == '#' || labyrinth[i].charAt(j) == ' ')
                    continue;
                nodes[i][j] = new Node(i, j / 2);
                graph.addVertex(nodes[i][j]);
                if (labyrinth[i].charAt(j) == 'S')
                    sourceNode = nodes[i][j];
                else if (labyrinth[i].charAt(j) == 'T')
                    targetNode = nodes[i][j];
            }
        }
        // Create the edges
        // a. Horizontal edges
        for (int i = 0; i < labyrinth.length; i++) {
            for (int j = 0; j < labyrinth[0].length() - 2; j++) {
                if (nodes[i][j] == null || nodes[i][j + 2] == null)
                    continue;
                Graphs.addEdge(graph, nodes[i][j], nodes[i][j + 2], 1);
            }
        }
        // b. Vertical edges
        for (int i = 0; i < labyrinth.length - 1; i++) {
            for (int j = 0; j < labyrinth[0].length(); j++) {
                if (nodes[i][j] == null || nodes[i + 1][j] == null)
                    continue;
                Graphs.addEdge(graph, nodes[i][j], nodes[i + 1][j], 1);
            }
        }
    }

    protected Graph<Node, DefaultWeightedEdge> getMultigraph()
    {
        WeightedMultigraph<Node, DefaultWeightedEdge> multigraph =
            new WeightedMultigraph<>(DefaultWeightedEdge.class);
        n1 = new Node(0, 0);
        multigraph.addVertex(n1);
        Node n2 = new Node(1, 0);
        multigraph.addVertex(n2);
        n3 = new Node(2, 0);
        multigraph.addVertex(n3);
        Graphs.addEdge(multigraph, n1, n2, 5.0);
        Graphs.addEdge(multigraph, n1, n2, 4.0);
        Graphs.addEdge(multigraph, n1, n2, 8.0);
        Graphs.addEdge(multigraph, n2, n3, 7.0);
        Graphs.addEdge(multigraph, n2, n3, 9);
        Graphs.addEdge(multigraph, n2, n3, 2);
        return multigraph;
    }

    protected Graph<Integer, DefaultWeightedEdge> getInconsistentHeuristicTestGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        graph.addVertex(0);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);

        graph.setEdgeWeight(graph.addEdge(0, 1), 0.5822723681370429);
        graph.setEdgeWeight(graph.addEdge(0, 3), 0.8512429683406786);
        graph.setEdgeWeight(graph.addEdge(3, 0), 0.22867383417976428);
        graph.setEdgeWeight(graph.addEdge(1, 2), 0.1531858692059932);
        graph.setEdgeWeight(graph.addEdge(3, 1), 0.9639222864568235);
        graph.setEdgeWeight(graph.addEdge(2, 2), 0.23262564370920258);
        graph.setEdgeWeight(graph.addEdge(2, 2), 0.6166416559599189);
        graph.setEdgeWeight(graph.addEdge(3, 3), 0.6088954021459719);
        graph.setEdgeWeight(graph.addEdge(3, 3), 0.2476189990121238);
        return graph;
    }

    protected AStarAdmissibleHeuristic<Integer> getInconsistentHeuristic()
    {
        return (s, t) -> {
            if (s == 0 && t == 1) {
                // actual = 0.5822723681370429
                return 0.5822723681370429;
            }
            if (s == 3 && t == 1) {
                // actual = 0.8109462023168071
                return 0.8109462023168071;
            }
            if (s == 3 && t == 2) {
                // actual = 0.9641320715228003
                return 0.9639222864568235;
            }
            if (s == 0 && t == 2) {
                // actual = 0.7354582373430361
                return 0.7354582373430361;
            }
            // all other zero
            return 0d;
        };
    }

    public static class Node
    {
        public final int x;
        public final int y;

        Node(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        public String toString()
        {
            return "(" + x + "," + y + ")";
        }
    }

    public static class ManhattanDistance
        implements
        AStarAdmissibleHeuristic<Node>
    {
        @Override
        public double getCostEstimate(Node sourceVertex, Node targetVertex)
        {
            return Math.abs(sourceVertex.x - targetVertex.x)
                + Math.abs(sourceVertex.y - targetVertex.y);
        }

        @Override
        public <E> boolean isConsistent(Graph<Node, E> graph)
        {
            return true;
        }
    }

    public static class EuclideanDistance
        implements
        AStarAdmissibleHeuristic<Node>
    {
        @Override
        public double getCostEstimate(Node sourceVertex, Node targetVertex)
        {
            return Math
                .sqrt(
                    Math.pow(sourceVertex.x - targetVertex.x, 2)
                        + Math.pow(sourceVertex.y - targetVertex.y, 2));
        }

        @Override
        public <E> boolean isConsistent(Graph<Node, E> graph)
        {
            return true;
        }
    }
}
