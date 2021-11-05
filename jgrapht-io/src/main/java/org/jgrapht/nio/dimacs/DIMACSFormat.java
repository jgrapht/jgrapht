/*
 * (C) Copyright 2017-2021, by Dimitrios Michail and Contributors.
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

/**
 * DIMACS challenge format.
 * 
 * <p>
 * For a general description of the formats see <a href="http://dimacs.rutgers.edu/Challenges/">
 * http://dimacs.rutgers.edu/Challenges</a>. Note that there are a lot of different formats based on
 * each different challenge.
 * 
 * @author Dimitrios Michail
 */
public enum DIMACSFormat
{
    /**
     * Shortest path challenge format.
     * 
     * <p>
     * This is the <a href="http://www.dis.uniroma1.it/challenge9/format.shtml">format</a> used in
     * the 9th DIMACS implementation challenge.
     * 
     * A shortest path graph file looks as follows:
     * 
     * <pre>
     * {@code
     * c <comments>
     * p sp <number of nodes> <number of edges>
     * a <edge source 1> <edge target 1>
     * a <edge source 2> <edge target 2>
     * a <edge source 3> <edge target 3>
     * a <edge source 4> <edge target 4>
     * ...
     * }
     * </pre>
     * 
     * A weighted variant where each edge has a floating-point weight is also supported:
     * 
     * <pre>
     * {@code 
     * a <edge source 1> <edge target 1> <edge_weight> 
     * }
     * </pre>
     */
    SHORTEST_PATH("sp", "a"),
    /**
     * Max-clique challenge format.
     * 
     * <p>
     * This is the <a href="http://mat.gsia.cmu.edu/COLOR/general/ccformat.ps">format</a> used in
     * the 2nd DIMACS implementation challenge.
     * 
     * A graph file looks as follows:
     * 
     * <pre>
     * {@code
     * c <comments>
     * p edge <number of nodes> <number of edges>
     * e <edge source 1> <edge target 1>
     * e <edge source 2> <edge target 2>
     * e <edge source 3> <edge target 3>
     * e <edge source 4> <edge target 4>
     * ...
     * }
     * </pre>
     * 
     * A weighted variant where each edge has a floating-point weight is also supported:
     * 
     * <pre>
     * {@code 
     * e <edge source 1> <edge target 1> <edge_weight> 
     * }
     * </pre>
     */
    MAX_CLIQUE("edge", "e"),
    /**
     * Coloring format.
     * 
     * <p>
     * This is the <a href="ftp://dimacs.rutgers.edu/pub/challenge/">format</a> used in the 2nd
     * DIMACS implementation challenge. Same as the {@link DIMACSFormat#MAX_CLIQUE} but uses "col"
     * instead of "edge" in the problem definition line.
     */
    COLORING("col", "e");

    private final String problem;
    private final String edge;

    private DIMACSFormat(String problem, String edge)
    {
        this.problem = problem;
        this.edge = edge;
    }

    /**
     * Get the name of the problem.
     * 
     * @return the name of the problem.
     */
    public String getProblem()
    {
        return problem;
    }

    /**
     * Get the edge descriptor used in the format.
     * 
     * @return the edge descriptor
     */
    public String getEdgeDescriptor()
    {
        return edge;
    }

}
