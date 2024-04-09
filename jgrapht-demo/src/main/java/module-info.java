/**
 * Provides demo applications of the JGraphT library.
 * 
 * @since 1.5.0
 */
module org.jgrapht.demo
{
    exports org.jgrapht.demo;

    requires transitive org.jgrapht.core;
    requires transitive org.jgrapht.io;
    requires transitive org.jgrapht.ext;

    requires java.desktop;
}
