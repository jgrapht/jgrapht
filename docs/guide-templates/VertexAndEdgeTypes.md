---
title: Vertex and Edge Types
---

# {{ page.title }}
{:.no_toc}

When constructing a JGraphT graph, it's important to select the vertex
and edge types carefully in order to ensure correct behavior while
satisfying application requirements.  This page walks through a number
of variations based on common application use cases:

1. Table of contents
{:toc}

## equals and hashCode

Vertex and edge objects are used as keys inside of the default graph
implementation, so when choosing their types, you must follow these
rules:

* You must follow the contract defined in `java.lang.Object` for both [equals](https://docs.oracle.com/javase/7/docs/api/java/lang/Object.html#equals(java.lang.Object)) and [hashCode](https://docs.oracle.com/javase/7/docs/api/java/lang/Object.html#hashCode()).
* In particular, if you override either `equals` or `hashCode`, you must override them both
* Your implementation for `hashCode` must produce a value which does not change over the lifetime of the object

[This article](https://www.ibm.com/developerworks/java/library/j-jtp05273/index.html) explains some of the nuances.

Additional guidelines are provided in the scenario-specific sections below.

## Anonymous Vertices

Applications interested only in graph structure (e.g. graph theory
research) may want to save memory by keeping vertices as minimalist as
possible.  In this case, just use
[java.lang.Object](https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html)
as the vertex type.  In this case, the vertex `hashCode` may serve as
a "nearly" unique identifier during debugging.

## Vertices as Key Values

More common is for each vertex to represent a key value, as in the
[HelloJGraphT](UserOverview#hello-jgrapht) example (where each vertex
corresponds to a website identified by a URL).  For this use case, a
`String`, `Integer`, or similar class is a good choice for vertex
type.  In this case, it's important to note that the value must be
unique within the graph.  In other words, for a vertex of type
`String`, the `String` value is an _identifier_, not a mere _label_.

## Vertices with Attributes

More sophisticated applications may need to associate non-key
attributes (possibly multiple) with each vertex.  The obvious way to
do this is with a class that stores the attributes, but there are a
few different cases to consider.

### No keys

If all attributes on the vertex are non-key, then the approach is
straightforward.  For example, suppose you are modeling molecular
structures, where the vertices are atoms and the edges are the bonds
between them.  Then you might define a class

```java
class AtomVertex
{
    Element element;  // using enum of the periodic table
    int formalCharge; // for bookkeeping purposes
    ... other atomic properties ...
}
```

In this case, you should **not** override `equals` and `hashCode`,
since there may be many distinct atoms with the exact same properties.

### All keys

Conversely, if all attributes are components of a key, then the
approach is also simple.  Suppose your application is a software
package manager, and each vertex in your graph corresponds to a
package, with edges representing package dependencies.  Then you might
define a class like

```java
class SoftwarePackageVertex
{
    final String orgName;
    final String packageName;
    final String packageVersion;
    
    ... constructor etc ...
    
    public String toString()
    {
        return orgName + "-" + packageName + "-" + packageVersion;
    }
    
    public int hashCode()
    {
        return toString().hashCode();
    }
    
    public boolean equals(Object o)
    {
        return (o instanceof SoftwarePackageVertex) && (toString().equals(o.toString()));
    }
}
```

Here, you almost certainly **do** want to override `equals` and
`hashCode`, since there should not be more than one vertex object
representing the same package version.  And you'll be able to access
an existing vertex in a graph just by constructing it, without having
to iterate over all vertices in the graph to find it.

Note that the fields are declared final; this is important since
vertices and edges are used as hash keys, meaning their hash codes
must never change after construction.

### Key subset

Now we come to the problematic case.  Continuing the previous example,
suppose you want to add a `releaseDate` field to
`SoftwarePackageVertex` in order to track when the package version was
released.  This new field is not part of the key; it's just data.  But
what do we do about `equals`/`hashCode`?

* It's not a good idea to incorporate `releaseDate` into them for a number of reasons.  For example, if we want to reference the vertex for a package by its identifier, but we don't know its release date, how do we find the vertex?  And what if the release date changes for an unreleased package?  How do we avoid two distinct vertex objects representing the same package version?
* However, if we don't incorporate `releaseDate` into `equals`/`hashCode`, then we could end up with inconsistencies due to two vertex objects with different `releaseDate` values being treated as equivalent.

So if you try to implement this case, beware that you're likely to run into unforeseen pitfalls.

## Vertices as Pointers

A more flexible way to handle the situation above is to make the
vertex refer to an external object rather than containing data itself.
For the example above, the vertex type could be
`SoftwarePackageVertex` as originally defined, without the release
date.  Then additional information such as the release date would be
stored via a separate `SoftwarePackageVersion` class, with a map keyed
on `SoftwarePackageVertex` for lookups.  This keeps the graph
representation clear, but adds some lookup cost.

An optimization is to implement the vertex as a direct reference:

```java
class SoftwarePackageVertex
{
    final SoftwarePackageVersion version;
    
    public String toString()
    {
        return version.keyString();
    }
    
    public int hashCode()
    {
        return toString().hashCode();
    }
    
    public boolean equals(Object o)
    {
        return (o instanceof SoftwarePackageVertex) && (toString().equals(o.toString()));
    }
}

class SoftwarePackageVersion
{
    final String orgName;
    final String packageName;
    final String packageVersion;
    Date releaseDate;
    
    public String keyString()
    {
        return orgName + "-" + packageName + "-" + packageVersion;
    }
}
```

This way, we can construct a vertex from a package version at any
time, and given a vertex, we can directly access package version
information without any map lookup required.  The application is still
responsible for avoiding inconsistencies due to the existence of
multiple SoftwarePackageVersion objects with the same key, but now
that responsibility is separate from the graph representation.

## Anonymous Edges

Now let's move on to edge types.  The most common case is that there
is no information associated with an edge other than its connectivity.
Generally, you can use the `DefaultEdge` class provided by JGraphT for
this and not think about it any further.  However, there is one point
you should be aware of:

* JGraphT optimizes `DefaultEdge`-based graphs by using an intrusive technique in which the connectivity information is stored inside the edge object itself (rather than inside the graph).

As a result, if you need to add the same edge object to two different
graphs, then those graphs must have the same vertex connections for
that edge, otherwise undefined behavior will result.  If this (rare)
case applies to your application, then instead of using `DefaultEdge`,
just use `java.lang.Object` as your edge type.  Note that this adds a
map lookup penalty to connectivity accessor methods.

It's common in JGraphT for edge objects to be reused across graphs;
for example, an algorithm may return a subgraph of the input graph as
its result, and the subgraph will reuse subsets of the input graph's
vertex and edge sets.  In these cases, the connectivity equivalence is
valid (or if it's not, the algorithm avoids reuse).

## Weighted Edges

Another common case is for each edge to bear a double-valued weight as
its only attribute (e.g. a physical network with latency measured for
each link).  For this case, JGraphT supplies the `DefaultWeightedEdge`
class, which extends the optimization mentioned in the previous
section by storing the weight directly on the edge object.

The same caveats apply, with the additional restriction that if a
`DefaultWeightedEdge` is reused in another graph, it will have the
same weight in both graphs.  Again, if this presents a problem, then
use `java.lang.Object` as your edge class instead.

## Edges as Key Values

Sometimes, applications may be able to associate a unique key value
with each edge.  For example, consider a graph representing money
transfers between bank accounts, where the vertices represent the
accounts and the edges represent the transfers.  In this case, the
application could use a `String` containing the transfer transaction
ID as the edge type.

* _NOTE:_  Although correct, this implementation may not be optimal, since it loses the connectivity optimization described for `DefaultEdge` above.

However, it would definitely be **incorrect** to use the transaction
amount as the edge type, since this is not unique across the entire
graph.  (A weighted edge could instead be used for this purpose.)

## Edges with Attributes

For edges with multiple attributes or non-key attributes, the same
considerations as those
[given previously for vertices](#vertices-with-attributes) apply.  In
addition, when defining a class which will be used as an edge type,
applications will typically want to subclass either `DefaultEdge` or
`DefaultWeightedEdge` (subject to the caveats already mentioned).
Those base classes do not override `equals`/`hashCode`, but
applications are free to do so in subclasses as appropriate.

Note that when overriding `equals`/`hashCode` for an edge class, it is
incorrect to incorporate the edge source/target into the operations;
the edge identity is always independent of its connectivity.

For an example of how to apply a `String` attribute as a non-key label
on each edge, see [the LabeledEdges demo](LabeledEdges.md).  JGraphT
does not provide a labeled edge class since there are many different
ways to implement this pattern.

## Vertices and Edges as External Objects

In some cases, an application may want to use existing complex objects
as vertex and/or edge types directly.  For example, consider an
application in which the graph is used in a manager thread to optimize
concurrent dataflow, with each vertex representing a worker thread and
each edge representing a dataflow producer/consumer queue.  In this
case, it would be OK to use
[java.lang.Thread](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.html)
for the vertex type and
[LinkedBlockingDeque](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/LinkedBlockingDeque.html)
for the edge type (since these classes do no override
`equals`/`hashCode`).

However, if the queue implementation were such that it allowed two
queue instances to be compared for value-equality via `equals`, then this
would **not** be a good choice for edge type.  In this case, it would
be necessary to wrap the queue in a custom edge class which references
it,
[similar to what was described for vertices above](#vertices-as-pointers).

## Labeled Edges in a Multigraph

This is one case for which JGraphT does not currently support a 100%
efficient implementation.  Suppose we want to represent a
[finite state machine](https://en.wikipedia.org/wiki/Finite-state_machine)
using a pseudograph (allowing both self-loops and multiple edges
between vertices).  Vertices will represent states, and edges
will represent transitions.  For the vertex type, we might choose
`String`, but for the edge type, we can't use `String` since
transition names are not unique across the entire graph; they are only
unique within the subset of edges between a given pair of vertices.

Instead, we can use a labeled edge class as
[described above](#edges-with-attributes).  However, suppose we want
to find an existing edge given a pair of vertices and a transition
name.  This requires invoking
[getAllEdges](https://jgrapht.org/javadoc/org/jgrapht/Graph.html#getAllEdges-V-V-)
for the vertex pair and then searching through the result, filtering
by transition name.  If many transitions are defined, this may become
slow.

It would be nice if there were a faster solution for this problem,
especially since the graph's edge set already provides an index into
all edges in the graph.  There are kludgy ways to accomplish a
constant time lookup, but we don't recommend them, so we won't go into
them here.
