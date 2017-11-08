/*
 * (C) Copyright 2003-2017, by Liviu Rau and Contributors.
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
package org.jgrapht.traverse;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.util.IntegerVertexFactory;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.graph.*;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the {@link DepthFirstIteratorTest} class.
 *
 * <p>
 * NOTE: This test uses hard-coded expected ordering isn't really guaranteed by the specification of
 * the algorithm. This could cause false failures if the traversal implementation changes.
 * </p>
 *
 * @author Liviu Rau, Patrick Sharp
 * @since Jul 30, 2003
 */
public class DepthFirstIteratorTest
    extends CrossComponentIteratorTest
{
    // ~ Methods ----------------------------------------------------------------

    @Override
    String getExpectedStr1()
    {
        return "1,3,6,5,7,9,4,8,2";
    }

    @Override
    String getExpectedStr2()
    {
        return "1,3,6,5,7,9,4,8,2,orphan";
    }

    @Override
    String getExpectedFinishString()
    {
        return "1:3:6:5:7:9:4:8:2:orphan:";
    }
    @Override
    String getExpectedCCStr1() {
        return "orphan";
    }

    @Override
    String getExpectedCCStr2() {
        return "orphan,7,9,4,8,2";
    }

    @Override
    String getExpectedCCStr3() {
        return "orphan,7,9,4,8,2,3,6,1,5";
    }

    @Override
    String getExpectedCCFinishString() {
        return "orphan:7:9:4:8:2:3:6:1:5:";
    }

    @Override
    AbstractGraphIterator<String, DefaultWeightedEdge> createIterator(Graph<String, DefaultWeightedEdge> g, Iterable<String> startVertex) {
        return new DepthFirstIterator<>(g, startVertex);
    }

    @Override
    AbstractGraphIterator<String, DefaultWeightedEdge> createIterator(
        Graph<String, DefaultWeightedEdge> g, String vertex)
    {
        AbstractGraphIterator<String, DefaultWeightedEdge> i = new DepthFirstIterator<>(g, vertex);
        i.setCrossComponentTraversal(true);

        return i;
    }



    /**
     * See <a href="http://sf.net/projects/jgrapht">Sourceforge bug 1169182</a> for details.
     */
    public void testBug1169182()
    {
        Graph<String, DefaultEdge> dg = new DefaultDirectedGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String d = "D";
        String e = "E";
        String f = "F";
        String g = "G";
        String h = "H";
        String i = "I";
        String j = "J";
        String k = "K";
        String l = "L";

        dg.addVertex(a);
        dg.addVertex(b);
        dg.addVertex(c);
        dg.addVertex(d);
        dg.addVertex(e);
        dg.addVertex(f);
        dg.addVertex(g);
        dg.addVertex(h);
        dg.addVertex(i);
        dg.addVertex(j);
        dg.addVertex(k);
        dg.addVertex(l);

        dg.addEdge(a, b);
        dg.addEdge(b, c);
        dg.addEdge(c, j);
        dg.addEdge(c, d);
        dg.addEdge(c, e);
        dg.addEdge(c, f);
        dg.addEdge(c, g);
        dg.addEdge(d, h);
        dg.addEdge(e, h);
        dg.addEdge(f, i);
        dg.addEdge(g, i);
        dg.addEdge(h, j);
        dg.addEdge(i, c);
        dg.addEdge(j, k);
        dg.addEdge(k, l);

        Iterator<String> dfs = new DepthFirstIterator<>(dg);
        String actual = "";
        while (dfs.hasNext()) {
            String v = dfs.next();
            actual += v;
        }

        String expected = "ABCGIFEHJKLD";
        assertEquals(expected, actual);
    }


    public void testWikiGraph(){
        Graph<Integer, DefaultEdge> g=new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12));
        g.addEdge(1,8);
        g.addEdge(1,7);
        g.addEdge(1,2);
        g.addEdge(2,6);
        g.addEdge(2,3);
        g.addEdge(3,5);
        g.addEdge(3,4);
        g.addEdge(8,12);
        g.addEdge(8,9);
        g.addEdge(9,11);
        g.addEdge(9,10);

        List<Integer> result = new ArrayList<>();
        DepthFirstIterator<Integer, DefaultEdge> dfs=new DepthFirstIterator<>(g, 1);
        dfs.forEachRemaining(result::add);
        List<Integer> expected=recursiveDFS(g, 1);
        assertEquals(expected, result);


        int[] depth= {0,1,2,3,3,2,1,1,2,3,3,2};
        Integer[] parent={null,1,2,3,3,2,1,1,8,9,9,8};
        DefaultEdge[] edges={null, g.getEdge(1,2), g.getEdge(2,3), g.getEdge(3,4), g.getEdge(3,5), g.getEdge(2,6), g.getEdge(1,7), g.getEdge(1,8), g.getEdge(8,9),g.getEdge(9,10), g.getEdge(9,11), g.getEdge(8,12)};
        for(int i=1; i<13; i++){
            assertEquals(depth[i-1], dfs.getDepth(i));
            assertEquals(parent[i-1], dfs.getParent(i));
            assertEquals(edges[i-1], dfs.getEdgeToParent(i));
        }
    }

    public void testMultiComponentGraph(){
        Graph<Integer, DefaultEdge> g=new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1,2,3,4,5,6));
        g.addEdge(1,2);
        g.addEdge(2,3);
        g.addEdge(3,1);
        g.addEdge(4,5);
        g.addEdge(4,6);

        List<Integer> result = new ArrayList<>();
        DepthFirstIterator<Integer, DefaultEdge> dfs=new DepthFirstIterator<>(g);
        dfs.forEachRemaining(result::add);
        List<Integer> expected=recursiveDFS(g);
        assertEquals(expected, result);

        int[] depth= {0,2,1,0,1,1};
        Integer[] parent={null, 3, 1, null, 4, 4};
        DefaultEdge[] edges={null, g.getEdge(2,3), g.getEdge(3,1), null, g.getEdge(4,5), g.getEdge(4,6)};
        for(int i=1; i<7; i++){
            assertEquals(depth[i-1], dfs.getDepth(i));
            assertEquals(parent[i-1], dfs.getParent(i));
            assertEquals(edges[i-1], dfs.getEdgeToParent(i));
        }
    }

    public void testRandomGraph(){
        GnpRandomGraphGenerator<Integer, DefaultEdge> gnp=new GnpRandomGraphGenerator<>(500,.6, 0);
        for(int i=0; i<10; i++){
            Graph<Integer, DefaultEdge> g=new SimpleGraph<>(DefaultEdge.class);
            gnp.generateGraph(g, new IntegerVertexFactory(), null);

            List<Integer> result = new ArrayList<>();
            new DepthFirstIterator<>(g).forEachRemaining(result::add);

            List<Integer> expected=recursiveDFS(g);
            assertEquals(expected, result);
        }
    }

    /*  Alternative DFS implementation which uses recursion. */
    private <V,E> List<V> recursiveDFS(Graph<V,E> g){
        List<V> result=new ArrayList<>();
        Set<V> visited=new HashSet<>();
        for(V v : g.vertexSet()) {
            if(!visited.contains(v))
                recursiveDFSHelper(g, v, visited, result);
        }
        return result;
    }

    private <V,E> List<V> recursiveDFS(Graph<V,E> g, V startVertex){
        List<V> result=new ArrayList<>();
        Set<V> visited=new HashSet<>();
        recursiveDFSHelper(g, startVertex, visited, result);
        return result;
    }
    private <V,E> void recursiveDFSHelper(Graph<V,E> g, V v, Set<V> visited, List<V> result){
        visited.add(v);
        result.add(v);

        List<V> neighbors= Graphs.neighborListOf(g, v);
        Collections.reverse(neighbors); //Returned DFS sequence is order dependent.
        for(V u : neighbors)
            if(!visited.contains(u))
                recursiveDFSHelper(g, u, visited, result);
    }
}

//// End DepthFirstIteratorTest.java
