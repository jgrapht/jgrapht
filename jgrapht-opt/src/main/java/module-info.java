module org.jgrapht.opt
{
    exports org.jgrapht.opt.graph.fastutil;
    exports org.jgrapht.opt.graph.sparse;
	exports org.jgrapht.opt.graph.webgraph;

    requires transitive org.jgrapht.core;
    requires transitive it.unimi.dsi.fastutil;
	requires transitive it.unimi.dsi.webgraph;
	requires transitive webgraph.big;
	requires transitive it.unimi.dsi.dsiutils;
	requires transitive it.unimi.dsi.sux4j;
	requires transitive com.google.common;
}
