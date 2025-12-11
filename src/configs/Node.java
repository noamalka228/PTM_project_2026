package configs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import graph.Message;

public class Node {
    private String name;
    private List<Node> edges;
    private Message msg;

    public Node(String name) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.edges = new ArrayList<Node>();
        this.msg = null;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String newName) {
        this.name = Objects.requireNonNull(newName, "name cannot be null");
    }

    public List<Node> getEdges() {
        return this.edges;
    }

    public void setEdges(List<Node> newEdges) {
        Objects.requireNonNull(newEdges, "edges cannot be null");
        this.edges = new ArrayList<Node>(newEdges);
    }

    public Message getMessage() {
        return this.msg;
    }

    public void setMessage(Message msg) {
        this.msg = msg;
    }

    public void addEdge(Node n) {
        Objects.requireNonNull(n, "node cannot be null");
        if (!this.edges.contains(n))
            this.edges.add(n);
    }

    public boolean hasCycles() {
        HashSet<Node> visited = new HashSet<Node>();
        return isCreatingCycles(this, this, visited);
    }

    private boolean isCreatingCycles(Node origin, Node current, HashSet<Node> visited) {
        if (current.edges.size() > 0) {
            if (current.edges.contains(origin)) {
                return true;
            }
            if (visited.contains(current)) {
                return false;
            }
            visited.add(current);
            for (Node child : current.edges) {
                if (isCreatingCycles(origin, child, visited))
                    return true;
            }
        }
        return false;
    }
}
