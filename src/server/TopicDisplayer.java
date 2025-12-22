package server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import graph.Topic;
import graph.TopicManagerSingleton;

public class TopicDisplayer implements Servlet {

    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Topic t : TopicManagerSingleton.get().getTopics()) {
            sb.append("Topic: ").append(t.name)
                    .append(" subscribers=").append(t.getSubscribers().size())
                    .append(" publishers=").append(t.getPublishers().size())
                    .append("\n");
        }
        if (sb.length() == 0) {
            sb.append("No topics defined\n");
        }
        writeResponse(toClient, "200 OK", "text/plain", sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private void writeResponse(OutputStream out, String status, String contentType, byte[] body) throws IOException {
        StringBuilder header = new StringBuilder();
        header.append("HTTP/1.1 ").append(status).append("\r\n");
        header.append("Content-Type: ").append(contentType).append("\r\n");
        header.append("Content-Length: ").append(body.length).append("\r\n");
        header.append("\r\n");
        out.write(header.toString().getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }

    @Override
    public void close() throws IOException {
        // no resources to release
    }
}
