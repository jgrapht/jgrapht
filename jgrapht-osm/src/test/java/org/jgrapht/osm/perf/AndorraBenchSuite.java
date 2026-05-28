/*
 * (C) Copyright 2026-2026, by Shai Eilat and Contributors.
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
package org.jgrapht.osm.perf;

import org.junit.jupiter.api.*;

import java.nio.file.*;

/**
 * Manual JMH entry points for the {@code org.jgrapht.osm.perf.Andorra*Bench} harnesses.
 * Class name ends in {@code Suite} (not {@code Test}) so the maven-surefire default
 * include pattern does not pick it up; each method is invoked explicitly via, for
 * example, {@code mvn -pl jgrapht-osm -Dtest=AndorraBenchSuite#runM2M test}.
 *
 * <p>
 * Each method writes its JMH summary to {@code target/jmh-andorra/<bench>.txt} so the
 * results survive surefire's stdout buffering. Copy this class to add new entry points
 * for your own benches; the {@link JmhBenchRunner} helpers handle the JMH plumbing.
 *
 * @author Shai Eilat
 */
class AndorraBenchSuite
{
    private static final Path OUT_DIR = Path.of("target", "jmh-andorra");

    @Test
    void runM2M() throws Exception
    {
        JmhBenchRunner.runAverageTime(
            AndorraDijkstraManyToManyShortestPathsBench.class, OUT_DIR.resolve("m2m.txt"));
    }

    @Test
    void runADP() throws Exception
    {
        JmhBenchRunner.runAverageTime(
            AndorraAllDirectedPathsNonSimpleBench.class, OUT_DIR.resolve("adp.txt"));
    }
}
