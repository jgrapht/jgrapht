/*
 * (C) Copyright 2025-2025, by Nicolas Rol and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.nio.dot;

import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.nio.Attribute;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A DOTSubgraph is a container class for a subgraph and its attributes. It may be used in DOT export and can be
 * parametrized to export or not vertices and/or edges.
 *
 * <p>
 *     For example, for a subgraph named "subg" with DefaultEdges (1, 2) and (2, 3) between Integer vertices 1, 2, and 3,
 *     with the subgraph attribute {@code pencolor} set to {@code transparent}, and the cluster attribute {@code shape}
 *     set to {@code point}, the DOT export of the subgraph will be:
 * </p>
 * <pre>
 *     subgraph subg {
 *         subg [ shape="point" ]
 *         pencolor=transparent;
 *         1;
 *         2;
 *         3;
 *         1 -- 2;
 *         2 -- 3;
 *     }
 * </pre>
 * @param <V> the vertex type
 * @param <E> the edge type
 *
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 * @see AsSubgraph
 * @see DOTExporter
 */
public class DOTSubgraph<V, E> {

    private final AsSubgraph<V, E> subgraph;
    private final Map<String, Attribute> subgraphAttributes;
    private final Map<String, Attribute> clusterAttributes;
    private final boolean exportVertices;
    private final boolean exportEdges;

    /**
     * Constructs a new DOTSubgraph with specified subgraph, attributes, and export options.
     *
     * @param subgraph the subgraph to be exported
     * @param subgraphAttributes attributes for the subgraph
     * @param clusterAttributes attributes for the cluster
     * @param exportVertices whether to export vertices
     * @param exportEdges whether to export edges
     */
    public DOTSubgraph(AsSubgraph<V, E> subgraph, Map<String, Attribute> subgraphAttributes, Map<String, Attribute> clusterAttributes,
                       boolean exportVertices, boolean exportEdges) {
        this.subgraph = subgraph;
        this.subgraphAttributes = new LinkedHashMap<>(subgraphAttributes);
        this.clusterAttributes = new LinkedHashMap<>(clusterAttributes);
        this.exportVertices = exportVertices;
        this.exportEdges = exportEdges;
    }

    /**
     * Constructs a new DOTSubgraph with specified subgraph, attributes.
     *
     * @param subgraph the subgraph to be exported
     * @param subgraphAttributes attributes for the subgraph
     * @param clusterAttributes attributes for the cluster
     */
    public DOTSubgraph(AsSubgraph<V, E> subgraph, Map<String, Attribute> subgraphAttributes, Map<String, Attribute> clusterAttributes) {
        this(subgraph, subgraphAttributes, clusterAttributes, true, true);
    }

    /**
     * Get the subgraph.
     *
     * @return the subgraph
     */
    public AsSubgraph<V, E> getSubgraph() {
        return subgraph;
    }

    /**
     * Get the subgraph attributes in a preserved order.
     *
     * @return the subgraph attributes.
     */
    public Map<String, Attribute> getSubgraphAttributes() {
        return subgraphAttributes;
    }

    /**
     * Get the cluster attributes in a preserved order.
     *
     * @return the cluster attributes.
     */
    public Map<String, Attribute> getClusterAttributes() {
        return clusterAttributes;
    }

    /**
     * Get the vertices of the subgraph.
     *
     * @return the vertices of the subgraph
     */
    public Set<V> vertexSet() {
        return subgraph.vertexSet();
    }

    /**
     * Get the edges of the subgraph.
     *
     * @return the edges of the subgraph
     */
    public Set<E> edgeSet() {
        return subgraph.edgeSet();
    }

    /**
     * Whether to export the subgraph vertices in the DOT export.
     * <p>
     *     If {@code true}, vertices will be included in the DOT export by writing their identifiers one by one on successive
     *     lines, with the same formalism as the main graph's vertices.
     * </p>
     *
     * <p>
     *     For example, for a subgraph with DefaultEdges (1, 2) and (2, 3) between Integer vertices 1, 2, and 3, the
     *     DOT export of the vertices will be:
     *     <pre>
     *         1;
     *         2;
     *         3;
     *     </pre>
     * </p>
     *
     * @return {@code true} if vertices should be exported, {@code false} otherwise.
     */
    public boolean isExportVertices() {
        return exportVertices;
    }

    /**
     * Whether to export the subgraph edges in the DOT export.
     * <p>
     *     If {@code true}, edges will be included in the DOT export by writing the identifiers of their respective source
     *     and target vertices on successive lines, with the same formalism as the main graph's edges.
     * </p>
     *
     * <p>
     *     For example, for a subgraph with DefaultEdges (1, 2) and (2, 3) between Integer vertices 1, 2, and 3, the
     *     DOT export of the edges will be:
     *     <pre>
     *         1 -- 2;
     *         2 -- 3;
     *     </pre>
     * </p>
     * @return {@code true} if edges should be exported, {@code false} otherwise.
     */
    public boolean isExportEdges() {
        return exportEdges;
    }
}
