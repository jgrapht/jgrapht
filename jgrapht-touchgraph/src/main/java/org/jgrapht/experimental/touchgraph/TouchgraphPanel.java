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
 * TouchgraphPanel.java
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

import com.touchgraph.graphlayout.GLPanel;
import com.touchgraph.graphlayout.Node;
import com.touchgraph.graphlayout.TGException;
import com.touchgraph.graphlayout.TGLensSet;
import com.touchgraph.graphlayout.TGPanel;
import com.touchgraph.graphlayout.interaction.HVScroll;
import com.touchgraph.graphlayout.interaction.HyperScroll;
import com.touchgraph.graphlayout.interaction.LocalityScroll;
import com.touchgraph.graphlayout.interaction.RotateScroll;
import com.touchgraph.graphlayout.interaction.ZoomScroll;
import org.jgrapht.Graph;

import java.awt.*;
import java.util.Hashtable;


/**
 * The Touchgraph panel that displays our graph
 * http://sourceforge.net/projects/touchgraph
 *
 * @author canderson
 */
public class TouchgraphPanel<V, E>
    extends GLPanel
{
    //~ Static fields/initializers ---------------------------------------------

    /**
     */
    private static final long serialVersionUID = -7441058429719746032L;

    //~ Instance fields --------------------------------------------------------

    private final Color defaultBackColor = new Color(0x01, 0x11, 0x44);
    private final Color defaultBorderBackColor = new Color(0x02, 0x35, 0x81);
    private final Color defaultForeColor =
        new Color((float) 0.95, (float) 0.85, (float) 0.55);

    /**
     * the JGraphT graph we are displaying
     */
    final Graph<V, E> graph;

    /**
     * are self-references allowed? They will not show up in TouchGraph unless
     * you override Touchgraph's Node or Edge class to do so
     */
    boolean selfReferencesAllowed = true;

    // =================

    //~ Constructors -----------------------------------------------------------

    /**constructor*/
    public TouchgraphPanel(final Graph<V, E> graph, final boolean selfReferencesAllowed)
    {
        this.graph = graph;
        this.selfReferencesAllowed = selfReferencesAllowed;

        /*
         * The code that was in the super's constructor. As it also called
         * super's initialize()
         *  it is impossible to subclass and insert our own graph into the
         * initialization process
         */
        preinitialize();

        initialize(); // now we can insert our own graph into this method
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * get everything setup: this is the code that was in the super's
     * constructor but which was followed by an initialize() call. Hence, it was
     * impossible to subclass the superclass and insert our own graph
     * initialization code without breaking it out as here.
     */
    public void preinitialize()
    {
        setBackground(defaultBorderBackColor);
        setForeground(defaultForeColor);
        scrollBarHash = new Hashtable();
        tgLensSet = new TGLensSet();
        tgPanel = new TGPanel();
        tgPanel.setBackColor(defaultBackColor);
        hvScroll = new HVScroll(tgPanel, tgLensSet);
        zoomScroll = new ZoomScroll(tgPanel);
        hyperScroll = new HyperScroll(tgPanel);
        rotateScroll = new RotateScroll(tgPanel);
        localityScroll = new LocalityScroll(tgPanel);
    }

    /**
     * Initialize panel, lens, and establish a random graph as a demonstration.
     */
    @Override
    public void initialize()
    {
        buildPanel();
        buildLens();
        tgPanel.setLensSet(tgLensSet);
        addUIs();
        try {
            if (graph == null) {
                /*
                 * Add a random graph
                 */
                randomGraph();
            } else {
                /*
                 * Add users graph
                 */
                final TouchgraphConverter<V, E> converter =
                    new TouchgraphConverter<V, E>();
                final Node n = converter.convertToTouchGraph(graph,
                    tgPanel, selfReferencesAllowed);
                getHVScroll().slowScrollToCenter(n);
                tgPanel.setLocale(n, Integer.MAX_VALUE);
            }
        } catch (TGException tge) {
            System.err.println(tge.getMessage());
            tge.printStackTrace(System.err);
        }
        setVisible(true);
    }
}

// End TouchgraphPanel.java
