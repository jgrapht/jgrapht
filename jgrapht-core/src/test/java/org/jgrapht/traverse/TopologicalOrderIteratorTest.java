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
/* ---------------------------
 * TopologicalOrderIteratorTest.java
 * ---------------------------
 * (C) Copyright 2005-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 25-Apr-2005 : Initial revision (JVS);
 *
 */
package org.jgrapht.traverse;

import org.jgrapht.DirectedGraph;
import org.jgrapht.EnhancedTestCase;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;

import java.util.Iterator;


/**
 * Tests for TopologicalOrderIterator.
 *
 * @author John V. Sichi
 * @since Apr 25, 2005
 */
public class TopologicalOrderIteratorTest
    extends EnhancedTestCase
{
    //~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    public void testRecipe()
    {
        final DirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);

        final String [] v = new String[9];

        v[0] = "preheat oven";
        v[1] = "sift dry ingredients";
        v[2] = "stir wet ingredients";
        v[3] = "mix wet and dry ingredients";
        v[4] = "spoon onto pan";
        v[5] = "bake";
        v[6] = "cool";
        v[7] = "frost";
        v[8] = "eat";

        // add in mixed up order
        graph.addVertex(v[4]);
        graph.addVertex(v[8]);
        graph.addVertex(v[1]);
        graph.addVertex(v[3]);
        graph.addVertex(v[7]);
        graph.addVertex(v[6]);
        graph.addVertex(v[0]);
        graph.addVertex(v[2]);
        graph.addVertex(v[5]);

        // specify enough edges to guarantee deterministic total order
        graph.addEdge(v[0], v[1]);
        graph.addEdge(v[1], v[2]);
        graph.addEdge(v[0], v[2]);
        graph.addEdge(v[1], v[3]);
        graph.addEdge(v[2], v[3]);
        graph.addEdge(v[3], v[4]);
        graph.addEdge(v[4], v[5]);
        graph.addEdge(v[5], v[6]);
        graph.addEdge(v[6], v[7]);
        graph.addEdge(v[7], v[8]);
        graph.addEdge(v[6], v[8]);

        Iterator<String> iter =
            new TopologicalOrderIterator<String, DefaultEdge>(graph);
        int i = 0;

        while (iter.hasNext()) {
            assertEquals(v[i], iter.next());
            ++i;
        }

        // Test with a reversed view
        final DirectedGraph<String, DefaultEdge> reversed =
            new EdgeReversedGraph<String, DefaultEdge>(graph);

        iter = new TopologicalOrderIterator<String, DefaultEdge>(reversed);
        i = v.length - 1;

        while (iter.hasNext()) {
            assertEquals(v[i], iter.next());
            --i;
        }
    }

    /**
     * .
     */
    public void testEmptyGraph()
    {
        final DirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);
        final Iterator<String> iter =
            new TopologicalOrderIterator<String, DefaultEdge>(graph);
        assertFalse(iter.hasNext());
    }
}

// End TopologicalOrderIteratorTest.java
