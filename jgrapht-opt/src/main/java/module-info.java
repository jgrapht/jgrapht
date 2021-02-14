module org.jgrapht.opt
{
    exports org.jgrapht.opt.graph.fastutil;
    exports org.jgrapht.opt.graph.sparse;

    requires transitive org.jgrapht.core;
    requires transitive it.unimi.dsi.fastutil;
}
