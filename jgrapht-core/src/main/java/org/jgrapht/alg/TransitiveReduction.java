package org.jgrapht.alg;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;

/**
 * 
 * @author Christophe Thiebaud
 * 
 * cf. https://en.wikipedia.org/wiki/Transitive_reduction
 * 
 * port from python example by Michael Clerx, posted as an answer to http://stackoverflow.com/questions/1690953/transitive-reduction-algorithm-pseudocode
 * 
 */
public class TransitiveReduction<V, E> {

    final private List<V> vertices;
    final private int [][] pathMatrix;
    
    private final DirectedGraph<V, E> graph;
    
    public TransitiveReduction(DirectedGraph<V, E> graph) {
        super();
        this.graph = graph;
        this.vertices = new ArrayList<V>(graph.vertexSet());
        int n = vertices.size();
        int[][] original = new int[n][n];

        // initialize matrix with zeros
        // --> 0 is the default value for int arrays
        
        // initialize matrix with edges
        Set<E> edges = graph.edgeSet();
        for (E edge : edges) {
            V v1 = graph.getEdgeSource(edge);
            V v2 = graph.getEdgeTarget(edge);

            int v_1 = vertices.indexOf(v1);
            int v_2 = vertices.indexOf(v2);

            original[v_1][v_2] = 1;
        }
        
        this.pathMatrix = original;
        transformToPathMatrix(this.pathMatrix);
    }
    
    // (package visible for unit testing)
    static void transformToPathMatrix(int[][] matrix) {
        // compute path matrix 
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) { 
                if (i == j) {
                    continue;
                }
                if (matrix[j][i] > 0 ){
                    for (int k = 0; k < matrix.length; k++) {
                        if (matrix[j][k] == 0) {
                            matrix[j][k] = matrix[i][k];
                        }
                    }
                }
            }
        }
    }

    // (package visible for unit testing)
    static void transitiveReduction(int[][] pathMatrix) {
        // transitively reduce
        for (int j = 0; j < pathMatrix.length; j++) { 
            for (int i = 0; i < pathMatrix.length; i++) {
                if (pathMatrix[i][j] > 0){
                    for (int k = 0; k < pathMatrix.length; k++) {
                        if (pathMatrix[j][k] > 0) {
                            pathMatrix[i][k] = 0;
                        }
                    }
                }
            }
        }
    }

    public void reduce() {
        
        int n = pathMatrix.length;
        int[][] transitivelyReducedMatrix = new int[n][n];
        System.arraycopy(pathMatrix, 0, transitivelyReducedMatrix, 0, pathMatrix.length);
        transitiveReduction(transitivelyReducedMatrix);
        
        for (int i = 0; i <n; i++) {
            for (int j = 0; j < n; j++) { 
                if (transitivelyReducedMatrix[i][j] == 0) {
                    // System.out.println("removing "+vertices.get(i)+" -> "+vertices.get(j));
                    graph.removeEdge(graph.getEdge(vertices.get(i), vertices.get(j)));
                }
            }
        }
    }

}