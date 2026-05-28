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

/**
 * OpenStreetMap road-graph integration for JGraphT.
 *
 * <p>
 * Builds {@code Graph<Integer, ...>} instances from Geofabrik free-tier OSM extracts,
 * either by preprocessing a GPKG into edge / node CSVs or by loading a previously
 * preprocessed pair of CSVs directly into a target graph. Pairs with the bundled
 * {@code HaversineHeuristic} for A* heuristics over geographic graphs.
 *
 * @since 1.6.0
 */
module org.jgrapht.osm
{
    exports org.jgrapht.osm;

    requires transitive org.jgrapht.core;
    requires org.jgrapht.io;
    requires java.sql;
}
