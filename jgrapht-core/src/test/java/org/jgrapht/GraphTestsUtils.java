/*
 * (C) Copyright 2018-2021, by Dimitrios Michail and Contributors.
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
