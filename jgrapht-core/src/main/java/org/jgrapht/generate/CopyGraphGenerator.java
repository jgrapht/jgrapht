package org.jgrapht.generate;

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;

import java.util.Map;

/**
 * Copies all vertexes and edges from the source graph to target graphs.
 * I.e. this class does some sort of merge work.
 * Result is an independent changeable copy of the source graph.
 *
 * @author Daneel S. Yaitksov
 */
public class CopyGraphGenerator<V,E,T> implements GraphGenerator<V,E,T> {

    private Graph<V,E> source;

    /**
     *
     * @param source origin graph
     */
    public CopyGraphGenerator(Graph<V, E> source) {
        this.source = source;
    }

    /**
     * Copies all vertexes and edges from the source graph to the target.
     * @param target receives the copies of edges and vertices from the source
     * @param vVertexFactory  not used
     * @param resultMap not used
     */
    @Override
    public void generateGraph(Graph<V, E> target,
                              VertexFactory<V> vVertexFactory,
                              Map<String, T> resultMap) {
        for (V v : source.vertexSet()) {
            target.addVertex(v);
        }
        for (E e : source.edgeSet()) {
            target.addEdge(source.getEdgeSource(e), source.getEdgeTarget(e), e);
        }
    }
}
