/*
 * (C) Copyright 2015-2018, by Fabian Späh and Contributors.
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
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Tests for {@link IsomorphicGraphMapping}
 *
 * @author Alexandru Valeanu
 */
public class IsomorphicGraphMappingTest {

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

    @Test
    public void testIdentity(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);

        for (char c = 'A'; c <= 'E'; c++) {
            tree1.addVertex(String.valueOf(c));
        }

        tree1.addEdge("A", "B");
        tree1.addEdge("A", "C");
        tree1.addEdge("C", "D");
        tree1.addEdge("C", "E");

        IsomorphicGraphMapping<String, DefaultEdge> identity =
                IsomorphicGraphMapping.identity(tree1);

        Graph<String, DefaultEdge> tree2 = generateMappedGraph(tree1, identity.getForwardMapping());

        AHURootedTreeIsomorphismInspector<String, DefaultEdge> isomorphism =
                new AHURootedTreeIsomorphismInspector<>(tree1, "A",
                        tree2, identity.getVertexCorrespondence("A", true));

        Assert.assertTrue(isomorphism.isomorphismExists());
        Assert.assertTrue(areIsomorphic(tree1, tree2, identity));
    }

}