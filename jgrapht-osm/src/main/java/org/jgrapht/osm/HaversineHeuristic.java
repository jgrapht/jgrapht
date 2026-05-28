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
package org.jgrapht.osm;

import org.jgrapht.alg.interfaces.*;

import java.util.*;
import java.util.function.*;

/**
 * Great-circle (Haversine) admissible heuristic for A* over geographic graphs.
 * Given a coordinate lookup that returns {@code {lat, lon}} in decimal degrees for a
 * vertex, this heuristic returns the great-circle distance in metres between two
 * vertices. The heuristic is admissible whenever edge weights represent ground distance
 * in metres along a path no shorter than the great-circle line.
 *
 * <p>
 * The default sphere radius is the IUGG mean Earth radius (6,371,008.8 m); supply a
 * custom radius to model bodies other than Earth or to match a preprocessing routine
 * that used a different constant.
 *
 * @param <V> the vertex type
 *
 * @author Shai Eilat
 */
public final class HaversineHeuristic<V>
    implements AStarAdmissibleHeuristic<V>
{
    /** IUGG mean Earth radius in metres. */
    public static final double EARTH_RADIUS_M = 6_371_008.8;

    private final Function<V, double[]> coordinates;
    private final double radiusMeters;

    /**
     * Constructs a heuristic backed by the given coordinate lookup, using the default
     * Earth radius.
     *
     * @param coordinates maps a vertex to {@code {lat, lon}} in decimal degrees
     */
    public HaversineHeuristic(Function<V, double[]> coordinates)
    {
        this(coordinates, EARTH_RADIUS_M);
    }

    /**
     * Constructs a heuristic with a custom sphere radius.
     *
     * @param coordinates maps a vertex to {@code {lat, lon}} in decimal degrees
     * @param radiusMeters the sphere radius in metres
     */
    public HaversineHeuristic(Function<V, double[]> coordinates, double radiusMeters)
    {
        this.coordinates = Objects.requireNonNull(coordinates, "coordinates");
        if (!(radiusMeters > 0)) {
            throw new IllegalArgumentException("radiusMeters must be positive");
        }
        this.radiusMeters = radiusMeters;
    }

    /**
     * Convenience factory for the common case where coordinates are stored in a
     * {@link Map}.
     *
     * @param coords the coordinate map
     * @param <V> the vertex type
     * @return a heuristic backed by {@code coords::get}
     */
    public static <V> HaversineHeuristic<V> ofMap(Map<V, double[]> coords)
    {
        Objects.requireNonNull(coords, "coords");
        return new HaversineHeuristic<>(coords::get);
    }

    @Override
    public double getCostEstimate(V source, V target)
    {
        double[] s = coordinates.apply(source);
        double[] t = coordinates.apply(target);
        if (s == null || t == null) {
            return 0.0;
        }
        return distanceMeters(s[0], s[1], t[0], t[1], radiusMeters);
    }

    /**
     * Computes the great-circle distance in metres between two
     * {@code (lat, lon)} points (in decimal degrees) on Earth, using the
     * {@link #EARTH_RADIUS_M default radius}.
     *
     * @param lat1 latitude of the first point in decimal degrees
     * @param lon1 longitude of the first point in decimal degrees
     * @param lat2 latitude of the second point in decimal degrees
     * @param lon2 longitude of the second point in decimal degrees
     * @return the great-circle distance in metres
     */
    public static double distanceMeters(
        double lat1, double lon1, double lat2, double lon2)
    {
        return distanceMeters(lat1, lon1, lat2, lon2, EARTH_RADIUS_M);
    }

    /**
     * Computes the great-circle distance in metres between two
     * {@code (lat, lon)} points (in decimal degrees) on a sphere of the given radius.
     *
     * @param lat1 latitude of the first point in decimal degrees
     * @param lon1 longitude of the first point in decimal degrees
     * @param lat2 latitude of the second point in decimal degrees
     * @param lon2 longitude of the second point in decimal degrees
     * @param radiusMeters the sphere radius in metres
     * @return the great-circle distance in metres
     */
    public static double distanceMeters(
        double lat1, double lon1, double lat2, double lon2, double radiusMeters)
    {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dPhi = Math.toRadians(lat2 - lat1);
        double dLambda = Math.toRadians(lon2 - lon1);
        double sdp = Math.sin(dPhi / 2);
        double sdl = Math.sin(dLambda / 2);
        double a = sdp * sdp + Math.cos(phi1) * Math.cos(phi2) * sdl * sdl;
        return 2 * radiusMeters * Math.asin(Math.min(1.0, Math.sqrt(a)));
    }
}
