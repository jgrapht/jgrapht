/*
 * (C) Copyright 2020-2020, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.linkprediction;

import static org.junit.Assert.assertArrayEquals;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

/**
 * Tests
 *
 * @author Dimitrios Michail
 */
public class AdamicAdarIndexLinkPredictionTest
{

    @Test
    public void testPrediction()
    {
        Graph<Integer,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().weighted(false).vertexSupplier(SupplierUtil.createIntegerSupplier())
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER).buildGraph();

        for (int i = 0; i < 6; i++)
            g.addVertex(i);

        g.addEdge(0, 1);
        g.addEdge(0, 3);
        g.addEdge(1, 2);
        g.addEdge(1, 4);
        g.addEdge(2, 3);
        g.addEdge(2, 4);
        g.addEdge(3, 4);
        g.addEdge(3, 5);
        g.addEdge(4, 5);

        AdamicAdarIndexLinkPrediction<Integer, DefaultEdge> alg =
            new AdamicAdarIndexLinkPrediction<>(g);

        double[] scores = new double[6];

        int pos = 0;
        for (int u : g.vertexSet()) {
            for (int v : g.vertexSet()) {
                if (u >= v || g.containsEdge(u, v)) {
                    continue;
                }
                double score = alg.predict(u, v);
                scores[pos++] = score;
            }
        }

        double[] expected = { 1.631586747071319, 1.631586747071319, 0.7213475204444817,
            3.074281787960283, 0.7213475204444817, 1.4426950408889634 };

        assertArrayEquals(expected, scores, 1e-9);

        System.out.print("{ ");
        for (int i = 0; i < pos; i++) {
            System.out.print(scores[i]);
            if (i == pos - 1) {
                System.out.println("}");
            } else {
                System.out.print(",");
            }
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPrediction()
    {
        Graph<Integer,
            DefaultEdge> g = GraphTypeBuilder
                .directed().weighted(false).vertexSupplier(SupplierUtil.createIntegerSupplier())
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER).buildGraph();

        g.addVertex(0);
        g.addVertex(1);
        g.addVertex(2);

        g.addEdge(0, 2);
        g.addEdge(1, 2);

        AdamicAdarIndexLinkPrediction<Integer, DefaultEdge> alg =
            new AdamicAdarIndexLinkPrediction<>(g);

        alg.predict(0, 1);
    }

}
