package org.jgrapht.alg.isomorphism;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.generate.BarabasiAlbertForestGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.jgrapht.alg.isomorphism.AHUTreeIsomorphismTest.*;

public class AHUForestIsomorphismTest {

    public static Graph<Integer, DefaultEdge> generateForest(int N, Random random){
        BarabasiAlbertForestGenerator<Integer, DefaultEdge> generator =
                new BarabasiAlbertForestGenerator<>(N / 10, N, random);

        Graph<Integer, DefaultEdge> forest = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        generator.generateGraph(forest);

        return forest;
    }

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
                new AHUForestIsomorphism<>(tree1, new HashSet<>(Arrays.asList("b", "d")),
                        tree2, new HashSet<>(Arrays.asList("A", "D")));

        Assert.assertFalse(forestIsomorphism.isomorphismExists());
    }

    @Test
    public void testUnrootedForest(){
        /*
            If we take two forests and take the first one in each connected component as the root the following
            test will return non-isomorphic

            ([0, 1, 2, 3, 4, 5, 6], [{3,2}, {4,3}, {5,3}, {6,4}])
            [0, 1, 2]

            ([3, 6, 1, 5, 2, 4, 0], [{5,1}, {2,5}, {4,5}, {0,2}])
            [1, 3, 6]
         */
    }

    public static Graph<Integer, DefaultEdge> parseGraph(String vertices, String edges){
        Graph<Integer, DefaultEdge> forest = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        vertices = vertices.substring(1, vertices.length() - 1);

        for (String s: vertices.split(", "))
            forest.addVertex(Integer.valueOf(s));

        edges = edges.substring(1, edges.length() - 1);

        for (String s: edges.split(", ")){
            String[] ends = s.substring(1, s.length() - 1).split(",");
            forest.addEdge(Integer.valueOf(ends[0]), Integer.valueOf(ends[1]));
        }

        return forest;
    }

    Pair<Graph<Integer, DefaultEdge>, Graph<Integer, DefaultEdge>> parseGraph(String vertices, String edges,
                                                                            String mapping, Map<Integer, Integer> map){

        Graph<Integer, DefaultEdge> forest = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        vertices = vertices.substring(1, vertices.length() - 1);

        for (String s: vertices.split(", "))
            forest.addVertex(Integer.valueOf(s));

        edges = edges.substring(1, edges.length() - 1);

        for (String s: edges.split(", ")){
            String[] ends = s.substring(1, s.length() - 1).split(",");
            forest.addEdge(Integer.valueOf(ends[0]), Integer.valueOf(ends[1]));
        }

        for (String s: mapping.substring(1, mapping.length() - 1).split(", ")){
            String[] ends = s.split("=");
            map.put(Integer.valueOf(ends[0]), Integer.valueOf(ends[1]));
        }

        return Pair.of(forest, generateMappedGraph(forest, map));

//        ([0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19], [{2,1}, {3,0}, {4,0}, {5,1}, {6,1}, {7,0}, {8,1}, {9,6}, {10,1}, {11,6}, {12,0}, {13,7}, {14,5}, {15,1}, {16,0}, {17,0}, {18,17}, {19,7}])
//        ([12, 10, 0, 8, 3, 16, 7, 18, 11, 17, 6, 14, 9, 5, 15, 2, 19, 13, 4, 1], [{0,10}, {8,12}, {3,12}, {16,10}, {7,10}, {18,12}, {11,10}, {17,7}, {6,10}, {14,7}, {9,12}, {5,18}, {15,16}, {2,10}, {19,12}, {13,12}, {4,13}, {1,18}])
//        {0=12, 1=10, 2=0, 3=8, 4=3, 5=16, 6=7, 7=18, 8=11, 9=17, 10=6, 11=14, 12=9, 13=5, 14=15, 15=2, 16=19, 17=13, 18=4, 19=1}
    }

    @Test
    public void test(){
        Map<Integer, Integer> map = new HashMap<>();

        Pair<Graph<Integer, DefaultEdge>, Graph<Integer, DefaultEdge>> pair =
                parseGraph("[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19]",
                "[{2,1}, {3,0}, {4,0}, {5,1}, {6,1}, {7,0}, {8,1}, {9,6}, {10,1}, {11,6}, " +
                        "{12,0}, {13,7}, {14,5}, {15,1}, {16,0}, {17,0}, {18,17}, {19,7}]",
                "{0=12, 1=10, 2=0, 3=8, 4=3, 5=16, 6=7, 7=18, 8=11, 9=17, 10=6, 11=14, 12=9, " +
                        "13=5, 14=15, 15=2, 16=19, 17=13, 18=4, 19=1}", map);

        Graph<Integer, DefaultEdge> forest1 = pair.getFirst();
        Graph<Integer, DefaultEdge> forest2 = pair.getSecond();

        Set<Integer> roots1 = new ConnectivityInspector<>(forest1).connectedSets()
                .stream().map(x -> x.iterator().next()).collect(Collectors.toSet());

        Set<Integer> roots2 = roots1.stream().map(map::get).collect(Collectors.toSet());

        AHUForestIsomorphism<Integer, DefaultEdge> isomorphism =
                new AHUForestIsomorphism<>(forest1, roots1, forest2, roots2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicTreeMapping<Integer, DefaultEdge> treeMapping = isomorphism.getIsomorphism();

        Assert.assertTrue(areIsomorphic(forest1, forest2, treeMapping));
    }

    @Test
    public void testRandomForests(){
        Random random = new Random(0x2312);
        final int NUM_TESTS = 1000;

        for (int test = 0; test < NUM_TESTS; test++) {
            final int N = 10 + random.nextInt(200);

            Graph<Integer, DefaultEdge> tree1 = generateForest(N, random);

            Pair<Graph<Integer, DefaultEdge>, Map<Integer, Integer>> pair = generateIsomorphicGraph(tree1, random);

            Graph<Integer, DefaultEdge> tree2 = pair.getFirst();

            Set<Integer> roots1 = new ConnectivityInspector<>(tree1).connectedSets()
                    .stream().map(x -> x.iterator().next()).collect(Collectors.toSet());

            Set<Integer> roots2 = roots1.stream().map(x -> pair.getSecond().get(x)).collect(Collectors.toSet());

            AHUForestIsomorphism<Integer, DefaultEdge> isomorphism =
                    new AHUForestIsomorphism<>(tree1, roots1, tree2, roots2);

            Assert.assertTrue(isomorphism.isomorphismExists());
            IsomorphicTreeMapping<Integer, DefaultEdge> treeMapping = isomorphism.getIsomorphism();

            Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
        }
    }
}