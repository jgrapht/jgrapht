/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/* -------------------
 * GraphReader.java
 * -------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 16-Sep-2003 : Initial revision (BN);
 *
 */
package org.jgrapht.experimental;

import com.google.common.collect.Maps;
import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.WeightedGraph;
import org.jgrapht.generate.GraphGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;


public class GraphReader<V, E>
    implements GraphGenerator<V, E, V>
{
    //~ Instance fields --------------------------------------------------------

    // ~ Static fields/initializers --------------------------------------------

    // ~ Instance fields -------------------------------------------------------

    // ~ Static fields/initializers --------------------------------------------

    // ~ Instance fields -------------------------------------------------------

    private final BufferedReader _in;
    private final boolean _isWeighted;
    private final double _defaultWeight;

    // ~ Constructors ----------------------------------------------------------

    //~ Constructors -----------------------------------------------------------

    /**
     * Construct a new GraphReader.
     */
    private GraphReader(
        final Reader input, final boolean isWeighted, final double defaultWeight)
        throws IOException
    {
        if (input instanceof BufferedReader) {
            _in = (BufferedReader) input;
        } else {
            _in = new BufferedReader(input);
        }
        _isWeighted = isWeighted;
        _defaultWeight = defaultWeight;
    }

    /**
     * Construct a new GraphReader.
     */
    public GraphReader(final Reader input)
        throws IOException
    {
        this(input, false, 1);
    }

    /**
     * Construct a new GraphReader.
     */
    public GraphReader(final Reader input, final double defaultWeight)
        throws IOException
    {
        this(input, true, defaultWeight);
    }

    //~ Methods ----------------------------------------------------------------

    // ~ Methods ---------------------------------------------------------------

    private String [] split(final String src)
    {
        if (src == null) {
            return null;
        }
        return src.split("\\s+");
    }

    private String [] skipComments()
    {
        String [] cols = null;
        try {
            cols = split(_in.readLine());
            while (
                cols != null
                && (cols.length == 0
                    || "c".equals(cols[0])
                    || cols[0].startsWith("%")))
            {
                cols = split(_in.readLine());
            }
        } catch (IOException e) {
        }
        return cols;
    }

    private int readNodeCount()
    {
        final String [] cols = skipComments();
        if ("p".equals(cols[0])) {
            return Integer.parseInt(cols[1]);
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateGraph(
        final Graph<V, E> target,
        final VertexFactory<V> vertexFactory,
        Map<String, V> resultMap)
    {
        final int size = readNodeCount();
        if (resultMap == null) {
            resultMap = Maps.newHashMap();
        }

        for (int i = 0; i < size; i++) {
            final V newVertex = vertexFactory.createVertex();
            target.addVertex(newVertex);
            resultMap.put(Integer.toString(i + 1), newVertex);
        }
        String [] cols = skipComments();
        while (cols != null) {
            if ("e".equals(cols[0])) {
                final E edge =
                    target.addEdge(
                        resultMap.get(cols[1]),
                        resultMap.get(cols[2]));
                if (_isWeighted && edge != null) {
                    double weight = _defaultWeight;
                    if (cols.length > 3) {
                        weight = Double.parseDouble(cols[3]);
                    }
                    ((WeightedGraph<V, E>) target).setEdgeWeight(edge, weight);
                }
            }
            cols = skipComments();
        }
    }
}

// End GraphReader.java
