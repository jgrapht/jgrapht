/*
 * (C) Copyright 2010-2021, by Michael Behrisch and Contributors.
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
package org.jgrapht.nio.dimacs;

import org.jgrapht.alg.util.*;
import org.jgrapht.nio.*;

import java.io.*;
import java.util.*;

/**
 * A generic importer using consumers for DIMACS format.
 *
 * <p>
 * See {@link DIMACSFormat} for a description of all the supported DIMACS formats.
 *
 * <p>
 * In summary, one of the most common DIMACS formats was used in the
 * <a href="http://mat.gsia.cmu.edu/COLOR/general/ccformat.ps">2nd DIMACS challenge</a> and follows
 * the following structure:
 * 
 * <pre>
 * {@code
 * DIMACS G {
 *    c <comments> ignored during parsing of the graph
 *    p edge <number of nodes> <number of edges>
 *    e <edge source 1> <edge target 1>
 *    e <edge source 2> <edge target 2>
 *    e <edge source 3> <edge target 3>
 *    e <edge source 4> <edge target 4>
 *    ...
 * }
 * }
 * </pre>
 * 
 * Although not specified directly in the DIMACS format documentation, this implementation also
 * allows for the a weighted variant:
 * 
 * <pre>
 * {@code 
 * e <edge source 1> <edge target 1> <edge_weight> 
 * }
 * </pre>
 *
 * <p>
 * By default this importer recomputes node identifiers starting from $0$ as they are encountered in
 * the file. It is also possible to instruct the importer to keep the original file numbering of the
 * nodes. Additionally you can also instruct the importer to use zero-based numbering or keep the
 * original number of DIMACS which starts from one.
 * 
 * Note: the current implementation does not fully implement the DIMACS specifications! Special
 * (rarely used) fields specified as 'Optional Descriptors' are currently not supported (ignored).
 *
 * @author Michael Behrisch (adaptation of GraphReader class)
 * @author Joris Kinable
 * @author Dimitrios Michail
 * 
 */
public class DIMACSEventDrivenImporter
    extends
    BaseEventDrivenImporter<Integer, Triple<Integer, Integer, Double>>
    implements
    EventDrivenImporter<Integer, Triple<Integer, Integer, Double>>
{
    private boolean zeroBasedNumbering;
    private boolean renumberVertices;

    private Map<String, Integer> vertexMap;
    private int nextId;

    /**
     * Construct a new importer
     */
    public DIMACSEventDrivenImporter()
    {
        super();
        this.zeroBasedNumbering = true;
        this.renumberVertices = true;
        this.vertexMap = new HashMap<>();
    }

    /**
     * Set whether to use zero-based numbering for vertices.
     * 
     * The DIMACS format by default starts vertices numbering from one. If true then we will use
     * zero-based numbering. Default to true.
     * 
     * @param zeroBasedNumbering whether to use zero-based numbering
     * @return the importer
     */
    public DIMACSEventDrivenImporter zeroBasedNumbering(boolean zeroBasedNumbering)
    {
        this.zeroBasedNumbering = zeroBasedNumbering;
        return this;
    }

    /**
     * Set whether to renumber vertices or not.
     * 
     * If true then the vertices are assigned new numbers from $0$ to $n-1$ in the order that they
     * are first encountered in the file. Otherwise, the original numbering (minus one in order to
     * get a zero-based numbering) of the DIMACS file is kept. Defaults to true.
     * 
     * @param renumberVertices whether to renumber vertices or not
     * @return the importer
     */
    public DIMACSEventDrivenImporter renumberVertices(boolean renumberVertices)
    {
        this.renumberVertices = renumberVertices;
        return this;
    }

    @Override
    public void importInput(Reader input)
    {
        // convert to buffered
        BufferedReader in;
        if (input instanceof BufferedReader) {
            in = (BufferedReader) input;
        } else {
            in = new BufferedReader(input);
        }

        if (zeroBasedNumbering) {
            this.nextId = 0;
        } else {
            this.nextId = 1;
        }

        notifyImportEvent(ImportEvent.START);

        // nodes
        final int size = readNodeCount(in);
        notifyVertexCount(size);

        // add edges
        String[] cols = skipComments(in);
        while (cols != null) {
            if (cols[0].equals("e") || cols[0].equals("a")) {
                if (cols.length < 3) {
                    throw new ImportException("Failed to parse edge:" + Arrays.toString(cols));
                }
                Integer source;
                try {
                    source = Integer.parseInt(cols[1]);
                } catch (NumberFormatException e) {
                    throw new ImportException(
                        "Failed to parse edge source node:" + e.getMessage(), e);
                }
                Integer target;
                try {
                    target = Integer.parseInt(cols[2]);
                } catch (NumberFormatException e) {
                    throw new ImportException(
                        "Failed to parse edge target node:" + e.getMessage(), e);
                }

                Integer from = mapVertexToInteger(String.valueOf(source));
                Integer to = mapVertexToInteger(String.valueOf(target));

                Double weight = null;
                if (cols.length > 3) {
                    try {
                        weight = Double.parseDouble(cols[3]);
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }

                // notify
                notifyEdge(Triple.of(from, to, weight));
            }
            cols = skipComments(in);
        }

        notifyImportEvent(ImportEvent.END);
    }

    private String[] split(final String src)
    {
        if (src == null) {
            return null;
        }
        return src.split("\\s+");
    }

    private String[] skipComments(BufferedReader input)
    {
        String[] cols = null;
        try {
            cols = split(input.readLine());
            while ((cols != null)
                && ((cols.length == 0) || cols[0].equals("c") || cols[0].startsWith("%")))
            {
                cols = split(input.readLine());
            }
        } catch (IOException e) {
            // ignore
        }
        return cols;
    }

    private int readNodeCount(BufferedReader input)
        throws ImportException
    {
        final String[] cols = skipComments(input);
        if (cols[0].equals("p")) {
            if (cols.length < 3) {
                throw new ImportException("Failed to read number of vertices.");
            }
            Integer nodes;
            try {
                nodes = Integer.parseInt(cols[2]);
            } catch (NumberFormatException e) {
                throw new ImportException("Failed to read number of vertices.");
            }
            if (nodes < 0) {
                throw new ImportException("Negative number of vertices.");
            }
            return nodes;
        }
        throw new ImportException("Failed to read number of vertices.");
    }

    /**
     * Map a vertex identifier to an integer.
     * 
     * @param id the vertex identifier
     * @return the integer
     */
    protected Integer mapVertexToInteger(String id)
    {
        if (renumberVertices) {
            return vertexMap.computeIfAbsent(id, (keyId) -> {
                return nextId++;
            });
        } else {
            if (zeroBasedNumbering) {
                return Integer.valueOf(id) - 1;
            } else {
                return Integer.valueOf(id);
            }
        }
    }

}
