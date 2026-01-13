/*
 * (C) Copyright 2026, by Jose Alejandro Cornejo-Acosta and Contributors.
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
package org.jgrapht.alg.interfaces;

import java.util.Set;

import org.jgrapht.Graph;

/**
 * An algorithm for solving the <a href="https://encyclopedia.pub/entry/52164"> vertex $k$-center problem</a>.
 * 
 * <p>
 * Center location problems are a class of optimization problems in graph theory and operations research that aims
 * selecting optimal locations for facilities (centers) to minimize the distance or cost for serving a set of
 * demand points or clients (vertices) in a graph.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Jose Alejandro Cornejo-Acosta
 */
public interface CentersLocationAlgorithm<V, E>
{

    /**
     * Computes a set of centers.
     *
     * @param graph the input graph.
     * @param k the number of centers to locate.
     * @return a set of centes.
     */
    Set<V> getCenters(Graph<V, E> graph, int k);

}
