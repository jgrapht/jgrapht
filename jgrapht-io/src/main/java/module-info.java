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
