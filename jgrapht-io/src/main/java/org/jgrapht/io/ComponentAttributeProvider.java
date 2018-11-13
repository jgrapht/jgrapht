/*
 * (C) Copyright 2010-2017, by John Sichi and Contributors.
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
 * Provides display attributes for vertices and/or edges in a graph.
 *
 * @param <T> the type for which attributes are provided for
 *
 * @author John Sichi
 */
public interface ComponentAttributeProvider<T>
{
    /**
     * Returns a set of attribute key/value pairs for a vertex or edge. If order is important in the
     * output, be sure to use an order-deterministic map implementation.
     *
     * @param component vertex or edge for which attributes are to be obtained
     *
     * @return key/value pairs, or null if no attributes should be supplied
     */
    public Map<String, Attribute> getComponentAttributes(T component);
}
