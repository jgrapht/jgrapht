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
 * SimpleTouchgraphApplet.java
 * -------------------
 * (C) Copyright 2006-2008, by Carl Anderson and Contributors.
 *
 * Original Author:  Carl Anderson
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 8-May-2006 : Initial revision (CA);
 *
 */
package org.jgrapht.experimental.touchgraph;

import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;


/**
 * SimpleTouchgraphApplet
 *
 * @author canderson
 */
public class SimpleTouchgraphApplet
    extends Applet
{
    //~ Static fields/initializers ---------------------------------------------

    /**
     */
    private static final long serialVersionUID = 6213379835360007840L;

    //~ Methods ----------------------------------------------------------------

    /**
     * create a graph: code taken from non-visible
     * org._3pq.jgrapht.demo.createStringGraph()
     */
    public static Graph<String, DefaultEdge> createSamplegraph()
    {
        final UndirectedGraph<String, DefaultEdge> g =
            new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

        final String v1 = "v1";
        final String v2 = "v2";
        final String v3 = "v3";
        final String v4 = "v4";

        // add the vertices
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);

        // add edges to create a circuit
        g.addEdge(v1, v2);
        g.addEdge(v2, v3);
        g.addEdge(v3, v4);
        g.addEdge(v4, v1);

        return g;
    }

    /**
     * initialize the applet
     */
    @Override
    public void init()
    {
        final Graph<String, DefaultEdge> g = createSamplegraph();
        final boolean selfReferencesAllowed = false;

        setLayout(new BorderLayout());
        setSize(800, 600);
        add(
            new TouchgraphPanel<String, DefaultEdge>(g, selfReferencesAllowed),
            BorderLayout.CENTER);
    }

    public static void main(final String [] args)
    {
        final Graph<String, DefaultEdge> g = createSamplegraph();
        final boolean selfReferencesAllowed = false;

        final JFrame frame = new JFrame();
        frame.getContentPane().add(
            new TouchgraphPanel<String, DefaultEdge>(g, selfReferencesAllowed));
        frame.setPreferredSize(new Dimension(800, 800));
        frame.setTitle("JGraphT to Touchgraph Converter Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        try {
            Thread.sleep(5000000);
        } catch (InterruptedException ex) {
        }
    }
}

// End SimpleTouchgraphApplet.java
