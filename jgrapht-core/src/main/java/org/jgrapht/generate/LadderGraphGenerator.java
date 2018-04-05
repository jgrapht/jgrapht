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
 * LadderGraphGenerator.java
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

import java.util.*;

import org.jgrapht.*;

/**
 * Generates a <a href="http://mathworld.wolfram.com/LadderGraph.html">ladder
 * graph</a> of any size.it is same as grid graph but with number of collumns
 * fixed to two.
 *
 * @author John V. Sichi
 * @since Sep 16, 2003
 */
 public class LadderGraphGenerator<V, E>
    implements GraphGenerator<V, E, V>
{
     //~ Static fields/initializers ---------------------------------------------
     //~ Instance fields --------------------------------------------------------

    private int rows;

    private int cols=2;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ladderGraphGenerator object with rows x cols dimension.
     *
     * @param rows the number of rows
     * @param cols the number of columns
     */
     public ladderGraphGenerator(int rows, int cols)
    {
        if (rows < 2) {
            throw new IllegalArgumentException(
                "illegal number of rows (" + rows
                + "). there must be at least two.");
        }
        if (cols < 2) {
            throw new IllegalArgumentException(
                "illegal number of columns (" + cols
                + "). there must be at least two.");
        }
        this.rows = rows;
        this.cols = cols;
    }

   //~ Methods ----------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
     @Override public void generateGraph(
        Graph<V, E> target,
        VertexFactory<V> vertexFactory,
        Map<String, V> resultMap)
    {
    GridGraphGenerator<V, E> gridGenerator =
            new gridGraphGenerator<V, E>(row,cols);
             gridGenerator.generateGraph(target, vertexFactory, resultMap);
}
  }      
