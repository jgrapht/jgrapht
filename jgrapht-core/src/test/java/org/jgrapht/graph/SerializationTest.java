/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* --------------
 * SerializationTest.java
 * --------------
 * (C) Copyright 2003-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 06-Oct-2003 : Initial revision (JVS);
 *
 */
package org.jgrapht.graph;

import java.io.*;

import org.jgrapht.*;


/**
 * SerializationTest tests serialization and deserialization of JGraphT objects.
 *
 * @author John V. Sichi
 */
public class SerializationTest
    extends EnhancedTestCase
{
    //~ Instance fields --------------------------------------------------------

    private String v1 = "v1";
    private String v2 = "v2";
    private String v3 = "v3";

    //~ Constructors -----------------------------------------------------------

    /**
     * @see junit.framework.TestCase#TestCase(java.lang.String)
     */
    public SerializationTest(String name)
    {
        super(name);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Tests serialization of DirectedMultigraph.
     */
    @SuppressWarnings("unchecked")
    public void testDirectedMultigraph()
        throws Exception
    {
        DirectedMultigraph<String, DefaultEdge> graph =
            new DirectedMultigraph<String, DefaultEdge>(
                DefaultEdge.class);
        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addVertex(v3);
        graph.addEdge(v1, v2);
        graph.addEdge(v2, v3);
        graph.addEdge(v2, v3);

        graph =
            (DirectedMultigraph<String, DefaultEdge>) serializeAndDeserialize(
                graph);
        assertTrue(graph.containsVertex(v1));
        assertTrue(graph.containsVertex(v2));
        assertTrue(graph.containsVertex(v3));
        assertTrue(graph.containsEdge(v1, v2));
        assertTrue(graph.containsEdge(v2, v3));
        assertEquals(1, graph.edgesOf(v1).size());
        assertEquals(3, graph.edgesOf(v2).size());
        assertEquals(2, graph.edgesOf(v3).size());
    }

    private Object serializeAndDeserialize(Object obj)
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
}

// End SerializationTest.java
