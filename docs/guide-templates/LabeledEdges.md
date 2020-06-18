---
title: Labeled Edges
---

# {{ page.title }}


A common requirement for JGraphT applications is the need to associate a label
with each edge.  This can be accomplished efficiently via a custom edge class:

```java
:[source code](http://code.jgrapht.org/raw/master/jgrapht-demo/src/main/java/org/jgrapht/demo/LabeledEdges.java?example=edgeclass)
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
:[source code](http://code.jgrapht.org/raw/master/jgrapht-demo/src/main/java/org/jgrapht/demo/LabeledEdges.java?example=create)
```

Since the `RelationshipEdge` class does not have a default constructor, edges
must be explicitly instantiated and added via [addEdge(V,V,E)](http://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/Graph.html#addEdge-V-V-E-) rather than implicitly instantiated via
[addEdge(V,V)](http://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/Graph.html#addEdge-V-V-).

Once the graph has been populated, label information can be accessed during traversal:

```java
:[source code](http://code.jgrapht.org/raw/master/jgrapht-demo/src/main/java/org/jgrapht/demo/LabeledEdges.java?example=print)
```

Given two vertices, an application can check the label on the edge between them by using [getEdge(V,V)](http://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/Graph.html#getEdge-V-V-):

```java
:[source code](http://code.jgrapht.org/raw/master/jgrapht-demo/src/main/java/org/jgrapht/demo/LabeledEdges.java?example=isEnemyOf)
```

You can find the complete source code for this example at [LabeledEdge.java](https://github.com/jgrapht/jgrapht/blob/master/jgrapht-demo/src/main/java/org/jgrapht/demo/LabeledEdges.java)
