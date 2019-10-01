/*
 * (C) Copyright 2019-2019, by Dimitrios Michail and Contributors.
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
package org.jgrapht.io.edgelist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

import org.jgrapht.io.DIMACSFormat;
import org.jgrapht.io.ImportException;

/**
 * Imports a graph specified in DIMACS format.
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
 * Note: the current implementation does not fully implement the DIMACS specifications! Special
 * (rarely used) fields specified as 'Optional Descriptors' are currently not supported (ignored).
 *
 * @author Dimitrios Michail
 */
public class DIMACSEdgeListImporter
    extends
    AbstractBaseEdgeListImporter
    implements
    EdgeListImporter
{
    /**
     * Construct a new importer
     */
    public DIMACSEdgeListImporter()
    {
        super();
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

    @Override
    public void importEdgeList(Reader input)
        throws ImportException
    {
        // convert to buffered
        BufferedReader in;
        if (input instanceof BufferedReader) {
            in = (BufferedReader) input;
        } else {
            in = new BufferedReader(input);
        }

        // nodes
        final int size = readNodeCount(in);
        notifyNodeCount(size);

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
                notifyEdge(from, to, weight);

            }
            cols = skipComments(in);
        }
    }

}
