/*
 * (C) Copyright 2016-2020, by Dimitrios Michail and Contributors.
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
package org.jgrapht.nio.csv;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import org.jgrapht.alg.util.Triple;
import org.jgrapht.nio.*;

import java.io.*;
import java.util.*;

/**
 * Imports a graph from a CSV Format or any other Delimiter-separated value format.
 * 
 * <p>
 * The importer supports various different formats which can be adjusted using the
 * {@link #setFormat(CSVFormat) setFormat} method. The supported formats are the same CSV formats
 * used by <a href="https://gephi.org/users/supported-graph-formats/csv-format">Gephi</a>. For some
 * of the formats, the behavior of the importer can be adjusted using the
 * {@link #setParameter(org.jgrapht.nio.csv.CSVFormat.Parameter, boolean) setParameter} method. See
 * {@link CSVFormat} for a description of the formats.
 * </p>
 * 
 * <p>
 * The importer respects <a href="http://www.ietf.org/rfc/rfc4180.txt">rfc4180</a>. The caller can
 * also adjust the separator to something like semicolon or pipe instead of comma. In such a case,
 * all fields are unescaped using the new separator. See
 * <a href="https://en.wikipedia.org/wiki/Delimiter-separated_values">Delimiter-separated values</a>
 * for more information.
 * </p>
 * 
 * <p>
 * This importer does not distinguish between {@link CSVFormat#EDGE_LIST} and
 * {@link CSVFormat#ADJACENCY_LIST}. In both cases it assumes the format is
 * {@link CSVFormat#ADJACENCY_LIST}.
 * </p>
 * 
 * @see CSVFormat
 * 
 * @author Dimitrios Michail
 */
public class CSVEventDrivenImporter
    extends
    BaseEventDrivenImporter<String, Triple<String, String, Double>>
    implements
    EventDrivenImporter<String, Triple<String, String, Double>>
{
    private static final char DEFAULT_DELIMITER = ',';

    private CSVFormat format;
    private char delimiter;
    private final Set<CSVFormat.Parameter> parameters;

    /**
     * Constructs a new importer using the {@link CSVFormat#ADJACENCY_LIST} format as default.
     */
    public CSVEventDrivenImporter()
    {
        this(CSVFormat.ADJACENCY_LIST, DEFAULT_DELIMITER);
    }

    /**
     * Constructs a new importer.
     * 
     * @param format format to use out of the supported ones
     */
    public CSVEventDrivenImporter(CSVFormat format)
    {
        this(format, DEFAULT_DELIMITER);
    }

    /**
     * Constructs a new importer.
     * 
     * @param format format to use out of the supported ones
     * @param delimiter delimiter to use (comma, semicolon, pipe, etc.)
     */
    public CSVEventDrivenImporter(CSVFormat format, char delimiter)
    {
        this.format = format;
        if (!DSVUtils.isValidDelimiter(delimiter)) {
            throw new IllegalArgumentException("Character cannot be used as a delimiter");
        }
        this.delimiter = delimiter;
        this.parameters = new HashSet<>();
    }

    /**
     * Get the format that the importer is using.
     * 
     * @return the input format
     */
    public CSVFormat getFormat()
    {
        return format;
    }

    /**
     * Set the format of the importer
     * 
     * @param format the format to use
     */
    public void setFormat(CSVFormat format)
    {
        this.format = format;
    }

    /**
     * Get the delimiter (comma, semicolon, pipe, etc).
     * 
     * @return the delimiter
     */
    public char getDelimiter()
    {
        return delimiter;
    }

    /**
     * Set the delimiter (comma, semicolon, pipe, etc).
     * 
     * @param delimiter the delimiter to use
     */
    public void setDelimiter(char delimiter)
    {
        if (!DSVUtils.isValidDelimiter(delimiter)) {
            throw new IllegalArgumentException("Character cannot be used as a delimiter");
        }
        this.delimiter = delimiter;
    }

    /**
     * Return if a particular parameter of the exporter is enabled
     * 
     * @param p the parameter
     * @return {@code true} if the parameter is set, {@code false} otherwise
     */
    public boolean isParameter(CSVFormat.Parameter p)
    {
        return parameters.contains(p);
    }

    /**
     * Set the value of a parameter of the exporter
     * 
     * @param p the parameter
     * @param value the value to set
     */
    public void setParameter(CSVFormat.Parameter p, boolean value)
    {
        if (value) {
            parameters.add(p);
        } else {
            parameters.remove(p);
        }
    }

    @Override
    public void importInput(Reader input)
        throws ImportException
    {
        notifyImportEvent(ImportEvent.START);
        switch (format) {
        case EDGE_LIST:
        case ADJACENCY_LIST:
            read(input, new AdjacencyListCSVListener());
            break;
        case MATRIX:
            read(input, new MatrixCSVListener());
            break;
        }
        notifyImportEvent(ImportEvent.END);
    }

    private void read(Reader input, CSVBaseListener listener)
        throws ImportException
    {
        try {
            ThrowingErrorListener errorListener = new ThrowingErrorListener();

            // create lexer
            CSVLexer lexer = new CSVLexer(CharStreams.fromReader(input));
            lexer.setSep(delimiter);
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);

            // create parser
            CSVParser parser = new CSVParser(new CommonTokenStream(lexer));
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            // Specify our entry point
            CSVParser.FileContext graphContext = parser.file();

            // Walk it and attach our listener
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(listener, graphContext);
        } catch (IOException e) {
            throw new ImportException("Failed to import CSV graph: " + e.getMessage(), e);
        } catch (ParseCancellationException pe) {
            throw new ImportException("Failed to import CSV graph: " + pe.getMessage(), pe);
        } catch (IllegalArgumentException iae) {
            throw new ImportException("Failed to import CSV graph: " + iae.getMessage(), iae);
        }
    }

    private class ThrowingErrorListener
        extends
        BaseErrorListener
    {
        @Override
        public void syntaxError(
            Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
            String msg, RecognitionException e)
            throws ParseCancellationException
        {
            throw new ParseCancellationException(
                "line " + line + ":" + charPositionInLine + " " + msg);
        }
    }

    // listener for the edge list format
    private class AdjacencyListCSVListener
        extends
        RowCSVListener
    {
        private boolean assumeEdgeWeights;

        public AdjacencyListCSVListener()
        {
            super();
            this.assumeEdgeWeights = parameters.contains(CSVFormat.Parameter.EDGE_WEIGHTS);
        }

        @Override
        protected void handleRow()
        {
            // first is source
            String source = row.get(0);
            if (source.isEmpty()) {
                throw new ParseCancellationException("Source vertex cannot be empty");
            }
            if (!vertices.contains(source)) {
                vertices.add(source);
                notifyVertex(source);
            }
            row.remove(0);

            // remaining are targets (if weighted pairs of target-weight)
            int step = assumeEdgeWeights ? 2 : 1;

            for (int i = 0; i < row.size(); i += step) {
                String target = row.get(i);

                if (target.isEmpty()) {
                    throw new ParseCancellationException("Target vertex cannot be empty");
                }
                if (!vertices.contains(target)) {
                    vertices.add(target);
                    notifyVertex(target);
                }

                Double weight = null;
                if (assumeEdgeWeights) {
                    try {
                        weight = Double.parseDouble(row.get(i + 1));
                    } catch (NumberFormatException nfe) {
                        throw new ParseCancellationException("Failed to parse edge weight");
                    }
                }

                notifyEdge(Triple.of(source, target, weight));
            }
        }

    }

    // listener for the edge list format
    private class MatrixCSVListener
        extends
        RowCSVListener
    {
        private boolean assumeNodeIds;
        private boolean assumeEdgeWeights;
        private boolean assumeZeroWhenNoEdge;
        private int verticesCount;
        private int currentVertex;
        private String currentVertexName;
        private Map<Integer, String> columnIndex;

        public MatrixCSVListener()
        {
            super();
            this.assumeNodeIds = parameters.contains(CSVFormat.Parameter.MATRIX_FORMAT_NODEID);
            this.assumeEdgeWeights = parameters.contains(CSVFormat.Parameter.EDGE_WEIGHTS);
            ;
            this.assumeZeroWhenNoEdge =
                parameters.contains(CSVFormat.Parameter.MATRIX_FORMAT_ZERO_WHEN_NO_EDGE);
            this.verticesCount = 0;
            this.currentVertex = 1;
            this.currentVertexName = null;
            this.columnIndex = new HashMap<>();
        }

        @Override
        protected void handleRow()
        {
            if (assumeNodeIds) {
                if (!header) {
                    currentVertexName = row.get(0);
                }
                row.remove(0);
            } else {
                currentVertexName = String.valueOf(currentVertex);
            }

            if (header) {
                if (assumeNodeIds) {
                    createVerticesFromNodeIds();
                } else {
                    createVertices();
                    createEdges();
                    currentVertex++;
                }
            } else {
                createEdges();
                currentVertex++;
            }
        }

        private void createVerticesFromNodeIds()
        {
            // header line contains nodes
            verticesCount = row.size();
            if (verticesCount < 1) {
                throw new ParseCancellationException("Failed to parse header with vertices");
            }
            int v = 1;
            for (String vertexName : row) {
                if (vertexName.trim().isEmpty()) {
                    throw new ParseCancellationException(
                        "Failed to parse header with vertices (empty name)");
                }

                if (!vertices.contains(vertexName)) {
                    vertices.add(vertexName);
                    notifyVertex(vertexName);
                }
                columnIndex.put(v, vertexName);
                v++;
            }
        }

        private void createVertices()
        {
            // header line contains nodes
            verticesCount = row.size();
            if (verticesCount < 1) {
                throw new ParseCancellationException("Failed to parse header with vertices");
            }
            int v = 1;
            for (v = 1; v <= verticesCount; v++) {
                String vertexName = String.valueOf(v);
                if (!vertices.contains(vertexName)) {
                    vertices.add(vertexName);
                    notifyVertex(vertexName);
                }
                columnIndex.put(v, vertexName);
            }
        }

        private void createEdges()
        {
            if (row.size() != verticesCount) {
                throw new ParseCancellationException(
                    "Row contains fewer than " + verticesCount + " entries");
            }

            int target = 1;
            for (String entry : row) {
                // try to parse an integer
                try {
                    Integer entryAsInteger = Integer.parseInt(entry);
                    if (entryAsInteger == 0) {
                        if (!assumeZeroWhenNoEdge && assumeEdgeWeights) {
                            notifyEdge(Triple.of(currentVertexName, columnIndex.get(target), 0d));
                        }
                    } else {
                        if (assumeEdgeWeights) {
                            notifyEdge(
                                Triple
                                    .of(
                                        currentVertexName, columnIndex.get(target),
                                        Double.valueOf(entryAsInteger)));
                        } else {
                            notifyEdge(Triple.of(currentVertexName, columnIndex.get(target), null));
                        }
                    }
                    target++;
                    continue;
                } catch (NumberFormatException nfe) {
                    // nothing
                }

                // try to parse a double
                try {
                    Double entryAsDouble = Double.parseDouble(entry);
                    if (assumeEdgeWeights) {
                        notifyEdge(
                            Triple.of(currentVertexName, columnIndex.get(target), entryAsDouble));
                    } else {
                        throw new ParseCancellationException(
                            "Double entry found when expecting no weights");
                    }
                } catch (NumberFormatException nfe) {
                    // nothing
                }

                target++;
            }
        }
    }

    // base listener
    private abstract class RowCSVListener
        extends
        CSVBaseListener
    {
        protected List<String> row;
        protected Set<String> vertices;
        protected boolean header;

        public RowCSVListener()
        {
            this.row = new ArrayList<>();
            this.vertices = new HashSet<>();
            this.header = false;
        }

        @Override
        public void enterHeader(CSVParser.HeaderContext ctx)
        {
            header = true;
        }

        @Override
        public void exitHeader(CSVParser.HeaderContext ctx)
        {
            header = false;
        }

        @Override
        public void enterRecord(CSVParser.RecordContext ctx)
        {
            row.clear();
        }

        @Override
        public void exitRecord(CSVParser.RecordContext ctx)
        {
            if (row.isEmpty()) {
                throw new ParseCancellationException("Empty CSV record");
            }

            handleRow();
        }

        @Override
        public void exitTextField(CSVParser.TextFieldContext ctx)
        {
            row.add(ctx.TEXT().getText());
        }

        @Override
        public void exitStringField(CSVParser.StringFieldContext ctx)
        {
            row.add(DSVUtils.unescapeDSV(ctx.STRING().getText(), delimiter));
        }

        @Override
        public void exitEmptyField(CSVParser.EmptyFieldContext ctx)
        {
            row.add("");
        }

        protected abstract void handleRow();

    }

}
