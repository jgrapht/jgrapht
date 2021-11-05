module org.jgrapht.core
{
    exports org.jgrapht;
    exports org.jgrapht.alg;
    exports org.jgrapht.alg.clique;
    exports org.jgrapht.alg.clustering;
    exports org.jgrapht.alg.color;
    exports org.jgrapht.alg.connectivity;
    exports org.jgrapht.alg.cycle;
    exports org.jgrapht.alg.decomposition;
    exports org.jgrapht.alg.densesubgraph;
    exports org.jgrapht.alg.drawing;
    exports org.jgrapht.alg.drawing.model;
    exports org.jgrapht.alg.flow;
    exports org.jgrapht.alg.flow.mincost;
    exports org.jgrapht.alg.independentset;
    exports org.jgrapht.alg.interfaces;
    exports org.jgrapht.alg.isomorphism;
    exports org.jgrapht.alg.lca;
    exports org.jgrapht.alg.linkprediction;
    exports org.jgrapht.alg.matching;
    exports org.jgrapht.alg.matching.blossom.v5;
    exports org.jgrapht.alg.partition;
    exports org.jgrapht.alg.planar;
    exports org.jgrapht.alg.scoring;
    exports org.jgrapht.alg.shortestpath;
    exports org.jgrapht.alg.similarity;
    exports org.jgrapht.alg.spanning;
    exports org.jgrapht.alg.tour;
    exports org.jgrapht.alg.transform;
    exports org.jgrapht.alg.util;
    exports org.jgrapht.alg.util.extension;
    exports org.jgrapht.alg.vertexcover;
    exports org.jgrapht.alg.vertexcover.util;
    exports org.jgrapht.event;
    exports org.jgrapht.generate;
    exports org.jgrapht.generate.netgen;
    exports org.jgrapht.graph;
    exports org.jgrapht.graph.builder;
    exports org.jgrapht.graph.concurrent;
    exports org.jgrapht.graph.specifics;
    exports org.jgrapht.traverse;
    exports org.jgrapht.util;

    requires transitive org.jheaps;
    requires transitive org.apfloat;
}
