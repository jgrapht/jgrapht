package org.jgrapht.generate;

import java.util.Map;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GraphGenerator;

/**
 * 
 * @author Philipp S. K&aumlsgen
 *
 * @param <V>
 * @param <E>
 */
public class ComplementGraphGenerator<V,E> implements GraphGenerator<V,E,V> {

	private final UndirectedGraph<V,E> g;
	
	public ComplementGraphGenerator(UndirectedGraph<V,E> graph){
        g = graph;
	}
	
	@Override
	public void generateGraph(Graph<V, E> target, VertexFactory<V> vertexFactory, Map<String, V> resultMap) {
		for (V v : g.vertexSet()) 
            target.addVertex(v);
		
		for (V v1 : g.vertexSet())
			for (V v2 : g.vertexSet())
				if (v1!=v2&&(!(g.containsEdge(v1, v2)||g.containsEdge(v2,v1))))
					target.addEdge(v1, v2);
	}

}
