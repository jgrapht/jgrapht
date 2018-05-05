package org.jgrapht.alg.blossom;

import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;
import org.jgrapht.util.GenericFibonacciHeap;

public class Test {
    public static void main(String[] args) {
        int n = 800 - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        System.out.println((n < 0) ? 1 : n + 1);

        GenericFibonacciHeap<Integer, Integer> heap = new GenericFibonacciHeap<>(Integer::compare);
    }
}
