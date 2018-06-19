package org.jgrapht.alg.isomorphism;

import org.jgrapht.Graph;
import org.jgrapht.GraphMetrics;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.GraphMeasurer;
import org.jgrapht.alg.shortestpath.TreeMeasurer;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.util.RadixSort;

import java.util.*;

public class AHUTreeIsomorphism<V, E> {
    private final Graph<V, E> graph1;
    private final Graph<V, E> graph2;

    private V root1;
    private V root2;

    private Map<V, V> forwardMapping;
    private Map<V, V> backwardMapping;

    public AHUTreeIsomorphism(Graph<V, E> graph1, Graph<V, E> graph2){
        this(graph1, null, graph2, null);
    }

    public AHUTreeIsomorphism(Graph<V, E> graph1, V root1, Graph<V, E> graph2, V root2){
        this.graph1 = Objects.requireNonNull(graph1);
        this.graph2 = Objects.requireNonNull(graph2);

        this.root1 = root1;
        this.root2 = root2;
    }

    private void bfs(Graph<V, E> graph, V root, List<List<V>> levels){
        BreadthFirstIterator<V, E> bfs = new BreadthFirstIterator<>(graph, root);

        while (bfs.hasNext()){
            V u = bfs.next();

            if (levels.size() < bfs.getDepth(u) + 1){
                levels.add(new ArrayList<>());
            }

            levels.get(bfs.getDepth(u)).add(u);
        }
    }

    private List<List<V>> computeLevels(Graph<V, E> graph, V root){
        List<List<V>> levels = new ArrayList<>();
        bfs(graph, root, levels);
        return levels;
    }

    private boolean isomorphismExists(V root1, V root2){
        this.forwardMapping = new HashMap<>();
        this.backwardMapping = new HashMap<>();

        // Are both graphs empty?
        if (Objects.isNull(root1) && Objects.isNull(root2))
            return true;

        @SuppressWarnings("unchecked")
        Map<V, Integer>[] canonicalName = new Map[2];
        canonicalName[0] = new HashMap<>(graph1.vertexSet().size());
        canonicalName[1] = new HashMap<>(graph2.vertexSet().size());

        List<List<V>> nodesByLevel1 = computeLevels(graph1, root1);
        List<List<V>> nodesByLevel2 = computeLevels(graph2, root2);

        if (nodesByLevel1.size() != nodesByLevel2.size())
            return false;

        final int MAX_LEVEL = nodesByLevel1.size() - 1;

        @SuppressWarnings("unchecked")
        Map<String, Integer> canonicalNameToInt = new HashMap<>();

        int freshName = 0;
        for (int lvl = MAX_LEVEL; lvl >= 0; lvl--) {

            if (lvl % 2 == MAX_LEVEL % 2)
                freshName = 0;

            @SuppressWarnings("unchecked")
            List<V>[] level = new List[2];

            level[0] = nodesByLevel1.get(lvl);
            level[1] = nodesByLevel2.get(lvl);

            if (level[0].size() != level[1].size())
                return false;

            final int n = level[0].size();

            @SuppressWarnings("unchecked")
            List<List<V>>[] sameLabelBags = new List[2];
            sameLabelBags[0] = new ArrayList<>(n);
            sameLabelBags[1] = new ArrayList<>(n);

            for (int k = 0; k < 2; k++) {
                for (int i = 0; i < n; i++) {
                    V u = level[k].get(i);

                    Graph<V, E> graph = graph1;

                    if (k == 1)
                        graph = graph2;

                    List<Integer> list = new ArrayList<>();
                    for (E edge: graph.edgesOf(u)){
                        V v = Graphs.getOppositeVertex(graph, edge, u);
                        int name = canonicalName[k].getOrDefault(v, -1);

                        if (name != -1){
                            list.add(name);
                        }
                    }

                    //TODO: optimize (switch to counting/radix sort)
                    //list.sort(Integer::compareTo);
                    RadixSort.sort(list);

                    StringBuilder sb = new StringBuilder();
                    sb.append(1);

                    for (int x: list)
                        sb.append(x);

                    sb.append(0);

                    String name = sb.toString();
                    Integer intName = canonicalNameToInt.get(name);

                    if (intName == null){
                        canonicalNameToInt.put(name, freshName);
                        intName = freshName;
                        freshName++;
                    }

                    canonicalName[k].put(u, intName);

                    while (sameLabelBags[k].size() <= intName){
                        sameLabelBags[k].add(new ArrayList<>());
                    }

                    sameLabelBags[k].get(intName).add(u);
                }
            }

            if (sameLabelBags[0].size() != sameLabelBags[1].size())
                return false;

            final int m = sameLabelBags[0].size();
            for (int i = 0; i < m; i++) {
                List<V> bag1 = sameLabelBags[0].get(i);
                List<V> bag2 = sameLabelBags[1].get(i);

                if (bag1.size() != bag2.size())
                    return false;

                for (int j = 0; j < bag1.size(); j++) {
                    V u = bag1.get(j);
                    V v = bag2.get(j);

                    forwardMapping.put(u, v);
                    backwardMapping.put(v, u);
                }
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean isomorphismExists(){
        if (this.root1 == null || this.root2 == null){
            if (graph1.vertexSet().isEmpty() && graph2.vertexSet().isEmpty())
                return isomorphismExists(null, null);

            TreeMeasurer<V, E> graphMeasurer1 = new TreeMeasurer<>(graph1);
            V[] centers1 = (V[]) graphMeasurer1.getGraphCenter().toArray();

            TreeMeasurer<V, E> graphMeasurer2 = new TreeMeasurer<>(graph2);
            V[] centers2 = (V[]) graphMeasurer2.getGraphCenter().toArray();
//
//            System.out.println(graph1);
//            System.out.println(graph2);
//            System.out.println(graphMeasurer1.getGraphCenter() + " " + graphMeasurer2.getGraphCenter());

            if (centers1.length == 1 && centers2.length == 1){
                return isomorphismExists(centers1[0], centers2[0]);
            }
            else if (centers1.length == 2 && centers2.length == 2){
                if (!isomorphismExists(centers1[0], centers2[0])){
                    return isomorphismExists(centers1[1], centers2[1]);
                }
                else{
                    return true;
                }
            }
            else {
                return false;
            }
        }

        return isomorphismExists(this.root1, this.root2);
    }

    public IsomorphicTreeMapping<V, E> getIsomorphism(){
        if (forwardMapping == null){
            if (isomorphismExists())
                return new IsomorphicTreeMapping<>(forwardMapping, backwardMapping, graph1, graph2);
            else
                return null;
        }

        if (forwardMapping.size() != graph1.vertexSet().size())
            return null;
        else
            return new IsomorphicTreeMapping<>(forwardMapping, backwardMapping, graph1, graph2);
    }
}
