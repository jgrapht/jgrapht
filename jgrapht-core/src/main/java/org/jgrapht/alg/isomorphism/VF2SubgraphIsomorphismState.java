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

import java.util.*;

class VF2SubgraphIsomorphismState<V, E>
    extends
    VF2State<V, E>
{
    public VF2SubgraphIsomorphismState(
        GraphOrdering<V, E> g1, GraphOrdering<V, E> g2, Comparator<V> vertexComparator,
        Comparator<E> edgeComparator)
    {
        super(g1, g2, vertexComparator, edgeComparator);
    }

    public VF2SubgraphIsomorphismState(VF2State<V, E> s)
    {
        super(s);
    }

    /**
     * @return true, if the already matched vertices of graph1 plus the first vertex of nextPair are
     *         subgraph isomorphic to the already matched vertices of graph2 and the second one
     *         vertex of nextPair.
     */
    @Override
    public boolean isFeasiblePair()
    {
        String pairstr = "(" + g1.getVertex(addVertex1) + ", " + g2.getVertex(addVertex2) + ")",
            abortmsg = pairstr + " does not fit in the current matching";

        // check for semantic equality of both vertexes
        if (!areCompatibleVertexes(addVertex1, addVertex2)) {
            return false;
        }

        int termOutPred1 = 0, termOutPred2 = 0, termInPred1 = 0, termInPred2 = 0, newPred1 = 0,
            newPred2 = 0, termOutSucc1 = 0, termOutSucc2 = 0, termInSucc1 = 0, termInSucc2 = 0,
            newSucc1 = 0, newSucc2 = 0;

        // check outgoing edges of addVertex1
        for (int other1 : g1.getOutEdges(addVertex1)) {
            if (core1[other1] != NULL_NODE) {
                int other2 = core1[other1];
                if (!g2.hasEdge(addVertex2, other2)
                    || !areCompatibleEdges(addVertex1, other1, addVertex2, other2))
                {
                    showLog(
                        "isFeasiblePair", abortmsg + ": edge from " + g2.getVertex(addVertex2)
                            + " to " + g2.getVertex(other2) + " is missing in the 2nd graph");
                    return false;
                }
            } else {
                if (in1[other1] > 0) {
                    termInSucc1++;
                }
                if (out1[other1] > 0) {
                    termOutSucc1++;
                }
                if ((in1[other1] == 0) && (out1[other1] == 0)) {
                    newSucc1++;
                }
            }
        }

        // check outgoing edges of addVertex2
        for (int other2 : g2.getOutEdges(addVertex2)) {
            if (core2[other2] != NULL_NODE) {
                int other1 = core2[other2];
                if (!g1.hasEdge(addVertex1, other1)) {
                    showLog(
                        "isFeasbilePair", abortmsg + ": edge from " + g1.getVertex(addVertex1)
                            + " to " + g1.getVertex(other1) + " is missing in the 1st graph");
                    return false;
                }
            } else {
                if (in2[other2] > 0) {
                    termInSucc2++;
                }
                if (out2[other2] > 0) {
                    termOutSucc2++;
                }
                if ((in2[other2] == 0) && (out2[other2] == 0)) {
                    newSucc2++;
                }
            }
        }

        if ((termInSucc1 < termInSucc2) || (termOutSucc1 < termOutSucc2) || (newSucc1 < newSucc2)) {
            if (DEBUG) {
                String cause = "", v1 = g1.getVertex(addVertex1).toString(),
                    v2 = g2.getVertex(addVertex2).toString();

                if (termInSucc2 > termInSucc1) {
                    cause = "|Tin2 ∩ Succ(Graph2, " + v2 + ")| > |Tin1 ∩ Succ(Graph1, " + v1 + ")|";
                } else if (termOutSucc2 > termOutSucc1) {
                    cause =
                        "|Tout2 ∩ Succ(Graph2, " + v2 + ")| > |Tout1 ∩ Succ(Graph1, " + v1 + ")|";
                } else if (newSucc2 > newSucc1) {
                    cause = "|N‾ ∩ Succ(Graph2, " + v2 + ")| > |N‾ ∩ Succ(Graph1, " + v1 + ")|";
                }

                showLog("isFeasbilePair", abortmsg + ": " + cause);
            }

            return false;
        }

        // check incoming edges of addVertex1
        for (int other1 : g1.getInEdges(addVertex1)) {
            if (core1[other1] != NULL_NODE) {
                int other2 = core1[other1];
                if (!g2.hasEdge(other2, addVertex2)
                    || !areCompatibleEdges(other1, addVertex1, other2, addVertex2))
                {
                    showLog(
                        "isFeasbilePair", abortmsg + ": edge from " + g2.getVertex(other2) + " to "
                            + g2.getVertex(addVertex2) + " is missing in the 2nd graph");
                    return false;
                }
            } else {
                if (in1[other1] > 0) {
                    termInPred1++;
                }
                if (out1[other1] > 0) {
                    termOutPred1++;
                }
                if ((in1[other1] == 0) && (out1[other1] == 0)) {
                    newPred1++;
                }
            }
        }

        // check incoming edges of addVertex2
        for (int other2 : g2.getInEdges(addVertex2)) {
            if (core2[other2] != NULL_NODE) {
                int other1 = core2[other2];
                if (!g1.hasEdge(other1, addVertex1)) {
                    showLog(
                        "isFeasiblePair", abortmsg + ": edge from " + g1.getVertex(other1) + " to "
                            + g1.getVertex(addVertex1) + " is missing in the 1st graph");
                    return false;
                }
            } else {
                if (in2[other2] > 0) {
                    termInPred2++;
                }
                if (out2[other2] > 0) {
                    termOutPred2++;
                }
                if ((in2[other2] == 0) && (out2[other2] == 0)) {
                    newPred2++;
                }
            }
        }

        if ((termInPred1 >= termInPred2) && (termOutPred1 >= termOutPred2)
            && (newPred1 >= newPred2))
        {
            showLog("isFeasiblePair", pairstr + " fits");
            return true;
        } else {
            if (DEBUG) {
                String cause = "", v1 = g1.getVertex(addVertex1).toString(),
                    v2 = g2.getVertex(addVertex2).toString();

                if (termInPred2 > termInPred1) {
                    cause = "|Tin2 ∩ Pred(Graph2, " + v2 + ")| > |Tin1 ∩ Pred(Graph1, " + v1 + ")|";
                } else if (termOutPred2 > termOutPred1) {
                    cause =
                        "|Tout2 ∩ Pred(Graph2, " + v2 + ")| > |Tout1 ∩ Pred(Graph1, " + v1 + ")|";
                } else if (newPred2 > newPred1) {
                    cause = "|N‾ ∩ Pred(Graph2, " + v2 + ")| > |N‾ ∩ Pred(Graph1, " + v1 + ")|";
                }

                showLog("isFeasbilePair", abortmsg + ": " + cause);
            }

            return false;
        }
    }
}
