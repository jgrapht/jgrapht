module org.jgrapht.guava
{
    exports org.jgrapht.graph.guava;

    requires transitive org.jgrapht.core;
    requires transitive com.google.common;
    requires transitive org.jheaps;
}
