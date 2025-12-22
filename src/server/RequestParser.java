package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestParser {

    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Empty request");
        }

        String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 2) {
            throw new IOException("Invalid request line: " + requestLine);
        }

        String method = requestParts[0];
        String uri = requestParts[1];
        // We can ignore the HTTP version for now

        Map<String, String> parameters = new HashMap<>();
        String[] uriSegments = parseUri(uri, parameters); // Also fills parameters from query string

        // skip headers
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
        }

        List<String> bodyLines = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            bodyLines.add(line);
        }

        int i = 0;
        while (i < bodyLines.size()) {
            String bodyLine = bodyLines.get(i);
            if (bodyLine.isEmpty()) {
                i++; // skip separator
                break;
            }
            int eqIdx = bodyLine.indexOf('=');
            if (eqIdx > 0) {
                parameters.put(bodyLine.substring(0, eqIdx), bodyLine.substring(eqIdx + 1));
                i++;
            } else {
                break;
            }
        }

        String contentString = String.join("\n", bodyLines.subList(i, bodyLines.size()));
        byte[] content = contentString.getBytes(StandardCharsets.UTF_8);

        return new RequestInfo(method, uri, uriSegments, parameters, content);
    }

    // RequestInfo given internal class
    public static class RequestInfo {
        private final String httpCommand;
        private final String uri;
        private final String[] uriSegments;
        private final Map<String, String> parameters;
        private final byte[] content;

        public RequestInfo(String httpCommand, String uri, String[] uriSegments, Map<String, String> parameters,
                byte[] content) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            this.uriSegments = uriSegments;
            this.parameters = parameters;
            this.content = content;
        }

        public String getHttpCommand() {
            return httpCommand;
        }

        public String getUri() {
            return uri;
        }

        public String[] getUriSegments() {
            return uriSegments;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public byte[] getContent() {
            return content;
        }
    }

    private static String[] parseUri(String uri, Map<String, String> parameters) {
        String path = uri;
        int queryStart = uri.indexOf('?');
        if (queryStart >= 0) {
            path = uri.substring(0, queryStart);
            String query = uri.substring(queryStart + 1);
            parseQuery(query, parameters);
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.isEmpty()) {
            return new String[0]; // Return empty array for root path
        }

        List<String> segments = new ArrayList<>();
        for (String segment : path.split("/")) {
            if (!segment.isEmpty()) {
                segments.add(segment);
            }
        }
        return segments.toArray(new String[0]);
    }

    private static void parseQuery(String query, Map<String, String> parameters) {
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                if (pair.isEmpty()) {
                    continue;
                }
                int eqIdx = pair.indexOf('=');
                if (eqIdx < 0) {
                    parameters.put(pair, ""); // This represents a parameter with no value
                } else {
                    String key = pair.substring(0, eqIdx);
                    String value = pair.substring(eqIdx + 1);
                    parameters.put(key, value);
                }
            }
        }
    }
}
