/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
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
import java.lang.String;

import junit.framework.TestCase;
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

    private final String v1 = "v1";
    private final String v2 = "v2";
    private final String v3 = "v3";

    //~ Constructors -----------------------------------------------------------

    /**
     * @see TestCase#TestCase(String)
     */
    public SerializationTest(final String name)
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
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(bout);

        out.writeObject(obj);
        out.flush();

        final ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        final ObjectInputStream in = new ObjectInputStream(bin);

        obj = in.readObject();
        return obj;
    }
}

// End SerializationTest.java
