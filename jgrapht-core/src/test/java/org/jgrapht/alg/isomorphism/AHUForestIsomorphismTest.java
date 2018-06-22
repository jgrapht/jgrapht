package org.jgrapht.alg.isomorphism;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

public class AHUForestIsomorphismTest {

    @Test(expected = IllegalArgumentException.class)
    public void testMissingSupplier(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        tree1.addVertex("1");
        tree1.addVertex("2");
        tree1.addEdge("1", "2");
        tree1.addVertex("3");

        AHUForestIsomorphism<String, DefaultEdge> forestIsomorphism =
                new AHUForestIsomorphism<>(tree1, new HashSet<>(Arrays.asList("1", "2")),
                        tree1, new HashSet<>(Arrays.asList("1", "2")));

        forestIsomorphism.isomorphismExists();
    }

    @Test
    public void testSmallForest(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(SupplierUtil.createStringSupplier(),
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        tree1.addVertex("a");
        tree1.addVertex("b");
        tree1.addVertex("c");

        tree1.addEdge("a", "b");
        tree1.addEdge("a", "c");

        tree1.addVertex("d");

        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(SupplierUtil.createStringSupplier(),
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        tree2.addVertex("A");
        tree2.addVertex("B");
        tree2.addVertex("C");


        tree2.addEdge("B", "A");
        tree2.addEdge("A", "C");

        tree2.addVertex("D");

        AHUForestIsomorphism<String, DefaultEdge> forestIsomorphism =
                new AHUForestIsomorphism<>(tree1, "b", tree2, "A");

        System.out.println(forestIsomorphism.isomorphismExists());
        System.out.println(forestIsomorphism.getIsomorphism());
    }

//    @Test
//    public void testRandomForests(){
//        Random random = new Random(0x88_88);
//        final int NUM_TESTS = 1500;
//
//        for (int test = 0; test < NUM_TESTS; test++) {
//            final int N = 10 + random.nextInt(100);
//
//            Graph<Integer, DefaultEdge> tree1 = generateTree(N, random);
//
//            Pair<Graph<Integer, DefaultEdge>, Map<Integer, Integer>> pair = generateIsomorphismTree(tree1, random);
//
//            Graph<Integer, DefaultEdge> tree2 = pair.getFirst();
//
//            AHUTreeIsomorphism<Integer, DefaultEdge> isomorphism =
//                    new AHUTreeIsomorphism<>(tree1, tree2);
//
//            Assert.assertTrue(isomorphism.isomorphismExists());
//            IsomorphicTreeMapping<Integer, DefaultEdge> treeMapping = isomorphism.getIsomorphism();
//            Assert.assertTrue(areIdentical(tree1, generatedMappedTree(tree2, treeMapping.getBackwardMapping())));
//        }
//    }
}