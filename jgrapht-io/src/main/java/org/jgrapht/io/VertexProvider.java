/*
 * (C) Copyright 2015-2017, by Wil Selwood and Contributors.
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
package org.jgrapht.io;

import java.util.*;

/**
 * Creates a vertex.
 *
 * @param <V> the vertex type
 */
public interface VertexProvider<V>
{
    /**
     * Create a vertex
     *
     * @param id a unique identifier for the vertex
     * @param attributes any other attributes of the vertex
     * @return the vertex
     */
    V buildVertex(String id, Map<String, Attribute> attributes);
}
