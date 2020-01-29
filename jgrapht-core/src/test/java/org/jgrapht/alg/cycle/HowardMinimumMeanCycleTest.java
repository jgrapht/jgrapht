/*
 * (C) Copyright 2020-2020, by Semen Chudakov and Contributors.
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

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.jgrapht.graph.GraphWalk;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link HowardMinimumMeanCycle}.
 */
public class HowardMinimumMeanCycleTest {

    // test graph instances
    private double[][] graph1 = {{1, 3, 7.0}, {3, 2, 3.0}, {2, 0, 7.0}, {2, 1, 5.0}};
    private double[][] graph2 = {{1, 0, 0.5309808994022128}, {2, 0, 0.887369465477802}, {0, 3, 0.24854619550940948},
            {5, 0, 0.7862072932065413}, {0, 6, 0.6597510963121964}, {8, 0, 0.3093510947826138},
            {1, 3, 0.16298399773945915}, {3, 1, 0.8880029290778009}, {1, 4, 0.8791863090101949},
            {4, 1, 0.11358409873434094}, {1, 5, 0.03559133251030855}, {1, 6, 0.8374371072320352},
            {7, 1, 0.40373199064561704}, {1, 8, 0.3659266491352863}, {2, 3, 0.19479123717630864},
            {4, 2, 0.4419484525036612}, {2, 5, 0.4507632555730162}, {2, 6, 0.03166341231438774},
            {8, 2, 0.5792275381432967}, {2, 9, 0.8078208387325877}, {9, 2, 0.01769111355335684},
            {3, 4, 0.40101703674683453}, {4, 3, 0.6634367592132826}, {5, 3, 0.693469788337928},
            {3, 6, 0.32775932796299034}, {7, 3, 0.5753257455513666}, {3, 9, 0.05863472248311086},
            {4, 5, 0.04322228439391351}, {6, 4, 0.2906318868919735}, {4, 7, 0.3699966314040184},
            {7, 4, 0.8207528010789471}, {4, 8, 0.3413909196087963}, {9, 4, 0.047503491784497975},
            {6, 5, 0.4717895784735354}, {7, 5, 0.5496166423219454}, {5, 9, 0.3610136990994086},
            {6, 7, 0.30034898347163264}, {9, 6, 0.3256821082193059}, {7, 8, 0.8211679242156328},
            {8, 7, 0.47245137817493654}, {9, 7, 0.6610595429601409}};
    private double[][] graph3 = {{0, 1, 0.9208023183776285}, {1, 0, 0.6430242504341916}, {0, 3, 0.09365718255006317},
            {5, 0, 0.31375412769298217}, {0, 7, 0.15792425419560785}, {7, 0, 0.13936355638785414},
            {0, 8, 0.8286556745309889}, {9, 0, 0.06546278011245077}, {1, 3, 0.8615410003817705},
            {4, 1, 0.06574244474823454}, {1, 5, 0.6178097749171817}, {5, 1, 0.5583780723473489},
            {1, 7, 0.6553642619919727}, {7, 1, 0.44490891354084894}, {8, 1, 0.6825867732670399},
            {2, 3, 0.8952226759266715}, {3, 2, 0.8533437845739735}, {5, 2, 0.14121317160400548},
            {2, 6, 0.6437222178613473}, {6, 2, 0.8049411475475996}, {2, 9, 0.14879952542017905},
            {3, 4, 0.33585405199165874}, {4, 3, 0.490216141318886}, {3, 7, 0.9550648737294264},
            {7, 3, 0.5045554184080497}, {3, 8, 0.6702361080542114}, {3, 9, 0.06373787937414133},
            {4, 6, 0.8277720223094508}, {7, 4, 0.2901248861595276}, {4, 8, 0.7834203859115622},
            {9, 4, 0.8022440614359926}, {6, 5, 0.39978526023721506}, {5, 7, 0.6465702401669658},
            {7, 6, 0.5995341305325894}, {6, 8, 0.1243106029504022}};
    private double[][] graph4 = {{5, 0, 0.12984728244943444}, {0, 9, 0.6091236198949413}, {1, 7, 0.8516203006087194},
            {4, 2, 0.6700978832134418}, {2, 6, 0.33295099587314136}, {6, 3, 0.6442914735780406},
            {4, 5, 0.6041550406570294}, {8, 5, 0.3669436593853893}};
    private double[][] graph5 = {{1, 0, 0.8049411475475996}, {4, 0, 0.14879952542017905}, {0, 5, 0.33585405199165874},
            {7, 0, 0.490216141318886}, {0, 10, 0.9550648737294264}, {10, 0, 0.5045554184080497},
            {0, 12, 0.6702361080542114}, {0, 13, 0.06373787937414133}, {14, 0, 0.8277720223094508},
            {1, 1, 0.2901248861595276}, {4, 1, 0.7834203859115622}, {1, 6, 0.8022440614359926},
            {8, 1, 0.39978526023721506}, {1, 9, 0.6465702401669658}, {1, 10, 0.5995341305325894},
            {11, 1, 0.1243106029504022}, {2, 2, 0.3242577712209569}, {2, 3, 0.6504770794296915},
            {4, 2, 0.2571376283289417}, {2, 6, 0.6296237549019814}, {7, 2, 0.1770563023011088},
            {9, 2, 0.09555575781265335}, {2, 11, 0.014929778639893532}, {2, 12, 0.2412712648050127},
            {14, 2, 0.6003572591667559}, {3, 3, 0.6137025853931498}, {4, 3, 0.816805569653777},
            {3, 7, 0.5219200324636206}, {3, 9, 0.8532298653175713}, {10, 3, 0.4169226462244163},
            {3, 11, 0.45416285172615767}, {3, 13, 0.6586612728209845}, {14, 3, 0.36188863806988525},
            {4, 5, 0.15733782851780476}, {4, 8, 0.4244876757276389}, {8, 4, 0.03025051155455427},
            {4, 9, 0.3515544396962552}, {4, 11, 0.2745727323277861}, {5, 5, 0.6526303487462352},
            {5, 10, 0.8456642051329823}, {11, 5, 0.5373719211903302}, {5, 12, 0.7281325512062164},
            {12, 5, 0.2148178071457969}, {5, 14, 0.07434051017359233}, {6, 6, 0.1535884729434781},
            {6, 8, 0.19695603470148493}, {6, 9, 0.08532996559090311}, {6, 11, 0.7761669538884948},
            {13, 6, 0.3082080065761067}, {6, 14, 0.6691483391125962}, {7, 8, 0.7119009182885047},
            {11, 7, 0.9832230593282196}, {12, 7, 0.7645904208606054}, {7, 13, 0.841968002481253},
            {8, 9, 0.9182885764373667}, {9, 8, 0.48548126661699165}, {10, 8, 0.7719753908775233},
            {12, 8, 0.9054626252650918}, {8, 14, 0.23108934328187725}, {14, 8, 0.22911500284211272},
            {9, 10, 0.4936673569770784}, {11, 9, 0.09271862364523031}, {10, 10, 0.23275669725461445},
            {12, 11, 0.548757460531207}, {13, 11, 0.9146257588559106}, {11, 14, 0.8455102919755371},
            {12, 12, 0.953916026514328}, {12, 13, 0.12400093191112904}, {14, 14, 0.849462984685311}};
    private double[][] graph6 = {{2, 0, 0.3011115464896066}, {3, 0, 0.03799965844589637}, {4, 0, 0.4794381869658567},
            {0, 5, 0.5043426071517753}, {5, 0, 0.9355611601089532}, {0, 7, 0.28520030585981493},
            {0, 8, 0.8001347603172168}, {8, 0, 0.791617600181339}, {0, 11, 0.5523882658250229},
            {11, 0, 0.05275408885677668}, {12, 0, 0.5535531434423085}, {1, 3, 0.48507355154682086},
            {4, 1, 0.41931302401238746}, {1, 6, 0.1258081085052315}, {1, 9, 0.9028130687721774},
            {9, 1, 0.4759598281591165}, {10, 1, 0.9140081398672594}, {1, 13, 0.1549152216029137},
            {2, 3, 0.7058722546492231}, {3, 2, 0.2093612635732508}, {2, 5, 0.4931186843389671},
            {10, 2, 0.5313020268328327}, {11, 2, 0.042596884421496006}, {12, 2, 0.7644017964331573},
            {2, 13, 0.37257515384707407}, {14, 2, 0.8585698305880247}, {3, 4, 0.23493980206399856},
            {3, 8, 0.06672386244834305}, {10, 3, 0.7223307665114344}, {3, 12, 0.36521888452812157},
            {3, 13, 0.16189279571717874}, {4, 4, 0.6738253907268404}, {4, 5, 0.5272871106108364},
            {7, 4, 0.7088326483481505}, {8, 4, 0.34763700799385644}, {9, 4, 0.13340746920723567},
            {4, 11, 0.5210545412495856}, {4, 12, 0.8854529536407005}, {4, 14, 0.38432339217409994},
            {14, 4, 0.13070514782552833}, {6, 5, 0.7603274734304047}, {5, 7, 0.3766235113894004},
            {5, 9, 0.740172824151688}, {13, 5, 0.5128449734953222}, {5, 14, 0.4750361441102906},
            {6, 7, 0.24177344817298785}, {6, 11, 0.8990934431517775}, {6, 12, 0.8065925985308405},
            {14, 6, 0.8030705787482436}, {7, 8, 0.5950582892823523}, {8, 7, 0.07977055297006574},
            {7, 9, 0.030424110952462002}, {9, 7, 0.8982546607708484}, {7, 11, 0.7934715703440332},
            {8, 8, 0.49019223801000766}, {8, 9, 0.44267569059084655}, {9, 8, 0.6863807643704616},
            {10, 8, 0.7069846072900937}, {8, 12, 0.18783148139677397}, {12, 8, 0.364595608577573},
            {14, 8, 0.23649901286119512}, {9, 9, 0.6416680913657361}, {10, 9, 0.13621382058732223},
            {12, 9, 0.37044373793300356}, {10, 10, 0.3132871842409578}, {11, 10, 0.8431941955960424},
            {10, 12, 0.9753568298497903}, {12, 10, 0.9463531873675847}, {11, 11, 0.18121082129435206},
            {12, 14, 0.020812929131225788}};

    // expected mean values
    private double expectedMean1 = 5.0;
    private double expectedMean2 = 0.090372357737592113;
    private double expectedMean3 = 0.074285947345551759;
    private double expectedMean4 = Double.POSITIVE_INFINITY;
    private double expectedMean5 = 0.067734720032592399;
    private double expectedMean6 = 0.14838114112973164;

    // expected minimum mean path for graph instance
    private double[][] expectedCycle1 = {{1, 3, 7}, {3, 2, 3}, {2, 1, 5}};
    private double[][] expectedCycle2 = {{3, 9, 0.058634722483110857}, {9, 2, 0.017691113553356841}, {2, 3, 0.19479123717630864}};
    private double[][] expectedCycle3 = {{0, 3, 0.093657182550063167}, {3, 9, 0.063737879374141326}, {9, 0, 0.065462780112450769}};
    private double[][] expectedCycle4 = null;
    private double[][] expectedCycle5 = {{9, 2, 0.095555757812653352}, {2, 11, 0.014929778639893532}, {11, 9, 0.092718623645230314}};
    private double[][] expectedCycle6 = {{14, 8, 0.23649901286119512}, {8, 12, 0.18783148139677397}, {12, 14, 0.020812929131225788}};

    @Test
    public void testGraph1() {
        testOnGraph(graph1, expectedMean1, expectedCycle1);
    }

    @Test
    public void testGraph2() {
        testOnGraph(graph2, expectedMean2, expectedCycle2);
    }

    @Test
    public void testGraph3() {
        testOnGraph(graph3, expectedMean3, expectedCycle3);
    }

    @Test
    public void testGraph4() {
        testOnGraph(graph4, expectedMean4, expectedCycle4);
    }

    @Test
    public void testGraph5() {
        testOnGraph(graph5, expectedMean5, expectedCycle5);
    }

    @Test
    public void testGraph6() {
        testOnGraph(graph6, expectedMean6, expectedCycle6);
    }

    /**
     * Tests the algorithm on the graph instance {@code graphArray} using {@code expectedMean}
     * and {@code expectedCycleArray} to check correctness.
     *
     * @param graphArray         graph instance
     * @param expectedMean       mean value
     * @param expectedCycleArray minimum mean cycle
     */
    private void testOnGraph(double[][] graphArray, double expectedMean, double[][] expectedCycleArray) {
        if (graphArray == null) {
            return;
        }
        Graph<Integer, DefaultWeightedEdge> graph = readGraph(graphArray);
        GraphPath<Integer, DefaultWeightedEdge> expectedPath;
        if (expectedCycleArray == null) {
            expectedPath = null;
        } else {
            expectedPath = readPath(expectedCycleArray, graph);
        }

        HowardMinimumMeanCycle<Integer, DefaultWeightedEdge> mmc = new HowardMinimumMeanCycle<>(graph);
        GraphPath<Integer, DefaultWeightedEdge> actualPath = mmc.getCycle();
        double actualMean = mmc.getCycleMean();

        assertEquals(expectedMean, actualMean, 1e-9);
        assertEquals(expectedPath, actualPath);
    }


    /**
     * Construct graph stored in {@code graph}.
     *
     * @param graph graph
     * @return constructed graph instance
     */
    private Graph<Integer, DefaultWeightedEdge> readGraph(double[][] graph) {
        Graph<Integer, DefaultWeightedEdge> result = new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        for (double[] edgeArray : graph) {
            int source = (int) edgeArray[0];
            int target = (int) edgeArray[1];
            double weight = edgeArray[2];

            Graphs.addEdgeWithVertices(result, source, target, weight);
        }
        return result;
    }

    /**
     * Constructs path stored in {@code path}.
     *
     * @param path  path
     * @param graph graph
     * @return constructed path instance
     */
    private GraphPath<Integer, DefaultWeightedEdge> readPath(double[][] path, Graph<Integer, DefaultWeightedEdge> graph) {
        int startVertex = (int) path[0][0];
        int endVertex = (int) path[path.length - 1][1];
        List<DefaultWeightedEdge> edges = new ArrayList<>(path.length);
        double pathWeight = 0.0;

        for (double[] edgeArray : path) {
            int source = (int) edgeArray[0];
            int target = (int) edgeArray[1];
            double weight = edgeArray[2];

            for (DefaultWeightedEdge edge : graph.getAllEdges(source, target)) {
                if (graph.getEdgeWeight(edge) == weight) {
                    edges.add(edge);
                    break;
                }
            }

            pathWeight += weight;
        }

        return new GraphWalk<>(graph, startVertex, endVertex, edges, pathWeight);
    }
}