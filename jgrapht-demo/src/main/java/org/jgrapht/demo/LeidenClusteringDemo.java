/*
 * (C) Copyright 2025-2025, by Adam Bouzid and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.demo;

import com.mxgraph.layout.*;
import com.mxgraph.model.*;
import com.mxgraph.swing.*;
import com.mxgraph.view.*;
import com.mxgraph.util.mxConstants;
import org.jgrapht.*;
import org.jgrapht.alg.clustering.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.ext.*;
import org.jgrapht.graph.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Same demo as LouvainClusteringDemo but using the Leiden algorithm.
 */
public class LeidenClusteringDemo
    extends JApplet
{
    private static final long serialVersionUID = 1L;
    private static final Dimension DEFAULT_SIZE = new Dimension(800, 600);

    private JGraphXAdapter<String, DefaultEdge> jgxAdapter;

    public static void main(String[] args)
    {
        LeidenClusteringDemo applet = new LeidenClusteringDemo();
        applet.init();

        JFrame frame = new JFrame();
        frame.getContentPane().add(applet);
        frame.setTitle("JGraphT Leiden Clustering Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void init()
    {
        // Create a JGraphT graph
        Graph<String, DefaultEdge> graph = createSampleGraph();

        // ✅ Use Leiden clustering instead of Louvain
        LeidenClustering<String, DefaultEdge> clustering =
            new LeidenClustering<>(graph, 1.0, new Random(42),
                LeidenClustering.Quality.MODULARITY);

        ClusteringAlgorithm.Clustering<String> result = clustering.getClustering();

        // Print clustering results
        System.out.println("Leiden Clustering Results:");
        System.out.println("Number of communities: " + result.getNumberClusters());
        int i = 0;
        for (Set<String> c : result.getClusters()) {
            System.out.println("Community " + (i++) + ": " + c);
        }

        // JGraphX visualization
        ListenableGraph<String, DefaultEdge> listenableGraph =
            new DefaultListenableGraph<>(graph);
        jgxAdapter = new JGraphXAdapter<>(listenableGraph);

        setPreferredSize(DEFAULT_SIZE);
        mxGraphComponent component = new mxGraphComponent(jgxAdapter);
        component.setConnectable(false);
        component.getGraph().setAllowDanglingEdges(false);
        getContentPane().add(component);
        resize(DEFAULT_SIZE);

        // Apply layout
        mxFastOrganicLayout layout = new mxFastOrganicLayout(jgxAdapter);
        layout.execute(jgxAdapter.getDefaultParent());

        // Color vertices by community
        colorVerticesByCommunity(result);

        // ✅ Refresh to make drawing visible
        component.refresh();
    }

    private Graph<String, DefaultEdge> createSampleGraph()
    {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        // Community 1: nodes 0-4
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

        // Community 2: nodes 5-9
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

        // Community 3: nodes 10-14
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
        graph.addEdge("v4", "v5");
        graph.addEdge("v9", "v10");

        return graph;
    }

    private void colorVerticesByCommunity(ClusteringAlgorithm.Clustering<String> clustering)
    {
        String[] colors = {
            "#FF6B6B", "#4ECDC4", "#45B7D1",
            "#FFA07A", "#98D8C8", "#F7DC6F",
            "#BB8FCE", "#85C1E2",
        };

        mxGraph graph = jgxAdapter;

        graph.getModel().beginUpdate();
        try {
            List<Set<String>> clusters = new ArrayList<>(clustering.getClusters());

            for (int i = 0; i < clusters.size(); i++) {
                String color = colors[i % colors.length];
                for (String v : clusters.get(i)) {
                    mxCell cell = (mxCell) jgxAdapter.getVertexToCellMap().get(v);
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
