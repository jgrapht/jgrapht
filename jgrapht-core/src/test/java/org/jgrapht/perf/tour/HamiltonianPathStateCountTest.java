/*
 * (C) Copyright 2026-2026, by seilat and Contributors.
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
package org.jgrapht.perf.tour;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.tour.*;
import org.jgrapht.graph.*;
import org.jgrapht.perf.tour.HamiltonianPathPerformanceTest.GraphFamily;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Diagnostic harness that reports, for every benchmark graph family, the number of DFS states
 * {@link BacktrackingHamiltonianPath} expands and the wall-clock cost of each solver. The goal
 * is to make the pruning's contribution observable and to guard against regressions in
 * specific families (e.g., star, sparse) where structural prechecks should reject in near zero
 * states.
 *
 * <p>
 * This test lives in {@code org.jgrapht.perf.tour} alongside the JMH benchmarks. The
 * {@code jgrapht-core} surefire configuration excludes {@code **&#47;perf/**} from the default
 * test execution, so this harness does not run in normal CI; it is invoked explicitly via
 * {@code mvn -pl jgrapht-core -Dtest=HamiltonianPathStateCountTest test}. The diagnostic
 * columns are printed to stdout because exact state counts vary with future pruning changes;
 * the only enforced assertion is existence agreement between
 * {@link BacktrackingHamiltonianPath} and {@link HeldKarpHamiltonianPath}.
 */
public class HamiltonianPathStateCountTest
{

    private static final int[] SIZES = { 8, 12, 16 };

    @Test
    public void stateCountsByFamily()
    {
        System.out.printf("%-18s %4s %-10s %12s %12s %12s%n",
            "family", "n", "result", "bt_states", "bt_ms", "hk_ms");
        System.out.println(repeat('-', 75));
        for (GraphFamily family : GraphFamily.values()) {
            for (int n : SIZES) {
                runOne(family, n);
            }
        }
    }

    private void runOne(GraphFamily family, int n)
    {
        Graph<Integer, DefaultEdge> graph = GraphBuilders.build(family, n);

        BacktrackingHamiltonianPath<Integer, DefaultEdge> bt = new BacktrackingHamiltonianPath<>();
        long btStart = System.nanoTime();
        HamiltonianPathSearchResult<Integer, DefaultEdge> btResult = bt.getPath(graph);
        long btMicros = (System.nanoTime() - btStart) / 1_000L;

        // Held-Karp respects its maxVertices ceiling; all benchmark sizes are well under it.
        HeldKarpHamiltonianPath<Integer, DefaultEdge> hk = new HeldKarpHamiltonianPath<>();
        long hkStart = System.nanoTime();
        HamiltonianPathSearchResult<Integer, DefaultEdge> hkResult = hk.getPath(graph);
        long hkMicros = (System.nanoTime() - hkStart) / 1_000L;

        boolean btFound = btResult.getPath().isPresent();
        boolean hkFound = hkResult.getPath().isPresent();
        assertEquals(
            btFound, hkFound,
            () -> "existence disagreement for family=" + family + " n=" + n);

        String result = btFound ? "path" : "no path";
        System.out.printf("%-18s %4d %-10s %12d %12.3f %12.3f%n",
            family, n, result, bt.getStatesExpanded(),
            btMicros / 1000.0, hkMicros / 1000.0);
    }

    private static String repeat(char c, int count)
    {
        char[] buf = new char[count];
        java.util.Arrays.fill(buf, c);
        return new String(buf);
    }
}
