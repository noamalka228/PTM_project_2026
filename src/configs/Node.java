package configs;

import java.util.ArrayList;
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
        this.msg = new Message("");
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
        this.msg = Objects.requireNonNull(msg, "message cannot be null");
    }

    public void addEdge(Node n) {

    }

    public boolean hasCycles(Node n) {
        return true;
    }
    
}
