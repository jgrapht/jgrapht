package org.jgrapht.alg.blossom;

import static org.jgrapht.alg.blossom.Node.Label.PLUS;

public class Node implements Comparable<Node> {
    private boolean pseudonode;
    private boolean treeRoot;
    private boolean blossom;
    private Label label;
    private double dual;

    public Node() {
        this.pseudonode = false;
        this.treeRoot = true;
        this.blossom = false;
        this.label = PLUS;
        this.dual = 0;
    }

    public boolean isPlusNode() {
        return label == PLUS;
    }

    @Override
    public int compareTo(Node o) {
        return Double.compare(dual, o.dual);
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    enum Label {
        PLUS, MINUS, INFTY;
    }

}
