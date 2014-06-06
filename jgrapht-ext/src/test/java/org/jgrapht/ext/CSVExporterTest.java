/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
/* ------------------------------
 * CSVExporterTest.java
 * ------------------------------
 * (C) Copyright 2014, by Ivan Gavrilovic.
 *
 * Original Author:  Ivan Gavrilovic
 *
 *
 */
package org.jgrapht.ext;

import junit.framework.TestCase;
import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.imp.CSVImporter;
import org.jgrapht.imp.StringVertexParser;
import org.jgrapht.util.VertexPair;

import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;

/**
 * Testing the CSV export.
 * @author Ivan Gavrilović
 */
public class CSVExporterTest extends TestCase {
    public void testExportCSV() {
        DirectedGraph<String , DefaultEdge> directed = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        UndirectedGraph<String, DefaultEdge> undirected = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

        directed.addVertex("\"A"); undirected.addVertex("A");
        directed.addVertex("B\n"); undirected.addVertex("B");
        directed.addVertex("C"); undirected.addVertex("C");
        directed.addVertex("D"); undirected.addVertex("D");

        directed.addEdge("\"A", "B\n"); undirected.addEdge("A", "B");
        directed.addEdge("\"A", "D"); undirected.addEdge("A", "D");
        directed.addEdge("B\n", "D"); undirected.addEdge("B", "D");

        String dir =    "\"\"\"A\",\"B\n\"\n" +
                        "\"\"\"A\",D\n" +
                        "\"B\n\",D\n" +
                        "C\n" +
                        "D";
        String undir =  "A,B\n" +
                        "A,D\n" +
                        "B,A\n" +
                        "B,D\n" +
                        "C\n" +
                        "D,A\n" +
                        "D,B";

        StringWriter wDir = new StringWriter();
        StringWriter wUnDir = new StringWriter();
        CSVExporter<String, DefaultEdge> csv = new CSVExporter<String, DefaultEdge>();

        try {
            csv.export(directed, wDir);
            csv.export(undirected, wUnDir);
            assertEquals(dir, wDir.toString());
            assertEquals(undir, wUnDir.toString());
        }
        catch (Exception e){
            e.printStackTrace();
            fail();
        }
    }

    public void testImport(){
        String paths[] = new String[]{
                "/Users/ivan/Projects/ontology/activity-subsumer/src/test/resources/word_net_nouns_hyponym",
                "/Users/ivan/Projects/ontology/activity-subsumer/src/test/resources/word_net_nouns_synset",
                "/Users/ivan/Projects/ontology/activity-subsumer/src/test/resources/word_net_verbs_hyponym",
                "/Users/ivan/Projects/ontology/activity-subsumer/src/test/resources/word_net_verbs_synset"
        };

        for(String p:paths){
            try {
                CSVImporter<String> importer = new CSVImporter<String>(" ", new StringVertexParser(), new FileInputStream(p));
                long start = System.currentTimeMillis();

                Set<String> vertices = importer.getVertices();
                List<VertexPair<String>> edges = importer.getEdges();

                DirectedAcyclicGraph<String, DefaultEdge> graph = new DirectedAcyclicGraph<String, DefaultEdge>(DefaultEdge.class);
                for (String v : vertices) graph.addVertex(v);
                for (VertexPair<String> ed : edges) graph.addEdge(ed.getFirst(), ed.getSecond());

                System.out.println("Size of graph, vetices and edges, " + graph.vertexSet().size() + " " + graph.edgeSet().size() +
                        ", took " + (System.currentTimeMillis() - start) + "[ms]");
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        assertEquals(1, 1);
     }

    public void testExportCollapsedCSV(){
        DirectedGraph<String , DefaultEdge> directed = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        UndirectedGraph<String, DefaultEdge> undirected = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

        directed.addVertex("A"); undirected.addVertex("A");
        directed.addVertex("B"); undirected.addVertex("B");
        directed.addVertex("C"); undirected.addVertex("C");
        directed.addVertex("D"); undirected.addVertex("D");

        directed.addEdge("A", "B"); undirected.addEdge("A", "B");
        directed.addEdge("A", "D"); undirected.addEdge("A", "D");
        directed.addEdge("B", "D"); undirected.addEdge("B", "D");

        String dir =    "A,B,D\n" +
                        "B,D\n" +
                        "C\n" +
                        "D";

        String undir =  "A,B,D\n" +
                        "B,A,D\n" +
                        "C\n" +
                        "D,A,B";

        StringWriter wDir = new StringWriter();
        StringWriter wUnDir = new StringWriter();
        CSVExporter<String, DefaultEdge> csv = new CSVExporter<String, DefaultEdge>();

        // source and destination, both undirected and directed
        try {
            // node and list of its neighbours, both types
            csv.exportCollapsed(directed, wDir);
            csv.exportCollapsed(undirected, wUnDir);
            assertEquals(dir, wDir.toString());
            assertEquals(undir, wUnDir.toString());
        }
        catch (Exception e){
            e.printStackTrace();
            fail();
        }
    }
}
