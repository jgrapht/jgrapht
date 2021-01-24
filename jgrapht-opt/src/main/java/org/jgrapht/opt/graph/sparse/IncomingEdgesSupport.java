/*
 * (C) Copyright 2021-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.opt.graph.sparse;

/**
 * Enumeration for different kind of incoming edges support. Several algorithms do not require the
 * use of incoming edges. In such cases either no support or lazy support of incoming edges may save
 * considerable amount of space.
 * 
 * @author Dimitrios Michail
 */
public enum IncomingEdgesSupport
{
    /**
     * No support for incoming edges
     */
    NO_INCOMING_EDGES,
    /**
     * Support incoming edges only if explicitly requested by the user using a method call which
     * requires incoming edges.
     */
    LAZY_INCOMING_EDGES,
    /**
     * Support incoming edges.
     */
    FULL_INCOMING_EDGES,
}
