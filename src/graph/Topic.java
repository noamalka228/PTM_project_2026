package graph;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Topic {
    public final String name;
    private final List<Agent> subscribers = new CopyOnWriteArrayList<>();
    private final Set<Agent> publishers = ConcurrentHashMap.newKeySet();

    Topic(String name) {
        this.name = name;
    }

    public List<Agent> getSubscribers() {
        return this.subscribers;
    }

    public Set<Agent> getPublishers() {
        return this.publishers;
    }

    public void subscribe(Agent a) {
        if (!subscribers.contains(a)) {
            subscribers.add(verifyAgentNotNull(a));
        }
    }

    public void unsubscribe(Agent a) {
        subscribers.remove(verifyAgentNotNull(a));
    }

    public void publish(Message m) {
        for (Agent a : subscribers) {
            a.callback(name, m);
        }
    }

    public void addPublisher(Agent a) {
        if (!publishers.contains(a)) {
            publishers.add(verifyAgentNotNull(a));
        }
    }

    public void removePublisher(Agent a) {
        publishers.remove(verifyAgentNotNull(a));
    }

    private static Agent verifyAgentNotNull(Agent a) {
        return Objects.requireNonNull(a, "Agent cannot be null");
    }
}
