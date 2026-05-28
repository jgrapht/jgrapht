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

import org.jgrapht.alg.connectivity.*;
import org.jgrapht.graph.*;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.zip.*;

/**
 * Reads a Geofabrik-style OpenStreetMap GPKG snapshot of a region's road network and
 * writes the largest strongly-connected component as a pair of gzipped CSV files that
 * {@link OsmCsvGraphLoader} loads back into a {@code Graph<Integer, ...>}.
 *
 * <p>
 * Output schema (both files are headerless, gzip-compressed UTF-8 CSV):
 * <ul>
 *   <li>{@code <prefix>.csv.gz} &mdash; {@code src,dst,weight_m} (one directed edge per
 *       line, weight is the Haversine great-circle distance in metres). Headerless so
 *       the file can be consumed directly by {@code CSVImporter} from
 *       {@code jgrapht-io}.</li>
 *   <li>{@code <prefix>.nodes.csv.gz} &mdash; {@code node_id,lat,lon} (one vertex per
 *       line, coordinates in decimal degrees). Loaded via
 *       {@link OsmCoordinatesReader}.</li>
 * </ul>
 *
 * <p>
 * Invoke from the command line:
 *
 * <pre>{@code
 * java --module-path <...> --module org.jgrapht.osm/org.jgrapht.osm.GpkgRoadGraphPreprocessor \
 *     /path/to/region.gpkg \
 *     /path/to/region-edges.csv.gz
 * }</pre>
 *
 * <p>
 * or programmatically via {@link #run(Path, Path)} from any test or application code.
 *
 * <h2>GPKG schema assumptions</h2>
 *
 * Geofabrik free-tier extracts (e.g.
 * {@code https://download.geofabrik.de/europe/andorra-latest-free.gpkg.zip}) ship a
 * {@code gis_osm_roads_free} table whose columns include {@code oneway} (B / F / T,
 * default B) and {@code geom} (GPKG-wrapped WKB {@code LINESTRING}). The preprocessor
 * filters to a routable {@code fclass} whitelist (motorway through service), parses each
 * line-string into directed edges, runs Tarjan-equivalent SCC analysis via
 * {@link KosarajuStrongConnectivityInspector}, keeps the largest component, deduplicates
 * parallel edges keeping the shortest, and writes the result.
 *
 * <p>
 * The class only depends on the GPKG / SQLite layout, not on any specific region, so it
 * works against any free-tier Geofabrik download.
 *
 * @author Shai Eilat
 */
public final class GpkgRoadGraphPreprocessor
{
    /** Geofabrik {@code fclass} values considered routable road segments. */
    public static final Set<String> ROUTABLE_FCLASSES = Set.of(
        "motorway", "motorway_link",
        "trunk", "trunk_link",
        "primary", "primary_link",
        "secondary", "secondary_link",
        "tertiary", "tertiary_link",
        "unclassified", "residential", "living_street",
        "service", "road");

    /**
     * Multiplier for snapping {@code (lon, lat)} pairs to integer keys before deduping
     * shared endpoints between adjacent line-strings. {@code 1e7} = ~1.1 cm at the
     * equator, coarse enough to dedupe touching segments and fine enough to keep
     * distinct nearby intersections distinct.
     */
    public static final double COORD_PRECISION = 1e7;

    private GpkgRoadGraphPreprocessor()
    {
    }

    /**
     * CLI entry point.
     *
     * @param args two arguments: {@code <input.gpkg>} and {@code <output-edges.csv.gz>}
     * @throws Exception on I/O, SQL, or parse failure
     */
    public static void main(String[] args) throws Exception
    {
        if (args.length != 2) {
            System.err.println(
                "Usage: GpkgRoadGraphPreprocessor <input.gpkg> <output-edges.csv.gz>");
            System.exit(2);
        }
        Result result = run(Path.of(args[0]), Path.of(args[1]));
        System.out.printf(
            "input segments: %d%n"
                + "vertices (all): %d%n"
                + "largest SCC vertices: %d%n"
                + "edges in largest SCC (after parallel-edge dedupe): %d%n"
                + "edges file: %s%n"
                + "nodes file: %s%n",
            result.inputSegments,
            result.verticesAll,
            result.verticesInLargestScc,
            result.edgesInLargestScc,
            result.edgesOut,
            result.nodesOut);
    }

    /**
     * Library entry point.
     *
     * @param gpkgPath path to the input Geofabrik GPKG
     * @param edgesOutPath destination for the edges CSV; the nodes CSV is written
     *        alongside with suffix {@code .nodes.csv.gz}
     * @return summary statistics
     * @throws IOException on I/O failure
     * @throws SQLException on GPKG / SQLite read failure
     */
    public static Result run(Path gpkgPath, Path edgesOutPath)
        throws IOException, SQLException
    {
        Path nodesOutPath = deriveNodesPath(edgesOutPath);

        Map<Long, Integer> coordToId = new LinkedHashMap<>();
        List<double[]> coordLatLon = new ArrayList<>();
        List<long[]> rawEdges = new ArrayList<>();
        long[] segCountBox = { 0L };

        readSegments(
            gpkgPath, (lon1, lat1, lon2, lat2, oneway) -> {
                segCountBox[0]++;
                long k1 = packCoordKey(lon1, lat1);
                long k2 = packCoordKey(lon2, lat2);
                if (k1 == k2) {
                    return;
                }
                int a = idForCoord(coordToId, coordLatLon, k1, lat1, lon1);
                int b = idForCoord(coordToId, coordLatLon, k2, lat2, lon2);
                double weight = HaversineHeuristic.distanceMeters(lat1, lon1, lat2, lon2);
                if ("T".equals(oneway)) {
                    rawEdges.add(packEdge(b, a, weight));
                } else {
                    rawEdges.add(packEdge(a, b, weight));
                    if (!"F".equals(oneway)) {
                        rawEdges.add(packEdge(b, a, weight));
                    }
                }
            });

        int verticesAll = coordLatLon.size();
        Set<Integer> largestScc = findLargestScc(verticesAll, rawEdges);

        int[] remap = new int[verticesAll];
        Arrays.fill(remap, -1);
        List<Integer> sortedScc = new ArrayList<>(largestScc);
        Collections.sort(sortedScc);
        for (int i = 0; i < sortedScc.size(); i++) {
            remap[sortedScc.get(i)] = i;
        }

        // Deduplicate parallel edges, keeping the shortest weight.
        Map<Long, Double> bestEdge = new LinkedHashMap<>();
        for (long[] e : rawEdges) {
            int newSrc = remap[(int) e[0]];
            int newDst = remap[(int) e[1]];
            if (newSrc < 0 || newDst < 0) {
                continue;
            }
            double weight = Double.longBitsToDouble(e[2]);
            long key = (((long) newSrc) << 32) | (newDst & 0xffffffffL);
            bestEdge.merge(key, weight, Math::min);
        }

        Path edgesAbs = edgesOutPath.toAbsolutePath();
        if (edgesAbs.getParent() != null) {
            Files.createDirectories(edgesAbs.getParent());
        }
        writeEdgesCsv(edgesOutPath, bestEdge);
        writeNodesCsv(nodesOutPath, sortedScc, coordLatLon);

        return new Result(
            segCountBox[0], verticesAll, sortedScc.size(), bestEdge.size(),
            edgesOutPath, nodesOutPath);
    }

    private static Path deriveNodesPath(Path edgesOutPath)
    {
        String name = edgesOutPath.getFileName().toString();
        String nodesName = name.endsWith(".csv.gz")
            ? name.substring(0, name.length() - ".csv.gz".length()) + ".nodes.csv.gz"
            : name + ".nodes.csv.gz";
        Path parent = edgesOutPath.getParent();
        return parent != null ? parent.resolve(nodesName) : Path.of(nodesName);
    }

    private static long packCoordKey(double lon, double lat)
    {
        long lonKey = Math.round(lon * COORD_PRECISION);
        long latKey = Math.round(lat * COORD_PRECISION);
        return (lonKey << 32) | (latKey & 0xffffffffL);
    }

    private static int idForCoord(
        Map<Long, Integer> coordToId, List<double[]> coordLatLon,
        long key, double lat, double lon)
    {
        Integer existing = coordToId.get(key);
        if (existing != null) {
            return existing;
        }
        int id = coordLatLon.size();
        coordToId.put(key, id);
        coordLatLon.add(new double[] { lat, lon });
        return id;
    }

    /**
     * Packs a directed edge into a {@code long[3]} tuple of {@code {src, dst,
     * doubleToRawLongBits(weight)}}. The packed form keeps the per-edge memory low
     * during the in-memory accumulation pass before SCC analysis runs.
     */
    private static long[] packEdge(int src, int dst, double weight)
    {
        return new long[] { src, dst, Double.doubleToRawLongBits(weight) };
    }

    private static void readSegments(Path gpkgPath, SegmentSink sink)
        throws SQLException, IOException
    {
        String jdbcUrl = "jdbc:sqlite:" + gpkgPath.toAbsolutePath();
        String inClause = String.join(",", Collections.nCopies(ROUTABLE_FCLASSES.size(), "?"));
        String sql =
            "SELECT oneway, geom FROM gis_osm_roads_free WHERE fclass IN (" + inClause + ")";
        try (Connection con = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = con.prepareStatement(sql))
        {
            int idx = 1;
            for (String fclass : ROUTABLE_FCLASSES) {
                ps.setString(idx++, fclass);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String onewayRaw = rs.getString(1);
                    String oneway =
                        (onewayRaw == null ? "B" : onewayRaw).toUpperCase(Locale.ROOT);
                    byte[] blob = rs.getBytes(2);
                    if (blob == null || blob.length < 8) {
                        continue;
                    }
                    double[][] points = parseGpkgLinestring(blob);
                    for (int i = 0; i < points.length - 1; i++) {
                        double[] p1 = points[i];
                        double[] p2 = points[i + 1];
                        sink.accept(p1[0], p1[1], p2[0], p2[1], oneway);
                    }
                }
            }
        }
    }

    /**
     * Parses a GPKG geometry blob holding a 2D LINESTRING and returns the vertices as
     * {@code [{lon, lat}, ...]}. Sufficient for the road graph use case; not a general
     * GPKG / WKB parser.
     */
    static double[][] parseGpkgLinestring(byte[] blob) throws IOException
    {
        if (blob.length < 8 || blob[0] != 'G' || blob[1] != 'P') {
            throw new IOException("not a GPKG geometry blob");
        }
        int flags = blob[3] & 0xff;
        int envType = (flags >> 1) & 0x07;
        int envSize = switch (envType) {
            case 0 -> 0;
            case 1 -> 32;
            case 2, 3 -> 48;
            case 4 -> 64;
            default -> throw new IOException("unknown GPKG envelope type: " + envType);
        };
        int wkbStart = 8 + envSize;
        if (blob.length < wkbStart + 9) {
            throw new IOException("GPKG blob truncated: header says wkb starts at "
                + wkbStart + " but blob length is " + blob.length);
        }
        ByteBuffer buf = ByteBuffer.wrap(blob, wkbStart, blob.length - wkbStart);
        byte byteOrder = buf.get();
        buf.order(byteOrder == 1 ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        int wkbType = buf.getInt();
        if (wkbType != 2) {
            throw new IOException("expected LINESTRING (wkb type 2), got " + wkbType);
        }
        int n = buf.getInt();
        double[][] points = new double[n][2];
        for (int i = 0; i < n; i++) {
            points[i][0] = buf.getDouble();
            points[i][1] = buf.getDouble();
        }
        return points;
    }

    private static Set<Integer> findLargestScc(int verticesAll, List<long[]> rawEdges)
    {
        // SimpleDirectedGraph silently drops parallel edges, which is fine for SCC
        // analysis: connectivity is unchanged by edge multiplicity.
        SimpleDirectedGraph<Integer, DefaultEdge> g =
            new SimpleDirectedGraph<>(DefaultEdge.class);
        for (int i = 0; i < verticesAll; i++) {
            g.addVertex(i);
        }
        for (long[] e : rawEdges) {
            int u = (int) e[0];
            int v = (int) e[1];
            if (u != v) {
                g.addEdge(u, v);
            }
        }
        KosarajuStrongConnectivityInspector<Integer, DefaultEdge> scc =
            new KosarajuStrongConnectivityInspector<>(g);
        Set<Integer> best = Collections.emptySet();
        for (Set<Integer> comp : scc.stronglyConnectedSets()) {
            if (comp.size() > best.size()) {
                best = comp;
            }
        }
        return best;
    }

    private static void writeEdgesCsv(Path edgesOutPath, Map<Long, Double> bestEdge)
        throws IOException
    {
        try (Writer w = newGzWriter(edgesOutPath)) {
            for (Map.Entry<Long, Double> e : bestEdge.entrySet()) {
                long key = e.getKey();
                int src = (int) (key >>> 32);
                int dst = (int) (key & 0xffffffffL);
                w.write(Integer.toString(src));
                w.write(',');
                w.write(Integer.toString(dst));
                w.write(',');
                w.write(String.format(Locale.ROOT, "%.4f", e.getValue()));
                w.write('\n');
            }
        }
    }

    private static void writeNodesCsv(
        Path nodesOutPath, List<Integer> sortedScc, List<double[]> coordLatLon)
        throws IOException
    {
        try (Writer w = newGzWriter(nodesOutPath)) {
            for (int newId = 0; newId < sortedScc.size(); newId++) {
                int oldId = sortedScc.get(newId);
                double[] c = coordLatLon.get(oldId);
                w.write(Integer.toString(newId));
                w.write(',');
                w.write(String.format(Locale.ROOT, "%.7f", c[0]));
                w.write(',');
                w.write(String.format(Locale.ROOT, "%.7f", c[1]));
                w.write('\n');
            }
        }
    }

    private static Writer newGzWriter(Path path) throws IOException
    {
        return new BufferedWriter(
            new OutputStreamWriter(
                new GZIPOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(path))),
                StandardCharsets.UTF_8));
    }

    /** Per-segment sink used to accumulate raw directed edges from the GPKG read. */
    @FunctionalInterface
    private interface SegmentSink
    {
        void accept(double lon1, double lat1, double lon2, double lat2, String oneway);
    }

    /** Summary statistics from a preprocessor run. */
    public static final class Result
    {
        /** Number of consecutive vertex pairs (segments) read from the GPKG. */
        public final long inputSegments;
        /** Total distinct vertex count across all components. */
        public final int verticesAll;
        /** Vertex count in the largest strongly-connected component. */
        public final int verticesInLargestScc;
        /** Edge count after parallel-edge dedup. */
        public final int edgesInLargestScc;
        /** Edges CSV output path. */
        public final Path edgesOut;
        /** Nodes CSV output path. */
        public final Path nodesOut;

        Result(
            long inputSegments, int verticesAll, int verticesInLargestScc,
            int edgesInLargestScc, Path edgesOut, Path nodesOut)
        {
            this.inputSegments = inputSegments;
            this.verticesAll = verticesAll;
            this.verticesInLargestScc = verticesInLargestScc;
            this.edgesInLargestScc = edgesInLargestScc;
            this.edgesOut = edgesOut;
            this.nodesOut = nodesOut;
        }
    }
}
