/*
 * (C) Copyright 2003-2018, by John V Sichi and Contributors.
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

import org.jgrapht.*;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Serialization test utils for the serialization and deserialization of JGraphT objects.
 *
 * @author John V. Sichi
 */
public class SerializationTestUtils
{
    private static final String v1 = "v1";
    private static final String v2 = "v2";
    private static final String v3 = "v3";
    private static final List<String> vertexList = Arrays.asList(v1, v2, v3);

    public static String getV1()
    {
        return v1;
    }

    public static String getV2()
    {
        return v2;
    }

    public static String getV3()
    {
        return v3;
    }

    public static List<String> getVertexList()
    {
        return vertexList;
    }

    public static Object serializeAndDeserialize(Object obj)
        throws Exception
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);

        out.writeObject(obj);
        out.flush();

        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bin);

        obj = in.readObject();
        return obj;
    }

    public static <V, E> void assertContainsAllVertices(Graph<V, E> graph, List<V> vertices)
    {
        for (V v : vertices) {
            assertTrue(graph.containsVertex(v));
        }
    }

    public static <V, E> void checkEdgesOf(Graph<V, E> graph, List<Integer> edges, List<V> vertices)
    {
        if (edges.size() != vertices.size()) {
            throw new IllegalArgumentException(
                "the size of list of #edges and vertices should match");
        }
        for (int i = 0; i < edges.size(); i++) {
            assertEquals(edges.get(i).intValue(), graph.edgesOf(vertices.get(i)).size());
        }
    }
}
