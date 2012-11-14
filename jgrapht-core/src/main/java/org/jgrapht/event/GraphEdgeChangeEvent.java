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
/* -------------------------
 * GraphEdgeChangeEvent.java
 * -------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 10-Aug-2003 : Initial revision (BN);
 * 11-Mar-2004 : Made generic (CH);
 *
 */
package org.jgrapht.event;

/**
 * An event which indicates that a graph edge has changed, or is about to
 * change. The event can be used either as an indication <i>after</i> the edge
 * has been added or removed, or <i>before</i> it is added. The type of the
 * event can be tested using the {@link
 * GraphChangeEvent#getType()} method.
 *
 * @author Barak Naveh
 * @since Aug 10, 2003
 */
public class GraphEdgeChangeEvent<V, E>
    extends GraphChangeEvent
{
    //~ Static fields/initializers ---------------------------------------------

    private static final long serialVersionUID = 3618134563335844662L;

    /**
     * Before edge added event. This event is fired before an edge is added to a
     * graph.
     */
    public static final int BEFORE_EDGE_ADDED = 21;

    /**
     * Before edge removed event. This event is fired before an edge is removed
     * from a graph.
     */
    public static final int BEFORE_EDGE_REMOVED = 22;

    /**
     * Edge added event. This event is fired after an edge is added to a graph.
     */
    public static final int EDGE_ADDED = 23;

    /**
     * Edge removed event. This event is fired after an edge is removed from a
     * graph.
     */
    public static final int EDGE_REMOVED = 24;

    //~ Instance fields --------------------------------------------------------

    /**
     * The edge that this event is related to.
     */
    protected E edge;

    /**
     * The source vertex of the edge that this event is related to.
     */
    protected V edgeSource;

    /**
     * The target vertex of the edge that this event is related to.
     */
    protected V edgeTarget;

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructor for GraphEdgeChangeEvent.
     *
     * @param eventSource the source of this event.
     * @param type the event type of this event.
     * @param edge the edge that this event is related to.
     *
     * @deprecated Use new constructor which takes vertex parameters.
     */
    public GraphEdgeChangeEvent(
        final Object eventSource, final int type, final E edge)
    {
        this(eventSource, type, edge, null, null);
    }

    /**
     * Constructor for GraphEdgeChangeEvent.
     *
     * @param eventSource the source of this event.
     * @param type the event type of this event.
     * @param edge the edge that this event is related to.
     * @param edgeSource edge source vertex
     * @param edgeTarget edge target vertex
     */
    public GraphEdgeChangeEvent(
        final Object eventSource, final int type, final E edge,
        final V edgeSource, final V edgeTarget)
    {
        super(eventSource, type);
        this.edge = edge;
        this.edgeSource = edgeSource;
        this.edgeTarget = edgeTarget;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns the edge that this event is related to.
     *
     * @return event edge
     */
    public E getEdge()
    {
        return edge;
    }

    /**
     * Returns the source vertex that this event is related to.
     *
     * @return event source vertex
     */
    public V getEdgeSource()
    {
        return edgeSource;
    }

    /**
     * Returns the target vertex that this event is related to.
     *
     * @return event target vertex
     */
    public V getEdgeTarget()
    {
        return edgeTarget;
    }
}

// End GraphEdgeChangeEvent.java
