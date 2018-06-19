package org.jgrapht.alg.isomorphism;

import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.generate.BarabasiAlbertGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class AHUTreeIsomorphismTest {
    
    private static <V> Graph<V, DefaultEdge> generatedMappedTree(Graph<V, DefaultEdge> tree,
                                                                   Map<V, V> mapping){

        Graph<V, DefaultEdge> isoTree = new SimpleGraph<>(DefaultEdge.class);
        
        for (V v: tree.vertexSet())
            isoTree.addVertex(mapping.get(v));
        
        for (DefaultEdge edge: tree.edgeSet()){
            V u = tree.getEdgeSource(edge);
            V v = tree.getEdgeTarget(edge);
            isoTree.addEdge(mapping.get(u), mapping.get(v));
        }
        
        return isoTree;
    }
    
    private static Pair<Graph<Integer, DefaultEdge>, Map<Integer, Integer>> 
    generateIsomorphismTree(Graph<Integer, DefaultEdge> tree, Random random){
        List<Integer> permutation = new ArrayList<>(tree.vertexSet().size());

        for (int i = 0; i < tree.vertexSet().size(); i++) {
            permutation.add(i);
        }

        Collections.shuffle(permutation, random);
        
        List<Integer> vertexList = new ArrayList<>(tree.vertexSet());
        Map<Integer, Integer> mapping = new HashMap<>();

        for (int i = 0; i < tree.vertexSet().size(); i++) {
            mapping.put(vertexList.get(i), vertexList.get(permutation.get(i)));
        }
        
        return Pair.of(generatedMappedTree(tree, mapping), mapping);
    }

    private static <V, E> boolean containsAllEdges(Graph<V, E> graph1, Graph<V, E> graph2) {
        for (E edge: graph1.edgeSet()){
            V u = graph1.getEdgeSource(edge);
            V v = graph1.getEdgeTarget(edge);

            if (!graph2.containsEdge(u, v)) {
                return false;
            }
        }
        return true;
    }


    private static <V, E> boolean areIdentical(Graph<V, E> graph1, Graph<V, E> graph2){
        if (!graph1.vertexSet().equals(graph2.vertexSet()))
            return false;

        if (!containsAllEdges(graph1, graph2)) return false;

        return containsAllEdges(graph2, graph1);
    }

    private static Graph<Integer, DefaultEdge> generateTree(int N, Random random){
        BarabasiAlbertGraphGenerator<Integer, DefaultEdge> generator =
                new BarabasiAlbertGraphGenerator<>(1, 1, N - 1, random);

        Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        generator.generateGraph(tree);

        return tree;
    }

    @Test
    public void testEmptyGraph(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);

        AHUTreeIsomorphism<String, DefaultEdge> isomorphism =
                new AHUTreeIsomorphism<>(tree1, tree1);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicTreeMapping<String, DefaultEdge> treeMapping = isomorphism.getIsomorphism();
        Assert.assertTrue(areIdentical(tree1, generatedMappedTree(tree1, treeMapping.getBackwardMapping())));
    }

    @Test
    public void testUnrootedIsomorphism(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        tree1.addVertex("1");
        tree1.addVertex("2");
        tree1.addVertex("3");

        tree1.addEdge("1", "2");
        tree1.addEdge("2", "3");

        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(DefaultEdge.class);
        tree2.addVertex("A");
        tree2.addVertex("B");
        tree2.addVertex("C");

        tree2.addEdge("A", "B");
        tree2.addEdge("B", "C");

        AHUTreeIsomorphism<String, DefaultEdge> isomorphism =
                new AHUTreeIsomorphism<>(tree1, tree2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicTreeMapping<String, DefaultEdge> treeMapping = isomorphism.getIsomorphism();
        Assert.assertTrue(areIdentical(tree1, generatedMappedTree(tree2, treeMapping.getBackwardMapping())));
    }

    @Test
    public void testSingleVertex(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        tree1.addVertex("1");

        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(DefaultEdge.class);
        tree2.addVertex("A");

        AHUTreeIsomorphism<String, DefaultEdge> isomorphism =
                new AHUTreeIsomorphism<>(tree1, tree2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicTreeMapping<String, DefaultEdge> treeMapping = isomorphism.getIsomorphism();
        Assert.assertTrue(areIdentical(tree1, generatedMappedTree(tree2, treeMapping.getBackwardMapping())));
    }

    @Test(expected = NullPointerException.class)
    public void testNullGraphs(){
        AHUTreeIsomorphism<String, DefaultEdge> isomorphism =
                new AHUTreeIsomorphism<>(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testOnlyOneNullGraph(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);

        AHUTreeIsomorphism<String, DefaultEdge> isomorphism =
                new AHUTreeIsomorphism<>(tree1, null);
    }

    @Test
    public void testNonIsomorphicAsUnrootedButAsRooted(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(DefaultEdge.class);

        for (char c = 'A'; c <= 'C'; c++) {
            tree1.addVertex(String.valueOf(c));
            tree2.addVertex(String.valueOf((char)(c + ' ')));
        }

        tree1.addEdge("A", "B");
        tree1.addEdge("B", "C");

        tree2.addEdge("a", "b");
        tree2.addEdge("b", "c");

        // They are not isomorphic as rooted trees

        AHUTreeIsomorphism<String, DefaultEdge> isomorphism =
                new AHUTreeIsomorphism<>(tree1, "A", tree2, "b");

        Assert.assertFalse(isomorphism.isomorphismExists());
        Assert.assertNull(isomorphism.getIsomorphism());

        // But they are isomorphic as unrooted trees

        isomorphism = new AHUTreeIsomorphism<>(tree1, tree2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicTreeMapping<String, DefaultEdge> treeMapping = isomorphism.getIsomorphism();
        Assert.assertTrue(areIdentical(tree1, generatedMappedTree(tree2, treeMapping.getBackwardMapping())));
    }

    @Test
    public void testNonIsomorphic(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(DefaultEdge.class);

        for (char c = 'A'; c <= 'F'; c++) {
            tree1.addVertex(String.valueOf(c));
            tree2.addVertex(String.valueOf((char)(c + ' ')));
        }

        tree1.addEdge("A", "B");
        tree1.addEdge("A", "C");
        tree1.addEdge("B", "F");
        tree1.addEdge("C", "D");
        tree1.addEdge("C", "E");

        tree2.addEdge("a", "b");
        tree2.addEdge("a", "c");
        tree2.addEdge("c", "f");
        tree2.addEdge("c", "d");
        tree2.addEdge("c", "e");

        // They are not isomorphic as rooted trees

        AHUTreeIsomorphism<String, DefaultEdge> isomorphism =
                new AHUTreeIsomorphism<>(tree1, "A", tree2, "a");

        Assert.assertFalse(isomorphism.isomorphismExists());
        Assert.assertNull(isomorphism.getIsomorphism());
    }

    @Test
    public void testSmall(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(DefaultEdge.class);

        for (char c = 'A'; c <= 'E'; c++) {
            tree1.addVertex(String.valueOf(c));
            tree2.addVertex(String.valueOf((char)(c + ' ')));
        }

        tree1.addEdge("A", "B");
        tree1.addEdge("A", "C");
        tree1.addEdge("C", "D");
        tree1.addEdge("C", "E");

        tree2.addEdge("a", "b");
        tree2.addEdge("a", "c");
        tree2.addEdge("b", "e");
        tree2.addEdge("b", "d");

        AHUTreeIsomorphism<String, DefaultEdge> isomorphism =
                new AHUTreeIsomorphism<>(tree1, "A", tree2, "a");

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicTreeMapping<String, DefaultEdge> treeMapping = isomorphism.getIsomorphism();
        Assert.assertTrue(areIdentical(tree1, generatedMappedTree(tree2, treeMapping.getBackwardMapping())));
    }

    @Test
    public void testSmall2(){
        Graph<Integer, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 1; i <= 13; i++) {
            tree1.addVertex(i);
        }

        tree1.addEdge(1, 2);
        tree1.addEdge(1, 3);

        tree1.addEdge(2, 4);
        tree1.addEdge(2, 5);
        tree1.addEdge(2, 6);

        tree1.addEdge(3, 7);
        tree1.addEdge(3, 8);
        tree1.addEdge(3, 9);

        tree1.addEdge(8, 10);
        tree1.addEdge(8, 11);

        tree1.addEdge(9, 12);
        tree1.addEdge(9, 13);

        Pair<Graph<Integer, DefaultEdge>, Map<Integer, Integer>> pair =
                generateIsomorphismTree(tree1, new Random(0x88));

        Graph<Integer, DefaultEdge> tree2 = pair.getFirst();
        Map<Integer, Integer> mapping = pair.getSecond();

        AHUTreeIsomorphism<Integer, DefaultEdge> isomorphism =
                new AHUTreeIsomorphism<>(tree1, 1, tree2, mapping.get(1));

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicTreeMapping<Integer, DefaultEdge> treeMapping = isomorphism.getIsomorphism();
        Assert.assertTrue(areIdentical(tree1, generatedMappedTree(tree2, treeMapping.getBackwardMapping())));
    }

    @Test
    public void testDisconnectedTree(){
        Graph<Integer, DefaultEdge> tree1 = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        tree1.addVertex(1);
        tree1.addVertex(2);
        tree1.addVertex(3);

        tree1.addEdge(1, 2);

        Graph<Integer, DefaultEdge> tree2 = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        tree2.addVertex(11);
        tree2.addVertex(21);
        tree2.addVertex(31);

        tree2.addEdge(11, 21);

        AHUTreeIsomorphism<Integer, DefaultEdge> isomorphism =
                new AHUTreeIsomorphism<>(tree1, 1, tree2, 11);

        Assert.assertFalse(isomorphism.isomorphismExists());
        Assert.assertNull(isomorphism.getIsomorphism());

        // Test as forest

        Assert.assertTrue(isomorphism.isomorphismExistsForest());
        IsomorphicTreeMapping<Integer, DefaultEdge> treeMapping = isomorphism.getIsomorphismForest();
        Assert.assertTrue(areIdentical(tree1, generatedMappedTree(tree2, treeMapping.getBackwardMapping())));
    }

    @Test
    public void testCompositionOfMappings(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        tree1.addVertex("1");
        tree1.addVertex("2");
        tree1.addEdge("1", "2");

        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(DefaultEdge.class);
        tree2.addVertex("a");
        tree2.addVertex("b");
        tree2.addEdge("a", "b");

        Graph<String, DefaultEdge> tree3 = new SimpleGraph<>(DefaultEdge.class);
        tree3.addVertex("A");
        tree3.addVertex("B");
        tree3.addEdge("A", "B");

        AHUTreeIsomorphism<String, DefaultEdge> isomorphism =
                new AHUTreeIsomorphism<>(tree1, tree2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicTreeMapping<String, DefaultEdge> mapping12 = isomorphism.getIsomorphism();

        isomorphism = new AHUTreeIsomorphism<>(tree2, tree3);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicTreeMapping<String, DefaultEdge> mapping23 = isomorphism.getIsomorphism();

        IsomorphicTreeMapping<String, DefaultEdge> mapping13 = mapping12.compose(mapping23);

        Assert.assertTrue(areIdentical(tree1, generatedMappedTree(tree3, mapping13.getBackwardMapping())));
    }

    @Test
    public void testLineGraph(){
        final int N = 10_000;
        Graph<Integer, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 1; i <= N; i++) {
            tree1.addVertex(i);
        }

        for (int i = 1; i <= N - 1; i++) {
            tree1.addEdge(i, i + 1);
        }

        Pair<Graph<Integer, DefaultEdge>, Map<Integer, Integer>> pair =
                generateIsomorphismTree(tree1, new Random(0x88));

        Graph<Integer, DefaultEdge> tree2 = pair.getFirst();
        Map<Integer, Integer> mapping = pair.getSecond();

        AHUTreeIsomorphism<Integer, DefaultEdge> isomorphism =
                new AHUTreeIsomorphism<>(tree1, 1, tree2, mapping.get(1));

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicTreeMapping<Integer, DefaultEdge> treeMapping = isomorphism.getIsomorphism();
        Assert.assertTrue(areIdentical(tree1, generatedMappedTree(tree2, treeMapping.getBackwardMapping())));
    }

    @Test
    public void testHugeNumberOfChildren(){
        final int N = 100_000;
        Graph<Integer, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 1; i <= N; i++) {
            tree1.addVertex(i);
        }

        for (int i = 2; i <= N; i++) {
            tree1.addEdge(1, i);
        }

        Pair<Graph<Integer, DefaultEdge>, Map<Integer, Integer>> pair =
                generateIsomorphismTree(tree1, new Random(0x2882));

        Graph<Integer, DefaultEdge> tree2 = pair.getFirst();
        Map<Integer, Integer> mapping = pair.getSecond();

        AHUTreeIsomorphism<Integer, DefaultEdge> isomorphism =
                new AHUTreeIsomorphism<>(tree1, 1, tree2, mapping.get(1));

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicTreeMapping<Integer, DefaultEdge> treeMapping = isomorphism.getIsomorphism();
        Assert.assertTrue(areIdentical(tree1, generatedMappedTree(tree2, treeMapping.getBackwardMapping())));
    }

    @Test
    public void testHugeRandomTree(){
        final int N = 50_000;
        Graph<Integer, DefaultEdge> tree1 = generateTree(N, new Random(0x88));

        Pair<Graph<Integer, DefaultEdge>, Map<Integer, Integer>> pair =
                generateIsomorphismTree(tree1, new Random(0x88));

        Graph<Integer, DefaultEdge> tree2 = pair.getFirst();
        Map<Integer, Integer> mapping = pair.getSecond();

        AHUTreeIsomorphism<Integer, DefaultEdge> isomorphism =
                new AHUTreeIsomorphism<>(tree1, 1, tree2, mapping.get(1));

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicTreeMapping<Integer, DefaultEdge> treeMapping = isomorphism.getIsomorphism();
        Assert.assertTrue(areIdentical(tree1, generatedMappedTree(tree2, treeMapping.getBackwardMapping())));

        isomorphism = new AHUTreeIsomorphism<>(tree1, tree2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        treeMapping = isomorphism.getIsomorphism();
        Assert.assertTrue(areIdentical(tree1, generatedMappedTree(tree2, treeMapping.getBackwardMapping())));
    }

    @Test
    public void testRandomTrees(){
        Random random = new Random(0x88_88);
        final int NUM_TESTS = 1500;

        for (int test = 0; test < NUM_TESTS; test++) {
            final int N = 10 + random.nextInt(100);

            Graph<Integer, DefaultEdge> tree1 = generateTree(N, random);

            Pair<Graph<Integer, DefaultEdge>, Map<Integer, Integer>> pair = generateIsomorphismTree(tree1, random);

            Graph<Integer, DefaultEdge> tree2 = pair.getFirst();

            AHUTreeIsomorphism<Integer, DefaultEdge> isomorphism =
                    new AHUTreeIsomorphism<>(tree1, tree2);

            Assert.assertTrue(isomorphism.isomorphismExists());
            IsomorphicTreeMapping<Integer, DefaultEdge> treeMapping = isomorphism.getIsomorphism();
            Assert.assertTrue(areIdentical(tree1, generatedMappedTree(tree2, treeMapping.getBackwardMapping())));
        }
    }
}