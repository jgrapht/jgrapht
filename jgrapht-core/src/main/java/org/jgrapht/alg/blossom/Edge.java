package org.jgrapht.alg.blossom;

public class Edge implements Comparable<Edge> {
    private double slack;

    private Node tailOriginal;
    private Node headOriginal;
    private Node tail;
    private Node head;

    @Override
    public int compareTo(Edge o) {
        return Double.compare(slack, o.slack);
    }
}
