/*
 * (C) Copyright 2006-2023, by Nicolas Rol and Contributors.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class DOTSubgraph<V, E> {

    private final AsSubgraph<V, E> subgraph;
    private final Map<String, Attribute> subgraphAttributes;
    private final Map<String, Attribute> clusterAttributes;
    private final boolean exportVertices;
    private final boolean exportEdges;

    public DOTSubgraph(AsSubgraph<V, E> subgraph, Map<String, Attribute> subgraphAttributes, Map<String, Attribute> clusterAttributes,
                       boolean exportVertices, boolean exportEdges) {
        this.subgraph = subgraph;
        this.subgraphAttributes = new HashMap<>(subgraphAttributes);
        this.clusterAttributes = new HashMap<>(clusterAttributes);
        this.exportVertices = exportVertices;
        this.exportEdges = exportEdges;
    }

    public DOTSubgraph(AsSubgraph<V, E> subgraph, Map<String, Attribute> subgraphAttributes, Map<String, Attribute> clusterAttributes) {
        this(subgraph, subgraphAttributes, clusterAttributes, true, true);
    }

    public AsSubgraph<V, E> getSubgraph() {
        return subgraph;
    }

    public Map<String, Attribute> getSubgraphAttributes() {
        return subgraphAttributes;
    }

    public Map<String, Attribute> getClusterAttributes() {
        return clusterAttributes;
    }

    public Set<V> vertexSet() {
        return subgraph.vertexSet();
    }

    public Set<E> edgeSet() {
        return subgraph.edgeSet();
    }

    public boolean isExportVertices() {
        return exportVertices;
    }

    public boolean isExportEdges() {
        return exportEdges;
    }
}
