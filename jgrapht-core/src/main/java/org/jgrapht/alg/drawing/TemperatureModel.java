/*
 * (C) Copyright 2018-2019, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.drawing;

/**
 * A general interface for a temperature model appropriate for running Simulated Annealing.
 * 
 * <p>
 * The temperature should start from a high enough value and gradually become zero.
 * 
 * @author Dimitrios Michail
 */
public interface TemperatureModel
{

    /**
     * Return the temperature for the new iteration
     * 
     * @param iteration the next iteration
     * @param maxIterations total number of iterations
     * @return the temperature for the next iteration
     */
    double temperature(int iteration, int maxIterations);

}
