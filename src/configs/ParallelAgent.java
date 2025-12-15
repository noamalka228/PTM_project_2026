package configs;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import graph.Agent;
import graph.Message;

public class ParallelAgent implements Agent, Runnable {
    private static final int QUEUE_POLL_MILLIS = 100;

    private static class Envelope {
        final String topic;
        final Message message;

        Envelope(String topic, Message message) {
            this.topic = topic;
            this.message = message;
        }
    }

    private final Agent delegate;
    private final BlockingQueue<Envelope> queue = new LinkedBlockingQueue<>();
    private final Thread worker;
    private volatile boolean running = true;

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
        if (!running) {
            return;
        }
        queue.offer(new Envelope(topic, msg));
    }

    @Override
    public void close() {
        running = false;
        worker.interrupt();
        try {
            worker.join(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        delegate.close();
    }

    @Override
    public void run() {
        try {
            while (running || !queue.isEmpty()) {
                Envelope env = queue.poll(QUEUE_POLL_MILLIS, TimeUnit.MILLISECONDS);
                if (env != null) {
                    delegate.callback(env.topic, env.message);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            Envelope env;
            while ((env = queue.poll()) != null) {
                delegate.callback(env.topic, env.message);
            }
        }
    }
}
