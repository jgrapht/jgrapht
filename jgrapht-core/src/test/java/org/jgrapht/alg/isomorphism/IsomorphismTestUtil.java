/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht.alg.isomorphism;

import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.generate.BarabasiAlbertForestGenerator;
import org.jgrapht.generate.BarabasiAlbertGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;

import java.util.*;

/**
 * Utility class for isomorphism tests
 *
 * @author Alexandru Valeanu
 */
public class IsomorphismTestUtil {

    public static Graph<Integer, DefaultEdge> parseGraph(String vertices, String edges){
        Graph<Integer, DefaultEdge> forest = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(-100),
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        vertices = vertices.substring(1, vertices.length() - 1);

        for (String s: vertices.split(", "))
            forest.addVertex(Integer.valueOf(s));

        edges = edges.substring(1, edges.length() - 1);

        for (String s: edges.split(", ")){
            String[] ends = s.substring(1, s.length() - 1).split(",");
            forest.addEdge(Integer.valueOf(ends[0]), Integer.valueOf(ends[1]));
        }

        return forest;
    }

    public static Pair<Graph<Integer, DefaultEdge>, Graph<Integer, DefaultEdge>> parseGraph(String vertices, String edges,
                                                                                            String mapping, Map<Integer, Integer> map){

        Graph<Integer, DefaultEdge> forest = parseGraph(vertices, edges);

        for (String s: mapping.substring(1, mapping.length() - 1).split(", ")){
            String[] ends = s.split("=");
            map.put(Integer.valueOf(ends[0]), Integer.valueOf(ends[1]));
        }

        return Pair.of(forest, generateMappedGraph(forest, map));
    }

    public static Graph<Integer, DefaultEdge> generateForest(int N, Random random){
        BarabasiAlbertForestGenerator<Integer, DefaultEdge> generator =
                new BarabasiAlbertForestGenerator<>(N / 10, N, random);

        Graph<Integer, DefaultEdge> forest = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        generator.generateGraph(forest);

        return forest;
    }

    public static Pair<Graph<Integer, DefaultEdge>, Map<Integer, Integer>>
    generateIsomorphicGraph(Graph<Integer, DefaultEdge> graph, Random random){
        List<Integer> permutation = new ArrayList<>(graph.vertexSet().size());

        for (int i = 0; i < graph.vertexSet().size(); i++) {
            permutation.add(i);
        }

        Collections.shuffle(permutation, random);

        List<Integer> vertexList = new ArrayList<>(graph.vertexSet());
        Map<Integer, Integer> mapping = new HashMap<>();

        for (int i = 0; i < graph.vertexSet().size(); i++) {
            mapping.put(vertexList.get(i), vertexList.get(permutation.get(i)));
        }

        return Pair.of(generateMappedGraph(graph, mapping), mapping);
    }

    public static Graph<Integer, DefaultEdge> generateTree(int N, Random random){
        BarabasiAlbertGraphGenerator<Integer, DefaultEdge> generator =
                new BarabasiAlbertGraphGenerator<>(1, 1, N - 1, random);

        Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        generator.generateGraph(tree);

        return tree;
    }

    public static <V, E> boolean areIsomorphic(Graph<V, E> graph1, Graph<V, E> graph2,
                                               IsomorphicGraphMapping<V, E> mapping){
        for (V v: graph1.vertexSet()){
            if (!mapping.getForwardMapping().containsKey(v) ||
                    !graph2.containsVertex(mapping.getForwardMapping().get(v)))
                return false;
        }

        for (V v: graph2.vertexSet()){
            if (!mapping.getBackwardMapping().containsKey(v) ||
                    !graph1.containsVertex(mapping.getBackwardMapping().get(v)))
                return false;
        }

        for (E edge: graph1.edgeSet()){
            E e = mapping.getEdgeCorrespondence(edge, true);
            V u = graph1.getEdgeSource(e);
            V v = graph1.getEdgeTarget(e);

            if (!graph2.containsEdge(u, v))
                return false;
        }

        for (E edge: graph2.edgeSet()){
            E e = mapping.getEdgeCorrespondence(edge, false);
            V u = graph2.getEdgeSource(e);
            V v = graph2.getEdgeTarget(e);

            if (!graph1.containsEdge(u, v))
                return false;
        }

        return true;
    }

    public static <V> Graph<V, DefaultEdge> generateMappedGraph(Graph<V, DefaultEdge> graph,
                                                                Map<V, V> mapping){

        SimpleGraph<V, DefaultEdge> isoGraph = new SimpleGraph<>(graph.getVertexSupplier(),
                graph.getEdgeSupplier(), false);

        for (V v: graph.vertexSet())
            isoGraph.addVertex(mapping.get(v));

        for (DefaultEdge edge: graph.edgeSet()){
            V u = graph.getEdgeSource(edge);
            V v = graph.getEdgeTarget(edge);

            isoGraph.addEdge(mapping.get(u), mapping.get(v));
        }

        return isoGraph;
    }
}
