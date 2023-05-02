---
title: Labeled Edges
---

# {{ page.title }}


A common requirement for JGraphT applications is the need to associate a label
with each edge.  This can be accomplished efficiently via a custom edge class:

```java
class RelationshipEdge
    extends DefaultEdge
{
    private String label;

    /**
     * Constructs a relationship edge
     *
     * @param label the label of the new edge.
     * 
     */
    public RelationshipEdge(String label)
    {
        this.label = label;
    }

    /**
     * Gets the label associated with this edge.
     *
     * @return edge label
     */
    public String getLabel()
    {
        return label;
    }

    @Override
    public String toString()
    {
        return "(" + getSource() + " : " + getTarget() + " : " + label + ")";
    }
}
```

JGraphT's default graph and edge implementations take care of
maintaining the connectivity information between vertices, so the
custom edge subclass only needs to store the label.  Since the custom
edge class does not override `equals`/`hashCode`, each edge object is
distinct from every other edge object (regardless of whether they
share the same label).  Consequently, labels do not have to be
unique within the same graph.

As defined above, `RelationshipEdge` could be used in either a
directed or undirected graph.  In the example below, we apply it to a
a backstabby form of non-symmetric friendship via a directed graph:

```java
        Graph<String, RelationshipEdge> graph = new DefaultDirectedGraph<>(RelationshipEdge.class);

        ArrayList<String> people = new ArrayList<String>();
        people.add("John");
        people.add("James");
        people.add("Sarah");
        people.add("Jessica");

        // John is everyone's friend
        for (String person : people) {
            graph.addVertex(person);
            if (!person.equals("John")) {
                graph.addEdge("John", person, new RelationshipEdge(FRIEND));
            }
        }

        // Apparently James doesn't really like John
        graph.addEdge("James", "John", new RelationshipEdge(ENEMY));

        // Jessica is Sarah and James's friend
        graph.addEdge("Jessica", "Sarah", new RelationshipEdge(FRIEND));
        graph.addEdge("Jessica", "James", new RelationshipEdge(FRIEND));

        // But Sarah doesn't really like James
        graph.addEdge("Sarah", "James", new RelationshipEdge(ENEMY));
```

Since the `RelationshipEdge` class does not have a default constructor, edges
must be explicitly instantiated and added via [addEdge(V,V,E)](http://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/Graph.html#addEdge-V-V-E-) rather than implicitly instantiated via
[addEdge(V,V)](http://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/Graph.html#addEdge-V-V-).

Once the graph has been populated, label information can be accessed during traversal:

```java
        for (RelationshipEdge edge : graph.edgeSet()) {
            String v1 = graph.getEdgeSource(edge);
            String v2 = graph.getEdgeTarget(edge);
            if (edge.getLabel().equals("enemy")) {
                System.out.printf(v1 + " is an enemy of " + v2 + "\n");
            } else if (edge.getLabel().equals("friend")) {
                System.out.printf(v1 + " is a friend of " + v2 + "\n");
            }
        }
```

Given two vertices, an application can check the label on the edge between them by using [getEdge(V,V)](http://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/Graph.html#getEdge-V-V-):

```java
    private static boolean isEnemyOf(
        Graph<String, RelationshipEdge> graph, String person1, String person2)
    {
        return graph.getEdge(person1, person2).getLabel().equals(ENEMY);
    }
```

You can find the complete source code for this example at [LabeledEdge.java](https://github.com/jgrapht/jgrapht/blob/master/jgrapht-demo/src/main/java/org/jgrapht/demo/LabeledEdges.java)
