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
 * Provides I/O extensions for the JGraphT library.
 * 
 * @since 1.5.0
 */
module org.jgrapht.io
{
    exports org.jgrapht.nio;
    exports org.jgrapht.nio.csv;
    exports org.jgrapht.nio.dimacs;
    exports org.jgrapht.nio.dot;
    exports org.jgrapht.nio.gexf;
    exports org.jgrapht.nio.gml;
    exports org.jgrapht.nio.graph6;
    exports org.jgrapht.nio.graphml;
    exports org.jgrapht.nio.json;
    exports org.jgrapht.nio.lemon;
    exports org.jgrapht.nio.matrix;
    exports org.jgrapht.nio.tsplib;

    requires transitive org.jgrapht.core;
    requires transitive org.apache.commons.text;
    requires transitive java.xml;
    requires transitive org.antlr.antlr4.runtime;
}
