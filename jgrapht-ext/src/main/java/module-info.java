/**
 * Provides adaptors for the JGraphT graphs to be used
 * with external libraries.
 * 
 * @since 1.5.0
 */
module org.jgrapht.ext
{
    exports org.jgrapht.ext;

    requires transitive org.jgrapht.core;
    requires transitive com.github.vlsi.mxgraph.jgraphx;
}
