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
/* -----------------------
 * JGraphModelAdapter.java
 * -----------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Erik Postma
 *
 * $Id$
 *
 * Changes
 * -------
 * 02-Aug-2003 : Initial revision (BN);
 * 10-Aug-2003 : Adaptation to new event model (BN);
 * 06-Nov-2003 : Allowed non-listenable underlying JGraphT graph (BN);
 * 12-Dec-2003 : Added CellFactory support (BN);
 * 27-Jan-2004 : Added support for JGraph->JGraphT change propagation (EP);
 * 29-Jan-2005 : Added support for JGraph dangling edges (BN);
 * 07-May-2006 : Changed from List<Edge> to Set<Edge> (JVS);
 * 28-May-2006 : Moved connectivity info from edge to graph (JVS);
 *
 */
package org.jgrapht.ext;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.*;

import java.io.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.*;

import org.jgraph.event.*;
import org.jgraph.event.GraphModelEvent.*;
import org.jgraph.graph.*;

import org.jgrapht.*;
import org.jgrapht.event.*;


/**
 * An adapter that reflects a JGraphT graph as a JGraph graph. This adapter is
 * useful when using JGraph in order to visualize JGraphT graphs. For more about
 * JGraph see <a href="http://jgraph.sourceforge.net">
 * http://jgraph.sourceforge.net</a>
 *
 * <p>Modifications made to the underlying JGraphT graph are reflected to this
 * JGraph model if and only if the underlying JGraphT graph is a {@link
 * org.jgrapht.ListenableGraph}. If the underlying JGraphT graph is <i>not</i>
 * ListenableGraph, then this JGraph model represent a snapshot if the graph at
 * the time of its creation.</p>
 *
 * <p>Changes made to this JGraph model are also reflected back to the
 * underlying JGraphT graph. To avoid confusion, variables are prefixed
 * according to the JGraph/JGraphT object(s) they are referring to.</p>
 *
 * <p><b>KNOWN BUGS:</b> There is a small issue to be aware of. JGraph allows
 * 'dangling edges' incident with just one vertex; JGraphT doesn't. Such a
 * configuration can arise when adding an edge or removing a vertex. The code
 * handles this by removing the newly-added dangling edge or removing all edges
 * incident with the vertex before actually removing the vertex, respectively.
 * This works very well, only it doesn't play all that nicely with the
 * undo-manager in the JGraph: for the second situation where you remove a
 * vertex incident with some edges, if you undo the removal, the vertex is
 * 'unremoved' but the edges aren't.</p>
 *
 * @author Barak Naveh
 * @since Aug 2, 2003
 */

/*
 * FUTURE WORK: Now that the adapter supports JGraph dangling edges, it is
 * possible, with a little effort, to eliminate the "known bugs" above. Some
 * todo and fixme marks in the code indicate where the possible improvements
 * could be made to realize that.
 */
public class JGraphModelAdapter<V, E>
    extends DefaultGraphModel
{
    

    private static final long serialVersionUID = 3256722883706302515L;

    

    /**
     * The following (jCells|jtElement)Being(Added|Removed) sets are used to
     * prevent bouncing of events between the JGraph and JGraphT listeners. They
     * ensure that their respective add/remove operations are done exactly once.
     * Here is an example of how jCellsBeingAdded is used when an edge is added
     * to a JGraph graph:
     *
     * <pre>
        1. First, we add the desired edge to jCellsBeingAdded to indicate
        that the edge is being inserted internally.
        2.    Then we invoke the JGraph 'insert' operation.
        3.    The JGraph listener will detect the newly inserted edge.
        4.    It checks if the edge is contained in jCellsBeingAdded.
        5.    If yes,
        it just removes it and does nothing else.
        if no,
        it knows that the edge was inserted externally and performs
        the insertion.
        6. Lastly, we remove the edge from the jCellsBeingAdded.
     * </pre>
     *
     * <p>Step 6 is not always required but we do it anyway as a safeguard
     * against the rare case where the edge to be added is already contained in
     * the graph and thus NO event will be fired. If 6 is not done, a junk edge
     * will remain in the jCellsBeingAdded set.</p>
     *
     * <p>The other sets are used in a similar manner to the above. Apparently,
     * All that complication could be eliminated if JGraph and JGraphT had both
     * allowed operations that do not inform listeners...</p>
     */
    final Set<GraphCell> jCellsBeingAdded = new HashSet<GraphCell>();
    final Set<GraphCell> jCellsBeingRemoved = new HashSet<GraphCell>();
    final Set<Object> jtElementsBeingAdded = new HashSet<Object>();
    final Set<Object> jtElementsBeingRemoved = new HashSet<Object>();
    private final CellFactory<V, E> cellFactory;

    /**
     * Maps JGraph edges to JGraphT edges
     */
    private final Map<org.jgraph.graph.Edge, E> cellToEdge =
        new HashMap<org.jgraph.graph.Edge, E>();

    /**
     * Maps JGraph vertices to JGraphT vertices
     */
    private final Map<GraphCell, V> cellToVertex = new HashMap<GraphCell, V>();
    private AttributeMap defaultEdgeAttributes;
    private AttributeMap defaultVertexAttributes;

    /**
     * Maps JGraphT edges to JGraph edges
     */
    private final Map<E, org.jgraph.graph.Edge> edgeToCell =
        new HashMap<E, org.jgraph.graph.Edge>();

    /**
     * Maps JGraphT vertices to JGraph vertices
     */
    private final Map<V, GraphCell> vertexToCell = new HashMap<V, GraphCell>();
    private final ShieldedGraph jtGraph;

    

    /**
     * Constructs a new JGraph model adapter for the specified JGraphT graph.
     *
     * @param jGraphTGraph the JGraphT graph for which JGraph model adapter to
     * be created. <code>null</code> is NOT permitted.
     */
    public JGraphModelAdapter(Graph<V, E> jGraphTGraph)
    {
        this(
            jGraphTGraph,
            createDefaultVertexAttributes(),
            createDefaultEdgeAttributes(jGraphTGraph));
    }

    /**
     * Constructs a new JGraph model adapter for the specified JGraphT graph.
     *
     * @param jGraphTGraph the JGraphT graph for which JGraph model adapter to
     * be created. <code>null</code> is NOT permitted.
     * @param defaultVertexAttributes a default map of JGraph attributes to
     * format vertices. <code>null</code> is NOT permitted.
     * @param defaultEdgeAttributes a default map of JGraph attributes to format
     * edges. <code>null</code> is NOT permitted.
     */
    public JGraphModelAdapter(
        Graph<V, E> jGraphTGraph,
        AttributeMap defaultVertexAttributes,
        AttributeMap defaultEdgeAttributes)
    {
        this(
            jGraphTGraph,
            defaultVertexAttributes,
            defaultEdgeAttributes,
            new DefaultCellFactory<V, E>());
    }

    /**
     * Constructs a new JGraph model adapter for the specified JGraphT graph.
     *
     * @param jGraphTGraph the JGraphT graph for which JGraph model adapter to
     * be created. <code>null</code> is NOT permitted.
     * @param defaultVertexAttributes a default map of JGraph attributes to
     * format vertices. <code>null</code> is NOT permitted.
     * @param defaultEdgeAttributes a default map of JGraph attributes to format
     * edges. <code>null</code> is NOT permitted.
     * @param cellFactory a {@link CellFactory} to be used to create the JGraph
     * cells. <code>null</code> is NOT permitted.
     *
     * @throws IllegalArgumentException
     */
    public JGraphModelAdapter(
        Graph<V, E> jGraphTGraph,
        AttributeMap defaultVertexAttributes,
        AttributeMap defaultEdgeAttributes,
        CellFactory<V, E> cellFactory)
    {
        super();

        if ((jGraphTGraph == null)
            || (defaultVertexAttributes == null)
            || (defaultEdgeAttributes == null)
            || (cellFactory == null))
        {
            throw new IllegalArgumentException("null is NOT permitted");
        }

        jtGraph = new ShieldedGraph(jGraphTGraph);
        setDefaultVertexAttributes(defaultVertexAttributes);
        setDefaultEdgeAttributes(defaultEdgeAttributes);
        this.cellFactory = cellFactory;

        if (jGraphTGraph instanceof ListenableGraph<?, ?>) {
            ListenableGraph<V, E> g = (ListenableGraph<V, E>) jGraphTGraph;
            g.addGraphListener(new JGraphTListener());
        }

        for (
            Iterator<V> i = jGraphTGraph.vertexSet().iterator();
            i.hasNext();)
        {
            handleJGraphTAddedVertex(i.next());
        }

        for (Iterator<E> i = jGraphTGraph.edgeSet().iterator(); i.hasNext();) {
            handleJGraphTAddedEdge(i.next());
        }

        this.addGraphModelListener(new JGraphListener());
    }

    

    /**
     * Creates and returns a map of attributes to be used as defaults for edge
     * attributes, depending on the specified graph.
     *
     * @param jGraphTGraph the graph for which default edge attributes to be
     * created.
     *
     * @return a map of attributes to be used as default for edge attributes.
     */
    public static <V, E> AttributeMap createDefaultEdgeAttributes(
        Graph<V, E> jGraphTGraph)
    {
        AttributeMap map = new AttributeMap();

        if (jGraphTGraph instanceof DirectedGraph<?, ?>) {
            GraphConstants.setLineEnd(map, GraphConstants.ARROW_TECHNICAL);
            GraphConstants.setEndFill(map, true);
            GraphConstants.setEndSize(map, 10);
        }

        GraphConstants.setForeground(map, Color.decode("#25507C"));
        GraphConstants.setFont(
            map,
            GraphConstants.DEFAULTFONT.deriveFont(Font.BOLD, 12));
        GraphConstants.setLineColor(map, Color.decode("#7AA1E6"));

        return map;
    }

    /**
     * Creates and returns a map of attributes to be used as defaults for vertex
     * attributes.
     *
     * @return a map of attributes to be used as defaults for vertex attributes.
     */
    public static AttributeMap createDefaultVertexAttributes()
    {
        AttributeMap map = new AttributeMap();
        Color c = Color.decode("#FF9900");

        GraphConstants.setBounds(map, new Rectangle2D.Double(50, 50, 90, 30));
        GraphConstants.setBorder(map, BorderFactory.createRaisedBevelBorder());
        GraphConstants.setBackground(map, c);
        GraphConstants.setForeground(map, Color.white);
        GraphConstants.setFont(
            map,
            GraphConstants.DEFAULTFONT.deriveFont(Font.BOLD, 12));
        GraphConstants.setOpaque(map, true);

        return map;
    }

    /**
     * Returns the cell factory used to create the JGraph cells.
     *
     * @return the cell factory used to create the JGraph cells.
     */
    public CellFactory<V, E> getCellFactory()
    {
        return cellFactory;
    }

    /**
     * Sets the default edge attributes used for creating new JGraph edges.
     *
     * @param defaultEdgeAttributes the default edge attributes to set.
     */
    public void setDefaultEdgeAttributes(AttributeMap defaultEdgeAttributes)
    {
        this.defaultEdgeAttributes = defaultEdgeAttributes;
    }

    /**
     * Returns the default edge attributes used for creating new JGraph edges.
     *
     * @return the default edge attributes used for creating new JGraph edges.
     */
    public AttributeMap getDefaultEdgeAttributes()
    {
        return defaultEdgeAttributes;
    }

    /**
     * Sets the default vertex attributes used for creating new JGraph vertices.
     *
     * @param defaultVertexAttributes the default vertex attributes to set.
     */
    public void setDefaultVertexAttributes(
        AttributeMap defaultVertexAttributes)
    {
        this.defaultVertexAttributes = defaultVertexAttributes;
    }

    /**
     * Returns the default vertex attributes used for creating new JGraph
     * vertices.
     *
     * @return the default vertex attributes used for creating new JGraph
     * vertices.
     */
    public AttributeMap getDefaultVertexAttributes()
    {
        return defaultVertexAttributes;
    }

    /**
     * Returns the JGraph edge cell that corresponds to the specified JGraphT
     * edge. If no corresponding cell found, returns <code>null</code>.
     *
     * @param jGraphTEdge a JGraphT edge of the JGraphT graph.
     *
     * @return the JGraph edge cell that corresponds to the specified JGraphT
     * edge, or <code>null</code> if no corresponding cell found.
     */
    public DefaultEdge getEdgeCell(E jGraphTEdge)
    {
        return (DefaultEdge) edgeToCell.get(jGraphTEdge);
    }

    /**
     * Returns the JGraph vertex cell that corresponds to the specified JGraphT
     * vertex. If no corresponding cell found, returns <code>null</code>.
     *
     * @param jGraphTVertex a JGraphT vertex of the JGraphT graph.
     *
     * @return the JGraph vertex cell that corresponds to the specified JGraphT
     * vertex, or <code>null</code> if no corresponding cell found.
     */
    public DefaultGraphCell getVertexCell(Object jGraphTVertex)
    {
        return (DefaultGraphCell) vertexToCell.get(jGraphTVertex);
    }

    /**
     * Returns the JGraph port cell that corresponds to the specified JGraphT
     * vertex. If no corresponding port found, returns <code>null</code>.
     *
     * @param jGraphTVertex a JGraphT vertex of the JGraphT graph.
     *
     * @return the JGraph port cell that corresponds to the specified JGraphT
     * vertex, or <code>null</code> if no corresponding cell found.
     */
    public DefaultPort getVertexPort(Object jGraphTVertex)
    {
        DefaultGraphCell vertexCell = getVertexCell(jGraphTVertex);

        if (vertexCell == null) {
            return null;
        } else {
            return (DefaultPort) vertexCell.getChildAt(0);
        }
    }

    /**
     * Adds/removes an edge to/from the underlying JGraphT graph according to
     * the change in the specified JGraph edge. If both vertices are connected,
     * we ensure to have a corresponding JGraphT edge. Otherwise, we ensure NOT
     * to have a corresponding JGraphT edge.
     *
     * <p>This method is to be called only for edges that have already been
     * changed in the JGraph graph.</p>
     *
     * @param jEdge the JGraph edge that has changed.
     */
    void handleJGraphChangedEdge(org.jgraph.graph.Edge jEdge)
    {
        if (isDangling(jEdge)) {
            if (cellToEdge.containsKey(jEdge)) {
                // a non-dangling edge became dangling -- remove the JGraphT
                // edge by faking as if the edge is removed from the JGraph.
                // TODO: Consider keeping the JGraphT edges outside the graph
                // to avoid loosing user data, such as weights.
                handleJGraphRemovedEdge(jEdge);
            } else {
                // a dangling edge is still dangling -- just ignore.
            }
        } else {
            // edge is not dangling
            if (cellToEdge.containsKey(jEdge)) {
                // edge already has a corresponding JGraphT edge.
                // check if any change to its endpoints.
                E jtEdge = cellToEdge.get(jEdge);

                Object jSource = getSourceVertex(this, jEdge);
                Object jTarget = getTargetVertex(this, jEdge);

                Object jtSource = cellToVertex.get(jSource);
                Object jtTarget = cellToVertex.get(jTarget);

                if ((jtGraph.getEdgeSource(jtEdge) == jtSource)
                    && (jtGraph.getEdgeTarget(jtEdge) == jtTarget))
                {
                    // no change in edge's endpoints -- nothing to do.
                } else {
                    // edge's end-points have changed -- need to refresh the
                    // JGraphT edge. Refresh by faking as if the edge has been
                    // removed from JGraph and then added again.
                    // ALSO HERE: consider an alternative that maintains user
                    // data
                    handleJGraphRemovedEdge(jEdge);
                    handleJGraphInsertedEdge(jEdge);
                }
            } else {
                // a new edge
                handleJGraphInsertedEdge(jEdge);
            }
        }
    }

    /**
     * Adds to the underlying JGraphT graph an edge that corresponds to the
     * specified JGraph edge. If the specified JGraph edge is a dangling edge,
     * it is NOT added to the underlying JGraphT graph.
     *
     * <p>This method is to be called only for edges that have already been
     * added to the JGraph graph.</p>
     *
     * @param jEdge the JGraph edge that has been added.
     */
    void handleJGraphInsertedEdge(org.jgraph.graph.Edge jEdge)
    {
        if (isDangling(jEdge)) {
            // JGraphT forbid dangling edges so we cannot add the edge yet. If
            // later the edge becomes connected, we will add it.
        } else {
            // FIXME hb 28-nov-05: waiting for jgraph to go generic
            Object jSource = getSourceVertex(this, jEdge);
            Object jTarget = getTargetVertex(this, jEdge);

            V jtSource = cellToVertex.get(jSource);
            V jtTarget = cellToVertex.get(jTarget);

            E jtEdge = jtGraph.addEdge(jtSource, jtTarget);

            if (jtEdge != null) {
                cellToEdge.put(jEdge, jtEdge);
                edgeToCell.put(jtEdge, jEdge);
            } else {
                // Adding failed because user is using a JGraphT graph the
                // forbids parallel edges.
                // For consistency, we remove the edge from the JGraph too.
                internalRemoveCell(jEdge);
                System.err.println(
                    "Warning: an edge was deleted because the underlying "
                    + "JGraphT graph refused to create it. "
                    + "This situation can happen when a constraint of the "
                    + "underlying graph is violated, e.g., an attempt to add "
                    + "a parallel edge or a self-loop to a graph that forbids "
                    + "them. To avoid this message, make sure to use a "
                    + "suitable underlying JGraphT graph.");
            }
        }
    }

    /**
     * Adds to the underlying JGraphT graph a vertex corresponding to the
     * specified JGraph vertex. In JGraph, two vertices with the same user
     * object are in principle allowed; in JGraphT, this would lead to duplicate
     * vertices, which is not allowed. So if such vertex already exists, the
     * specified vertex is REMOVED from the JGraph graph and a a warning is
     * printed.
     *
     * <p>This method is to be called only for vertices that have already been
     * added to the JGraph graph.</p>
     *
     * @param jVertex the JGraph vertex that has been added.
     */
    @SuppressWarnings("unchecked")
    void handleJGraphInsertedVertex(GraphCell jVertex)
    {
        V jtVertex;

        if (jVertex instanceof DefaultGraphCell) {
            // FIXME hb 28-nov-05: waiting for jgraph to go generic
            jtVertex = (V) ((DefaultGraphCell) jVertex).getUserObject();
        } else {
            // FIXME: Why toString? Explain if for a good reason otherwise fix.
            jtVertex = (V) jVertex.toString();
        }

        if (vertexToCell.containsKey(jtVertex)) {
            // We have to remove the new vertex, because it would lead to
            // duplicate vertices. We can't use ShieldedGraph.removeVertex for
            // that, because it would remove the wrong (existing) vertex.
            System.err.println(
                "Warning: detected two JGraph vertices with "
                + "the same JGraphT vertex as user object. It is an "
                + "indication for a faulty situation that should NOT happen."
                + "Removing vertex: " + jVertex);
            internalRemoveCell(jVertex);
        } else {
            jtGraph.addVertex(jtVertex);

            cellToVertex.put(jVertex, jtVertex);
            vertexToCell.put(jtVertex, jVertex);
        }
    }

    /**
     * Removes the edge corresponding to the specified JGraph edge from the
     * JGraphT graph. If the specified edge is not contained in {@link
     * #cellToEdge}, it is silently ignored.
     *
     * <p>This method is to be called only for edges that have already been
     * removed from the JGraph graph.</p>
     *
     * @param jEdge the JGraph edge that has been removed.
     */
    void handleJGraphRemovedEdge(org.jgraph.graph.Edge jEdge)
    {
        if (cellToEdge.containsKey(jEdge)) {
            E jtEdge = cellToEdge.get(jEdge);

            jtGraph.removeEdge(jtEdge);

            cellToEdge.remove(jEdge);
            edgeToCell.remove(jtEdge);
        }
    }

    /**
     * Removes the vertex corresponding to the specified JGraph vertex from the
     * JGraphT graph. If the specified vertex is not contained in {@link
     * #cellToVertex}, it is silently ignored.
     *
     * <p>If any edges are incident with this vertex, we first remove them from
     * the both graphs, because otherwise the JGraph graph would leave them
     * intact and the JGraphT graph would throw them out. TODO: Revise this
     * behavior now that we gracefully tolerate dangling edges. It might be
     * possible to remove just the JGraphT edges. The JGraph edges will be left
     * dangling, as a result.</p>
     *
     * <p>This method is to be called only for vertices that have already been
     * removed from the JGraph graph.</p>
     *
     * @param jVertex the JGraph vertex that has been removed.
     */
    void handleJGraphRemovedVertex(GraphCell jVertex)
    {
        if (cellToVertex.containsKey(jVertex)) {
            V jtVertex = cellToVertex.get(jVertex);
            Set<E> jtIncidentEdges = jtGraph.edgesOf(jtVertex);

            if (!jtIncidentEdges.isEmpty()) {
                // We can't just call removeAllEdges with this list: that
                // would throw a ConcurrentModificationException. So we create
                // a shallow copy.
                // This also triggers removal of the corresponding JGraph
                // edges.
                jtGraph.removeAllEdges(new ArrayList<E>(jtIncidentEdges));
            }

            jtGraph.removeVertex(jtVertex);

            cellToVertex.remove(jVertex);
            vertexToCell.remove(jtVertex);
        }
    }

    /**
     * Adds the specified JGraphT edge to be reflected by this graph model. To
     * be called only for edges that already exist in the JGraphT graph.
     *
     * @param jtEdge a JGraphT edge to be reflected by this graph model.
     */
    void handleJGraphTAddedEdge(E jtEdge)
    {
        DefaultEdge edgeCell = cellFactory.createEdgeCell(jtEdge);
        edgeToCell.put(jtEdge, edgeCell);
        cellToEdge.put(edgeCell, jtEdge);

        ConnectionSet cs = new ConnectionSet();
        cs.connect(
            edgeCell,
            getVertexPort(jtGraph.getEdgeSource(jtEdge)),
            getVertexPort(jtGraph.getEdgeTarget(jtEdge)));

        internalInsertCell(edgeCell, createEdgeAttributeMap(edgeCell), cs);
    }

    /**
     * Adds the specified JGraphT vertex to be reflected by this graph model. To
     * be called only for edges that already exist in the JGraphT graph.
     *
     * @param jtVertex a JGraphT vertex to be reflected by this graph model.
     */
    void handleJGraphTAddedVertex(V jtVertex)
    {
        DefaultGraphCell vertexCell = cellFactory.createVertexCell(jtVertex);
        vertexCell.add(new DefaultPort());

        vertexToCell.put(jtVertex, vertexCell);
        cellToVertex.put(vertexCell, jtVertex);

        internalInsertCell(
            vertexCell,
            createVertexAttributeMap(vertexCell),
            null);
    }

    /**
     * Removes the specified JGraphT vertex from being reflected by this graph
     * model. To be called only for vertices that have already been removed from
     * the JGraphT graph.
     *
     * @param jtVertex a JGraphT vertex to be removed from being reflected by
     * this graph model.
     */
    void handleJGraphTRemoveVertex(Object jtVertex)
    {
        DefaultGraphCell vertexCell =
            (DefaultGraphCell) vertexToCell.remove(jtVertex);
        cellToVertex.remove(vertexCell);

        List<Object> ports = new ArrayList<Object>();

        for (Object child : vertexCell.getChildren()) {
            if (this.isPort(child)) {
                ports.add(child);
            }
        }
        this.remove(ports.toArray());

        internalRemoveCell(vertexCell);
    }

    /**
     * Removes the specified JGraphT edge from being reflected by this graph
     * model. To be called only for edges that have already been removed from
     * the JGraphT graph.
     *
     * @param jtEdge a JGraphT edge to be removed from being reflected by this
     * graph model.
     */
    void handleJGraphTRemovedEdge(E jtEdge)
    {
        DefaultEdge edgeCell = (DefaultEdge) edgeToCell.remove(jtEdge);
        cellToEdge.remove(edgeCell);
        internalRemoveCell(edgeCell);
    }

    /**
     * Tests if the specified JGraph edge is 'dangling', that is having at least
     * one endpoint which is not connected to a vertex.
     *
     * @param jEdge the JGraph edge to be tested for being dangling.
     *
     * @return <code>true</code> if the specified edge is dangling, otherwise
     * <code>false</code>.
     */
    private boolean isDangling(org.jgraph.graph.Edge jEdge)
    {
        Object jSource = getSourceVertex(this, jEdge);
        Object jTarget = getTargetVertex(this, jEdge);

        return !cellToVertex.containsKey(jSource)
            || !cellToVertex.containsKey(jTarget);
    }

    @SuppressWarnings("unchecked")
    private AttributeMap createEdgeAttributeMap(DefaultEdge edgeCell)
    {
        AttributeMap attrs = new AttributeMap();

        // FIXME hb 28-nov-05: waiting for graph to go generic
        attrs.put(edgeCell, getDefaultEdgeAttributes().clone());

        return attrs;
    }

    @SuppressWarnings("unchecked")
    private AttributeMap createVertexAttributeMap(GraphCell vertexCell)
    {
        AttributeMap attrs = new AttributeMap();

        // FIXME hb 28-nov-05: waiting for graph to go generic
        attrs.put(vertexCell, getDefaultVertexAttributes().clone());

        return attrs;
    }

    /**
     * Inserts the specified cell into the JGraph graph model.
     *
     * @param cell
     * @param attrs
     * @param cs
     */
    // FIXME hb 28-nov-05: waiting for graph to go generic
    private void internalInsertCell(
        GraphCell cell,
        AttributeMap attrs,
        ConnectionSet cs)
    {
        jCellsBeingAdded.add(cell);
        insert(new Object[] { cell }, attrs, cs, null, null);
        jCellsBeingAdded.remove(cell);
    }

    /**
     * Removed the specified cell from the JGraph graph model.
     *
     * @param cell
     */
    private void internalRemoveCell(GraphCell cell)
    {
        jCellsBeingRemoved.add(cell);
        remove(new Object[] { cell });
        jCellsBeingRemoved.remove(cell);
    }

    

    /**
     * Creates the JGraph cells that reflect the respective JGraphT elements.
     *
     * @author Barak Naveh
     * @since Dec 12, 2003
     */
    public static interface CellFactory<VV, EE>
    {
        /**
         * Creates an edge cell that contains its respective JGraphT edge.
         *
         * @param jGraphTEdge a JGraphT edge to be contained.
         *
         * @return an edge cell that contains its respective JGraphT edge.
         */
        public DefaultEdge createEdgeCell(EE jGraphTEdge);

        /**
         * Creates a vertex cell that contains its respective JGraphT vertex.
         *
         * @param jGraphTVertex a JGraphT vertex to be contained.
         *
         * @return a vertex cell that contains its respective JGraphT vertex.
         */
        public DefaultGraphCell createVertexCell(VV jGraphTVertex);
    }

    

    /**
     * A simple default cell factory.
     *
     * @author Barak Naveh
     * @since Dec 12, 2003
     */
    public static class DefaultCellFactory<VV, EE>
        implements CellFactory<VV, EE>,
            Serializable
    {
        private static final long serialVersionUID = 3690194343461861173L;

        /**
         * @see JGraphModelAdapter.CellFactory#createEdgeCell(Object)
         */
        public DefaultEdge createEdgeCell(EE jGraphTEdge)
        {
            return new DefaultEdge(jGraphTEdge);
        }

        /**
         * @see JGraphModelAdapter.CellFactory#createVertexCell(Object)
         */
        public DefaultGraphCell createVertexCell(VV jGraphTVertex)
        {
            return new DefaultGraphCell(jGraphTVertex);
        }
    }

    /**
     * <p>Inner class listening to the GraphModel. If something is changed in
     * the GraphModel, this Listener gets notified and propagates the change
     * back to the JGraphT graph, if it didn't originate there.</p>
     *
     * <p>If this change contains changes that would make this an illegal
     * JGraphT graph, like adding an edge that is incident with only one vertex,
     * the illegal parts of the change are undone.</p>
     */
    private class JGraphListener
        implements GraphModelListener,
            Serializable
    {
        private static final long serialVersionUID = 3544673988098865209L;

        /**
         * This method is called for all JGraph changes.
         *
         * @param e
         */
        public void graphChanged(GraphModelEvent e)
        {
            // We first remove edges that have to be removed, then we
            // remove vertices, then we add vertices and finally we add
            // edges. Otherwise, things might go wrong: for example, if we
            // would first remove vertices and then edges, removal of the
            // vertices might induce 'automatic' removal of edges. If we
            // later attempt to re-remove these edges, we get confused.
            GraphModelChange change = e.getChange();

            Object [] removedCells = change.getRemoved();

            if (removedCells != null) {
                handleRemovedEdges(filterEdges(removedCells));
                handleRemovedVertices(filterVertices(removedCells));
            }

            Object [] insertedCells = change.getInserted();

            if (insertedCells != null) {
                handleInsertedVertices(filterVertices(insertedCells));
                handleInsertedEdges(filterEdges(insertedCells));
            }

            // Now handle edges that became 'dangling' or became connected.
            Object [] changedCells = change.getChanged();

            if (changedCells != null) {
                handleChangedEdges(filterEdges(changedCells));
            }
        }

        /**
         * Filters a list of edges out of an array of JGraph GraphCell objects.
         * Other objects are thrown away.
         *
         * @param cells Array of cells to be filtered.
         *
         * @return a list of edges.
         */
        private List<Object> filterEdges(Object [] cells)
        {
            List<Object> jEdges = new ArrayList<Object>();

            for (int i = 0; i < cells.length; i++) {
                if (cells[i] instanceof org.jgraph.graph.Edge) {
                    jEdges.add(cells[i]);
                }
            }

            return jEdges;
        }

        /**
         * Filters a list of vertices out of an array of JGraph GraphCell
         * objects. Other objects are thrown away.
         *
         * @param cells Array of cells to be filtered.
         *
         * @return a list of vertices.
         */
        private List<Object> filterVertices(Object [] cells)
        {
            List<Object> jVertices = new ArrayList<Object>();

            for (int i = 0; i < cells.length; i++) {
                Object cell = cells[i];

                if (cell instanceof org.jgraph.graph.Edge) {
                    // ignore -- we don't care about edges.
                } else if (cell instanceof Port) {
                    // ignore -- we don't care about ports.
                } else if (cell instanceof DefaultGraphCell) {
                    DefaultGraphCell graphCell = (DefaultGraphCell) cell;

                    // If a DefaultGraphCell has a Port as a child, it is a
                    // vertex.
                    // Note: do not change the order of following conditions;
                    // the code uses the short-circuit evaluation of ||.
                    if (graphCell.isLeaf()
                        || (graphCell.getFirstChild() instanceof Port))
                    {
                        jVertices.add(cell);
                    }
                } else if (cell instanceof GraphCell) {
                    // If it is not a DefaultGraphCell, it doesn't have
                    // children.
                    jVertices.add(cell);
                }
            }

            return jVertices;
        }

        private void handleChangedEdges(List<Object> jEdges)
        {
            for (Iterator<Object> i = jEdges.iterator(); i.hasNext();) {
                org.jgraph.graph.Edge jEdge = (org.jgraph.graph.Edge) i.next();

                handleJGraphChangedEdge(jEdge);
            }
        }

        private void handleInsertedEdges(List<Object> jEdges)
        {
            for (Iterator<Object> i = jEdges.iterator(); i.hasNext();) {
                org.jgraph.graph.Edge jEdge = (org.jgraph.graph.Edge) i.next();

                if (!jCellsBeingAdded.remove(jEdge)) {
                    handleJGraphInsertedEdge(jEdge);
                }
            }
        }

        private void handleInsertedVertices(List<Object> jVertices)
        {
            for (Iterator<Object> i = jVertices.iterator(); i.hasNext();) {
                GraphCell jVertex = (GraphCell) i.next();

                if (!jCellsBeingAdded.remove(jVertex)) {
                    handleJGraphInsertedVertex(jVertex);
                }
            }
        }

        private void handleRemovedEdges(List<Object> jEdges)
        {
            for (Iterator<Object> i = jEdges.iterator(); i.hasNext();) {
                org.jgraph.graph.Edge jEdge = (org.jgraph.graph.Edge) i.next();

                if (!jCellsBeingRemoved.remove(jEdge)) {
                    handleJGraphRemovedEdge(jEdge);
                }
            }
        }

        private void handleRemovedVertices(List<Object> jVertices)
        {
            for (Iterator<Object> i = jVertices.iterator(); i.hasNext();) {
                GraphCell jVertex = (GraphCell) i.next();

                if (!jCellsBeingRemoved.remove(jVertex)) {
                    handleJGraphRemovedVertex(jVertex);
                }
            }
        }
    }

    /**
     * A listener on the underlying JGraphT graph. This listener is used to keep
     * the JGraph model in sync. Whenever one of the event handlers is called,
     * it first checks whether the change is due to a previous change in the
     * JGraph model. If it is, then no action is taken.
     *
     * @author Barak Naveh
     * @since Aug 2, 2003
     */
    private class JGraphTListener
        implements GraphListener<V, E>,
            Serializable
    {
        private static final long serialVersionUID = 3616724963609360440L;

        /**
         * @see GraphListener#edgeAdded(GraphEdgeChangeEvent)
         */
        public void edgeAdded(GraphEdgeChangeEvent<V, E> e)
        {
            E jtEdge = e.getEdge();

            if (!jtElementsBeingAdded.remove(jtEdge)) {
                handleJGraphTAddedEdge(jtEdge);
            }
        }

        /**
         * @see GraphListener#edgeRemoved(GraphEdgeChangeEvent)
         */
        public void edgeRemoved(GraphEdgeChangeEvent<V, E> e)
        {
            E jtEdge = e.getEdge();

            if (!jtElementsBeingRemoved.remove(jtEdge)) {
                handleJGraphTRemovedEdge(jtEdge);
            }
        }

        /**
         * @see VertexSetListener#vertexAdded(GraphVertexChangeEvent)
         */
        public void vertexAdded(GraphVertexChangeEvent<V> e)
        {
            V jtVertex = e.getVertex();

            if (!jtElementsBeingAdded.remove(jtVertex)) {
                handleJGraphTAddedVertex(jtVertex);
            }
        }

        /**
         * @see VertexSetListener#vertexRemoved(GraphVertexChangeEvent)
         */
        public void vertexRemoved(GraphVertexChangeEvent<V> e)
        {
            V jtVertex = e.getVertex();

            if (!jtElementsBeingRemoved.remove(jtVertex)) {
                handleJGraphTRemoveVertex(jtVertex);
            }
        }
    }

    /**
     * A wrapper around a JGraphT graph that ensures a few atomic operations.
     */
    private class ShieldedGraph
    {
        private final Graph<V, E> graph;

        ShieldedGraph(Graph<V, E> graph)
        {
            this.graph = graph;
        }

        E addEdge(V jtSource, V jtTarget)
        {
            E jtEdge = graph.getEdgeFactory().createEdge(jtSource, jtTarget);
            jtElementsBeingAdded.add(jtEdge);

            boolean added = graph.addEdge(jtSource, jtTarget, jtEdge);
            jtElementsBeingAdded.remove(jtEdge);

            return added ? jtEdge : null;
        }

        V getEdgeSource(E e)
        {
            return graph.getEdgeSource(e);
        }

        V getEdgeTarget(E e)
        {
            return graph.getEdgeTarget(e);
        }

        void addVertex(V jtVertex)
        {
            jtElementsBeingAdded.add(jtVertex);
            graph.addVertex(jtVertex);
            jtElementsBeingAdded.remove(jtVertex);
        }

        Set<E> edgesOf(V vertex)
        {
            return graph.edgesOf(vertex);
        }

        boolean removeAllEdges(Collection<E> edges)
        {
            return graph.removeAllEdges(edges);
        }

        void removeEdge(E jtEdge)
        {
            jtElementsBeingRemoved.add(jtEdge);
            graph.removeEdge(jtEdge);
            jtElementsBeingRemoved.remove(jtEdge);
        }

        void removeVertex(V jtVertex)
        {
            jtElementsBeingRemoved.add(jtVertex);
            graph.removeVertex(jtVertex);
            jtElementsBeingRemoved.remove(jtVertex);
        }
    }
}

// End JGraphModelAdapter.java
