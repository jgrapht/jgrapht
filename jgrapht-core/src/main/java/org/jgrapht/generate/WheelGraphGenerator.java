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
 * WheelGraphGenerator.java
 * -------------------
 * (C) Copyright 2003-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 16-Sep-2003 : Initial revision (JVS);
 *
 */
package org.jgrapht.generate;

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;


/**
 * Generates a <a href="http://mathworld.wolfram.com/WheelGraph.html">wheel
 * graph</a> of any size. Reminding a bicycle wheel, a wheel graph has a hub
 * vertex in the center and a rim of vertices around it that are connected to
 * each other (as a ring). The rim vertices are also connected to the hub with
 * edges that are called "spokes".
 *
 * @author John V. Sichi
 * @since Sep 16, 2003
 */
public class WheelGraphGenerator<V, E>
    implements GraphGenerator<V, E, V>
{
    //~ Static fields/initializers ---------------------------------------------

    /**
     * Role for the hub vertex.
     */
    public static final String HUB_VERTEX = "Hub Vertex";

    //~ Instance fields --------------------------------------------------------

    private final boolean inwardSpokes;
    private final int size;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new WheelGraphGenerator object. This constructor is more
     * suitable for undirected graphs, where spokes' direction is meaningless.
     * In the directed case, spokes will be oriented from rim to hub.
     *
     * @param size number of vertices to be generated.
     */
    public WheelGraphGenerator(final int size)
    {
        this(size, true);
    }

    /**
     * Construct a new WheelGraphGenerator.
     *
     * @param size number of vertices to be generated.
     * @param inwardSpokes if <code>true</code> and graph is directed, spokes
     * are oriented from rim to hub; else from hub to rim.
     *
     * @throws IllegalArgumentException
     */
    public WheelGraphGenerator(final int size, final boolean inwardSpokes)
    {
        if (size < 0) {
            throw new IllegalArgumentException("must be non-negative");
        }

        this.size = size;
        this.inwardSpokes = inwardSpokes;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateGraph(
        final Graph<V, E> target,
        final VertexFactory<V> vertexFactory,
        final Map<String, V> resultMap)
    {
        if (size < 1) {
            return;
        }

        // A little trickery to intercept the rim generation.  This is
        // necessary since target may be initially non-empty, meaning we can't
        // rely on its vertex set after the rim is generated.
        final Collection<V> rim = new ArrayList<V>();
        final VertexFactory<V> rimVertexFactory =
            new VertexFactory<V>() {
                @Override
                public V createVertex()
                {
                    final V vertex = vertexFactory.createVertex();
                    rim.add(vertex);

                    return vertex;
                }
            };

        final RingGraphGenerator<V, E> ringGenerator =
            new RingGraphGenerator<V, E>(size - 1);
        ringGenerator.generateGraph(target, rimVertexFactory, resultMap);

        final V hubVertex = vertexFactory.createVertex();
        target.addVertex(hubVertex);

        if (resultMap != null) {
            resultMap.put(HUB_VERTEX, hubVertex);
        }

        for (final V rimVertex : rim) {
            if (inwardSpokes) {
                target.addEdge(rimVertex, hubVertex);
            } else {
                target.addEdge(hubVertex, rimVertex);
            }
        }
    }
}

// End WheelGraphGenerator.java
