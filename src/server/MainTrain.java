package server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import server.RequestParser.RequestInfo;
import server.*;

public class MainTrain { // RequestParser

    private static void testParseRequest() {
        // Test data
        String request = "GET /api/resource?id=123&name=test HTTP/1.1\n" +
                "Host: example.com\n" +
                "Content-Length: 5\n" +
                "\n" +
                "filename=\"hello_world.txt\"\n" +
                "\n" +
                "hello world!\n" +
                "\n";

        BufferedReader input = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(request.getBytes())));
        try {
            RequestParser.RequestInfo requestInfo = RequestParser.parseRequest(input);

            // Test HTTP command
            if (!requestInfo.getHttpCommand().equals("GET")) {
                System.out.println("HTTP command test failed (-5)");
            }

            // Test URI
            if (!requestInfo.getUri().equals("/api/resource?id=123&name=test")) {
                System.out.println("URI test failed (-5)");
            }

            // Test URI segments
            String[] expectedUriSegments = { "api", "resource" };
            if (!Arrays.equals(requestInfo.getUriSegments(), expectedUriSegments)) {
                System.out.println("URI segments test failed (-5)");
                for (String s : requestInfo.getUriSegments()) {
                    System.out.println(s);
                }
            }
            // Test parameters
            Map<String, String> expectedParams = new HashMap<>();
            expectedParams.put("id", "123");
            expectedParams.put("name", "test");
            expectedParams.put("filename", "\"hello_world.txt\"");
            if (!requestInfo.getParameters().equals(expectedParams)) {
                System.out.println("Parameters test failed (-5)");
            }

            // Test content
            byte[] expectedContent = "hello world!\n".getBytes();
            if (!Arrays.equals(requestInfo.getContent(), expectedContent)) {
                System.out.println("Content test failed (-5)");
            }
            input.close();
        } catch (

        IOException e) {
            System.out.println("Exception occurred during parsing: " + e.getMessage() + " (-5)");
        }
    }

    public static void testServer() throws Exception {
        int initialThreadCount = Thread.activeCount();
        MyHTTPServer server = new MyHTTPServer(5000, 4);
        server.addServlet("GET", "/publish", new TopicDisplayer());
        server.addServlet("POST", "/upload", new ConfLoader());
        server.addServlet("GET", "/app/", new HtmlLoader("html_files"));
        server.start();
        Thread.sleep(1000); // Give server time to start
        if (Thread.activeCount() - 1 != initialThreadCount) {
            System.out.println("Server thread count test failed (-10)");
        }
        int port = server.getPort();
        System.out.println("Server running on port: " + port);
        try (Socket clientSocket = new Socket("localhost", port);
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            out.print("GET /publish HTTP/1.1\r\nHost: localhost\r\n\r\n");
            out.flush();
            in.readLine(); // consume status line to ensure server replied
        }
        // server.close();
        // server.join(500);
        // Thread.sleep(500);
    }

    public static void main(String[] args) {
        testParseRequest(); // 40 points
        try {
            testServer(); // 60
        } catch (Exception e) {
            System.out.println("your server throwed an exception (-60)");
        }
        System.out.println("done");
    }

}
