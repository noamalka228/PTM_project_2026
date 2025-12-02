package graph;

import java.util.List;
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

    public void subscribe(Agent a) {
        if (a != null && !subscribers.contains(a)) {
            subscribers.add(a);
        }
    }

    public void unsubscribe(Agent a) {
        subscribers.remove(a);
    }

    public void publish(Message m) {
        for (Agent a : subscribers) {
            a.callback(name, m);
        }
    }

    public void addPublisher(Agent a) {
        if (a != null) {
            publishers.add(a);
        }
    }

    public void removePublisher(Agent a) {
        publishers.remove(a);
    }
}
