package test;

import java.util.function.BinaryOperator;

import test.Agent;
import test.Message;
import test.TopicManagerSingleton;
import test.TopicManagerSingleton.TopicManager;

public class BinOpAgent implements Agent {
    private final String name;
    private final String firstInputTopicName;
    private final String secondInputTopicName;
    private final String outputTopicName;
    private final BinaryOperator<Double> binOp;
    private final TopicManager tm;
    private Message firstInputMsg;
    private Message secondInputMsg;

    public BinOpAgent(String name, String firstInputTopicName, String secondInputTopicName, String outputTopicName,
            BinaryOperator<Double> binOp) {
        this.name = name;
        this.firstInputTopicName = firstInputTopicName;
        this.secondInputTopicName = secondInputTopicName;
        this.outputTopicName = outputTopicName;
        this.binOp = binOp;
        
        tm = TopicManagerSingleton.get();
        tm.getTopic(this.firstInputTopicName).subscribe(this);
        tm.getTopic(this.secondInputTopicName).subscribe(this);
        tm.getTopic(this.outputTopicName).addPublisher(this);

    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void reset() {
        firstInputMsg = new Message(0.0);
        secondInputMsg = new Message(0.0);
        firstInputMsg = null;
        secondInputMsg = null;
    }

    @Override
    public void callback(String topic, Message msg) {
        if (topic.equals(this.firstInputTopicName)) {
            firstInputMsg = msg;
        } else if (topic.equals(this.secondInputTopicName)) {
            secondInputMsg = msg;
        } else {
            return;
        }

        if (firstInputMsg != null && secondInputMsg != null) {
            double result = binOp.apply(firstInputMsg.asDouble, secondInputMsg.asDouble);
            tm.getTopic(outputTopicName).publish(new Message(result));
        }
    }

    @Override
    public void close() {
    }
}
