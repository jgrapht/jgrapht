package org.jgrapht.demo;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import org.jgrapht.Graph;
import org.jgrapht.alg.clustering.LeidenClustering;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.ext.JGraphXAdapter;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;

public class LeidenJGraphXDemo extends JFrame {

    public LeidenJGraphXDemo() {
        super("Leiden Clustering + JGraphX Demo");

        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        // Simple test graph with communities
        graph.addVertex("A"); graph.addVertex("B"); graph.addVertex("C");
        graph.addVertex("D"); graph.addVertex("E"); graph.addVertex("F");

        graph.addEdge("A", "B");
        graph.addEdge("A", "C");
        graph.addEdge("B", "C");  // Cluster 1

        graph.addEdge("D", "E");
        graph.addEdge("E", "F");
        graph.addEdge("D", "F");  // Cluster 2

        // Run Leiden clustering
        LeidenClustering<String, DefaultEdge> leiden =
                new LeidenClustering<>(graph);
        var result = leiden.getClustering();
        List<Set<String>> communities = result.getClusters();

        System.out.println("Detected communities:");
        for (int i = 0; i < communities.size(); i++) {
            System.out.println("Community " + i + ": " + communities.get(i));
        }

        // JGraphX visualization
        JGraphXAdapter<String, DefaultEdge> adapter =
                new JGraphXAdapter<>(graph);

        mxGraphComponent graphComponent = new mxGraphComponent(adapter);
        graphComponent.setConnectable(false);

        getContentPane().add(graphComponent, BorderLayout.CENTER);
        setSize(800, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Apply layout
        mxCircleLayout layout = new mxCircleLayout(adapter);
        layout.execute(adapter.getDefaultParent());

        // Color communities
        Color[] palette = {
                Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE,
                Color.MAGENTA, Color.CYAN
        };

        int index = 0;
        for (Set<String> cluster : communities) {
            Color color = palette[index % palette.length];
            for (String v : cluster) {
                Object cell = adapter.getVertexToCellMap().get(v);
                adapter.setCellStyles("fillColor",
                        "#" + Integer.toHexString(color.getRGB()).substring(2),
                        new Object[]{ cell });
            }
            index++;
        }

        graphComponent.refresh(); // ✅ Fixes blank screen
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LeidenJGraphXDemo().setVisible(true);
        });
    }
}
