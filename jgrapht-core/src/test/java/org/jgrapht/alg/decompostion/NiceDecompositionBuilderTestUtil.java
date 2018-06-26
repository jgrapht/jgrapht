package org.jgrapht.alg.decompostion;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.Map.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;

public final class NiceDecompositionBuilderTestUtil
{
    //static class
    private NiceDecompositionBuilderTestUtil() {}
    
    /**
     * tests whether the tree decomposition (decomposition, map) with root is nice
     * 
     * @param decomposition the tree decomposition to check
     * @param map the map of the tree decomposition
     * @param root the root of the tree decomposition
     */
    public static <V,E,W> void testNiceDecomposition(Graph<V,E> decomposition, Map<V,Set<W>> map, V root){
        
        Queue<V> queue = new LinkedList<V>();
        
        //test and add root
        assertTrue(root+" is no valid root"
                + "\n in decomposition "+decomposition
                + "\n and map"+map, map.get(root).isEmpty());
       queue.add(root);
        
        while(!queue.isEmpty())
        {
            V current = queue.poll();
            List<V> successor = Graphs.successorListOf(decomposition, current);
            if(successor.size() == 0 && map.get(current).isEmpty()) continue; //leaf node
            if(successor.size() == 1) //forget or introduce
            {
                V next = successor.get(0);
                queue.add(next);
                Set<W> union = new HashSet<W>(map.get(current));
                union.addAll(map.get(next));
                if(union.size() == map.get(next).size() || union.size() == map.get(current).size())
                {
                    if(map.get(current).size() == map.get(next).size()-1) continue; //introduce
                    else if(map.get(current).size()-1 == map.get(next).size()) continue; //forget
                }
            }
            if(successor.size() == 2) //join
            {
                V first = successor.get(0);
                V second = successor.get(1);
                queue.add(first);
                queue.add(second);
                Set<W> union = new HashSet<W>(map.get(current));
                union.addAll(map.get(first));
                union.addAll(map.get(second));
                if(union.size() == map.get(current).size() 
                && union.size() == map.get(first).size() 
                && union.size() == map.get(second).size()) 
                    continue; //join node!
            }
            assertFalse("Vertex Set "+current+" is not a valid node for a nice decomposition"
                + "\nin decomposition "+decomposition
                + "\nwith map "+map, true); //no valid node!
        }
        assertTrue(true);
    }
    
    /**
     * Test whether (decomposition, map) a tree decomposition of oldGraph is
     * 
     * @param oldGraph the graph of the tree decomposition
     * @param decomposition the tree decomposition
     * @param map the map from vertices to the tree decomposition to the sets of the tree decomposition
     */
    public static <V,E,W,F> void testDecomposition(Graph<W,F> oldGraph, Graph<V,E> decomposition, Map<V,Set<W>> map){
        Set<F> edgeSet = oldGraph.edgeSet();
        Set<V> vertexSet = decomposition.vertexSet();
        //Every edge is represented
        for(F e : edgeSet)
        {
            boolean hasVertex = false;
            for(V v : vertexSet)
            {
                if(map.get(v).contains(oldGraph.getEdgeSource(e)) && map.get(v).contains(oldGraph.getEdgeTarget(e))) {
                    hasVertex = true;
                    break; 
                }
            }
            assertTrue("Edge "+e+" is not found"
                + "\nin graph "+decomposition
                + "\nwith map "+map, hasVertex);
        }
        //every vertex has non-empty connected set of vertex sets
        Set<W> oldVertexSet = oldGraph.vertexSet();
        for(W w : oldVertexSet)
        {
            Set<V> keySet = new HashSet<V>();
            for(Entry<V,Set<W>> entry : map.entrySet())
            {
                if(entry.getValue().contains(w))
                    keySet.add(entry.getKey());
            }
            //not empty
            assertFalse("Vertex "+w+" is not represented\n in decomposition "+decomposition+"\n and map "+map, keySet.isEmpty());
            //connected
            Graph<V,E> subgraph = new AsSubgraph<V,E>(decomposition,keySet);
            assertTrue("Vertex "+w+" is not connected\n in decomposition "+decomposition+"\n and map "+map,GraphTests.isConnected(subgraph));
        }
    }
    
}
