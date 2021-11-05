/*
 * (C) Copyright 2016-2021, by Dimitrios Michail and Contributors.
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

/**
 * Supported CSV formats.
 * 
 * <ul>
 * <li>
 * <p>
 * Format {@link #EDGE_LIST} contains one edge per line. The following example
 * 
 * <pre>
 * a,b
 * b,c
 * </pre>
 * 
 * represents a graph with two edges: a-&gt;b and b-&gt;c.</li>
 * 
 * <li>
 * <p>
 * Format {@link #ADJACENCY_LIST} contains the adjacency list of each vertex per line. The first
 * field on a line is a vertex while the remaining fields are its neighbors.
 * 
 * <pre>
 * a,b
 * b,c,d
 * c,a,c,d
 * </pre>
 * 
 * represents a graph with edges: a-&gt;b, b-&gt;c, b-&gt;d, c-&gt;a, c-&gt;c, c-&gt;d.
 * 
 * <p>
 * Mixed variants of {@link #EDGE_LIST} and {@link #ADJACENCY_LIST} are also considered valid. As an
 * example consider the following input
 * 
 * <pre>
 * a,b
 * b,a
 * d,a
 * c,a,b
 * b,d,a
 * </pre>
 * 
 * which represents a graph with edges: a-&gt;b, b-&gt;a, d-&gt;a, c-&gt;a, c-&gt;b, b-&gt;d,
 * b-&gt;a. Multiple occurrences of the same edge result into a multi-graph.
 * 
 * <p>
 * Weighted variants are also valid if {@link CSVFormat.Parameter#EDGE_WEIGHTS} is set. In this case
 * the target vertex must be followed by the edge weight. The following example illustrates the
 * weighted variant:
 * 
 * <pre>
 * a,b,2.0
 * b,a,3.0
 * d,a,2.0
 * c,a,1.5,b,2.5
 * b,d,3.3,a,5.5
 * </pre>
 * 
 * </li>
 * <li>
 * <p>
 * Format {@link #MATRIX} outputs an adjacency matrix representation of the graph. Each line
 * represents a vertex.
 * 
 * The following
 * 
 * <pre>
 * 0,1,0,1,0
 * 1,0,0,0,0
 * 0,0,1,0,0
 * 0,1,0,1,0
 * 0,0,0,0,0
 * </pre>
 * 
 * represents a graph with five vertices 1,2,3,4,5 which contains edges: 1-&gt;2, 1-&gt;4, 2-&gt;1,
 * 3-&gt;3, 4-&gt;2, 4-&gt;4.
 * 
 * <p>
 * In case {@link CSVFormat.Parameter#MATRIX_FORMAT_ZERO_WHEN_NO_EDGE} is not set the equivalent
 * format would be:
 * 
 * <pre>
 * ,1,,1,
 * 1,,,,
 * ,,1,,
 * ,1,,1,
 * ,,,,
 * </pre>
 * 
 * <p>
 * Weighted variants are also valid if {@link CSVFormat.Parameter#EDGE_WEIGHTS} is set. The above
 * example would then be:
 * 
 * <pre>
 * ,1.0,,1.0,
 * 1.0,,,,
 * ,,1.0,,
 * ,1.0,,1.0,
 * ,,,,
 * </pre>
 * 
 * If additionally {@link CSVFormat.Parameter#MATRIX_FORMAT_ZERO_WHEN_NO_EDGE} is set then a zero as
 * an integer means that the corresponding edge is missing, while a zero as a double means than the
 * edge exists and has zero weight.
 * 
 * <p>
 * If parameter {@link CSVFormat.Parameter#MATRIX_FORMAT_NODEID} is set then node identifiers are
 * also included as in the following example:
 * 
 * <pre>
 * ,a,b,c,d,e
 * a,,1,,1,
 * b,1,,,,
 * c,,,1,,
 * d,,1,,1,
 * e,,,,,
 * </pre>
 * 
 * In the above example the first line contains the node identifiers and the first field of each
 * line contain the vertex it corresponds to. In case node identifiers are present line-shuffled
 * input is also valid such as:
 * 
 * <pre>
 * ,a,b,c,d,e
 * c,,,1,,
 * b,1,,,,
 * e,,,,,
 * d,,1,,1,
 * a,,1,,1,
 * </pre>
 * 
 * The last example represents the graph with edges: a-&gt;b, a-&gt;d, b-&gt;a, c-&gt;c, d-&gt;b,
 * d-&gt;d.
 * 
 * </li>
 * </ul>
 * 
 * @author Dimitrios Michail
 *
 */
public enum CSVFormat
{
    /**
     * Edge list
     */
    EDGE_LIST,
    /**
     * Adjacency list
     */
    ADJACENCY_LIST,
    /**
     * Matrix
     */
    MATRIX;

    /**
     * Parameters that affect the behavior of CVS importers/exporters.
     */
    public enum Parameter
    {
        /**
         * Whether to import/export edge weights.
         */
        EDGE_WEIGHTS,
        /**
         * Whether to import/export node ids. Only valid for the {@link CSVFormat#MATRIX MATRIX}
         * format.
         */
        MATRIX_FORMAT_NODEID,
        /**
         * Whether the input/output contains zero for missing edges. Only valid for the
         * {@link CSVFormat#MATRIX MATRIX} format.
         */
        MATRIX_FORMAT_ZERO_WHEN_NO_EDGE,
    }

}
