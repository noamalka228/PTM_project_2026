package server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HtmlLoader implements Servlet {
    private final Path baseDir;

    public HtmlLoader(String htmlFolder) {
        this.baseDir = Paths.get(htmlFolder).toAbsolutePath().normalize();
    }

    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        String uri = ri.getUri();
        String pathPart = uri;
        int queryIdx = uri.indexOf('?');
        if (queryIdx >= 0) {
            pathPart = uri.substring(0, queryIdx);
        }
        // strip /app prefix
        if (pathPart.startsWith("/app")) {
            pathPart = pathPart.substring("/app".length());
        }
        while (pathPart.startsWith("/")) {
            pathPart = pathPart.substring(1);
        }
        if (pathPart.isEmpty()) {
            pathPart = "index.html";
        }

        Path requested = baseDir.resolve(pathPart).normalize();
        if (!requested.startsWith(baseDir) || !Files.exists(requested) || Files.isDirectory(requested)) {
            writeResponse(toClient, "404 Not Found", "text/plain", "Not Found");
            return;
        }

        byte[] content = Files.readAllBytes(requested);
        writeResponse(toClient, "200 OK", "text/html; charset=UTF-8", content);
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

    private void writeResponse(OutputStream out, String status, String contentType, String body) throws IOException {
        writeResponse(out, status, contentType, body.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void close() throws IOException {
        // nothing to clean up
    }
}
