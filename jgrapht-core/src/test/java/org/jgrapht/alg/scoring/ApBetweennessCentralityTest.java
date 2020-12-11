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
package org.jgrapht.alg.scoring;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.apfloat.Apfloat;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.junit.Test;

public class ApBetweennessCentralityTest
{

    @Test
    public void testNoOverflow()
    {
        final Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        for (int i = 0; i < 3300; i++)
            g.addVertex(i);
        for (int i = 0; i < 3290; i++)
            for (int j = 0; j < 10; j++)
                g.addEdge(i, i - i % 10 + 10 + j);
        VertexScoringAlgorithm<Integer, Apfloat> bc =
            new ApBetweennessCentrality<>(g);
        Map<Integer, Apfloat> scores = bc.getScores();
        
        assertEquals(scores.get(9), new Apfloat(0));
        assertEquals(scores.get(10), new Apfloat("3.28e3"));
        assertEquals(scores.get(3289), new Apfloat("3.28e3"));
        assertEquals(scores.get(3290), new Apfloat("0"));
    }

}
