package org.jgrapht.demo;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.JFrame;

import org.jgrapht.Graph;
import org.jgrapht.alg.clustering.LeidenClustering;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

/**
 * Demo: Visualizing Leiden community detection using JGraphX.
 */
public class LeidenJGraphXDemo {

    public static void main(String[] args) {
        Graph<String, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        // Create example nodes
        String[] nodes = {"A","B","C","D","E","F","G","H"};
        for (String n : nodes) g.addVertex(n);

        // Create edges forming 3 communities
        add(g, "A","B");
        add(g, "A","C");
        add(g, "B","C");

        add(g, "D","E");
        add(g, "D","F");
        add(g, "E","F");

        add(g, "G","H");

        // Run Leiden (CPM scoring = recommended)
        LeidenClustering<String, DefaultWeightedEdge> leiden =
            new LeidenClustering<>(
                g,
                0.5,
                new Random(),
                LeidenClustering.Quality.CPM
            );

        var clustering = leiden.getClustering();
        var assignmentMap = clustering.getCommunities();

        new LeidenJGraphXDemo().displayGraph(g, assignmentMap);
    }

    private static void add(Graph<String, DefaultWeightedEdge> g, String u, String v) {
        DefaultWeightedEdge e = g.addEdge(u, v);
        g.setEdgeWeight(e, 1.0);
    }

    private void displayGraph(Graph<String, DefaultWeightedEdge> g,
                              Map<String, Integer> assignment) {

        JFrame frame = new JFrame("Leiden Community Detection Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);

        mxGraph mxg = new mxGraph();
        Object parent = mxg.getDefaultParent();
        Map<String, Object> cells = new HashMap<>();

        mxg.getModel().beginUpdate();
        try {
            for (String v : g.vertexSet()) {
                cells.put(v, mxg.insertVertex(parent, null, v, 0, 0, 70, 40));
            }

            for (DefaultWeightedEdge e : g.edgeSet()) {
                String src = g.getEdgeSource(e);
                String tgt = g.getEdgeTarget(e);
                mxg.insertEdge(parent, null, "", cells.get(src), cells.get(tgt));
            }

            // Random community colors
            Random r = new Random();
            Map<Integer, String> cmap = new HashMap<>();

            for (String v : g.vertexSet()) {
                int com = assignment.get(v);

                cmap.computeIfAbsent(
                    com,
                    k -> String.format("#%06X", r.nextInt(0xFFFFFF))
                );

                mxg.setCellStyle("fillColor=" + cmap.get(com),
                    new Object[] { cells.get(v) });
            }
        }
        finally {
            mxg.getModel().endUpdate();
        }

        // Apply layout
        new mxOrganicLayout(mxg).execute(parent);

        frame.add(new mxGraphComponent(mxg));
        frame.setVisible(true);
    }
}
