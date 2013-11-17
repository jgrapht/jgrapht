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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jgrapht.experimental.isomorphism.IsomorphismInspectorTest;


/**
 * A TestSuite for all tests in this package.
 *
 * @author Barak Naveh
 */
public final class AllAlgTests
{
    //~ Constructors -----------------------------------------------------------

    private AllAlgTests()
    {
    } // ensure non-instantiability.

    //~ Methods ----------------------------------------------------------------

    /**
     * Creates a test suite for all tests in this package.
     *
     * @return a test suite for all tests in this package.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        // $JUnit-BEGIN$
        suite.addTest(new TestSuite(ConnectivityInspectorTest.class));
        suite.addTest(new TestSuite(DijkstraShortestPathTest.class));
        suite.addTest(new TestSuite(BellmanFordShortestPathTest.class));
        suite.addTest(new TestSuite(FloydWarshallShortestPathsTest.class));
        suite.addTest(new TestSuite(VertexCoversTest.class));
        suite.addTest(new TestSuite(CycleDetectorTest.class));
        suite.addTest(new TestSuite(BronKerboschCliqueFinderTest.class));
        suite.addTest(new TestSuite(TransitiveClosureTest.class));
        suite.addTest(new TestSuite(BiconnectivityInspectorTest.class));
        suite.addTest(new TestSuite(BlockCutpointGraphTest.class));
        suite.addTest(new TestSuite(KShortestPathCostTest.class));
        suite.addTest(new TestSuite(KShortestPathKValuesTest.class));
        suite.addTest(new TestSuite(KSPExampleTest.class));
        suite.addTest(new TestSuite(KSPDiscardsValidPathsTest.class));
        suite.addTestSuite(IsomorphismInspectorTest.class);
        suite.addTest(new TestSuite(EdmondsKarpMaximumFlowTest.class));
        suite.addTest(new TestSuite(ChromaticNumberTest.class));
        suite.addTest(new TestSuite(EulerianCircuitTest.class));
        suite.addTest(new TestSuite(HamiltonianCycleTest.class));
        suite.addTest(new TestSuite(MinimumSpanningTreeTest.class));
        suite.addTest(new TestSuite(StoerWagnerMinimumCutTest.class));
        suite.addTest(new TestSuite(EdmondsBlossomShrinkingTest.class));
        suite.addTest(new TestSuite(MinSourceSinkCutTest.class));
        suite.addTest(new TestSuite(HopcroftKarpBipartiteMatchingTest.class));
        suite.addTest(new TestSuite(KuhnMunkresMinimalWeightBipartitePerfectMatchingTest.class));
        suite.addTest(new TestSuite(TarjanLowestCommonAncestorTest.class));
        
        // $JUnit-END$
        return suite;
    }
}

// End AllAlgTests.java
