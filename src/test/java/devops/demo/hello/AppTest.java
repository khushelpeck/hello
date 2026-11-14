
package devops.demo.hello;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.Test;

public class AppTest {

    @Test
    public void testAppMain() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(output));

            new App();
            new HelloServer();

            App.main(new String[]{});

            assertTrue(output.toString().contains("Hello World!"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testHelloHandler() throws Exception {
        HttpServer server = HttpServer.create(
            new InetSocketAddress("localhost", 0), 0);

        try {
            server.createContext("/", new HelloServer.HelloHandler());
            server.start();

            int port = server.getAddress().getPort();

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/"))
                .GET()
                .build();

            HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("DevOps Demo"));
            assertTrue(response.body().contains(
                "Welcome to our first DevOps demo application."));
            assertTrue(response.headers()
                .firstValue("Content-Type")
                .orElse("")
                .contains("text/html"));

        } finally {
            server.stop(0);
        }
    }

    @Test
    public void testServerStartup() throws Exception {
        HttpServer server = HelloServer.startServer(0);

        try {
            assertTrue(server.getAddress().getPort() > 0);
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void testHelloServerMain() throws Exception {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(output));

            HelloServer.main(new String[]{});

            assertTrue(output.toString().contains(
                "Server started on http://localhost:"));
            assertNotNull(HelloServer.runningServer);

        } finally {
            if (HelloServer.runningServer != null) {
                HelloServer.runningServer.stop(0);
                HelloServer.runningServer = null;
            }

            System.setOut(originalOut);
        }
    }
}
