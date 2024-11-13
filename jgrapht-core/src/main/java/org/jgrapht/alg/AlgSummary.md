
## JGraphT Algorithm Package Summary:

1. Clique:
This package contains 5 algorithms related to a Graph concept called Clique. A clique is a subset of vertices of an undirected graph
where every two distinct vertices are adjacent, which means that they are both end points of the same edge. For example, there is an
algorithm called BronKerboschCliqueFinder which calculates all maximum cliques and returns them to the user. 

2. Clustering:
This package contains 4 algorithms related to Graph clustering. Graph clustering is about dividing nodes in groups based on their 
common characteristics. For example, there is an algorithm called UndirectedModularityMeasurer that calculates the modularity of a 
graph, that is the measure of the strength of division of a graph into clusters.

3. Color:
This package contains 8 algorithms related to Graph Coloring. Graph coloring is a method to assign labels called "colors" to elements 
of a graph depending on some constraints. For example, there is an algorithm called GreedyColoring which assigns the minimum number 
possible of colors to the vertices of the graph, so that vertices that are neighbors do not have the same color. 

4. Connectivity:
This package contains 6 algorithms related to connectivity in graphs. A graph is connected when there is an edge between every pair of
vertices. Considering this, connectivity is the minimum number of elements in a graph that need to be removed so the resultant graph 
remains into two or more isolated subgraphs. For example, there is an algorithm called ConnectivityInspector that reviews different 
aspects of a graph, such as letting you know if a graph is connected or not. 

5. Cycle:
This package contains 21 algorithms related to graph cycles. A cycle is a trail in which only the first and last vertices are the same.
For example, there is an algorithm called CycleDetector which detects if there is any cycle in a graph.

6. Decomposition: 
This package contains 3 algorithms related to computing decompositions. Computing decompositions involves breaking down a system into
smaller parts. There is an algorithm called DulmageMendelsohnDecomposition which decompose a bipartite graph into subsets, where 
two adjacent vertices belong to the same subset if they are paired with each other in a perfect matching of the graph.

7. Densesubgraph:
This package contains 4 algorithms related to maximum density subgraphs. The density of a subgraph is defined as the total number of 
edges that the subgraph contains divided by the total number of vertices that the subgraph contains. The densest subgraph problem
is about finding the subgraph of maximum density. For example, there is an algorithm called GoldbergMaximumDensitySubgraphAlgorithm
that computes the maximum density subgraph.

8. Drawing:
This package contains 10 algorithms related to graph drawing. The algorithms contained in this package not just draw the graph but also
organize the distribution of the vertices in different layouts. For example, there is an algorithm called CircularLayoutAlgorithm2D
which draws a graph and places the vertices on a circle evenly spaced. The package inside this one called model includes some basic 
types and models to draw the graphs, such as the box model.

9. Flow:
This package contains 8 algorithms related to flow graphs. A flow graph is a type of directed graph associated with a set of linear
algebraic or differential equations. For example, there is an algorithm called EdmondsKarpMFImpl which calculates a maximum flow in
a flow network. The package inside the flow package named mincostcontains algorithms to resolve minimum cost flow problems. 

10. Independentset:
This package contains 1 algorithm related to independent sets in a graph. This algorithm, that is called 
ChordalGraphIndependentSetFinder, fulfills the task of calculating a maximum cardinality independent set in a chordalgraph.
A chordalgraph is a simple graph where all cycles of four or more vertices have a chord, which is an edge that connects two vertices
of the cycle but is not part of the cycle. Moreover, an independent set in a graph is a set of vertices in which no pair of vertices 
has an edge connecting them. The maximum cardinality independent set is the independent set of the largest size possible.

11. Interfaces:
This package contains the interfaces that the algorithms of other packages use to solve some problems, and it also contains
some algorithms of these packages. It contains 69 classes and interfaces for different uses. For example, the interface called
EdgeScoringAlgorithm is used by algorithms that assign scores to the edges of a graph. 

12. Isomorphism:
This package contains 10 algorithms related to (sub)graph isomorphism. Isomorphism is a property which indicates that two graphs's 
vertex set are bijective. For example, there is an algorithm called IsomorphicGraphMapping which represents a graph mapping between two
isomorphic graphs.

13. lca:
This package contains 5 algorithms related to lowest common ancestors in graphs. The lowest common ancestor of two nodes in a directed 
graph is the lowest graph that has both nodes as descendants, which means that there is a path from that node to the other two, and the 
graph does not have cycles. For example, there is an algorithm called NaiveLCAFinder that finds the lower common ancestor of a directed
graph.

14. Linkprediction:
This package contains 11 algorithms related to link prediction. Link prediction is a task where the goal is to predict missing or future
connections between nodes in a network. For example, there is an algorithm called AdamicAdarIndexLinkPrediction that is used for link
prediction.

15. Matching:
This package contains 8 algorithms related to the computation of matchings. A matching is an independent edge set, which is, in an 
undirected graph, a set of edges that does not have any vertices in common. For example, there is an algorithm called 
GreedyMaximumCardinalityMatching that calculates a maximum cardinality matching. The package inside this one called blossom contains
algorithms related to computation of matchings using the Kolmogorov's Blossom V algorithm.

16. Partition:
This package contains 1 algorithm related to computing partitions. This algorithm is called BipartitePartitioning. It calculates
the bipartite partitions of a graph, so it also checks whether a graph is bipartite or not. A bipartite graph is a graph whose 
vertices can be divided into two disjoint and independent sets, which means that every edge connects a vertex in one of the groups
to a vertex in the other group.

17. Planar:
This package contains 1 algorithm related to testing planarity of the graphs. This algorithm is named BoyerMyrvoldPlanarityInspector,
and it determines if a graph is planar or not. A graph is planar if	it can be drawn in a two-dimensional space without any of its
edges crossing.

18. Scoring:
This package contains 12 algorithms related to vertex and/or edge scoring. Scoring in graphs is about assigning coefficients to 
elements in graphs. For example, there is an algorithm called ClusteringCoefficient which calculates the clustering coefficient.

19. Shortestpath:
This package contains 36 algorithms related to computing shortest-path. The shortest-path problem is about finding a path between
two vertices in a graph so that the sum of the weights of the edges contained in that path is minimized. For example, there is an 
algorithm called DijkstraShortestPath that calculates the shortest path between two vertices. 

20. Similarity: 
This package contains 3 algorithms related to computing graph similarity metrics. The similarity of two graphs indicates how "similar"
these two graphs are. For example, there is an algorithm called ZhangShashaTreeEditDistance which computes edit distance between 
trees.

21. Spanning: 
This package contains 7 algorithms related to spanning trees. A spanning tree is a subgraph that is a tree which includes all of the
vertices of the original graph. For example, there is an algorithm called KruskalMinimumSpanningTree which computes the minimum 
spanning tree or forest, that is the spanning tree or forest that has the minimum weight. 

22. Tour:
This package contains 10 algorithms related to graph tours. A graph tour is a path that contains every vertex of the graph but
without repeating any edge. For example, there is an algorithm named HeldKarpTSP which solves the TSP problem.

23. Transform:
This package contains 1 algorithm related to graph transformers. This one algorithm is called LineGraphConverter. It produces the 
line graph of a given graph.

24. Util: 
This package contains 10 algorithms related to utilities used by JGraphT algorithms. For example, there is a class called Pair that
is used to create generic pairs of elements. There is a package inside this one that is called extension, and it has 3 algorithms 
that are used for managing extensions/encapsulations.

25. Vertexcover:
This package contains 5 algorithms related to vertex cover. A vertex cover of a graph is a set of vertices that includes at least
one endpointof every edge of the graph. For example, there is an algorithm called GreedyVCImpl that is used to calculate a minimum
vertex cover. There is a package inside this one that is called util, and it has one class used as an utility for vertex cover.