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
/* ------------------
 * DOTImporterTest.java
 * ------------------
 * (C) Copyright 2015-2016, by  Wil Selwood and Contributors.
 *
 * Original Author:  Wil Selwood <wselwood@ijento.com>
 * Contributor(s): Dimitrios Michail
 *
 * Changes
 * -------
 * 2015 : Initial revision (WS);
 * 19-Aug-2016: Always provide id to vertex providers (DM);
 * 
 */
package org.jgrapht.ext;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import junit.framework.*;

import org.jgrapht.Graph;
import org.jgrapht.graph.*;

public class DOTImporterTest extends TestCase
{

   public void testUndirectedWithLabels() throws ImportException {
      String input = "graph G {\n"
                     + "  1 [ \"label\"=\"abc123\" ];\n"
                     + "  2 [ label=\"fred\" ];\n"
                     + "  1 -- 2;\n"
                     + "}";

      Multigraph<String, DefaultEdge> expected
            = new Multigraph<String, DefaultEdge>(DefaultEdge.class);
      expected.addVertex("1");
      expected.addVertex("2");
      expected.addEdge("1", "2");


      DOTImporter<String, DefaultEdge> importer = buildImporter();

      Multigraph<String, DefaultEdge> result
            = new Multigraph<String, DefaultEdge>(DefaultEdge.class);
      importer.importGraph(result, new StringReader(input));

      Assert.assertEquals(expected.toString(), result.toString());

      Assert.assertEquals(2, result.vertexSet().size());
      Assert.assertEquals(1, result.edgeSet().size());

   }

   public void testDirectedNoLabels() throws ImportException {
      String input = "digraph graphname {\r\n"
                     + "     a -> b -> c;\r\n"
                     + "     b -> d;\r\n"
                     + " }";

      DirectedMultigraph<String, DefaultEdge> expected
            = new DirectedMultigraph<String, DefaultEdge>(DefaultEdge.class);
      expected.addVertex("a");
      expected.addVertex("b");
      expected.addVertex("c");
      expected.addVertex("d");
      expected.addEdge("a", "b");
      expected.addEdge("b", "c");
      expected.addEdge("b", "d");


      DOTImporter<String, DefaultEdge> importer = buildImporter();

      DirectedMultigraph<String, DefaultEdge> result
            = new DirectedMultigraph<String, DefaultEdge>(DefaultEdge.class);
      importer.importGraph(result, new StringReader(input));

      Assert.assertEquals(expected.toString(), result.toString());

      Assert.assertEquals(4, result.vertexSet().size());
      Assert.assertEquals(3, result.edgeSet().size());

   }
   
   public void testDirectedSameLabels() throws ImportException {
       String input =   "digraph sample {\n"
                      + "  a -> b;"
                      + "  b -> c;\n"
                      + "  a [ label=\"Test\"];\n"
                      + "  b [ label=\"Test\"];\n"
                      + "  c [ label=\"Test\"];\n"
                      + "}";

       DirectedMultigraph<String, DefaultEdge> expected
             = new DirectedMultigraph<String, DefaultEdge>(DefaultEdge.class);
       expected.addVertex("a");
       expected.addVertex("b");
       expected.addVertex("c");
       expected.addEdge("a", "b");
       expected.addEdge("b", "c");

        DOTImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>(
            new VertexProvider<String>()
            {
                @Override
                public String buildVertex(
                    String label,
                    Map<String, String> attributes)
                {
                    return label;
                }
            },
            new EdgeProvider<String, DefaultEdge>()
            {
                @Override
                public DefaultEdge buildEdge(
                    String from,
                    String to,
                    String label,
                    Map<String, String> attributes)
                {
                    return new DefaultEdge();
                }
            },
            new VertexUpdater<String>()
            {
                @Override
                public void updateVertex(
                    String vertex,
                    Map<String, String> attributes)
                {
                }
            });

       DirectedMultigraph<String, DefaultEdge> result
             = new DirectedMultigraph<String, DefaultEdge>(DefaultEdge.class);
       importer.importGraph(result, new StringReader(input));

       Assert.assertEquals(expected.toString(), result.toString());
    }

   public void testMultiLinksUndirected() throws ImportException {
      String input = "graph G {\n"
                     + "  1 [ label=\"bob\" ];\n"
                     + "  2 [ label=\"fred\" ];\n"
              // the extra label will be ignored but not cause any problems.
                     + "  1 -- 2 [ label=\"friend\"];\n"
                     + "  1 -- 2;\n"
                     + "}";

      Multigraph<String, DefaultEdge> expected
            = new Multigraph<String, DefaultEdge>(DefaultEdge.class);
      expected.addVertex("1");
      expected.addVertex("2");
      expected.addEdge("1", "2", new DefaultEdge());
      expected.addEdge("1", "2", new DefaultEdge());


      DOTImporter<String, DefaultEdge> importer = buildImporter();

      Multigraph<String, DefaultEdge> result
            = new Multigraph<String, DefaultEdge>(DefaultEdge.class);
      importer.importGraph(result, new StringReader(input));

      Assert.assertEquals(expected.toString(), result.toString());

      Assert.assertEquals(2, result.vertexSet().size());
      Assert.assertEquals(2, result.edgeSet().size());
   }

   public void testExportImportLoop() 
       throws ImportException, ExportException, UnsupportedEncodingException {
      DirectedMultigraph<String, DefaultEdge> start
            = new DirectedMultigraph<String, DefaultEdge>(DefaultEdge.class);
      start.addVertex("a");
      start.addVertex("b");
      start.addVertex("c");
      start.addVertex("d");
      start.addEdge("a", "b");
      start.addEdge("b", "c");
      start.addEdge("b", "d");

      DOTExporter<String, DefaultEdge> exporter
            = new DOTExporter<String, DefaultEdge>(new VertexNameProvider<String>() {
         @Override
         public String getVertexName(String vertex) {
            return vertex;
         }
      }, null, new IntegerEdgeNameProvider<DefaultEdge>());

      DOTImporter<String, DefaultEdge> importer = buildImporter();

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      exporter.exportGraph(start, os);
      String output = new String(os.toByteArray(), "UTF-8");

      DirectedMultigraph<String, DefaultEdge> result
            = new DirectedMultigraph<String, DefaultEdge>(DefaultEdge.class);

      importer.importGraph(result, new StringReader(output));

      Assert.assertEquals(start.toString(), result.toString());

      Assert.assertEquals(4, result.vertexSet().size());
      Assert.assertEquals(3, result.edgeSet().size());


   }

   public void testDashLabelVertex() throws ImportException {
      String input = "graph G {\n"
                     + "a [label=\"------this------contains-------dashes------\"]\n"
                     + "}";

      Multigraph<String, DefaultEdge> result = new Multigraph<String, DefaultEdge>(DefaultEdge.class);

      DOTImporter<String, DefaultEdge> importer = new DOTImporter<String, DefaultEdge>(
            new VertexProvider<String>() {
                @Override
                public String buildVertex(
                    String label,
                    Map<String, String> attributes)
                {
                    if (label.equals("a")) {
                        Assert.assertTrue(
                            attributes.get("label").equals(
                                "------this------contains-------dashes------"));
                    }
                    return label;
                }
            },
            new EdgeProvider<String, DefaultEdge>() {
               @Override
               public DefaultEdge buildEdge(String from, String to, String label,
                                            Map<String, String> attributes) {
                  return new DefaultEdge();
               }
            },
            new VertexUpdater<String>() {
               @Override
               public void updateVertex(String vertex, Map<String, String> attributes) {
                  // do nothing strings can't update.
               }
            }
      );

      importer.importGraph(result, new StringReader(input));

      Assert.assertEquals(1, result.vertexSet().size());
      Assert.assertTrue(result.vertexSet().contains("a"));

   }

   public void testAttributesWithNoQuotes() throws ImportException {
      String input = "graph G {\n"
              + "  1 [ label = \"bob\" \"foo\"=bar ];\n"
              + "  2 [ label = \"fred\" ];\n"
              // the extra label will be ignored but not cause any problems.
              + "  1 -- 2 [ label = \"friend\" \"foo\" = wibble];\n"
              + "}";

      Multigraph<TestVertex, TestEdge> result
              = new Multigraph<TestVertex, TestEdge>(TestEdge.class);
      DOTImporter<TestVertex, TestEdge> importer
              = new DOTImporter<TestVertex, TestEdge>(
              new VertexProvider<TestVertex>() {
                 @Override
                 public TestVertex buildVertex(String label,
                                               Map<String, String> attributes) {
                    return new TestVertex(label, attributes);
                 }
              },
              new EdgeProvider<TestVertex, TestEdge>() {
                 @Override
                 public TestEdge buildEdge(TestVertex from,
                                              TestVertex to,
                                              String label,
                                              Map<String, String> attributes) {
                    return new TestEdge(label, attributes);
                 }
              }
      );


      importer.importGraph(result, new StringReader(input));
      Assert.assertEquals("wrong size of vertexSet", 2, result.vertexSet().size());
      Assert.assertEquals("wrong size of edgeSet", 1, result.edgeSet().size());

      for(TestVertex v : result.vertexSet()) {
         if ("1".equals(v.getId())) {
            Assert.assertEquals("wrong number of attributes", 2, v.getAttributes().size());
            Assert.assertEquals("Wrong attribute values", "bar", v.getAttributes().get("foo"));
            Assert.assertEquals("Wrong attribute values", "bob", v.getAttributes().get("label"));
         } else {
            Assert.assertEquals("wrong number of attributes", 1, v.getAttributes().size());
            Assert.assertEquals("Wrong attribute values", "fred", v.getAttributes().get("label"));
         }
      }

      for (TestEdge e : result.edgeSet()) {
         Assert.assertEquals("wrong id", "friend", e.getId());
         Assert.assertEquals("wrong number of attributes", 2, e.getAttributes().size());
         Assert.assertEquals("Wrong attribute value", "wibble", e.getAttributes().get("foo"));
         Assert.assertEquals("Wrong attribute value", "friend", e.getAttributes().get("label"));
      }

   }

   public void testEmptyString()
   {
      testGarbage("", "Dot string was empty");
   }

   public void testGarbageStringEnoughLines()
   {
      String input = "jsfhg kjdsf hgkfds\n"
                     + "fdsgfdsgfd\n"
                     + "gfdgfdsgfdsg\n"
                     + "jdhgkjfdshgsjkhl\n";

      testGarbage(input, "Invalid Header");
   }

   public void testGarbageStringInvalidFirstLine()
   {
      String input = "jsfhgkjdsfhgkfds\n"
                     + "fdsgfdsgfd\n";

      testGarbage(input, "Invalid Header");
   }

   public void testGarbageStringNotEnoughLines()
   {
      String input = "jsfhgkjdsfhgkfds\n";

      testGarbage(input, "Invalid Header");
   }

   public void testIncompatibleGraphMulti() {
      String input = "strict digraph G {\n"
                     + "a -- b\n"
                     + "}";
      testGarbage(input, "graph defines strict but Multigraph given.");
   }

   public void testIncompatibleDirectedGraph() {
      String input = "digraph G {\n"
                     + "a -- b\n"
                     + "}";

      Multigraph<String, DefaultEdge> result
            = new Multigraph<String, DefaultEdge>(DefaultEdge.class);

      testGarbageGraph(input, "input asks for directed graph but undirected graph provided.", result);
   }

   public void testIncompatibleGraph() {
      String input = "graph G {\n"
                     + "a -- b\n"
                     + "}";

      DirectedMultigraph<String, DefaultEdge> result
            = new DirectedMultigraph<String, DefaultEdge>(DefaultEdge.class);

      testGarbageGraph(input, "input asks for undirected graph and directed graph provided.", result);
   }

   public void testInvalidAttributes() {
      String input = "graph G {\n"
              + "  1 [ label = \"bob\" \"foo\" ];\n"
              + "  2 [ label = \"fred\" ];\n"
              // the extra label will be ignored but not cause any problems.
              + "  1 -- 2 [ label = friend foo];\n"
              + "}";

      Multigraph<TestVertex, TestEdge> graph
              = new Multigraph<TestVertex, TestEdge>(TestEdge.class);

      DOTImporter<TestVertex, TestEdge> importer
              = new DOTImporter<TestVertex, TestEdge>(
              new VertexProvider<TestVertex>() {
                 @Override
                 public TestVertex buildVertex(String label,
                                               Map<String, String> attributes) {
                    return new TestVertex(label, attributes);
                 }
              },
              new EdgeProvider<TestVertex, TestEdge>() {
                 @Override
                 public TestEdge buildEdge(TestVertex from,
                                           TestVertex to,
                                           String label,
                                           Map<String, String> attributes) {
                    return new TestEdge(label, attributes);
                 }
              }
      );

      try {
         importer.importGraph(graph, new StringReader(input));
         Assert.fail("Should not get here");
      } catch (ImportException e) {
         Assert.assertEquals("Invalid attributes", e.getMessage());
      }
   }

   public void testUpdatingVertex() throws ImportException {
      String input = "graph G {\n"
                     + "a -- b;\n"
                     + "a [foo=\"bar\"];\n"
                     + "}";
      Multigraph<TestVertex, DefaultEdge> result
            = new Multigraph<TestVertex, DefaultEdge>(DefaultEdge.class);

      DOTImporter<TestVertex, DefaultEdge> importer = new DOTImporter<TestVertex, DefaultEdge>(
            new VertexProvider<TestVertex>() {
               @Override
               public TestVertex buildVertex(String label, Map<String, String> attributes) {
                  return new TestVertex(label, attributes);
               }
            },
            new EdgeProvider<TestVertex, DefaultEdge>() {
               @Override
               public DefaultEdge buildEdge(TestVertex from, TestVertex to, String label,
                                            Map<String, String> attributes) {
                  return new DefaultEdge();
               }
            },
            new VertexUpdater<TestVertex>() {
               @Override
               public void updateVertex(TestVertex vertex, Map<String, String> attributes) {
                  vertex.getAttributes().putAll(attributes);
               }
            }
      );

      importer.importGraph(result, new StringReader(input));

      Assert.assertEquals("wrong size of vertexSet", 2, result.vertexSet().size());
      Assert.assertEquals("wrong size of edgeSet", 1, result.edgeSet().size());
      for(TestVertex v : result.vertexSet()) {
         if ("a".equals(v.getId())) {
            Assert.assertEquals("wrong number of attributes", 1, v.getAttributes().size());
         } else {
            Assert.assertEquals("attributes are populated", 0, v.getAttributes().size());
         }
      }

   }

   public void testParametersWithSemicolons() throws ImportException {
      String input = "graph G {\n  1 [ label=\"this label; contains a semi colon\" ];\n}\n";
      Multigraph<TestVertex, DefaultEdge> result
            = new Multigraph<TestVertex, DefaultEdge>(DefaultEdge.class);
      DOTImporter<TestVertex, DefaultEdge> importer
            = new DOTImporter<TestVertex, DefaultEdge>(
            new VertexProvider<TestVertex>() {
               @Override
               public TestVertex buildVertex(String label,
                                             Map<String, String> attributes) {
                  return new TestVertex(label, attributes);
               }
            },
            new EdgeProvider<TestVertex, DefaultEdge>() {
               @Override
               public DefaultEdge buildEdge(TestVertex from,
                                            TestVertex to,
                                            String label,
                                            Map<String, String> attributes) {
                  return new DefaultEdge();
               }
            }
      );


      importer.importGraph(result, new StringReader(input));
      Assert.assertEquals("wrong size of vertexSet", 1, result.vertexSet().size());
      Assert.assertEquals("wrong size of edgeSet", 0, result.edgeSet().size());
   }

   public void testLabelsWithEscapedSemicolons() throws ImportException {
      String escapedLabel = "this \\\"label; \\\"contains an escaped semi colon";
      String input = "graph G {\n node [ label=\"" + escapedLabel + "\" ];\n}\n";
      Multigraph<TestVertex, DefaultEdge> result
              = new Multigraph<TestVertex, DefaultEdge>(DefaultEdge.class);
      DOTImporter<TestVertex, DefaultEdge> importer
              = new DOTImporter<TestVertex, DefaultEdge>(
              new VertexProvider<TestVertex>() {
                 @Override
                 public TestVertex buildVertex(String label,
                                               Map<String, String> attributes) {
                     if (label.equals("node")) { 
                         Assert.assertEquals(attributes.get("label"), escapedLabel);
                     }
                     return new TestVertex(label, attributes);
                 }
              },
              new EdgeProvider<TestVertex, DefaultEdge>() {
                 @Override
                 public DefaultEdge buildEdge(TestVertex from,
                                              TestVertex to,
                                              String label,
                                              Map<String, String> attributes) {
                    return new DefaultEdge();
                 }
              }
      );


      importer.importGraph(result, new StringReader(input));
      Assert.assertEquals("wrong size of vertexSet", 1, result.vertexSet().size());
      Assert.assertEquals("wrong size of edgeSet", 0, result.edgeSet().size());
      Assert.assertEquals("wrong parsing", "node", ((TestVertex) result.vertexSet().toArray()[0]).getId());
   }

   public void testNoLineEndBetweenNodes() throws ImportException {
      String input = "graph G {\n  1 [ label=\"this label; contains a semi colon\" ];  2 [ label=\"wibble\" ] \n}\n";
      Multigraph<TestVertex, DefaultEdge> result
            = new Multigraph<TestVertex, DefaultEdge>(DefaultEdge.class);
      DOTImporter<TestVertex, DefaultEdge> importer
            = new DOTImporter<TestVertex, DefaultEdge>(
            new VertexProvider<TestVertex>() {
               @Override
               public TestVertex buildVertex(String label,
                                             Map<String, String> attributes) {
                  return new TestVertex(label, attributes);
               }
            },
            new EdgeProvider<TestVertex, DefaultEdge>() {
               @Override
               public DefaultEdge buildEdge(TestVertex from,
                                            TestVertex to,
                                            String label,
                                            Map<String, String> attributes) {
                  return new DefaultEdge();
               }
            }
      );


      importer.importGraph(result, new StringReader(input));
      Assert.assertEquals("wrong size of vertexSet", 2, result.vertexSet().size());
      Assert.assertEquals("wrong size of edgeSet", 0, result.edgeSet().size());
   }

   public void testNonConfiguredUpdate() {
      String input = "graph G {\n"
                     + "a -- b // this is before the attributes for this test\n"
                     + "a [foo=\"bar\"];\n"
                     + "}";
      Multigraph<TestVertex, DefaultEdge> result
            = new Multigraph<TestVertex, DefaultEdge>(DefaultEdge.class);
      DOTImporter<TestVertex, DefaultEdge> importer
            = new DOTImporter<TestVertex, DefaultEdge>(
            new VertexProvider<TestVertex>() {
               @Override
               public TestVertex buildVertex(String label,
                                             Map<String, String> attributes) {
                  return new TestVertex(label, attributes);
               }
            },
            new EdgeProvider<TestVertex, DefaultEdge>() {
               @Override
               public DefaultEdge buildEdge(TestVertex from,
                                            TestVertex to,
                                            String label,
                                            Map<String, String> attributes) {
                  return new DefaultEdge();
               }
            }
      );

      try {
         importer.importGraph(result, new StringReader(input));
         Assert.fail("should not get here");
      } catch (ImportException e) {
         Assert.assertEquals(
               "exception not as expected",
               "Update required for vertex a but no vertexUpdater provided",
               e.getMessage()
         );
      }

   }
   
   public void testWithReader() throws ImportException {
       String input = "graph G {\n"
                      + "  1 [ \"label\"=\"abc123\" ];\n"
                      + "  2 [ label=\"fred\" ];\n"
                      + "  1 -- 2;\n"
                      + "}";

       Multigraph<String, DefaultEdge> expected
             = new Multigraph<String, DefaultEdge>(DefaultEdge.class);
       expected.addVertex("1");
       expected.addVertex("2");
       expected.addEdge("1", "2");

       DOTImporter<String, DefaultEdge> importer = buildImporter();

       Graph<String, DefaultEdge> result
             = new Multigraph<String, DefaultEdge>(DefaultEdge.class);
       importer.importGraph(result, new StringReader(input));

       Assert.assertEquals(expected.toString(), result.toString());

       Assert.assertEquals(2, result.vertexSet().size());
       Assert.assertEquals(1, result.edgeSet().size());

    }

   private void testGarbage(String input, String expected) {
      DirectedMultigraph<String, DefaultEdge> result
            = new DirectedMultigraph<String, DefaultEdge>(DefaultEdge.class);
      testGarbageGraph(input, expected, result);
   }

   private void testGarbageGraph(String input, String expected, AbstractBaseGraph<String, DefaultEdge> graph) {
      DOTImporter<String, DefaultEdge> importer = buildImporter();
      try {
         importer.importGraph(graph, new StringReader(input));
         Assert.fail("Should not get here");
      } catch (ImportException e) {
         Assert.assertEquals(expected, e.getMessage());
      }
   }

   private DOTImporter<String, DefaultEdge> buildImporter() {
      return new DOTImporter<String, DefaultEdge>(
            new VertexProvider<String>() {
               @Override
               public String buildVertex(String label,
                                         Map<String, String> attributes) {
                  return label;
               }
            },
            new EdgeProvider<String, DefaultEdge>() {
               @Override
               public DefaultEdge buildEdge(String from,
                                            String to,
                                            String label,
                                            Map<String, String> attributes) {
                  return new DefaultEdge();
               }
            }
      );
   }

   private class TestVertex {
      String id;
      Map <String, String> attributes;

      public TestVertex(String id, Map<String, String> attributes) {
         this.id = id;
         this.attributes = attributes;
      }

      public String getId() {
         return id;
      }

      public void setId(String id) {
         this.id = id;
      }

      public Map<String, String> getAttributes() {
         return attributes;
      }

      public void setAttributes(Map<String, String> attributes) {
         this.attributes = attributes;
      }
   }

   private class TestEdge extends DefaultEdge {
      String id;
      Map<String, String> attributes;

      public TestEdge(String id, Map<String, String> attributes) {
         super();
         this.id = id;
         this.attributes = attributes;
      }

      public String getId() {
         return id;
      }

      public void setId(String id) {
         this.id = id;
      }

      public Map<String, String> getAttributes() {
         return attributes;
      }

      public void setAttributes(Map<String, String> attributes) {
         this.attributes = attributes;
      }
   }
}
