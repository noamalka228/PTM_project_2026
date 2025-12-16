package configs;
import graph.Agent;
import graph.Message;
import java.util.Objects;

public class ParallelAgent implements Agent, Runnable {
    private final Agent delegate;
    private final Thread worker;

    public ParallelAgent(Agent delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate agent cannot be null");
        this.worker = new Thread(this, "ParallelAgent-" + delegate.getName());
        this.worker.start();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public void reset() {
        delegate.reset();
    }

    @Override
    public void callback(String topic, Message msg) {
        delegate.callback(topic, msg);
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public void run() {
    }
}
