/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
/* ----------------
 * AllAlgTests.java
 * ----------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 *
 */
package org.jgrapht.alg;

import org.jgrapht.alg.flow.EdmondsKarpMFImplTest;
import org.jgrapht.alg.flow.EdmondsKarpMinimumSTCutTest;
import org.jgrapht.alg.flow.PushRelabelMFImplTest;
import org.jgrapht.alg.flow.PushRelabelMinimumSTCutTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * A TestSuite for all tests in this package.
 *
 * @author Barak Naveh
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    AStarShortestPathTest.class,
    AllDirectedPathsTest.class,
    BellmanFordShortestPathTest.class,
    BiconnectivityInspectorTest.class,
    BidirectionalDijkstraShortestPathTest.class,
    BlockCutpointGraphTest.class,
    BronKerboschCliqueFinderTest.class,
    ChromaticNumberTest.class,
    ConnectivityInspectorTest.class,
    CycleDetectorTest.class,
    DijkstraShortestPathTest.class,
    EdmondsBlossomShrinkingTest.class,
    EdmondsKarpMFImplTest.class,
    EdmondsKarpMinimumSTCutTest.class,
    EulerianCircuitTest.class,
    FloydWarshallShortestPathsTest.class,
    HamiltonianCycleTest.class,
    HopcroftKarpBipartiteMatchingTest.class,
    KShortestPathCostTest.class,
    KShortestPathKValuesTest.class,
    KSPDiscardsValidPathsTest.class,
    KSPExampleTest.class,
    KuhnMunkresMinimalWeightBipartitePerfectMatchingTest.class,
    MinimumSpanningTreeTest.class,
    NaiveLcaFinderTest.class,
    NeighborIndexTest.class,
    PushRelabelMFImplTest.class,
    PushRelabelMinimumSTCutTest.class,
    StoerWagnerMinimumCutTest.class,
    StrongConnectivityAlgorithmTest.class,
    TarjanLowestCommonAncestorTest.class,
    TransitiveClosureTest.class,
    VertexCoverTest.class,
    WeightedVertexCoverTest.class})
public final class AllAlgTests
{
}
// End AllAlgTests.java
