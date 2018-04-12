/*
 * (C) Copyright 2018-2018, by Emilio Cruciani and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht.generate;

import org.junit.Test;

/**
 * .
 *
 * @author Emilio Cruciani
 * @since April 2018
 */
public class PlantedPartitionGraphGeneratorTest
{
    private final long SEED = 5;

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeL() {
        new PlantedPartitionGraphGenerator<>(-5, 10, 0.5, 0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeK() {
        new PlantedPartitionGraphGenerator<>(5, -10, 0.5, 0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeP() {
        new PlantedPartitionGraphGenerator<>(5, 10, -0.5, 0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeQ() {
        new PlantedPartitionGraphGenerator<>(5, 10, 0.5, -0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooLargeP() {
        new PlantedPartitionGraphGenerator<>(5, 10, 1.5, 0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooLargeQ() {
        new PlantedPartitionGraphGenerator<>(5, 10, 0.5, 1.1);
    }

}
