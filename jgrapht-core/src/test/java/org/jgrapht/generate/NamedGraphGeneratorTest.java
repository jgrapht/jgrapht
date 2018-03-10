/*
 * (C) Copyright 2017-2018, by Joris Kinable and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.generate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Array;
import java.util.Arrays;
import java.util.List;

import org.jgrapht.*;
import org.jgrapht.alg.shortestpath.*;
import org.jgrapht.graph.*;
import org.junit.*;

/**
 * Tests for NamedGraphGenerator
 *
 * @author Joris Kinable
 */
public class NamedGraphGeneratorTest
{

    @Test
    public void testDoyleGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.doyleGraph();
        this.validateBasics(g, 27, 54, 3, 3, 5);
        assertTrue(GraphTests.isEulerian(g));
    }

    @Test
    public void testBullGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.bullGraph();
        this.validateBasics(g, 5, 5, 2, 3, 3);
    }

    @Test
    public void testClawGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.clawGraph();
        this.validateBasics(g, 4, 3, 1, 2, Integer.MAX_VALUE);
        assertTrue(GraphTests.isBipartite(g));
    }

    @Test
    public void testBuckyBallGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.buckyBallGraph();
        this.validateBasics(g, 60, 90, 9, 9, 5);
        assertTrue(GraphTests.isCubic(g));
    }

    @Test
    public void testClebschGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.clebschGraph();
        this.validateBasics(g, 16, 40, 2, 2, 4);
    }

    @Test
    public void testGrötzschGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.grötzschGraph();
        this.validateBasics(g, 11, 20, 2, 2, 4);
    }

    @Test
    public void testBidiakisCubeGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.bidiakisCubeGraph();
        this.validateBasics(g, 12, 18, 3, 3, 4);
        assertTrue(GraphTests.isCubic(g));
    }

    @Test
    public void testBlanusaFirstSnarkGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.blanusaFirstSnarkGraph();
        this.validateBasics(g, 18, 27, 4, 4, 5);
        assertTrue(GraphTests.isCubic(g));
    }

    @Test
    public void testBlanusaSecondSnarkGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.blanusaSecondSnarkGraph();
        this.validateBasics(g, 18, 27, 4, 4, 5);
        assertTrue(GraphTests.isCubic(g));
    }

    @Test
    public void testDoubleStarSnarkGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.doubleStarSnarkGraph();
        this.validateBasics(g, 30, 45, 4, 4, 6);
    }

    @Test
    public void testBrinkmannGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.brinkmannGraph();
        this.validateBasics(g, 21, 42, 3, 3, 5);
        assertTrue(GraphTests.isEulerian(g));
    }

    @Test
    public void testGossetGraphGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.gossetGraph();
        this.validateBasics(g, 56, 756, 3, 3, 3);
    }

    @Test
    public void testChvatalGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.chvatalGraph();
        this.validateBasics(g, 12, 24, 2, 2, 4);
        assertTrue(GraphTests.isEulerian(g));
    }

    @Test
    public void testKittellGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.kittellGraph();
        this.validateBasics(g, 23, 63, 3, 4, 3);
    }

    @Test
    public void testCoxeterGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.coxeterGraph();
        this.validateBasics(g, 28, 42, 4, 4, 7);
        assertTrue(GraphTests.isCubic(g));
    }

    @Test
    public void testEllinghamHorton54Graph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.ellinghamHorton54Graph();
        this.validateBasics(g, 54, 81, 9, 10, 6);
        assertTrue(GraphTests.isCubic(g));
        assertTrue(GraphTests.isBipartite(g));
    }

    @Test
    public void testEllinghamHorton78Graph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.ellinghamHorton78Graph();
        this.validateBasics(g, 78, 117, 7, 13, 6);
        assertTrue(GraphTests.isCubic(g));
        assertTrue(GraphTests.isBipartite(g));
    }

    @Test
    public void testErreraGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.erreraGraph();
        this.validateBasics(g, 17, 45, 3, 4, 3);
    }

    @Test
    public void testFranklinGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.franklinGraph();
        this.validateBasics(g, 12, 18, 3, 3, 4);
        assertTrue(GraphTests.isCubic(g));
        assertTrue(GraphTests.isBipartite(g));
    }

    @Test
    public void testFrughtGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.fruchtGraph();
        this.validateBasics(g, 12, 18, 3, 4, 3);
        assertTrue(GraphTests.isCubic(g));
    }

    @Test
    public void testGoldnerHararyGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.goldnerHararyGraph();
        this.validateBasics(g, 11, 27, 2, 2, 3);
    }

    @Test
    public void testHeawoodGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.heawoodGraph();
        this.validateBasics(g, 14, 21, 3, 3, 6);
        assertTrue(GraphTests.isCubic(g));
        assertTrue(GraphTests.isBipartite(g));
    }

    @Test
    public void testHerschelGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.herschelGraph();
        this.validateBasics(g, 11, 18, 3, 4, 4);
        assertTrue(GraphTests.isBipartite(g));
    }

    @Test
    public void testHoffmanGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.hoffmanGraph();
        this.validateBasics(g, 16, 32, 3, 4, 4);
        assertTrue(GraphTests.isBipartite(g));
    }

    @Test
    public void testKrackhardtKiteGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.krackhardtKiteGraph();
        this.validateBasics(g, 10, 18, 2, 4, 3);
    }

    @Test
    public void testKlein3RegularGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.klein3RegularGraph();
        this.validateBasics(g, 56, 84, 6, 6, 7);
        assertTrue(GraphTests.isCubic(g));
    }

    @Test
    public void testKlein7RegularGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.klein7RegularGraph();
        this.validateBasics(g, 24, 84, 3, 3, 3);
    }

    @Test
    public void testMoserSpindleGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.moserSpindleGraph();
        this.validateBasics(g, 7, 11, 2, 2, 3);
    }

    @Test
    public void testPoussinGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.poussinGraph();
        this.validateBasics(g, 15, 39, 3, 3, 3);
    }

    @Test
    public void testSchläfliGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.schläfliGraph();
        this.validateBasics(g, 27, 216, 2, 2, 3);
    }

    @Test
    public void testThomsenGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.thomsenGraph();
        this.validateBasics(g, 6, 9, 2, 2, 4);
        assertTrue(GraphTests.isBipartite(g));
    }
    
    @Test
    public void testCirculantGraph()
    {
    	List<Integer> exampleList = Arrays.asList(1,2);
    	Graph<Integer, DefaultEdge> g = NamedGraphGenerator.circulantGraph(13,exampleList);
    	this.validateBasics(g, 13, 66, 0, 0, 0);
    }
    
    @Test
    public void testBalaban10Cage() 
    {
    	Graph<Integer, DefaultEdge> g = NamedGraphGenerator.balaban10Cage();
    	this.validateBasics(g, 70, 105, 6, 6, 10);
    }

    private <V, E> void validateBasics(
        Graph<V, E> g, int vertices, int edges, int radius, int diameter, double girth)
    {
        assertEquals(vertices, g.vertexSet().size());
        assertEquals(edges, g.edgeSet().size());
        GraphMeasurer<V, E> gm = new GraphMeasurer<>(g);
        assertEquals(radius, gm.getRadius(), 0.00000001);
        assertEquals(diameter, gm.getDiameter(), 0.00000001);
        assertEquals(girth, GraphMetrics.getGirth(g), 0.00000001);
    }
}
