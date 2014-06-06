package org.jgrapht.imp;

import org.jgrapht.util.VertexPair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author Ivan Gavrilović
 */
public class CSVImporter<V> {
    private String delimiter;
    private InputStream stream;
    private VertexParser<V> vertexParser;

    private int numVertices;
    private int numEdges;

    private Set<V> vertices = null;
    private List<VertexPair<V>> edges = null;

    public CSVImporter(String delimiter, VertexParser<V> parser, InputStream stream) {
        this.delimiter = delimiter;
        this.vertexParser = parser;
        this.stream = stream;
        numVertices = 16;
        numEdges = 16;
    }

    public CSVImporter(String delimiter, VertexParser<V> parser, InputStream stream, int numVertices, int numEdges) {
        this.delimiter = delimiter;
        this.vertexParser = parser;
        this.stream = stream;
        this.numVertices = numVertices;
        this.numEdges = numEdges;
    }

    /**
     * Parse the input file
     */
    public void processCSV() {
        try {
            vertices = new HashSet<V>(numVertices);
            edges = new ArrayList<VertexPair<V>>(numEdges);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));

            String line = bufferedReader.readLine();
            while (line != null) {
                if (!line.startsWith("#")) {
                    StringTokenizer tokenizer = new StringTokenizer(line, delimiter);
                    // FIRST IS ALWAYS VERTEX
                    V start = vertexParser.parseVertex(tokenizer.nextToken());
                    vertices.add(start);

                    while (tokenizer.hasMoreTokens()) {
                        V end = vertexParser.parseVertex(tokenizer.nextToken());
                        vertices.add(end);
                        edges.add(new VertexPair<V>(start, end));
                    }

                }
                line = bufferedReader.readLine();
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public Set<V> getVertices() {
        if (vertices == null) processCSV();
        return vertices;
    }

    public List<VertexPair<V>> getEdges() {
        if (edges == null) processCSV();
        return edges;
    }
}
