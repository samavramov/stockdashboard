package frontendold;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A minimal HTTP server that serves static files from the current directory.
 * - Listens on port 8000 by default.
 * - If the request path is "/", it serves "index.html".
 * - Otherwise, it tries to serve exactly the file that matches the URI.
 */
public class SimpleHttpServer {

    public static void main(String[] args) throws IOException {
        // 1) Choose a port (8000)
        int port = 8000;

        // 2) Create and bind the server
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // 3) Create a context for all incoming requests ("/")
       server.createContext("/", new StaticFileHandler("./frontend"));

        // 4) Use the default executor (null)
        server.setExecutor(null);

        // 5) Start the server
        server.start();
        System.out.println("🚀 Server started on http://localhost:" + port);
    }


    /**
     * This HttpHandler tries to locate a file under 'rootDir' that matches the request URI.
     * If request is "/", it maps to "/index.html".
     * If file exists, it is read and returned with an appropriate Content-Type.
     * Otherwise, returns a 404 response.
     */
    static class StaticFileHandler implements HttpHandler {
        private final Path rootPath;

        /**
         * @param rootDir the directory (relative or absolute) from which to serve files.
         */
        public StaticFileHandler(String rootDir) {
            // Convert to absolute path so we can check for directory traversal.
            this.rootPath = Paths.get(rootDir).toAbsolutePath().normalize();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 1) Extract the path from the request URI
            String requestPath = exchange.getRequestURI().getPath();

            // 2) If user requests "/", serve "index.html"
            if (requestPath.equals("/")) {
                requestPath = "/index.html";
            }

            // 3) Resolve the requested file path under rootPath
            Path filePath = rootPath.resolve("." + requestPath).normalize();
            //    - "./" prefix ensures we stay within rootPath
            //    - .normalize() prevents "../" from escaping the root directory

            // 4) If the resulting path is outside rootPath, return 404
            if (!filePath.startsWith(rootPath)) {
                send404(exchange);
                return;
            }

            // 5) Check if file exists and is not a directory
            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                // 5a) Determine MIME type (Content-Type)
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                byte[] fileBytes = Files.readAllBytes(filePath);

                // 5b) Send 200 response headers
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, fileBytes.length);

                // 5c) Write the file bytes to the response body
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(fileBytes);
                }
            } else {
                // 6) If file not found, send 404
                send404(exchange);
            }
        }

        private void send404(HttpExchange exchange) throws IOException {
            String response = "404 (Not Found)\n";
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(404, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}
