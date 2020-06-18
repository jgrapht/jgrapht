---
title: Guava Graph Adapter
---

# {{ page.title }}

If you are using [Guava's common.graph data structure](https://github.com/google/guava/wiki/GraphsExplained), and would like to take advantage of an algorithm implemented by JGraphT, it's quite straightforward to do this via the adapters supplied by JGraphT.

For example, suppose you've created a Guava graph as follows:

```java
:[source code](http://code.jgrapht.org/raw/master/jgrapht-guava/src/test/java/org/jgrapht/graph/guava/MutableGraphAdapterTest.java?example=createGuavaGraph)
```

The graph does not have any information associated with the edges, so we can use JGraphT's [MutableGraphAdapter](https://jgrapht.org/javadoc/org.jgrapht.guava/org/jgrapht/graph/guava/MutableGraphAdapter.html) to view it in JGraphT:

```java
:[source code](http://code.jgrapht.org/raw/master/jgrapht-guava/src/test/java/org/jgrapht/graph/guava/MutableGraphAdapterTest.java?example=adaptGuavaGraph)
```

Now suppose we want to find a [minimum vertex cover](https://brilliant.org/wiki/vertex-cover) for this graph.  JGraphT supplies [several algorithms](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/alg/vertexcover/package-summary.html) for this purpose:

```java
:[source code](http://code.jgrapht.org/raw/master/jgrapht-guava/src/test/java/org/jgrapht/graph/guava/MutableGraphAdapterTest.java?example=findVertexCover)
```

Since the result is just a set of strings, it can be used to directly reference the JGraphT view as well as the underlying Guava graph.

For more information on the available adapters, please see the [org.jgrapht.graph.guava javadoc](https://jgrapht.org/javadoc/org.jgrapht.guava/org/jgrapht/graph/guava/package-summary.html).
