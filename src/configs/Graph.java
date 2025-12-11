package configs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import graph.Agent;
import graph.Topic;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

public class Graph extends ArrayList<Node> {
    private final TopicManager tm;

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
        Collection<Topic> topics = tm.getTopics();
        for (Topic t : topics) {
            Node topicNode = new Node("T" + t.name);
            if (this.getNodeByName(topicNode.getName()) == null)
                this.add(topicNode);
            for (Agent subscriber : t.getSubscribers()) {
                String subscriberName = subscriber.getName();
                Node existingAgentNode = getNodeByName("A" + subscriberName);
                if (existingAgentNode != null)
                    topicNode.addEdge(existingAgentNode);
                else
                    topicNode.addEdge(new Node("A" + subscriberName));
            }
            for (Agent publisher : t.getPublishers()) {
                String publisherName = publisher.getName();
                Node existingAgentNode = getNodeByName("A" + publisherName);
                if (existingAgentNode != null)
                    existingAgentNode.addEdge(topicNode);
                else {
                    Node agentNode = new Node("A" + publisherName);
                    agentNode.addEdge(topicNode);
                    if (this.getNodeByName(agentNode.getName()) == null)
                        this.add(agentNode);
                }
            }
        }
    }

    public Node getNodeByName(String name) {
        for (Node n : this) {
            if (n.getName().equals(name))
                return n;
        }
        return null;
    }
}
