/*
 * (C) Copyright 2021-2021, by Hannes Wellmann and Contributors.
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
package org.jgrapht.graph;

/**
 * Special {@link RuntimeException} to signal that {@link IntrusiveEdge} is used incorrectly.
 * 
 * @author Hannes Wellmann
 */
public class IntrusiveEdgeException
    extends
    RuntimeException
{
    private static final long serialVersionUID = 7261763645809925025L;

    public <V> IntrusiveEdgeException(V source, V target)
    {
        super("Edge already associated with source <" + source + "> and target <" + target + ">");
    }
}
