/*
 * (C) Copyright 2010-2017, by John Sichi and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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

// End ComponentAttributeProvider.java
