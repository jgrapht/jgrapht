# OSM road-graph benchmark fixtures

The classes in `org.jgrapht.osm.perf` build real road graphs from two gzipped CSV
files that live in this directory:

- `andorra-edges.csv.gz` &mdash; `src,dst,weight_m` per directed edge in the largest
  strongly-connected component of the Andorra road network. **Headerless**, one record
  per line.
- `andorra-edges.nodes.csv.gz` &mdash; `node_id,lat,lon` per vertex. **Headerless.**

**These files are not committed to the repository.** They are derived from the
Geofabrik OpenStreetMap "free" GPKG extract, which is freely redistributable under the
Open Database License but is too large (and changes daily upstream) to vendor here.

## Producing the fixtures locally

1. Download the latest Andorra extract from Geofabrik:

   <https://download.geofabrik.de/europe/andorra-latest-free.gpkg.zip>

   Unzip it; the archive contains a single `andorra.gpkg` (~11 MB).

2. From the repository root, run the preprocessor against the GPKG and write the CSVs
   into this directory. The preprocessor is the production class
   `org.jgrapht.osm.GpkgRoadGraphPreprocessor` in this module; invoke it via Maven:

   ```
   mvn -pl jgrapht-osm -am exec:java \
       -Dexec.mainClass=org.jgrapht.osm.GpkgRoadGraphPreprocessor \
       -Dexec.args="/path/to/andorra.gpkg jgrapht-osm/src/test/resources/perf/osm/andorra-edges.csv.gz"
   ```

   or directly with `java --module-path <...> --module org.jgrapht.osm/org.jgrapht.osm.GpkgRoadGraphPreprocessor <gpkg> <out.csv.gz>`.
   The preprocessor writes both `andorra-edges.csv.gz` and the companion
   `andorra-edges.nodes.csv.gz` next to it. Total output is ~700 KB.

3. After the CSVs are in place, the smoke test and benches load automatically on the
   next `mvn test` / JMH run:

   ```
   mvn -pl jgrapht-osm -Dtest=AndorraGraphLoaderSmokeTest test
   mvn -pl jgrapht-osm -Dtest=AndorraBenchSuite#runM2M test
   ```

   When the fixtures are absent, `AndorraGraphLoaderSmokeTest` skips with an
   explanatory message and `AndorraGraphLoader.load()` raises an
   `IllegalStateException` pointing back here.

## Using a different OSM region

The preprocessor only assumes the standard Geofabrik free-tier GPKG schema
(`gis_osm_roads_free` with `oneway`, `fclass`, `geom` columns). Any other country or
sub-region from <https://download.geofabrik.de/> works the same way; pick a small
region first because the preprocessor runs an in-memory Kosaraju SCC pass.

`OsmCsvGraphLoader.loadGzippedFile(Path)` and `OsmCoordinatesReader.readGzippedFile(Path)`
accept arbitrary paths, so contributors loading a custom region do not need to place
the CSVs under `src/test/resources/perf/osm/` &mdash; they can keep them anywhere on
disk and reference them from their own bench classes.
