/*
 * (C) Copyright 2018-2021, by Timofey Chudakov and Contributors.
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
package org.jgrapht.alg.cycle;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;

import java.util.*;

/**
 * Tests whether a graph is <a href="http://www.graphclasses.org/classes/gc_14.html">weakly
 * chordal</a>. Weakly chordal graphs are also known as weakly triangulated graphs. Triangulated in
 * the context of chordality has a different meaning than triangulated in the context of planarity,
 * where it refers to a maximal planar graph, see:
 * <a href="http://mathworld.wolfram.com/TriangulatedGraph.html">
 * http://mathworld.wolfram.com/TriangulatedGraph.html</a>
 * <p>
 * The following definitions of are equivalent:
 * <ol>
 * <li>A graph is weakly chordal (weakly triangulated) if neither it nor its complement contains a
 * <a href="http://mathworld.wolfram.com/ChordlessCycle.html">chordless cycles</a> with five or more
 * vertices.</li>
 * <li>A 2-pair in a graph is a pair of non-adjacent vertices $x$, $y$ such that every chordless
 * path has exactly two edges. A graph is weakly chordal if every connected
 * <a href="https://en.wikipedia.org/wiki/Induced_subgraph">induced subgraph</a> $H$ that is not a
 * complete graph, contains a 2-pair.</li>
 * </ol>
 * Chordal and weakly chordal graphs are
 * <a href="http://mathworld.wolfram.com/PerfectGraph.html">perfect</a>.<br>
 * For more details, refer to: Hayward, R.B. Weakly triangulated graphs, Journal of Combinatorial
 * Theory, Series B, vol 39, Issue 3, pp 200-208, 1985.
 * <p>
 * The implementation in this class is based on: Lars Severin Skeide (2002)
 * <a href="http://www.ii.uib.no/~skeide/rec_wcg.pdf"><i>Recognizing weakly chordal graphs</i></a>.
 * Candidate Scientist Thesis in Informatics. Department of Informatics, University of Bergen,
 * Norway. The terminology used in this implementation is consistent with the one used in this
 * thesis.
 * <p>
 * Both the runtime complexity and space complexity of the algorithm implemented in this class is
 * $\mathcal{O}(|E|^2)$.<br>
 * The inspected {@code graph} is specified at the construction time and cannot be modified. When
 * the graph is modified externally, the behavior of the {@code WeakChordalityInspector} is
 * undefined.
 * <p>
 * In the case the inspected graph in not weakly chordal, this inspector provides a certificate in
 * the form of some <a href = "http://graphclasses.org/smallgraphs.html#holes">hole</a> or
 * <a href="http://graphclasses.org/smallgraphs.html#antiholes">anti-hole</a>. The running time of
 * finding a hole is $\mathcal{O}(|V| + |E|)$ and of finding an anti-hole - $\mathcal{O}(|E|^2)$ in
 * the worst case.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 */
public class WeakChordalityInspector<V, E>
{
    /**
     * Vertex number
     */
    private final int n;
    /**
     * Edge number
     */
    private final int m;
    /**
     * The inspected graph
     */
    private Graph<V, E> graph;
    /**
     * Bijective mapping of vertices onto $\left[0,n-1\right]$
     */
    private Map<V, Integer> vertices;
    /**
     * Inverse of the bijective mapping of vertices onto $\left[0,n-1\right]$
     */
    private List<V> indices;
    /**
     * Contains true if the graph is weakly chordal, otherwise false. Is null before the first call
     * to the {@link WeakChordalityInspector#isWeaklyChordal()}.
     */
    private Boolean weaklyChordal = null;
    /**
     * Contains a hole or an anti-hole of the graph, if it isn't weakly chordal
     */
    private GraphPath<V, E> certificate;

    /**
     * Creates a weak chordality inspector for the {@code graph}
     *
     * @param graph the inspected {@code graph}
     */
    public WeakChordalityInspector(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph);
        if (graph.getType().isDirected()) {
            this.graph = new AsUndirectedGraph<>(graph);
        }
        n = graph.vertexSet().size();
        m = graph.edgeSet().size();
        initMappings();
    }

    /**
     * Initializes the mappings of the vertices
     */
    private void initMappings()
    {
        VertexToIntegerMapping<V> mapping = new VertexToIntegerMapping<>(graph.vertexSet());
        vertices = mapping.getVertexMap();
        indices = mapping.getIndexList();
    }

    /**
     * Check whether the inspected {@code graph} is weakly chordal. Note: this value is computed
     * lazily.
     *
     * @return true, if the inspected {@code graph} is weakly chordal, otherwise false.
     */
    public boolean isWeaklyChordal()
    {
        return lazyComputeWeakChordality();
    }

    /**
     * Computes and returns the certificate in the form of a hole or anti-hole in the inspected
     * {@code graph}. Returns null if the inspected graph is weakly chordal. Note: certificate is
     * computed lazily.
     *
     * @return a <a href="http://graphclasses.org/smallgraphs.html#holes">hole</a> or
     *         <a href="http://graphclasses.org/smallgraphs.html#antiholes">anti-hole</a> in the
     *         inspected {@code graph}, null if the {@code graph} is weakly chordal
     */
    public GraphPath<V, E> getCertificate()
    {
        lazyComputeWeakChordality();
        return certificate;
    }

    /**
     * Lazily tests the weak chordality of the {@code graph} and returns the computed value.
     *
     * @return true, if the inspected {@code graph} is weakly chordal, otherwise false.
     */
    private boolean lazyComputeWeakChordality()
    {
        if (weaklyChordal == null) {
            List<Pair<List<Pair<Integer, Integer>>, E>> globalSeparatorList =
                computeGlobalSeparatorList();

            if (globalSeparatorList.size() > 0) {
                Pair<Integer, Integer> pair;
                sortSeparatorsList(globalSeparatorList);

                // Iterating over separators. Computing coconnected components only for distinct
                // separators
                int separatorsNum = 1;
                List<Pair<Integer, Integer>> original = globalSeparatorList.get(0).getFirst();
                List<List<Integer>> coConnectedComponents =
                    computeCoConnectedComponents(graph, original);

                for (Pair<List<Pair<Integer, Integer>>, E> separator : globalSeparatorList) {
                    if (unequalSeparators(original, separator.getFirst())) {
                        original = separator.getFirst();
                        ++separatorsNum;
                        if (n + m < separatorsNum) {
                            return weaklyChordal = false;
                        } else {
                            coConnectedComponents = computeCoConnectedComponents(graph, original);
                        }
                    }
                    if ((pair = checkLabels(coConnectedComponents, separator.getFirst())) != null) {
                        // Found a pair of vertices which has labels 1 and 2. This means the graph
                        // isn't weakly chordal. Start detecting a hole
                        E holeFormer = separator.getSecond();
                        V source = graph.getEdgeSource(holeFormer);
                        V target = graph.getEdgeTarget(holeFormer);

                        V sourceInSeparator = indices.get(pair.getFirst());
                        V targetInSeparator = indices.get(pair.getSecond());

                        if (!graph.containsEdge(source, sourceInSeparator)) {
                            V t = sourceInSeparator;
                            sourceInSeparator = targetInSeparator;
                            targetInSeparator = t;
                        }
                        if (graph.containsEdge(sourceInSeparator, targetInSeparator)) {
                            findAntiHole(source, targetInSeparator);
                        } else {
                            findHole(sourceInSeparator, source, target, targetInSeparator);
                        }
                        return weaklyChordal = false;
                    }
                }

                return weaklyChordal = true;
            } else {

                return weaklyChordal = true;
            }
        }
        return weaklyChordal;
    }

    /**
     * Computes the global separator list of the {@code graph}. More precisely, for every edge $e$
     * in the $G = (V, E)$ computes list of minimal separators $S_e$ in the neighborhood of $e$ and
     * then concatenates these lists. Note: the result may contain duplicates
     *
     * @return the list of minimal separators of every edge $e$ in the inspected graph
     */
    private List<Pair<List<Pair<Integer, Integer>>, E>> computeGlobalSeparatorList()
    {
        List<Pair<List<Pair<Integer, Integer>>, E>> globalSeparatorList = new ArrayList<>();
        for (E edge : graph.edgeSet()) {
            V source = graph.getEdgeSource(edge);
            V target = graph.getEdgeTarget(edge);
            if (source != target) {
                List<Set<V>> edgeSeparators = findSeparators(graph, edge);
                globalSeparatorList.addAll(reformatSeparatorList(edgeSeparators, edge));
            }
        }
        return globalSeparatorList;
    }

    /**
     * Reformats the list o {@code separators} so that is can be conveniently used by this
     * inspector. More precisely, in every separator from the list of minimal separators in the
     * neighborhood of the {@code edge} substitutes all vertices for their indices in the numeration
     * defined by {@code vertices}. Pairs every separator with the {@code edge}.
     *
     * @param separators the list of minimal separators in the neighborhood of the {@code edge}
     * @param edge the edge, which neighborhood contains minimal separators from {@code separators}
     * @return the reformatted list of minimal separators
     */
    private List<Pair<List<Pair<Integer, Integer>>, E>> reformatSeparatorList(
        List<Set<V>> separators, E edge)
    {
        List<Integer> labeling = getLabeling(edge);
        List<Pair<List<Pair<Integer, Integer>>, E>> reformattedSeparators = new ArrayList<>();
        List<List<List<Pair<Integer, Integer>>>> vInSeparator = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            vInSeparator.add(new ArrayList<>());
        }

        for (Set<V> computedSeparator : separators) {
            List<Pair<Integer, Integer>> reformattedSeparator =
                new ArrayList<>(computedSeparator.size());
            reformattedSeparators.add(new Pair<>(reformattedSeparator, edge));
            for (V vertex : computedSeparator) {
                int vertexIndex = vertices.get(vertex);
                vInSeparator.get(vertexIndex).add(reformattedSeparator);
            }
        }

        for (int vertex = 0; vertex < n; vertex++) {
            List<List<Pair<Integer, Integer>>> listOfSeparators = vInSeparator.get(vertex);
            for (List<Pair<Integer, Integer>> separator : listOfSeparators) {
                separator.add(new Pair<>(vertex, labeling.get(vertex)));
            }
        }

        return reformattedSeparators;

    }

    /**
     * Computes the labeling of the neighborhood of {@code edge} on the vertices {@code source} and
     * {@code target}. Vertex from the neighborhood is labeled with "1" if it sees only
     * {@code source}, "2" is it sees only {@code target}, and "3" if it sees both vertices.
     *
     * @param edge the edge, whose neighborhood is to be labeled
     * @return the computed labeling with the respect to the rule described above
     */
    private List<Integer> getLabeling(E edge)
    {
        V source = graph.getEdgeSource(edge);
        V target = graph.getEdgeTarget(edge);
        List<Integer> labeling = new ArrayList<>(Collections.nCopies(n, null));
        for (E sourceEdge : graph.edgesOf(source)) {
            labeling.set(vertices.get(Graphs.getOppositeVertex(graph, sourceEdge, source)), 1);
        }
        for (E targetEdge : graph.edgesOf(target)) {
            Integer oppositeIndex =
                vertices.get(Graphs.getOppositeVertex(graph, targetEdge, target));
            if (labeling.get(oppositeIndex) != null) {
                labeling.set(oppositeIndex, 3);
            } else {
                labeling.set(oppositeIndex, 2);
            }
        }
        return labeling;
    }

    /**
     * Sorts the {@code separators} using bucket sort
     *
     * @param separators the list of separators to be sorted
     */
    private void sortSeparatorsList(List<Pair<List<Pair<Integer, Integer>>, E>> separators)
    {
        Queue<Pair<List<Pair<Integer, Integer>>, E>> mainQueue = new ArrayDeque<>();
        int maxSeparatorLength = 0;
        for (Pair<List<Pair<Integer, Integer>>, E> separator : separators) {
            if (separator.getFirst().size() > maxSeparatorLength) {
                maxSeparatorLength = separator.getFirst().size();
            }
            mainQueue.add(separator);
        }
        separators.clear();
        List<Queue<Pair<List<Pair<Integer, Integer>>, E>>> queues = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            queues.add(new LinkedList<>());
        }
        for (int i = 0; i < maxSeparatorLength; i++) {
            while (!mainQueue.isEmpty()) {
                Pair<List<Pair<Integer, Integer>>, E> separator = mainQueue.remove();
                if (i >= separator.getFirst().size()) {
                    separators.add(separator);
                } else {
                    queues
                        .get(
                            separator
                                .getFirst().get(separator.getFirst().size() - i - 1).getFirst())
                        .add(separator);
                }
            }
            for (Queue<Pair<List<Pair<Integer, Integer>>, E>> queue : queues) {
                mainQueue.addAll(queue);
                queue.clear();
            }
        }
        separators.addAll(mainQueue);
    }

    /**
     * Compares two separators for inequality. Labeling of the vertices in the separators isn't
     * considered
     *
     * @param sep1 first separator
     * @param sep2 second separator
     * @return true, if the separators are unequal, false otherwise
     */
    private boolean unequalSeparators(
        List<Pair<Integer, Integer>> sep1, List<Pair<Integer, Integer>> sep2)
    {
        if (sep1.size() == sep2.size()) {
            for (int i = 0; i < sep1.size(); i++) {
                if (!sep2.get(i).getFirst().equals(sep1.get(i).getFirst())) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Computes the connected components of the complement of the graph induces by the vertices of
     * the {@code separator}. They are also called "coconnected components". The running time is
     * $\mathcal{O}(|V| + |E|)$.
     *
     * @param separator the separators, whose coconnected components are computed
     * @return the coconected of the {@code separator}
     */
    private List<List<Integer>> computeCoConnectedComponents(
        Graph<V, E> graph, List<Pair<Integer, Integer>> separator)
    {
        List<List<Integer>> coConnectedComponents = new ArrayList<>();

        // Initializing buckets, labels and set of unvisited vertices. Every vertex in separator is
        // put
        // to bucket with label 0
        List<Set<Integer>> bucketsByLabel = new ArrayList<>(separator.size());
        for (int i = 0; i < separator.size(); i++) {
            bucketsByLabel.add(new HashSet<>());
        }
        List<Integer> labels = new ArrayList<>(Collections.nCopies(n, -1));
        Set<Integer> unvisited = CollectionUtil.newHashSetWithExpectedSize(separator.size());
        separator.forEach(pair -> {
            unvisited.add(pair.getFirst());
            labels.set(pair.getFirst(), 0);
        });
        bucketsByLabel.set(0, unvisited);
        int minLabel = 0;

        while (unvisited.size() > 0) {
            List<Integer> coConnectedComponent = new ArrayList<>();
            do {
                // When minLabel = coConnectedComponent.size(), we've visited all vertices in some
                // coconnected component in the separator. If there exist unvisited vertices, we
                // start again
                while (!bucketsByLabel.get(minLabel).isEmpty()) {
                    Integer vertex = bucketsByLabel.get(minLabel).iterator().next();
                    bucketsByLabel.get(minLabel).remove(vertex);
                    coConnectedComponent.add(vertex);
                    labels.set(vertex, -1);

                    for (E edge : graph.edgesOf(indices.get(vertex))) {
                        Integer opposite = vertices
                            .get(Graphs.getOppositeVertex(graph, edge, indices.get(vertex)));
                        Integer oppositeLabel = labels.get(opposite);
                        if (oppositeLabel != -1) {
                            putToNextBucket(opposite, oppositeLabel, bucketsByLabel, labels);
                        }
                    }
                }
                ++minLabel;
            } while (minLabel != coConnectedComponent.size());
            reload(bucketsByLabel, labels, minLabel);

            coConnectedComponents.add(coConnectedComponent);
            minLabel = 0;
        }
        return coConnectedComponents;
    }

    /**
     * Moves the {@code vertex} to the next bucket.
     *
     * @param vertex the vertex to be moved
     * @param vertexLabel the label of the {@code vertex}
     * @param bucketsByLabel the buckets, in which vertices are stored
     * @param labels the labels of the vertices
     */
    private void putToNextBucket(
        Integer vertex, Integer vertexLabel, List<Set<Integer>> bucketsByLabel,
        List<Integer> labels)
    {
        bucketsByLabel.get(vertexLabel).remove(vertex);
        bucketsByLabel.get(vertexLabel + 1).add(vertex);
        labels.set(vertex, vertexLabel + 1);
    }

    /**
     * Moves all vertices from the bucket with label {@code minLabel} to the bucket with label 0.
     * Clears the bucket with label {@code minLabel}. Updates the labeling accordingly.
     *
     * @param bucketsByLabel the buckets vertices are stored in
     * @param labels the labels of the vertices
     * @param minLabel the minimum value of the non-empty bucket
     */
    private void reload(List<Set<Integer>> bucketsByLabel, List<Integer> labels, int minLabel)
    {
        if (minLabel != 0 && minLabel < bucketsByLabel.size()) {
            Set<Integer> bucket = bucketsByLabel.get(minLabel);
            for (Integer vertex : bucket) {
                labels.set(vertex, 0);
                bucketsByLabel.get(0).add(vertex);
            }
            bucket.clear();
        }
    }

    /**
     * For a given coconnected component of the {@code separator} checks whether every vertex in it
     * is seen by al least one vertex of the edge that is separated by the {@code separator}
     *
     * @param coConnectedComponents the set of the coconected components of the {@code separator}
     * @param separator minimal separator of some edge in the {@code graph}
     * @return true if the condition described above holds, false otherwise
     */
    private Pair<Integer, Integer> checkLabels(
        List<List<Integer>> coConnectedComponents, List<Pair<Integer, Integer>> separator)
    {
        List<Integer> vertexLabels = new ArrayList<>(Collections.nCopies(n, null));
        for (Pair<Integer, Integer> vertexAndLabel : separator) {
            vertexLabels.set(vertexAndLabel.getFirst(), vertexAndLabel.getSecond());
        }
        for (List<Integer> coConnectedComponent : coConnectedComponents) {
            int label = 0;
            Integer labelVertex = null;
            for (Integer vertex : coConnectedComponent) {
                if (vertexLabels.get(vertex) != 3) {
                    if (label != 0) {
                        if (label != vertexLabels.get(vertex)) {
                            return new Pair<>(labelVertex, vertex);
                        }
                    } else {
                        label = vertexLabels.get(vertex);
                        labelVertex = vertex;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds a hole in the inspected {@code graph}. Vertices {@code sourceInSeparator},
     * {@code source}, {@code target} and {@code targetInSeparator} belong to the computes hole.
     * They are used to correctly find a hole in the inspected graph.
     *
     * @param sourceInSeparator vertex on the hole
     * @param source vertex on the hole
     * @param target vertex on the hole
     * @param targetInSeparator vertex on the hole
     */
    private void findHole(V sourceInSeparator, V source, V target, V targetInSeparator)
    {
        this.certificate = findHole(graph, sourceInSeparator, source, target, targetInSeparator);
    }

    /**
     * Finds an anti-hole in the inspected {@code graph}. Vertices {@code source} and
     * {@code targetInSeparator} specify an edge, which belongs to the anti-hole in the complement
     * of the {@code graph}. Then the hole in the complement of the graph is computed in the graph's
     * complement in the same way a hole is computed in the {@code graph}.
     *
     * @param source endpoint of the edge that belongs to the anti-hole
     * @param targetInSeparator endpoint of the edge that belongs to the anti-hole
     */
    private void findAntiHole(V source, V targetInSeparator)
    {
        // Generating the complement of the inspected graph
        ComplementGraphGenerator<V, E> generator = new ComplementGraphGenerator<>(graph, false);
        Graph<V, E> complement = Pseudograph.<V, E> createBuilder(graph.getEdgeSupplier()).build();
        generator.generateGraph(complement);

        E cycleFormer = complement.getEdge(source, targetInSeparator);
        V cycleSource = graph.getEdgeSource(cycleFormer);
        V cycleTarget = graph.getEdgeTarget(cycleFormer);

        // For edge cycleFormer we need to find the separator, which contains vertices with labels 1
        // and 2
        // After that the procedure of detecting a hole in the complement of the graph is identical
        // to finding a hole in the graph itself
        List<Set<V>> separators = findSeparators(complement, cycleFormer);
        List<Pair<List<Pair<Integer, Integer>>, E>> reformatted =
            reformatSeparatorList(separators, cycleFormer);

        sortSeparatorsList(reformatted);

        List<Pair<Integer, Integer>> original = reformatted.get(0).getFirst();
        List<List<Integer>> coConnectedComponents =
            computeCoConnectedComponents(complement, original);

        Pair<Integer, Integer> pair;
        for (Pair<List<Pair<Integer, Integer>>, E> separator : reformatted) {
            if (unequalSeparators(separator.getFirst(), original)) {
                original = separator.getFirst();
                coConnectedComponents =
                    computeCoConnectedComponents(complement, separator.getFirst());
            }
            if ((pair = checkLabels(coConnectedComponents, separator.getFirst())) != null) {
                // Found a pair of vertices with labels 1 and 2
                V cycleSourceInSeparator = indices.get(pair.getFirst());
                V cycleTargetInSeparator = indices.get(pair.getSecond());
                if (!complement.containsEdge(cycleSourceInSeparator, cycleSource)) {
                    V t = cycleSourceInSeparator;
                    cycleSourceInSeparator = cycleTargetInSeparator;
                    cycleTargetInSeparator = t;
                }
                this.certificate = findHole(
                    complement, cycleSourceInSeparator, cycleSource, cycleTarget,
                    cycleTargetInSeparator);
                return;
            }
        }
    }

    /**
     * Finds a hole in the specified {@code graph}. Vertices {@code sourceInSeparator},
     * {@code source}, {@code target} and {@code targetInSeparator} belong to the computes hole.
     * They are used to correctly find a hole in the specified {@code graph}.
     *
     * @param sourceInSeparator vertex on the hole
     * @param source vertex on the hole
     * @param target vertex on the hole
     * @param targetInSeparator vertex on the hole
     * @return the computed hole on the {@code graph}
     */
    private GraphPath<V, E> findHole(
        Graph<V, E> graph, V sourceInSeparator, V source, V target, V targetInSeparator)
    {
        Set<V> visited = CollectionUtil.newHashSetWithExpectedSize(graph.vertexSet().size());
        visited.add(target);
        visited.add(source);

        // Obtaining some cycle, which can be minimized to a hole
        List<V> cycle =
            findCycle(visited, graph, targetInSeparator, target, source, sourceInSeparator);
        cycle = minimizeCycle(graph, cycle, target, targetInSeparator, source, sourceInSeparator);

        return new GraphWalk<>(graph, cycle, 0);
    }

    /**
     * Starts the iterative depth-first traversal from {@code sourInSep} vertex. Tries to build a
     * cycle with the vertices, which aren't adjacent to the {@code tar} and {@code sour}. This
     * condition is used in order to ensure that the cycle contains a hole, to which it is later
     * minimized.
     *
     * @param visited defines which vertices have been visited already
     * @param graph the graph the search is performed on
     * @param tarInSep the end point of the cycle
     * @param tar the vertex, which can't be adjacent to the vertices in the cycle
     * @param sour the vertex, which can't be adjacent to the vertices in the cycle
     * @param sourInSep the vertex the search is started from
     * @return the computed cycle, which contains a hole
     */
    private List<V> findCycle(
        Set<V> visited, Graph<V, E> graph, V tarInSep, V tar, V sour, V sourInSep)
    {
        List<V> cycle = new ArrayList<>(Arrays.asList(tarInSep, tar, sour));
        Deque<V> stack = new ArrayDeque<>();
        stack.add(sourInSep);

        while (!stack.isEmpty()) {
            V currentVertex = stack.removeLast();
            if (visited.add(currentVertex)) {

                // trying to advance cycle from current vertex
                // removing all vertices from the head of the cycle, which aren't adjacent to the
                // current vertex
                while (!graph.containsEdge(cycle.get(cycle.size() - 1), currentVertex)) {
                    cycle.remove(cycle.size() - 1);
                }
                cycle.add(currentVertex);
                if (tarInSep.equals(currentVertex)) {
                    // the cycle is complete
                    break;
                } else {
                    for (V neighbor : Graphs.neighborListOf(graph, currentVertex)) {
                        // add a vertex to the stack if it hasn't been visited yet and it isn't
                        // adjacent to the
                        // source vertex and (it isn't adjacent to the target vertex or it is
                        // targetInSeparator (the end of the cycle))
                        if (!visited.contains(neighbor) && !graph.containsEdge(sour, neighbor)
                            && (!graph.containsEdge(tar, neighbor) || neighbor.equals(tarInSep)))
                        {
                            stack.add(neighbor);
                        }
                    }
                }
            }
        }
        return cycle;
    }

    /**
     * Minimizes the {@code cycle} so that it contains a hole in the {@code graph}. Vertices
     * {@code tar}, {@code tarInSep}, {@code sour} and {@code sourInSep} belong to the final result.
     *
     * @param graph the graph, which contains vertices from {@code cycle}
     * @param cycle the cycle to minimize
     * @param tar vertex, which should belong to the final result
     * @param tarInSep vertex, which should belong to the final result
     * @param sour vertex, which should belong to the final result
     * @param sourInSep vertex, which should belong to the final result
     * @return a list of vertices, which defines a hole in the {@code graph}
     */
    private List<V> minimizeCycle(
        Graph<V, E> graph, List<V> cycle, V tar, V tarInSep, V sour, V sourInSep)
    {
        List<V> minimizedCycle = new ArrayList<>(Arrays.asList(tarInSep, tar, sour));
        Set<V> forwardVertices = new HashSet<>(cycle);
        forwardVertices.remove(tar);
        forwardVertices.remove(sour);
        forwardVertices.remove(sourInSep);

        for (int i = 3; i < cycle.size() - 1;) {
            V current = cycle.get(i);
            minimizedCycle.add(current);
            forwardVertices.remove(current);

            // Computing a set of vertices, which are adjacent to current and have greater index
            // in the cycle than current
            Set<V> currentForward = new HashSet<>();
            for (V neighbor : Graphs.neighborListOf(graph, current)) {
                if (forwardVertices.contains(neighbor)) {
                    currentForward.add(neighbor);
                }
            }

            // Jump to the forward vertex with the greatest index. Therefore we ensure, that
            // the resulting cycle doesn't contain chords
            for (V forwardVertex : currentForward) {
                if (forwardVertices.contains(forwardVertex)) {
                    do {
                        forwardVertices.remove(cycle.get(i));
                        i++;
                    } while (i < cycle.size() && !cycle.get(i).equals(forwardVertex));
                }
            }
        }
        minimizedCycle.add(tarInSep);
        return minimizedCycle;
    }

    /**
     * Computes and returns all minimal separators in the neighborhood of the {@code edge} in the
     * {@code graph}. The result may contain duplicate separators.
     *
     * @param graph the graph to search minimal separators in
     * @param edge the edge, whose neighborhood is being explored
     * @return the list of all minimal separators in the neighborhood of the {@code edge}. The
     *         resulted list may contain duplicates.
     */
    private List<Set<V>> findSeparators(Graph<V, E> graph, E edge)
    {
        List<Set<V>> separators = new ArrayList<>();
        V source = graph.getEdgeSource(edge);
        V target = graph.getEdgeTarget(edge);
        Set<V> neighborhood = neighborhoodSetOf(graph, edge);
        Map<V, Byte> dfsMap = CollectionUtil.newHashMapWithExpectedSize(graph.vertexSet().size());

        // 0 - unvisited (white), 1 - neighbor of the edge (red), 2 - visited (black)
        for (V vertex : graph.vertexSet()) {
            if (neighborhood.contains(vertex)) {
                dfsMap.put(vertex, (byte) 1);
            } else {
                dfsMap.put(vertex, (byte) 0);
            }
        }
        dfsMap.put(source, (byte) 2);
        dfsMap.put(target, (byte) 2);

        for (V vertex : graph.vertexSet()) {
            if (dfsMap.get(vertex) == 0) {
                // possible to find one more separator
                Set<V> separator = getSeparator(graph, vertex, dfsMap);
                if (!separator.isEmpty()) {
                    separators.add(separator);
                }
            }
        }

        return separators;
    }

    /**
     * Performs iterative depth-first search starting from the {@code startVertex} in the
     * {@code graph}. Adds every encountered red vertex to the resulting separator. Doesn't process
     * red vertices. Marks all white vertices with black color.
     *
     * @param graph the graph dfs is performed on
     * @param startVertex the vertex to start depth-first traversal from
     * @param dfsMap the depth-first vertex labeling
     * @return the computed separator, which consists of all encountered red vertices
     */
    private Set<V> getSeparator(Graph<V, E> graph, V startVertex, Map<V, Byte> dfsMap)
    {
        Deque<V> stack = new ArrayDeque<>();
        Set<V> separator = new HashSet<>();
        stack.add(startVertex);

        while (!stack.isEmpty()) {
            V currentVertex = stack.removeLast();
            if (dfsMap.get(currentVertex) == 0) {
                dfsMap.put(currentVertex, (byte) 2);
                for (E edge : graph.edgesOf(currentVertex)) {
                    V opposite = Graphs.getOppositeVertex(graph, edge, currentVertex);
                    if (dfsMap.get(opposite) == 0) {
                        stack.add(opposite);
                    } else if (dfsMap.get(opposite) == 1) {
                        separator.add(opposite); // found red vertex, which belongs to the separator
                    }
                }
            }
        }

        return separator;
    }

    /**
     * Returns a set of vertices that are neighbors of the source of the specified edge or of the
     * target of specified edge. The endpoints of the specified edge aren't included in the result.
     *
     * @param g the graph to look for neighbors in
     * @param edge the edge to get the neighbors of
     * @return a set of vertices that are neighbors of at least one endpoint of the specified edge.
     *         The endpoints of the specified edge aren't included in the result
     */
    private Set<V> neighborhoodSetOf(Graph<V, E> g, E edge)
    {
        Set<V> neighborhood = new HashSet<>();

        V source = g.getEdgeSource(edge);
        V target = g.getEdgeTarget(edge);

        for (E e : g.edgesOf(source)) {
            neighborhood.add(Graphs.getOppositeVertex(g, e, source));
        }
        for (E e : g.edgesOf(target)) {
            neighborhood.add(Graphs.getOppositeVertex(g, e, target));
        }
        neighborhood.remove(source);
        neighborhood.remove(target);

        return neighborhood;
    }
}
