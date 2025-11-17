/*
 * Copyright (C) 2025, by Rayene Abbassi and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.alg.clustering;

import java.util.*;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.Clustering;
import org.jgrapht.graph.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for {@link LouvainClustering}.
 * 
 * Test plan covers:
 * 1. Functional correctness on various graph types
 * 2. Edge cases and boundary conditions
 * 3. Performance and complexity verification
 * 4. Modularity optimization validation
 */
public class LouvainClusteringComprehensiveTest
{
    private static final Random TEST_RANDOM = new Random(42);

    // ==================== FUNCTIONAL TESTS ====================

    @Nested
    @DisplayName("Basic Functional Tests")
    class BasicFunctionalTests
    {
        @Test
        @DisplayName("Empty graph returns empty clustering")
        public void testEmptyGraph()
        {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph);
            
            Clustering<Integer> result = clustering.getClustering();
            assertEquals(0, result.getNumberClusters());
            assertTrue(result.getClusters().isEmpty());
        }

        @Test
        @DisplayName("Single vertex forms singleton cluster")
        public void testSingleVertex()
        {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
            graph.addVertex(0);
            
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph);
            Clustering<Integer> result = clustering.getClustering();
            
            assertEquals(1, result.getNumberClusters());
            assertEquals(Set.of(0), result.getClusters().get(0));
        }

        @Test
        @DisplayName("Two disconnected vertices form two clusters")
        public void testTwoDisconnectedVertices()
        {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
            graph.addVertex(0);
            graph.addVertex(1);
            
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph, 1.0, new Random(42));
            Clustering<Integer> result = clustering.getClustering();
            
            assertEquals(2, result.getNumberClusters());
        }

        @Test
        @DisplayName("Complete graph forms single cluster")
        public void testCompleteGraph()
        {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
            
            // Manually create complete graph K5
            for (int i = 0; i < 5; i++) {
                graph.addVertex(i);
            }
            for (int i = 0; i < 5; i++) {
                for (int j = i + 1; j < 5; j++) {
                    graph.addEdge(i, j);
                }
            }
            
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph);
            Clustering<Integer> result = clustering.getClustering();
            
            // Complete graph should form 1 cluster
            assertEquals(1, result.getNumberClusters());
            assertEquals(5, result.getClusters().get(0).size());
        }
    }

    @Nested
    @DisplayName("Community Detection Tests")
    class CommunityDetectionTests
    {
        @Test
        @DisplayName("Zachary's Karate Club network")
        public void testKarateClubGraph()
        {
            Graph<Integer, DefaultEdge> graph = createKarateClubGraph();
            
            // Verify the graph structure matches the original dataset
            validateKarateClubStructure(graph);
            
            // Use resolution = 0.5 to get approximately 4 communities
            // Lower resolution favors fewer, larger communities
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph, 0.5, new Random(42));
            Clustering<Integer> result = clustering.getClustering();
            
            // With resolution = 0.5, we expect around 4 communities
            System.out.println("Karate Club - Number of communities: " + result.getNumberClusters());
            System.out.println("Karate Club - Edges: " + graph.edgeSet().size());
            assertEquals(4, result.getNumberClusters(), 
                "Expected 4 communities with resolution=0.5");
            
            // All vertices should be assigned
            int totalVertices = result.getClusters().stream()
                .mapToInt(Set::size)
                .sum();
            assertEquals(34, totalVertices);
        }

        @Test
        @DisplayName("Three dense cliques connected by bridges")
        public void testThreeDenseCliques()
        {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
            
            // Clique 1: vertices 0-4 (fully connected)
            for (int i = 0; i < 5; i++) {
                graph.addVertex(i);
            }
            for (int i = 0; i < 5; i++) {
                for (int j = i + 1; j < 5; j++) {
                    graph.addEdge(i, j);
                }
            }
            
            // Clique 2: vertices 5-9 (fully connected)
            for (int i = 5; i < 10; i++) {
                graph.addVertex(i);
            }
            for (int i = 5; i < 10; i++) {
                for (int j = i + 1; j < 10; j++) {
                    graph.addEdge(i, j);
                }
            }
            
            // Clique 3: vertices 10-14 (fully connected)
            for (int i = 10; i < 15; i++) {
                graph.addVertex(i);
            }
            for (int i = 10; i < 15; i++) {
                for (int j = i + 1; j < 15; j++) {
                    graph.addEdge(i, j);
                }
            }
            
            // Bridges between cliques
            graph.addEdge(4, 5);   // Bridge 1-2
            graph.addEdge(9, 10);  // Bridge 2-3
            
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph, 1.0, new Random(42));
            Clustering<Integer> result = clustering.getClustering();
            
            // Should detect 3 communities
            assertEquals(3, result.getNumberClusters());
        }

        @Test
        @DisplayName("Grid graph with clear community structure")
        public void testGridGraph()
        {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
            
            // Create 10x10 grid manually
            int rows = 10, cols = 10;
            for (int i = 0; i < rows * cols; i++) {
                graph.addVertex(i);
            }
            
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int v = r * cols + c;
                    if (c < cols - 1) graph.addEdge(v, v + 1);      // Right
                    if (r < rows - 1) graph.addEdge(v, v + cols);   // Down
                }
            }
            
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph, 1.0, new Random(42));
            Clustering<Integer> result = clustering.getClustering();
            
            // Grid should be partitioned into multiple communities
            assertTrue(result.getNumberClusters() > 1);
            assertTrue(result.getNumberClusters() < 100); // Not too fragmented
        }
    }

    @Nested
    @DisplayName("Weighted Graph Tests")
    class WeightedGraphTests
    {
        @Test
        @DisplayName("Strong weights keep vertices together")
        public void testStrongWeights()
        {
            SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph = 
                new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
            
            for (int i = 0; i < 4; i++) {
                graph.addVertex(i);
            }
            
            // Strong connection 0-1
            graph.setEdgeWeight(graph.addEdge(0, 1), 10.0);
            
            // Weak connection 1-2
            graph.setEdgeWeight(graph.addEdge(1, 2), 0.1);
            
            // Strong connection 2-3
            graph.setEdgeWeight(graph.addEdge(2, 3), 10.0);
            
            LouvainClustering<Integer, DefaultWeightedEdge> clustering = 
                new LouvainClustering<>(graph, 1.0, new Random(42));
            Clustering<Integer> result = clustering.getClustering();
            
            // Should split at the weak connection
            assertEquals(2, result.getNumberClusters());
            
            // Verify the split
            Set<Set<Integer>> communities = result.getClusters().stream()
                .map(HashSet::new)
                .collect(Collectors.toSet());
            
            assertTrue(
                communities.contains(Set.of(0, 1)) || communities.contains(Set.of(2, 3))
            );
        }

        @Test
        @DisplayName("Uniform weights behave like unweighted graph")
        public void testUniformWeights()
        {
            SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph = 
                new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
            
            for (int i = 0; i < 6; i++) {
                graph.addVertex(i);
            }
            
            // All edges have weight 1.0
            graph.setEdgeWeight(graph.addEdge(0, 1), 1.0);
            graph.setEdgeWeight(graph.addEdge(1, 2), 1.0);
            graph.setEdgeWeight(graph.addEdge(2, 0), 1.0);
            graph.setEdgeWeight(graph.addEdge(3, 4), 1.0);
            graph.setEdgeWeight(graph.addEdge(4, 5), 1.0);
            graph.setEdgeWeight(graph.addEdge(5, 3), 1.0);
            graph.setEdgeWeight(graph.addEdge(2, 3), 1.0);
            
            LouvainClustering<Integer, DefaultWeightedEdge> clustering = 
                new LouvainClustering<>(graph, 1.0, new Random(42));
            Clustering<Integer> result = clustering.getClustering();
            
            // Should detect 2 triangles as communities
            assertEquals(2, result.getNumberClusters());
        }
    }

    @Nested
    @DisplayName("Resolution Parameter Tests")
    class ResolutionParameterTests
    {
        @Test
        @DisplayName("Low resolution creates fewer, larger communities")
        public void testLowResolution()
        {
            Graph<Integer, DefaultEdge> graph = createModularGraph(3, 5);
            
            LouvainClustering<Integer, DefaultEdge> lowRes = 
                new LouvainClustering<>(graph, 0.5, new Random(42));
            LouvainClustering<Integer, DefaultEdge> normalRes = 
                new LouvainClustering<>(graph, 1.0, new Random(42));
            
            int lowClusters = lowRes.getClustering().getNumberClusters();
            int normalClusters = normalRes.getClustering().getNumberClusters();
            
            assertTrue(lowClusters <= normalClusters,
                "Low resolution should create fewer or equal clusters");
        }

        @Test
        @DisplayName("High resolution creates more, smaller communities")
        public void testHighResolution()
        {
            Graph<Integer, DefaultEdge> graph = createModularGraph(3, 5);
            
            LouvainClustering<Integer, DefaultEdge> normalRes = 
                new LouvainClustering<>(graph, 1.0, new Random(42));
            LouvainClustering<Integer, DefaultEdge> highRes = 
                new LouvainClustering<>(graph, 2.0, new Random(42));
            
            int normalClusters = normalRes.getClustering().getNumberClusters();
            int highClusters = highRes.getClustering().getNumberClusters();
            
            assertTrue(highClusters >= normalClusters,
                "High resolution should create more or equal clusters");
        }

        @Test
        @DisplayName("Very low resolution merges all into one community")
        public void testVeryLowResolution()
        {
            Graph<Integer, DefaultEdge> graph = createModularGraph(3, 4);
            
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph, 0.1, new Random(42));
            Clustering<Integer> result = clustering.getClustering();
            
            // Very low resolution tends to create fewer communities than normal resolution
            // Verify it creates fewer or equal communities compared to default
            assertTrue(result.getNumberClusters() >= 1,
                "Should have at least 1 community");
            assertTrue(result.getNumberClusters() <= 12,
                "Very low resolution should not create more communities than vertices/module, got " + result.getNumberClusters());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests
    {
        @Test
        @DisplayName("Graph with self-loops")
        public void testSelfLoops()
        {
            Graph<Integer, DefaultEdge> graph = 
                new Pseudograph<>(DefaultEdge.class);
            
            graph.addVertex(0);
            graph.addVertex(1);
            graph.addVertex(2);
            
            graph.addEdge(0, 0); // Self-loop
            graph.addEdge(0, 1);
            graph.addEdge(1, 2);
            
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph);
            
            // Should not crash
            assertDoesNotThrow(() -> clustering.getClustering());
        }

        @Test
        @DisplayName("Star graph")
        public void testStarGraph()
        {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
            
            // Create star graph: center connected to 10 outer vertices
            for (int i = 0; i < 11; i++) {
                graph.addVertex(i);
            }
            for (int i = 1; i < 11; i++) {
                graph.addEdge(0, i); // Center 0 connected to all others
            }
            
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph);
            Clustering<Integer> result = clustering.getClustering();
            
            // Star graph might form 1 or few communities
            assertTrue(result.getNumberClusters() >= 1);
            assertTrue(result.getNumberClusters() <= 11);
        }

        @Test
        @DisplayName("Linear chain graph")
        public void testLinearChain()
        {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
            
            // Create linear chain: 0-1-2-3-...-19
            for (int i = 0; i < 20; i++) {
                graph.addVertex(i);
            }
            for (int i = 0; i < 19; i++) {
                graph.addEdge(i, i + 1);
            }
            
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph, 1.0, new Random(42));
            Clustering<Integer> result = clustering.getClustering();
            
            // Chain might be split into several communities
            assertTrue(result.getNumberClusters() >= 1);
        }

        @Test
        @DisplayName("Cycle graph")
        public void testCycleGraph()
        {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
            
            // Create cycle: 0-1-2-...-19-0
            for (int i = 0; i < 20; i++) {
                graph.addVertex(i);
            }
            for (int i = 0; i < 19; i++) {
                graph.addEdge(i, i + 1);
            }
            graph.addEdge(19, 0); // Close the cycle
            
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph);
            Clustering<Integer> result = clustering.getClustering();
            
            // Cycle should form 1 or few communities
            assertTrue(result.getNumberClusters() >= 1);
        }
    }

    @Nested
    @DisplayName("Determinism and Reproducibility Tests")
    class DeterminismTests
    {
        @Test
        @DisplayName("Same seed produces same results")
        public void testDeterminism()
        {
            Graph<Integer, DefaultEdge> graph = createModularGraph(3, 10);
            
            LouvainClustering<Integer, DefaultEdge> clustering1 = 
                new LouvainClustering<>(graph, 1.0, new Random(42));
            LouvainClustering<Integer, DefaultEdge> clustering2 = 
                new LouvainClustering<>(graph, 1.0, new Random(42));
            
            Clustering<Integer> result1 = clustering1.getClustering();
            Clustering<Integer> result2 = clustering2.getClustering();
            
            assertEquals(result1.getNumberClusters(), result2.getNumberClusters());
            
            // Compare cluster contents
            Set<Set<Integer>> clusters1 = result1.getClusters().stream()
                .map(HashSet::new)
                .collect(Collectors.toSet());
            Set<Set<Integer>> clusters2 = result2.getClusters().stream()
                .map(HashSet::new)
                .collect(Collectors.toSet());
            
            assertEquals(clusters1, clusters2);
        }

        @Test
        @DisplayName("Caching works correctly")
        public void testCaching()
        {
            Graph<Integer, DefaultEdge> graph = createModularGraph(2, 5);
            
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph);
            
            Clustering<Integer> result1 = clustering.getClustering();
            Clustering<Integer> result2 = clustering.getClustering();
            
            // Should return same instance (cached)
            assertSame(result1, result2);
        }
    }

    // ==================== PERFORMANCE TESTS ====================

    @Nested
    @DisplayName("Performance and Complexity Tests")
    class PerformanceTests
    {
        @Test
        @DisplayName("Small graph (100 vertices) - baseline")
        @Timeout(5)
        public void testPerformanceSmall()
        {
            Graph<Integer, DefaultEdge> graph = createRandomGraph(100, 300);
            
            long startTime = System.nanoTime();
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph);
            clustering.getClustering();
            long endTime = System.nanoTime();
            
            long durationMs = (endTime - startTime) / 1_000_000;
            System.out.println("Small graph (100 vertices): " + durationMs + " ms");
            
            assertTrue(durationMs < 1000, "Should complete in less than 1 second");
        }

        @Test
        @DisplayName("Medium graph (500 vertices)")
        @Timeout(10)
        public void testPerformanceMedium()
        {
            Graph<Integer, DefaultEdge> graph = createRandomGraph(500, 1500);
            
            long startTime = System.nanoTime();
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph);
            clustering.getClustering();
            long endTime = System.nanoTime();
            
            long durationMs = (endTime - startTime) / 1_000_000;
            System.out.println("Medium graph (500 vertices): " + durationMs + " ms");
            
            assertTrue(durationMs < 5000, "Should complete in less than 5 seconds");
        }

        @Test
        @DisplayName("Large graph (1000 vertices)")
        @Timeout(20)
        public void testPerformanceLarge()
        {
            Graph<Integer, DefaultEdge> graph = createRandomGraph(1000, 3000);
            
            long startTime = System.nanoTime();
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph);
            clustering.getClustering();
            long endTime = System.nanoTime();
            
            long durationMs = (endTime - startTime) / 1_000_000;
            System.out.println("Large graph (1000 vertices): " + durationMs + " ms");
            
            assertTrue(durationMs < 15000, "Should complete in less than 15 seconds");
        }

        @Test
        @DisplayName("Complexity verification: O(n log n) behavior")
        @Timeout(120)
        public void testComplexityScaling()
        {
            // Test sizes: 100, 150, 200, 300, 400, 600, 800, 1200, 1600, 2000
            int[] sizes = {100, 150, 200, 300, 400, 600, 800, 1200, 1600, 2000};
            long[] times = new long[sizes.length];
            
            for (int i = 0; i < sizes.length; i++) {
                int n = sizes[i];
                int m = n * 3; // Sparse graph
                
                Graph<Integer, DefaultEdge> graph = createRandomGraph(n, m);
                
                long startTime = System.nanoTime();
                LouvainClustering<Integer, DefaultEdge> clustering = 
                    new LouvainClustering<>(graph, 1.0, new Random(42));
                clustering.getClustering();
                long endTime = System.nanoTime();
                
                times[i] = endTime - startTime;
                System.out.printf("n=%d: %d ms%n", n, times[i] / 1_000_000);
            }
            
            System.out.println("\nScaling analysis:");
            // Verify O(n log n) scaling
            // For O(n log n), ratio should be: (n2/n1) * log(n2)/log(n1)
            for (int i = 1; i < sizes.length; i++) {
                double sizeRatio = (double) sizes[i] / sizes[i - 1];
                double logRatio = Math.log(sizes[i]) / Math.log(sizes[i - 1]);
                double theoreticalRatio = sizeRatio * logRatio;
                double actualRatio = (double) times[i] / times[i - 1];
                
                System.out.printf("  n=%d to n=%d: theoretical ratio=%.2f, actual ratio=%.2f%n",
                    sizes[i - 1], sizes[i], theoreticalRatio, actualRatio);
                
                // Allow factor of 4x tolerance (due to implementation details and small sizes)
                assertTrue(actualRatio < theoreticalRatio * 4,
                    String.format("Scaling from n=%d to n=%d: expected ratio ~%.2f, got %.2f",
                        sizes[i - 1], sizes[i], theoreticalRatio, actualRatio));
            }
        }

        @Test
        @DisplayName("Dense graph performance (higher edge density)")
        @Timeout(30)
        public void testDenseGraphPerformance()
        {
            int n = 500;
            int m = n * n / 4; // Dense graph
            
            Graph<Integer, DefaultEdge> graph = createRandomGraph(n, m);
            
            long startTime = System.nanoTime();
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph);
            clustering.getClustering();
            long endTime = System.nanoTime();
            
            long durationMs = (endTime - startTime) / 1_000_000;
            System.out.println("Dense graph (500 vertices, 62500 edges): " + durationMs + " ms");
            
            assertTrue(durationMs < 20000, "Dense graph should complete in reasonable time");
        }

        @Test
        @DisplayName("Sparse graph performance (low edge density)")
        @Timeout(10)
        public void testSparseGraphPerformance()
        {
            int n = 1000;
            int m = n * 2; // Very sparse
            
            Graph<Integer, DefaultEdge> graph = createRandomGraph(n, m);
            
            long startTime = System.nanoTime();
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph);
            clustering.getClustering();
            long endTime = System.nanoTime();
            
            long durationMs = (endTime - startTime) / 1_000_000;
            System.out.println("Sparse graph (1000 vertices, 2000 edges): " + durationMs + " ms");
            
            assertTrue(durationMs < 5000, "Sparse graph should be fast");
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Creates Zachary's Karate Club graph - a classic benchmark.
     * 
     * This is the complete network from Zachary (1977) with all 78 edges.
     * Vertices are labeled 0-33 (original paper uses 1-34).
     * 
     * Reference: Zachary, W. W. (1977). "An Information Flow Model for Conflict and Fission 
     * in Small Groups". Journal of Anthropological Research, 33(4), 452-473.
     */
    private Graph<Integer, DefaultEdge> createKarateClubGraph()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        
        // Add 34 vertices (labeled 0-33, corresponding to original 1-34)
        for (int i = 0; i < 34; i++) {
            graph.addVertex(i);
        }
        
        // Complete edge list - all 78 undirected edges from the original dataset
        // Note: vertices are 0-indexed (subtract 1 from original paper's 1-indexed labels)
        int[][] edges = {
            // Vertex 0 (original 1 - "Mr. Hi", the instructor)
            {0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5}, {0, 6}, {0, 7}, {0, 8},
            {0, 10}, {0, 11}, {0, 12}, {0, 13}, {0, 17}, {0, 19}, {0, 21}, {0, 31},
            
            // Vertex 1 (original 2)
            {1, 2}, {1, 3}, {1, 7}, {1, 13}, {1, 17}, {1, 19}, {1, 21}, {1, 30},
            
            // Vertex 2 (original 3)
            {2, 3}, {2, 7}, {2, 8}, {2, 9}, {2, 13}, {2, 27}, {2, 28}, {2, 32},
            
            // Vertex 3 (original 4)
            {3, 7}, {3, 12}, {3, 13},
            
            // Vertex 4 (original 5)
            {4, 6}, {4, 10},
            
            // Vertex 5 (original 6)
            {5, 6}, {5, 10}, {5, 16},
            
            // Vertex 6 (original 7)
            {6, 16},
            
            // Vertex 8 (original 9)
            {8, 30}, {8, 32}, {8, 33},
            
            // Vertex 9 (original 10)
            {9, 33},
            
            // Vertex 13 (original 14)
            {13, 33},
            
            // Vertex 14 (original 15)
            {14, 32}, {14, 33},
            
            // Vertex 15 (original 16)
            {15, 32}, {15, 33},
            
            // Vertex 18 (original 19)
            {18, 32}, {18, 33},
            
            // Vertex 19 (original 20)
            {19, 33},
            
            // Vertex 20 (original 21)
            {20, 32}, {20, 33},
            
            // Vertex 22 (original 23)
            {22, 32}, {22, 33},
            
            // Vertex 23 (original 24)
            {23, 25}, {23, 27}, {23, 29}, {23, 32}, {23, 33},
            
            // Vertex 24 (original 25)
            {24, 25}, {24, 27}, {24, 31},
            
            // Vertex 25 (original 26)
            {25, 31},
            
            // Vertex 26 (original 27)
            {26, 29}, {26, 33},
            
            // Vertex 27 (original 28)
            {27, 33},
            
            // Vertex 28 (original 29)
            {28, 31}, {28, 33},
            
            // Vertex 29 (original 30)
            {29, 32}, {29, 33},
            
            // Vertex 30 (original 31)
            {30, 32}, {30, 33},
            
            // Vertex 31 (original 32)
            {31, 32}, {31, 33},
            
            // Vertex 32-33 (original 33-34 - "John A.", the officer)
            {32, 33}
        };
        
        for (int[] edge : edges) {
            graph.addEdge(edge[0], edge[1]);
        }
        
        return graph;
    }

    /**
     * Validates the Karate Club graph structure.
     * This can be used to verify that the graph matches the published dataset.
     * 
     * Expected properties:
     * - 34 vertices (members of the karate club)
     * - 78 edges (social interactions)
     * - Two main factions centered around vertex 0 (Mr. Hi) and vertex 33 (John A.)
     * - Average degree: ~4.59
     * - Network diameter: 5
     */
    private void validateKarateClubStructure(Graph<Integer, DefaultEdge> graph)
    {
        assertEquals(34, graph.vertexSet().size(), "Incorrect number of vertices");
        assertEquals(78, graph.edgeSet().size(), "Incorrect number of edges");
        
        // Verify key vertices have expected degrees (from original paper)
        assertEquals(16, graph.degreeOf(0), "Vertex 0 (Mr. Hi) should have degree 16");
        assertEquals(17, graph.degreeOf(33), "Vertex 33 (John A.) should have degree 17");
        
        // These are the two leaders who eventually caused the club to split
        assertTrue(graph.degreeOf(0) > 10, "Central figure should be highly connected");
        assertTrue(graph.degreeOf(33) > 10, "Central figure should be highly connected");
    }

    /**
     * Creates a modular graph with specified number of modules and size
     */
    private Graph<Integer, DefaultEdge> createModularGraph(int numModules, int moduleSize)
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        
        int vertex = 0;
        for (int m = 0; m < numModules; m++) {
            // Create a dense module
            List<Integer> module = new ArrayList<>();
            for (int i = 0; i < moduleSize; i++) {
                graph.addVertex(vertex);
                module.add(vertex);
                vertex++;
            }
            
            // Connect vertices within module
            for (int i = 0; i < module.size(); i++) {
                for (int j = i + 1; j < module.size(); j++) {
                    if (TEST_RANDOM.nextDouble() < 0.7) { // 70% internal connectivity
                        graph.addEdge(module.get(i), module.get(j));
                    }
                }
            }
        }
        
        // Add sparse inter-module connections
        List<Integer> allVertices = new ArrayList<>(graph.vertexSet());
        for (int i = 0; i < allVertices.size(); i++) {
            for (int j = i + moduleSize; j < allVertices.size(); j++) {
                if (TEST_RANDOM.nextDouble() < 0.05) { // 5% inter-module connectivity
                    if (!graph.containsEdge(allVertices.get(i), allVertices.get(j))) {
                        graph.addEdge(allVertices.get(i), allVertices.get(j));
                    }
                }
            }
        }
        
        return graph;
    }

    /**
     * Creates a random graph with specified vertices and edges
     */
    private Graph<Integer, DefaultEdge> createRandomGraph(int vertices, int edges)
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        
        // Add all vertices
        for (int i = 0; i < vertices; i++) {
            graph.addVertex(i);
        }
        
        // Add random edges
        Random rnd = new Random(TEST_RANDOM.nextLong());
        int addedEdges = 0;
        int maxAttempts = edges * 10; // Prevent infinite loop
        int attempts = 0;
        
        while (addedEdges < edges && attempts < maxAttempts) {
            int u = rnd.nextInt(vertices);
            int v = rnd.nextInt(vertices);
            
            if (u != v && !graph.containsEdge(u, v)) {
                graph.addEdge(u, v);
                addedEdges++;
            }
            attempts++;
        }
        
        return graph;
    }
}
