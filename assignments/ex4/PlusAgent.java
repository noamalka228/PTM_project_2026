package test;

import test.Agent;
import test.Message;
import test.TopicManagerSingleton;
import test.TopicManagerSingleton.TopicManager;

public class PlusAgent implements Agent {
    private static int instanceCounter = 0;

    private final String name;
    private final String firstSubTopic;
    private final String secondSubTopic;
    private final String publishTopic;
    private final TopicManager tm;
    private double x = 0.0;
    private double y = 0.0;
    private boolean xValid = true;
    private boolean yValid = true;

    public PlusAgent(String[] subs, String[] pubs) {
        validateArray(subs, 2);
        validateArray(pubs, 1);

        this.name = "PlusAgent#" + (++instanceCounter);
        this.firstSubTopic = subs[0];
        this.secondSubTopic = subs[1];
        this.publishTopic = pubs[0];
        this.tm = TopicManagerSingleton.get();

        tm.getTopic(this.firstSubTopic).subscribe(this);
        tm.getTopic(this.secondSubTopic).subscribe(this);
        tm.getTopic(this.publishTopic).addPublisher(this);
    }

    private static void validateArray(String[] arr, int minLength) {
        if (arr == null || arr.length < minLength) {
            throw new IllegalArgumentException("Expected at least " + minLength + " entries in " + arr);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void reset() {
        this.x = 0.0;
        this.y = 0.0;
        this.xValid = true;
        this.yValid = true;
    }

    @Override
    public void callback(String topic, Message msg) {
        if (topic.equals(this.firstSubTopic)) {
            this.x = msg.asDouble;
            this.xValid = !Double.isNaN(this.x);
        } else if (topic.equals(this.secondSubTopic)) {
            this.y = msg.asDouble;
            this.yValid = !Double.isNaN(this.y);
        } else {
            return;
        }

        if (xValid && yValid) {
            double sum = this.x + this.y;
            tm.getTopic(this.publishTopic).publish(new Message(sum));
        }
    }

    @Override
    public void close() {
        tm.getTopic(this.firstSubTopic).unsubscribe(this);
        tm.getTopic(this.secondSubTopic).unsubscribe(this);
        tm.getTopic(this.publishTopic).removePublisher(this);
    }
}
