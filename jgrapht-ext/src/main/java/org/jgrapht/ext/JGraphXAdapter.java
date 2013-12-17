/* This program and the accompanying materials are dual-licensed under
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
package org.jgrapht.ext;

import com.mxgraph.model.*;
import com.mxgraph.view.*;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.event.*;


/**
 * <P>Adapter to draw a JGraphT graph with the JGraphX drawing library.</P>
 *
 * <P>This adapter will not convert JGraphX to JGraphT - this should be handled
 * in another class entirely.</P>
 *
 * <P>Known Bugs: If this class is used with String-Edges, please note that
 * there is a bug with the method JgraphT.addEdge(vertex1, vertex2); The edge
 * will be created with an empty String "" as value and saved (in JGraphT as
 * well as in this class), which results in the edge not saving correctly. So,
 * if you're using Strings as Edgeclass please use the method addEdge(vertex1,
 * vertex2, "Edgename"); with a unique edgename.</P>
 *
 * @param <V> Vertex
 * @param <E> Edge
 *
 * @author Original: JeanYves Tinevez
 * @since 09 July, 2013
 */
public class JGraphXAdapter<V, E>
    extends mxGraph
    implements GraphListener<V, E>
{
    

    /**
     * The graph to be drawn. Has vertices "V" and edges "E".
     */
    private Graph<V, E> graphT;

    /**
     * Maps the JGraphT-Vertices onto JGraphX-mxICells. {@link #cellToVertexMap}
     * is for the opposite direction.
     */
    private HashMap<V, mxICell> vertexToCellMap = new HashMap<V, mxICell>();

    /**
     * Maps the JGraphT-Edges onto JGraphX-mxICells. {@link #cellToEdgeMap} is
     * for the opposite direction.
     */
    private HashMap<E, mxICell> edgeToCellMap = new HashMap<E, mxICell>();

    /**
     * Maps the JGraphX-mxICells onto JGraphT-Edges. {@link #edgeToCellMap} is
     * for the opposite direction.
     */
    private HashMap<mxICell, V> cellToVertexMap = new HashMap<mxICell, V>();

    /**
     * Maps the JGraphX-mxICells onto JGraphT-Vertices. {@link #vertexToCellMap}
     * is for the opposite direction.
     */
    private HashMap<mxICell, E> cellToEdgeMap = new HashMap<mxICell, E>();

    

    /**
     * Constructs and draws a new ListenableGraph. If the graph changes through
     * as ListenableGraph, the JGraphXAdapter will automatically add/remove the
     * new edge/vertex as it implements the GraphListener interface. Throws a
     * IllegalArgumentException if the graph is null.
     *
     * @param graph casted to graph
     */
    public JGraphXAdapter(ListenableGraph<V, E> graph)
    {
        // call normal constructor with graph class
        this((Graph<V, E>) graph);

        graph.addGraphListener(this);
    }

    /**
     * Constructs and draws a new mxGraph from a jGraphT graph. Changes on the
     * jgraphT graph will not edit this mxGraph any further; use the constructor
     * with the ListenableGraph parameter instead or use this graph as a normal
     * mxGraph. Throws an IllegalArgumentException if the parameter is null.
     *
     * @param graph is a graph
     */
    public JGraphXAdapter(Graph<V, E> graph)
    {
        super();

        // Don't accept null as jgrapht graph
        if (graph == null) {
            throw new IllegalArgumentException();
        } else {
            this.graphT = graph;
        }

        // generate the drawing
        insertJGraphT(graph);

        setAutoSizeCells(true);
    }

    

    /**
     * Returns Hashmap which maps the vertices onto their visualization
     * mxICells.
     *
     * @return {@link #vertexToCellMap}
     */
    public HashMap<V, mxICell> getVertexToCellMap()
    {
        return vertexToCellMap;
    }

    /**
     * Returns Hashmap which maps the edges onto their visualization mxICells.
     *
     * @return {@link #edgeToCellMap}
     */
    public HashMap<E, mxICell> getEdgeToCellMap()
    {
        return edgeToCellMap;
    }

    /**
     * Returns Hashmap which maps the visualization mxICells onto their edges.
     *
     * @return {@link #cellToEdgeMap}
     */
    public HashMap<mxICell, E> getCellToEdgeMap()
    {
        return cellToEdgeMap;
    }

    /**
     * Returns Hashmap which maps the visualization mxICells onto their
     * vertices.
     *
     * @return {@link #cellToVertexMap}
     */
    public HashMap<mxICell, V> getCellToVertexMap()
    {
        return cellToVertexMap;
    }

    @Override public void vertexAdded(GraphVertexChangeEvent<V> e)
    {
        addJGraphTVertex(e.getVertex());
    }

    @Override public void vertexRemoved(GraphVertexChangeEvent<V> e)
    {
        mxICell cell = vertexToCellMap.remove(e.getVertex());
        removeCells(new Object[] { cell });

        // remove vertex from hashmaps
        cellToVertexMap.remove(cell);
        vertexToCellMap.remove(e.getVertex());

        // remove all edges that connected to the vertex
        ArrayList<E> removedEdges = new ArrayList<E>();

        // first, generate a list of all edges that have to be deleted
        // so we don't change the cellToEdgeMap.values by deleting while
        // iterating
        // we have to iterate over this because the graphT has already
        // deleted the vertex and edges so we can't query what the edges were
        for (E edge : cellToEdgeMap.values()) {
            if (!graphT.edgeSet().contains(edge)) {
                removedEdges.add(edge);
            }
        }

        // then delete all entries of the previously generated list
        for (E edge : removedEdges) {
            removeEdge(edge);
        }
    }

    @Override public void edgeAdded(GraphEdgeChangeEvent<V, E> e)
    {
        addJGraphTEdge(e.getEdge());
    }

    @Override public void edgeRemoved(GraphEdgeChangeEvent<V, E> e)
    {
        removeEdge(e.getEdge());
    }

    /**
     * Removes a jgrapht edge and its visual representation from this graph
     * completely.
     *
     * @param edge The edge that will be removed
     */
    private void removeEdge(E edge)
    {
        mxICell cell = edgeToCellMap.remove(edge);
        removeCells(new Object[] { cell });

        // remove edge from hashmaps
        cellToEdgeMap.remove(cell);
        edgeToCellMap.remove(edge);
    }

    /**
     * Draws a new vertex into the graph.
     *
     * @param vertex vertex to be added to the graph
     */
    private void addJGraphTVertex(V vertex)
    {
        getModel().beginUpdate();

        try {
            // create a new JGraphX vertex at position 0
            mxICell cell =
                (mxICell) insertVertex(defaultParent, null, vertex,
                    0, 0, 0, 0);

            // update cell size so cell isn't "above" graph
            updateCellSize(cell);

            // Save reference between vertex and cell
            vertexToCellMap.put(vertex, cell);
            cellToVertexMap.put(cell, vertex);
        } finally {
            getModel().endUpdate();
        }
    }

    /**
     * Draws a new egde into the graph.
     *
     * @param edge edge to be added to the graph. Source and target vertices are
     * needed.
     */
    private void addJGraphTEdge(E edge)
    {
        getModel().beginUpdate();

        try {
            // find vertices of edge
            V sourceVertex = graphT.getEdgeSource(edge);
            V targetVertex = graphT.getEdgeTarget(edge);

            // if the one of the vertices is not drawn, don't draw the edge
            if (!(vertexToCellMap.containsKey(sourceVertex)
                    && vertexToCellMap.containsKey(targetVertex)))
            {
                return;
            }

            // get mxICells
            Object sourceCell = vertexToCellMap.get(sourceVertex);
            Object targetCell = vertexToCellMap.get(targetVertex);

            // add edge between mxICells
            mxICell cell =
                (mxICell) insertEdge(
                    defaultParent,
                    null,
                    edge,
                    sourceCell,
                    targetCell);

            // update cell size so cell isn't "above" graph
            updateCellSize(cell);

            // Save reference between vertex and cell
            edgeToCellMap.put(edge, cell);
            cellToEdgeMap.put(cell, edge);
        } finally {
            getModel().endUpdate();
        }
    }

    /**
     * Draws a given graph with all its vertices and edges.
     *
     * @param graph the graph to be added to the existing graph.
     */
    private void insertJGraphT(Graph<V, E> graph)
    {
        for (V vertex : graph.vertexSet()) {
            addJGraphTVertex(vertex);
        }

        for (E edge : graph.edgeSet()) {
            addJGraphTEdge(edge);
        }
    }
}

//End JGraphXAdapter.java
