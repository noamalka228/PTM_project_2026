package configs;

import java.util.Random;

import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;

public class MainTrainEx4 {

    public static void main(String[] args) {
        int c = Thread.activeCount();
        GenericConfig gc = new GenericConfig();
        gc.setConfFile("config_files/simple.conf");
        gc.create();

        if (Thread.activeCount() != c + 2) {
            System.out.println("PTM2: the configuration did not create the right number of threads.");
        }

        double result[] = { 0.0 };

        TopicManagerSingleton.get().getTopic("D").subscribe(new Agent() {

            @Override
            public String getName() {
                return "";
            }

            @Override
            public void reset() {
            }

            @Override
            public void callback(String topic, Message msg) {
                result[0] = msg.asDouble;
            }

            @Override
            public void close() {
            }

        });

        Random r = new Random();
        for (int i = 0; i < 9; i++) {
            int x, y;
            x = r.nextInt(1000);
            y = r.nextInt(1000);
            TopicManagerSingleton.get().getTopic("A").publish(new Message(x));
            TopicManagerSingleton.get().getTopic("B").publish(new Message(y));

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }

            if (result[0] != x + y + 1) {
                System.out.println("your agents did not produce the desierd result (-10)");
            }
        }

        gc.close();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }

        if (Thread.activeCount() != c) {
            System.out.println("your code did not close all threads (-10)");
        }

        System.out.println("done");

    }
}
