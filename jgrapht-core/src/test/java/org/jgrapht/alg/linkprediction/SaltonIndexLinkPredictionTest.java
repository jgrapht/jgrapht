/*
 * (C) Copyright 2020-2021, by Dimitrios Michail and Contributors.
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
import org.jgrapht.TestUtil;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

/**
 * Tests for {@link SaltonIndexLinkPrediction}
 *
 * @author Dimitrios Michail
 */
public class SaltonIndexLinkPredictionTest
{

    @Test
    public void testPrediction()
    {
        Graph<Integer,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().weighted(false).vertexSupplier(SupplierUtil.createIntegerSupplier())
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER).buildGraph();

        TestUtil
            .constructGraph(
                g, new int[][] { { 0, 1 }, { 0, 3 }, { 1, 2 }, { 1, 4 }, { 2, 3 }, { 2, 4 },
                    { 3, 4 }, { 3, 5 }, { 4, 5 } });

        SaltonIndexLinkPrediction<Integer, DefaultEdge> alg = new SaltonIndexLinkPrediction<>(g);

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

        double[] expected = { 0.8164965809277261, 0.7071067811865475, 0.5, 0.8660254037844387,
            0.4082482904638631, 0.8164965809277261 };

        assertArrayEquals(expected, scores, 1e-9);

    }

    @Test(expected = LinkPredictionIndexNotWellDefinedException.class)
    public void testInvalidPrediction()
    {
        Graph<Integer,
            DefaultEdge> g = GraphTypeBuilder
                .directed().weighted(false).vertexSupplier(SupplierUtil.createIntegerSupplier())
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER).buildGraph();

        g.addVertex(0);
        g.addVertex(1);

        SaltonIndexLinkPrediction<Integer, DefaultEdge> alg = new SaltonIndexLinkPrediction<>(g);

        alg.predict(0, 1);
    }

}
