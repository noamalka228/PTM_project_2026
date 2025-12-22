package test;

import test.Agent;
import test.Message;
import test.TopicManagerSingleton;
import test.TopicManagerSingleton.TopicManager;

public class IncAgent implements Agent {
    private static int instanceCounter = 0;

    private final String name;
    private final String inputTopic;
    private final String outputTopic;
    private final TopicManager tm;

    public IncAgent(String[] subs, String[] pubs) {
        validateArray(subs, 1);
        validateArray(pubs, 1);

        this.name = "IncAgent#" + (++instanceCounter);
        this.inputTopic = subs[0];
        this.outputTopic = pubs[0];
        this.tm = TopicManagerSingleton.get();

        tm.getTopic(this.inputTopic).subscribe(this);
        tm.getTopic(this.outputTopic).addPublisher(this);
    }

    private static void validateArray(String[] arr, int minLength) {
        if (arr == null || arr.length < minLength) {
            throw new IllegalArgumentException("Expected at least " + minLength + " entries in ");
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void reset() {
    }

    @Override
    public void callback(String topic, Message msg) {
        if (!topic.equals(this.inputTopic)) {
            return;
        }

        double value = msg.asDouble;
        if (!Double.isNaN(value)) {
            tm.getTopic(this.outputTopic).publish(new Message(value + 1));
        }
    }

    @Override
    public void close() {
        tm.getTopic(this.inputTopic).unsubscribe(this);
        tm.getTopic(this.outputTopic).removePublisher(this);
    }
}
