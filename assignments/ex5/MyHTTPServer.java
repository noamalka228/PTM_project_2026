package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import test.RequestParser.RequestInfo;

public class MyHTTPServer extends Thread implements HTTPServer {
    private final int serverPort;
    private final Map<String, Map<String, Servlet>> servletRegistry = new ConcurrentHashMap<>();
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    public MyHTTPServer(int port, int nThreads) {
        this.serverPort = port;
    }

    @Override
    public void addServlet(String httpCommanmd, String uri, Servlet s) {
        if (httpCommanmd == null || uri == null || s == null) {
            throw new IllegalArgumentException("httpCommand, uri, and servlet must be non-null");
        }
        String methodKey = httpCommanmd.toUpperCase();
        servletRegistry.computeIfAbsent(methodKey, k -> new ConcurrentHashMap<>()).put(uri, s);
    }

    @Override
    public void removeServlet(String httpCommanmd, String uri) {
        if (httpCommanmd == null || uri == null) {
            return;
        }
        Map<String, Servlet> perMethod = servletRegistry.get(httpCommanmd.toUpperCase());
        if (perMethod != null) {
            perMethod.remove(uri);
        }
    }

    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket(serverPort)) {
            this.serverSocket = server;
            while (running) {
                try {
                    Socket client = server.accept();
                    handleClient(client); // sequential handling per request
                } catch (SocketException se) {
                    if (running) {
                        throw se;
                    }
                }
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Server I/O error: " + e.getMessage());
            }
        }
    }

    private void handleClient(Socket client) {
        try (Socket socket = client;
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                OutputStream out = socket.getOutputStream()) {
            socket.setSoTimeout(2000);
            RequestInfo requestInfo = RequestParser.parseRequest(reader);
            Servlet servlet = lookupServlet(requestInfo);
            if (servlet != null) {
                servlet.handle(requestInfo, out);
            } else {
                sendSimpleResponse(out, "404 Not Found", "text/plain", "Not Found");
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Client handling error: " + e.getMessage());
            }
        }
    }

    private Servlet lookupServlet(RequestInfo requestInfo) {
        Map<String, Servlet> perMethod = servletRegistry.get(requestInfo.getHttpCommand().toUpperCase());
        if (perMethod == null) {
            return null;
        }
        String uri = requestInfo.getUri();
        int queryIdx = uri.indexOf('?');
        if (queryIdx >= 0) {
            uri = uri.substring(0, queryIdx);
        }
        Servlet s = perMethod.get(uri);
        if (s == null && uri.endsWith("/") && uri.length() > 1) {
            s = perMethod.get(uri.substring(0, uri.length() - 1));
        } else if (s == null && !uri.endsWith("/")) {
            s = perMethod.get(uri + "/");
        }
        return s;
    }

    private void sendSimpleResponse(OutputStream out, String status, String contentType, String body)
            throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(status).append("\r\n");
        sb.append("Content-Type: ").append(contentType).append("\r\n");
        sb.append("Content-Length: ").append(bytes.length).append("\r\n");
        sb.append("\r\n");
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        out.write(bytes);
        out.flush();
    }

    @Override
    public void start() {
        running = true;
        super.start();
    }

    @Override
    public void close() {
        running = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
            }
        }
        this.interrupt();
    }

    public int getPort() {
        return serverPort;
    }
}
