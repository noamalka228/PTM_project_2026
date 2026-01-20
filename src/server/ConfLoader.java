package server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import configs.GenericConfig;

public class ConfLoader implements Servlet {

    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        String body = new String(ri.getContent(), StandardCharsets.UTF_8);
        if (body.isEmpty()) {
            writeResponse(toClient, "400 Bad Request", "text/plain", "No configuration supplied");
            return;
        }

        Path target = Paths.get("config_files", "uploaded.conf");
        Files.createDirectories(target.getParent());
        Files.write(target, body.getBytes(StandardCharsets.UTF_8));

        GenericConfig config = new GenericConfig();
        config.setConfFile(target.toString());
        config.create();

        String response = "Configuration loaded from " + target;
        writeResponse(toClient, "200 OK", "text/plain", response);
    }

    private void writeResponse(OutputStream out, String status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        StringBuilder header = new StringBuilder();
        header.append("HTTP/1.1 ").append(status).append("\r\n");
        header.append("Content-Type: ").append(contentType).append("\r\n");
        header.append("Content-Length: ").append(bytes.length).append("\r\n");
        header.append("\r\n");
        out.write(header.toString().getBytes(StandardCharsets.UTF_8));
        out.write(bytes);
        out.flush();
    }

    @Override
    public void close() throws IOException {
        // nothing to close for now
    }
}
