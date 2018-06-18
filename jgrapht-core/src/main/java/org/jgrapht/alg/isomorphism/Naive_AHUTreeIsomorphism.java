package org.jgrapht.alg.isomorphism;

import org.jgrapht.Graph;
import org.jgrapht.GraphMapping;
import org.jgrapht.Graphs;
import org.jgrapht.alg.util.Pair;

import java.util.*;

public class Naive_AHUTreeIsomorphism<V, E> {
    private final Graph<V, E> graph1;
    private final Graph<V, E> graph2;

    private V root1;
    private V root2;

    public Naive_AHUTreeIsomorphism(Graph<V, E> graph1, Graph<V, E> graph2){

        this.graph1 = graph1;
        this.graph2 = graph2;
    }

    public Naive_AHUTreeIsomorphism(Graph<V, E> graph1, V root1, Graph<V, E> graph2, V root2){

        this.graph1 = graph1;
        this.graph2 = graph2;

        this.root1 = root1;
        this.root2 = root2;
    }

    private String clean(List<Pair<Integer, String>> offspring){
        StringBuilder sb = new StringBuilder();
        sb.append(offspring.size());

        for (Pair<Integer, String> pair: offspring){
            sb.append(",").append(pair.getSecond());
        }

        return sb.toString();
    }

    private String assignCanonicalNames(Graph<V, E> graph, V root, V parent){
        List<String> offspring = new ArrayList<>();

        for (E edge: graph.edgesOf(root)){
            V vertex = Graphs.getOppositeVertex(graph, edge, root);

            if (!vertex.equals(parent)){
                offspring.add(assignCanonicalNames(graph, vertex, root));
            }
        }

        offspring.sort(String::compareTo);

        StringBuilder sb = new StringBuilder();

        sb.append(1);
        offspring.forEach(sb::append);
        sb.append(0);

        return sb.toString();
    }

    boolean isomorphismExists(){
//        System.out.println(assignCanonicalNames(graph1, root1, null));

        return assignCanonicalNames(graph1, root1, null).equals(assignCanonicalNames(graph2, root2, null));
    }
}
