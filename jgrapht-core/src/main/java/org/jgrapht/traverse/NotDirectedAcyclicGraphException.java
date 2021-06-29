/*
 * (C) Copyright 2021-2021, by Kaiichiro Ota and Contributors.
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
package org.jgrapht.traverse;

/**
 * An exception to signal that {@link TopologicalOrderIterator} is used for a non-directed acyclic graph.
 * Note that this class extends {@link IllegalArgumentException} for backward compatibility
 *
 * @author Kaiichiro Ota
 */
public class NotDirectedAcyclicGraphException
    extends IllegalArgumentException
{
    private static final String GRAPH_IS_NOT_A_DAG = "Graph is not a DAG";

    private static final long serialVersionUID = 1L;

    public NotDirectedAcyclicGraphException()
    {
        super(GRAPH_IS_NOT_A_DAG);
    }
}
