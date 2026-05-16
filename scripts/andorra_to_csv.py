#!/usr/bin/env python3
"""
Andorra GPKG -> edges CSV preprocessor.

One-shot tool: reads a Geofabrik-style OSM-derived GPKG snapshot of Andorra
(or any similar country) and writes a gzip-compressed CSV of the largest
strongly-connected component of its routable road graph. The CSV is then
checked into the test resources so the Java JMH benches can build the
benchmark graph without depending on SQLite/GDAL at test time.

Usage:
    python scripts/andorra_to_csv.py \
        D:/projects/JgraphT/andorra-data/andorra.gpkg \
        jgrapht-core/src/test/resources/perf/osm/andorra-edges.csv.gz

CSV columns: src,dst,weight_m
- src, dst: 0-based integer node ids inside the largest SCC
- weight_m: great-circle distance in metres (Haversine, R = 6_371_008.8 m)

The graph is directed; oneway=B emits both directions, F emits only
forward, T emits only the reverse direction.

Companion node-coordinate file (lat,lon, one row per node id) is also
written next to the edges CSV with suffix `.nodes.csv.gz`. The Java
heuristics (great-circle, ALT-with-coordinates) read it.
"""
from __future__ import annotations

import csv
import gzip
import math
import os
import struct
import sqlite3
import sys
from collections import defaultdict
from typing import Iterator

# Geofabrik "free" GPKG layout: only these fclasses count as routable road
ROUTABLE_FCLASSES = {
    "motorway", "motorway_link",
    "trunk", "trunk_link",
    "primary", "primary_link",
    "secondary", "secondary_link",
    "tertiary", "tertiary_link",
    "unclassified", "residential", "living_street",
    "service", "road",
}

# 1e-7 deg ~= 1.1 cm at the equator. Coarse enough to dedupe shared
# endpoints between adjacent linestrings, fine enough to keep distinct
# nearby intersections distinct.
COORD_PRECISION = 1e7
EARTH_RADIUS_M = 6_371_008.8  # IUGG mean radius


def haversine(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    p1, p2 = math.radians(lat1), math.radians(lat2)
    dp = math.radians(lat2 - lat1)
    dl = math.radians(lon2 - lon1)
    a = math.sin(dp / 2) ** 2 + math.cos(p1) * math.cos(p2) * math.sin(dl / 2) ** 2
    return 2 * EARTH_RADIUS_M * math.asin(math.sqrt(a))


def parse_gpkg_linestring(blob: bytes) -> list[tuple[float, float]]:
    """Return [(lon, lat), ...] from a GPKG geometry blob holding a LINESTRING."""
    assert blob[:2] == b"GP", "not a GPKG geometry blob"
    flags = blob[3]
    env_type = (flags >> 1) & 0x07
    env_size = {0: 0, 1: 32, 2: 48, 3: 48, 4: 64}[env_type]
    wkb = blob[8 + env_size:]
    little_endian = wkb[0] == 1
    fmt_u32 = "<I" if little_endian else ">I"
    fmt_xy = "<dd" if little_endian else ">dd"
    wkb_type = struct.unpack(fmt_u32, wkb[1:5])[0]
    assert wkb_type == 2, f"expected LINESTRING (type 2), got {wkb_type}"
    n = struct.unpack(fmt_u32, wkb[5:9])[0]
    return [struct.unpack(fmt_xy, wkb[9 + i * 16: 9 + (i + 1) * 16]) for i in range(n)]


def iterate_segments(gpkg_path: str) -> Iterator[tuple[float, float, float, float, str]]:
    """Yield (lon1, lat1, lon2, lat2, oneway_flag) for each routable segment."""
    con = sqlite3.connect(gpkg_path)
    try:
        rows = con.execute(
            "SELECT oneway, geom FROM gis_osm_roads_free "
            "WHERE fclass IN ({})".format(",".join("?" * len(ROUTABLE_FCLASSES))),
            tuple(ROUTABLE_FCLASSES),
        )
        for oneway, blob in rows:
            ow = (oneway or "B").upper()
            pts = parse_gpkg_linestring(blob)
            for i in range(len(pts) - 1):
                x1, y1 = pts[i]
                x2, y2 = pts[i + 1]
                yield x1, y1, x2, y2, ow
    finally:
        con.close()


def largest_scc(adj: dict[int, list[int]], n: int) -> set[int]:
    """Iterative Tarjan; returns the vertex set of the largest SCC."""
    index = 0
    ids: dict[int, int] = {}
    low: dict[int, int] = {}
    on_stack: set[int] = set()
    stack: list[int] = []
    comps: list[list[int]] = []

    for start in range(n):
        if start in ids:
            continue
        call = [(start, iter(adj.get(start, ())))]
        ids[start] = index
        low[start] = index
        index += 1
        on_stack.add(start)
        stack.append(start)
        while call:
            v, it = call[-1]
            nxt = next(it, None)
            if nxt is None:
                if low[v] == ids[v]:
                    comp: list[int] = []
                    while True:
                        w = stack.pop()
                        on_stack.discard(w)
                        comp.append(w)
                        if w == v:
                            break
                    comps.append(comp)
                call.pop()
                if call:
                    parent = call[-1][0]
                    if low[v] < low[parent]:
                        low[parent] = low[v]
            else:
                if nxt not in ids:
                    ids[nxt] = index
                    low[nxt] = index
                    index += 1
                    on_stack.add(nxt)
                    stack.append(nxt)
                    call.append((nxt, iter(adj.get(nxt, ()))))
                elif nxt in on_stack:
                    if ids[nxt] < low[v]:
                        low[v] = ids[nxt]

    comps.sort(key=len, reverse=True)
    return set(comps[0]) if comps else set()


def main() -> int:
    if len(sys.argv) != 3:
        print(__doc__, file=sys.stderr)
        return 2
    gpkg_path, edges_out = sys.argv[1], sys.argv[2]
    nodes_out = edges_out.replace(".csv.gz", ".nodes.csv.gz")
    if nodes_out == edges_out:
        nodes_out = edges_out + ".nodes.csv.gz"

    coord_to_id: dict[tuple[int, int], int] = {}
    coord_lat_lon: list[tuple[float, float]] = []
    raw_edges: list[tuple[int, int, float]] = []

    seg_count = 0
    for x1, y1, x2, y2, ow in iterate_segments(gpkg_path):
        seg_count += 1
        k1 = (round(x1 * COORD_PRECISION), round(y1 * COORD_PRECISION))
        k2 = (round(x2 * COORD_PRECISION), round(y2 * COORD_PRECISION))
        if k1 == k2:
            continue
        for key, lon, lat in ((k1, x1, y1), (k2, x2, y2)):
            if key not in coord_to_id:
                coord_to_id[key] = len(coord_lat_lon)
                coord_lat_lon.append((lat, lon))
        a = coord_to_id[k1]
        b = coord_to_id[k2]
        w = haversine(y1, x1, y2, x2)
        if ow == "T":
            raw_edges.append((b, a, w))
        else:
            raw_edges.append((a, b, w))
            if ow != "F":
                raw_edges.append((b, a, w))

    n = len(coord_lat_lon)
    adj: dict[int, list[int]] = defaultdict(list)
    for u, v, _ in raw_edges:
        adj[u].append(v)

    scc = largest_scc(adj, n)
    remap = {old: new for new, old in enumerate(sorted(scc))}

    out_edges = [(remap[u], remap[v], w) for u, v, w in raw_edges if u in scc and v in scc]
    # Deduplicate parallel edges keeping the shortest
    best: dict[tuple[int, int], float] = {}
    for u, v, w in out_edges:
        if (u, v) not in best or w < best[(u, v)]:
            best[(u, v)] = w
    out_edges = [(u, v, w) for (u, v), w in best.items()]

    os.makedirs(os.path.dirname(edges_out), exist_ok=True)
    with gzip.open(edges_out, "wt", newline="") as f:
        w = csv.writer(f)
        w.writerow(["src", "dst", "weight_m"])
        for u, v, weight in out_edges:
            w.writerow([u, v, f"{weight:.4f}"])

    with gzip.open(nodes_out, "wt", newline="") as f:
        w = csv.writer(f)
        w.writerow(["node_id", "lat", "lon"])
        for old, new in remap.items():
            lat, lon = coord_lat_lon[old]
            w.writerow([new, f"{lat:.7f}", f"{lon:.7f}"])

    print(
        f"input segments: {seg_count}\n"
        f"vertices (all): {n}\n"
        f"largest SCC vertices: {len(scc)}\n"
        f"edges in largest SCC (after parallel-edge dedupe): {len(out_edges)}\n"
        f"edges file: {edges_out}\n"
        f"nodes file: {nodes_out}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
