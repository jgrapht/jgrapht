package org.jgrapht.traverse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;

public class NewRandomWalkIterator<V, E>
    implements
    Iterator<V>
{
    private final Random rng;
    private final Graph<V, E> graph;
    private final boolean weighted;
    private final long maxHops;
    private long hops;
    private V nextVertex;

    public NewRandomWalkIterator(Graph<V, E> graph, V vertex)
    {
        this(graph, vertex, Long.MAX_VALUE, false, new Random());
    }

    public NewRandomWalkIterator(Graph<V, E> graph, V vertex, long maxHops)
    {
        this(graph, vertex, maxHops, false, new Random());
    }

    public NewRandomWalkIterator(
        Graph<V, E> graph, V vertex, long maxHops, boolean weighted, Random rng)
    {
        this.graph = Objects.requireNonNull(graph);
        this.weighted = weighted;
        this.hops = 0;
        this.nextVertex = Objects.requireNonNull(vertex);
        if (!graph.containsVertex(vertex)) {
            throw new IllegalArgumentException("Random walk must start at a graph vertex");
        }
        this.maxHops = maxHops;
        this.rng = rng;
    }

    @Override
    public boolean hasNext()
    {
        return nextVertex != null;
    }

    @Override
    public V next()
    {
        if (nextVertex == null) {
            throw new NoSuchElementException();
        }
        V value = nextVertex;
        computeNext();
        return value;
    }

    private void computeNext()
    {
        if (hops >= maxHops) {
            nextVertex = null;
            return;
        }

        hops++;
        List<E> outEdges = new ArrayList<>(graph.outgoingEdgesOf(nextVertex));
        int count = outEdges.size();
        if (count == 0) {
            nextVertex = null;
            return;
        }

        E e = null;
        if (weighted) {
            double p = outEdges.stream().collect(Collectors.summingDouble(graph::getEdgeWeight))
                * rng.nextDouble();
            double cumulativeP = 0d;
            for (E curEdge : outEdges) {
                cumulativeP += graph.getEdgeWeight(curEdge);
                if (p <= cumulativeP) {
                    e = curEdge;
                    break;
                }
            }
        } else {
            e = outEdges.get(rng.nextInt(count));
        }
        nextVertex = Graphs.getOppositeVertex(graph, e, nextVertex);
    }

}
