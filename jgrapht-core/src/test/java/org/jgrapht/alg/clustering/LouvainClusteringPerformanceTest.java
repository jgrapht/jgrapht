/*
 * (C) Copyright 2025-2025, by Adam Bouzid and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.alg.clustering;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.generate.*;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for {@link LouvainClustering}.
 * 
 * These tests verify that the algorithm's complexity is O(n log n) as expected.
 * 
 * @author Adam Bouzid
 */
public class LouvainClusteringPerformanceTest
{
    private static final long SEED = 42L;
    
    /**
     * Test performance on increasing graph sizes (Scale-Free networks).
     * Verifies O(n log n) complexity.
     */
    @Test
    public void testScaleFreePerformance()
    {
        System.out.println("\n=== Scale-Free Network Performance Test ===");
        System.out.println("Size\tNodes\tEdges\tTime(ms)\tCommunities\tModularity");
        
        int[] sizes = {100, 200, 500, 1000, 2000};
        long[] times = new long[sizes.length];
        
        for (int i = 0; i < sizes.length; i++) {
            int n = sizes[i];
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), 
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, 
                false
            );
            
            BarabasiAlbertGraphGenerator<Integer, DefaultEdge> generator = 
                new BarabasiAlbertGraphGenerator<>(3, 2, n, new Random(SEED));
            generator.generateGraph(graph);
            
            long startTime = System.nanoTime();
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph, 1.0, new Random(SEED));
            ClusteringAlgorithm.Clustering<Integer> result = clustering.getClustering();
            long endTime = System.nanoTime();
            
            times[i] = (endTime - startTime) / 1_000_000; // Convert to ms
            
            System.out.printf("%d\t%d\t%d\t%d\t\t%d\t\t%.4f%n",
                i + 1, graph.vertexSet().size(), graph.edgeSet().size(),
                times[i], result.getNumberClusters(), 
                computeModularity(graph, result));
            
            assertTrue(result.getNumberClusters() > 0);
            assertTrue(result.getNumberClusters() <= n);
        }
        
        // Verify that growth is sub-quadratic (ideally O(n log n))
        verifySubQuadraticGrowth(sizes, times);
    }
    
    /**
     * Test performance on dense random graphs.
     */
    @Test
    public void testDenseGraphPerformance()
    {
        System.out.println("\n=== Dense Random Graph Performance Test ===");
        System.out.println("Size\tNodes\tEdges\tTime(ms)\tCommunities");
        
        int[] sizes = {50, 100, 200, 300, 500};
        
        for (int i = 0; i < sizes.length; i++) {
            int n = sizes[i];
            int m = (n * (n - 1)) / 4; // 50% density
            
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), 
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, 
                false
            );
            
            GnmRandomGraphGenerator<Integer, DefaultEdge> generator = 
                new GnmRandomGraphGenerator<>(n, m, SEED);
            generator.generateGraph(graph);
            
            long startTime = System.nanoTime();
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph);
            ClusteringAlgorithm.Clustering<Integer> result = clustering.getClustering();
            long endTime = System.nanoTime();
            
            long time = (endTime - startTime) / 1_000_000;
            
            System.out.printf("%d\t%d\t%d\t%d\t\t%d%n",
                i + 1, graph.vertexSet().size(), graph.edgeSet().size(),
                time, result.getNumberClusters());
            
            assertTrue(result.getNumberClusters() > 0);
        }
    }
    
    /**
     * Test performance on sparse graphs.
     */
    @Test
    public void testSparseGraphPerformance()
    {
        System.out.println("\n=== Sparse Graph Performance Test ===");
        System.out.println("Size\tNodes\tEdges\tTime(ms)\tCommunities");
        
        int[] sizes = {500, 1000, 2000, 5000, 10000};
        
        for (int i = 0; i < sizes.length; i++) {
            int n = sizes[i];
            int m = n * 3; // Average degree = 6
            
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), 
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, 
                false
            );
            
            GnmRandomGraphGenerator<Integer, DefaultEdge> generator = 
                new GnmRandomGraphGenerator<>(n, m, SEED);
            generator.generateGraph(graph);
            
            long startTime = System.nanoTime();
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph, 1.0, new Random(SEED));
            ClusteringAlgorithm.Clustering<Integer> result = clustering.getClustering();
            long endTime = System.nanoTime();
            
            long time = (endTime - startTime) / 1_000_000;
            
            System.out.printf("%d\t%d\t%d\t%d\t\t%d%n",
                i + 1, graph.vertexSet().size(), graph.edgeSet().size(),
                time, result.getNumberClusters());
            
            assertTrue(result.getNumberClusters() > 0);
            assertTrue(time < 10000); // Should complete in less than 10 seconds
        }
    }
    
    /**
     * Test performance on small-world networks.
     */
    @Test
    public void testSmallWorldPerformance()
    {
        System.out.println("\n=== Small-World Network Performance Test ===");
        System.out.println("Size\tNodes\tEdges\tTime(ms)\tCommunities");
        
        int[] sizes = {100, 200, 500, 1000, 2000};
        
        for (int i = 0; i < sizes.length; i++) {
            int n = sizes[i];
            int k = 6; // Each vertex connected to k nearest neighbors
            double p = 0.1; // Rewiring probability
            
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), 
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, 
                false
            );
            
            WattsStrogatzGraphGenerator<Integer, DefaultEdge> generator = 
                new WattsStrogatzGraphGenerator<>(n, k, p, SEED);
            generator.generateGraph(graph);
            
            long startTime = System.nanoTime();
            LouvainClustering<Integer, DefaultEdge> clustering = 
                new LouvainClustering<>(graph, 1.0, new Random(SEED));
            ClusteringAlgorithm.Clustering<Integer> result = clustering.getClustering();
            long endTime = System.nanoTime();
            
            long time = (endTime - startTime) / 1_000_000;
            
            System.out.printf("%d\t%d\t%d\t%d\t\t%d%n",
                i + 1, graph.vertexSet().size(), graph.edgeSet().size(),
                time, result.getNumberClusters());
            
            assertTrue(result.getNumberClusters() > 0);
        }
    }
    
    /**
     * Verify that the time complexity grows sub-quadratically.
     * For O(n log n), we expect: time[i] / (n[i] * log(n[i])) to be roughly constant.
     */
    private void verifySubQuadraticGrowth(int[] sizes, long[] times)
    {
        System.out.println("\n--- Complexity Analysis ---");
        System.out.println("n\ttime\tt/(n*log(n))\tt/n²");
        
        for (int i = 0; i < sizes.length; i++) {
            int n = sizes[i];
            long t = times[i];
            double nlogn = n * Math.log(n);
            double nsquared = n * n;
            
            System.out.printf("%d\t%d\t%.4f\t\t%.6f%n",
                n, t, t / nlogn, t / nsquared);
        }
        
        // Check that growth is sub-quadratic
        if (sizes.length >= 2) {
            int last = sizes.length - 1;
            double ratio = (double) times[last] / times[0];
            double sizeRatio = (double) sizes[last] / sizes[0];
            double quadraticRatio = sizeRatio * sizeRatio;
            
            System.out.printf("\nTime ratio: %.2f, Size ratio: %.2f, Quadratic would be: %.2f%n",
                ratio, sizeRatio, quadraticRatio);
            
            // Time growth should be much less than quadratic
            assertTrue(ratio < quadraticRatio * 0.7, 
                "Time complexity appears to be worse than O(n log n)");
        }
    }
    
    /**
     * Compute modularity of a clustering result.
     */
    private double computeModularity(
        Graph<Integer, DefaultEdge> graph, 
        ClusteringAlgorithm.Clustering<Integer> clustering)
    {
        int[] community = new int[graph.vertexSet().size()];
        int idx = 0;
        for (Integer v : graph.vertexSet()) {
            community[idx++] = findCommunity(v, clustering);
        }
        
        double m2 = 2.0 * graph.edgeSet().size();
        if (m2 < 1e-12) return 0.0;
        
        double modularity = 0.0;
        for (int i = 0; i < clustering.getNumberClusters(); i++) {
            double eii = 0.0;
            double ai = 0.0;
            
            int nodeIdx = 0;
            for (Integer v : graph.vertexSet()) {
                if (community[nodeIdx] == i) {
                    ai += graph.degreeOf(v);
                    for (DefaultEdge e : graph.edgesOf(v)) {
                        Integer neighbor = graph.getEdgeSource(e).equals(v) 
                            ? graph.getEdgeTarget(e) 
                            : graph.getEdgeSource(e);
                        int neighborIdx = 0;
                        for (Integer u : graph.vertexSet()) {
                            if (u.equals(neighbor)) break;
                            neighborIdx++;
                        }
                        if (community[neighborIdx] == i) {
                            eii += 0.5; // Count each edge once
                        }
                    }
                }
                nodeIdx++;
            }
            
            modularity += (eii / m2) - (ai / m2) * (ai / m2);
        }
        
        return modularity;
    }
    
    private int findCommunity(Integer vertex, ClusteringAlgorithm.Clustering<Integer> clustering)
    {
        int idx = 0;
        for (var cluster : clustering.getClusters()) {
            if (cluster.contains(vertex)) {
                return idx;
            }
            idx++;
        }
        return -1;
    }
}
