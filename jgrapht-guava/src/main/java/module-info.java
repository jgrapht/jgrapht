/**
 * Provides adaptors for the Google Guava graphs to be used with the
 * JGraphT library.
 * 
 * @since 1.5.0
 */
module org.jgrapht.guava
{
    exports org.jgrapht.graph.guava;

    requires transitive org.jgrapht.core;
    requires transitive com.google.common;
    requires transitive org.jheaps;
}
