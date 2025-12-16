package test;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TopicManagerSingleton {

    private static final TopicManager instance = new TopicManager();

    public static TopicManager get() {
        return instance;
    }

    public static class TopicManager {
        private final Map<String, Topic> topics = new ConcurrentHashMap<>();

        private TopicManager() { }

        public Topic getTopic(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Topic name cannot be null!");
            }
            return topics.computeIfAbsent(name, Topic::new);
        }

        public Collection<Topic> getTopics() {
            return topics.values();
        }

        public void clear() {
            topics.clear();
        }
    }
}
