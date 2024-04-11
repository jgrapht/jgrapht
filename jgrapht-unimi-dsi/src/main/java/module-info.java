/*
 * (C) Copyright 2020-2024, by Dimitrios Michail and Contributors.
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

/**
 * Provides graph implementations using succinct data structures.
 * 
 * @since 1.5.1
 */
module org.jgrapht.unimi.dsi
{
    exports org.jgrapht.webgraph;
    exports org.jgrapht.sux4j;

    requires transitive org.jgrapht.core;
    requires transitive org.jgrapht.opt;
    requires transitive it.unimi.dsi.fastutil;
    requires transitive it.unimi.dsi.webgraph;
    requires transitive it.unimi.dsi.big.webgraph;
    requires transitive it.unimi.dsi.dsiutils;
    requires transitive it.unimi.dsi.sux4j;
    requires transitive com.google.common;
}
