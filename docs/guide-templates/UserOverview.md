---
title: Overview for Application Developers
---

# {{ page.title }}
{:.no_toc}

This overview will help get you started with using the JGraphT library in
your own applications.  We'll cover the following topics:

1. Table of contents
{:toc}

## Development Setup

First, [set up your development environment](https://github.com/jgrapht/jgrapht/wiki/Users:-How-to-use-JGraphT-as-a-dependency-in-your-projects) with JGraphT as a dependency.

## Hello JGraphT

In JGraphT, a graph is defined as a set of vertices connected by a set
of edges.  Many possible variations on this fundamental definition are
supported, as we'll explain further on; but for now, let's take a look
at a simple example of creating a directed graph:

```java
:[source code](http://code.jgrapht.org/raw/master/jgrapht-demo/src/main/java/org/jgrapht/demo/HelloJGraphT.java?example=uriCreate)
```

Notice how the vertex objects are instances of the
[java.net.URI](https://docs.oracle.com/javase/8/docs/api/java/net/URI.html)
class.  JGraphT does not supply a vertex class itself; instead, you're
free to choose your own based on whatever works best for your
application, subject to certain restrictions mentioned below.

You are also free to choose your own edge class.  If you don't need to
associate any application-specific information with your edges, you
can just use the library-supplied
[DefaultEdge](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/graph/DefaultEdge.html)
as in this example.  The graph constructor takes the edge class as a
parameter so that it can create new edge objects implicitly whenever
`addEdge` is called to connect two vertices.

## Choosing Vertex and Edge Types

There are a number of restrictions to be aware of when choosing custom
vertex and edge types, mostly regarding override of the
`equals`/`hashCode` methods; be sure to read through
[this overview](VertexAndEdgeTypes).

## Graph Accessors

Once a graph has been created, an application can access its vertices
and edges directly via live set views:

```java
:[source code](http://code.jgrapht.org/raw/master/jgrapht-demo/src/main/java/org/jgrapht/demo/HelloJGraphT.java?example=findVertex)
```

Here we iterate over all vertices of the graph via the [vertexSet](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/Graph.html#vertexSet--) method, filtering for only those
whose URL has `www.jgrapht.org` for its hostname; in our example, we can
expect to find exactly one match, which we obtain via `findAny().get()`.

Given a reference to a vertex or edge, we can find connections via
`Graph` methods such as `getEdgeSource`, `getEdgeTarget`, `edgesOf`,
`incomingEdgesOf`, and `outgoingEdgesOf`.  Given a pair of vertices,
we can find the edge(s) connecting them via `getEdge` and
`getAllEdges`.  Here, collection-returning methods should not to be
assumed to be live views (although they may be for some graph
implementations).  In some cases, the returned collections may be
unmodifiable, while in others they may consist of transient results.
In no case should an application expect modifications to the returned
collection to result in modifications to the underyling graph.

The [Graphs](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/Graphs.html)
utility class has additional convenience methods such as
[successorListOf](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/Graphs.html#successorListOf-org.jgrapht.Graph-V-)
and
[getOppositeVertex](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/Graphs.html#getOppositeVertex-org.jgrapht.Graph-E-V-)
for easing common access patterns.

Note that the default graph implementations guarantee predictable
ordering for the collections that they maintain; so, for example, if
you add vertices in the order `[B, A, C]`, you can expect to see them in
that order when iterating over the vertex set.  However, this is not a
requirement of the `Graph` interface, so other graph implementations
are not guaranteed to honor it.

## Graph Structures

Besides choosing your vertex and edge classes, JGraphT also allows you
to choose a graph structure.  One way to do so is by instantiating a
concrete class which implements the
[Graph](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/Graph.html) interface,
as with `DefaultDirectedGraph` in the example above.  When doing so,
you can make your selection from the table below (or from your own
subclasses of any of these).

| Class Name                   | Edges    | Self-loops | Multiple edges | Weighted |
|:----------------------------:|:--------:|:----------:|:--------------:|:--------:|
|SimpleGraph                   |undirected|no          | no             |no        |
|Multigraph                    |undirected|no          | yes            |no        |
|Pseudograph                   |undirected|yes         | yes            |no        |
|DefaultUndirectedGraph        |undirected|yes         | no             |no        |
|SimpleWeightedGraph           |undirected|no          | no             |yes       |
|WeightedMultigraph            |undirected|no          | yes            |yes       |
|WeightedPseudograph           |undirected|yes         | yes            |yes       |
|DefaultUndirectedWeightedGraph|undirected|yes         | no             |yes       |
|SimpleDirectedGraph           |directed  |no          | no             |no        |
|DirectedMultigraph            |directed  |no          | yes            |no        |
|DirectedPseudograph           |directed  |yes         | yes            |no        |
|DefaultDirectedGraph          |directed  |yes         | no             |no        |
|SimpleDirectedWeightedGraph   |directed  |no          | no             |yes       |
|DirectedWeightedMultigraph    |directed  |no          | yes            |yes       |
|DirectedWeightedPseudograph   |directed  |yes         | yes            |yes       |
|DefaultDirectedWeightedGraph  |directed  |yes         | no             |yes       |

The structural properties are as follows:

* undirected edges:  an edge simply connects a vertex pair, without imposing a direction
* directed edges:  an edge has a source and a target
* self-loops:  whether to allow edges which connect a vertex to itself
* multiple edges:  whether to allow more than one edge between the same vertex pair (note that in a directed graph, two edges between the same vertex pair but with opposite direction do not count as multiple edges)
* weighted:  whether a double weight is associated with each edge (for these graph types, you'll usually want to use [DefaultWeightedEdge](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/graph/DefaultWeightedEdge.html) as your edge class); unweighted graphs are treated as if they have a uniform edge weight of 1.0, which allows them to be used in algorithms such as finding a shortest path

The [GraphType](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/GraphType.html)
interface allows you to access this metadata for an existing graph
instance (using the
[getType](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/Graph.html#getType--)
accessor).

You can also use [GraphTypeBuilder](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/graph/builder/GraphTypeBuilder.html) to instantiate a new graph without directly constructing a concrete class:

```java
:[source code](http://code.jgrapht.org/raw/master/jgrapht-demo/src/main/java/org/jgrapht/demo/GraphBuilderDemo.java?example=buildType)
```

`GraphTypeBuilder` uses the property values you supply in order to
automatically choose the correct concrete class for you.  This is
generally a cleaner pattern to follow, but it's not applicable if you
end up needing to subclass one of the provided graph classes.

## Graph Modification

Earlier, we saw how to add vertices and edges to a new graph by
calling the
[addVertex](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/Graph.html#addVertex-V-)
and
[addEdge](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/Graph.html#addEdge-V-V-)
methods on the `Graph` interface.  Likewise, there are corresponding
methods for removing graph components.  All of these methods are
modeled on the `java.util` collections framework, so:

* adding a duplicate object to a set (e.g. when adding a vertex) is not an error, but the duplicate is discarded
* adding a duplicate object to a non-unique collection (e.g. when adding an edge to a multigraph) inserts the new instance
* attempting to remove an object which was not part of the graph is not an error
* but _attempting to access attributes of an object which is not part of the graph_ is strictly forbidden, and results in an `IllegalArgumentException` (e.g. when you ask for the edges of a vertex, but the vertex is not currently part of the graph)

The strictness enforcement mentioned above is the default in order to
help catch application errors.  There are two convenience helpers
available to assist with this when adding components to a graph; both
of them take care of automatically adding vertices whenever edges are
added:

* The [Graphs](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/Graphs.html) utility class provides methods such as [addEdgeWithVertices](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/Graphs.html#addEdgeWithVertices-org.jgrapht.Graph-V-V-)
* The [GraphBuilder](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/graph/builder/AbstractGraphBuilder.html) framework also  allows you to use [method chaining](https://en.wikipedia.org/wiki/Method_chaining) when populating data in a new graph.

Here's an example using `GraphBuilder` to construct a
[kite graph](http://mathworld.wolfram.com/KiteGraph.html):

```java
:[source code](http://code.jgrapht.org/raw/master/jgrapht-demo/src/main/java/org/jgrapht/demo/GraphBuilderDemo.java?example=buildEdges)
```

The integer vertex objects are added to the graph implicitly as the
referencing edges are added.  Note that building the graph proceeds in
two phases; first `buildEmptySimpleGraph` builds an empty graph
instance for the specified graph type, then `GraphBuilder` takes over
for populating the vertices and edges.

### Vertex and Edge Suppliers

JGraphT optionally allows you to provide a graph with vertex and edge
suppliers.  When these are available, the graph will automatically
construct a new object instance whenever one is not explicitly
supplied by the corresponding `add` method.

### Modification Listeners

JGrapht provides a framework for reacting to graph modifications via
the
[ListenableGraph](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/ListenableGraph.html)
interface.  By default, graph instances are not listenable for
efficiency; here's how to use the framework:

* wrap your graph instance via [DefaultListenableGraph](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/graph/DefaultListenableGraph.html)
* perform all modifications on the wrapper (not the underlying graph instance)
* register one or more [GraphListener](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/event/GraphListener.html) to react to modification events

This can be a convenient way to keep other data structures or
visualizations in sync with graph changes.  For example, suppose your
graph represents a CAD model being visualized; then every time the
graph is edited, all affected views can be automatically refreshed
from listener events.

### Concurrency

The default graph implementations are not safe for concurrent reads
and writes from different threads.  If an application attempts to
modify a graph in one thread while another thread is reading or
writing the same graph, undefined behavior will result.  However,
concurrent reads against the same graph from different threads are
safe.  (Note that the Graph interface itself makes no such guarantee,
so for non-default implementations, different rules may apply.)

If you need support for concurrent reads and writes, consider using
the
[AsSynchronizedGraph wrapper](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/graph/concurrent/AsSynchronizedGraph.html).

## Graph Generation

Besides constructing vertices and edges individually, applications can
also generate graph instances according to predefined patterns.  This
is often useful for generating test cases or default topologies.
JGraphT provides a number of different generators for this purpose in
the
[org.jgrapht.generate](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/generate/package-summary.html)
package.  Here's an example of generating a [complete graph](http://mathworld.wolfram.com/CompleteGraph.html):

```java
:[source code](http://code.jgrapht.org/raw/master/jgrapht-demo/src/main/java/org/jgrapht/demo/CompleteGraphDemo.java?example=class)
```

The `SIZE` parameter controls the number of vertices added to the
graph (which in turn dictates the number of edges added).

## Graph Traversal

Once you've created a graph, you can traverse it using an ordering
such as depth-first, breadth-first, or topological.  JGraphT provides
for this via package
[org.jgrapht.traverse](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/traverse/package-summary.html).
The common interface is
[GraphIterator](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/traverse/GraphIterator.html),
which specializes the generic Java `Iterator` interface with JGraphT
specifics.  A graph iterator produces vertices in the requested order;
as the iteration proceeds, additional information (such as when a
particular edge is traversed) can be obtained by registering a
[TraversalListener](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/event/TraversalListener.html).
(The specific meaning of traversal events varies with the iterator
type.)

Here's an example using depth-first ordering on our HelloJGraphT example:

```java
:[source code](http://code.jgrapht.org/raw/master/jgrapht-demo/src/main/java/org/jgrapht/demo/HelloJGraphT.java?example=traverse)
```

with expected output

```
http://www.jgrapht.org
http://www.wikipedia.org
http://www.google.com
```

In this example, no extra information is required during the
traversal, so it is treated as a standard Java `Iterator`.

## Graph Algorithms

Beyond basic traversals, you'll often want to run more complex algorithms on
a graph.  JGraphT provides quite a few of these, so they are subcategorized
under the
[org.jgrapht.alg parent package](https://jgrapht.org/javadoc/org.jgrapht.core/module-summary.html).
For example, various shortest path algorithms are implemented in
[org.jgrapht.alg.shortestpath](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/alg/shortestpath/package-summary.html).

In cases where there are alternative algorithms available for the same
problem, the commonality is abstracted via an interface in
[org.jgrapht.alg.interfaces](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/alg/interfaces/package-summary.html).
This makes it easier to write application code which selects an
optimal algorithm implementation for a given graph instance.

Here's an example of running [strongly connected components](http://mathworld.wolfram.com/StronglyConnectedComponent.html) and shortest path algorithms on a directed graph:

```java
:[source code](http://code.jgrapht.org/raw/master/jgrapht-demo/src/main/java/org/jgrapht/demo/DirectedGraphDemo.java?example=main)
```

with expected output

```
Strongly connected components:
([i], [])
([h], [])
([e, f, g], [(e,f), (f,g), (g,e)])
([a, b, c, d], [(a,b), (b,d), (d,c), (c,a)])

Shortest path from i to c:
[(i : h), (h : e), (e : d), (d : c)]

Shortest path from c to i:
null
```

## Graph Serialization and Export/Import

The default graph implementations provided by JGraphT are
[serializable](https://docs.oracle.com/javase/8/docs/api/java/io/Serializable.html)
as long as you choose vertex and edge types which are themselves
serializable.

Serialization is a convenient way to store a graph instance as binary
data, but the format is not human-readable, and we don't make any
guarantee of serialization compatibility across JGraphT versions.  (In
other words, if you serialize a graph with version X, and then attempt
to deserialize it with version X+1, an exception may be thrown.)

To address this, JGraphT provides module
[org.jgrapht.io](https://jgrapht.org/javadoc/org.jgrapht.io/module-summary.html)
for exporting and importing graphs in a variety of standard formats.
These can also be used for data interchange with other applications.

Continuing our HelloJGraphT example, here's how to export a graph in [GraphViz .dot](https://www.graphviz.org/) format:

```java
:[source code](http://code.jgrapht.org/raw/master/jgrapht-demo/src/main/java/org/jgrapht/demo/HelloJGraphT.java?example=render)
```

with expected output

```
strict digraph G {
  www_google_com [ label="http://www.google.com" ];
  www_wikipedia_org [ label="http://www.wikipedia.org" ];
  www_jgrapht_org [ label="http://www.jgrapht.org" ];
  www_jgrapht_org -> www_wikipedia_org;
  www_google_com -> www_jgrapht_org;
  www_google_com -> www_wikipedia_org;
  www_wikipedia_org -> www_google_com;
}
```

which GraphViz renders as:

![example graph rendering](hello.png "Hello GraphViz!")

If you just want a quick dump of the structure of a small graph, you
can also use the `toString` method; here's another example from the HelloJGraphT demo:

```java
:[source code](http://code.jgrapht.org/raw/master/jgrapht-demo/src/main/java/org/jgrapht/demo/HelloJGraphT.java?example=toString)
```

which produces

```
([v1, v2, v3, v4], [{v1,v2}, {v2,v3}, {v3,v4}, {v4,v1}])

```

First comes the vertex set, followed by the edge set.  Directed edges
are rendered with round brackets, whereas undirected edges are
rendered with curly brackets.  Custom edge attributes are not
rendered.  If you want a nicer rendering, you can override
[toStringFromSets](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/graph/AbstractGraph.html#toStringFromSets-java.util.Collection-java.util.Collection-boolean-)
in your graph implementation, but you're probably better off using one
of the exporters instead.

## Graph Cloning

The `Graph` interface does not expose a public `clone` method, because
we do not require all implementations to be cloneable.  However, all
subclasses of
[AbstractBaseGraph](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/graph/AbstractBaseGraph.html)
are cloneable.  The clone semantics are shallow in that the same vertex
and edge objects are shared between the original graph and the clone;
however, the vertex and edge sets and all associated connectivity
structures are copied, not shared, so that the two graphs are otherwise
independent.

## Graph Comparisons

The default JGraphT implementations of the `Graph` interface override `equals`/`hashCode`, so it's possible to use them to compare two graph instances.  However, it's important to note that the definition of equality used may not be the one you are expecting.  Here are the rules used:

* the two graph instances must be of identical concrete class (e.g. `DefaultDirectedGraph`)
* the vertex sets of the two graph instances must be equal (using the definition from [java.util.Set](https://docs.oracle.com/javase/7/docs/api/java/util/Set.html#equals(java.lang.Object)), and taking into account the `equals` implementation of the vertex type you've chosen)
* the edges sets of the two graph instances must be equal (again using the `java.util.Set` definition, and taking into account the `equals` implementation of the edge type you've chosen)
* for a given edge, the source/target/weight must be equal in both graph instances (for undirected graphs, the source/target distinction is ignored)

In general, an exact copy of a graph object via `Graphs.addGraph` or
`clone` will be equal to the original according to this definition
(assuming the same concrete class is chosen for the copy). However,
for copy via serialization followed by deserialization, this won't
hold unless both the vertex and edge classes override `equals`/`hashCode`.

If you were expecting a structural comparison instead, then you might
want to investigate the
[isomorphism](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/alg/isomorphism/package-summary.html)
package.  In the unrestricted case, isomorphism detection can take
exponential time, but it can be speeded up significantly if you're
able to guide it with a labeling.  For example, suppose you have two
graphs with anonymous edges, but the vertex set is the same, and you
want to decide whether the graphs are effectively equal.  In that case, you can run an
isomorphism inspector with a comparator specified for the vertices.
Then JGraphT can tell you whether the two graphs are structurally
equivalent (and if so, provide a mapping between the edge objects).

## Graph Wrappers

Besides core graph data structures, JGraphT also provides a number of useful wrappers which allow you to define live _transformed_ views into other graphs:

* [AsGraphUnion](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/graph/AsGraphUnion.html):  a union of two underlying graphs
* [AsSubgraph](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/graph/AsSubgraph.html):  a subgraph (possibly induced) of an underlying graph
* [AsUndirectedGraph](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/graph/AsUndirectedGraph.html):  an undirected view of an underlying directed graph (with edge directions ignored)
* [AsUnmodifiableGraph](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/graph/AsUnmodifiableGraph.html): an unmodifiable view of an underlying graph
* [AsUnweightedGraph](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/graph/AsUnweightedGraph.html): an unweighted view of a underlying weighted graph (ignoring all edge weights and treating them as 1.0 instead)
* [AsWeightedGraph](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/graph/AsWeightedGraph.html): a weighted view of an underlying unweighted graph (with edge-specific weights imposed via a map)
* [EdgeReversedGraph](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/graph/EdgeReversedGraph.html): an edge-reversed view of a directed graph

Wrappers add some access cost, so if you don't need a live view, and you will be accessing the transformed results heavily, then you can copy the view to a snapshot using [Graphs.addGraph](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/Graphs.html#addGraph-org.jgrapht.Graph-org.jgrapht.Graph-).

## Graph Adapters

### Guava Graph Adapter

If you are already using
[com.google.common.graph](https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/graph/package-summary.html)
for representing graphs, it's easy to interoperate with JGraphT by
using our
[adapter package](https://jgrapht.org/javadoc/org.jgrapht.guava/org/jgrapht/graph/guava/package-summary.html).
Simply instantiate the correct adapter on top of your Guava graph, and
you'll have an implementation of JGraphT's `Graph` interface which
stays in sync with the Guava graph automatically, at no extra memory
cost.  Now you can [run JGraphT algorithms](GuavaAdapter) on top of your Guava graph,
or run our importers or exporters against it.

### JGraphX Adapter

JGraphT also provides an adapter that lets you use a JGraphT graph
instance as the data model for a
[JGraphX](https://jgraph.github.io/mxgraph/docs/manual_javavis.html)
visualization.  All you need to do is wrap your JGraphT graph with
[org.jgrapht.ext.JGraphXAdapter](https://jgrapht.org/javadoc/org.jgrapht.ext/org/jgrapht/ext/JGraphXAdapter.html) as in the following example:

```java
:[source code](http://code.jgrapht.org/raw/master/jgrapht-demo/src/main/java/org/jgrapht/demo/JGraphXAdapterDemo.java?example=full)
```

## Running Demos

If you want to run the demo programs excerpted throughout this
overview, see
[these instructions](https://github.com/jgrapht/jgrapht/wiki/Users:-Running-JGraphT-demos).
You can also find the full source code in
[github](https://github.com/jgrapht/jgrapht/tree/master/jgrapht-demo/src/main/java/org/jgrapht/demo).

## Browsing Unit Tests

Another good way to learn how to use the various classes provided by
JGraphT is to study their usage in unit tests.  Here's the source code
of
[tests for the core classes](https://github.com/jgrapht/jgrapht/tree/master/jgrapht-core/src/test/java/org/jgrapht).
