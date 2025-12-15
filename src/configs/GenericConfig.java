package configs;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import graph.Agent;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

public class GenericConfig implements Config {
    private String confFilePath;
    private final List<ParallelAgent> agents = new ArrayList<>();
    private final TopicManager tm = TopicManagerSingleton.get();

    public void setConfFile(String fileName) {
        this.confFilePath = fileName;
    }

    @Override
    public void create() {
        if (confFilePath == null || confFilePath.isEmpty()) {
            throw new IllegalStateException("Configuration file path is not set");
        }

        List<String> lines = readConfigLines();
        if (lines.size() % 3 != 0) {
            throw new IllegalArgumentException("Configuration file should contain groups of 3 lines (class, subs, pubs)");
        }

        for (int i = 0; i < lines.size(); i += 3) {
            String agentClassName = lines.get(i);
            String[] subs = parseTopics(lines.get(i + 1));
            String[] pubs = parseTopics(lines.get(i + 2));

            Agent agent = instantiateAgent(agentClassName, subs, pubs);
            ParallelAgent parallelAgent = new ParallelAgent(agent);
            rewireTopics(agent, parallelAgent, subs, pubs);
            agents.add(parallelAgent);
        }
    }

    @Override
    public String getName() {
        return this.confFilePath;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void close() {
        for (ParallelAgent pa : agents) {
            pa.close();
        }
        agents.clear();
        tm.clear();
    }

    private List<String> readConfigLines() {
        try {
            List<String> rawLines = Files.readAllLines(Path.of(confFilePath), StandardCharsets.UTF_8);
            List<String> trimmed = new ArrayList<>();
            for (String line : rawLines) {
                String l = line.trim();
                if (!l.isEmpty()) {
                    trimmed.add(l);
                }
            }
            return trimmed;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read configuration file: " + confFilePath, e);
        }
    }

    private Agent instantiateAgent(String className, String[] subs, String[] pubs) {
        try {
            Class<? extends Agent> clazz = resolveAgentClass(className);
            Constructor<? extends Agent> ctor = clazz.getConstructor(String[].class, String[].class);
            return ctor.newInstance((Object) subs, (Object) pubs);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Agent class not found: " + className, e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Agent " + className + " must have a constructor (String[] subs, String[] pubs)", e);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to instantiate agent: " + className, e);
        }
    }

    private Class<? extends Agent> resolveAgentClass(String className) throws ClassNotFoundException {
        try {
            return Class.forName(className).asSubclass(Agent.class);
        } catch (ClassNotFoundException e) {
            if (className.startsWith("src.")) {
                return Class.forName(className.substring(4)).asSubclass(Agent.class);
            }
            throw e;
        }
    }

    private void rewireTopics(Agent original, Agent wrapper, String[] subs, String[] pubs) {
        for (String topicName : subs) {
            if (topicName.isEmpty()) {
                continue;
            }
            tm.getTopic(topicName).unsubscribe(original);
            tm.getTopic(topicName).subscribe(wrapper);
        }
        for (String topicName : pubs) {
            if (topicName.isEmpty()) {
                continue;
            }
            tm.getTopic(topicName).removePublisher(original);
            tm.getTopic(topicName).addPublisher(wrapper);
        }
    }

    private String[] parseTopics(String line) {
        return Arrays.stream(line.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }
}
