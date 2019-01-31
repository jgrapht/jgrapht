/*
 * (C) Copyright 2015-2018, by Fabian Späh and Contributors.
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
package org.jgrapht.alg.isomorphism;

import org.jgrapht.*;
import org.jgrapht.graph.*;

import java.util.*;

public class SubgraphIsomorphismTestUtils
{
    private static boolean DEBUG = false;

    public static boolean allMatchingsCorrect(
        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vf2, Graph<Integer, DefaultEdge> g1,
        Graph<Integer, DefaultEdge> g2)
    {
        showLog(">> ");
        boolean isCorrect = true;

        for (Iterator<GraphMapping<Integer, DefaultEdge>> mappings = vf2.getMappings();
            mappings.hasNext();)
        {
            isCorrect = isCorrect && isCorrectMatching(mappings.next(), g1, g2);
            showLog(".");
        }
        showLog("\n");

        return isCorrect;
    }

    public static boolean isCorrectMatching(
        GraphMapping<Integer, DefaultEdge> rel, Graph<Integer, DefaultEdge> g1,
        Graph<Integer, DefaultEdge> g2)
    {
        Set<Integer> vertexSet = g2.vertexSet();

        for (Integer u1 : vertexSet) {
            Integer v1 = rel.getVertexCorrespondence(u1, false);

            for (Integer u2 : vertexSet) {
                if (u1 == u2)
                    continue;

                Integer v2 = rel.getVertexCorrespondence(u2, false);

                if (v1 == v2) {
                    showLog(u1 + " and " + u2 + " are both mapped on " + v1 + "\n");
                    return false;
                }

                if (g1.containsEdge(v1, v2) != g2.containsEdge(u1, u2)) {
                    if (g1.containsEdge(v1, v2))
                        showLog(
                            "there is an edge from " + v1 + " to " + v2
                                + " in graph1 that does not exist from " + u1 + " to " + u2
                                + " in graph2");
                    else
                        showLog(
                            "there is an edge from " + u1 + " to " + u2
                                + "in graph2 that does not exist from " + v1 + " to " + v2
                                + " in graph1");
                    return false;
                }
            }
        }

        return true;
    }

    public static Graph<Integer, DefaultEdge> randomSubgraph(
        Graph<Integer, DefaultEdge> g1, int vertexCount, long seed)
    {
        Map<Integer, Integer> map = new HashMap<>();
        Graph<Integer, DefaultEdge> g2 = new DefaultDirectedGraph<>(DefaultEdge.class);
        Set<Integer> vertexSet = g1.vertexSet();
        int n = vertexSet.size();

        Random rnd = new Random();
        rnd.setSeed(seed);

        for (int i = 0; i < vertexCount;) {
            for (Integer v : vertexSet) {
                if (rnd.nextInt(n) == 0 && !map.containsKey(v)) {
                    Integer u = i++;
                    g2.addVertex(u);
                    map.put(v, u);
                }
            }
        }

        for (DefaultEdge e : g1.edgeSet()) {
            Integer v1 = g1.getEdgeSource(e), v2 = g1.getEdgeTarget(e);
            if (map.containsKey(v1) && map.containsKey(v2)) {
                Integer u1 = map.get(v1), u2 = map.get(v2);
                g2.addEdge(u1, u2);
            }
        }

        return g2;
    }

    public static Graph<Integer, DefaultEdge> randomGraph(int vertexCount, int edgeCount, long seed)
    {
        Integer[] vertexes = new Integer[vertexCount];
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        for (int i = 0; i < vertexCount; i++)
            g.addVertex(vertexes[i] = i);

        Random rnd = new Random();
        rnd.setSeed(seed);

        for (int i = 0; i < edgeCount;) {
            Integer source = vertexes[rnd.nextInt(vertexCount)],
                target = vertexes[rnd.nextInt(vertexCount)];

            if (source != target && !g.containsEdge(source, target)) {
                g.addEdge(source, target);
                i++;
            }
        }

        return g;
    }

    /**
     * Assuming g1 and g2 have vertexes labeled with 0, 1, ... No semantic check is done. Assuming
     * SubgraphIsomorphismRelation.equals and getMatchings are correct.
     *
     * @param vf2 the SubgraphIsomorphismInspector
     * @param g1 first Graph
     * @param g2 second Graph, smaller or equal to g1
     * @return
     */
    public static boolean containsAllMatchings(
        VF2SubgraphIsomorphismInspector<Integer, DefaultEdge> vf2, Graph<Integer, DefaultEdge> g1,
        Graph<Integer, DefaultEdge> g2)
    {
        boolean correct = true;
        ArrayList<IsomorphicGraphMapping<Integer, DefaultEdge>> matchings = getMatchings(g1, g2);

        loop: for (Iterator<GraphMapping<Integer, DefaultEdge>> mappings = vf2.getMappings();
            mappings.hasNext();)
        {
            IsomorphicGraphMapping<Integer, DefaultEdge> rel1 =
                (IsomorphicGraphMapping<Integer, DefaultEdge>) mappings.next();

            showLog("> " + rel1 + " ..");

            for (IsomorphicGraphMapping<Integer, DefaultEdge> rel2 : matchings) {
                if (rel1.isEqualMapping(rel2)) {
                    matchings.remove(rel2);
                    showLog("exists\n");
                    continue loop;
                }
            }

            correct = false;
            showLog("does not exist!\n");
        }

        if (!matchings.isEmpty()) {
            correct = false;

            showLog("-- no counterpart for:\n");
            for (IsomorphicGraphMapping<Integer, DefaultEdge> match : matchings)
                showLog("  " + match + "\n");
        }

        if (correct)
            showLog("-- ok\n");

        return correct;
    }

    /**
     * Assuming g1 and g2 have vertexes labeled with 0, 1, ... No semantic check is done.
     *
     * @param g1 first Graph
     * @param g2 second Graph, smaller or equal to g1
     * @return
     */
    private static ArrayList<IsomorphicGraphMapping<Integer, DefaultEdge>> getMatchings(
        Graph<Integer, DefaultEdge> g1, Graph<Integer, DefaultEdge> g2)
    {
        int n1 = g1.vertexSet().size(), n2 = g2.vertexSet().size();

        GraphOrdering<Integer, DefaultEdge> g1o = new GraphOrdering<Integer, DefaultEdge>(g1),
            g2o = new GraphOrdering<Integer, DefaultEdge>(g2);

        ArrayList<ArrayList<Integer>> perms = getPermutations(new boolean[n1], n2);

        ArrayList<IsomorphicGraphMapping<Integer, DefaultEdge>> rels =
            new ArrayList<IsomorphicGraphMapping<Integer, DefaultEdge>>();

        loop: for (ArrayList<Integer> perm : perms) {
            int[] core2 = new int[n2];
            int i = 0;
            for (Integer p : perm)
                core2[i++] = p.intValue();

            for (DefaultEdge edge : g2.edgeSet()) {
                Integer u1 = g2.getEdgeSource(edge), u2 = g2.getEdgeTarget(edge), v1 = core2[u1],
                    v2 = core2[u2];

                if (!g1.containsEdge(v1, v2))
                    continue loop;
            }

            int[] core1 = new int[n1];
            Arrays.fill(core1, VF2State.NULL_NODE);

            for (i = 0; i < n2; i++)
                core1[core2[i]] = i;

            for (DefaultEdge edge : g1.edgeSet()) {
                Integer v1 = g1.getEdgeSource(edge), v2 = g1.getEdgeTarget(edge), u1 = core1[v1],
                    u2 = core1[v2];

                if (u1 == VF2State.NULL_NODE || u2 == VF2State.NULL_NODE)
                    continue;

                if (!g2.containsEdge(u1, u2))
                    continue loop;
            }

            rels.add(new IsomorphicGraphMapping<Integer, DefaultEdge>(g1o, g2o, core1, core2));
        }

        return rels;
    }

    private static ArrayList<ArrayList<Integer>> getPermutations(boolean[] vertexSet, int len)
    {
        ArrayList<ArrayList<Integer>> perms = new ArrayList<ArrayList<Integer>>();

        if (len <= 0) {
            perms.add(new ArrayList<Integer>());
            return perms;
        }

        for (int i = 0; i < vertexSet.length; i++) {
            if (!vertexSet[i]) {
                vertexSet[i] = true;
                ArrayList<ArrayList<Integer>> newPerms = getPermutations(vertexSet, len - 1);
                vertexSet[i] = false;

                for (ArrayList<Integer> perm : newPerms)
                    perm.add(i);

                perms.addAll(newPerms);
            }
        }

        return perms;
    }

    public static void showLog(String str)
    {
        if (!DEBUG)
            return;

        System.out.print(str);
    }
}
