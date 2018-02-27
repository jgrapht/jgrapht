package org.jgrapht.own;

import org.jgrapht.graph.*;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;


import java.util.*;

/*Conversion of a given graph to its line graph
 * This algorithm returns back a directed line graph for the input directed graph
 @author Arica Chakraborty*/


public class LineDirectedGraphs {
    /*Method that checks whether the given graph is Empty or not
     *  @param graph is a graph
     *  @param <V> the graph vertex type
     * @param <E> the graph edge type
     *  @return boolean value depending on the given graph is empty or not*/

    public static <V, E> boolean isEmpty(Graph<V, E> graph)
    {
        Objects.requireNonNull(graph, "Graph can't be null");
        return graph.edgeSet().isEmpty();
    }
    SimpleDirectedGraph<Integer,DefaultEdge> resultGraph;
    /*Method that creates a line graph out of the given input graph
     * @param graph is a graph
     * @return directed line graph*/

    public Graph<Integer,DefaultEdge> createListGraph(SimpleDirectedGraph<Integer,DefaultEdge> graph){
        if(!isEmpty(graph)) {
            resultGraph = new SimpleDirectedGraph<>(DefaultEdge.class);
            Set<DefaultEdge> set = graph.edgeSet();
            Iterator iterator = set.iterator();

            while (iterator.hasNext()) {
                DefaultEdge defaultEdge = (DefaultEdge) iterator.next();
                Integer iOne = graph.getEdgeSource(defaultEdge);
                Integer iTwo = graph.getEdgeTarget(defaultEdge);
                Integer iThree = iOne * 10 + iTwo;
                resultGraph.addVertex(iThree);
            }

            Set<Integer> vertexSet = resultGraph.vertexSet();

            Integer arr[] = new Integer[vertexSet.size()];
            int n = 0;
            for (Integer s : vertexSet) {
                arr[n] = s;
                n++;
            }

            int m = arr.length;
            for (int i = 0; i < (m - 1); i++) {
                int rem = arr[i] % 10;
                int div;
                for (int l = i + 1; l < m; l++) {
                    div = arr[l] / 10;
                    //System.out.println(div);
                    if (rem == div) {
                        resultGraph.addEdge(arr[i], arr[l]);
                    }
                }
            }
        }
        return resultGraph;

    }
    /*
     * Checks if two  input vertices are whether connected or not
     * @param i1 is an Integer object
     * @param i2 is an Integer Object
     * @return boolean value*/

    public boolean isConnected(Integer i1,Integer i2){
        boolean answer = false;
        int rem,div;
        Set<Integer> setVertex = resultGraph.vertexSet();
        if(setVertex.contains(i1) && setVertex.contains(i2)) {


            rem = i1 % 10;
            div = i2 / 10;
            if (rem == div)
                answer = true;
        }
        return answer;
    }

}
