package org.jgrapht.alg.blossom;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.graph.AsUndirectedGraph;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class BlossomPerfectMatching<V, E> {
    public static final Options DEFAULT_OPTIONS = new Options(false);
    private final Graph<V, E> graph;
    private int n;
    private int m;
    private Set<Tree> trees;
    private Set<Node> nodes;


    private Statistics statistics;
    private Options options;


    public BlossomPerfectMatching(Graph<V, E> graph) {
        this(graph, DEFAULT_OPTIONS);
    }

    public BlossomPerfectMatching(Graph<V, E> graph, Options options) {
        Objects.requireNonNull(graph);
        if (graph.getType().isDirected()) {
            this.graph = new AsUndirectedGraph<>(graph);
        } else {
            this.graph = graph;
        }
        this.n = graph.vertexSet().size();
        this.m = graph.edgeSet().size();
        this.options = Objects.requireNonNull(options);
    }

    public MatchingAlgorithm.Matching<V, E> solve() {
        init();
        while (!trees.isEmpty()) {
            Tree tree = trees.iterator().next();
            Node root = tree.getRoot();

        }
    }

    private void init() {
        trees = new HashSet<>(n);
        nodes = new HashSet<>(n);
        for (V vertex : graph.vertexSet()) {
            Node root = new Node();
            nodes.add(root);
            trees.add(new Tree(root));
        }
    }


    public static class Options {
        boolean computeDualSolution;

        public Options(boolean computeDualSolution) {
            this.computeDualSolution = computeDualSolution;
        }
    }

    public class Statistics {
        int shrinkNum;
        int expandNum;
        int growNum;

        public int getShrinkNum() {
            return shrinkNum;
        }

        public int getExpandNum() {
            return expandNum;
        }

        public int getGrowNum() {
            return growNum;
        }
    }
}
