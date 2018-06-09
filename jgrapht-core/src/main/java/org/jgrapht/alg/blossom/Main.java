package org.jgrapht.alg.blossom;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.KuhnMunkresMinimalWeightBipartitePerfectMatching;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Random;

import static org.jgrapht.alg.blossom.KolmogorovMinimumWeightPerfectMatching.EPS;
import static org.jgrapht.alg.blossom.KolmogorovMinimumWeightPerfectMatching.SingleTreeDualUpdatePhase.UPDATE_DUAL_AFTER;
import static org.jgrapht.alg.blossom.KolmogorovMinimumWeightPerfectMatching.SingleTreeDualUpdatePhase.UPDATE_DUAL_BEFORE;
import static org.jgrapht.alg.blossom.DualUpdater.DualUpdateType.MULTIPLE_TREE_CONNECTED_COMPONENTS;
import static org.jgrapht.alg.blossom.DualUpdater.DualUpdateType.MULTIPLE_TREE_FIXED_DELTA;
import static org.jgrapht.alg.blossom.Initializer.InitializationType.GREEDY;
import static org.jgrapht.alg.blossom.Initializer.InitializationType.NONE;

public class Main {
    private static final String MATRIX_PATH = "C:\\Users\\timof\\Documents\\GSoC 2018\\Tmp\\matrix.txt";
    private static final String EDGE_LIST_PATH = "C:\\Users\\timof\\Documents\\GSoC 2018\\Tmp\\edge_list.txt";
    private static final String CODE_PATH = "C:\\Users\\timof\\Documents\\GSoC 2018\\Tmp\\code.txt";
    private static final String JAR_PATH = "C:\\Java_Projects\\delaunay-triangulator\\library\\build\\libs\\Triangulation-1.jar";
    private static final String ARR_PATH = "C:\\Users\\timof\\Documents\\GSoC 2018\\Tmp\\arr.txt";
    private static KolmogorovMinimumWeightPerfectMatching.Options[] options = new KolmogorovMinimumWeightPerfectMatching.Options[]{
            new KolmogorovMinimumWeightPerfectMatching.Options(UPDATE_DUAL_BEFORE, MULTIPLE_TREE_FIXED_DELTA, NONE),  // [0]
            new KolmogorovMinimumWeightPerfectMatching.Options(UPDATE_DUAL_BEFORE, MULTIPLE_TREE_FIXED_DELTA, GREEDY),  // [1]
            new KolmogorovMinimumWeightPerfectMatching.Options(UPDATE_DUAL_BEFORE, MULTIPLE_TREE_CONNECTED_COMPONENTS, NONE), // [2]
            new KolmogorovMinimumWeightPerfectMatching.Options(UPDATE_DUAL_BEFORE, MULTIPLE_TREE_CONNECTED_COMPONENTS, GREEDY),  // [3]
            new KolmogorovMinimumWeightPerfectMatching.Options(UPDATE_DUAL_AFTER, MULTIPLE_TREE_FIXED_DELTA, NONE),  // [4]
            new KolmogorovMinimumWeightPerfectMatching.Options(UPDATE_DUAL_AFTER, MULTIPLE_TREE_FIXED_DELTA, GREEDY),  // [5]
            new KolmogorovMinimumWeightPerfectMatching.Options(UPDATE_DUAL_AFTER, MULTIPLE_TREE_CONNECTED_COMPONENTS, NONE),  // [6]
            new KolmogorovMinimumWeightPerfectMatching.Options(UPDATE_DUAL_AFTER, MULTIPLE_TREE_CONNECTED_COMPONENTS, GREEDY),  // [7]
    };


    public static void main(String[] args) {
        testOnTriangulation();
    }

    private static void printCode() {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(EDGE_LIST_PATH)));
             BufferedWriter writer = new BufferedWriter(new FileWriter(new File(CODE_PATH)))) {
            String[] tokens = reader.readLine().split("\\s+");
            int m = Integer.valueOf(tokens[1]);
            for (int i = 0; i < m; i++) {
                if (i % 4 == 0 && i != 0) {
                    writer.write('\n');
                }
                tokens = reader.readLine().split("\\s+");
                writer.write(String.format("Graphs.addEdgeWithVertices(graph, %d, %d, %d);\n", Integer.valueOf(tokens[0]), Integer.valueOf(tokens[1]), Double.valueOf(tokens[2]).intValue()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void convertIntoArrInt() {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(EDGE_LIST_PATH)));
             BufferedWriter writer = new BufferedWriter(new FileWriter(new File(ARR_PATH)))) {
            String[] token = reader.readLine().split("\\s+");
            int m = Integer.valueOf(token[1]);
            writer.write("int[][] edges = new int[][]{");
            for (int i = 0; i < m; i++) {
                token = reader.readLine().split("\\s+");
                writer.write("{" + Integer.valueOf(token[0]) + ", " + Integer.valueOf(token[1]) + ", " + Integer.valueOf(token[2]) + "}");
                if (i != m - 1) {
                    writer.write(",");
                }
                if (i % 6 == 0 && i != 0) {
                    writer.write("\n");
                }
            }
            writer.write("};");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void testOnTriangulation() {
        try {
            int growNum = 0;
            int shrinkNum = 0;
            int expandNum = 0;
            for (int i = 0; i < 1000; i++) {
                Runtime runtime = Runtime.getRuntime();
                Process process = runtime.exec(String.format("java -jar %s --points-file " +
                        "\"C:\\Users\\timof\\Documents\\GSoC 2018\\Tmp\\point_set.txt\" " +
                        "--edge-file \"C:\\Users\\timof\\Documents\\GSoC 2018\\Tmp\\edge_list.txt\" " +
                        "--point-num %d --start %d --end %d", JAR_PATH, 10, 0, 15));
                InputStream inputStream = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                reader.readLine();
                reader.readLine();
                /*System.out.println(reader.readLine());
                System.out.println(reader.readLine());*/

                /*convertIntoArrInt();
                System.out.println("Converted");*/

                Graph<Integer, DefaultWeightedEdge> graph = readEdgeList(EDGE_LIST_PATH);
                System.out.println("Graph's edge list has been imported");
                KolmogorovMinimumWeightPerfectMatching.Statistics statistics = testOnGraph(graph, i + 1);

                shrinkNum += statistics.getShrinkNum();
                expandNum += statistics.getExpandNum();
                growNum += statistics.getGrowNum();
            }
            System.out.println("Grow num: " + growNum);
            System.out.println("Shrink num: " + shrinkNum);
            System.out.println("Expand num: " + expandNum);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateComplete(int size, int upperBound) throws IOException {
        Random random = new Random(System.currentTimeMillis());
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (int i = 1; i < size; i++) {
            for (int j = 0; j < i; j++) {
                int weight = random.nextInt(upperBound) + 1;
                Graphs.addEdgeWithVertices(graph, i, j, weight);
            }
        }
        exportSimple(graph, EDGE_LIST_PATH);
        convertIntoArrInt();
        testOnGraph(graph, 1);
    }


    private static void testOnGenerated() {
        try {
            int shrinkNum = 0;
            int expandNum = 0;
            int growNum = 0;
            for (int i = 0; i < 1; i++) {
                generate(MATRIX_PATH, 3000);
                System.out.println("Generated");
                Graph<Integer, DefaultWeightedEdge> graph = readGraph(MATRIX_PATH, true);
                System.out.println("Read from file");
                exportSimple(graph, EDGE_LIST_PATH);
                System.out.println("Exported");
                testOnGraph(graph, i + 1);
                /*KolmogorovMinimumWeightPerfectMatching.Statistics statistics = perfectMatching.getStatistics();
                shrinkNum += statistics.getShrinkNum();
                expandNum += statistics.getExpandNum();
                growNum += statistics.getGrowNum();*/
            }
            /*System.out.println("Shrink num: " + shrinkNum);
            System.out.println("Expand num: " + expandNum);
            System.out.println("Grow num: " + growNum);*/

        } catch (IOException e) {
            System.out.flush();
            e.printStackTrace();
        }
    }

    private static KolmogorovMinimumWeightPerfectMatching.Statistics testOnGraph(Graph<Integer, DefaultWeightedEdge> graph, int testCase) {
        double weight;
        options[0].verbose = false;
        KolmogorovMinimumWeightPerfectMatching<Integer, DefaultWeightedEdge> perfectMatching = new KolmogorovMinimumWeightPerfectMatching<>(
                graph, options[0]);
        MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching = perfectMatching.getMatching();
        weight = matching.getWeight();
        System.out.println("Test case: " + testCase + ", matching weight = " + matching.getWeight());
        for (KolmogorovMinimumWeightPerfectMatching.Options options : options) {
            options.verbose = false;
            KolmogorovMinimumWeightPerfectMatching<Integer, DefaultWeightedEdge> blossomPerfectMatching = new KolmogorovMinimumWeightPerfectMatching<>(graph, options);
            MatchingAlgorithm.Matching<Integer, DefaultWeightedEdge> matching1 = blossomPerfectMatching.getMatching();
            double weight2 = matching1.getWeight();
            if (Math.abs(weight2 - weight) > EPS) {
                System.out.println(weight);
                System.out.println(weight2);
                throw new RuntimeException();
            }
            if (!blossomPerfectMatching.testOptimality()) {
                System.out.println(blossomPerfectMatching.getDualSolution());
                throw new RuntimeException();
            }
        }
        return perfectMatching.getStatistics();
    }

    private static Graph<Integer, DefaultWeightedEdge> readGraph(String path, boolean bipartite) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(path)))) {
            Graph<Integer, DefaultWeightedEdge> graph =
                    new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
            int vertexNum = Integer.valueOf(reader.readLine());
            for (int i = 0; i < vertexNum; i++) {
                String[] tokens = reader.readLine().split("\\s+");
                if (tokens.length != vertexNum) {
                    throw new IOException("Invalid vertex num at row " + (i + 1));
                }
                for (int j = 0; j < vertexNum; j++) {
                    double weight = Double.valueOf(tokens[j]);
                    if (weight > 0) {
                        if (!bipartite) {
                            if (i < j) {
                                Graphs.addEdgeWithVertices(graph, i, j, weight);
                            }
                        } else {
                            Graphs.addEdgeWithVertices(graph, i, vertexNum + j, weight);
                        }
                    }
                }
            }
            return graph;
        }
    }

    private static Graph<Integer, DefaultWeightedEdge> readEdgeList(String path) throws IOException {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(path)))) {
            String[] tokens = reader.readLine().split("\\s+");
            int n = Integer.valueOf(tokens[0]);
            int m = Integer.valueOf(tokens[1]);
            for (int i = 0; i < m; i++) {
                tokens = reader.readLine().split("\\s+");
                int a = Integer.valueOf(tokens[0]);
                int b = Integer.valueOf(tokens[1]);
                int w = Double.valueOf(tokens[2]).intValue();
                Graphs.addEdgeWithVertices(graph, a, b, w);
            }
            return graph;
        }
    }

    private static void generate(String path, int vertexNum) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)))) {
            Random random = new Random();
            boolean odd = (vertexNum & 1) == 1;
            double upperBound = 30;
            int dummyVertex = -1;
            int dummyWeight = 0;
            writer.write((odd ? vertexNum + 1 : vertexNum) + "\n");
            if (odd) {
                dummyVertex = (int) (random.nextDouble() * vertexNum);
                dummyWeight = (int) (random.nextDouble() * upperBound + 1);
            }
            for (int i = 0; i < vertexNum; i++) {
                for (int j = 0; j < vertexNum; j++) {
                    int weight = (int) (random.nextDouble() * upperBound + 1);
                    writer.write(weight + " ");
                }
                if (odd) {
                    if (i == dummyVertex) {
                        writer.write(dummyWeight + " ");
                    } else {
                        writer.write("0 ");
                    }
                }
                writer.write("\n");
            }
            if (odd) {
                for (int i = 0; i < vertexNum + 1; i++) {
                    if (i == dummyVertex) {
                        writer.write(dummyWeight + " ");
                    } else {
                        writer.write("0 ");
                    }
                }
            }
        }
    }

    private static void exportSimple(Graph<Integer, DefaultWeightedEdge> graph, String path) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)))) {
            writer.write(graph.vertexSet().size() + " " + graph.edgeSet().size() + "\n");
            for (DefaultWeightedEdge edge : graph.edgeSet()) {
                int a = graph.getEdgeSource(edge);
                int b = graph.getEdgeTarget(edge);
                double weight = graph.getEdgeWeight(edge);
                writer.write(a + " " + b + " " + (int) weight + "\n");
            }
        }
    }
}
