package configs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import graph.Agent;
import graph.Topic;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

public class Graph extends ArrayList<Node> {
    private final TopicManager tm;
    private final Map<String, Node> nodesByName = new HashMap<>();

    public Graph() {
        super();
        this.tm = TopicManagerSingleton.get();
    }

    public boolean hasCycles() {
        for (Node n : this) {
            if (n.hasCycles())
                return true;
        }
        return false;
    }

    public void createFromTopics() {
        this.nodesByName.clear();
        for (Node n : this) {
            this.nodesByName.put(n.getName(), n);
        }
        Set<String> edgeKeys = new HashSet<>();
        Collection<Topic> topics = tm.getTopics();
        for (Topic t : topics) {
            Node topicNode = getOrCreateNode("T" + t.name);
            for (Agent subscriber : t.getSubscribers()) {
                Node subscriberAgentNode = getOrCreateNode("A" + subscriber.getName());
                addEdgeIfMissing(topicNode, subscriberAgentNode, edgeKeys);
            }
            for (Agent publisher : t.getPublishers()) {
                Node publisherAgentNode = getOrCreateNode("A" + publisher.getName());
                addEdgeIfMissing(publisherAgentNode, topicNode, edgeKeys);
            }
        }
    }

    private Node getOrCreateNode(String name) {
        Node node = this.nodesByName.get(name);
        if (node == null) {
            node = new Node(name);
            this.nodesByName.put(name, node);
            this.add(node);
        }
        return node;
    }

    private void addEdgeIfMissing(Node from, Node to, Set<String> edgeKeys) {
        String key = from.getName() + "->" + to.getName();
        if (edgeKeys.add(key)) {
            from.addEdge(to);
        }
    }
}
