package org.jgrapht.alg;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * TODO: add tests, for now there are only helper methods to be used both here and in DirectedHamiltonianCycleTest
 */
public class CyclicTransitiveReductionTest {
  private static final Random RANDOM = new Random();

  private static <V> Graph<V, DefaultEdge> createEmptyGraph() {
    return GraphTypeBuilder
      .<V, DefaultEdge>directed()
      .allowingMultipleEdges(false)
      .allowingSelfLoops(false)
      .edgeClass(DefaultEdge.class)
      .buildGraph();
  }

  /**
   * Creates a directed graph suitable for testing cyclic transitive reduction and Hamiltonian cycle (HC).
   * <p></p>
   * More precisely, a graph with <i>sccCount</i> strongly connected components (SCCs) and <i>sccSize</i> vertices per
   * SCC will be generated. Within each SCC there will of course be a HC plus edges in random directions between all
   * other remaining pairs of vertices, i.e. the graph will be dense. Furthermore, between vertex #v of SCC #s and
   * vertex #v of SCC #(s+1) there will be a forward edge, i.e. there is a number of redundant edges between
   * corresponding vertices or neighbouring SCCs. Hence, both within SCCs and between them there are redundant edges to
   * be detected and removed by a cyclic transitive reduction algorithm. The condensation graph will be a linear string
   * of SCCs.
   *
   * @param graph graph to be populated with SCCs (ideally empty before calling this method)
   * @param sccCount number of SCCs to be generated; if equals 1, generate a single HC
   * @param sccSize number of vertices per SCC to be generated
   * @param <E> edge type
   */
  public static <E> void populateGraphWithSCCs(final Graph<String, E> graph, final int sccCount, final int sccSize) {
    // Generate vertices + HCs for all SCCs
    for (int currentSCC = 0; currentSCC < sccCount; currentSCC++) {
      // Generate vertices for current SCC
      for (int currentVertex = 0; currentVertex < sccSize; currentVertex++)
        graph.addVertex(getSCCVertex(currentSCC, currentVertex));
      // Generate HC for current SCC
      for (int currentVertex = 0; currentVertex < sccSize; currentVertex++)
        graph.addEdge(
          getSCCVertex(currentSCC, currentVertex),
          getSCCVertex(currentSCC, (currentVertex + 1) % sccSize)
        );
    }

    // Generate redundant edges within SCCs
    for (int currentSCC = 0; currentSCC < sccCount; currentSCC++) {
      for (int source = 0; source < sccSize; source++) {
        String sourceVertex = getSCCVertex(currentSCC, source);
        // Generate redundant edges within SCCs in random direction
        for (int target = source + 1; target < sccSize; target++) {
          String targetVertex = getSCCVertex(currentSCC, target);
          if (sourceVertex.equals(targetVertex))
            continue;
          if (graph.containsEdge(sourceVertex, targetVertex) || graph.containsEdge(targetVertex, sourceVertex))
            continue;
          if (RANDOM.nextBoolean())
            graph.addEdge(sourceVertex, targetVertex);
          else
            graph.addEdge(targetVertex, sourceVertex);
        }
        // Add redundant forward edges to vertices with identical numbers inside next SCC
        if (currentSCC + 1 < sccCount)
          graph.addEdge(sourceVertex, getSCCVertex(currentSCC + 1, source));
      }
    }
  }

  public static String getSCCVertex(final int sccNumber, final int vertexNumber) {
    return "S" + sccNumber + "/V" + vertexNumber;
  }

}
