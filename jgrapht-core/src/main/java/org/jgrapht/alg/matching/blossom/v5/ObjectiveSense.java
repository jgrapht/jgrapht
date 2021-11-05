/*
 * (C) Copyright 2018-2021, by Timofey Chudakov and Contributors.
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
package org.jgrapht.alg.matching.blossom.v5;

/**
 * Enum specifying the objective sense of the algorithm. {@link ObjectiveSense#MAXIMIZE} means the
 * goal is to maximize the linear programming objective value, {@link ObjectiveSense#MINIMIZE} - to
 * minimize the linear programming objective value.
 *
 * @author Timofey Chudakov
 * @see KolmogorovWeightedMatching
 * @see KolmogorovWeightedPerfectMatching
 */
public enum ObjectiveSense
{
    MAXIMIZE,
    MINIMIZE
}
