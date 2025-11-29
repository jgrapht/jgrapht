/*
 * (C) Copyright 2025, by Your Name and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.demo;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;

import org.jgrapht.Graph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.alg.clustering.LeidenClustering;
import org.jgrapht.alg.clustering.LeidenClustering.Quality;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.SimpleGraph;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.util.*;

/**
 * A demo that shows how to use the Leiden clustering algorithm to detect communities
 * in a graph and visualize them with different colors using JGraphX.
 */
public class LeidenClusteringDemo
    extends JApplet
{
    private static final long serialVersionUID = 1L;
    private static final Dimension DEFAULT_SIZE = new Dimension(800, 600);

    private JGraphXAdapter<String, DefaultEdge> jgxAdapter;

    /**
     * Entry point to run this applet as a standalone application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> {
            LeidenClusteringDemo applet = new LeidenClusteringDemo();
            applet.init();

            JFrame frame = new JFrame();
            frame.getContentPane().add(applet);
            frame.setTitle("JGraphT Leiden Clustering Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
    }

    @Override
    public void init()
    {
        // Create a JGraphT graph
        Graph<String, DefaultEdge> graph = createSampleGraph();

        // Choose quality function and resolution
        double resolution = 1.0;
        Quality quality = Quality.MODULARITY; // change to Quality.CPM to test CPM behavior

        // Apply Leiden clustering algorithm
        LeidenClustering<String, DefaultEdge> leiden =
            new LeidenClustering<>(graph, resolution, new Random(42), quality);
        ClusteringAlgorithm.Clustering<String> result = leiden.getClustering();

        // Print clustering results to console
        System.out.println("Leiden Clustering Results (" + quality + ", γ=" + resolution + "):");
        System.out.println("Number of communities: " + result.getNumberClusters());
        int clusterIndex = 0;
        for (Set<String> cluster : result.getClusters()) {
            System.out.println("Community " + (clusterIndex++) + ": " + cluster);
        }

        // Wrap in a listenable graph for JGraphX
        ListenableGraph<String, DefaultEdge> listenableGraph =
            new DefaultListenableGraph<>(graph);
        jgxAdapter = createAdapter(listenableGraph);

        setPreferredSize(DEFAULT_SIZE);
        mxGraphComponent component = new mxGraphComponent(jgxAdapter);
        component.setConnectable(false);
        component.getGraph().setAllowDanglingEdges(false);
        getContentPane().add(component);
        resize(DEFAULT_SIZE);

        // Layout
        mxFastOrganicLayout layout = new mxFastOrganicLayout(jgxAdapter);
        layout.setForceConstant(80);
        layout.execute(jgxAdapter.getDefaultParent());

        // Color vertices by community
        colorVerticesByCommunity(result);
    }

    /**
     * Creates a sample graph with multiple communities. This graph is designed to have clear
     * community structure that the Leiden algorithm can detect.
     *
     * @return a graph with community structure
     */
    Graph<String, DefaultEdge> createSampleGraph()
    {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        // Community 1: nodes v0–v4 (densely connected)
        for (int i = 0; i < 5; i++) {
            graph.addVertex("v" + i);
        }
        graph.addEdge("v0", "v1");
        graph.addEdge("v0", "v2");
        graph.addEdge("v0", "v3");
        graph.addEdge("v1", "v2");
        graph.addEdge("v1", "v3");
        graph.addEdge("v2", "v3");
        graph.addEdge("v3", "v4");
        graph.addEdge("v2", "v4");

        // Community 2: nodes v5–v9 (densely connected)
        for (int i = 5; i < 10; i++) {
            graph.addVertex("v" + i);
        }
        graph.addEdge("v5", "v6");
        graph.addEdge("v5", "v7");
        graph.addEdge("v5", "v8");
        graph.addEdge("v6", "v7");
        graph.addEdge("v6", "v8");
        graph.addEdge("v7", "v8");
        graph.addEdge("v8", "v9");
        graph.addEdge("v7", "v9");

        // Community 3: nodes v10–v14 (densely connected)
        for (int i = 10; i < 15; i++) {
            graph.addVertex("v" + i);
        }
        graph.addEdge("v10", "v11");
        graph.addEdge("v10", "v12");
        graph.addEdge("v10", "v13");
        graph.addEdge("v11", "v12");
        graph.addEdge("v11", "v13");
        graph.addEdge("v12", "v13");
        graph.addEdge("v13", "v14");
        graph.addEdge("v12", "v14");

        // Inter-community bridges
        graph.addEdge("v4", "v5");   // between community 1 and 2
        graph.addEdge("v9", "v10");  // between community 2 and 3

        return graph;
    }

    JGraphXAdapter<String, DefaultEdge> createAdapter(ListenableGraph<String, DefaultEdge> listenableGraph)
    {
        jgxAdapter = new JGraphXAdapter<>(listenableGraph);
        return jgxAdapter;
    }

    /**
     * Colors vertices according to their community membership.
     *
     * @param clustering the clustering result from Leiden algorithm
     */
    void colorVerticesByCommunity(ClusteringAlgorithm.Clustering<String> clustering)
    {
        // Define colors for different communities
        String[] colors = {
            "#FF6B6B", // Red
            "#4ECDC4", // Teal
            "#45B7D1", // Blue
            "#FFA07A", // Light Salmon
            "#98D8C8", // Mint
            "#F7DC6F", // Yellow
            "#BB8FCE", // Purple
            "#85C1E2"  // Sky Blue
        };

        mxGraph graph = jgxAdapter;

        graph.getModel().beginUpdate();
        try {
            List<Set<String>> clusters = new ArrayList<>(clustering.getClusters());

            for (int i = 0; i < clusters.size(); i++) {
                String color = colors[i % colors.length];
                Set<String> cluster = clusters.get(i);

                for (String vertex : cluster) {
                    mxCell cell = (mxCell) jgxAdapter.getVertexToCellMap().get(vertex);
                    if (cell != null) {
                        graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, color, new Object[]{cell});
                        graph.setCellStyles(mxConstants.STYLE_STROKECOLOR, "#000000", new Object[]{cell});
                        graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, "#000000", new Object[]{cell});
                    }
                }
            }
        } finally {
            graph.getModel().endUpdate();
        }
    }
}
