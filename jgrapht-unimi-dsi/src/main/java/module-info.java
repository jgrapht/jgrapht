module org.jgrapht.unimi.dsi
{
    exports org.jgrapht.webgraph;

    requires transitive org.jgrapht.core;
    requires transitive it.unimi.dsi.fastutil;
    requires transitive it.unimi.dsi.webgraph;
    requires transitive it.unimi.dsi.big.webgraph;
    requires transitive it.unimi.dsi.dsiutils;
    requires transitive it.unimi.dsi.sux4j;
    requires transitive com.google.common;    
}
