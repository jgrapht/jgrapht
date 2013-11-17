/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
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

import com.touchgraph.graphlayout.*;
import com.touchgraph.graphlayout.interaction.*;

import java.awt.*;

import java.util.*;

import org.jgrapht.*;


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

    private Color defaultBackColor = new Color(0x01, 0x11, 0x44);
    private Color defaultBorderBackColor = new Color(0x02, 0x35, 0x81);
    private Color defaultForeColor =
        new Color((float) 0.95, (float) 0.85, (float) 0.55);

    /**
     * the JGraphT graph we are displaying
     */
    Graph<V, E> graph;

    /**
     * are self-references allowed? They will not show up in TouchGraph unless
     * you override Touchgraph's Node or Edge class to do so
     */
    boolean selfReferencesAllowed = true;

    // =================

    //~ Constructors -----------------------------------------------------------

    /**constructor*/
    public TouchgraphPanel(Graph<V, E> graph, boolean selfReferencesAllowed)
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
        this.setBackground(defaultBorderBackColor);
        this.setForeground(defaultForeColor);
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
    public void initialize()
    {
        buildPanel();
        buildLens();
        tgPanel.setLensSet(tgLensSet);
        addUIs();
        try {
            if (this.graph == null) {
                /*
                 * Add a random graph
                 */
                randomGraph();
            } else {
                /*
                 * Add users graph
                 */
                TouchgraphConverter<V, E> converter =
                    new TouchgraphConverter<V, E>();
                Node n =
                    (Node) converter.convertToTouchGraph(
                        this.graph,
                        tgPanel,
                        this.selfReferencesAllowed);
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
