/*
 * (C) Copyright 2018-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht;

import org.jgrapht.graph.*;
import org.jgrapht.util.*;

/**
 * Helper methods for graph creation on all tests.
 * 
 * @author Dimitrios Michail
 */
public class GraphTestsUtils
{

    /**
     * Create a simple graph with integer vertices and default edges.
     * 
     * @return a simple graph with integer vertices and default edges.
     */
    public static Graph<Integer, DefaultEdge> createSimpleGraph()
    {
        return new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
    }

    /**
     * Create a pseudo graph with integer vertices and default edges.
     * 
     * @return a pseudo graph with integer vertices and default edges
     */
    public static Graph<Integer, DefaultEdge> createPseudograph()
    {
        return new Pseudograph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
    }

}
